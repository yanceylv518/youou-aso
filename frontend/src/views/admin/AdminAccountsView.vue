<template>
  <section class="admin-permission-page">
    <div class="permission-layout">
      <aside class="admin-list-panel">
        <div class="panel-head">
          <div><strong>{{ t('adminAccounts.adminList') }}</strong><span>{{ t('adminAccounts.accountCount', { count: filteredAdmins.length }) }}</span></div>
          <el-button type="primary" link :icon="Plus" @click="openCreateDialog">{{ t('adminAccounts.addAdmin') }}</el-button>
        </div>
        <el-input
          v-model.trim="adminKeyword"
          class="admin-search"
          clearable
          :placeholder="t('adminAccounts.searchAdmin')"
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
                  {{ t('adminAccounts.superAdmin') }}
                </el-tag>
              </span>
            </span>
          </button>
          <el-empty v-if="!loading && filteredAdmins.length === 0" :description="t('adminAccounts.noAdmins')" />
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
                    {{ t('adminAccounts.superAdmin') }}
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
                {{ t('adminAccounts.enable') }}
              </el-button>
              <el-button
                v-if="selectedAdmin.status !== 'DISABLED'"
                :disabled="selectedAdmin.id === auth.accountId"
                @click="changeStatus(selectedAdmin, 'DISABLED')"
              >
                {{ t('adminAccounts.disable') }}
              </el-button>
              <el-button :icon="Refresh" :loading="loading" @click="loadAll">{{ t('adminAccounts.refresh') }}</el-button>
            </div>
          </div>

          <div class="summary-grid">
            <div class="summary-item">
              <span>{{ t('adminAccounts.accountId') }}</span>
              <strong>{{ selectedAdmin.id }}</strong>
            </div>
            <div class="summary-item">
              <span>{{ t('adminAccounts.status') }}</span>
              <strong>
                <span class="account-status" :class="selectedAdmin.status.toLowerCase()">
                  <i></i>{{ t(`customers.statuses.${selectedAdmin.status}`) }}
                </span>
              </strong>
            </div>
            <div class="summary-item">
              <span>{{ t('adminAccounts.lastLogin') }}</span>
              <strong>{{ formatDate(selectedAdmin.lastLoginAt) }}</strong>
            </div>
            <div class="summary-item">
              <span>{{ t('adminAccounts.createdAt') }}</span>
              <strong>{{ formatDate(selectedAdmin.createdAt) }}</strong>
            </div>
          </div>

          <el-alert
            v-if="selectedAdmin.roleCode === 'SUPER_ADMIN'"
            class="super-alert"
            type="primary"
            show-icon
            :closable="false"
            :title="t('adminAccounts.superAdminNotice')"
          />

          <div class="permission-card" :class="{ 'is-readonly': selectedAdmin.roleCode === 'SUPER_ADMIN' }">
            <div class="permission-toolbar">
              <div class="permission-heading"><strong>{{ t('adminAccounts.permissions') }}</strong><span>{{ t('adminAccounts.permissionHint') }}</span></div>
              <div class="permission-tools">
                <el-button v-if="selectedAdmin.roleCode !== 'SUPER_ADMIN'" type="primary" :loading="savingPermissions" @click="savePermissions">{{ t('adminAccounts.savePermissions') }}</el-button>
                <el-input
                  v-model.trim="permissionKeyword"
                  class="permission-search"
                  clearable
                  :placeholder="t('adminAccounts.searchPermission')"
                  :prefix-icon="Search"
                />
                <el-button :icon="Expand" @click="expandAll">{{ t('adminAccounts.expandAll') }}</el-button>
                <el-button :icon="Fold" @click="collapseAll">{{ t('adminAccounts.collapseAll') }}</el-button>
              </div>
            </div>

            <el-table
              ref="permissionTableRef"
              class="permission-table"
              :data="permissionRows"
              row-key="id"
              default-expand-all
              @select="handlePermissionSelect"
              @selection-change="handlePermissionSelection"
              :tree-props="{ children: 'children' }"
              :empty-text="t('adminAccounts.noPermissions')"
            >
              <el-table-column type="selection" width="44" />
              <el-table-column :label="t('adminAccounts.permissionName')" min-width="180"><template #default="{ row }">{{ permissionName(row) }}</template></el-table-column>
              <el-table-column :label="t('adminAccounts.permissionCode')" min-width="180">
                <template #default="{ row }">
                  {{ row.permissionCode || row.menuCode || '-' }}
                </template>
              </el-table-column>
              <el-table-column :label="t('adminAccounts.type')" width="110">
                <template #default="{ row }">{{ menuTypeLabel(row.menuType) }}</template>
              </el-table-column>
              <el-table-column :label="t('adminAccounts.description')" min-width="180">
                <template #default="{ row }">{{ rowDescription(row) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>

        <el-empty v-else :description="t('adminAccounts.selectAdmin')" />
      </section>
    </div>

    <el-dialog append-to-body v-model="createDialogVisible" :title="t('adminAccounts.createTitle')" width="460px">
      <el-form label-position="top" class="create-form">
        <el-form-item :label="t('auth.username')" required>
          <el-input v-model.trim="createForm.username" maxlength="64" />
        </el-form-item>
        <el-form-item :label="t('auth.email')" required>
          <el-input v-model.trim="createForm.email" maxlength="128" />
        </el-form-item>
        <el-form-item :label="t('adminAccounts.password')" required>
          <el-input
            v-model="createForm.password"
            type="password"
            maxlength="72"
            show-password
            :placeholder="t('adminAccounts.passwordPlaceholder')"
          />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">{{ t('common.confirm') }}</el-button>
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
  getAdminAccountPermissions,
  getAdminAccounts,
  updateAdminAccountPermissions,
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

