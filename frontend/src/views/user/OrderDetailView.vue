<template>
  <section class="order-detail-page">
    <div class="detail-toolbar">
      <el-button :icon="ArrowLeft" @click="goBack">{{ t('orderDetail.back') }}</el-button>
      <el-button :icon="Refresh" @click="loadOrder">{{ t('ordersPage.refresh') }}</el-button>
      <el-button :icon="Download" :loading="exporting" :disabled="!order" @click="handleExportDetail">
        {{ t('orderDetail.exportDetail') }}
      </el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="!order" :description="t('orderDetail.empty')" />
    <template v-else>
      <div class="hero-card">
        <div class="app-card">
          <div class="app-icon">
            <img v-if="order.appIconUrl" :src="order.appIconUrl" :alt="order.appName" />
            <span v-else>{{ order.appName.slice(0, 1).toUpperCase() }}</span>
          </div>
          <div class="app-copy">
            <strong>{{ order.appName }}</strong>
            <span>{{ order.appIdentifier }}</span>
            <div class="app-tags">
              <span>{{ storeLabel(order.storeType) }}</span>
              <span>{{ typeLabel(order.orderType) }}</span>
            </div>
          </div>
        </div>
        <div class="order-status">
          <span>{{ t('ordersPage.orderNo') }}</span>
          <strong>{{ order.orderNo }}</strong>
          <el-tag :type="statusTagType(order.status)" effect="light">{{ statusLabel(order.status) }}</el-tag>
        </div>
      </div>

      <div class="detail-grid">
        <section class="detail-card">
          <h2>{{ t('orderDetail.basicInfo') }}</h2>
          <dl class="info-list">
            <div><dt>{{ t('ordersPage.orderCategory') }}</dt><dd>{{ order.sourceAuditId ? t('ordersPage.categories.SPECIAL') : t('ordersPage.categories.REGULAR') }}</dd></div>
            <div><dt>{{ orderDateLabel }}</dt><dd>{{ order.orderStartDate }} - {{ order.orderEndDate }}</dd></div>
            <div><dt>{{ t('ordersPage.region') }}</dt><dd>{{ regionLabel(order.regionCode) }}</dd></div>
            <div><dt>{{ t('orderDetail.totalDays') }}</dt><dd>{{ valueOrDash(order.totalDays) }}</dd></div>
            <div v-if="isKeywordInstall"><dt>{{ t('orderDetail.executionHours') }}</dt><dd>{{ valueOrDash(order.executionHours) }}</dd></div>
            <div><dt>{{ t('ordersPage.createdAt') }}</dt><dd>{{ formatDateTime(order.createdAt) }}</dd></div>
            <div><dt>{{ t('ordersPage.expectedCompletedAt') }}</dt><dd>{{ formatDateTime(order.expectedCompletedAt) }}</dd></div>
          </dl>
        </section>

        <section class="detail-card amount-card">
          <h2>{{ t('orderDetail.amountInfo') }}</h2>
          <div class="amount-summary">
            <span>{{ t('ordersPage.amount') }}</span>
            <strong>{{ money(order.totalAmount) }}</strong>
          </div>
          <div class="fee-metrics">
            <div>
              <span>{{ t('orderDetail.totalDays') }}</span>
              <strong>{{ valueOrDash(order.totalDays) }}</strong>
            </div>
            <div>
              <span>{{ t('ordersPage.quantity') }}</span>
              <strong>{{ valueOrDash(order.quantity) }}</strong>
            </div>
            <div>
              <span>{{ t('orderDetail.unitPrice') }}</span>
              <strong>{{ displayUnitPrice }}</strong>
            </div>
          </div>
        </section>
      </div>

      <section class="detail-card">
        <h2>{{ t('orderDetail.itemDetails') }}</h2>
        <el-table :data="order.items" class="detail-table" :empty-text="t('orderDetail.noItems')">
          <el-table-column :label="t('ordersPage.type')" min-width="150">
            <template #default="{ row }">{{ itemTypeLabel(row.itemType) }}</template>
          </el-table-column>
          <el-table-column v-if="showItemNameColumn" :label="t('orderDetail.itemName')" min-width="180">
            <template #default="{ row }">{{ row.itemName || '-' }}</template>
          </el-table-column>
          <el-table-column :label="t('ordersPage.region')" min-width="150">
            <template #default="{ row }">{{ detailRegionLabel(row.regionCode) }}</template>
          </el-table-column>
          <el-table-column :label="t('ordersPage.quantity')" width="110" align="right">
            <template #default="{ row }">{{ valueOrDash(row.quantity) }}</template>
          </el-table-column>
          <el-table-column :label="t('orderDetail.unitPrice')" width="130" align="right">
            <template #default="{ row }">{{ row.unitPrice === null ? '-' : money(row.unitPrice) }}</template>
          </el-table-column>
          <el-table-column :label="t('ordersPage.amount')" width="130" align="right">
            <template #default="{ row }">{{ row.amount === null ? '-' : money(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="order.commentDetails?.length" class="detail-card">
        <h2>{{ t('orderDetail.commentDetails') }}</h2>
        <el-table :data="order.commentDetails" class="detail-table">
          <el-table-column :label="t('ordersPage.region')" min-width="150">
            <template #default="{ row }">{{ detailRegionLabel(row.regionCode) }}</template>
          </el-table-column>
          <el-table-column :label="t('orderCreate.starLevel')" width="110" align="center">
            <template #default="{ row }">{{ row.starLevel }}</template>
          </el-table-column>
          <el-table-column :label="t('orderCreate.reviewTitle')" min-width="180">
            <template #default="{ row }">{{ row.commentTitle }}</template>
          </el-table-column>
          <el-table-column :label="t('orderCreate.reviewContent')" min-width="260">
            <template #default="{ row }">{{ row.commentContent }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="detail-card">
        <h2>{{ t('orderDetail.timeline') }}</h2>
        <div class="timeline-grid">
          <div v-for="item in timelineItems" :key="item.label" class="timeline-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Download, Refresh } from '@element-plus/icons-vue'
