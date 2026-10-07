<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar" :class="{ 'is-mobile-open': mobileMenuOpen }">
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
      <section
        class="sidebar-support"
      >
        <span class="sidebar-support-heading">
          <span class="sidebar-support-icon">
            <el-icon><Service /></el-icon>
            <span class="sidebar-support-status" aria-hidden="true"></span>
          </span>
        </span>
        <strong class="sidebar-support-title">{{ t('wallet.customerService') }}</strong>
        <span class="sidebar-support-name">{{ customerServiceName || t('supportConfig.defaultServiceName') }}</span>
        <span v-if="!customerServiceChannels.length" class="sidebar-support-hint">{{ t('wallet.customerServiceEntryHint') }}</span>
        <div v-if="customerServiceChannels.length" class="sidebar-support-channels">
          <button
            v-for="channel in customerServiceChannels"
            :key="channel.type"
            type="button"
            :class="[`channel-${channel.type}`, { active: selectedCustomerServiceChannel === channel.type }]"
            :title="channel.name"
            :aria-label="channel.name"
            @click="selectedCustomerServiceChannel = channel.type"
          >
            <el-icon v-if="channel.type === 'email'"><Message /></el-icon>
            <el-icon v-else-if="channel.type === 'phone'"><Phone /></el-icon>
            <el-icon v-else-if="channel.type === 'telegram'"><Promotion /></el-icon>
            <el-icon v-else><ChatDotRound /></el-icon>
          </button>
        </div>
        <div v-if="activeCustomerServiceChannel" class="sidebar-support-contact">
          <span>{{ activeCustomerServiceChannel.name }}</span>
          <img v-if="activeCustomerServiceChannel.kind === 'qr'" :src="activeCustomerServiceChannel.value" :alt="activeCustomerServiceChannel.name" />
          <strong v-else :title="activeCustomerServiceChannel.value">{{ activeCustomerServiceChannel.displayValue }}</strong>
        </div>
      </section>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">
          <button class="mobile-menu-trigger" type="button" @click="mobileMenuOpen = !mobileMenuOpen">
            <el-icon><Menu /></el-icon>
          </button>
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
                <el-dropdown-item v-for="option in localeOptions" :key="option.code" :command="option.code">
                  <el-icon class="language-check" :class="{ visible: currentLocale === option.code }"><Check /></el-icon>
                  <span>{{ option.nativeLabel }}</span>
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

  <el-dialog append-to-body v-model="rechargeDialogVisible" :title="t('wallet.recharge')" width="360px" class="recharge-dialog">
    <div class="recharge-qr-panel">
      <div v-if="customerServiceQrUrl" class="qr-image-wrap">
        <img :src="customerServiceQrUrl" :alt="t('wallet.customerServiceQr')" />
      </div>
      <div v-else-if="!customerServiceChannels.length" class="qr-placeholder">
        <span>{{ t('wallet.qrNotConfigured') }}</span>
      </div>
      <strong v-if="customerServiceName" class="customer-service-name">{{ customerServiceName }}</strong>
      <p>{{ customerServiceQrUrl ? (customerServiceHint || t('wallet.scanCustomerServiceQr')) : t('wallet.customerServiceEntryHint') }}</p>
      <div v-if="customerServiceChannels.length" class="dialog-support-tab-panel">
        <div class="dialog-support-tabs" role="tablist">
          <button
            v-for="channel in customerServiceChannels"
            :key="channel.type"
            type="button"
            role="tab"
            :class="{ active: selectedCustomerServiceChannel === channel.type }"
            :aria-selected="selectedCustomerServiceChannel === channel.type"
            :title="channel.name"
            @click="selectedCustomerServiceChannel = channel.type"
          >
            <el-icon v-if="channel.type === 'email'"><Message /></el-icon>
            <el-icon v-else-if="channel.type === 'phone'"><Phone /></el-icon>
            <el-icon v-else-if="channel.type === 'telegram'"><Promotion /></el-icon>
            <el-icon v-else><ChatDotRound /></el-icon>
            <span>{{ channel.name }}</span>
          </button>
        </div>
        <div v-if="activeCustomerServiceChannel" class="dialog-support-tab-content">
          <span class="dialog-support-channel-name">{{ activeCustomerServiceChannel.name }}</span>
          <img
            v-if="activeCustomerServiceChannel.kind === 'qr'"
            :src="activeCustomerServiceChannel.value"
            :alt="activeCustomerServiceChannel.name"
          />
          <strong v-else>{{ activeCustomerServiceChannel.displayValue }}</strong>
        </div>
      </div>
      <div v-if="customerServiceChannels.length" class="dialog-support-channels">
        <a
          v-for="channel in customerServiceChannels"
          :key="channel.type"
          :href="channel.href"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span>{{ channel.badge }}</span>
          {{ channel.name }} · {{ channel.displayValue }}
        </a>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { formatCurrency } from '@/utils/presentation'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { useRoute, useRouter } from 'vue-router'
