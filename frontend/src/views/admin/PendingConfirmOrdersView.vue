<template>
  <section class="orders-page">
    <div class="query-panel">
      <div class="query-grid">
        <label class="query-item keyword-item">
          <span>{{ t('ordersPage.contentFilter') }}</span>
          <el-input
            v-model.trim="filters.keyword"
            clearable
            :placeholder="t('ordersPage.contentPlaceholder')"
            @keyup.enter="loadOrders"
          />
        </label>
        <label class="query-item">
          <span>{{ t('ordersPage.customer') }}</span>
          <el-select
            v-model="filters.customerId"
            clearable
            filterable
            :loading="customersLoading"
            :placeholder="t('ordersPage.allCustomers')"
            popper-class="order-customer-select-dropdown"
          >
            <el-option :label="t('ordersPage.allCustomers')" value="" />
            <el-option v-for="customer in customerOptions" :key="customer.id" :label="customerLabel(customer)" :value="customer.id">
              <div class="customer-option">
                <strong>{{ customerName(customer) }}</strong>
                <small>{{ customer.email }}</small>
              </div>
            </el-option>
          </el-select>
        </label>
        <label class="query-item">
          <span>{{ t('ordersPage.appFilter') }}</span>
          <el-select
            v-model="filters.customerAppId"
            clearable
            filterable
            :placeholder="t('ordersPage.allApps')"
            popper-class="order-app-select-dropdown"
          >
            <el-option :label="t('ordersPage.allApps')" value="" />
            <el-option v-for="app in appOptions" :key="app.id" :label="app.name" :value="app.id">
              <div class="app-option">
                <span class="app-option-icon">
                  <img v-if="app.iconUrl" :src="app.iconUrl" :alt="app.name" />
                  <span v-else>{{ app.name.slice(0, 1).toUpperCase() }}</span>
                </span>
                <span class="app-option-copy">
                  <strong>{{ app.name }}</strong>
                  <small>{{ app.identifier }}</small>
                </span>
              </div>
            </el-option>
          </el-select>
        </label>
        <label class="query-item">
          <span>{{ t('ordersPage.store') }}</span>
          <el-select v-model="filters.storeType" clearable :placeholder="t('ordersPage.allStores')" popper-class="order-store-select-dropdown">
            <el-option :label="t('ordersPage.allStores')" value="">
              <div class="store-option">
                <span class="store-option-icon all-store-icon">ALL</span>
                <span>{{ t('ordersPage.allStores') }}</span>
              </div>
            </el-option>
            <el-option v-for="store in storeOptions" :key="store" :label="storeLabel(store)" :value="store">
              <div class="store-option">
                <span class="store-option-icon">
                  <StoreIcon :store-type="store" size="sm" />
                </span>
                <span>{{ storeLabel(store) }}</span>
              </div>
            </el-option>
          </el-select>
        </label>
        <label class="query-item">
          <span>{{ t('ordersPage.taskType') }}</span>
          <el-select v-model="filters.orderType" clearable :placeholder="t('ordersPage.allTaskTypes')">
            <el-option :label="t('ordersPage.allTaskTypes')" value="" />
            <el-option v-for="type in orderTypeOptions" :key="type" :label="typeLabel(type)" :value="type" />
          </el-select>
        </label>
        <label class="query-item">
          <span>{{ t('ordersPage.orderCategory') }}</span>
          <el-select v-model="filters.specialOrder" clearable :placeholder="t('ordersPage.allOrderCategories')">
            <el-option :label="t('ordersPage.allOrderCategories')" value="" />
            <el-option :label="t('ordersPage.categories.REGULAR')" :value="false" />
            <el-option :label="t('ordersPage.categories.SPECIAL')" :value="true" />
          </el-select>
        </label>
        <label class="query-item date-item">
          <span>{{ t('ordersPage.orderTime') }}</span>
          <el-date-picker
            v-model="orderDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="-"
            :start-placeholder="t('ordersPage.startDate')"
            :end-placeholder="t('ordersPage.endDate')"
          />
        </label>
        <label class="query-item date-item">
          <span>{{ t('ordersPage.createdTime') }}</span>
          <el-date-picker
            v-model="createdDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="-"
            :start-placeholder="t('ordersPage.startDate')"
            :end-placeholder="t('ordersPage.endDate')"
          />
        </label>
      </div>
      <div class="query-actions">
        <el-button type="primary" :icon="Check" :disabled="selectedOrders.length === 0" @click="batchConfirm">
          {{ t('ordersPage.batchConfirm') }}
        </el-button>
        <el-button type="primary" :icon="Search" @click="searchOrders">{{ t('ordersPage.search') }}</el-button>
        <el-button :icon="Delete" @click="resetFilters">{{ t('ordersPage.clear') }}</el-button>
        <el-button :icon="Refresh" @click="loadOrders">{{ t('ordersPage.refresh') }}</el-button>
        <el-button :icon="Download" @click="exportOrders">{{ t('ordersPage.export') }}</el-button>
        <TableColumnSettings
          v-model="visibleColumns"
          :options="columnOptions"
          :defaults="defaultColumnKeys"
          :title="t('ordersPage.columnSettings')"
          :select-all-text="t('ordersPage.selectAllColumns')"
          :restore-defaults-text="t('ordersPage.restoreDefaultColumns')"
        />
      </div>
    </div>

    <div class="selection-note">{{ t('ordersPage.selectedCount', { count: selectedOrders.length }) }}</div>

    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="orders"
        class="orders-table"
        :empty-text="t('ordersPage.empty')"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column v-if="isColumnVisible('orderNo')" prop="orderNo" :label="t('ordersPage.orderNo')" min-width="190" />
        <el-table-column v-if="isColumnVisible('app')" :label="t('ordersPage.app')" min-width="260">
          <template #default="{ row }">
            <div class="app-cell">
              <div class="app-icon">
                <img v-if="row.appIconUrl" :src="row.appIconUrl" :alt="row.appName" />
                <span v-else>{{ row.appName.slice(0, 1).toUpperCase() }}</span>
              </div>
              <div class="app-info">
                <div class="app-name">{{ row.appName }}</div>
                <div class="app-badges">
                  <span class="store-badge">{{ storeLabel(row.storeType) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('appIdentifier')" prop="appIdentifier" :label="t('ordersPage.appIdentifier')" min-width="190" />
        <el-table-column v-if="isColumnVisible('store')" :label="t('ordersPage.store')" min-width="120">
          <template #default="{ row }">{{ storeLabel(row.storeType) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('region')" prop="regionCode" :label="t('ordersPage.region')" min-width="100" />
        <el-table-column v-if="isColumnVisible('customer')" :label="t('ordersPage.customer')" min-width="180">
          <template #default="{ row }">
            <div class="customer-cell">
              <strong>{{ orderCustomerName(row) }}</strong>
              <span>{{ orderCustomerEmail(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('type')" :label="t('ordersPage.type')" min-width="150">
          <template #default="{ row }">{{ orderTypeLabel(row) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('orderCategory')" :label="t('ordersPage.orderCategory')" min-width="130">
          <template #default="{ row }">{{ row.sourceAuditId ? t('ordersPage.categories.SPECIAL') : t('ordersPage.categories.REGULAR') }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('orderTime')" :label="t('ordersPage.orderTime')" min-width="190">
          <template #default="{ row }">{{ formatOrderSchedule(row) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('totalDays')" :label="t('orderDetail.totalDays')" min-width="90" align="right">
          <template #default="{ row }">{{ valueOrDash(row.totalDays) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('executionHours')" :label="t('orderDetail.executionHours')" min-width="120" align="right">
          <template #default="{ row }">{{ valueOrDash(row.executionHours) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('quantity')" :label="t('ordersPage.quantity')" min-width="100" align="right">
          <template #default="{ row }">{{ valueOrDash(row.quantity) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('unitPrice')" :label="t('ordersPage.unitPrice')" min-width="120" align="right">
          <template #default="{ row }">{{ money(row.unitPrice, 4) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('amount')" :label="t('ordersPage.amount')" width="120" align="right">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('refundAmount')" :label="t('ordersPage.refundAmount')" min-width="120" align="right">
          <template #default="{ row }">{{ money(row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" :label="t('ordersPage.status')" min-width="130" align="center">
          <template #default="{ row }">
            <div class="order-status-tags">
              <el-tag class="order-status-tag" :type="statusTone(row.status)" effect="light">{{ t(`ordersPage.statuses.${row.status}`) }}</el-tag>
              <ReservedOrderTag :order="row" />
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('expectedCompletedAt')" :label="t('ordersPage.expectedCompletedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.expectedCompletedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('confirmedAt')" :label="t('orderDetail.confirmedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.confirmedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('executedAt')" :label="t('orderDetail.executedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.executedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('completedAt')" :label="t('orderDetail.completedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.completedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createdAt')" :label="t('ordersPage.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('actions')" :label="t('ordersPage.actions')" :width="locale === 'zh-CN' ? 240 : 360" fixed="right" align="center">
          <template #default="{ row }">
            <div class="order-row-actions pending-row-actions">
              <el-button class="action-detail" size="small" text type="primary" :icon="View" @click="viewDetail(row)">
                {{ t('ordersPage.actionDetail') }}
              </el-button>
              <el-button size="small" type="primary" :icon="Check" @click="confirmOrder(row)">
                {{ t('ordersPage.actionConfirm') }}
              </el-button>
              <el-dropdown trigger="click">
                <el-button size="small" :icon="MoreFilled" :aria-label="t('ordersPage.actionMore')">{{ t('ordersPage.actionMore') }}</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="Edit" v-if="['PENDING_CONFIRM', 'PENDING_EXECUTION'].includes(row.status) && editAuth.hasPermission('order:create')" @click="router.push({ name: 'admin-order-edit', query: { orderId: String(row.id) } })">{{ t('ordersPage.editOrder') }}</el-dropdown-item>
                    <el-dropdown-item :icon="Delete" class="danger-menu-item" :divided="['PENDING_CONFIRM', 'PENDING_EXECUTION'].includes(row.status) && editAuth.hasPermission('order:create')" @click="cancelOrder(row)">{{ t('ordersPage.actionCancel') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadOrders"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { orderModuleLabel } from '@/utils/orderModuleLabel'
import { formatCurrency, statusTone } from '@/utils/presentation'
import { useAuthStore } from '@/stores/auth'
import ReservedOrderTag from '@/components/ReservedOrderTag.vue'
import { formatOrderSchedule } from '@/utils/orderTime'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled, Check, Delete, Download, Edit, Refresh, Search, View } from '@element-plus/icons-vue'
import { batchConfirmAdminOrders, cancelAdminOrder, confirmAdminOrder, getAdminOrdersPage, type Order, type OrderType } from '@/api/orders'
import { getAdminApps, type CustomerApp, type StoreType } from '@/api/applications'
import { getAdminCustomers, type CustomerAccount } from '@/api/customers'
import StoreIcon from '@/components/StoreIcon.vue'
import TableColumnSettings, { type TableColumnOption } from '@/components/TableColumnSettings.vue'
import { usePersistentTableColumns } from '@/composables/usePersistentTableColumns'
import { exportOrdersCsv } from '@/utils/orderExport'

const router = useRouter()
const editAuth = useAuthStore()
const { t, locale } = useI18n()

const defaultColumnKeys = ['app', 'customer', 'type', 'amount', 'status', 'createdAt', 'actions']
const allColumnKeys = [
  'orderNo', 'app', 'appIdentifier', 'store', 'region', 'customer', 'type', 'orderCategory', 'orderTime', 'totalDays',
  'executionHours', 'quantity', 'unitPrice', 'amount', 'refundAmount', 'status', 'expectedCompletedAt', 'confirmedAt',
  'executedAt', 'completedAt', 'createdAt', 'actions'
]
const { visibleColumns, isColumnVisible } = usePersistentTableColumns(
  'youou.admin.pending-confirm-order-list.columns',
  allColumnKeys,
  defaultColumnKeys
)
const columnOptions = computed<TableColumnOption[]>(() => [
  { key: 'orderNo', label: t('ordersPage.orderNo') },
  { key: 'app', label: t('ordersPage.app') },
  { key: 'appIdentifier', label: t('ordersPage.appIdentifier') },
  { key: 'store', label: t('ordersPage.store') },
  { key: 'region', label: t('ordersPage.region') },
  { key: 'customer', label: t('ordersPage.customer') },
  { key: 'type', label: t('ordersPage.type') },
  { key: 'orderCategory', label: t('ordersPage.orderCategory') },
  { key: 'orderTime', label: t('ordersPage.orderTime') },
  { key: 'totalDays', label: t('orderDetail.totalDays') },
  { key: 'executionHours', label: t('orderDetail.executionHours') },
  { key: 'quantity', label: t('ordersPage.quantity') },
  { key: 'unitPrice', label: t('ordersPage.unitPrice') },
  { key: 'amount', label: t('ordersPage.amount') },
  { key: 'refundAmount', label: t('ordersPage.refundAmount') },
  { key: 'status', label: t('ordersPage.status') },
  { key: 'expectedCompletedAt', label: t('ordersPage.expectedCompletedAt') },
  { key: 'confirmedAt', label: t('orderDetail.confirmedAt') },
  { key: 'executedAt', label: t('orderDetail.executedAt') },
  { key: 'completedAt', label: t('orderDetail.completedAt') },
  { key: 'createdAt', label: t('ordersPage.createdAt') },
  { key: 'actions', label: t('ordersPage.actions') }
])

const orders = ref<Order[]>([])
const selectedOrders = ref<Order[]>([])
const appOptionList = ref<CustomerApp[]>([])
const customerList = ref<CustomerAccount[]>([])
const loading = ref(false)
const customersLoading = ref(false)
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const orderDateRange = ref<[string, string] | ''>('')
const createdDateRange = ref<[string, string] | ''>('')
const filters = reactive<{
  keyword: string
  customerId: number | ''
  customerAppId: number | ''
  storeType: StoreType | ''
  orderType: OrderType | ''
  specialOrder: boolean | ''
}>({
  keyword: '',
  customerId: '',
  customerAppId: '',
  storeType: '',
  orderType: '',
  specialOrder: ''
})

const storeOptions: StoreType[] = ['APP_STORE', 'GOOGLE_PLAY', 'IPAD_STORE']
const orderTypeOptions: OrderType[] = [
  'KEYWORD_INSTALL',
  'DOWNLOAD',
  'RATING',
  'REVIEW',
  'RANK_GUARANTEE',
  'CHART_RANK_GUARANTEE',
  'KEYWORD_COVERAGE'
]

const appOptions = computed(() => {
  return appOptionList.value
    .filter((app) => !filters.storeType || app.storeType === filters.storeType)
    .map((app) => ({
      id: app.id,
      name: app.appName,
      identifier: app.bundleId || app.externalAppId || app.appIdentifier,
      iconUrl: app.appIconUrl
    }))
})
const customerOptions = computed(() => {
  return customerList.value.slice().sort((a, b) => customerLabel(a).localeCompare(customerLabel(b)))
})
const customerMap = computed(() => new Map(customerList.value.map((customer) => [customer.id, customer])))

onMounted(() => {
  loadCustomerOptions()
  loadAppOptions()
  loadOrders()
})

watch(
  () => filters.storeType,
  () => {
    ensureSelectedAppMatchesStore()
  }
)

async function loadOrders() {
  loading.value = true
  try {
    const result = await getAdminOrdersPage({
      status: 'PENDING_CONFIRM',
      keyword: filters.keyword,
      customerId: filters.customerId === '' ? null : filters.customerId,
      customerAppId: filters.customerAppId === '' ? null : filters.customerAppId,
      storeType: filters.storeType,
      orderType: filters.orderType,
      specialOrder: filters.specialOrder === '' ? null : filters.specialOrder,
      orderDateFrom: orderDateRange.value ? orderDateRange.value[0] : '',
      orderDateTo: orderDateRange.value ? orderDateRange.value[1] : '',
      createdDateFrom: createdDateRange.value ? createdDateRange.value[0] : '',
      createdDateTo: createdDateRange.value ? createdDateRange.value[1] : '',
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    orders.value = result.items
    pagination.total = result.total
    selectedOrders.value = []
  } catch {
    ElMessage.error(t('ordersPage.loadFailed'))
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.customerId = ''
  filters.customerAppId = ''
  filters.storeType = ''
  filters.orderType = ''
  filters.specialOrder = ''
  orderDateRange.value = ''
  createdDateRange.value = ''
  pagination.page = 1
  loadOrders()
}

function searchOrders() {
  pagination.page = 1
  loadOrders()
}

function handlePageSizeChange() {
  pagination.page = 1
  loadOrders()
}

function viewDetail(order: Order) {
  router.push({ name: 'admin-order-detail', params: { id: order.id } })
}

async function loadAppOptions() {
  try {
    appOptionList.value = await getAdminApps()
    ensureSelectedAppMatchesStore()
  } catch {
    ElMessage.error(t('ordersPage.appOptionsLoadFailed'))
  }
}

async function loadCustomerOptions() {
  customersLoading.value = true
  try {
    customerList.value = await getAdminCustomers()
  } catch {
    ElMessage.error(t('customers.loadFailed'))
  } finally {
    customersLoading.value = false
  }
}

function customerLabel(customer: CustomerAccount) {
  const name = customerName(customer)
  return customer.email ? `${name} / ${customer.email}` : name
}

function customerName(customer: CustomerAccount) {
  return customer.username || t('ordersPage.customerIdShort', { id: customer.id })
}

function orderCustomerName(order: Order) {
  const customer = customerMap.value.get(order.customerId)
  return order.customerUsername || customer?.username || t('ordersPage.customerIdShort', { id: order.customerId })
}

function orderCustomerEmail(order: Order) {
  const customer = customerMap.value.get(order.customerId)
  return order.customerEmail || customer?.email || '-'
}

function ensureSelectedAppMatchesStore() {
  if (filters.customerAppId === '') {
    return
  }
  const selectedApp = appOptionList.value.find((app) => app.id === filters.customerAppId)
  if (selectedApp && filters.storeType && selectedApp.storeType !== filters.storeType) {
    filters.customerAppId = ''
  }
}

async function confirmOrder(order: Order) {
  try {
    await confirmAdminOrder(order.id)
    ElMessage.success(t('ordersPage.confirmSuccess'))
    await loadOrders()
  } catch {
    ElMessage.error(t('ordersPage.operationFailed'))
  }
}

async function batchConfirm() {
  const pendingOrders = selectedOrders.value.filter((order) => order.status === 'PENDING_CONFIRM')
  if (pendingOrders.length === 0) {
    ElMessage.warning(t('ordersPage.noConfirmableSelected'))
    return
  }
  try {
    await ElMessageBox.confirm(t('ordersPage.batchConfirmConfirm', { count: pendingOrders.length }), t('ordersPage.batchConfirm'), {
      type: 'warning',
      confirmButtonText: t('ordersPage.confirm'),
      cancelButtonText: t('ordersPage.keepOrder')
    })
    await batchConfirmAdminOrders(pendingOrders.map((order) => order.id))
    ElMessage.success(t('ordersPage.batchConfirmSuccess'))
    selectedOrders.value = []
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

async function cancelOrder(order: Order) {
  try {
    const { value } = await ElMessageBox.prompt(t('ordersPage.cancelPrompt'), t('ordersPage.cancelOrderTitle'), {
      confirmButtonText: t('ordersPage.confirmCancel'),
      cancelButtonText: t('ordersPage.keepOrder'),
      inputType: 'textarea',
      inputPlaceholder: t('ordersPage.cancelReasonPlaceholder')
    })
    await cancelAdminOrder(order.id, value)
    ElMessage.success(t('ordersPage.cancelSuccess'))
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

function handleSelectionChange(selection: Order[]) {
  selectedOrders.value = selection
}

function exportOrders() {
  if (orders.value.length === 0) {
    ElMessage.warning(t('ordersPage.exportEmpty'))
    return
  }
  exportOrdersCsv(orders.value.map(withExportCustomer), { typeLabel, storeLabel, statusLabel }, 'pending-order')
  ElMessage.success(t('ordersPage.exported'))
}

function withExportCustomer(row: Order): Order {
  const email = orderCustomerEmail(row)
  return {
    ...row,
    customerUsername: orderCustomerName(row),
    customerEmail: email === '-' ? null : email
  }
}

function typeLabel(type?: string | null) {
  return type ? t(`ordersPage.types.${type}`) : '-'
}

function orderTypeLabel(order: Order) {
  return orderModuleLabel(order)
}

function statusLabel(status?: string | null) {
  return status ? t(`ordersPage.statuses.${status}`) : '-'
}

function itemDetails(row: Order) {
  if (!row.items || row.items.length === 0) return ''
  return row.items
    .map((item) => {
      const name = item.itemName ? `${item.itemName}: ` : ''
      const quantity = item.quantity ?? 0
      const amount = item.amount === null || item.amount === undefined ? '' : ` = ${item.amount}`
      return `${name}${itemTypeLabel(item.itemType)} x ${quantity}${amount}`
    })
    .join('; ')
}

function itemTypeLabel(type?: string | null) {
  return type ? t(`ordersPage.itemTypes.${type}`) : '-'
}

function storeLabel(storeType?: StoreType | null) {
  if (storeType === 'GOOGLE_PLAY') return 'Google Play'
  if (storeType === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
}

function storeCode(store?: StoreType | null) {
  if (store === 'GOOGLE_PLAY') return 'GP'
  if (store === 'IPAD_STORE') return 'iPad'
  if (store === 'APP_STORE') return 'APP'
  return 'ALL'
}

function formatDateTime(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

function valueOrDash(value?: number | string | null) {
  return value === null || value === undefined || value === '' ? '-' : value
}

const money = formatCurrency
</script>

<style scoped>
.orders-page {
  color: #0f172a;
}

.query-panel {
  margin-bottom: 16px;
  padding: 16px 16px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  align-items: center;
  gap: 14px 20px;
}

.query-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  min-width: 0;
  max-width: 100%;
}

.query-item > span {
  flex: 0 0 auto;
  min-width: 0;
  color: #101828;
  font-size: 14px;
  white-space: nowrap;
}

.query-item :deep(.el-input),
.query-item :deep(.el-select) {
  width: 100%;
  min-width: 0;
}

.date-item {
  width: 420px;
  min-width: 0;
  max-width: 100%;
}

.date-item :deep(.el-date-editor) {
  width: 100%;
  max-width: 100%;
}

.query-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  width: 100%;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #eef2f7;
}

.query-actions :deep(.el-button) {
  margin-left: 0;
}

.table-card {
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.selection-note {
  margin: 0 0 10px;
  color: #64748b;
  font-size: 13px;
}

.orders-table {
  width: 100%;
}

.orders-table :deep(.el-table__cell) {
  padding: 12px 0;
}

.orders-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.orders-table :deep(.el-table__header th) {
  background: #f6f7f9;
  color: #64748b;
  font-weight: 500;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

.app-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.app-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 700;
}

.app-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-name {
  overflow: hidden;
  font-weight: 600;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-info {
  min-width: 0;
}

.app-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.store-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: #f2f4f7;
  color: #475569;
  font-size: 12px;
  line-height: 22px;
}

.store-badge {
  background: #eff6ff;
  color: #1d4ed8;
}

.muted {
  color: #64748b;
  font-size: 12px;
}

.customer-cell,
.customer-option {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
  line-height: 1.35;
}

.customer-option {
  justify-content: center;
  gap: 1px;
  padding: 2px 0;
}

.customer-cell strong,
.customer-option strong {
  overflow: hidden;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-cell span,
.customer-option small {
  overflow: hidden;
  color: #98a2b3;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-option strong {
  line-height: 18px;
}

.customer-option small {
  line-height: 16px;
}













.app-option {
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 0;
  line-height: 1.35;
}

.app-option-icon {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 7px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 800;
}

.app-option-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-option-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.app-option-copy strong {
  overflow: hidden;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-option-copy small {
  overflow: hidden;
  color: #98a2b3;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-option,
.store-option,
.region-option {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.store-option-icon,
.region-option-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 7px;
  font-size: 11px;
  font-weight: 800;
}

.region-option-icon img {
  width: 24px;
  height: 18px;
  border-radius: 3px;
  object-fit: cover;
}

.region-option-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 1px;
}

.region-option-copy strong {
  overflow: hidden;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.region-option-copy small {
  color: #98a2b3;
  font-size: 11px;
  line-height: 1.2;
}

.all-store-icon,
.all-region-icon {
  background: #fffbeb;
  color: #b45309;
}

.region-option-icon {
  background: #f2f4f7;
  color: #475569;
}

:global(.order-app-select-dropdown .el-select-dropdown__list),
:global(.order-customer-select-dropdown .el-select-dropdown__list),
:global(.order-store-select-dropdown .el-select-dropdown__list),
:global(.order-region-select-dropdown .el-select-dropdown__list) {
  padding: 6px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item),
:global(.order-customer-select-dropdown .el-select-dropdown__item),
:global(.order-store-select-dropdown .el-select-dropdown__item),
:global(.order-region-select-dropdown .el-select-dropdown__item) {
  display: flex;
  height: auto;
  align-items: center;
  border-radius: 6px;
  line-height: 1.35;
}

:global(.order-customer-select-dropdown) {
  min-width: 300px;
}

:global(.order-customer-select-dropdown .el-select-dropdown__wrap) {
  max-height: 320px;
}

:global(.order-customer-select-dropdown .el-select-dropdown__item) {
  min-height: 48px;
  padding: 7px 10px;
}

:global(.order-customer-select-dropdown .el-select-dropdown__item:first-child) {
  min-height: 34px;
  padding: 6px 10px;
  line-height: 20px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item) {
  min-height: 50px;
  padding: 8px 10px;
}

:global(.order-store-select-dropdown .el-select-dropdown__item),
:global(.order-region-select-dropdown .el-select-dropdown__item) {
  min-height: 40px;
  padding: 6px 10px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item.hover),
:global(.order-app-select-dropdown .el-select-dropdown__item:hover),
:global(.order-customer-select-dropdown .el-select-dropdown__item.hover),
:global(.order-customer-select-dropdown .el-select-dropdown__item:hover),
:global(.order-store-select-dropdown .el-select-dropdown__item.hover),
:global(.order-store-select-dropdown .el-select-dropdown__item:hover),
:global(.order-region-select-dropdown .el-select-dropdown__item.hover),
:global(.order-region-select-dropdown .el-select-dropdown__item:hover) {
  background: #f3f7ff;
}

@media (max-width: 1280px) {
  .query-grid {
    gap: 12px;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }
}

@media (max-width: 980px) {
  .query-grid {
    grid-template-columns: 1fr;
  }

  .query-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .date-item {
    width: 100%;
  }
}

@media (max-width: 560px) {
  .query-item {
    align-items: flex-start;
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .query-actions :deep(.el-button) {
    flex: 1 1 calc(50% - 4px);
    margin-left: 0;
  }
}
</style>
