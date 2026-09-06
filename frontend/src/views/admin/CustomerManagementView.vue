<template>
  <section class="customer-page">
    <div class="summary-grid">
      <div class="summary-card">
        <span>{{ t('customers.totalCustomers') }}</span>
        <strong>{{ customers.length }}</strong>
      </div>
      <div class="summary-card">
        <span>{{ t('customers.enabledCustomers') }}</span>
        <strong>{{ enabledCount }}</strong>
      </div>
      <div class="summary-card">
        <span>{{ t('customers.disabledCustomers') }}</span>
        <strong>{{ disabledCount }}</strong>
      </div>
      <div class="summary-card">
        <span>{{ t('customers.totalAvailableBalance') }}</span>
        <strong>{{ formatMoney(totalBalance) }}</strong>
      </div>
    </div>

    <div class="customer-card">
      <div class="customer-toolbar">
        <div class="filter-panel">
          <el-input
            v-model.trim="keyword"
            clearable
            :prefix-icon="Search"
            :placeholder="t('customers.keywordPlaceholder')"
            class="keyword-input"
            @keyup.enter="searchCustomers"
          />
          <el-button type="primary" :icon="Search" @click="searchCustomers">{{ t('ordersPage.search') }}</el-button>
          <el-button :icon="Delete" @click="resetFilters">{{ t('ordersPage.clear') }}</el-button>
        </div>
        <el-button :icon="Refresh" :loading="loading" @click="loadCustomers">
          {{ t('ordersPage.refresh') }}
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        class="customer-table"
        :data="customers"
        :empty-text="t('customers.empty')"
      >
        <el-table-column :label="t('customers.customer')" min-width="420">
          <template #default="{ row }">
            <div class="customer-cell">
              <div class="customer-avatar">{{ avatarText(row.username) }}</div>
              <div class="customer-copy">
                <strong>{{ row.username }}</strong>
                <span>{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('customers.status')" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" effect="light" round>
              {{ t(`customers.statuses.${displayStatus(row.status)}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          :label="t('wallet.balance')"
          width="150"
          align="right"
          class-name="balance-column"
          label-class-name="balance-column"
        >
          <template #default="{ row }">
            <span class="money-cell">{{ formatMoney(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="t('customers.lastLoginAt')"
          width="230"
          class-name="date-column"
          label-class-name="date-column"
        >
          <template #default="{ row }">
            <span class="date-cell">{{ formatDate(row.lastLoginAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.createdAt')" width="200">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('ordersPage.actions')" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="displayStatus(row.status) !== 'ENABLED'"
              size="small"
              type="success"
              plain
              @click="changeStatus(row, 'ENABLED')"
            >
              {{ t('customers.enable') }}
            </el-button>
            <el-button
              v-else
              size="small"
              plain
              @click="changeStatus(row, 'DISABLED')"
            >
              {{ t('customers.disable') }}
            </el-button>
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
          @current-change="loadCustomers"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Refresh, Search } from '@element-plus/icons-vue'
import {
  getAdminCustomersPage,
  updateAdminCustomerStatus,
  type AccountStatus,
  type CustomerAccount
} from '@/api/customers'

const { t } = useI18n()
const loading = ref(false)
const keyword = ref('')
const customers = ref<CustomerAccount[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const enabledCount = computed(() => customers.value.filter((customer) => displayStatus(customer.status) === 'ENABLED').length)
const disabledCount = computed(() => customers.value.length - enabledCount.value)
const totalBalance = computed(() => customers.value.reduce((sum, customer) => sum + Number(customer.balance || 0), 0))

onMounted(loadCustomers)

async function loadCustomers() {
  loading.value = true
  try {
    const result = await getAdminCustomersPage({
      keyword: keyword.value,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    customers.value = result.items
    pagination.total = result.total
  } catch {
    ElMessage.error(t('customers.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function changeStatus(row: CustomerAccount, status: AccountStatus) {
  try {
    await ElMessageBox.confirm(
      t('customers.confirmStatusMessage', {
        name: row.username,
        status: t(`customers.statuses.${status}`)
      }),
      t('customers.confirmStatusTitle'),
      { type: 'warning', confirmButtonText: t('customers.confirm'), cancelButtonText: t('applications.cancel') }
    )
    await updateAdminCustomerStatus(row.id, status)
    ElMessage.success(t('customers.statusUpdated'))
    await loadCustomers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(t('customers.statusUpdateFailed'))
    }
  }
}

function resetFilters() {
  keyword.value = ''
  pagination.page = 1
  void loadCustomers()
}

function searchCustomers() {
  pagination.page = 1
  void loadCustomers()
}

function handlePageSizeChange() {
  pagination.page = 1
  void loadCustomers()
}

function statusTag(status: AccountStatus) {
  return displayStatus(status) === 'ENABLED' ? 'success' : 'danger'
}

function displayStatus(status: AccountStatus) {
  return status === 'ENABLED' ? 'ENABLED' : 'DISABLED'
}

function avatarText(username: string) {
  return username.slice(0, 1).toUpperCase()
}

function formatMoney(value: string | number | undefined | null) {
  if (value === undefined || value === null || value === '') return '-'
  return `$${Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.customer-page {
  padding: 12px 16px 24px;
  color: #0f172a;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-card {
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 26px rgb(24 34 48 / 5%);
}

.summary-card span {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
}

.summary-card strong {
  display: block;
  overflow: hidden;
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-card {
  overflow: hidden;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgb(24 34 48 / 6%);
}

.customer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #e9eef5;
  background: #ffffff;
}

.filter-panel {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 8px;
}

.keyword-input {
  width: min(420px, 100%);
}

.customer-table {
  width: 100%;
}

.customer-table :deep(.el-table__cell) {
  padding: 12px 0;
  color: #334155;
}

.customer-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
}

.customer-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f9fbff;
}

.customer-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.customer-table :deep(.balance-column .cell) {
  padding-right: 30px;
}

.customer-table :deep(.date-column .cell) {
  padding-left: 34px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #edf1f7;
}

.customer-cell {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
}

.customer-avatar {
  display: flex;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 800;
}

.customer-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.customer-copy strong,
.customer-copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-copy strong {
  color: #0f172a;
  font-weight: 700;
}

.customer-copy span {
  color: #64748b;
  font-size: 12px;
}

.money-cell {
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}

.date-cell {
  display: inline-block;
  font-variant-numeric: tabular-nums;
}

@media (max-width: 760px) {
  .customer-page {
    padding: 10px;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .customer-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-panel {
    flex-wrap: wrap;
  }

  .keyword-input {
    width: 100%;
  }
}

@media (max-width: 520px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
