package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.WalletAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class JdbcWalletAccountRepository implements WalletAccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcWalletAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WalletAccount save(WalletAccount walletAccount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO wallet_account
                            (customer_id, balance, frozen_balance, version)
                            VALUES (?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, walletAccount.getCustomerId());
            ps.setBigDecimal(2, walletAccount.getBalance());
            ps.setBigDecimal(3, walletAccount.getFrozenBalance());
            ps.setLong(4, walletAccount.getVersion());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            walletAccount.setId(key.longValue());
        }
        return walletAccount;
    }
}