import { getCustomerOrder, type Order, type OrderStatus, type OrderType } from '@/api/orders'
import { getEnabledRegions, type MarketRegion, type StoreType } from '@/api/applications'
import { exportOrderDetailExcel } from '@/utils/orderDetailExport'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const order = ref<Order | null>(null)
const regions = ref<MarketRegion[]>([])
const loading = ref(false)
const exporting = ref(false)

const orderId = computed(() => Number(route.params.id))
const isKeywordInstall = computed(() => order.value?.orderType === 'KEYWORD_INSTALL')
const orderDateLabel = computed(() => isKeywordInstall.value ? t('orderDetail.executionDate') : t('ordersPage.orderTime'))
const showItemNameColumn = computed(() => {
  return Boolean(order.value?.items.some((item) => item.itemName && item.itemName.trim()))
})
const displayUnitPrice = computed(() => {
  if (!order.value) return '-'
  if (order.value.unitPrice !== null) return money(order.value.unitPrice)
  const prices = [...new Set(order.value.items
    .map((item) => item.unitPrice)
    .filter((price): price is number => price !== null && price !== undefined)
    .map((price) => Number(price).toFixed(2)))]
  if (prices.length === 0) return '-'
  if (prices.length === 1) return `$${prices[0]}`
  return t('orderDetail.multipleUnitPrices')
})
const timelineItems = computed(() => {
  if (!order.value) return []
  return [
    { label: t('ordersPage.createdAt'), value: formatDateTime(order.value.createdAt) },
    { label: t('orderDetail.confirmedAt'), value: formatDateTime(order.value.confirmedAt) },
    { label: t('orderDetail.executedAt'), value: formatDateTime(order.value.executedAt) },
    { label: t('orderDetail.completedAt'), value: formatDateTime(order.value.completedAt) }
  ]
})

onMounted(() => {
  loadRegions()
  loadOrder()
})

