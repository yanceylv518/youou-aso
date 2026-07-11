package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.dto.AdminMenuResult;
import com.youou.aso.modules.account.dto.AdminRoleResult;
import com.youou.aso.modules.account.dto.SaveAdminRoleCommand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcAdminPermissionRepository implements AdminPermissionRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AdminMenuResult> menuRowMapper = (rs, rowNum) -> new AdminMenuResult(
            rs.getLong("id"),
            rs.getObject("parent_id") == null ? null : rs.getLong("parent_id"),
            rs.getString("menu_type"),
            rs.getString("menu_code"),
            rs.getString("name_zh"),
            rs.getString("name_en"),
            rs.getString("route_path"),
            rs.getString("permission_code"),
            rs.getString("icon"),
            rs.getInt("sort_order"),
            rs.getBoolean("visible"),
            rs.getString("status")
    );

    private final RowMapper<AdminRoleResult> roleRowMapper = (rs, rowNum) -> new AdminRoleResult(
            rs.getLong("id"),
            rs.getString("role_key"),
            rs.getString("role_name"),
            rs.getString("status"),
            rs.getInt("sort_order"),
            rs.getBoolean("built_in"),
            rs.getString("remark"),
            List.of(),
            List.of(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    public JdbcAdminPermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AdminMenuResult> findAllMenus() {
        return jdbcTemplate.query(
                """
                        SELECT *
                        FROM sys_menu
                        ORDER BY parent_id IS NOT NULL, COALESCE(parent_id, 0), sort_order, id
                        """,
                menuRowMapper
        );
    }

    @Override
    public List<AdminRoleResult> findAllRoles() {
        return jdbcTemplate.query(
                        "SELECT * FROM sys_role ORDER BY sort_order, id",
                        roleRowMapper
                ).stream()
                .map(this::withRoleMenus)
                .toList();
    }

    @Override
    public Optional<AdminRoleResult> findRoleById(Long id) {
        return jdbcTemplate.query("SELECT * FROM sys_role WHERE id = ?", roleRowMapper, id)
                .stream()
                .findFirst()
                .map(this::withRoleMenus);
    }

    @Override
    public Optional<AdminRoleResult> findRoleByKey(String roleKey) {
        return jdbcTemplate.query("SELECT * FROM sys_role WHERE role_key = ?", roleRowMapper, roleKey)
                .stream()
                .findFirst()
                .map(this::withRoleMenus);
    }

    @Override
    public AdminRoleResult createRole(SaveAdminRoleCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO sys_role (role_key, role_name, status, sort_order, built_in, remark)
                            VALUES (?, ?, ?, ?, FALSE, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, command.roleKey().trim().toUpperCase());
            ps.setString(2, command.roleName().trim());
            ps.setString(3, normalizeStatus(command.status()));
            ps.setInt(4, command.sortOrder() == null ? 100 : command.sortOrder());
            ps.setString(5, command.remark());
            return ps;
        }, keyHolder);
        Long roleId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        replaceRoleMenus(roleId, command.menuIds());
        return findRoleById(roleId).orElseThrow();
    }

    @Override
    public void updateRole(Long roleId, SaveAdminRoleCommand command) {
        jdbcTemplate.update(
                """
                        UPDATE sys_role
                        SET role_name = ?, status = ?, sort_order = ?, remark = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                command.roleName().trim(),
                normalizeStatus(command.status()),
                command.sortOrder() == null ? 100 : command.sortOrder(),
                command.remark(),
                roleId
        );
        replaceRoleMenus(roleId, command.menuIds());
    }

    @Override
    @Transactional
    public void replaceRoleMenus(Long roleId, List<Long> menuIds) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<Object[]> batch = menuIds.stream()
                .distinct()
                .map(menuId -> new Object[]{roleId, menuId})
                .toList();
        jdbcTemplate.batchUpdate(
                "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)",
                batch
        );
    }

    @Override
    public List<Long> findRoleIdsByAdminId(Long adminId) {
        return jdbcTemplate.queryForList(
                "SELECT role_id FROM admin_account_role WHERE admin_id = ? ORDER BY role_id",
                Long.class,
                adminId
        );
    }

    @Override
    public List<String> findRoleKeysByAdminId(Long adminId) {
        return jdbcTemplate.queryForList(
                """
                        SELECT r.role_key
                        FROM admin_account_role ar
                        JOIN sys_role r ON r.id = ar.role_id
                        WHERE ar.admin_id = ? AND r.status = 'ENABLED'
                        ORDER BY r.sort_order, r.id
                        """,
                String.class,
                adminId
        );
    }

    @Override
    public List<String> findMenuCodesByAdminId(Long adminId) {
        return jdbcTemplate.queryForList(
                """
                        SELECT DISTINCT m.menu_code
                        FROM admin_account_role ar
                        JOIN sys_role r ON r.id = ar.role_id AND r.status = 'ENABLED'
                        JOIN sys_role_menu rm ON rm.role_id = r.id
                        JOIN sys_menu m ON m.id = rm.menu_id AND m.status = 'ENABLED' AND m.menu_type <> 'BUTTON'
                        WHERE ar.admin_id = ? AND m.menu_code IS NOT NULL
                        ORDER BY m.menu_code
                        """,
                String.class,
                adminId
        );
    }

    @Override
    public List<String> findPermissionCodesByAdminId(Long adminId) {
        return jdbcTemplate.queryForList(
                """
                        SELECT DISTINCT m.permission_code
                        FROM admin_account_role ar
                        JOIN sys_role r ON r.id = ar.role_id AND r.status = 'ENABLED'
                        JOIN sys_role_menu rm ON rm.role_id = r.id
                        JOIN sys_menu m ON m.id = rm.menu_id AND m.status = 'ENABLED'
                        WHERE ar.admin_id = ? AND m.permission_code IS NOT NULL
                        ORDER BY m.permission_code
                        """,
                String.class,
                adminId
        );
    }

    @Override
    @Transactional
    public void replaceAdminRoles(Long adminId, List<Long> roleIds) {
        jdbcTemplate.update("DELETE FROM admin_account_role WHERE admin_id = ?", adminId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Object[]> batch = roleIds.stream()
                .distinct()
                .map(roleId -> new Object[]{adminId, roleId})
                .toList();
        jdbcTemplate.batchUpdate(
                "INSERT INTO admin_account_role (admin_id, role_id) VALUES (?, ?)",
                batch
        );
    }

    private AdminRoleResult withRoleMenus(AdminRoleResult role) {
        List<Long> menuIds = jdbcTemplate.queryForList(
                "SELECT menu_id FROM sys_role_menu WHERE role_id = ? ORDER BY menu_id",
                Long.class,
                role.id()
        );
        List<String> permissionCodes = new ArrayList<>();
        if (!menuIds.isEmpty()) {
            permissionCodes = jdbcTemplate.queryForList(
                    """
                            SELECT DISTINCT permission_code
                            FROM sys_menu
                            WHERE permission_code IS NOT NULL
                              AND id IN (SELECT menu_id FROM sys_role_menu WHERE role_id = ?)
                            ORDER BY permission_code
                            """,
                    String.class,
                    role.id()
            );
        }
        return new AdminRoleResult(
                role.id(),
                role.roleKey(),
                role.roleName(),
                role.status(),
                role.sortOrder(),
                role.builtIn(),
                role.remark(),
                menuIds,
                permissionCodes,
                role.createdAt(),
                role.updatedAt()
        );
    }

    private String normalizeStatus(String status) {
        return "DISABLED".equals(status) ? "DISABLED" : "ENABLED";
    }
}
