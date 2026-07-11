package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.PasswordResetCode;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetCodeRepository {
    void invalidateActiveCodes(String email);

    PasswordResetCode save(PasswordResetCode code);

    Optional<PasswordResetCode> findLatestActiveByEmail(String email, Instant now);

    void incrementAttemptCount(Long id);

    void markUsed(Long id, Instant usedAt);
}
