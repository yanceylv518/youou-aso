package com.youou.aso.modules.appmanagement.repository;

import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMarketRegionRepository implements MarketRegionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MarketRegion> rowMapper = (rs, rowNum) -> {
        MarketRegion region = new MarketRegion();
        region.setId(rs.getLong("id"));
        region.setCode(rs.getString("code"));
        region.setNameZh(rs.getString("name_zh"));
        region.setNameEn(rs.getString("name_en"));
        region.setNameRu(rs.getString("name_ru")); region.setNamePt(rs.getString("name_pt")); region.setNameEs(rs.getString("name_es"));
        region.setEnabled(rs.getBoolean("enabled"));
        region.setSupportsAppStore(readBoolean(rs, "supports_app_store", true));
        region.setSupportsGooglePlay(readBoolean(rs, "supports_google_play", true));
        region.setSupportsIpadStore(readBoolean(rs, "supports_ipad_store", true));
        region.setSortOrder(rs.getInt("sort_order"));
        return region;
    };

    public JdbcMarketRegionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<MarketRegion> findByCode(String code) {
        return jdbcTemplate.query(
                "SELECT * FROM market_region WHERE code = ?",
                rowMapper,
                code
        ).stream().findFirst();
    }

    @Override
    public List<MarketRegion> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM market_region ORDER BY sort_order ASC, code ASC",
                rowMapper
        );
    }

    @Override
    public List<MarketRegion> findEnabled() {
        return jdbcTemplate.query(
                "SELECT * FROM market_region WHERE enabled = 1 ORDER BY sort_order ASC, code ASC",
                rowMapper
        );
    }

    @Override
    public MarketRegion save(MarketRegion region) {
        jdbcTemplate.update(
                "INSERT INTO market_region (code, name_zh, name_en, name_ru, name_pt, name_es, enabled, supports_app_store, supports_google_play, supports_ipad_store, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                region.getCode(), region.getNameZh(), region.getNameEn(), region.getNameRu(), region.getNamePt(), region.getNameEs(), region.isEnabled(),
                region.isSupportsAppStore(), region.isSupportsGooglePlay(), region.isSupportsIpadStore(), region.getSortOrder()
        );
        return findByCode(region.getCode()).orElse(region);
    }

    @Override
    public void update(MarketRegion region) {
        jdbcTemplate.update(
                """
                        UPDATE market_region
                        SET name_zh = ?,
                            name_en = ?,
                            name_ru = ?, name_pt = ?, name_es = ?,
                            enabled = ?,
                            supports_app_store = ?,
                            supports_google_play = ?,
                            supports_ipad_store = ?,
                            sort_order = ?
                        WHERE code = ?
                        """,
                region.getNameZh(),
                region.getNameEn(),
                region.getNameRu(), region.getNamePt(), region.getNameEs(),
                region.isEnabled(),
                region.isSupportsAppStore(),
                region.isSupportsGooglePlay(),
                region.isSupportsIpadStore(),
                region.getSortOrder(),
                region.getCode()
        );
    }

    @Override
    public void renameAndUpdate(String originalCode, MarketRegion region) {
        if (!originalCode.equals(region.getCode())) {
            updateRegionReferences("customer_app", originalCode, region.getCode());
            updateRegionReferences("aso_order", originalCode, region.getCode());
            updateRegionReferences("aso_order_item", originalCode, region.getCode());
            updateRegionReferences("order_comment_detail", originalCode, region.getCode());
            updateRegionReferences("special_order_audit", originalCode, region.getCode());
            updateRegionReferences("special_order_audit_item", originalCode, region.getCode());
        }
        jdbcTemplate.update(
                """
                        UPDATE market_region
                        SET code = ?,
                            name_zh = ?,
                            name_en = ?,
                            name_ru = ?, name_pt = ?, name_es = ?,
                            enabled = ?,
                            supports_app_store = ?,
                            supports_google_play = ?,
                            supports_ipad_store = ?,
                            sort_order = ?
                        WHERE code = ?
                        """,
                region.getCode(),
                region.getNameZh(),
                region.getNameEn(),
                region.getNameRu(), region.getNamePt(), region.getNameEs(),
                region.isEnabled(),
                region.isSupportsAppStore(),
                region.isSupportsGooglePlay(),
                region.isSupportsIpadStore(),
                region.getSortOrder(),
                originalCode
        );
    }

    private void updateRegionReferences(String table, String originalCode, String newCode) {
        jdbcTemplate.update("UPDATE " + table + " SET region_code = ? WHERE region_code = ?", newCode, originalCode);
    }

    private boolean readBoolean(java.sql.ResultSet rs, String column, boolean fallback) throws java.sql.SQLException {
        try {
            return rs.getBoolean(column);
        } catch (java.sql.SQLException ex) {
            return fallback;
        }
    }
}