async function loadOrder() {
  if (!Number.isFinite(orderId.value)) return
  loading.value = true
  try {
    order.value = await getCustomerOrder(orderId.value)
  } catch {
    ElMessage.error(t('orderDetail.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function loadRegions() {
  try {
    regions.value = await getEnabledRegions()
  } catch {
    regions.value = []
  }
}

function goBack() {
  router.back()
}

async function handleExportDetail() {
  if (!order.value) return
  exporting.value = true
  try {
    await exportOrderDetailExcel(order.value, { storeLabel, regionLabel })
    ElMessage.success(t('orderDetail.exportSuccess'))
  } catch {
    ElMessage.error(t('orderDetail.exportFailed'))
  } finally {
    exporting.value = false
  }
}

function regionLabel(code?: string | null) {
  if (!code) return '-'
  if (code === 'MULTI') return t('orderDetail.multiRegion')
  const region = regions.value.find((item) => item.code === code)
  if (!region) return code
  return `${locale.value.startsWith('zh') ? region.nameZh : region.nameEn} (${code})`
}

function detailRegionLabel(code?: string | null) {
  return regionLabel(code || order.value?.regionCode)
}

function typeLabel(type?: OrderType | null) {
  return type ? t(`ordersPage.types.${type}`) : '-'
}

function itemTypeLabel(type?: string | null) {
  return type ? t(`ordersPage.itemTypes.${type}`) : '-'
}

function statusLabel(status?: OrderStatus | null) {
  return status ? t(`ordersPage.statuses.${status}`) : '-'
}

function storeLabel(store?: StoreType | null) {
  if (store === 'GOOGLE_PLAY') return 'Google Play'
  if (store === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
}

function statusTagType(status?: OrderStatus | null) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'EXECUTING') return 'primary'
  if (status === 'PAUSED') return 'info'
  if (status === 'PENDING_EXECUTION') return 'warning'
  if (status === 'PENDING_PAYMENT') return 'danger'
  if (status === 'CANCELLED') return 'danger'
  return 'info'
}

function formatDateTime(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

function money(value: number) {
  return `$${Number(value).toFixed(2)}`
}

function valueOrDash(value?: number | null) {
  return value === null || value === undefined ? '-' : value
}
</script>

<style scoped>
.order-detail-page {
  color: #182230;
}

.detail-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.hero-card,
.detail-card {
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 16px;
  padding: 20px;
}

.app-card {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.app-icon {
  display: flex;
  width: 56px;
  height: 56px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 12px;
  background: #eef4ff;
  color: #2f7df4;
  font-size: 22px;
  font-weight: 800;
}

.app-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.app-copy strong {
  font-size: 18px;
}

.app-copy > span {
  color: #667085;
  font-size: 13px;
}

.app-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.app-tags span {
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: #eef4ff;
  color: #175cd3;
  font-size: 12px;
  line-height: 22px;
}

.order-status {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 6px;
  text-align: right;
}

.order-status span {
  color: #667085;
  font-size: 12px;
}

.order-status strong {
  font-size: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.detail-card {
  margin-bottom: 16px;
  padding: 18px;
}

.detail-card h2 {
  margin: 0 0 16px;
  font-size: 16px;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 24px;
  margin: 0;
}

.info-list div {
  min-width: 0;
}

.info-list dt {
  margin-bottom: 5px;
  color: #667085;
  font-size: 12px;
}

.info-list dd {
  margin: 0;
  overflow: hidden;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amount-card {
  display: flex;
  flex-direction: column;
}

.amount-summary {
  padding: 14px 16px;
  border: 1px solid #d7e7ff;
  border-radius: 8px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%);
}

.amount-summary span,
.fee-metrics span {
  display: block;
  color: #667085;
  font-size: 12px;
}

.amount-summary strong {
  display: block;
  margin-top: 8px;
  color: #1677ff;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
}

.fee-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.fee-metrics div {
  min-width: 0;
  padding: 12px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #f8fafc;
}

.fee-metrics strong {
  display: block;
  margin-top: 6px;
  overflow: hidden;
  color: #182230;
  font-size: 16px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-table {
  width: 100%;
}

.detail-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
}

.timeline-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.timeline-item {
  min-width: 0;
  padding: 12px;
  border-radius: 8px;
  background: #f7f9fc;
}

.timeline-item span {
  display: block;
  margin-bottom: 6px;
  color: #667085;
  font-size: 12px;
}

.timeline-item strong {
  font-size: 13px;
}

@media (max-width: 900px) {
  .hero-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-status {
    align-items: flex-start;
    text-align: left;
  }

  .detail-grid,
  .info-list,
  .timeline-grid {
    grid-template-columns: 1fr;
  }
}
</style>
