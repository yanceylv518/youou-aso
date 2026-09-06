package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.dto.AdminMenuResult;
import com.youou.aso.modules.account.dto.AdminRoleResult;
import com.youou.aso.modules.account.dto.SaveAdminRoleCommand;

import java.util.List;
import java.util.Optional;

public interface AdminPermissionRepository {
    List<AdminMenuResult> findAllMenus();

    List<AdminRoleResult> findAllRoles();

    Optional<AdminRoleResult> findRoleById(Long id);

    Optional<AdminRoleResult> findRoleByKey(String roleKey);

    AdminRoleResult createRole(SaveAdminRoleCommand command);

    void updateRole(Long roleId, SaveAdminRoleCommand command);

    void replaceRoleMenus(Long roleId, List<Long> menuIds);

    List<Long> findRoleIdsByAdminId(Long adminId);

    List<String> findRoleKeysByAdminId(Long adminId);

    List<String> findMenuCodesByAdminId(Long adminId);

    List<String> findPermissionCodesByAdminId(Long adminId);

    void replaceAdminRoles(Long adminId, List<Long> roleIds);

    List<Long> findMenuIdsByAdminId(Long adminId);

    void replaceAdminMenus(Long adminId, List<Long> menuIds);
}
