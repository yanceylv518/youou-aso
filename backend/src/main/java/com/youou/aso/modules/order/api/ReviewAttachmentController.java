package com.youou.aso.modules.order.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.*;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.order.service.OrderService;
import com.youou.aso.modules.order.service.ReviewAttachmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/api/customer/review-attachments", "/api/admin/review-attachments"})
public class ReviewAttachmentController {
    private final ReviewAttachmentService attachments;
    private final OrderService orders;
    public ReviewAttachmentController(ReviewAttachmentService attachments, OrderService orders) {
        this.attachments = attachments;
        this.orders = orders;
    }

    @PostMapping
    @PreAuthorize("#account.accountType() == 'CUSTOMER' || @perm.has('order:create')")
    public ApiResponse<ReviewAttachmentService.Attachment> upload(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) Long customerId, @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(attachments.upload(customer(account, customerId), file));
    }

    @GetMapping
    @PreAuthorize("#account.accountType() == 'CUSTOMER' || @perm.hasAny('order:create', 'order:confirm', 'order:execute', 'order:pause', 'order:resume', 'order:cancel', 'order:export') || @perm.hasMenu('orders') || @perm.hasMenu('orderExecution')")
    public ApiResponse<List<ReviewAttachmentService.Attachment>> list(
            @AuthenticationPrincipal AuthenticatedAccount account, @RequestParam Long orderId) {
        if ("CUSTOMER".equals(account.accountType())) orders.getCustomerOrder(account.accountId(), orderId);
        else if ("ADMIN".equals(account.accountType())) orders.getAdminOrder(orderId);
        else throw new BusinessException(ErrorCode.FORBIDDEN);
        return ApiResponse.ok(attachments.list(orderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("#account.accountType() == 'CUSTOMER' || @perm.hasAny('order:create', 'order:confirm', 'order:execute', 'order:pause', 'order:resume', 'order:cancel', 'order:export') || @perm.hasMenu('orders') || @perm.hasMenu('orderExecution')")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable String id, @RequestParam(required = false) Long customerId) {
        var file = attachments.download(id, customer(account, customerId));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff").body(file.content());
    }

    private Long customer(AuthenticatedAccount account, Long customerId) {
        if ("CUSTOMER".equals(account.accountType())) return account.accountId();
        if ("ADMIN".equals(account.accountType()) && customerId != null) return customerId;
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
