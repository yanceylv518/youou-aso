package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AdminReferralService {
    private final JdbcTemplate jdbc;
    public AdminReferralService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public String registrationPath(Long adminId) {
        jdbc.update("INSERT INTO admin_referral (admin_id, invite_code) VALUES (?, ?) ON DUPLICATE KEY UPDATE admin_id = admin_id",
                adminId, UUID.randomUUID().toString().replace("-", ""));
        return "/register?invite=" + jdbc.queryForObject("SELECT invite_code FROM admin_referral WHERE admin_id = ?", String.class, adminId);
    }

    public Long resolveOwner(String code) {
        if (code == null || code.isBlank()) return null;
        return jdbc.query("SELECT r.admin_id FROM admin_referral r JOIN admin_account a ON a.id = r.admin_id WHERE r.invite_code = ? AND a.status = 'ENABLED'",
                (rs, row) -> rs.getLong(1), code.trim()).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITATION));
    }
}
