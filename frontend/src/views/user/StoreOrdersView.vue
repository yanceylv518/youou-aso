<template>
  <section class="orders-page">
    <div class="query-panel" :class="{ 'filters-expanded': filtersExpanded }">
      <el-button class="mobile-filter-toggle" :aria-expanded="filtersExpanded" @click="filtersExpanded = !filtersExpanded">{{ t(filtersExpanded ? 'visual.collapse' : 'visual.filters') }} · {{ t('visual.filterCount', { count: activeFilterCount }) }}</el-button>
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
        <label class="query-item status-item">
          <span>{{ t('ordersPage.status') }}</span>
          <el-select v-model="filters.status" clearable :placeholder="t('ordersPage.allStatuses')">
            <el-option :label="t('ordersPage.allStatuses')" value="" />
            <el-option v-for="status in statusOptions" :key="status" :label="statusLabel(status)" :value="status" />
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
        <el-button type="primary" :icon="Plus" @click="createOrder">{{ t('ordersPage.newOrder') }}</el-button>
        <el-button type="primary" :icon="Search" @click="searchOrders">{{ t('ordersPage.search') }}</el-button>
        <el-button :icon="Delete" @click="resetFilters">{{ t('ordersPage.clear') }}</el-button>
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

    <div class="table-card">
      <el-table v-loading="loading" :data="orders" class="orders-table" :empty-text="t('ordersPage.empty')">
        <el-table-column v-if="isMobile" :label="t('ordersPage.orderNo')" min-width="180">
          <template #default="{row}"><div class="mobile-order-summary"><strong>{{row.appName}}</strong><span>{{row.orderNo}}</span><span>{{orderDateText(row)}}</span><el-tag class="order-status-tag" :type="statusTagType(row.status)">{{statusLabel(row.status)}}</el-tag><ReservedOrderTag :order="row" /></div></template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('orderNo')" prop="orderNo" :label="t('ordersPage.orderNo')" min-width="150">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('app')" :label="t('ordersPage.app')" min-width="210">
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
        <el-table-column v-if="!isMobile && isColumnVisible('appIdentifier')" prop="appIdentifier" :label="t('ordersPage.appIdentifier')" min-width="190" />
        <el-table-column v-if="!isMobile && isColumnVisible('store')" :label="t('ordersPage.store')" min-width="120">
          <template #default="{ row }">{{ storeLabel(row.storeType) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('region')" prop="regionCode" :label="t('ordersPage.region')" min-width="100" />
        <el-table-column v-if="!isMobile && isColumnVisible('orderTime')" :label="t('ordersPage.orderTime')" min-width="170">
          <template #default="{ row }">
            <span class="date-range-text">{{ orderDateText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('totalDays')" :label="t('orderDetail.totalDays')" min-width="90" align="right">
          <template #default="{ row }">{{ valueOrDash(row.totalDays) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('executionHours')" :label="t('orderDetail.executionHours')" min-width="120" align="right">
          <template #default="{ row }">{{ valueOrDash(row.executionHours) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('quantity')" :label="t('ordersPage.quantity')" min-width="100" align="right">
          <template #default="{ row }">{{ valueOrDash(row.quantity) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('unitPrice')" :label="t('ordersPage.unitPrice')" min-width="120" align="right">
          <template #default="{ row }">{{ formatCurrency(row.unitPrice, 4) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('amount')" :label="t('ordersPage.amount')" min-width="120" align="right">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('refundAmount')" :label="t('ordersPage.refundAmount')" min-width="120" align="right">
          <template #default="{ row }">{{ money(row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('taskType')" :label="t('ordersPage.taskType')" min-width="120">
          <template #default="{ row }">{{ orderTypeLabel(row) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('orderCategory')" :label="t('ordersPage.orderCategory')" min-width="110">
          <template #default="{ row }">{{ isAuditRow(row) || row.sourceAuditId ? t('ordersPage.categories.SPECIAL') : t('ordersPage.categories.REGULAR') }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('status')" :label="t('ordersPage.status')" min-width="120" align="center">
          <template #default="{ row }">
            <div class="order-status-tags">
              <el-tag class="order-status-tag" :type="statusTagType(row.status)" effect="light">{{ statusLabel(row.status) }}</el-tag>
              <ReservedOrderTag :order="row" />
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('expectedCompletedAt')" :label="t('ordersPage.expectedCompletedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.expectedCompletedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('confirmedAt')" :label="t('orderDetail.confirmedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.confirmedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('executedAt')" :label="t('orderDetail.executedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.executedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('completedAt')" :label="t('orderDetail.completedAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.completedAt) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile && isColumnVisible('createdAt')" :label="t('ordersPage.createdAt')" min-width="150">
          <template #default="{ row }">
            <span class="datetime-text">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isMobile || isColumnVisible('actions')" :label="t('ordersPage.actions')" :width="isMobile ? 130 : 190" :fixed="isMobile ? false : 'right'" align="center">
          <template #default="{ row }">
            <div class="order-row-actions">
              <el-button class="action-detail" size="small" text type="primary" :icon="View" @click="viewDetail(row)">
                {{ t('ordersPage.actionDetail') }}
              </el-button>
              <el-button v-if="canSubmitApprovedAudit(row)" size="small" text type="primary" :icon="CreditCard" @click="submitApprovedAudit(row)">
                {{ t('ordersPage.actionPaySubmit') }}
              </el-button>
              <el-button v-if="canEditOrder(row)" size="small" text type="primary" :icon="Edit" @click="editOrder(row)">
                {{ row.status === 'PENDING_PAYMENT' ? t('ordersPage.actionPayEdit') : t('ordersPage.editOrder') }}
              </el-button>
              <el-button v-if="canRenewOrder(row)" size="small" text type="primary" :icon="RefreshRight" @click="renewOrder(row)">
                {{ t('ordersPage.actionRenew') }}
              </el-button>
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
import { useCompactLayout } from '@/composables/useCompactLayout'
const isMobile = useCompactLayout()
const filtersExpanded = ref(false)
const activeFilterCount = computed(() => Object.entries(filters).filter(([key,v]) => key !== 'storeType' && v !== '' && v !== null && v !== undefined).length + (orderDateRange.value?.length ? 1 : 0) + (createdDateRange.value?.length ? 1 : 0))
import { statusTone, formatCurrency } from '@/utils/presentation'
import ReservedOrderTag from '@/components/ReservedOrderTag.vue'
import { formatOrderSchedule } from '@/utils/orderTime'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { CreditCard, Delete, Download, Edit, Plus, RefreshRight, Search, View } from '@element-plus/icons-vue'
import { getCustomerOrdersPage, type Order, type OrderListStatus, type OrderStatus, type OrderType } from '@/api/orders'
import { getCustomerApps, type CustomerApp, type StoreType } from '@/api/applications'
import TableColumnSettings, { type TableColumnOption } from '@/components/TableColumnSettings.vue'
import { usePersistentTableColumns } from '@/composables/usePersistentTableColumns'
import {
  getCustomerSpecialAudits,
  submitApprovedSpecialAudit,
  type SpecialAuditStatus,
  type SpecialOrderAudit
} from '@/api/specialOrderAudits'
import { exportOrdersCsv } from '@/utils/orderExport'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const defaultColumnKeys = ['orderNo', 'app', 'orderTime', 'taskType', 'orderCategory', 'status', 'createdAt', 'actions']
const allColumnKeys = [
  'orderNo', 'app', 'appIdentifier', 'store', 'region', 'orderTime', 'totalDays', 'executionHours', 'quantity',
  'unitPrice', 'amount', 'refundAmount', 'taskType', 'orderCategory', 'status', 'expectedCompletedAt', 'confirmedAt',
  'executedAt', 'completedAt', 'createdAt', 'actions'
]
const { visibleColumns, isColumnVisible } = usePersistentTableColumns(
  'youou.user.order-list.columns',
  allColumnKeys,
  defaultColumnKeys
)
const columnOptions = computed<TableColumnOption[]>(() => [
  { key: 'orderNo', label: t('ordersPage.orderNo') },
  { key: 'app', label: t('ordersPage.app') },
  { key: 'appIdentifier', label: t('ordersPage.appIdentifier') },
  { key: 'store', label: t('ordersPage.store') },
  { key: 'region', label: t('ordersPage.region') },
  { key: 'orderTime', label: t('ordersPage.orderTime') },
  { key: 'totalDays', label: t('orderDetail.totalDays') },
  { key: 'executionHours', label: t('orderDetail.executionHours') },
  { key: 'quantity', label: t('ordersPage.quantity') },
  { key: 'unitPrice', label: t('ordersPage.unitPrice') },
  { key: 'amount', label: t('ordersPage.amount') },
  { key: 'refundAmount', label: t('ordersPage.refundAmount') },
  { key: 'taskType', label: t('ordersPage.taskType') },
  { key: 'orderCategory', label: t('ordersPage.orderCategory') },
  { key: 'status', label: t('ordersPage.status') },
  { key: 'expectedCompletedAt', label: t('ordersPage.expectedCompletedAt') },
  { key: 'confirmedAt', label: t('orderDetail.confirmedAt') },
  { key: 'executedAt', label: t('orderDetail.executedAt') },
  { key: 'completedAt', label: t('orderDetail.completedAt') },
  { key: 'createdAt', label: t('ordersPage.createdAt') },
  { key: 'actions', label: t('ordersPage.actions') }
])
const auth = useAuthStore()

type OrderRow = Omit<Order, 'status'> & {
  status: OrderListStatus
  rowKind: 'ORDER' | 'AUDIT'
  audit?: SpecialOrderAudit
}

const orders = ref<OrderRow[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const appOptionList = ref<CustomerApp[]>([])
const loading = ref(false)
const orderDateRange = ref<[string, string] | ''>('')
const createdDateRange = ref<[string, string] | ''>('')
const filters = reactive<{
  keyword: string
  customerAppId: number | ''
  storeType: StoreType | ''
  orderType: OrderType | ''
  specialOrder: boolean | ''
  status: OrderListStatus | ''
}>({
  keyword: '',
  customerAppId: '',
  storeType: '',
  orderType: '',
  specialOrder: '',
  status: ''
})

const statusOptions: OrderListStatus[] = [
  'PENDING_REVIEW',
  'APPROVED_WAIT_SUBMIT',
  'PENDING_PAYMENT',
  'PENDING_CONFIRM',
  'PENDING_EXECUTION',
  'EXECUTING',
  'PAUSED',
  'COMPLETED',
  'CANCELLED',
  'SUBMITTED'
]
const orderTypeOptions: OrderType[] = [
  'KEYWORD_INSTALL',
  'DOWNLOAD',
  'RATING',
  'REVIEW',
  'RANK_GUARANTEE',
  'CHART_RANK_GUARANTEE',
  'KEYWORD_COVERAGE'
]

const storeType = computed(() => route.meta.storeType as StoreType)
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
onMounted(() => {
  resetRouteStoreFilter()
  loadAppOptions()
  loadOrders()
})

watch(
  () => route.fullPath,
  () => {
    resetRouteStoreFilter()
    ensureSelectedAppMatchesStore()
    pagination.page = 1
    loadOrders()
  }
)

watch(
  () => filters.storeType,
  () => {
    ensureSelectedAppMatchesStore()
  }
)

async function loadOrders() {
  loading.value = true
  try {
    const orderStatus = isOrderStatus(filters.status) ? filters.status : ''
    const [result, auditRows] = await Promise.all([
      getCustomerOrdersPage({
      storeType: filters.storeType,
      status: orderStatus,
      keyword: filters.keyword,
      customerAppId: filters.customerAppId === '' ? null : filters.customerAppId,
      orderType: filters.orderType,
      specialOrder: filters.specialOrder === '' ? null : filters.specialOrder,
      orderDateFrom: orderDateRange.value ? orderDateRange.value[0] : '',
      orderDateTo: orderDateRange.value ? orderDateRange.value[1] : '',
      createdDateFrom: createdDateRange.value ? createdDateRange.value[0] : '',
      createdDateTo: createdDateRange.value ? createdDateRange.value[1] : '',
      page: pagination.page,
      pageSize: pagination.pageSize
      }),
      loadFilteredAuditRows()
    ])
    const orderRows = result.items.map(toOrderRow).filter(matchesSelectedListStatus)
    orders.value = [...auditRows, ...orderRows].sort(compareRowsByCreatedAt)
    pagination.total = result.total + auditRows.length
  } catch {
    ElMessage.error(t('ordersPage.loadFailed'))
  } finally {
    loading.value = false
  }
}

function searchOrders() {
  pagination.page = 1
  loadOrders()
}

function resetFilters() {
  filters.keyword = ''
  filters.customerAppId = ''
  filters.orderType = ''
  filters.specialOrder = ''
  filters.status = ''
  orderDateRange.value = ''
  createdDateRange.value = ''
  resetRouteStoreFilter()
  pagination.page = 1
  loadOrders()
}

function handlePageSizeChange() {
  pagination.page = 1
  loadOrders()
}

function resetRouteStoreFilter() {
  filters.storeType = storeType.value || ''
}

function viewDetail(order: OrderRow) {
  if (isAuditRow(order)) {
    router.push({ name: 'user-special-order-audit-detail', params: { id: order.audit.id } })
    return
  }
  router.push({ name: 'user-order-detail', params: { id: order.id } })
}

function createOrder() {
  router.push({ name: 'user-order-create' })
}

function canEditOrder(order: OrderRow) {
  return (order.status === 'PENDING_PAYMENT' || order.status === 'PENDING_CONFIRM' || order.status === 'CANCELLED') && !order.sourceAuditId
}

function editOrder(order: OrderRow) {
  if (isAuditRow(order)) return
  router.push({ name: 'user-order-create', query: { orderId: String(order.id) } })
}

function canRenewOrder(order: OrderRow) {
  return !isAuditRow(order)
    && isRenewableOrderStatus(order.status)
}

function renewOrder(order: OrderRow) {
  if (!canRenewOrder(order)) return
  router.push({ name: 'user-order-create', query: { renewOrderId: String(order.id) } })
}

function canSubmitApprovedAudit(order: OrderRow) {
  return isAuditRow(order) && order.status === 'APPROVED_WAIT_SUBMIT'
}

async function submitApprovedAudit(order: OrderRow) {
  if (!isAuditRow(order)) return
  try {
    await submitApprovedSpecialAudit(order.audit.id)
    ElMessage.success(t('specialAudit.submitSuccess'))
    await loadOrders()
  } catch {
    ElMessage.error(t('specialAudit.submitFailed'))
  }
}

async function loadAppOptions() {
  try {
    appOptionList.value = await getCustomerApps()
    ensureSelectedAppMatchesStore()
  } catch {
    ElMessage.error(t('ordersPage.appOptionsLoadFailed'))
  }
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

async function loadFilteredAuditRows() {
  if (filters.specialOrder === false) return []
  const audits = await getCustomerSpecialAudits()
  return audits.filter((audit) => audit.status !== 'SUBMITTED').map(toAuditRow).filter(matchesAuditFilters)
}

function toOrderRow(order: Order): OrderRow {
  return { ...order, rowKind: 'ORDER' }
}

function toAuditRow(audit: SpecialOrderAudit): OrderRow {
  return {
    id: -audit.id,
    orderNo: audit.auditNo,
    customerId: audit.customerId,
    customerUsername: null,
    customerEmail: null,
    customerAppId: audit.customerAppId,
    sourceAuditId: audit.id,
    orderType: audit.orderType,
    orderModuleId: null,
    orderModuleName: null,
    storeType: audit.storeType,
    regionCode: audit.regionCode,
    appIdentifier: audit.appIdentifier,
    appName: audit.appName,
    appIconUrl: audit.appIconUrl,
    status: audit.status,
    orderStartDate: '',
    orderEndDate: '',
    executionHours: null,
    totalDays: null,
    quantity: null,
    unitPrice: null,
    totalAmount: Number(audit.negotiatedPrice || 0),
    expectedCompletedAt: null,
    confirmedAt: audit.reviewedAt,
    executedAt: null,
    completedAt: audit.submittedAt,
    createdAt: audit.createdAt,
    items: [],
    commentDetails: [],
    events: [],
    rowKind: 'AUDIT',
    audit
  }
}

function matchesAuditFilters(row: OrderRow) {
  if (!isAuditRow(row)) return false
  if (filters.storeType && row.storeType !== filters.storeType) return false
  if (filters.status && row.status !== filters.status) return false
  if (filters.customerAppId !== '' && row.customerAppId !== filters.customerAppId) return false
  if (filters.orderType && row.orderType !== filters.orderType) return false
  if (filters.specialOrder === false) return false
  if (orderDateRange.value) return false
  if (createdDateRange.value && !isDateInRange(row.createdAt, createdDateRange.value)) return false
  const keyword = filters.keyword.trim().toLowerCase()
  if (!keyword) return true
  return [row.orderNo, row.appName, row.appIdentifier, row.audit.requestedContent, row.audit.negotiatedContent]
    .filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(keyword))
}

function matchesSelectedListStatus(row: OrderRow) {
  if (!filters.status) return true
  return row.status === filters.status
}

function isDateInRange(value: string | null, range: [string, string]) {
  if (!value) return false
  const date = value.slice(0, 10)
  return date >= range[0] && date <= range[1]
}

function compareRowsByCreatedAt(left: OrderRow, right: OrderRow) {
  return String(right.createdAt || '').localeCompare(String(left.createdAt || ''))
}

function isAuditRow(row: Order | OrderRow): row is OrderRow & { audit: SpecialOrderAudit } {
  return (row as OrderRow).rowKind === 'AUDIT' && Boolean((row as OrderRow).audit)
}

function isOrderStatus(status: OrderListStatus | ''): status is OrderStatus {
  return status === 'PENDING_PAYMENT' || status === 'PENDING_CONFIRM' || status === 'PENDING_EXECUTION'
    || status === 'EXECUTING' || status === 'PAUSED' || status === 'COMPLETED' || status === 'CANCELLED'
}

function isRenewableOrderStatus(status: OrderListStatus) {
  return status === 'PENDING_CONFIRM'
    || status === 'PENDING_EXECUTION'
    || status === 'EXECUTING'
    || status === 'PAUSED'
    || status === 'COMPLETED'
}

function orderDateText(row: OrderRow) {
  return isAuditRow(row) ? '-' : formatOrderSchedule(row)
}

function exportOrders() {
  if (orders.value.length === 0) {
    ElMessage.warning(t('ordersPage.exportEmpty'))
    return
  }
  exportOrdersCsv(orders.value.map(withCurrentAccount), { typeLabel, storeLabel, statusLabel }, 'order')
  ElMessage.success(t('ordersPage.exported'))
}

function withCurrentAccount(row: OrderRow): OrderRow {
  return {
    ...row,
    customerUsername: row.customerUsername || auth.username || null,
    customerEmail: row.customerEmail || auth.email || null
  }
}

function typeLabel(type?: string | null) {
  return type ? t(`ordersPage.types.${type}`) : '-'
}

function orderTypeLabel(order: OrderRow) {
  return orderModuleLabel(order)
}

function statusLabel(status?: OrderListStatus | null) {
  if (!status) return '-'
  if (status === 'PENDING_REVIEW' || status === 'APPROVED_WAIT_SUBMIT' || status === 'SUBMITTED') {
    return t(`specialAudit.statuses.${status}`)
  }
  return t(`ordersPage.statuses.${status}`)
}

function itemDetails(row: OrderRow) {
  if (isAuditRow(row)) {
    return row.audit.items?.map((item) => item.keyword || item.coverageNote || item.regionCode).filter(Boolean).join('; ') || row.audit.requestedContent
  }
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

function storeLabel(store?: StoreType | null) {
  if (store === 'GOOGLE_PLAY') return 'Google Play'
  if (store === 'IPAD_STORE') return 'iPad Store'
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

const statusTagType = statusTone

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

.status-item {
  width: 260px;
  max-width: 100%;
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

.keyword-item :deep(.el-input) {
  width: 100%;
}

.status-item :deep(.el-select) {
  width: 100%;
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
  min-width: 0;
  margin-top: 16px;
  padding-top: 16px;
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

.orders-table {
  width: 100%;
}

.orders-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.orders-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #64748b;
  font-weight: 700;
}

.orders-table :deep(.el-table__cell) {
  height: 58px;
}

.order-no {
  color: #334155;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
  font-size: 12px;
  white-space: nowrap;
}

.orders-table :deep(.el-table__empty-block) {
  min-height: 86px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

.date-range-text,
.datetime-text {
  color: #334155;
  font-size: 13px;
  line-height: 1.45;
}









.app-cell {
  display: flex;
  align-items: center;
  gap: 10px;
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
  font-weight: 600;
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
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
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
.region-option {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

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

.all-region-icon {
  background: #fffbeb;
  color: #b45309;
}

.region-option-icon {
  background: #f2f4f7;
  color: #475569;
}

:global(.order-app-select-dropdown .el-select-dropdown__list),
:global(.order-region-select-dropdown .el-select-dropdown__list) {
  padding: 6px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item),
:global(.order-region-select-dropdown .el-select-dropdown__item) {
  display: flex;
  height: auto;
  align-items: center;
  border-radius: 6px;
  line-height: 1.35;
}

:global(.order-app-select-dropdown .el-select-dropdown__item) {
  min-height: 50px;
  padding: 8px 10px;
}

:global(.order-region-select-dropdown .el-select-dropdown__item) {
  min-height: 40px;
  padding: 6px 10px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item.hover),
:global(.order-app-select-dropdown .el-select-dropdown__item:hover),
:global(.order-region-select-dropdown .el-select-dropdown__item.hover),
:global(.order-region-select-dropdown .el-select-dropdown__item:hover) {
  background: #f3f7ff;
}

@media (max-width: 1280px) {
  .query-grid {
    gap: 12px;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .query-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 860px) {
  .query-grid {
    grid-template-columns: 1fr;
  }

  .status-item {
    width: 100%;
  }

  .date-item {
    width: 100%;
  }

  .query-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 520px) {
  .query-item {
    align-items: flex-start;
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .query-item > span {
    text-align: left;
  }

  .query-actions :deep(.el-button) {
    flex: 1 1 calc(50% - 4px);
    margin-left: 0;
  }
}

.mobile-order-summary{display:flex;align-items:flex-start;flex-direction:column;gap:6px;overflow-wrap:anywhere}.mobile-order-summary>span{font-size:12px}.mobile-order-summary .el-tag{max-width:100%;height:auto;white-space:normal}
@media(max-width:700px){.query-panel:not(.filters-expanded) .query-actions>.el-button{width:auto;min-width:0;flex:1 1 0}}
</style>
