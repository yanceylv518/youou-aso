<template>
  <section class="admin-permission-page">
    <div class="permission-layout">
      <aside class="admin-list-panel">
        <div class="panel-head">
          <strong>管理员列表</strong>
          <el-button type="primary" link :icon="Plus" @click="openCreateDialog">新增管理员</el-button>
        </div>
        <el-input
          v-model.trim="adminKeyword"
          class="admin-search"
          clearable
          placeholder="搜索管理员"
          :prefix-icon="Search"
        />
        <el-scrollbar class="admin-scroll">
          <button
            v-for="admin in filteredAdmins"
            :key="admin.id"
            class="admin-row"
            :class="{ active: selectedAdmin?.id === admin.id }"
            type="button"
            @click="selectAdmin(admin)"
          >
            <span class="admin-avatar">{{ admin.username.slice(0, 1).toUpperCase() }}</span>
            <span class="admin-meta">
              <span class="admin-name">{{ admin.username }}</span>
              <span class="admin-email">{{ admin.email }}</span>
              <span class="admin-tags">
                <el-tag size="small" :type="statusTag(admin.status)" effect="light">
                  {{ t(`customers.statuses.${admin.status}`) }}
                </el-tag>
                <el-tag v-if="admin.roleCode === 'SUPER_ADMIN'" size="small" type="danger" effect="light">
                  超级管理员
                </el-tag>
                <el-tag v-else v-for="roleKey in admin.roleKeys" :key="roleKey" size="small" effect="light">
                  {{ roleName(roleKey) }}
                </el-tag>
              </span>
            </span>
          </button>
          <el-empty v-if="!loading && filteredAdmins.length === 0" description="暂无管理员" />
        </el-scrollbar>
      </aside>

      <section class="detail-panel">
        <template v-if="selectedAdmin">
          <div class="profile-card">
            <div class="profile-main">
              <span class="profile-avatar">{{ selectedAdmin.username.slice(0, 1).toUpperCase() }}</span>
              <div class="profile-text">
                <div class="profile-title">
                  <h2>{{ selectedAdmin.username }}</h2>
                  <el-tag
                    v-if="selectedAdmin.roleCode === 'SUPER_ADMIN'"
                    type="danger"
                    effect="light"
                    round
                  >
                    超级管理员
                  </el-tag>
                </div>
                <p>
                  {{ selectedAdmin.email }}
                  <el-button class="copy-button" :icon="CopyDocument" text @click="copyEmail" />
                </p>
              </div>
            </div>
            <div class="profile-actions">
              <el-button
                v-if="selectedAdmin.status !== 'ENABLED'"
                type="success"
                :disabled="selectedAdmin.id === auth.accountId"
                @click="changeStatus(selectedAdmin, 'ENABLED')"
              >
                启用
              </el-button>
              <el-button
                v-if="selectedAdmin.status !== 'DISABLED'"
                :disabled="selectedAdmin.id === auth.accountId"
                @click="changeStatus(selectedAdmin, 'DISABLED')"
              >
                禁用
              </el-button>
              <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
            </div>
          </div>

          <div class="summary-grid">
            <div class="summary-item">
              <span>账号 ID</span>
              <strong>{{ selectedAdmin.id }}</strong>
            </div>
            <div class="summary-item">
              <span>状态</span>
              <strong>
                <el-tag size="small" :type="statusTag(selectedAdmin.status)" effect="light">
                  {{ t(`customers.statuses.${selectedAdmin.status}`) }}
                </el-tag>
              </strong>
            </div>
            <div class="summary-item">
              <span>最后登录</span>
              <strong>{{ formatDate(selectedAdmin.lastLoginAt) }}</strong>
            </div>
            <div class="summary-item">
              <span>创建时间</span>
              <strong>{{ formatDate(selectedAdmin.createdAt) }}</strong>
            </div>
          </div>

          <el-alert
            v-if="selectedAdmin.roleCode === 'SUPER_ADMIN'"
            class="super-alert"
            type="primary"
            show-icon
            :closable="false"
            title="超级管理员拥有系统全部菜单和接口权限，无法修改权限配置。"
          />



          <div class="permission-card">
            <div class="permission-toolbar">
              <strong>权限信息</strong>
              <div class="permission-tools">
                <el-input
                  v-model.trim="permissionKeyword"
                  class="permission-search"
                  clearable
                  placeholder="搜索权限名称或标识"
                  :prefix-icon="Search"
                />
                <el-button :icon="Expand" @click="expandAll">展开全部</el-button>
                <el-button :icon="Fold" @click="collapseAll">收起全部</el-button>
              </div>
            </div>

            <el-table
              ref="permissionTableRef"
              class="permission-table"
              :data="permissionRows"
              row-key="id"
              default-expand-all
              :tree-props="{ children: 'children' }"
              empty-text="暂无权限"
            >
              <el-table-column type="selection" width="44" />
              <el-table-column prop="nameZh" label="权限名称" min-width="180" />
              <el-table-column label="权限标识" min-width="180">
                <template #default="{ row }">
                  {{ row.permissionCode || row.menuCode || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="类型" width="110">
                <template #default="{ row }">{{ menuTypeLabel(row.menuType) }}</template>
              </el-table-column>
              <el-table-column label="描述" min-width="180">
                <template #default="{ row }">{{ rowDescription(row) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>

        <el-empty v-else description="请选择左侧管理员" />
      </section>
    </div>

    <el-dialog v-model="createDialogVisible" title="新增管理员" width="460px">
      <el-form label-position="top" class="create-form">
        <el-form-item label="用户名" required>
          <el-input v-model.trim="createForm.username" maxlength="64" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model.trim="createForm.email" maxlength="128" />
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input
            v-model="createForm.password"
            type="password"
            maxlength="72"
            show-password
            placeholder="请输入 8-72 位初始密码"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.roleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色">
            <el-option v-for="role in assignableRoles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type TableInstance } from 'element-plus'
import { CopyDocument, Expand, Fold, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createAdminAccount,
  getAdminAccounts,
  updateAdminAccountRoles,
  updateAdminAccountStatus,
  type AdminAccount
} from '@/api/adminAccounts'
import type { AccountStatus } from '@/api/customers'
import { getAdminRoles, getRoleMenus, type AdminMenu, type AdminRoleConfig, type MenuType } from '@/api/roles'
import { useAuthStore } from '@/stores/auth'

interface PermissionNode extends AdminMenu {
  children?: PermissionNode[]
}

const { t } = useI18n()
const auth = useAuthStore()
const loading = ref(false)
const creating = ref(false)
const savingRoles = ref(false)
const createDialogVisible = ref(false)
const adminKeyword = ref('')
const permissionKeyword = ref('')
const admins = ref<AdminAccount[]>([])
const roleOptions = ref<AdminRoleConfig[]>([])
const menus = ref<AdminMenu[]>([])
const selectedAdmin = ref<AdminAccount | null>(null)
const selectedRoleIds = ref<number[]>([])
const permissionTableRef = ref<TableInstance>()
const displayParentByPermission: Record<string, string> = {
  'app:add': 'applications',
  'app:delete': 'applications',
  'order:confirm': 'orders_pending_confirm',
  'order:execute': 'order_execution_pending',
  'order:cancel': 'orders',
  'order:export': 'orders',
  'wallet:adjust': 'finance_transactions'
}
const createForm = reactive({
  username: '',
  email: '',
  password: '',
  roleIds: [] as number[]
})

const assignableRoles = computed(() => roleOptions.value.filter((role) => role.roleKey !== 'SUPER_ADMIN' && role.status === 'ENABLED'))
const selectedRoles = computed(() => roleOptions.value.filter((role) => selectedRoleIds.value.includes(role.id)))
const selectedMenuIdSet = computed(() => {
  if (selectedAdmin.value?.roleCode === 'SUPER_ADMIN') {
    return new Set(menus.value.map((menu) => menu.id))
  }
  return new Set(selectedRoles.value.flatMap((role) => role.menuIds || []))
})
const filteredAdmins = computed(() => {
  const keyword = adminKeyword.value.toLowerCase()
  if (!keyword) return admins.value
  return admins.value.filter((admin) => admin.username.toLowerCase().includes(keyword) || admin.email.toLowerCase().includes(keyword))
})
const permissionRows = computed(() => {
  const allowed = menus.value.filter((menu) => selectedMenuIdSet.value.has(menu.id))
  const keyword = permissionKeyword.value.toLowerCase()
  const visibleIds = new Set<number>()

  if (keyword) {
    const menuMap = new Map(allowed.map((menu) => [menu.id, menu]))
    allowed.forEach((menu) => {
      const matched = [menu.nameZh, menu.nameEn, menu.menuCode, menu.permissionCode]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
      if (matched) {
        visibleIds.add(menu.id)
        let parentId = menu.parentId
        while (parentId && menuMap.has(parentId)) {
          visibleIds.add(parentId)
          parentId = menuMap.get(parentId)?.parentId || null
        }
      }
    })
  }

  const source = keyword ? allowed.filter((menu) => visibleIds.has(menu.id)) : allowed
  return buildPermissionTree(withDisplayParents(source))
})

onMounted(loadAll)

watch(selectedAdmin, (admin) => {
  selectedRoleIds.value = admin?.roleIds ? [...admin.roleIds] : []
  permissionKeyword.value = ''
})

async function loadAll() {
  loading.value = true
  try {
    const [nextAdmins, nextRoles, nextMenus] = await Promise.all([getAdminAccounts(), getAdminRoles(), getRoleMenus()])
    admins.value = nextAdmins
    roleOptions.value = nextRoles
    menus.value = nextMenus
    if (selectedAdmin.value) {
      selectedAdmin.value = nextAdmins.find((admin) => admin.id === selectedAdmin.value?.id) || nextAdmins[0] || null
    } else {
      selectedAdmin.value = nextAdmins[0] || null
    }
  } catch {
    ElMessage.error('管理员权限数据加载失败')
  } finally {
    loading.value = false
  }
}

function selectAdmin(admin: AdminAccount) {
  selectedAdmin.value = admin
}

function openCreateDialog() {
  createForm.username = ''
  createForm.email = ''
  createForm.password = ''
  createForm.roleIds = assignableRoles.value.slice(0, 1).map((role) => role.id)
  createDialogVisible.value = true
}

async function submitCreate() {
  if (!createForm.username.trim() || !createForm.email.trim() || createForm.password.length < 8) {
    ElMessage.warning('请填写用户名、邮箱和至少 8 位初始密码')
    return
  }
  creating.value = true
  try {
    const created = await createAdminAccount({
      username: createForm.username.trim(),
      email: createForm.email.trim(),
      password: createForm.password,
      roleIds: createForm.roleIds
    })
    ElMessage.success('管理员已创建')
    createDialogVisible.value = false
    await loadAll()
    await nextTick()
    selectedAdmin.value = admins.value.find((admin) => admin.id === created.id) || selectedAdmin.value
  } catch {
    ElMessage.error('管理员创建失败')
  } finally {
    creating.value = false
  }
}

async function submitRoles() {
  if (!selectedAdmin.value || selectedAdmin.value.roleCode === 'SUPER_ADMIN') return
  if (selectedRoleIds.value.length === 0) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  savingRoles.value = true
  try {
    const updated = await updateAdminAccountRoles(selectedAdmin.value.id, selectedRoleIds.value)
    ElMessage.success('管理员权限已更新')
    const index = admins.value.findIndex((admin) => admin.id === updated.id)
    if (index >= 0) admins.value[index] = updated
    selectedAdmin.value = updated
  } catch {
    ElMessage.error('管理员权限保存失败')
  } finally {
    savingRoles.value = false
  }
}

async function changeStatus(row: AdminAccount, status: AccountStatus) {
  const statusText = t(`customers.statuses.${status}`)
  try {
    await ElMessageBox.confirm(`确认将管理员“${row.username}”状态改为“${statusText}”吗？`, '调整管理员状态', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const updated = await updateAdminAccountStatus(row.id, status)
    ElMessage.success('管理员状态已更新')
    const index = admins.value.findIndex((admin) => admin.id === updated.id)
    if (index >= 0) admins.value[index] = updated
    selectedAdmin.value = updated
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('管理员状态更新失败')
    }
  }
}

async function copyEmail() {
  if (!selectedAdmin.value?.email) return
  try {
    await navigator.clipboard.writeText(selectedAdmin.value.email)
    ElMessage.success('邮箱已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

function withDisplayParents(source: AdminMenu[]) {
  const allMenuMap = new Map(menus.value.map((menu) => [menu.id, menu]))
  const allMenuCodeMap = new Map(menus.value.map((menu) => [menu.menuCode, menu]))
  const result = new Map<number, AdminMenu>()

  function addWithParents(menu: AdminMenu) {
    const displayParentCode = menu.parentId ? null : displayParentByPermission[menu.permissionCode || '']
    const displayParent = displayParentCode ? allMenuCodeMap.get(displayParentCode) : undefined
    const normalizedMenu = displayParent ? { ...menu, parentId: displayParent.id } : menu
    result.set(normalizedMenu.id, normalizedMenu)

    let parentId = normalizedMenu.parentId
    while (parentId && allMenuMap.has(parentId)) {
      const parent = allMenuMap.get(parentId)!
      result.set(parent.id, parent)
      parentId = parent.parentId
    }
  }

  source.forEach(addWithParents)
  return Array.from(result.values())
}

function buildPermissionTree(source: AdminMenu[]) {
  const nodeMap = new Map<number, PermissionNode>()
  source
    .slice()
    .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    .forEach((menu) => {
      nodeMap.set(menu.id, { ...menu, children: [] })
    })

  const roots: PermissionNode[] = []
  nodeMap.forEach((node) => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId)?.children?.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function expandAll() {
  setAllRowsExpanded(permissionRows.value, true)
}

function collapseAll() {
  setAllRowsExpanded(permissionRows.value, false)
}

function setAllRowsExpanded(rows: PermissionNode[], expanded: boolean) {
  rows.forEach((row) => {
    permissionTableRef.value?.toggleRowExpansion(row, expanded)
    if (row.children?.length) setAllRowsExpanded(row.children, expanded)
  })
}

function roleName(roleKey: string) {
  return roleOptions.value.find((role) => role.roleKey === roleKey)?.roleName || roleKey
}

function statusTag(status: AccountStatus) {
  return status === 'ENABLED' ? 'success' : 'danger'
}

function menuTypeLabel(type: MenuType) {
  const labels: Record<MenuType, string> = {
    CATALOG: '目录',
    MENU: '菜单',
    BUTTON: '按钮'
  }
  return labels[type]
}

function rowDescription(row: AdminMenu) {
  if (row.menuType === 'BUTTON') return row.nameZh
  return `${row.nameZh}模块`
}

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.admin-permission-page {
  color: #182230;
}

.permission-layout {
  min-height: 680px;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
}

.admin-list-panel,
.detail-panel {
  min-width: 0;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
  background: #ffffff;
}

.admin-list-panel {
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 10px;
}

.panel-head strong {
  font-size: 15px;
}

.admin-search {
  width: calc(100% - 32px);
  margin: 0 16px 12px;
}

.admin-scroll {
  height: 590px;
}

.admin-row {
  width: 100%;
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border: 0;
  border-left: 3px solid transparent;
  background: #ffffff;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.admin-row:hover,
.admin-row.active {
  background: #f3f8ff;
}

.admin-row.active {
  border-left-color: #1f7ae0;
}

.admin-avatar,
.profile-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #ffb11b;
  color: #d9361f;
  font-weight: 800;
}

.admin-avatar {
  width: 36px;
  height: 36px;
}

.admin-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.admin-name {
  font-weight: 800;
}

.admin-email {
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.detail-panel {
  padding: 0 18px 18px;
}

.profile-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 0 18px;
  border-bottom: 1px solid #edf1f7;
}

.profile-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-avatar {
  width: 54px;
  height: 54px;
  font-size: 22px;
}

.profile-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-title h2 {
  margin: 0;
  color: #182230;
  font-size: 24px;
  line-height: 1.2;
}

.profile-text p {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 6px 0 0;
  color: #667085;
}

.copy-button {
  width: 22px;
  height: 22px;
  padding: 0;
}

.profile-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin: 0 -18px;
  border-bottom: 1px solid #edf1f7;
}

.summary-item {
  min-height: 70px;
  padding: 16px 18px;
  border-right: 1px solid #edf1f7;
}

.summary-item:last-child {
  border-right: 0;
}

.summary-item span {
  display: block;
  margin-bottom: 8px;
  color: #667085;
  font-size: 12px;
}

.summary-item strong {
  color: #182230;
  font-size: 14px;
}

.super-alert {
  margin: 16px 0;
}

.permission-card {
  margin-top: 16px;
}

.permission-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.permission-toolbar strong {
  font-size: 16px;
}

.permission-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-search {
  width: 260px;
}

.permission-table {
  border: 1px solid #edf1f7;
  border-radius: 6px;
}

:deep(.permission-table .el-table__cell) {
  padding: 8px 0;
}

@media (max-width: 1080px) {
  .permission-layout,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .admin-scroll {
    height: 360px;
  }

  .summary-item {
    border-right: 0;
    border-bottom: 1px solid #edf1f7;
  }

  .profile-card,
  .permission-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .permission-search {
    width: 100%;
  }

  .permission-tools {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>



