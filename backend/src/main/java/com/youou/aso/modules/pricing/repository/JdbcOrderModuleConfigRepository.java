package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.domain.OrderModuleConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOrderModuleConfigRepository implements OrderModuleConfigRepository {
    private final JdbcTemplate jdbc;
    public JdbcOrderModuleConfigRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private OrderModuleConfig map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new OrderModuleConfig(rs.getLong("id"), rs.getString("module_name"), rs.getString("module_name_en"), rs.getString("module_name_ru"), rs.getString("module_name_pt"), rs.getString("module_name_es"),
                rs.getString("module_description"), rs.getString("module_description_en"), rs.getString("module_description_ru"), rs.getString("module_description_pt"), rs.getString("module_description_es"),
                OrderType.valueOf(rs.getString("order_type")), rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("china_unit_price"), rs.getBoolean("enabled"), rs.getInt("sort_order"), java.util.Arrays.stream(rs.getString("store_types").split(",")).map(com.youou.aso.modules.appmanagement.domain.StoreType::valueOf).toList());
    }
    public List<OrderModuleConfig> findAll() {
        return jdbc.query("SELECT * FROM order_module_config ORDER BY sort_order ASC, id ASC", this::map);
    }
    public List<OrderModuleConfig> findEnabled(OrderType type) {
        return jdbc.query("SELECT * FROM order_module_config WHERE enabled = 1 AND order_type = ? ORDER BY sort_order, id", this::map, type.name());
    }
    public Optional<OrderModuleConfig> findById(Long id) {
        return jdbc.query("SELECT * FROM order_module_config WHERE id = ?", this::map, id).stream().findFirst();
    }
    public OrderModuleConfig save(OrderModuleConfig m) {
        if (m.id() == null) {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(c -> { PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO order_module_config(module_name,module_name_en,module_name_ru,module_name_pt,module_name_es,module_description,module_description_en,module_description_ru,module_description_pt,module_description_es,order_type,unit_price,china_unit_price,enabled,sort_order,store_types) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS); bind(ps, m); return ps; }, keys);
            return findById(keys.getKey().longValue()).orElseThrow();
        }
        jdbc.update("UPDATE order_module_config SET module_name=?,module_name_en=?,module_name_ru=?,module_name_pt=?,module_name_es=?,module_description=?,module_description_en=?,module_description_ru=?,module_description_pt=?,module_description_es=?,order_type=?,unit_price=?,china_unit_price=?,enabled=?,sort_order=?,store_types=? WHERE id=?",
                m.moduleName(), m.moduleNameEn(), m.moduleNameRu(), m.moduleNamePt(), m.moduleNameEs(),
                m.moduleDescription(), m.moduleDescriptionEn(), m.moduleDescriptionRu(), m.moduleDescriptionPt(), m.moduleDescriptionEs(),
                m.orderType().name(), m.unitPrice(), m.chinaUnitPrice(), m.enabled(), m.sortOrder(), stores(m), m.id());
        return findById(m.id()).orElseThrow();
    }
    private void bind(PreparedStatement ps, OrderModuleConfig m) throws java.sql.SQLException {
        ps.setString(1,m.moduleName()); ps.setString(2,m.moduleNameEn()); ps.setString(3,m.moduleNameRu()); ps.setString(4,m.moduleNamePt()); ps.setString(5,m.moduleNameEs());
        ps.setString(6,m.moduleDescription()); ps.setString(7,m.moduleDescriptionEn()); ps.setString(8,m.moduleDescriptionRu()); ps.setString(9,m.moduleDescriptionPt()); ps.setString(10,m.moduleDescriptionEs());
        ps.setString(11,m.orderType().name()); ps.setBigDecimal(12,m.unitPrice()); ps.setBigDecimal(13,m.chinaUnitPrice()); ps.setBoolean(14,m.enabled()); ps.setInt(15,m.sortOrder()); ps.setString(16,stores(m));
    }
    private String stores(OrderModuleConfig m) { return m.storeTypes().stream().map(Enum::name).collect(java.util.stream.Collectors.joining(",")); }
    public void deleteById(Long id) { jdbc.update("DELETE FROM order_module_config WHERE id = ?", id); }
}
