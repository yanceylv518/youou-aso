<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar">
      <router-link class="shell-brand" to="/user/dashboard">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ $t('app.name') }}</span>
      </router-link>
      <el-menu :default-active="activeMenu" router unique-opened class="shell-menu">
        <el-menu-item index="/user/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ $t('menu.home') }}</span>
        </el-menu-item>
        <el-menu-item index="/user/promotion">
          <el-icon><Promotion /></el-icon>
          <span>{{ $t('menu.promotion') }}</span>
        </el-menu-item>
        <el-menu-item index="/user/applications">
          <el-icon><Grid /></el-icon>
          <span>{{ $t('menu.applications') }}</span>
        </el-menu-item>
        <el-sub-menu index="/user/orders">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.orders') }}</span>
          </template>
          <el-menu-item index="/user/orders/apple">
            <el-icon><Apple /></el-icon>
            <span>{{ $t('menu.appleOrders') }}</span>
          </el-menu-item>
          <el-menu-item index="/user/orders/google">
            <el-icon><ChromeFilled /></el-icon>
            <span>{{ $t('menu.googleOrders') }}</span>
          </el-menu-item>
          <el-menu-item index="/user/orders/ipad">
            <el-icon><Monitor /></el-icon>
            <span>{{ $t('menu.ipadOrders') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/user/transactions">
          <el-icon><Wallet /></el-icon>
          <span>{{ $t('menu.transactions') }}</span>
        </el-menu-item>
        <el-menu-item index="/user/consumption-records">
          <el-icon><Money /></el-icon>
          <span>{{ $t('menu.consumptionRecords') }}</span>
        </el-menu-item>
        <el-menu-item index="/user/settings">
          <el-icon><Setting /></el-icon>
          <span>{{ $t('menu.settings') }}</span>
        </el-menu-item>
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
          <div class="wallet-pill">
            <span class="wallet-label">{{ t('wallet.accountBalance') }}:</span>
            <strong>{{ formatMoney(walletBalance) }}</strong>
            <button type="button" class="wallet-refresh" :disabled="walletLoading" @click="loadWallet">
              <el-icon><Refresh /></el-icon>
              <span>{{ t('wallet.latest') }}</span>
            </button>
            <el-button type="warning" class="wallet-recharge" @click="goRecharge">
              {{ t('wallet.recharge') }}
            </el-button>
          </div>
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
                  <span v-if="accountSubline">{{ accountSubline }}</span>
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

  <el-dialog v-model="rechargeDialogVisible" :title="t('wallet.recharge')" width="360px" class="recharge-dialog">
    <div class="recharge-qr-panel">
      <div v-if="customerServiceQrUrl" class="qr-image-wrap">
        <img :src="customerServiceQrUrl" :alt="t('wallet.customerServiceQr')" />
      </div>
      <div v-else class="qr-placeholder">
        <span>{{ t('wallet.qrNotConfigured') }}</span>
      </div>
      <strong v-if="customerServiceName" class="customer-service-name">{{ customerServiceName }}</strong>
      <p>{{ customerServiceHint || t('wallet.scanCustomerServiceQr') }}</p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { useRoute, useRouter } from 'vue-router'
import {
  Apple,
  ArrowDown,
  Check,
  ChromeFilled,
  Connection,
  Grid,
  HomeFilled,
  Money,
  Monitor,
  Promotion,
  Refresh,
  Setting,
  SwitchButton,
  Tickets,
  Wallet
} from '@element-plus/icons-vue'
import { getCustomerServiceConfig } from '@/api/support'
import { LOCALE_STORAGE_KEY, type AppLocale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'
import { useWalletStore } from '@/stores/wallet'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const walletStore = useWalletStore()
const { locale, t } = useI18n()
const LEGACY_DEFAULT_SERVICE_NAME = 'Youou-ASO Support'
const LEGACY_DEFAULT_CONTACT_HINT = 'Scan the QR code to contact customer service for recharge.'
const ZH_DEFAULT_SERVICE_NAME = 'Youou-ASO 客服'
const ZH_DEFAULT_CONTACT_HINT = '请扫码联系客服完成充值，到账后余额将更新。'
const rechargeDialogVisible = ref(false)
const customerServiceQrUrl = ref('')
const customerServiceName = ref('')
const customerServiceHint = ref('')

const currentLocale = computed(() => locale.value as AppLocale)
const walletBalance = computed(() => walletStore.balance)
const walletLoading = computed(() => walletStore.loading)
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
  const editingOrder = route.name === 'user-order-create' && Number.isFinite(Number(route.query.orderId))
  const current = editingOrder ? t('orderCreate.editTitle') : typeof titleKey === 'string' ? t(titleKey) : t('menu.home')
  if (typeof topTitleKey !== 'string') return { parent: '', current }

  const parent = t(topTitleKey)
  return parent === current ? { parent: '', current } : { parent, current }
})
const displayName = computed(() => {
  if (auth.username) return auth.username
  if (auth.email) return auth.email
  if (auth.accountId) return t('account.userFallback', { id: auth.accountId })
  return t('account.unknown')
})
const accountSubline = computed(() => {
  if (auth.email) return auth.email
  if (auth.accountId) return `${t('wallet.customerId')}: ${auth.accountId}`
  return ''
})
const accountInitial = computed(() => {
  if (auth.username || auth.email) return displayName.value.trim().slice(0, 1).toUpperCase() || 'Y'
  if (auth.accountId) return String(auth.accountId).slice(-1)
  return 'Y'
})

onMounted(() => {
  void loadWallet()
})

function setLocale(nextLocale: AppLocale | string | number | object) {
  if (nextLocale !== 'zh-CN' && nextLocale !== 'en-US') return
  locale.value = nextLocale
  window.localStorage.setItem(LOCALE_STORAGE_KEY, nextLocale)
  document.documentElement.lang = nextLocale === 'zh-CN' ? 'zh-CN' : 'en'
}

async function loadWallet() {
  try {
    await walletStore.loadCustomerWallet()
  } catch {
    walletStore.setOverview(null)
  }
}

async function loadCustomerServiceConfig() {
  try {
    const config = await getCustomerServiceConfig()
    customerServiceName.value = localizeServiceName(config.serviceName)
    customerServiceHint.value = localizeContactHint(config.contactHint)
    customerServiceQrUrl.value = config.enabled && config.qrCodeUrl ? config.qrCodeUrl : ''
  } catch {
    customerServiceName.value = ''
    customerServiceHint.value = ''
    customerServiceQrUrl.value = ''
  }
}

function goRecharge() {
  void loadCustomerServiceConfig()
  rechargeDialogVisible.value = true
}

function localizeServiceName(value: string | null | undefined) {
  if (!value || value === LEGACY_DEFAULT_SERVICE_NAME || value === ZH_DEFAULT_SERVICE_NAME) {
    return t('supportConfig.defaultServiceName')
  }
  return value
}

function localizeContactHint(value: string | null | undefined) {
  if (!value || value === LEGACY_DEFAULT_CONTACT_HINT || value === ZH_DEFAULT_CONTACT_HINT) {
    return t('supportConfig.defaultContactHint')
  }
  return value
}

function formatMoney(value: string | number | undefined | null) {
  if (value === undefined || value === null || value === '') return '$--'
  return `$${Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })}`
}

async function handleAccountCommand(command: string | number | object) {
  if (command === 'settings') {
    await router.push('/user/settings')
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
  gap: 14px;
}

.wallet-pill {
  height: 40px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.wallet-label {
  color: #344054;
  font-size: 14px;
}

.wallet-pill strong {
  color: #ff9300;
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
}

.wallet-refresh {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 0 4px;
  border: 0;
  background: transparent;
  color: #2f7df4;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 700;
}

.wallet-refresh:disabled {
  cursor: default;
  opacity: 0.55;
}

.wallet-recharge {
  height: 34px;
  min-width: 78px;
  border: 0;
  border-radius: 999px;
  background: #ff9700;
  box-shadow: 0 8px 18px rgb(255 151 0 / 24%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
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

.recharge-qr-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 4px 0 8px;
  text-align: center;
}

.qr-image-wrap,
.qr-placeholder {
  width: 220px;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #ffffff;
}

.qr-image-wrap img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: contain;
}

.qr-placeholder {
  background:
    linear-gradient(90deg, rgb(24 34 48 / 8%) 1px, transparent 1px),
    linear-gradient(rgb(24 34 48 / 8%) 1px, transparent 1px),
    #f8fafc;
  background-size: 18px 18px;
  color: #667085;
  font-size: 13px;
  font-weight: 700;
}

.recharge-qr-panel p {
  margin: 0;
  color: #475467;
  font-size: 14px;
  line-height: 1.7;
}

.customer-service-name {
  color: #182230;
  font-size: 15px;
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

  .wallet-pill {
    order: -1;
    width: 100%;
    justify-content: flex-end;
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

