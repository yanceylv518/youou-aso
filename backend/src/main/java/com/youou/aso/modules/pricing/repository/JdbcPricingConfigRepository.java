package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPricingConfigRepository implements PricingConfigRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PricingConfig> rowMapper = (rs, rowNum) -> {
        PricingConfig config = new PricingConfig();
        config.setCode(PriceCode.valueOf(rs.getString("code")));
        config.setUnitPrice(rs.getBigDecimal("unit_price"));
        config.setEnabled(rs.getBoolean("enabled"));
        config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return config;
    };

    public JdbcPricingConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PricingConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM pricing_config ORDER BY sort_order ASC", rowMapper);
    }

    @Override
    public Optional<PricingConfig> findByCode(PriceCode code) {
        return jdbcTemplate.query("SELECT * FROM pricing_config WHERE code = ?", rowMapper, code.name())
                .stream()
                .findFirst();
    }

    @Override
    public void saveAll(List<PricingConfig> configs) {
        for (PricingConfig config : configs) {
            jdbcTemplate.update(
                    "UPDATE pricing_config SET unit_price = ?, enabled = ?, updated_at = CURRENT_TIMESTAMP WHERE code = ?",
                    config.getUnitPrice(),
                    config.isEnabled(),
                    config.getCode().name()
            );
        }
    }
}
