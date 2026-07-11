package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.AccountStatus;

import java.util.List;
import java.util.Optional;

public interface AdminAccountRepository {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AdminAccount> findById(Long id);

    Optional<AdminAccount> findByUsernameOrEmail(String account);

    List<AdminAccount> findAll();

    AdminAccount save(AdminAccount admin);

    void updateStatus(Long id, AccountStatus status);

    void updatePassword(Long id, String passwordHash, boolean forcePasswordChange);
}
