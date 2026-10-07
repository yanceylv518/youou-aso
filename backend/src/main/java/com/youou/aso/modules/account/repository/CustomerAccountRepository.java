package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.domain.AccountStatus;

import java.util.List;
import java.util.Optional;

public interface CustomerAccountRepository {
    default void lockRegistration() {}

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<CustomerAccount> findById(Long id);

    default List<CustomerAccount> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .distinct()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    Optional<CustomerAccount> findByUsernameOrEmail(String account);

    List<CustomerAccount> findAll(String keyword);

    default List<CustomerAccount> findAll(String keyword, int limit, int offset) {
        return findAll(keyword).stream().skip(offset).limit(limit).toList();
    }

    default long countAll(String keyword) {
        return findAll(keyword).size();
    }

    CustomerAccount save(CustomerAccount customer);

    void updateStatus(Long id, AccountStatus status);

    void updatePassword(Long id, String passwordHash, boolean forcePasswordChange);
}
