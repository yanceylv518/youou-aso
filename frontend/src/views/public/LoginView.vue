<template>
  <section class="auth-page">
    <el-form class="auth-panel" :model="form" label-position="top" @submit.prevent="submit">
      <div class="auth-brand">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ t('app.name') }}</span>
      </div>
      <header class="auth-header">
        <h1>{{ t('auth.loginTitle') }}</h1>
        <p>{{ t('auth.loginSubtitle') }}</p>
      </header>
      <el-form-item :label="t('auth.account')">
        <el-input v-model="form.account" autocomplete="username" size="large" />
      </el-form-item>
      <el-form-item :label="t('auth.password')">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password size="large" />
      </el-form-item>
      <div class="form-meta">
        <el-checkbox v-model="rememberAccount">{{ t('auth.rememberAccount') }}</el-checkbox>
        <router-link to="/forgot-password">{{ t('auth.forgotPassword') }}</router-link>
      </div>
      <el-button type="primary" native-type="submit" class="full-width" size="large" :loading="loading">
        {{ t('auth.loginButton') }}
      </el-button>
      <p class="auth-switch">
        <span>{{ t('auth.noAccount') }}</span>
        <router-link to="/register">{{ t('auth.createNow') }}</router-link>
      </p>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { readBrowserStorage, removeBrowserStorage, writeBrowserStorage } from '@/utils/browserStorage'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const rememberAccount = ref(false)
const REMEMBER_LOGIN_KEY = 'youou_aso_remember_login'

const form = reactive({
  account: '',
  password: ''
})

onMounted(() => {
  try {
    const stored = readBrowserStorage(REMEMBER_LOGIN_KEY)
    if (!stored) return
    const session = JSON.parse(stored) as { account?: string; rememberAccount?: boolean; rememberPassword?: boolean }
    rememberAccount.value = Boolean(session.rememberAccount ?? session.rememberPassword)
    form.account = session.account || ''
  } catch {
    removeBrowserStorage(REMEMBER_LOGIN_KEY)
  }
})

function getErrorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message || t('auth.loginFailed')
  }
  return t('auth.loginFailed')
}

async function submit() {
  if (!form.account.trim() || !form.password) {
    ElMessage.warning(t('auth.requiredFields'))
    return
  }
  loading.value = true
  try {
    const result = await auth.login({
      account: form.account.trim(),
      password: form.password
    })
    persistRememberPreference()
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (isProtectedRedirect(redirect)) {
      await router.replace(redirect)
      return
    }
    await router.replace(result.accountType === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

function persistRememberPreference() {
  if (!rememberAccount.value) {
    removeBrowserStorage(REMEMBER_LOGIN_KEY)
    return
  }
  writeBrowserStorage(REMEMBER_LOGIN_KEY, JSON.stringify({
    account: form.account.trim(),
    rememberAccount: true
  }))
}

function isProtectedRedirect(redirect: string) {
  return redirect.startsWith('/user/') || redirect.startsWith('/admin/')
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 20px;
  background-color: #f6f9ff;
  background-image:
    linear-gradient(135deg, rgb(246 249 255 / 80%), rgb(239 247 244 / 86%)),
    url("../../assets/public-home-bg.svg");
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.auth-panel {
  width: min(420px, 100%);
  padding: 34px;
  border: 1px solid rgb(208 218 230 / 76%);
  border-radius: 8px;
  background: linear-gradient(180deg, rgb(255 255 255 / 94%), rgb(250 253 255 / 88%));
  box-shadow: 0 24px 54px rgb(16 24 40 / 10%);
  backdrop-filter: blur(10px);
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #0f172a;
  font-weight: 800;
}

.brand-mark {
  width: 36px;
  height: 36px;
  display: block;
  flex: 0 0 auto;
  border-radius: 50%;
  object-fit: cover;
}

.auth-header {
  margin: 26px 0 24px;
}

h1,
p {
  letter-spacing: 0;
}

h1 {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.2;
}

.auth-header p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.65;
}

:deep(.el-form-item__label) {
  color: #334155;
  font-weight: 700;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1d4ed8 inset, 0 0 0 3px rgb(27 117 208 / 12%);
}

.form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: -4px 0 18px;
}

.form-meta :deep(.el-checkbox) {
  height: auto;
  color: #475569;
  font-weight: 700;
}

.form-meta a,
.auth-switch a {
  color: #1d4ed8;
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 360px) {
  .form-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}

.full-width {
  width: 100%;
  border-radius: 8px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgb(27 117 208 / 22%);
}

.auth-switch {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin: 20px 0 0;
  color: #64748b;
}

@media (max-width: 520px) {
  .auth-page {
    align-items: flex-start;
    padding: 24px 16px;
  }

  .auth-panel {
    padding: 26px;
  }
}
</style>

