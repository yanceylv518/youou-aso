package com.youou.aso.modules.wallet.repository;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class JdbcAdminWalletRepository implements AdminWalletRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WalletAccount> walletRowMapper = (rs, rowNum) -> {
        WalletAccount wallet = new WalletAccount();
        wallet.setId(rs.getLong("id"));
        wallet.setCustomerId(rs.getLong("customer_id"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setFrozenBalance(rs.getBigDecimal("frozen_balance"));
        wallet.setVersion(rs.getLong("version"));
        wallet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        wallet.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return wallet;
    };

    public JdbcAdminWalletRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<WalletAccount> findByCustomerIdForUpdate(Long customerId) {
        return jdbcTemplate.query(
                        "SELECT * FROM wallet_account WHERE customer_id = ? FOR UPDATE",
                        walletRowMapper,
                        customerId
                )
                .stream()
                .findFirst();
    }

    @Override
    public void updateBalance(Long walletId, BigDecimal balance) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE wallet_account
                        SET balance = ?, version = version + 1, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                balance,
                walletId
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.WALLET_CONCURRENT_MODIFICATION);
        }
    }

    @Override
    public WalletTransaction saveTransaction(WalletTransaction transaction) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO wallet_transaction
                            (transaction_no, customer_id, wallet_id, direction, transaction_type, amount,
                             balance_before, balance_after, related_order_id, remark, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, transaction.getTransactionNo());
            ps.setLong(2, transaction.getCustomerId());
            ps.setLong(3, transaction.getWalletId());
            ps.setString(4, transaction.getDirection().name());
            ps.setString(5, transaction.getTransactionType().name());
            ps.setBigDecimal(6, transaction.getAmount());
            ps.setBigDecimal(7, transaction.getBalanceBefore());
            ps.setBigDecimal(8, transaction.getBalanceAfter());
            ps.setObject(9, transaction.getRelatedOrderId());
            ps.setString(10, transaction.getRemark());
            ps.setObject(11, transaction.getCreatedAt());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        transaction.setId(key.longValue());
        return transaction;
    }
}