const { t, locale } = useI18n()
const auth = useAuthStore()
const loading = ref(false)
const creating = ref(false)
const savingRoles = ref(false)
const savingPermissions = ref(false)
const createDialogVisible = ref(false)
const adminKeyword = ref('')
const permissionKeyword = ref('')
const admins = ref<AdminAccount[]>([])
const roleOptions = ref<AdminRoleConfig[]>([])
const menus = ref<AdminMenu[]>([])
const selectedAdmin = ref<AdminAccount | null>(null)
const selectedRoleIds = ref<number[]>([])
const permissionTableRef = ref<TableInstance>()
const selectedPermissionIds = ref<number[]>([])
const syncingPermissionSelection = ref(false)
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
  return new Set(menus.value.map((menu) => menu.id))
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

watch(selectedAdmin, async (admin) => {
  selectedRoleIds.value = admin?.roleIds ? [...admin.roleIds] : []
  permissionKeyword.value = ''
  selectedPermissionIds.value = admin ? await getAdminAccountPermissions(admin.id) : []
  await nextTick()
  syncPermissionSelection()
})

function handlePermissionSelection(rows: PermissionNode[]) {
  if (syncingPermissionSelection.value) return
  selectedPermissionIds.value = rows.map((row) => row.id)
}

async function handlePermissionSelect(selection: PermissionNode[], row: PermissionNode) {
  if (syncingPermissionSelection.value) return
  syncingPermissionSelection.value = true
  const checked = selection.some((item) => item.id === row.id)
  function toggleChildren(node: PermissionNode) {
    node.children?.forEach((child) => {
      permissionTableRef.value?.toggleRowSelection(child, checked)
      toggleChildren(child)
    })
  }
  toggleChildren(row)
  if (checked) {
    const byId = new Map(menus.value.map((menu) => [menu.id, menu]))
    let parentId = row.parentId
    while (parentId && byId.has(parentId)) {
      const parent = byId.get(parentId)!
      const parentRow = findPermissionRow(permissionRows.value, parent.id)
      if (parentRow) permissionTableRef.value?.toggleRowSelection(parentRow, true)
      parentId = parent.parentId
    }
  }
  await nextTick()
  const selectedRows = (permissionTableRef.value?.getSelectionRows() || []) as PermissionNode[]
  selectedPermissionIds.value = selectedRows.map((item) => item.id)
  syncingPermissionSelection.value = false
}

function findPermissionRow(rows: PermissionNode[], id: number): PermissionNode | undefined {
  for (const row of rows) {
    if (row.id === id) return row
    const child = row.children?.length ? findPermissionRow(row.children, id) : undefined
    if (child) return child
  }
  return undefined
}

async function syncPermissionSelection() {
  const selected = new Set(selectedPermissionIds.value)
  syncingPermissionSelection.value = true
  permissionTableRef.value?.clearSelection()
  function apply(rows: PermissionNode[]) {
    rows.forEach((row) => {
      if (selected.has(row.id)) permissionTableRef.value?.toggleRowSelection(row, true)
      if (row.children?.length) apply(row.children)
    })
  }
  apply(permissionRows.value)
  await nextTick()
  syncingPermissionSelection.value = false
}

async function savePermissions() {
  if (!selectedAdmin.value || selectedAdmin.value.roleCode === 'SUPER_ADMIN') return
  savingPermissions.value = true
  try {
    selectedPermissionIds.value = await updateAdminAccountPermissions(selectedAdmin.value.id, selectedPermissionIds.value)
    ElMessage.success(t('adminAccounts.permissionsSaved'))
  } catch {
    ElMessage.error(t('adminAccounts.permissionsSaveFailed'))
  } finally {
    savingPermissions.value = false
  }
}

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
    ElMessage.error(t('adminAccounts.permissionLoadFailed'))
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
    ElMessage.warning(t('adminAccounts.createRequired'))
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
    ElMessage.success(t('adminAccounts.createSuccess'))
    createDialogVisible.value = false
    await loadAll()
    await nextTick()
    selectedAdmin.value = admins.value.find((admin) => admin.id === created.id) || selectedAdmin.value
  } catch {
    ElMessage.error(t('adminAccounts.createFailed'))
  } finally {
    creating.value = false
  }
}

