<template>
  <section class="role-page">
    <header class="toolbar">
      <p class="page-note">按角色分配后台菜单和按钮权限，超级管理员默认拥有全部权限。</p>
      <div class="toolbar-actions">
        <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </header>

    <el-table v-loading="loading" class="role-table" :data="roles" empty-text="暂无角色">
      <el-table-column prop="roleName" label="角色名称" min-width="160" />
      <el-table-column prop="roleKey" label="角色编码" min-width="150" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" effect="light">
            {{ row.status === 'ENABLED' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
      <el-table-column label="权限" min-width="260">
        <template #default="{ row }">
          <span class="permission-summary">{{ permissionSummary(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" :disabled="row.roleKey === 'SUPER_ADMIN'" @click="openEditDialog(row)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="680px">
      <el-form label-position="top" class="role-form">
        <div class="form-grid">
          <el-form-item label="角色名称" required>
            <el-input v-model.trim="form.roleName" maxlength="64" />
          </el-form-item>
          <el-form-item label="角色编码" required>
            <el-input v-model.trim="form.roleKey" maxlength="32" :disabled="Boolean(editingRole)" placeholder="如 OPERATOR" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="1" :max="999" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="form.remark" maxlength="255" />
        </el-form-item>
        <el-form-item label="权限范围" required>
          <el-tree
            ref="treeRef"
            class="permission-tree"
            :data="treeData"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="treeProps"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, type TreeInstance } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  createAdminRole,
  getAdminRoles,
  getRoleMenus,
  updateAdminRole,
  type AdminMenu,
  type AdminRoleConfig,
  type RoleStatus
} from '@/api/roles'

interface MenuNode extends AdminMenu {
  label: string
  children: MenuNode[]
}

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingRole = ref<AdminRoleConfig | null>(null)
const roles = ref<AdminRoleConfig[]>([])
const menus = ref<AdminMenu[]>([])
const treeRef = ref<TreeInstance>()
const treeProps = { label: 'label', children: 'children' }
const form = reactive({
  roleKey: '',
  roleName: '',
  status: 'ENABLED' as RoleStatus,
  sortOrder: 100,
  remark: '',
  menuIds: [] as number[]
})

const treeData = computed(() => {
  const nodeMap = new Map<number, MenuNode>()
  menus.value.forEach((menu) => {
    nodeMap.set(menu.id, {
      ...menu,
      label: menu.menuType === 'BUTTON' ? `${menu.nameZh} (${menu.permissionCode || '-'})` : menu.nameZh,
      children: []
    })
  })
  const roots: MenuNode[] = []
  nodeMap.forEach((node) => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId)?.children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [nextRoles, nextMenus] = await Promise.all([getAdminRoles(), getRoleMenus()])
    roles.value = nextRoles
    menus.value = nextMenus
  } catch {
    ElMessage.error('角色权限加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingRole.value = null
  form.roleKey = ''
  form.roleName = ''
  form.status = 'ENABLED'
  form.sortOrder = 100
  form.remark = ''
  form.menuIds = []
  dialogVisible.value = true
  nextTick(() => treeRef.value?.setCheckedKeys([]))
}

function openEditDialog(role: AdminRoleConfig) {
  editingRole.value = role
  form.roleKey = role.roleKey
  form.roleName = role.roleName
  form.status = role.status
  form.sortOrder = role.sortOrder
  form.remark = role.remark || ''
  form.menuIds = role.menuIds || []
  dialogVisible.value = true
  nextTick(() => treeRef.value?.setCheckedKeys(form.menuIds))
}

async function submitRole() {
  if (!form.roleName.trim() || (!editingRole.value && !form.roleKey.trim())) {
    ElMessage.warning('请填写角色名称和角色编码')
    return
  }
  const checkedKeys = treeRef.value?.getCheckedKeys(false) || []
  const halfCheckedKeys = treeRef.value?.getHalfCheckedKeys() || []
  const menuIds = [...checkedKeys, ...halfCheckedKeys].map((key) => Number(key)).filter((id) => Number.isFinite(id))
  saving.value = true
  try {
    const payload = {
      roleKey: form.roleKey.trim(),
      roleName: form.roleName.trim(),
      status: form.status,
      sortOrder: form.sortOrder,
      remark: form.remark.trim(),
      menuIds
    }
    if (editingRole.value) {
      await updateAdminRole(editingRole.value.id, payload)
    } else {
      await createAdminRole(payload)
    }
    ElMessage.success('角色已保存')
    dialogVisible.value = false
    await loadData()
  } catch {
    ElMessage.error('角色保存失败')
  } finally {
    saving.value = false
  }
}

function permissionSummary(role: AdminRoleConfig) {
  if (role.roleKey === 'SUPER_ADMIN') return '全部权限'
  if (!role.permissionCodes.length) return '仅菜单访问'
  return role.permissionCodes.slice(0, 4).join(', ') + (role.permissionCodes.length > 4 ? ` 等 ${role.permissionCodes.length} 项` : '')
}
</script>

<style scoped>
.role-page {
  color: #182230;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.page-note {
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.role-table {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.role-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
  font-weight: 600;
}

.permission-summary {
  color: #667085;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.permission-tree {
  width: 100%;
  max-height: 360px;
  overflow: auto;
  padding: 10px 12px;
  border: 1px solid #d8dee8;
  border-radius: 8px;
}

@media (max-width: 760px) {
  .toolbar,
  .form-grid {
    display: flex;
    align-items: stretch;
    flex-direction: column;
  }
}
</style>