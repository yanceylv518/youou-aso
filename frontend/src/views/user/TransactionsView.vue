<template>
  <section class="wallet-page">
    <div class="filter-panel">
      <label class="filter-item">
        <span>{{ t('wallet.type') }}</span>
        <el-select v-model="filters.transactionType" clearable :placeholder="t('ordersPage.all')">
          <el-option
            v-for="type in transactionTypeOptions"
            :key="type"
            :label="transactionTypeLabel(type)"
            :value="type"
          />
        </el-select>
      </label>
      <label class="filter-item">
        <span>{{ t('wallet.direction') }}</span>
        <el-select v-model="filters.direction" clearable :placeholder="t('ordersPage.all')">
          <el-option
            v-for="direction in directionOptions"
            :key="direction"
            :label="t(`wallet.directions.${direction}`)"
            :value="direction"
          />
        </el-select>
      </label>
      <div class="filter-actions">
        <el-button type="primary" @click="searchTransactions">
          {{ t('wallet.search') }}
        </el-button>
        <el-button @click="resetFilters">
          {{ t('wallet.reset') }}
        </el-button>
        <el-button :loading="loading" @click="loadTransactions">
          {{ t('wallet.refresh') }}
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" class="transaction-table" :data="transactions" :empty-text="t('wallet.empty')">
        <el-table-column prop="transactionNo" :label="t('wallet.transactionNo')" min-width="170" />
        <el-table-column :label="t('wallet.type')" min-width="140">
          <template #default="{ row }">
            {{ transactionTypeLabel(row.transactionType) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.relatedOrder')" min-width="190">
          <template #default="{ row }">
            {{ row.orderNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.orderType')" min-width="140">
          <template #default="{ row }">
            {{ orderTypeLabel(row.orderType) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.direction')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'CREDIT' ? 'success' : 'danger'" effect="light">
              {{ t(`wallet.directions.${row.direction}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.amount')" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.balanceAfter')" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.balanceAfter) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.createdAt')" min-width="170">
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
          @current-change="loadTransactions"
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
import {
  getCustomerWalletTransactionsPage,
  type WalletDirection,
  type WalletTransaction,
  type WalletTransactionType
} from '@/api/wallet'
import { useWalletTransactionTypeLabels } from '@/composables/useWalletTransactionTypeLabels'

const { t } = useI18n()
const { loadTransactionTypeConfigs, transactionTypeLabel } = useWalletTransactionTypeLabels('customer')
const loading = ref(false)
const transactions = ref<WalletTransaction[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const filters = reactive<{
  transactionType: WalletTransactionType | ''
  direction: WalletDirection | ''
}>({
  transactionType: '',
  direction: ''
})
const transactionTypeOptions: WalletTransactionType[] = [
  'ORDER_DEDUCT',
  'ORDER_REFUND',
  'ADMIN_RECHARGE',
  'ADMIN_ADJUSTMENT',
  'ADMIN_REFUND',
  'ADMIN_GIFT',
  'ADMIN_DEDUCT',
  'DELIVERY'
]
const directionOptions: WalletDirection[] = ['CREDIT', 'DEBIT']
onMounted(() => {
  loadTransactionTypeConfigs()
  loadTransactions()
})

async function loadTransactions() {
  loading.value = true
  try {
    const result = await getCustomerWalletTransactionsPage({
      transactionType: filters.transactionType,
      direction: filters.direction,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    transactions.value = result.items
    pagination.total = result.total
  } catch {
    ElMessage.error(t('wallet.loadFailed'))
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.transactionType = ''
  filters.direction = ''
  pagination.page = 1
  loadTransactions()
}

function searchTransactions() {
  pagination.page = 1
  loadTransactions()
}

function handlePageSizeChange() {
  pagination.page = 1
  loadTransactions()
}

function formatMoney(value: string | number | undefined | null) {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return `$${Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}

function orderTypeLabel(orderType?: string | null) {
  return orderType ? t(`ordersPage.types.${orderType}`) : '-'
}
</script>

<style scoped>
.wallet-page {
  color: #182230;
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

.filter-item :deep(.el-select) {
  width: 180px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-card {
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.transaction-table {
  width: 100%;
}

.transaction-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.transaction-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
  font-weight: 700;
}

.transaction-table :deep(.el-table__cell) {
  height: 56px;
}

.transaction-table :deep(.el-table__empty-block) {
  min-height: 86px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

@media (max-width: 860px) {
  .filter-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-item :deep(.el-select) {
    width: 100%;
  }
}
</style>