async function submitRoles() {
  if (!selectedAdmin.value || selectedAdmin.value.roleCode === 'SUPER_ADMIN') return
  if (selectedRoleIds.value.length === 0) {
    ElMessage.warning(t('adminAccounts.roleRequired'))
    return
  }
  savingRoles.value = true
  try {
    const updated = await updateAdminAccountRoles(selectedAdmin.value.id, selectedRoleIds.value)
    ElMessage.success(t('adminAccounts.permissionsSaved'))
    const index = admins.value.findIndex((admin) => admin.id === updated.id)
    if (index >= 0) admins.value[index] = updated
    selectedAdmin.value = updated
  } catch {
    ElMessage.error(t('adminAccounts.permissionsSaveFailed'))
  } finally {
    savingRoles.value = false
  }
}

async function changeStatus(row: AdminAccount, status: AccountStatus) {
  const statusText = t(`customers.statuses.${status}`)
  try {
    await ElMessageBox.confirm(t('adminAccounts.confirmStatusMessage', { name: row.username, status: statusText }), t('adminAccounts.statusTitle'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
    const updated = await updateAdminAccountStatus(row.id, status)
    ElMessage.success(t('adminAccounts.statusUpdated'))
    const index = admins.value.findIndex((admin) => admin.id === updated.id)
    if (index >= 0) admins.value[index] = updated
    selectedAdmin.value = updated
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('adminAccounts.statusUpdateFailed'))
    }
  }
}

