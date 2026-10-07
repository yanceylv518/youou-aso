<template>
  <section class="dashboard-page">
    <header class="page-header dashboard-hero">
      <div class="hero-copy">
        <span class="hero-eyebrow">{{ t('dashboard.adminEyebrow') }}</span>
        <h1>{{ t('dashboard.adminTitle') }}</h1>
        <p class="page-note">{{ t('dashboard.adminSubtitle') }}</p>
      </div>
      <el-button class="hero-refresh" :icon="Refresh" :loading="loading" @click="loadDashboard">
        {{ t('dashboard.refresh') }}
      </el-button>
      <i class="hero-glow hero-glow-one" />
      <i class="hero-glow hero-glow-two" />
    </header>

    <div v-loading="loading" class="metric-grid">
      <article class="metric-card accent-orange">
        <span class="metric-icon"><DocumentChecked /></span>
        <div>
          <p>{{ t('menu.pendingConfirmOrders') }}</p>
          <strong>{{ pendingConfirmCount }}</strong>
        </div>
      </article>
      <article class="metric-card accent-purple">
        <span class="metric-icon"><Finished /></span>
        <div>
          <p>{{ t('dashboard.pendingAudits') }}</p>
          <strong>{{ pendingAuditCount }}</strong>
        </div>
      </article>
      <article class="metric-card accent-blue">
        <span class="metric-icon"><Tickets /></span>
        <div>
          <p>{{ t('dashboard.pendingExecution') }}</p>
          <strong>{{ pendingExecutionCount }}</strong>
        </div>
      </article>
      <article class="metric-card accent-green">
        <span class="metric-icon"><Grid /></span>
        <div>
          <p>{{ t('dashboard.managedApps') }}</p>
          <strong>{{ apps.length }}</strong>
        </div>
      </article>
    </div>

    <div class="quick-actions">
      <el-button type="primary" :icon="DocumentChecked" @click="router.push('/admin/pending-confirm-orders')">
        {{ t('menu.pendingConfirmOrders') }}
      </el-button>
      <el-button :icon="Finished" @click="router.push('/admin/audits')">
        {{ t('menu.auditManagement') }}
      </el-button>
      <el-button :icon="Money" @click="router.push('/admin/finance')">
        {{ t('menu.finance') }}
      </el-button>
      <el-button :icon="PriceTag" @click="router.push('/admin/pricing')">
        {{ t('menu.pricing') }}
      </el-button>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('dashboard.pendingWork') }}</h2>
          <el-button text @click="router.push('/admin/pending-confirm-orders')">{{ t('dashboard.viewAll') }}</el-button>
        </div>
        <el-table :data="priorityOrders" :empty-text="t('dashboard.noOrders')">
          <el-table-column prop="orderNo" :label="t('dashboard.orderNo')" min-width="150" />
          <el-table-column :label="t('dashboard.app')" min-width="180">
            <template #default="{ row }">{{ row.appName }}</template>
          </el-table-column>
          <el-table-column :label="t('dashboard.status')" width="130" align="center">
            <template #default="{ row }">
              <el-tag class="order-status-tag" :type="orderStatusTag(row.status)" effect="light">
                {{ t(`ordersPage.statuses.${row.status}`) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('dashboard.amount')" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('dashboard.pendingSpecialAudits') }}</h2>
          <el-button text @click="router.push('/admin/audits')">{{ t('dashboard.viewAll') }}</el-button>
        </div>
        <el-table :data="pendingAudits" :empty-text="t('dashboard.noSpecialRequests')">
          <el-table-column prop="auditNo" :label="t('dashboard.reviewNo')" min-width="160" />
          <el-table-column :label="t('dashboard.app')" min-width="180">
            <template #default="{ row }">{{ row.appName }}</template>
          </el-table-column>
          <el-table-column :label="t('dashboard.orderType')" min-width="150">
            <template #default="{ row }">{{ t(`ordersPage.types.${row.orderType}`) }}</template>
          </el-table-column>
          <el-table-column :label="t('dashboard.status')" width="120" align="center">
            <template #default="{ row }">
              <el-tag class="order-status-tag" type="warning" effect="light">{{ t(`specialAudit.statuses.${row.status}`) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <section class="panel ledger-panel">
      <div class="panel-header">
        <h2>{{ t('dashboard.recentTransactions') }}</h2>
        <el-button text @click="router.push('/admin/finance')">{{ t('dashboard.viewAll') }}</el-button>
      </div>
      <el-table :data="transactions" :empty-text="t('wallet.empty')">
        <el-table-column prop="transactionNo" :label="t('wallet.transactionNo')" min-width="170" />
        <el-table-column prop="customerId" :label="t('wallet.customerId')" width="110" />
        <el-table-column :label="t('wallet.type')" min-width="150">
          <template #default="{ row }">{{ t(`wallet.types.${row.transactionType}`) }}</template>
        </el-table-column>
        <el-table-column :label="t('wallet.direction')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'CREDIT' ? 'success' : 'danger'" effect="light">
              {{ t(`wallet.directions.${row.direction}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('wallet.amount')" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column :label="t('wallet.createdAt')" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { statusTone, formatCurrency } from '@/utils/presentation'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DocumentChecked,
  Finished,
  Grid,
  Money,
  PriceTag,
  Refresh,
  Tickets
} from '@element-plus/icons-vue'
import { getAdminApps, type CustomerApp } from '@/api/applications'
import { getAdminOrders, type Order, type OrderStatus } from '@/api/orders'
import { getAdminSpecialAudits, type SpecialOrderAudit } from '@/api/specialOrderAudits'
import { getAdminWalletTransactions, type WalletTransaction } from '@/api/wallet'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const orders = ref<Order[]>([])
const audits = ref<SpecialOrderAudit[]>([])
const transactions = ref<WalletTransaction[]>([])
const apps = ref<CustomerApp[]>([])

const pendingConfirmCount = computed(() => orders.value.filter((order) => order.status === 'PENDING_CONFIRM').length)
const pendingExecutionCount = computed(() => orders.value.filter((order) => order.status === 'PENDING_EXECUTION').length)
const pendingAuditCount = computed(() => audits.value.filter((audit) => audit.status === 'PENDING_REVIEW').length)
const priorityOrders = computed(() => {
  return orders.value
    .filter((order) => ['PENDING_CONFIRM', 'PENDING_EXECUTION', 'EXECUTING', 'PAUSED'].includes(order.status))
    .slice(0, 6)
})
const pendingAudits = computed(() => audits.value.filter((audit) => audit.status === 'PENDING_REVIEW').slice(0, 6))

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  try {
    const [orderResult, auditResult, transactionResult, appResult] = await Promise.all([
      canReadOrders() ? getAdminOrders() : Promise.resolve([]),
      auth.hasMenu('orders.pendingReview') ? getAdminSpecialAudits() : Promise.resolve([]),
      auth.hasMenu('finance.transactions') ? getAdminWalletTransactions({ limit: 8 }) : Promise.resolve([]),
      auth.hasMenu('applications') ? getAdminApps() : Promise.resolve([])
    ])
    orders.value = orderResult
    audits.value = auditResult
    transactions.value = transactionResult
    apps.value = appResult
  } catch {
    ElMessage.error(t('dashboard.loadFailed'))
  } finally {
    loading.value = false
  }
}

function canReadOrders() {
  return [
    'orders.pendingReview', 'orders.pendingConfirm', 'orders.apple', 'orders.google', 'orders.ipad',
    'orderExecution.pending', 'orderExecution.executing', 'orderExecution.completed'
  ].some((code) => auth.hasMenu(code))
}

const orderStatusTag = statusTone

const formatMoney = formatCurrency

function formatDate(value: string | undefined | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.dashboard-page {
  color: #0f172a;
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
  color: #64748b;
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
  border: 1px solid #e2e8f0;
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
  color: #64748b;
}

.metric-card strong {
  font-size: 26px;
}

.accent-blue .metric-icon {
  background: #dbeafe;
  color: #2563eb;
}

.accent-green .metric-icon {
  background: #f0fdf4;
  color: #16a34a;
}

.accent-orange .metric-icon {
  background: #fffbeb;
  color: #b45309;
}

.accent-purple .metric-icon {
  background: #f5f3ff;
  color: #7c3aed;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.panel {
  min-width: 0;
  padding: 18px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.ledger-panel {
  margin-top: 16px;
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
  background: #f6f7f9;
  color: #64748b;
  font-weight: 500;
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
