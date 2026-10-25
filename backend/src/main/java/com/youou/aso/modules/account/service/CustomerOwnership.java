package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared repository boundary: apply before pagination and before locking/mutating customer data. */
public final class CustomerOwnership {
    private CustomerOwnership() {}

    public static Long restrictedAdminId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedAccount account
                && "ADMIN".equals(account.accountType()) && !"SUPER_ADMIN".equals(account.roleCode())) {
            if (account.accountId() == null) throw new BusinessException(ErrorCode.FORBIDDEN);
            return account.accountId();
        }
        return null;
    }

    public static String condition(String customerIdColumn) {
        Long adminId = restrictedAdminId();
        // Only a server-authenticated Long is interpolated; column names are repository constants.
        return adminId == null ? "" : " AND " + customerIdColumn
                + " IN (SELECT owned.id FROM customer_account owned WHERE owned.owner_admin_id = " + adminId + ")";
    }

    public static void requireCustomer(JdbcTemplate jdbc, Long customerId) {
        if (restrictedAdminId() == null) return;
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM customer_account WHERE id = ?" + condition("id"), Integer.class, customerId);
        if (count == null || count != 1) throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
