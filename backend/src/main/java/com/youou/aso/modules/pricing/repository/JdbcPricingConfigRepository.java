package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.youou.aso.modules.order.domain.OrderType;

@Repository
public class JdbcPricingConfigRepository implements PricingConfigRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PricingConfig> rowMapper = (rs, rowNum) -> {
        PricingConfig config = new PricingConfig();
        config.setCode(PriceCode.valueOf(rs.getString("code")));
        config.setUnitPrice(rs.getBigDecimal("unit_price"));
        config.setChinaUnitPrice(rs.getBigDecimal("china_unit_price"));
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
                    "UPDATE pricing_config SET unit_price = ?, china_unit_price = ?, enabled = ?, updated_at = CURRENT_TIMESTAMP WHERE code = ?",
                    config.getUnitPrice(),
                    config.getChinaUnitPrice(),
                    config.isEnabled(),
                    config.getCode().name()
            );
        }
    }

    @Override
    public List<String> findAllowedRegionCodes(OrderType orderType) {
        return jdbcTemplate.queryForList("SELECT region_code FROM order_type_region_config WHERE order_type = ? ORDER BY region_code", String.class, orderType.name());
    }

    @Override
    public boolean hasRegionConfiguration(OrderType orderType) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_type_region_config WHERE order_type = ?", Integer.class, orderType.name());
        return count != null && count > 0;
    }

    @Override
    public Map<String, BigDecimal> findRegionPrices(PriceCode code) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT region_code, unit_price FROM pricing_region_override WHERE price_code = ? ORDER BY region_code",
                rs -> { result.put(rs.getString("region_code"), rs.getBigDecimal("unit_price")); }, code.name());
        return result;
    }

    @Override
    public Optional<BigDecimal> findRegionPrice(PriceCode code, String regionCode) {
        return jdbcTemplate.query("SELECT unit_price FROM pricing_region_override WHERE price_code = ? AND region_code = ?",
                (rs, rowNum) -> rs.getBigDecimal(1), code.name(), regionCode).stream().findFirst();
    }

    @Override
    public void replaceAllowedRegions(OrderType orderType, List<String> regionCodes) {
        jdbcTemplate.update("DELETE FROM order_type_region_config WHERE order_type = ?", orderType.name());
        regionCodes.forEach(code -> jdbcTemplate.update("INSERT INTO order_type_region_config (order_type, region_code) VALUES (?, ?)", orderType.name(), code));
    }

    @Override
    public void replaceRegionPrices(PriceCode code, Map<String, BigDecimal> regionPrices) {
        jdbcTemplate.update("DELETE FROM pricing_region_override WHERE price_code = ?", code.name());
        regionPrices.forEach((regionCode, price) -> jdbcTemplate.update(
                "INSERT INTO pricing_region_override (price_code, region_code, unit_price) VALUES (?, ?, ?)", code.name(), regionCode, price));
    }
}
