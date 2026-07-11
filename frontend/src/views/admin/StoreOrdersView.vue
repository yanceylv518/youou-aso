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
        <label v-if="showOrderTypeFilter" class="query-item">
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
        <label v-if="showStatusFilter" class="query-item status-item">
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
        <el-button type="primary" color="#ff6a00" :icon="Search" @click="searchOrders">{{ t('ordersPage.search') }}</el-button>
        <el-button :icon="Delete" @click="resetFilters">{{ t('ordersPage.clear') }}</el-button>
        <el-button v-if="showBatchConfirm" :icon="Check" @click="batchConfirm">{{ t('ordersPage.batchConfirm') }}</el-button>
        <el-button v-if="showBatchExecute" :icon="VideoPlay" @click="batchExecute">{{ t('ordersPage.batchExecute') }}</el-button>
        <el-button v-if="showBatchPause" :icon="VideoPause" @click="batchPause">{{ t('ordersPage.batchPause') }}</el-button>
        <el-button :icon="Download" @click="exportOrders">{{ t('ordersPage.export') }}</el-button>
      </div>
    </div>

    <div v-if="showBatchActions" class="selection-note">{{ t('ordersPage.selectedCount', { count: selectedOrders.length }) }}</div>

    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="orders"
        class="orders-table"
        :empty-text="t('ordersPage.empty')"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="showBatchActions" type="selection" width="48" :selectable="isSelectableOrderRow" />
        <el-table-column prop="orderNo" :label="t('ordersPage.orderNo')" min-width="210">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.customer')" min-width="180">
          <template #default="{ row }">
            <div class="customer-cell">
              <strong>{{ orderCustomerName(row) }}</strong>
              <span>{{ orderCustomerEmail(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.app')" min-width="220">
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
        <el-table-column :label="t('ordersPage.orderTime')" min-width="190">
          <template #default="{ row }">{{ orderDateText(row) }}</template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.taskType')" min-width="150">
          <template #default="{ row }">{{ typeLabel(row.orderType) }}</template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.orderCategory')" min-width="130">
          <template #default="{ row }">{{ isAuditRow(row) || row.sourceAuditId ? t('ordersPage.categories.SPECIAL') : t('ordersPage.categories.REGULAR') }}</template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.status')" min-width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.createdAt')" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.actions')" width="210" fixed="right" align="center">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(row)">
                {{ t('ordersPage.detail') }}
              </el-button>
              <el-button v-if="row.status === 'PENDING_CONFIRM'" size="small" :icon="Check" @click="confirmOrder(row)">
                {{ t('ordersPage.confirm') }}
              </el-button>
              <el-button v-if="showBatchExecute && row.status === 'PENDING_EXECUTION'" size="small" :icon="VideoPlay" @click="executeOrder(row)">
                {{ t('ordersPage.execute') }}
              </el-button>
              <el-button v-if="row.status === 'EXECUTING'" size="small" :icon="VideoPause" @click="pauseOrder(row)">
                {{ t('ordersPage.pause') }}
              </el-button>
              <el-button v-if="row.status === 'PAUSED'" size="small" :icon="VideoPlay" @click="resumeOrder(row)">
                {{ t('ordersPage.resume') }}
              </el-button>
              <el-button v-if="canRenewOrder(row)" size="small" text type="primary" :icon="RefreshRight" @click="renewOrder(row)">
                {{ t('ordersPage.renewOrder') }}
              </el-button>
              <el-button v-if="isAuditRow(row) && row.status === 'PENDING_REVIEW'" size="small" :icon="Check" @click="openReviewAudit(row)">
                {{ t('specialAudit.review') }}
              </el-button>
              <el-button v-if="canCancel(row)" size="small" type="danger" plain @click="cancelOrder(row)">
                {{ t('ordersPage.cancelOrder') }}
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

    <el-dialog v-model="reviewDialogVisible" :title="t('specialAudit.review')" width="520px">
      <el-form label-position="top">
        <el-form-item :label="t('specialAudit.content')">
          <el-input v-model="reviewForm.negotiatedContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="t('specialAudit.price')">
          <el-input-number v-model="reviewForm.negotiatedPrice" :min="0.01" :precision="2" :step="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submittingAudit" @click="submitReviewAudit">{{ t('specialAudit.approve') }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Download, Plus, RefreshRight, Search, VideoPause, VideoPlay, View } from '@element-plus/icons-vue'
import {
  batchConfirmAdminOrders,
  batchExecuteAdminOrders,
  batchPauseAdminOrders,
  cancelAdminOrder,
  confirmAdminOrder,
  executeAdminOrder,
  getAdminOrdersPage,
  pauseAdminOrder,
  resumeAdminOrder,
  type Order,
  type OrderListStatus,
  type OrderStatus,
  type OrderType
} from '@/api/orders'
import { getAdminApps, type CustomerApp, type StoreType } from '@/api/applications'
import { getAdminCustomers, type CustomerAccount } from '@/api/customers'
import {
  cancelSpecialAudit,
  getAdminSpecialAudits,
  reviewSpecialAudit,
  type SpecialAuditStatus,
  type SpecialOrderAudit
} from '@/api/specialOrderAudits'
import { exportOrdersCsv } from '@/utils/orderExport'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

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
const customerList = ref<CustomerAccount[]>([])
const selectedOrders = ref<OrderRow[]>([])
const loading = ref(false)
const customersLoading = ref(false)
const reviewDialogVisible = ref(false)
const submittingAudit = ref(false)
const selectedAudit = ref<SpecialOrderAudit | null>(null)
const orderDateRange = ref<[string, string] | ''>('')
const createdDateRange = ref<[string, string] | ''>('')
const reviewForm = reactive({
  negotiatedContent: '',
  negotiatedPrice: 0
})

const filters = reactive<{
  keyword: string
  customerId: number | ''
  customerAppId: number | ''
  storeType: StoreType | ''
  orderType: OrderType | ''
  specialOrder: boolean | ''
  status: OrderListStatus | ''
}>({
  keyword: '',
  customerId: '',
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
  'KEYWORD_COVERAGE'
]

const storeType = computed(() => route.meta.storeType as StoreType)
const statusFilter = computed(() => route.meta.statusFilter as OrderListStatus | undefined)
const showStatusFilter = computed(() => !statusFilter.value)
const showOrderTypeFilter = computed(() => !statusFilter.value)
const showBatchConfirm = computed(() => (statusFilter.value || filters.status) === 'PENDING_CONFIRM')
const showBatchExecute = computed(() => statusFilter.value === 'PENDING_EXECUTION')
const showBatchPause = computed(() => statusFilter.value === 'EXECUTING')
const showBatchActions = computed(() => showBatchConfirm.value || showBatchExecute.value || showBatchPause.value)
const title = computed(() => {
  if (statusFilter.value === 'PENDING_REVIEW') return t('menu.pendingReviewOrders')
  if (statusFilter.value === 'PENDING_EXECUTION') return t('menu.pendingExecutionOrders')
  if (statusFilter.value === 'EXECUTING') return t('menu.executingOrders')
  if (statusFilter.value === 'COMPLETED') return t('menu.completedOrders')
  if (storeType.value === 'GOOGLE_PLAY') return t('menu.googleOrders')
  if (storeType.value === 'IPAD_STORE') return t('menu.ipadOrders')
  return t('menu.appleOrders')
})
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
  resetRouteFilters()
  loadCustomerOptions()
  loadAppOptions()
  loadOrders()
})

watch(
  () => route.fullPath,
  () => {
    resetRouteFilters()
    ensureSelectedAppMatchesStore()
    selectedOrders.value = []
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
      getAdminOrdersPage({
      storeType: filters.storeType,
      status: orderStatus,
      keyword: filters.keyword,
      customerId: filters.customerId === '' ? null : filters.customerId,
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

function resetFilters() {
  filters.keyword = ''
  filters.customerId = ''
  filters.customerAppId = ''
  filters.orderType = ''
  filters.specialOrder = ''
  orderDateRange.value = ''
  createdDateRange.value = ''
  resetRouteFilters()
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

function resetRouteFilters() {
  filters.storeType = storeType.value || ''
  filters.status = statusFilter.value || ''
}

function viewDetail(order: OrderRow) {
  if (isAuditRow(order)) {
    return
  }
  router.push({ name: 'admin-order-detail', params: { id: order.id } })
}

function createOrder() {
  router.push({ name: 'admin-order-create' })
}

function canRenewOrder(order: OrderRow) {
  return !isAuditRow(order)
    && isRenewableOrderStatus(order.status)
}

function renewOrder(order: OrderRow) {
  if (!canRenewOrder(order)) return
  router.push({ name: 'admin-order-create', query: { renewOrderId: String(order.id) } })
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

function orderCustomerName(order: OrderRow) {
  const customer = customerMap.value.get(order.customerId)
  return order.customerUsername || customer?.username || t('ordersPage.customerIdShort', { id: order.customerId })
}

function orderCustomerEmail(order: OrderRow) {
  const customer = customerMap.value.get(order.customerId)
  return order.customerEmail || customer?.email || '-'
}

async function loadFilteredAuditRows() {
  if (statusFilter.value && isOrderStatus(statusFilter.value)) return []
  if (filters.specialOrder === false) return []
  const audits = await getAdminSpecialAudits()
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
    rowKind: 'AUDIT',
    audit
  }
}

function matchesAuditFilters(row: OrderRow) {
  if (!isAuditRow(row)) return false
  if (filters.storeType && row.storeType !== filters.storeType) return false
  if (filters.status && row.status !== filters.status) return false
  if (filters.customerId !== '' && row.customerId !== filters.customerId) return false
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

function isAuditStatus(status: string): status is SpecialAuditStatus {
  return status === 'PENDING_REVIEW' || status === 'APPROVED_WAIT_SUBMIT' || status === 'CANCELLED' || status === 'SUBMITTED'
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
  return isAuditRow(row) ? '-' : `${row.orderStartDate} - ${row.orderEndDate}`
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

async function executeOrder(order: OrderRow) {
  if (isAuditRow(order)) return
  try {
    await executeAdminOrder(order.id)
    ElMessage.success(t('ordersPage.executeSuccess'))
    await loadOrders()
  } catch {
    ElMessage.error(t('ordersPage.operationFailed'))
  }
}

async function confirmOrder(order: OrderRow) {
  if (isAuditRow(order)) return
  try {
    await confirmAdminOrder(order.id)
    ElMessage.success(t('ordersPage.confirmSuccess'))
    await loadOrders()
  } catch {
    ElMessage.error(t('ordersPage.operationFailed'))
  }
}

async function pauseOrder(order: OrderRow) {
  if (isAuditRow(order)) return
  try {
    await ElMessageBox.confirm(t('ordersPage.pauseConfirm', { orderNo: order.orderNo }), t('ordersPage.pauseOrderTitle'), {
      type: 'warning',
      confirmButtonText: t('ordersPage.pause'),
      cancelButtonText: t('ordersPage.keepOrder')
    })
    await pauseAdminOrder(order.id)
    ElMessage.success(t('ordersPage.pauseSuccess'))
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

async function resumeOrder(order: OrderRow) {
  if (isAuditRow(order)) return
  try {
    await ElMessageBox.confirm(t('ordersPage.resumeConfirm', { orderNo: order.orderNo }), t('ordersPage.resumeOrderTitle'), {
      type: 'warning',
      confirmButtonText: t('ordersPage.resume'),
      cancelButtonText: t('ordersPage.keepOrder')
    })
    await resumeAdminOrder(order.id)
    ElMessage.success(t('ordersPage.resumeSuccess'))
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

async function cancelOrder(order: OrderRow) {
  try {
    const { value } = await ElMessageBox.prompt(t('ordersPage.cancelPrompt'), t('ordersPage.cancelOrderTitle'), {
      confirmButtonText: t('ordersPage.confirmCancel'),
      cancelButtonText: t('ordersPage.keepOrder'),
      inputType: 'textarea',
      inputPlaceholder: t('ordersPage.cancelReasonPlaceholder')
    })
    if (isAuditRow(order)) {
      await cancelSpecialAudit(order.audit.id, value || t('ordersPage.cancelOrder'))
    } else {
      await cancelAdminOrder(order.id, value)
    }
    ElMessage.success(t('ordersPage.cancelSuccess'))
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

async function batchExecute() {
  const executable = selectedOrders.value.filter((order) => order.status === 'PENDING_EXECUTION')
  if (executable.length === 0) {
    ElMessage.warning(t('ordersPage.noExecutableSelected'))
    return
  }
  try {
    await batchExecuteAdminOrders(executable.map((order) => order.id))
    ElMessage.success(t('ordersPage.executeSuccess'))
    selectedOrders.value = []
    await loadOrders()
  } catch {
    ElMessage.error(t('ordersPage.operationFailed'))
  }
}

async function batchConfirm() {
  const confirmable = selectedOrders.value.filter((order) => order.status === 'PENDING_CONFIRM')
  if (confirmable.length === 0) {
    ElMessage.warning(t('ordersPage.noConfirmableSelected'))
    return
  }
  try {
    await ElMessageBox.confirm(t('ordersPage.batchConfirmConfirm', { count: confirmable.length }), t('ordersPage.batchConfirm'), {
      type: 'warning',
      confirmButtonText: t('ordersPage.confirm'),
      cancelButtonText: t('ordersPage.keepOrder')
    })
    await batchConfirmAdminOrders(confirmable.map((order) => order.id))
    ElMessage.success(t('ordersPage.batchConfirmSuccess'))
    selectedOrders.value = []
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

async function batchPause() {
  const pausable = selectedOrders.value.filter((order) => order.status === 'EXECUTING')
  if (pausable.length === 0) {
    ElMessage.warning(t('ordersPage.noPausableSelected'))
    return
  }
  try {
    await ElMessageBox.confirm(t('ordersPage.batchPauseConfirm', { count: pausable.length }), t('ordersPage.batchPause'), {
      type: 'warning',
      confirmButtonText: t('ordersPage.pause'),
      cancelButtonText: t('ordersPage.keepOrder')
    })
    await batchPauseAdminOrders(pausable.map((order) => order.id))
    ElMessage.success(t('ordersPage.batchPauseSuccess'))
    selectedOrders.value = []
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('ordersPage.operationFailed'))
    }
  }
}

function handleSelectionChange(selection: OrderRow[]) {
  selectedOrders.value = selection.filter((row) => !isAuditRow(row))
}

function canCancel(order: OrderRow) {
  if (isAuditRow(order)) {
    return order.status === 'PENDING_REVIEW'
  }
  return order.status === 'PENDING_CONFIRM' || order.status === 'PENDING_EXECUTION'
}

function isSelectableOrderRow(row: OrderRow) {
  return !isAuditRow(row)
}

function openReviewAudit(row: OrderRow) {
  if (!isAuditRow(row)) return
  selectedAudit.value = row.audit
  reviewForm.negotiatedContent = row.audit.negotiatedContent || row.audit.requestedContent || ''
  reviewForm.negotiatedPrice = Number(row.audit.negotiatedPrice || 0)
  reviewDialogVisible.value = true
}

async function submitReviewAudit() {
  if (!selectedAudit.value || !reviewForm.negotiatedContent.trim() || reviewForm.negotiatedPrice <= 0) {
    ElMessage.warning(t('specialAudit.reviewRequired'))
    return
  }
  submittingAudit.value = true
  try {
    await reviewSpecialAudit(selectedAudit.value.id, {
      negotiatedContent: reviewForm.negotiatedContent.trim(),
      negotiatedPrice: reviewForm.negotiatedPrice
    })
    ElMessage.success(t('specialAudit.reviewSuccess'))
    reviewDialogVisible.value = false
    selectedAudit.value = null
    await loadOrders()
  } catch {
    ElMessage.error(t('specialAudit.reviewFailed'))
  } finally {
    submittingAudit.value = false
  }
}

function exportOrders() {
  if (orders.value.length === 0) {
    ElMessage.warning(t('ordersPage.exportEmpty'))
    return
  }
  exportOrdersCsv(orders.value.map(withExportCustomer), { typeLabel, storeLabel, statusLabel }, 'order')
  ElMessage.success(t('ordersPage.exported'))
}

function withExportCustomer(row: OrderRow): OrderRow {
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

function statusTagType(status?: OrderListStatus | null) {
  if (status === 'PENDING_REVIEW') return 'warning'
  if (status === 'APPROVED_WAIT_SUBMIT') return 'danger'
  if (status === 'SUBMITTED') return 'info'
  if (status === 'COMPLETED') return 'success'
  if (status === 'EXECUTING') return 'primary'
  if (status === 'PAUSED') return 'info'
  if (status === 'PENDING_EXECUTION') return 'warning'
  if (status === 'PENDING_PAYMENT') return 'danger'
  if (status === 'CANCELLED') return 'danger'
  return 'info'
}

</script>

<style scoped>
.orders-page {
  color: #182230;
}

.query-panel {
  margin-bottom: 16px;
  padding: 16px 16px 14px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.query-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  min-width: 0;
  max-width: 100%;
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

.keyword-item :deep(.el-input) {
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

.selection-note {
  min-height: 22px;
  margin-bottom: 8px;
  color: #667085;
  font-size: 13px;
}

.table-card {
  overflow: hidden;
  border: 1px solid #e4e9f2;
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
  background: #f6f7f9;
  color: #667085;
  font-weight: 500;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

.order-no {
  color: #344054;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
  font-size: 12px;
  white-space: nowrap;
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
  color: #182230;
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
  background: #eef4ff;
  color: #2d7dd2;
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
  color: #475467;
  font-size: 12px;
  line-height: 22px;
}

.store-badge {
  background: #eef4ff;
  color: #175cd3;
}

.muted {
  color: #667085;
  font-size: 12px;
}

.row-actions {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}

.row-actions :deep(.el-button + .el-button) {
  margin-left: 0;
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
  background: #eef4ff;
  color: #2f7df4;
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
  color: #182230;
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
  color: #182230;
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
  background: #fff3e6;
  color: #ff6a00;
}

.region-option-icon {
  background: #f2f4f7;
  color: #475467;
}

:global(.order-app-select-dropdown .el-select-dropdown__list),
:global(.order-customer-select-dropdown .el-select-dropdown__list),
:global(.order-region-select-dropdown .el-select-dropdown__list) {
  padding: 6px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item),
:global(.order-customer-select-dropdown .el-select-dropdown__item),
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

:global(.order-region-select-dropdown .el-select-dropdown__item) {
  min-height: 40px;
  padding: 6px 10px;
}

:global(.order-app-select-dropdown .el-select-dropdown__item.hover),
:global(.order-app-select-dropdown .el-select-dropdown__item:hover),
:global(.order-customer-select-dropdown .el-select-dropdown__item.hover),
:global(.order-customer-select-dropdown .el-select-dropdown__item:hover),
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

  .status-item,
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
