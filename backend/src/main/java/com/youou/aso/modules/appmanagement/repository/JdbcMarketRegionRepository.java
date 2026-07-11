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
    public void update(MarketRegion region) {
        jdbcTemplate.update(
                """
                        UPDATE market_region
                        SET enabled = ?,
                            supports_app_store = ?,
                            supports_google_play = ?,
                            supports_ipad_store = ?
                        WHERE code = ?
                        """,
                region.isEnabled(),
                region.isSupportsAppStore(),
                region.isSupportsGooglePlay(),
                region.isSupportsIpadStore(),
                region.getCode()
        );
    }

    private boolean readBoolean(java.sql.ResultSet rs, String column, boolean fallback) throws java.sql.SQLException {
        try {
            return rs.getBoolean(column);
        } catch (java.sql.SQLException ex) {
            return fallback;
        }
    }
}
