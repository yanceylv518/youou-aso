<template>
  <el-container class="app-shell">
    <el-aside width="240px" class="sidebar">
      <router-link class="shell-brand" to="/admin/dashboard">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ $t('app.name') }}</span>
      </router-link>
      <el-menu :default-active="activeMenu" router unique-opened class="shell-menu">
        <el-menu-item v-if="auth.hasMenu('dashboard')" index="/admin/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ $t('menu.home') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasMenu('customers')" index="/admin/customers">
          <el-icon><User /></el-icon>
          <span>{{ $t('menu.customers') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasMenu('promotion')" index="/admin/promotion">
          <el-icon><Promotion /></el-icon>
          <span>{{ $t('menu.promotion') }}</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasMenu('applications')" index="/admin/applications">
          <el-icon><Grid /></el-icon>
          <span>{{ $t('menu.applications') }}</span>
        </el-menu-item>
        <el-sub-menu v-if="canAnyMenu(['orderExecution.pending', 'orderExecution.executing', 'orderExecution.completed'])" index="/admin/order-execution">
          <template #title>
            <el-icon><Finished /></el-icon>
            <span>{{ $t('menu.orderExecution') }}</span>
          </template>
          <el-menu-item v-if="auth.hasMenu('orderExecution.pending')" index="/admin/orders/pending-execution">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.pendingExecutionOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orderExecution.executing')" index="/admin/orders/executing">
            <el-icon><Finished /></el-icon>
            <span>{{ $t('menu.executingOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orderExecution.completed')" index="/admin/orders/completed">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ $t('menu.completedOrders') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAnyMenu(['orders.pendingReview', 'orders.pendingConfirm', 'orders.apple', 'orders.google', 'orders.ipad'])" index="/admin/orders">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.orders') }}</span>
          </template>
          <el-menu-item v-if="auth.hasMenu('orders.pendingReview')" index="/admin/pending-review-orders">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ $t('menu.pendingReviewOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orders.pendingConfirm')" index="/admin/pending-confirm-orders">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ $t('menu.pendingConfirmOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orders.apple')" index="/admin/orders/apple">
            <el-icon><Apple /></el-icon>
            <span>{{ $t('menu.appleOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orders.google')" index="/admin/orders/google">
            <el-icon><ChromeFilled /></el-icon>
            <span>{{ $t('menu.googleOrders') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('orders.ipad')" index="/admin/orders/ipad">
            <el-icon><Monitor /></el-icon>
            <span>{{ $t('menu.ipadOrders') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAnyMenu(['finance.transactions', 'finance.recharges'])" index="/admin/finance-group">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>{{ $t('menu.finance') }}</span>
          </template>
          <el-menu-item v-if="auth.hasMenu('finance.transactions')" index="/admin/finance">
            <el-icon><Notebook /></el-icon>
            <span>{{ $t('menu.financeTransactions') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('finance.recharges')" index="/admin/finance/recharges">
            <el-icon><Money /></el-icon>
            <span>{{ $t('menu.rechargeRecords') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAnyMenu(['system.pricing', 'system.walletTypes', 'system.customerService', 'system.mail', 'system.regions', 'system.adminAccounts', 'system.settings'])" index="/admin/system">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>{{ $t('menu.systemManagement') }}</span>
          </template>
          <el-menu-item v-if="auth.hasMenu('system.pricing')" index="/admin/pricing">
            <el-icon><PriceTag /></el-icon>
            <span>{{ $t('menu.pricing') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.walletTypes')" index="/admin/wallet-transaction-types">
            <el-icon><Notebook /></el-icon>
            <span>{{ $t('menu.walletTransactionTypeConfig') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.customerService')" index="/admin/customer-service">
            <el-icon><Service /></el-icon>
            <span>{{ $t('menu.customerServiceConfig') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.mail')" index="/admin/order-notification">
            <el-icon><Message /></el-icon>
            <span>{{ $t('menu.mailConfig') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.regions')" index="/admin/regions">
            <el-icon><Location /></el-icon>
            <span>{{ $t('menu.regions') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.adminAccounts')" index="/admin/admin-accounts">
            <el-icon><Avatar /></el-icon>
            <span>{{ $t('menu.adminAccounts') }}</span>
          </el-menu-item>
          <el-menu-item v-if="auth.hasMenu('system.settings')" index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>{{ $t('menu.settings') }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">
          <h1 class="page-heading" :class="{ 'is-nested': pageTitle.parent }">
            <template v-if="pageTitle.parent">
              <span class="heading-parent">{{ pageTitle.parent }}</span>
              <span class="heading-separator">/</span>
            </template>
            <span class="heading-current">{{ pageTitle.current }}</span>
          </h1>
        </div>
        <div class="topbar-actions">
          <el-dropdown trigger="click" @command="setLocale">
            <button class="language-trigger" type="button" :aria-label="t('common.language')">
              <el-icon><Connection /></el-icon>
              <span>{{ currentLanguageLabel }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-CN">
                  <el-icon class="language-check" :class="{ visible: currentLocale === 'zh-CN' }"><Check /></el-icon>
                  <span>{{ t('common.chinese') }}</span>
                </el-dropdown-item>
                <el-dropdown-item command="en-US">
                  <el-icon class="language-check" :class="{ visible: currentLocale === 'en-US' }"><Check /></el-icon>
                  <span>{{ t('common.english') }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleAccountCommand">
            <button class="account-trigger" type="button">
              <span class="account-avatar">{{ accountInitial }}</span>
              <span class="account-meta">
                <span class="account-name">{{ displayName }}</span>
              </span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="account-summary">
                  <strong>{{ displayName }}</strong>
                  <span>{{ auth.email }}</span>
                </div>
                <el-dropdown-item divided command="settings">
                  <el-icon><Setting /></el-icon>
                  <span>{{ t('menu.settings') }}</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>{{ t('auth.logout') }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="page-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { useRoute, useRouter } from 'vue-router'
import {
  Apple,
  ArrowDown,
  Avatar,
  Check,
  ChromeFilled,
  Connection,
  DocumentChecked,
  Finished,
  Grid,
  HomeFilled,
  Location,
  Message,
  Money,
  Monitor,
  Notebook,
  PriceTag,
  Promotion,
  Service,
  Setting,
  SwitchButton,
  Tickets,
  Tools,
  User
} from '@element-plus/icons-vue'
import { LOCALE_STORAGE_KEY, type AppLocale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const { locale, t } = useI18n()

const currentLocale = computed(() => locale.value as AppLocale)
const currentLanguageLabel = computed(() => {
  return currentLocale.value === 'en-US' ? t('common.english') : t('common.chinese')
})
const activeMenu = computed(() => {
  const menu = route.meta.activeMenu
  return typeof menu === 'string' ? menu : route.path
})
const pageTitle = computed(() => {
  const topTitleKey = route.meta.topTitleKey
  const titleKey = route.meta.titleKey
  const current = typeof titleKey === 'string' ? t(titleKey) : t('menu.home')
  if (typeof topTitleKey !== 'string') return { parent: '', current }

  const parent = t(topTitleKey)
  return parent === current ? { parent: '', current } : { parent, current }
})
const displayName = computed(() => auth.username || auth.email || t('account.unknown'))
const accountInitial = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'Y')

function setLocale(nextLocale: AppLocale | string | number | object) {
  if (nextLocale !== 'zh-CN' && nextLocale !== 'en-US') return
  locale.value = nextLocale
  window.localStorage.setItem(LOCALE_STORAGE_KEY, nextLocale)
  document.documentElement.lang = nextLocale === 'zh-CN' ? 'zh-CN' : 'en'
}

function canAnyMenu(codes: string[]) {
  return codes.some((code) => auth.hasMenu(code))
}

async function handleAccountCommand(command: string | number | object) {
  if (command === 'settings') {
    await router.push('/admin/settings')
    return
  }

  if (command === 'logout') {
    await logout()
  }
}

async function logout() {
  auth.logout()
  await router.replace('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid #e6eaf0;
}

.shell-brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  font-weight: 700;
  color: #182230;
  text-decoration: none;
  cursor: pointer;
}

.brand-mark {
  width: 32px;
  height: 32px;
  display: block;
  flex: 0 0 auto;
  border-radius: 50%;
  object-fit: cover;
}

.shell-menu {
  border-right: 0;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e6eaf0;
  color: #475467;
}

.topbar-title {
  min-width: 0;
}

.page-heading {
  display: inline-flex;
  max-width: 100%;
  align-items: baseline;
  gap: 8px;
  margin: 0;
  overflow: hidden;
  color: #182230;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-heading.is-nested {
  font-size: 17px;
}

.heading-parent {
  min-width: 0;
  overflow: hidden;
  color: #667085;
  font-weight: 700;
  text-overflow: ellipsis;
}

.heading-separator {
  color: #c2cad6;
  font-weight: 700;
}

.heading-current {
  min-width: 0;
  overflow: hidden;
  color: #182230;
  font-weight: 800;
  text-overflow: ellipsis;
}

.topbar-actions {
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.language-trigger {
  height: 38px;
  min-width: 112px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid #d8dee8;
  border-radius: 999px;
  background: #ffffff;
  color: #344054;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 800;
}

.language-trigger:hover {
  border-color: #b9c5d6;
  background: #f8fafc;
}

.language-check {
  opacity: 0;
  color: #1b75d0;
}

.language-check.visible {
  opacity: 1;
}

.account-trigger {
  height: 40px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px 0 6px;
  border: 1px solid #d8dee8;
  border-radius: 999px;
  background: #ffffff;
  color: #344054;
  cursor: pointer;
}

.account-avatar {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffb11b;
  color: #d9361f;
  font-weight: 800;
}

.account-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}

.account-name {
  color: #344054;
  font-weight: 700;
}

.account-summary {
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  color: #182230;
}

.account-summary span {
  color: #667085;
  font-size: 12px;
}

.page-main {
  padding: 24px;
}

@media (max-width: 700px) {
  .sidebar {
    display: none;
  }

  .topbar {
    height: auto;
    min-height: 64px;
    align-items: flex-start;
    flex-direction: column;
    padding: 10px 12px;
  }

  .topbar-actions {
    width: 100%;
    justify-content: space-between;
    gap: 8px;
    flex-wrap: wrap;
  }

  .account-trigger {
    max-width: 100%;
  }

  .account-name {
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .page-main {
    padding: 16px 12px;
  }
}
</style>