async function copyEmail() {
  if (!selectedAdmin.value?.email) return
  try {
    await navigator.clipboard.writeText(selectedAdmin.value.email)
    ElMessage.success(t('adminAccounts.emailCopied'))
  } catch {
    ElMessage.warning(t('adminAccounts.copyFailed'))
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
  const labels = {
    'zh-CN': { CATALOG:'目录', MENU:'菜单', BUTTON:'按钮' },
    'en-US': { CATALOG:'Catalog', MENU:'Menu', BUTTON:'Action' },
    'ru-RU': { CATALOG:'Раздел', MENU:'Меню', BUTTON:'Действие' },
    'pt-PT': { CATALOG:'Secção', MENU:'Menu', BUTTON:'Ação' },
    'es-ES': { CATALOG:'Sección', MENU:'Menú', BUTTON:'Acción' }
  }
  return (labels[locale.value as keyof typeof labels] || labels['en-US'])[type]
}

function rowDescription(row: AdminMenu) {
  const name = permissionName(row)
  if (row.menuType === 'BUTTON') return name
  const suffix = { 'zh-CN':'模块','en-US':' module','ru-RU':' — модуль','pt-PT':' — módulo','es-ES':' — módulo' }[locale.value] || ' module'
  return `${name}${suffix}`
}

function permissionName(row: AdminMenu) {
  if (locale.value === 'zh-CN') return row.nameZh
  const menuKeys: Record<string, string> = {
    dashboard:'menu.home', customers:'menu.customers', promotion:'menu.promotion', applications:'menu.applications',
    orderExecution:'menu.orderExecution', 'orderExecution.pending':'menu.pendingExecutionOrders',
    'orderExecution.executing':'menu.executingOrders', 'orderExecution.completed':'menu.completedOrders',
    orders:'menu.orders', 'orders.pendingReview':'menu.pendingReviewOrders', 'orders.pendingConfirm':'menu.pendingConfirmOrders',
    'orders.apple':'menu.appleOrders', 'orders.google':'menu.googleOrders', 'orders.ipad':'menu.ipadOrders',
    finance:'menu.finance', 'finance.transactions':'menu.financeTransactions', 'finance.recharges':'menu.rechargeRecords',
    system:'menu.systemManagement', 'system.homeMetrics':'menu.homeMetricsConfig', 'system.pricing':'menu.pricing', 'system.walletTypes':'menu.walletTransactionTypeConfig',
    'system.customerService':'menu.customerServiceConfig', 'system.mail':'menu.mailConfig', 'system.regions':'menu.regions',
    'system.adminAccounts':'menu.adminAccounts', 'system.settings':'menu.settings'
  }
  if (row.menuCode && menuKeys[row.menuCode]) return t(menuKeys[row.menuCode])
  const actions: Record<string, Record<string, string>> = {
    'customer:status': { 'ru-RU':'Включение и отключение пользователей','pt-PT':'Ativar ou desativar utilizadores','es-ES':'Activar o desactivar usuarios' },
    'order:create': { 'ru-RU':'Создание заказа для клиента','pt-PT':'Criar encomenda para cliente','es-ES':'Crear pedido para cliente' },
    'app:add': { 'ru-RU':'Добавление приложения','pt-PT':'Adicionar aplicação','es-ES':'Añadir aplicación' },
    'app:delete': { 'ru-RU':'Удаление приложения','pt-PT':'Eliminar aplicação','es-ES':'Eliminar aplicación' },
    'order:confirm': { 'ru-RU':'Подтверждение заказа','pt-PT':'Confirmar encomenda','es-ES':'Confirmar pedido' },
    'order:execute': { 'ru-RU':'Выполнение заказа','pt-PT':'Executar encomenda','es-ES':'Ejecutar pedido' },
    'order:pause': { 'ru-RU':'Приостановка заказа','pt-PT':'Pausar encomenda','es-ES':'Pausar pedido' },
    'order:resume': { 'ru-RU':'Возобновление заказа','pt-PT':'Retomar encomenda','es-ES':'Reanudar pedido' },
    'order:cancel': { 'ru-RU':'Отмена заказа','pt-PT':'Cancelar encomenda','es-ES':'Cancelar pedido' },
    'order:export': { 'ru-RU':'Экспорт заказов','pt-PT':'Exportar encomendas','es-ES':'Exportar pedidos' },
    'wallet:adjust': { 'ru-RU':'Корректировка баланса','pt-PT':'Ajustar saldo','es-ES':'Ajustar saldo' }
  }
  return actions[row.permissionCode || '']?.[locale.value] || row.nameEn || row.nameZh
}

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.admin-permission-page {
  color: #0f172a;
}

.permission-layout {
  min-height: 680px;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
}

.admin-list-panel,
.detail-panel {
  min-width: 0;
  border: 1px solid #e5eaf3;
  border-radius: 12px;
  background: #ffffff;
}

.admin-list-panel {
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 17px 18px 11px;
  border-bottom: 1px solid #edf1f7;
}

.panel-head > div { display: flex; flex-direction: column; gap: 3px; }
.panel-head strong { font-size: 15px; }
.panel-head span { color: #94a3b8; font-size: 11px; }

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
  background: #fbbf24;
  color: #92400e;
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
  color: #64748b;
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
  color: #0f172a;
  font-size: 24px;
  line-height: 1.2;
}

.profile-text p {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 6px 0 0;
  color: #64748b;
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
  color: #64748b;
  font-size: 12px;
}

.summary-item strong {
  color: #0f172a;
  font-size: 14px;
}

.account-status {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 10px;
  border: 1px solid #dbe4ee;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}
.account-status i { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
.account-status.enabled { border-color: #bbf7d0; background: #f0fdf4; color: #15803d; }
.account-status.disabled { border-color: #fecaca; background: #fef2f2; color: #dc2626; }
.super-alert {
  margin: 16px 0;
}

.permission-card {
  margin-top: 18px;
  overflow: hidden;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  background: #ffffff;
}

.permission-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 15px 16px;
  border-bottom: 1px solid #e8edf4;
  background: #fbfcfe;
}

.permission-heading { display: flex; flex-direction: column; gap: 5px; min-width: 220px; }
.permission-heading strong { font-size: 16px; line-height: 1.2; }
.permission-heading span { color: #64748b; font-size: 12px; line-height: 1.4; }

.permission-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-search {
  width: 260px;
}

.permission-card.is-readonly :deep(.el-table__header-wrapper .el-checkbox),
.permission-card.is-readonly :deep(.el-table__body-wrapper .el-checkbox) {
  pointer-events: none;
}
.permission-card.is-readonly :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  opacity: .72;
}
.permission-table {
  border: 0;
  border-radius: 0;
}

:deep(.permission-table .el-table__cell) {
  padding: 8px 0;
}

.role-card {
  margin: 16px 0 0;
  padding: 16px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  background: #f8fafc;
}
.section-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.section-heading > div { display: flex; flex-direction: column; gap: 4px; }
.section-heading strong { font-size: 16px; }
.section-heading span { color: #64748b; font-size: 12px; }
.role-options { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.role-options :deep(.el-checkbox) { width: 100%; height: auto; min-height: 54px; margin: 0; padding: 10px 12px; background: #fff; }
.role-option { display: flex; flex-direction: column; gap: 3px; }
.role-option strong { color: #0f172a; font-size: 13px; }
.role-option small { color: #94a3b8; font-size: 10px; }
.super-role-note { padding: 11px 13px; border-radius: 8px; background: #fff; color: #64748b; font-size: 12px; }
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



