package com.youou.aso.modules.wallet.repository;

import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcWalletQueryRepository implements WalletQueryRepository {
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

    public JdbcWalletQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<WalletAccount> findAccountByCustomerId(Long customerId) {
        return jdbcTemplate.query(
                        "SELECT * FROM wallet_account WHERE customer_id = ?",
                        walletRowMapper,
                        customerId
                )
                .stream()
                .findFirst();
    }

    @Override
    public List<WalletTransaction> findTransactionsByCustomerId(Long customerId, int limit) {
        return jdbcTemplate.query(
                """
                        SELECT wt.*, o.id AS linked_order_id, o.order_no AS linked_order_no,
                               o.order_type AS linked_order_type, o.app_name AS linked_app_name
                        FROM wallet_transaction wt
                        LEFT JOIN aso_order o
                            ON wt.id = o.deducted_transaction_id
                                OR wt.id = o.refund_transaction_id
                                OR wt.related_order_id = o.id
                        WHERE wt.customer_id = ?
                        ORDER BY wt.created_at DESC, wt.id DESC
                        LIMIT ?
                        """,
                this::mapTransaction,
                customerId,
                limit
        );
    }

    @Override
    public List<WalletTransaction> findTransactions(Long customerId, WalletTransactionType transactionType, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT wt.*, o.id AS linked_order_id, o.order_no AS linked_order_no,
                       o.order_type AS linked_order_type, o.app_name AS linked_app_name
                FROM wallet_transaction wt
                LEFT JOIN aso_order o
                    ON wt.id = o.deducted_transaction_id
                        OR wt.id = o.refund_transaction_id
                        OR wt.related_order_id = o.id
                WHERE 1 = 1
                """);
        List<Object> args = new java.util.ArrayList<>();
        if (customerId != null) {
            sql.append(" AND wt.customer_id = ?");
            args.add(customerId);
        }
        if (transactionType != null) {
            sql.append(" AND wt.transaction_type = ?");
            args.add(transactionType.name());
        }
        sql.append(" ORDER BY wt.created_at DESC, wt.id DESC LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(), this::mapTransaction, args.toArray());
    }

    @Override
    public List<WalletTransaction> findTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo,
            int limit,
            int offset
    ) {
        QueryParts parts = buildTransactionQuery(
                customerId,
                transactionType,
                direction,
                orderType,
                createdDateFrom,
                createdDateTo
        );
        List<Object> args = new ArrayList<>(parts.args());
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(parts.sql() + " ORDER BY wt.created_at DESC, wt.id DESC LIMIT ? OFFSET ?", this::mapTransaction, args.toArray());
    }

    @Override
    public long countTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo
    ) {
        QueryParts parts = buildTransactionQuery(
                customerId,
                transactionType,
                direction,
                orderType,
                createdDateFrom,
                createdDateTo
        );
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + parts.sql() + ") counted",
                Long.class,
                parts.args().toArray()
        );
        return total == null ? 0 : total;
    }

    private QueryParts buildTransactionQuery(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT wt.*, o.id AS linked_order_id, o.order_no AS linked_order_no,
                       o.order_type AS linked_order_type, o.app_name AS linked_app_name
                FROM wallet_transaction wt
                LEFT JOIN aso_order o
                    ON wt.id = o.deducted_transaction_id
                        OR wt.id = o.refund_transaction_id
                        OR wt.related_order_id = o.id
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (customerId != null) {
            sql.append(" AND wt.customer_id = ?");
            args.add(customerId);
        }
        if (transactionType != null) {
            sql.append(" AND wt.transaction_type = ?");
            args.add(transactionType.name());
        }
        if (direction != null) {
            sql.append(" AND wt.direction = ?");
            args.add(direction.name());
        }
        if (orderType != null) {
            sql.append(" AND (o.order_type = ? OR wt.remark LIKE ?)");
            args.add(orderType.name());
            args.add("%:" + orderType.name() + "%");
        }
        if (createdDateFrom != null) {
            sql.append(" AND wt.created_at >= ?");
            args.add(createdDateFrom.atStartOfDay());
        }
        if (createdDateTo != null) {
            sql.append(" AND wt.created_at < ?");
            args.add(createdDateTo.plusDays(1).atStartOfDay());
        }
        return new QueryParts(sql.toString(), args);
    }

    private WalletTransaction mapTransaction(ResultSet rs, int rowNum) throws SQLException {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(rs.getLong("id"));
        transaction.setTransactionNo(rs.getString("transaction_no"));
        transaction.setCustomerId(rs.getLong("customer_id"));
        transaction.setWalletId(rs.getLong("wallet_id"));
        transaction.setDirection(WalletDirection.valueOf(rs.getString("direction")));
        transaction.setTransactionType(WalletTransactionType.valueOf(rs.getString("transaction_type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setBalanceBefore(rs.getBigDecimal("balance_before"));
        transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
        Long linkedOrderId = readLong(rs, "linked_order_id");
        transaction.setRelatedOrderId(linkedOrderId == null ? readLong(rs, "related_order_id") : linkedOrderId);
        transaction.setOrderNo(rs.getString("linked_order_no"));
        transaction.setOrderType(resolveOrderType(rs.getString("linked_order_type"), rs.getString("remark")));
        transaction.setAppName(rs.getString("linked_app_name"));
        transaction.setRemark(rs.getString("remark"));
        transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return transaction;
    }

    private OrderType resolveOrderType(String orderType, String remark) {
        if (orderType != null && !orderType.isBlank()) {
            return OrderType.valueOf(orderType);
        }
        if (remark != null && remark.startsWith("ORDER_DEDUCT:")) {
            String[] parts = remark.split(":");
            if (parts.length >= 2) {
                try {
                    return OrderType.valueOf(parts[1]);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Long readLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private record QueryParts(String sql, List<Object> args) {
    }
}
