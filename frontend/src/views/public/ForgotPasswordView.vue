<template>
  <section class="auth-page">
    <el-form class="auth-panel" :model="form" label-position="top" @submit.prevent="submit">
      <div class="auth-brand">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ t('app.name') }}</span>
      </div>
      <header class="auth-header">
        <h1>{{ t('auth.forgotTitle') }}</h1>
        <p>{{ t('auth.forgotSubtitle') }}</p>
      </header>
      <el-form-item :label="t('auth.email')">
        <el-input v-model="form.email" autocomplete="email" size="large" />
      </el-form-item>
      <el-form-item :label="t('auth.resetCode')">
        <div class="code-row">
          <el-input v-model="form.code" maxlength="6" inputmode="numeric" size="large" :placeholder="t('auth.resetCodePlaceholder')" />
          <el-button native-type="button" size="large" :loading="sendingCode" :disabled="countdown > 0" @click="sendCode">
            {{ countdown > 0 ? t('auth.resendCodeCountdown', { seconds: countdown }) : t('auth.sendResetCode') }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item :label="t('auth.newPassword')">
        <el-input v-model="form.newPassword" type="password" autocomplete="new-password" show-password size="large" />
      </el-form-item>
      <el-form-item :label="t('auth.confirmNewPassword')">
        <el-input v-model="form.confirmPassword" type="password" autocomplete="new-password" show-password size="large" />
      </el-form-item>
      <el-button type="primary" native-type="submit" class="full-width" size="large" :loading="submitting">
        {{ t('auth.resetPasswordButton') }}
      </el-button>
      <p class="auth-switch">
        <router-link to="/login">{{ t('auth.backToLogin') }}</router-link>
      </p>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { confirmPasswordReset, requestPasswordResetCode } from '@/api/auth'

const { t } = useI18n()
const router = useRouter()
const sendingCode = ref(false)
const submitting = ref(false)
const countdown = ref(0)
let countdownTimer: number | undefined

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

onBeforeUnmount(() => {
  stopCountdown()
})

async function sendCode() {
  const email = normalizedEmail()
  if (!email) {
    ElMessage.warning(t('auth.invalidEmail'))
    return
  }
  sendingCode.value = true
  try {
    await requestPasswordResetCode({ email })
    ElMessage.success(t('auth.resetCodeSent'))
    startCountdown()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('auth.resetCodeSendFailed')))
  } finally {
    sendingCode.value = false
  }
}

async function submit() {
  const email = normalizedEmail()
  if (!email || !form.code.trim() || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning(t('auth.requiredFields'))
    return
  }
  if (!/^\d{6}$/.test(form.code.trim())) {
    ElMessage.warning(t('auth.invalidResetCode'))
    return
  }
  if (form.newPassword.length < 8) {
    ElMessage.warning(t('auth.passwordTooShort'))
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning(t('auth.passwordMismatch'))
    return
  }
  submitting.value = true
  try {
    await confirmPasswordReset({
      email,
      code: form.code.trim(),
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    ElMessage.success(t('auth.passwordResetSuccess'))
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(errorMessage(error, t('auth.passwordResetFailed')))
  } finally {
    submitting.value = false
  }
}

function normalizedEmail() {
  const email = form.email.trim().toLowerCase()
  return emailPattern.test(email) ? email : ''
}

function startCountdown() {
  countdown.value = 60
  stopCountdown()
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      stopCountdown()
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer !== undefined) {
    window.clearInterval(countdownTimer)
    countdownTimer = undefined
  }
}

function errorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message || fallback
  }
  return fallback
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
  color: #182230;
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
  color: #182230;
  font-size: 28px;
  line-height: 1.2;
}

.auth-header p {
  margin: 8px 0 0;
  color: #667085;
  line-height: 1.65;
}

:deep(.el-form-item__label) {
  color: #344054;
  font-weight: 700;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d8dee8 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1b75d0 inset, 0 0 0 3px rgb(27 117 208 / 12%);
}

.full-width {
  width: 100%;
  margin-top: 4px;
  border-radius: 8px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgb(27 117 208 / 22%);
}

.code-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.code-row .el-button {
  min-width: 128px;
}

.auth-switch {
  display: flex;
  justify-content: center;
  margin: 20px 0 0;
}

.auth-switch a {
  color: #1b75d0;
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 520px) {
  .auth-page {
    align-items: flex-start;
    padding: 24px 16px;
  }

  .auth-panel {
    padding: 26px;
  }

  .code-row {
    grid-template-columns: 1fr;
  }
}
</style>

