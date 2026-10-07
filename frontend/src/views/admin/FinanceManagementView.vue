<template>
  <section class="finance-page">
    <div class="finance-control-card" :class="{ 'recharge-controls': isRechargeRecordsPage }">
    <div class="toolbar">
      <p v-if="!isRechargeRecordsPage" class="page-note">{{ pageCopy.subtitle }}</p>
      <div class="toolbar-actions">
        <el-button type="primary" :plain="isRechargeRecordsPage" :icon="Plus" @click="openAdjustmentDialog">
          {{ t('wallet.balanceAdjustment') }}
        </el-button>
        <el-button v-if="isRechargeRecordsPage" type="primary" :icon="Plus" @click="openRechargeDialog">
          {{ t('wallet.recharge') }}
        </el-button>
        <el-button :loading="loading" @click="loadTransactions">
          {{ t('wallet.refresh') }}
        </el-button>
      </div>
    </div>

    <div class="filter-panel">
      <label class="filter-item">
        <span>{{ t('wallet.customer') }}</span>
        <el-select
          v-model="customerId"
          clearable
          filterable
          popper-class="finance-customer-select-dropdown"
          :loading="customersLoading"
          :placeholder="t('wallet.customerPlaceholder')"
          class="customer-select"
        >
          <el-option
            v-for="customer in customers"
            :key="customer.id"
            :label="customerLabel(customer.id)"
            :value="customer.id"
          >
            <div class="customer-option">
              <strong>{{ customer.username }}</strong>
              <span>{{ customer.email }}</span>
            </div>
          </el-option>
        </el-select>
      </label>
      <div class="filter-actions">
        <el-button type="primary" @click="searchTransactions">
          {{ t('wallet.search') }}
        </el-button>
        <el-button @click="resetFilter">
          {{ t('wallet.reset') }}
        </el-button>
      </div>
    </div>

    </div>

    <div class="table-card">
      <el-table v-loading="loading" max-height="calc(100vh - 250px)" scrollbar-always-on class="transaction-table" :data="transactions" :empty-text="t('wallet.empty')">
        <el-table-column :label="t(isRechargeRecordsPage ? 'visual.rechargeTime' : 'visual.transactionTime')" :min-width="isRechargeRecordsPage ? 190 : 170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.customer')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="customer-cell">
              <strong>{{ customerName(row.customerId) }}</strong>
              <span>{{ customerEmail(row.customerId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isRechargeRecordsPage" :label="t('wallet.type')" min-width="140">
          <template #default="{ row }">
            {{ transactionTypeLabel(row.transactionType) }}
          </template>
        </el-table-column>
        <el-table-column v-if="!isRechargeRecordsPage" :label="t('wallet.direction')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'CREDIT' ? 'success' : 'danger'" effect="light">
              {{ t(`wallet.directions.${row.direction}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t(isRechargeRecordsPage ? 'wallet.rechargeAmount' : 'visual.changeAmount')" width="140" align="right">
          <template #default="{ row }">
            <span class="money-value" :class="row.direction === 'CREDIT' ? 'is-credit' : 'is-debit'">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t(isRechargeRecordsPage ? 'visual.balanceAfterRecharge' : 'wallet.balanceAfter')" width="150" align="right">
          <template #default="{ row }">
            <span class="balance-value">{{ formatMoney(row.balanceAfter) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isRechargeRecordsPage" :label="t('visual.remark')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark?.trim() || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="!isRechargeRecordsPage" :label="t('wallet.relatedOrder')" min-width="190">
          <template #default="{ row }">
            <span class="related-id">{{ row.orderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isRechargeRecordsPage" :label="t('wallet.orderType')" min-width="140">
          <template #default="{ row }">
            {{ orderTypeLabel(row.orderType) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.transactionNo')" :min-width="isRechargeRecordsPage ? 230 : 170">
          <template #default="{ row }"><span class="data-id">{{ row.transactionNo }}</span></template>
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

    <el-dialog append-to-body v-model="rechargeDialogVisible" :title="t('wallet.rechargeTitle')" width="420px">
      <el-form class="recharge-form" label-position="top">
        <el-form-item :label="t('wallet.customer')" required>
          <el-select
            v-model="rechargeForm.customerId"
            filterable
            popper-class="finance-customer-select-dropdown"
            :loading="customersLoading"
            :placeholder="t('wallet.customerPlaceholder')"
          >
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="customerLabel(customer.id)"
              :value="customer.id"
            >
              <div class="customer-option">
                <strong>{{ customer.username }}</strong>
                <span>{{ customer.email }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('wallet.rechargeAmount')" required>
          <el-input-number
            v-model="rechargeForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item :label="t('wallet.rechargeRemark')">
          <el-input
            v-model="rechargeForm.remark"
            type="textarea"
            :rows="3"
            :maxlength="200"
            show-word-limit
            :placeholder="t('wallet.rechargeRemarkPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">
            {{ t('wallet.cancel') }}
          </el-button>
          <el-button type="primary" :loading="recharging" @click="submitRecharge">
            {{ t('wallet.confirmRecharge') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog append-to-body v-model="adjustmentDialogVisible" :title="t('wallet.balanceAdjustmentTitle')" width="460px">
      <el-form class="recharge-form" label-position="top">
        <el-form-item :label="t('wallet.customer')" required>
          <el-select
            v-model="adjustmentForm.customerId"
            filterable
            popper-class="finance-customer-select-dropdown"
            :loading="customersLoading"
            :placeholder="t('wallet.customerPlaceholder')"
          >
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="customerLabel(customer.id)"
              :value="customer.id"
            >
              <div class="customer-option">
                <strong>{{ customer.username }}</strong>
                <span>{{ customer.email }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('wallet.adjustmentType')" required>
          <el-select v-model="adjustmentForm.adjustmentType" :placeholder="t('wallet.adjustmentTypePlaceholder')">
            <el-option
              v-for="type in adjustmentTypeOptions"
              :key="type"
              :label="t(`wallet.adjustmentTypes.${type}`)"
              :value="type"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('wallet.adjustmentAmount')" required>
          <el-input-number
            v-model="adjustmentForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item :label="t('wallet.adjustmentRemark')" required>
          <el-input
            v-model="adjustmentForm.remark"
            type="textarea"
            :rows="3"
            :maxlength="200"
            show-word-limit
            :placeholder="t('wallet.adjustmentRemarkPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adjustmentDialogVisible = false">
            {{ t('wallet.cancel') }}
          </el-button>
          <el-button type="primary" :loading="adjusting" @click="submitAdjustment">
            {{ t('wallet.confirmAdjustment') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { formatCurrency } from '@/utils/presentation'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminCustomers, type CustomerAccount } from '@/api/customers'
import {
  adjustCustomerWallet,
  getAdminWalletTransactionsPage,
  rechargeCustomerWallet,
  type AdminBalanceAdjustmentType,
  type WalletTransaction,
  type WalletTransactionType
} from '@/api/wallet'
import { useWalletTransactionTypeLabels } from '@/composables/useWalletTransactionTypeLabels'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { loadTransactionTypeConfigs, transactionTypeLabel } = useWalletTransactionTypeLabels('admin')
const loading = ref(false)
const customersLoading = ref(false)
const recharging = ref(false)
const adjusting = ref(false)
const rechargeDialogVisible = ref(false)
const adjustmentDialogVisible = ref(false)
const customerId = ref<number | undefined>()
const customers = ref<CustomerAccount[]>([])
const transactions = ref<WalletTransaction[]>([])
const adjustmentTypeOptions: AdminBalanceAdjustmentType[] = ['REFUND', 'GIFT', 'DEDUCT']
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const rechargeForm = reactive<{
  customerId: number | undefined
  amount: number | undefined
  remark: string
}>({
  customerId: undefined,
  amount: undefined,
  remark: ''
})
const adjustmentForm = reactive<{
  customerId: number | undefined
  adjustmentType: AdminBalanceAdjustmentType | ''
  amount: number | undefined
  remark: string
}>({
  customerId: undefined,
  adjustmentType: '',
  amount: undefined,
  remark: ''
})
const routeTransactionType = computed(() => {
  return typeof route.meta.transactionType === 'string'
    ? route.meta.transactionType as WalletTransactionType
    : undefined
})
const isRechargeRecordsPage = computed(() => routeTransactionType.value === 'ADMIN_RECHARGE')
const pageCopy = computed(() => {
  if (isRechargeRecordsPage.value) {
    return {
      title: t('wallet.rechargeRecordsTitle'),
      subtitle: t('wallet.rechargeRecordsSubtitle')
    }
  }
  return {
    title: t('wallet.adminTitle'),
    subtitle: t('wallet.adminSubtitle')
  }
})

onMounted(() => {
  loadCustomers()
  loadTransactionTypeConfigs()
  loadTransactions()
})

watch(
  () => route.fullPath,
  () => {
    pagination.page = 1
    loadTransactions()
  }
)

async function loadTransactions() {
  loading.value = true
  try {
    const result = await getAdminWalletTransactionsPage({
      customerId: customerId.value,
      transactionType: routeTransactionType.value,
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

async function loadCustomers() {
  customersLoading.value = true
  try {
    customers.value = await getAdminCustomers()
  } catch {
    ElMessage.error(t('customers.loadFailed'))
  } finally {
    customersLoading.value = false
  }
}

function resetFilter() {
  customerId.value = undefined
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

function openRechargeDialog() {
  rechargeForm.customerId = customerId.value
  rechargeForm.amount = undefined
  rechargeForm.remark = ''
  rechargeDialogVisible.value = true
}

function openAdjustmentDialog() {
  adjustmentForm.customerId = customerId.value
  adjustmentForm.adjustmentType = ''
  adjustmentForm.amount = undefined
  adjustmentForm.remark = ''
  adjustmentDialogVisible.value = true
}

async function submitRecharge() {
  if (!rechargeForm.customerId || !rechargeForm.amount || rechargeForm.amount <= 0) {
    ElMessage.warning(t('wallet.rechargeRequired'))
    return
  }

  recharging.value = true
  try {
    await rechargeCustomerWallet({
      customerId: rechargeForm.customerId,
      amount: rechargeForm.amount,
      remark: rechargeForm.remark.trim() || undefined
    })
    ElMessage.success(t('wallet.rechargeSuccess'))
    rechargeDialogVisible.value = false
    customerId.value = rechargeForm.customerId
    await loadTransactions()
  } catch {
    ElMessage.error(t('wallet.rechargeFailed'))
  } finally {
    recharging.value = false
  }
}

async function submitAdjustment() {
  if (!adjustmentForm.customerId || !adjustmentForm.adjustmentType || !adjustmentForm.amount || adjustmentForm.amount <= 0 || !adjustmentForm.remark.trim()) {
    ElMessage.warning(t('wallet.adjustmentRequired'))
    return
  }

  adjusting.value = true
  try {
    await adjustCustomerWallet({
      customerId: adjustmentForm.customerId,
      adjustmentType: adjustmentForm.adjustmentType,
      amount: adjustmentForm.amount,
      remark: adjustmentForm.remark.trim()
    })
    ElMessage.success(t('wallet.adjustmentSuccess'))
    adjustmentDialogVisible.value = false
    customerId.value = adjustmentForm.customerId
    if (routeTransactionType.value) {
      await router.push('/admin/finance')
    }
    await loadTransactions()
  } catch {
    ElMessage.error(t('wallet.adjustmentFailed'))
  } finally {
    adjusting.value = false
  }
}

const formatMoney = formatCurrency

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}

function customerLabel(id: number) {
  const customer = customers.value.find((item) => item.id === id)
  return customer ? `${customer.username} / ${customer.email}` : t('wallet.unknownCustomer')
}

function customerName(id: number) {
  return customers.value.find((item) => item.id === id)?.username || t('wallet.unknownCustomer')
}

function customerEmail(id: number) {
  return customers.value.find((item) => item.id === id)?.email || '-'
}

function orderTypeLabel(orderType?: string | null) {
  return orderType ? t(`ordersPage.types.${orderType}`) : '-'
}
</script>

<style scoped>
.finance-page {
  color: #0f172a;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.toolbar-actions {
  display: flex;
  margin-left: auto;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

p {
  letter-spacing: 0;
}

.page-note {
  max-width: 720px;
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
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

.customer-select {
  width: 280px;
}

.customer-option,
.customer-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  line-height: 1.35;
}

.customer-option strong,
.customer-cell strong {
  display: block;
  overflow: hidden;
  color: #0f172a;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-option span,
.customer-cell span {
  display: block;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.finance-customer-select-dropdown .el-select-dropdown__list) {
  padding: 6px;
}

:global(.finance-customer-select-dropdown .el-select-dropdown__item) {
  height: auto;
  min-height: 52px;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  line-height: 1.35;
}

:global(.finance-customer-select-dropdown .el-select-dropdown__item + .el-select-dropdown__item) {
  margin-top: 2px;
}

:global(.finance-customer-select-dropdown .el-select-dropdown__item.hover),
:global(.finance-customer-select-dropdown .el-select-dropdown__item:hover) {
  background: #f3f7ff;
}

:global(.finance-customer-select-dropdown .el-select-dropdown__item.selected) {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
}

:global(.finance-customer-select-dropdown .customer-option) {
  width: 100%;
}

:global(.finance-customer-select-dropdown .customer-option strong) {
  color: #0f172a;
  font-size: 13px;
}

:global(.finance-customer-select-dropdown .customer-option span) {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-card {
  background: #ffffff;
}

.transaction-table {
  width: 100%;
}

.transaction-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.transaction-table :deep(.el-table__header th) {
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

.recharge-form :deep(.el-select),
.recharge-form :deep(.el-input) {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 860px) {
  .toolbar {
    flex-direction: column;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

.finance-control-card {
  margin-bottom: 18px;
  overflow: hidden;
  border: 1px solid #dfe7f1;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgb(15 23 42 / 5%);
}

.finance-control-card .toolbar {
  min-height: 68px;
  margin: 0;
  padding: 16px 18px;
  border-bottom: 1px solid #edf1f6;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
}

.finance-control-card .filter-panel {
  min-height: 62px;
  margin: 0;
  padding: 13px 18px;
  background: #ffffff;
}

.finance-control-card .filter-item > span {
  color: #475569;
  font-weight: 650;
}

.finance-control-card .customer-select {
  width: 300px;
}

.finance-control-card .filter-actions {
  margin-left: 2px;
}

@media (max-width: 860px) {
  .finance-control-card .toolbar {
    align-items: flex-start;
    padding: 15px;
  }

  .finance-control-card .filter-panel {
    align-items: stretch;
    flex-direction: column;
    padding: 15px;
  }

  .finance-control-card .filter-item {
    align-items: stretch;
    flex-direction: column;
  }

  .finance-control-card .customer-select {
    width: 100%;
  }
}

.recharge-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 24px;
  padding: 16px 18px;
}

.recharge-controls .filter-panel {
  order: -1;
  flex: 1 1 480px;
  min-width: 0;
  min-height: 0;
  padding: 0;
  flex-direction: row;
}

.recharge-controls .toolbar {
  min-height: 0;
  padding: 0;
  border: 0;
  background: transparent;
  margin-left: auto;
}

.recharge-controls .toolbar-actions > .el-button,
.recharge-controls .filter-actions > .el-button {
  margin-left: 0;
}

.recharge-controls .filter-item {
  flex-direction: row;
  align-items: center;
}

.recharge-controls .customer-select {
  width: 280px;
}

@media (max-width: 600px) {
  .recharge-controls { padding: 14px; gap: 14px; }
  .recharge-controls .filter-panel { flex-basis: 100%; }
  .recharge-controls .filter-item { width: 100%; }
  .recharge-controls .customer-select { flex: 1; width: 0; min-width: 0; }
  .recharge-controls .toolbar { width: 100%; margin: 0; }
  .recharge-controls .toolbar-actions { gap: 8px; }
  .recharge-controls .toolbar-actions > .el-button { flex: 1; padding-inline: 10px; }
}
</style>
