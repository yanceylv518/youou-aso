package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.common.util.BusinessNumberGenerator;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class JdbcWalletService implements WalletService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final RowMapper<WalletAccount> rowMapper = (rs, rowNum) -> {
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

    public JdbcWalletService(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public WalletDebitResult debitForOrder(Long customerId, BigDecimal amount, String remark) {
        if (customerId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.WALLET_AMOUNT_INVALID);
        }

        WalletAccount wallet = jdbcTemplate.query(
                        "SELECT * FROM wallet_account WHERE customer_id = ? FOR UPDATE",
                        rowMapper,
                        customerId
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal before = wallet.getBalance();
        if (before.compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH);
        }
        BigDecimal after = before.subtract(amount);
        int updated = jdbcTemplate.update(
                """
                        UPDATE wallet_account
                        SET balance = ?, version = version + 1, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ? AND balance >= ?
                        """,
                after,
                wallet.getId(),
                amount
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.WALLET_CONCURRENT_MODIFICATION);
        }

        Long transactionId = insertTransaction(
                wallet,
                amount,
                before,
                after,
                WalletDirection.DEBIT,
                WalletTransactionType.ORDER_DEDUCT,
                remark
        );
        return new WalletDebitResult(transactionId, before, after);
    }

    @Override
    public WalletDebitResult refundForOrder(Long customerId, BigDecimal amount, String remark) {
        if (customerId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.WALLET_AMOUNT_INVALID);
        }

        WalletAccount wallet = jdbcTemplate.query(
                        "SELECT * FROM wallet_account WHERE customer_id = ? FOR UPDATE",
                        rowMapper,
                        customerId
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(amount);
        int updated = jdbcTemplate.update(
                """
                        UPDATE wallet_account
                        SET balance = ?, version = version + 1, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                after,
                wallet.getId()
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.WALLET_CONCURRENT_MODIFICATION);
        }

        Long transactionId = insertTransaction(
                wallet,
                amount,
                before,
                after,
                WalletDirection.CREDIT,
                WalletTransactionType.ORDER_REFUND,
                remark
        );
        return new WalletDebitResult(transactionId, before, after);
    }

    @Override
    public BigDecimal currentBalanceForUpdate(Long customerId) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        WalletAccount wallet = jdbcTemplate.query(
                        "SELECT * FROM wallet_account WHERE customer_id = ? FOR UPDATE",
                        rowMapper,
                        customerId
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
        return wallet.getBalance();
    }

    @Override
    public void linkTransactionToOrder(Long transactionId, Long orderId) {
        if (transactionId == null || orderId == null) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE wallet_transaction SET related_order_id = ? WHERE id = ?",
                orderId,
                transactionId
        );
    }
    private Long insertTransaction(
            WalletAccount wallet,
            BigDecimal amount,
            BigDecimal before,
            BigDecimal after,
            WalletDirection direction,
            WalletTransactionType transactionType,
            String remark
    ) {
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
            ps.setString(1, generateTransactionNo());
            ps.setLong(2, wallet.getCustomerId());
            ps.setLong(3, wallet.getId());
            ps.setString(4, direction.name());
            ps.setString(5, transactionType.name());
            ps.setBigDecimal(6, amount);
            ps.setBigDecimal(7, before);
            ps.setBigDecimal(8, after);
            ps.setObject(9, null);
            ps.setString(10, remark);
            ps.setObject(11, LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return key.longValue();
    }

    private String generateTransactionNo() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE);
        return BusinessNumberGenerator.generate("WT", now);
    }
}
