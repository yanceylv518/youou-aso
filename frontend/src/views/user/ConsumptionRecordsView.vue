<template>
  <section class="consumption-page">
    <div class="filter-panel">
      <label class="filter-item">
        <span>{{ t('wallet.orderType') }}</span>
        <el-select v-model="filters.orderType" clearable :placeholder="t('ordersPage.allTaskTypes')">
          <el-option
            v-for="type in orderTypeOptions"
            :key="type"
            :label="t(`ordersPage.types.${type}`)"
            :value="type"
          />
        </el-select>
      </label>
      <label class="filter-item">
        <span>{{ t('wallet.startDate') }}</span>
        <el-date-picker
          v-model="filters.startDate"
          type="date"
          value-format="YYYY-MM-DD"
          :placeholder="t('wallet.startDatePlaceholder')"
        />
      </label>
      <label class="filter-item">
        <span>{{ t('wallet.endDate') }}</span>
        <el-date-picker
          v-model="filters.endDate"
          type="date"
          value-format="YYYY-MM-DD"
          :placeholder="t('wallet.endDatePlaceholder')"
        />
      </label>
      <div class="filter-actions">
        <el-button type="primary" @click="searchRecords">
          {{ t('wallet.search') }}
        </el-button>
        <el-button @click="resetFilters">
          {{ t('wallet.reset') }}
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" class="record-table" :data="records" :empty-text="t('wallet.emptyConsumption')">
        <el-table-column :label="t('wallet.orderNo')" min-width="190">
          <template #default="{ row }">
            <span class="data-id">{{ row.orderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.appName')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.appName || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.orderType')" min-width="140">
          <template #default="{ row }">
            {{ orderTypeLabel(resolveOrderType(row)) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.balanceBefore')" width="150" align="right">
          <template #default="{ row }">
            <span class="balance-value">{{ formatMoney(row.balanceBefore) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.orderAmount')" width="140" align="right">
          <template #default="{ row }">
            <span class="money-value is-debit">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.balanceAfterConsumption')" width="150" align="right">
          <template #default="{ row }">
            <span class="balance-value">{{ formatMoney(row.balanceAfter) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.paidAt')" min-width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
          @current-change="loadRecords"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getCustomerWalletTransactionsPage, type WalletTransaction } from '@/api/wallet'
import type { OrderType } from '@/api/orders'

const { t } = useI18n()
const loading = ref(false)
const records = ref<WalletTransaction[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const filters = reactive<{
  orderType: OrderType | ''
  startDate: string
  endDate: string
}>({
  orderType: '',
  startDate: '',
  endDate: ''
})
const orderTypeOptions: OrderType[] = [
  'KEYWORD_INSTALL',
  'DOWNLOAD',
  'RATING',
  'REVIEW',
  'RANK_GUARANTEE',
  'CHART_RANK_GUARANTEE',
  'KEYWORD_COVERAGE'
]
onMounted(() => {
  loadRecords()
})

async function loadRecords() {
  loading.value = true
  try {
    const result = await getCustomerWalletTransactionsPage({
      transactionType: 'ORDER_DEDUCT',
      orderType: filters.orderType,
      createdDateFrom: filters.startDate,
      createdDateTo: filters.endDate,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    records.value = result.items
    pagination.total = result.total
  } catch {
    ElMessage.error(t('wallet.loadFailed'))
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.orderType = ''
  filters.startDate = ''
  filters.endDate = ''
  pagination.page = 1
  loadRecords()
}

function searchRecords() {
  pagination.page = 1
  loadRecords()
}

function handlePageSizeChange() {
  pagination.page = 1
  loadRecords()
}

function resolveOrderType(record: WalletTransaction) {
  if (record.orderType) return record.orderType
  const match = record.remark?.match(/^ORDER_DEDUCT:([A-Z_]+)/)
  return (match?.[1] as OrderType | undefined) || null
}

function orderTypeLabel(orderType?: string | null) {
  return orderType ? t(`ordersPage.types.${orderType}`) : '-'
}

function formatMoney(value: string | number | undefined | null) {
  if (value === undefined || value === null || value === '') return '-'
  return Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.consumption-page {
  color: #0f172a;
}

.filter-panel {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item span {
  color: #101828;
  font-size: 14px;
}

.filter-item :deep(.el-select),
.filter-item :deep(.el-date-editor) {
  width: 220px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-card {
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.record-table {
  width: 100%;
}

.record-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.record-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #64748b;
  font-weight: 700;
}

.record-table :deep(.el-table__cell) {
  height: 56px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

@media (max-width: 860px) {
  .filter-panel {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-item {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-item :deep(.el-select),
  .filter-item :deep(.el-date-editor) {
    width: 100%;
  }
}
</style>
