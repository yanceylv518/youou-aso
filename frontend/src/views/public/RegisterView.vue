<template>
  <section class="auth-page">
    <el-form ref="formRef" :rules="rules" scroll-to-error class="auth-panel" :model="form" label-position="top" @submit.prevent="submit">
      <div class="auth-brand">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ t('app.name') }}</span>
      </div>
      <header class="auth-header">
        <h1>{{ t('auth.registerTitle') }}</h1>
        <p>{{ t('auth.registerSubtitle') }}</p>
      </header>
      <el-form-item prop="username" :label="t('auth.username')">
        <el-input v-model="form.username" autocomplete="username" size="large" />
      </el-form-item>
      <el-form-item prop="email" :label="t('auth.email')">
        <el-input v-model="form.email" autocomplete="email" size="large" />
      </el-form-item>
      <el-form-item prop="password" :label="t('auth.password')">
        <el-input v-model="form.password" type="password" autocomplete="new-password" show-password size="large" />
      <small class="password-hint">{{ t('auth.passwordTooShort') }}</small>
      </el-form-item>
      <el-button type="primary" native-type="submit" class="full-width" size="large" :loading="loading">
        {{ t('auth.registerButton') }}
      </el-button>
      <p class="auth-switch">
        <span>{{ t('auth.hasAccount') }}</span>
        <router-link to="/login">{{ t('auth.loginNow') }}</router-link>
      </p>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: ''
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function getErrorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message || t('auth.registerFailed')
  }
  return t('auth.registerFailed')
}


const formRef = ref<FormInstance>()
const rules = computed<FormRules>(() => ({
  username: [{ required:true, whitespace:true, message:t('visual.required'), trigger:'blur' }],
  email: [{ required:true, whitespace:true, message:t('visual.required'), trigger:'blur' }, { type:'email', message:t('auth.invalidEmail'), trigger:'blur' }],
  password: [{ required:true, whitespace:true, message:t('visual.required'), trigger:'blur' }, { min:8, message:t('auth.passwordTooShort'), trigger:'blur' }],
}))
async function validateForm() {
 const valid = await formRef.value?.validate().catch(() => false)
 if(!valid){await nextTick();formRef.value?.$el.querySelector('.is-error input')?.focus()}
 return valid
}

async function submit() {
  if (!await validateForm()) return
  const username = form.username.trim()
  const email = form.email.trim()
  if (!username || !email || !form.password) {
    ElMessage.warning(t('auth.requiredFields'))
    return
  }
  if (!emailPattern.test(email)) {
    ElMessage.warning(t('auth.invalidEmail'))
    return
  }
  if (form.password.length < 8) {
    ElMessage.warning(t('auth.passwordTooShort'))
    return
  }
  loading.value = true
  try {
    await auth.register({
      username,
      email,
      password: form.password
    })
    ElMessage.success(t('auth.registerSuccess'))
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
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
  width: min(430px, 100%);
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

.full-width {
  width: 100%;
  margin-top: 4px;
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

.auth-switch a {
  color: #1d4ed8;
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
}
.password-hint { display:block; color:#64748b; margin-top:6px; line-height:1.5; }
</style>