import {
  Apple,
  ArrowDown,
  ChatDotRound,
  Check,
  ChromeFilled,
  Connection,
  Grid,
  HomeFilled,
  Money,
  Monitor,
  Menu,
  Message,
  Phone,
  Promotion,
  Refresh,
  Service,
  Setting,
  SwitchButton,
  Tickets,
  Wallet
} from '@element-plus/icons-vue'
import { getCustomerServiceConfig } from '@/api/support'
import { LOCALE_STORAGE_KEY, localeHtmlLang, localeOptions, supportedLocales, type AppLocale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'
import { useWalletStore } from '@/stores/wallet'
import { writeBrowserStorage } from '@/utils/browserStorage'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const walletStore = useWalletStore()
const { locale, t } = useI18n()
const mobileMenuOpen = ref(false)
const LEGACY_DEFAULT_SERVICE_NAME = 'Youou-ASO Support'
const LEGACY_DEFAULT_CONTACT_HINT = 'Scan the QR code to contact customer service for recharge.'
const ZH_DEFAULT_SERVICE_NAME = 'Youou-ASO 客服'
const ZH_DEFAULT_CONTACT_HINT = '请扫码联系客服完成充值，到账后余额将更新。'
const rechargeDialogVisible = ref(false)
const customerServiceQrUrl = ref('')
const customerServiceName = ref('')
const customerServiceHint = ref('')
type CustomerServiceChannelType = 'email' | 'phone' | 'telegram' | 'wechat' | 'teams' | 'whatsapp'
interface CustomerServiceChannel {
  type: CustomerServiceChannelType
  name: string
  value: string
  displayValue: string
  href: string
  badge: string
  kind: 'text' | 'qr'
}
const customerServiceChannels = ref<CustomerServiceChannel[]>([])
const selectedCustomerServiceChannel = ref<CustomerServiceChannelType | null>(null)

const currentLocale = computed(() => locale.value as AppLocale)
const walletBalance = computed(() => walletStore.balance)
const walletLoading = computed(() => walletStore.loading)
const activeCustomerServiceChannel = computed(() => (
  customerServiceChannels.value.find(({ type }) => type === selectedCustomerServiceChannel.value)
  || customerServiceChannels.value[0]
  || null
))
const currentLanguageLabel = computed(() => {
  return localeOptions.find(({ code }) => code === currentLocale.value)?.nativeLabel || currentLocale.value
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
  void loadCustomerServiceConfig()
  window.addEventListener('focus', refreshCustomerServiceConfig)
})

onBeforeUnmount(() => {
  window.removeEventListener('focus', refreshCustomerServiceConfig)
})


watch(() => route.fullPath, () => {
  mobileMenuOpen.value = false
  void loadCustomerServiceConfig()
})
watch(locale, () => { void loadCustomerServiceConfig() })

function setLocale(nextLocale: AppLocale | string | number | object) {
  if (!supportedLocales.includes(nextLocale as AppLocale)) return
  const normalized = nextLocale as AppLocale
  locale.value = normalized
  writeBrowserStorage(LOCALE_STORAGE_KEY, normalized)
  document.documentElement.lang = localeHtmlLang[normalized]
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
    customerServiceQrUrl.value = config.qrCodeUrl || ''
    customerServiceChannels.value = buildVisibleCustomerServiceChannels(config)
    if (!customerServiceChannels.value.some(({ type }) => type === selectedCustomerServiceChannel.value)) {
      selectedCustomerServiceChannel.value = customerServiceChannels.value[0]?.type || null
    }
  } catch {
    customerServiceName.value = ''
    customerServiceHint.value = ''
    customerServiceQrUrl.value = ''
    customerServiceChannels.value = []
    selectedCustomerServiceChannel.value = null
  }
}

function refreshCustomerServiceConfig() {
  void loadCustomerServiceConfig()
}

