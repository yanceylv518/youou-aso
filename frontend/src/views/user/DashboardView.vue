<template>
  <section class="dashboard-page">
    <header class="page-header">
      <p class="page-note">{{ t('dashboard.userSubtitle') }}</p>
      <el-button :icon="Refresh" :loading="loading" @click="loadDashboard">
        {{ t('ordersPage.refresh') }}
      </el-button>
    </header>

    <div v-loading="loading" class="metric-grid">
      <article class="metric-card accent-blue">
        <span class="metric-icon"><Wallet /></span>
        <div>
          <p>{{ t('wallet.balance') }}</p>
          <strong>{{ formatMoney(walletBalance) }}</strong>
        </div>
      </article>
      <article class="metric-card accent-green">
        <span class="metric-icon"><Grid /></span>
        <div>
          <p>{{ t('dashboard.myApps') }}</p>
          <strong>{{ apps.length }}</strong>
        </div>
      </article>
      <article class="metric-card accent-orange">
        <span class="metric-icon"><Tickets /></span>
        <div>
          <p>{{ t('dashboard.pendingOrders') }}</p>
          <strong>{{ pendingOrderCount }}</strong>
        </div>
      </article>
      <article class="metric-card accent-purple">
        <span class="metric-icon"><Finished /></span>
        <div>
          <p>{{ t('dashboard.executingOrders') }}</p>
          <strong>{{ executingOrderCount }}</strong>
        </div>
      </article>
    </div>

    <div class="quick-actions">
      <el-button type="primary" :icon="Plus" @click="router.push('/user/applications')">
        {{ t('applications.addApp') }}
      </el-button>
      <el-button :icon="Promotion" @click="router.push('/user/promotion')">
        {{ t('menu.promotion') }}
      </el-button>
      <el-button :icon="Tickets" @click="router.push('/user/orders/apple')">
        {{ t('menu.orders') }}
      </el-button>
      <el-button :icon="Wallet" @click="router.push('/user/transactions')">
        {{ t('menu.transactions') }}
      </el-button>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('dashboard.recentOrders') }}</h2>
          <el-button text @click="router.push('/user/orders/apple')">{{ t('dashboard.viewAll') }}</el-button>
        </div>
        <el-table :data="recentOrders" :empty-text="t('ordersPage.empty')">
          <el-table-column prop="orderNo" :label="t('ordersPage.orderNo')" min-width="150" />
          <el-table-column :label="t('ordersPage.app')" min-width="180">
            <template #default="{ row }">{{ row.appName }}</template>
          </el-table-column>
          <el-table-column :label="t('ordersPage.status')" width="130" align="center">
            <template #default="{ row }">
              <el-tag :type="orderStatusTag(row.status)" effect="light">
                {{ t(`ordersPage.statuses.${row.status}`) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('ordersPage.amount')" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('dashboard.recentTransactions') }}</h2>
          <el-button text @click="router.push('/user/transactions')">{{ t('dashboard.viewAll') }}</el-button>
        </div>
        <el-table :data="recentTransactions" :empty-text="t('wallet.empty')">
          <el-table-column :label="t('wallet.type')" min-width="150">
            <template #default="{ row }">{{ transactionTypeLabel(row.transactionType) }}</template>
          </el-table-column>
          <el-table-column :label="t('wallet.direction')" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.direction === 'CREDIT' ? 'success' : 'danger'" effect="light">
                {{ t(`wallet.directions.${row.direction}`) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('wallet.amount')" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column :label="t('wallet.createdAt')" min-width="160">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Finished, Grid, Plus, Promotion, Refresh, Tickets, Wallet } from '@element-plus/icons-vue'
import { getCustomerApps, type CustomerApp } from '@/api/applications'
import { getCustomerOrders, type Order, type OrderStatus } from '@/api/orders'
import {
  getCustomerWallet,
  getCustomerWalletTransactions,
  type WalletTransaction
} from '@/api/wallet'
import { useWalletTransactionTypeLabels } from '@/composables/useWalletTransactionTypeLabels'
import { useWalletStore } from '@/stores/wallet'

const { t } = useI18n()
const router = useRouter()
const { loadTransactionTypeConfigs, transactionTypeLabel } = useWalletTransactionTypeLabels('customer')
const walletStore = useWalletStore()

const loading = ref(false)
const apps = ref<CustomerApp[]>([])
const orders = ref<Order[]>([])
const transactions = ref<WalletTransaction[]>([])

const walletBalance = computed(() => walletStore.balance)
const pendingOrderCount = computed(() => orders.value.filter((order) => order.status === 'PENDING_CONFIRM').length)
const executingOrderCount = computed(() => orders.value.filter((order) => order.status === 'EXECUTING').length)
const recentOrders = computed(() => orders.value.slice(0, 6))
const recentTransactions = computed(() => transactions.value.slice(0, 6))

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  try {
    const [walletResult, appResult, orderResult, transactionResult] = await Promise.all([
      getCustomerWallet(),
      getCustomerApps(),
      getCustomerOrders(),
      getCustomerWalletTransactions(10),
      loadTransactionTypeConfigs()
    ])
    walletStore.setOverview(walletResult)
    apps.value = appResult
    orders.value = orderResult
    transactions.value = transactionResult
  } catch {
    ElMessage.error(t('dashboard.loadFailed'))
  } finally {
    loading.value = false
  }
}

function orderStatusTag(status: OrderStatus) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'danger'
  if (status === 'PENDING_PAYMENT') return 'danger'
  if (status === 'PAUSED') return 'info'
  if (status === 'EXECUTING') return 'warning'
  return 'info'
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
.dashboard-page {
  color: #182230;
}

.page-header,
.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header {
  margin-bottom: 18px;
}

h2,
p {
  letter-spacing: 0;
}

.page-note {
  max-width: 720px;
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  min-height: 116px;
  margin-bottom: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.metric-icon {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.metric-icon :deep(svg) {
  width: 23px;
  height: 23px;
  display: block;
}

.metric-card p {
  margin: 0 0 7px;
  color: #667085;
}

.metric-card strong {
  font-size: 26px;
}

.accent-blue .metric-icon {
  background: #dbe8ff;
  color: #2f7df4;
}

.accent-green .metric-icon {
  background: #d9f5e6;
  color: #37bd78;
}

.accent-orange .metric-icon {
  background: #ffedcf;
  color: #f5a11c;
}

.accent-purple .metric-icon {
  background: #e7ddff;
  color: #7657d8;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 16px;
}

.panel {
  min-width: 0;
  padding: 18px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.panel-header {
  align-items: center;
  margin-bottom: 12px;
}

.panel h2 {
  margin: 0;
  font-size: 18px;
}

.panel :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.panel :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
  font-weight: 700;
}

.panel :deep(.el-table__cell) {
  height: 52px;
}

.panel :deep(.el-table__empty-block) {
  min-height: 78px;
}

@media (max-width: 1100px) {
  .metric-grid,
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .page-header {
    flex-direction: column;
  }

  .metric-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
