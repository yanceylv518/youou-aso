package com.youou.aso.modules.order.service;

import com.youou.aso.common.error.*;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.io.IOException;

@Service
public class ReviewAttachmentService {
    private final JdbcTemplate jdbc;
    public ReviewAttachmentService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Attachment(String id, String fileName, long fileSize, String regionCode) {}
    public record Download(String fileName, byte[] content) {}

    public Attachment upload(Long customerId, MultipartFile file) {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        if (name.isBlank() || name.length() > 255 || name.chars().anyMatch(Character::isISOControl)
                || !(name.toLowerCase(Locale.ROOT).endsWith(".xlsx") || name.toLowerCase(Locale.ROOT).endsWith(".csv")))
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        if (file.isEmpty() || file.getSize() > 20 * 1024 * 1024) throw new BusinessException(ErrorCode.BAD_REQUEST);
        String id = UUID.randomUUID().toString();
        try {
            jdbc.update("INSERT INTO aso_review_attachment (id, customer_id, file_name, file_size, content) VALUES (?, ?, ?, ?, ?)",
                    id, customerId, name, file.getSize(), file.getBytes());
        } catch (IOException e) { throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED); }
        return new Attachment(id, name, file.getSize(), null);
    }

    public void bind(AsoOrder order, CreateOrderCommand command) {
        jdbc.update("DELETE FROM aso_order_review_attachment WHERE order_id = ?", order.getId());
        if (command.orderType() != OrderType.REVIEW || command.regionItems() == null) return;
        Set<String> seen = new HashSet<>();
        for (var region : command.regionItems()) {
            if (region == null || region.attachmentIds() == null) continue;
            if (region.attachmentIds().size() > 20) throw new BusinessException(ErrorCode.BAD_REQUEST);
            for (String id : region.attachmentIds()) {
                if (id == null || !seen.add(id)) throw new BusinessException(ErrorCode.BAD_REQUEST);
                // Preserve historical attachments when renewing or resubmitting an old order.
                if (id.startsWith("legacy:")) {
                    Download file = download(id, order.getCustomerId());
                    id = UUID.randomUUID().toString();
                    jdbc.update("INSERT INTO aso_review_attachment (id, customer_id, file_name, file_size, content) VALUES (?, ?, ?, ?, ?)",
                            id, order.getCustomerId(), file.fileName(), file.content().length, file.content());
                }
                if (jdbc.queryForObject("SELECT COUNT(*) FROM aso_review_attachment WHERE id = ? AND customer_id = ?", Integer.class, id, order.getCustomerId()) != 1)
                    throw new BusinessException(ErrorCode.BAD_REQUEST);
                jdbc.update("INSERT INTO aso_order_review_attachment (order_id, attachment_id, region_code) VALUES (?, ?, ?)", order.getId(), id, region.regionCode());
            }
        }
    }

    public List<Attachment> list(Long orderId) {
        List<Attachment> result = new ArrayList<>(jdbc.query("SELECT a.id, a.file_name, a.file_size, r.region_code FROM aso_review_attachment a JOIN aso_order_review_attachment r ON a.id = r.attachment_id WHERE r.order_id = ? ORDER BY a.created_at, a.id",
                (rs, n) -> new Attachment(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getString(4)), orderId));
        // Historical comments are available on demand, never in order page responses.
        jdbc.query("SELECT DISTINCT region_code FROM aso_order_comment_detail WHERE order_id = ?", (rs, n) -> rs.getString(1), orderId)
                .forEach(region -> result.add(new Attachment("legacy:" + orderId + ":" + region, "reviews-" + region + ".csv", 0, region)));
        return result;
    }

    public Download download(String id, Long customerId) {
        if (id.startsWith("legacy:")) {
            String[] parts = id.split(":", 3);
            if (parts.length != 3) throw new BusinessException(ErrorCode.NOT_FOUND);
            Long orderId;
            try { orderId = Long.valueOf(parts[1]); } catch (NumberFormatException e) { throw new BusinessException(ErrorCode.NOT_FOUND); }
            if (jdbc.queryForObject("SELECT COUNT(*) FROM aso_order WHERE id = ? AND customer_id = ?", Integer.class, orderId, customerId) != 1)
                throw new BusinessException(ErrorCode.NOT_FOUND);
            StringBuilder csv = new StringBuilder("\ufeffStar,Title,Content\r\n");
            jdbc.query("SELECT star_level, comment_title, comment_content FROM aso_order_comment_detail WHERE order_id = ? AND region_code = ? ORDER BY id", (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                csv.append(rs.getInt(1)).append(',').append(cell(rs.getString(2))).append(',').append(cell(rs.getString(3))).append("\r\n");
            }, orderId, parts[2]);
            return new Download("reviews-" + parts[2] + ".csv", csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return jdbc.query("SELECT file_name, content FROM aso_review_attachment WHERE id = ? AND customer_id = ?",
                (rs, n) -> new Download(rs.getString(1), rs.getBytes(2)), id, customerId).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }
    private static String cell(String value) {
        String safe = value == null ? "" : value;
        if (safe.stripLeading().matches("(?s)^[=+@\\-].*")) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