function buildVisibleCustomerServiceChannels(config: {
  email: string | null
  emailVisible: boolean
  phone: string | null
  phoneVisible: boolean
  telegramQrUrl: string | null
  telegramQrVisible: boolean
  wechatQrUrl: string | null
  wechatQrVisible: boolean
}): CustomerServiceChannel[] {
  return [
    config.emailVisible && config.email ? createVisibleCustomerServiceChannel('email', t('supportConfig.email'), config.email, '@', 'text') : null,
    config.phoneVisible && config.phone ? createVisibleCustomerServiceChannel('phone', t('supportConfig.phone'), config.phone, 'P', 'text') : null,
    config.telegramQrVisible && config.telegramQrUrl ? createVisibleCustomerServiceChannel('telegram', 'Telegram', config.telegramQrUrl, 'T', 'qr') : null,
    config.wechatQrVisible && config.wechatQrUrl ? createVisibleCustomerServiceChannel('wechat', t('supportConfig.wechatQr'), config.wechatQrUrl, 'W', 'qr') : null
  ].filter((channel): channel is CustomerServiceChannel => channel !== null)
}

function createVisibleCustomerServiceChannel(
  type: CustomerServiceChannelType,
  name: string,
  value: string,
  badge: string,
  kind: 'text' | 'qr'
): CustomerServiceChannel {
  return { type, name, value, displayValue: value, href: '', badge, kind }
}

function buildCustomerServiceChannels(config: {
  email: string | null
  teamsUrl: string | null
  telegramUrl: string | null
  whatsappUrl: string | null
}): CustomerServiceChannel[] {
  return [
    config.email ? createCustomerServiceChannel('email', t('supportConfig.email'), config.email, `mailto:${config.email}`, '@') : null,
    config.teamsUrl ? createCustomerServiceChannel('teams', 'Microsoft Teams', config.teamsUrl, config.teamsUrl, 'T') : null,
    config.telegramUrl ? createCustomerServiceChannel('telegram', 'Telegram', config.telegramUrl, config.telegramUrl, '➤') : null,
    config.whatsappUrl ? createCustomerServiceChannel('whatsapp', 'WhatsApp', config.whatsappUrl, config.whatsappUrl, 'W') : null
  ].filter((channel): channel is CustomerServiceChannel => channel !== null)
}

