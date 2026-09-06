package com.youou.aso.modules.wallet.repository;

import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.domain.WalletTransactionTypeConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcWalletTransactionTypeConfigRepository implements WalletTransactionTypeConfigRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WalletTransactionTypeConfig> rowMapper = (rs, rowNum) -> {
        WalletTransactionTypeConfig config = new WalletTransactionTypeConfig();
        config.setTransactionType(WalletTransactionType.valueOf(rs.getString("transaction_type")));
        config.setDisplayNameZh(rs.getString("display_name_zh"));
        config.setDisplayNameEn(rs.getString("display_name_en"));
        config.setDisplayNameRu(rs.getString("display_name_ru"));
        config.setDisplayNamePt(rs.getString("display_name_pt"));
        config.setDisplayNameEs(rs.getString("display_name_es"));
        config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return config;
    };

    public JdbcWalletTransactionTypeConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<WalletTransactionTypeConfig> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM wallet_transaction_type_config ORDER BY FIELD(transaction_type, 'ORDER_DEDUCT', 'ORDER_REFUND', 'ADMIN_RECHARGE', 'ADMIN_REFUND', 'ADMIN_GIFT', 'ADMIN_DEDUCT', 'ADMIN_ADJUSTMENT', 'DELIVERY')",
                rowMapper
        );
    }

    @Override
    public void saveAll(List<WalletTransactionTypeConfig> configs) {
        for (WalletTransactionTypeConfig config : configs) {
            jdbcTemplate.update(
                    """
                            INSERT INTO wallet_transaction_type_config (transaction_type, display_name_zh, display_name_en, display_name_ru, display_name_pt, display_name_es)
                            VALUES (?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                                display_name_zh = VALUES(display_name_zh),
                                display_name_en = VALUES(display_name_en),
                                display_name_ru = VALUES(display_name_ru),
                                display_name_pt = VALUES(display_name_pt),
                                display_name_es = VALUES(display_name_es),
                                updated_at = CURRENT_TIMESTAMP
                            """,
                    config.getTransactionType().name(),
                    config.getDisplayNameZh(),
                    config.getDisplayNameEn(),
                    config.getDisplayNameRu(),
                    config.getDisplayNamePt(),
                    config.getDisplayNameEs()
            );
        }
    }
}