function createCustomerServiceChannel(
  type: CustomerServiceChannelType,
  name: string,
  value: string,
  href: string,
  badge: string
): CustomerServiceChannel {
  return {
    type,
    name,
    value,
    href,
    badge,
    kind: 'text',
    displayValue: type === 'email' ? value : value.replace(/^https?:\/\//i, '').replace(/\/$/, '')
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

const formatMoney = formatCurrency

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
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
}

.shell-brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  font-weight: 700;
  color: #0f172a;
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
  height: auto;
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  border-right: 0;
}

.sidebar-support {
  width: calc(100% - 24px);
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  flex-direction: column;
  gap: 5px;
  margin: 10px 12px 14px;
  padding: 13px 10px 11px;
  overflow: visible;
  border: 1px solid rgb(96 165 250 / 24%);
  border-radius: 16px;
  background:
    radial-gradient(circle at 50% 0%, rgb(59 130 246 / 17%), transparent 42%),
    linear-gradient(180deg, #172a47 0%, #102039 100%);
  box-shadow: 0 14px 30px rgb(2 8 23 / 32%);
  color: #f8fafc;
  text-align: center;
}

.sidebar-support-heading {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  gap: 9px;
}

.sidebar-support-icon {
  position: relative;
  width: 46px;
  height: 46px;
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 3px solid #29466e;
  border-radius: 50%;
  background: linear-gradient(145deg, #233f67, #1a3153);
  box-shadow: 0 7px 18px rgb(2 8 23 / 30%);
  color: #7db3ff;
  font-size: 23px;
}

.sidebar-support-status {
  position: absolute;
  right: 0;
  bottom: 1px;
  width: 10px;
  height: 10px;
  border: 2px solid #172a47;
  border-radius: 50%;
  background: #22c55e;
}

.sidebar-support-title {
  margin-top: 1px;
  color: #f8fafc;
  font-size: 15px;
  font-weight: 900;
  line-height: 1.3;
}

.sidebar-support-name {
  max-width: 100%;
  overflow: hidden;
  color: #9fb0c8;
  font-size: 10px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-support-hint {
  display: -webkit-box;
  overflow: hidden;
  color: #9fb0c8;
  font-size: 11px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.sidebar-support-channels {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(32px, 1fr));
  gap: 4px;
  margin-top: 4px;
  padding: 4px;
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 11px;
  background: rgb(5 14 29 / 35%);
}

.sidebar-support-channels button {
  height: 32px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #9fb0c8;
  cursor: pointer;
  font-family: inherit;
  font-size: 15px;
  transition: background 150ms ease, color 150ms ease, transform 150ms ease;
}

.sidebar-support-channels button:hover,
.sidebar-support-channels button.active {
  background: #2563c7;
  box-shadow: 0 5px 12px rgb(15 70 160 / 32%);
  color: #ffffff;
  transform: translateY(-1px);
}

.sidebar-support-channels .channel-email {
  color: #60a5fa;
}

.sidebar-support-channels .channel-phone {
  color: #a5b4fc;
}

.sidebar-support-channels .channel-telegram {
  color: #67c7ef;
}

.sidebar-support-channels .channel-wechat {
  color: #4ade80;
}

.sidebar-support-channels button.active {
  color: #ffffff;
}

.channel-letter {
  font-size: 14px;
  font-weight: 900;
}

.sidebar-support-contact {
  width: 100%;
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 8px 9px;
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 9px;
  background: rgb(5 14 29 / 42%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 3%);
  text-align: center;
}

.sidebar-support-contact span {
  color: #7f96b5;
  font-size: 10px;
  font-weight: 700;
}

.sidebar-support-contact strong {
  max-width: 100%;
  overflow: hidden;
  color: #dce8f8;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-support-contact img {
  width: 92px;
  height: 92px;
  margin-top: 5px;
  border: 5px solid #ffffff;
  border-radius: 8px;
  background: #ffffff;
  object-fit: contain;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  color: #475569;
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
  color: #0f172a;
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
  color: #64748b;
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
  color: #0f172a;
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
  color: #334155;
  font-size: 14px;
}

.wallet-pill strong {
  color: #b45309;
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
  color: #2563eb;
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
  background: #b45309;
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
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
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
  color: #1d4ed8;
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
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
  cursor: pointer;
}

.account-avatar {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fbbf24;
  color: #92400e;
  font-weight: 800;
}

.account-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}

.account-name {
  color: #334155;
  font-weight: 700;
}

.account-summary {
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  color: #0f172a;
}

.account-summary span {
  color: #64748b;
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
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.recharge-qr-panel p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.customer-service-name {
  color: #0f172a;
  font-size: 15px;
}

.dialog-support-channels {
  display: none;
}

.dialog-support-tab-panel {
  width: 100%;
  overflow: hidden;
  border: 1px solid #dbe4f0;
  border-radius: 12px;
  background: #f8fafc;
}

.dialog-support-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(74px, 1fr));
  gap: 4px;
  padding: 5px;
  border-bottom: 1px solid #e2e8f0;
  background: #f1f5f9;
}

.dialog-support-tabs button {
  min-width: 0;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  padding: 6px 5px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font: inherit;
}

.dialog-support-tabs button:hover {
  color: #2563eb;
}

.dialog-support-tabs button.active {
  border-color: #d4e3fb;
  background: #ffffff;
  box-shadow: 0 3px 10px rgb(15 23 42 / 7%);
  color: #2563eb;
}

.dialog-support-tabs button .el-icon {
  font-size: 18px;
}

.dialog-support-tabs button span {
  max-width: 100%;
  overflow: hidden;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dialog-support-tab-content {
  min-height: 104px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  background: #ffffff;
}

.dialog-support-channel-name {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.dialog-support-tab-content strong {
  max-width: 100%;
  overflow-wrap: anywhere;
  color: #0f172a;
  font-size: 15px;
}

.dialog-support-tab-content img {
  width: 180px;
  height: 180px;
  padding: 7px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  object-fit: contain;
}

.dialog-support-channels a {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 11px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  background: #f8fafc;
  color: #334155;
  font-size: 13px;
  text-decoration: none;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dialog-support-channels a:hover {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
}

.dialog-support-channels a span {
  width: 26px;
  height: 26px;
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  background: #dbeafe;
  color: #2563eb;
  font-weight: 900;
}


.mobile-menu-trigger { display: none; width: 38px; height: 38px; align-items: center; justify-content: center; border: 1px solid #e2e8f0; border-radius: 9px; background: #fff; color: #334155; cursor: pointer; }

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

@media(max-width:700px) {
 .topbar { display:grid; grid-template-columns:minmax(0,1fr); gap:0; padding:8px 12px; }
 .topbar-actions { flex-wrap:nowrap; gap:6px; }
 .wallet-pill { width:auto; order:0; flex:1; padding:0; justify-content:flex-start; gap:6px; }
 .wallet-refresh, .wallet-label { display:none; }
 .language-trigger { min-width:40px; padding:4px 6px; }
 .account-trigger { padding:4px 6px; }
 .wallet-recharge { min-width:54px; height:30px; min-height:30px; padding:4px 10px; }
 .account-avatar { width:24px; height:24px; }
}

</style>

