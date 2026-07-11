<template>
  <section class="settings-page">
    <div class="settings-grid">
      <section class="panel profile-panel">
        <div class="avatar">{{ accountInitial }}</div>
        <div>
          <h2>{{ displayName }}</h2>
          <p>{{ auth.email || '-' }}</p>
        </div>
        <el-tag :type="auth.isSuperAdmin ? 'danger' : 'warning'" effect="light">
          {{ auth.isSuperAdmin ? t('accountSettings.superAdminRole') : t('accountSettings.adminRole') }}
        </el-tag>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('accountSettings.basicInfo') }}</h2>
          <el-icon><User /></el-icon>
        </div>
        <dl class="info-list">
          <div>
            <dt>{{ t('accountSettings.accountId') }}</dt>
            <dd>{{ auth.accountId || '-' }}</dd>
          </div>
          <div>
            <dt>{{ t('auth.username') }}</dt>
            <dd>{{ auth.username || '-' }}</dd>
          </div>
          <div>
            <dt>{{ t('auth.email') }}</dt>
            <dd>{{ auth.email || '-' }}</dd>
          </div>
          <div>
            <dt>{{ t('accountSettings.role') }}</dt>
            <dd>{{ auth.roleCode || '-' }}</dd>
          </div>
        </dl>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('accountSettings.preferences') }}</h2>
          <el-icon><Setting /></el-icon>
        </div>
        <div class="language-row">
          <span>{{ t('common.language') }}</span>
          <el-radio-group v-model="currentLocale" @change="setLocale">
            <el-radio-button label="zh-CN">{{ t('common.chinese') }}</el-radio-button>
            <el-radio-button label="en-US">{{ t('common.english') }}</el-radio-button>
          </el-radio-group>
        </div>
        <p class="hint">{{ t('accountSettings.localeHint') }}</p>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>{{ t('accountSettings.security') }}</h2>
          <el-icon><Lock /></el-icon>
        </div>
        <div class="security-actions">
          <el-button type="primary" :icon="Lock" @click="openPasswordDialog">
            {{ t('accountSettings.changePassword') }}
          </el-button>
          <el-button :icon="SwitchButton" @click="logout">
            {{ t('auth.logout') }}
          </el-button>
        </div>
      </section>
    </div>

    <el-dialog
      v-model="passwordDialogVisible"
      :title="t('accountSettings.changePassword')"
      width="420px"
      :close-on-click-modal="!passwordSubmitting"
      @closed="resetPasswordForm"
    >
      <el-form class="password-form" label-position="top" @submit.prevent>
        <el-form-item :label="t('accountSettings.currentPassword')">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            maxlength="72"
            :placeholder="t('accountSettings.currentPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('accountSettings.newPassword')">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            maxlength="72"
            :placeholder="t('accountSettings.newPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('accountSettings.confirmNewPassword')">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            maxlength="72"
            :placeholder="t('accountSettings.confirmNewPasswordPlaceholder')"
            @keyup.enter="submitPasswordChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="passwordSubmitting" @click="passwordDialogVisible = false">
          {{ t('accountSettings.cancel') }}
        </el-button>
        <el-button type="primary" :loading="passwordSubmitting" @click="submitPasswordChange">
          {{ t('accountSettings.savePassword') }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'
import { LOCALE_STORAGE_KEY, type AppLocale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'

const { locale, t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const displayName = computed(() => auth.username || auth.email || t('account.unknown'))
const accountInitial = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'Y')
const currentLocale = computed({
  get: () => locale.value as AppLocale,
  set: (value: AppLocale) => setLocale(value)
})
function setLocale(nextLocale: AppLocale | string | number | boolean | undefined) {
  const normalized = nextLocale === 'en-US' ? 'en-US' : 'zh-CN'
  locale.value = normalized
  auth.locale = normalized
  auth.persist()
  window.localStorage.setItem(LOCALE_STORAGE_KEY, normalized)
  document.documentElement.lang = normalized === 'zh-CN' ? 'zh-CN' : 'en'
}

function openPasswordDialog() {
  passwordDialogVisible.value = true
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

async function submitPasswordChange() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning(t('accountSettings.passwordMismatch'))
    return
  }
  if (passwordForm.oldPassword && passwordForm.oldPassword === passwordForm.newPassword) {
    ElMessage.warning(t('accountSettings.passwordSameAsOld'))
    return
  }
  passwordSubmitting.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    auth.forcePasswordChange = false
    auth.persist()
    passwordDialogVisible.value = false
    ElMessage.success(t('accountSettings.passwordChanged'))
  } catch (error) {
    ElMessage.error(errorMessage(error, t('accountSettings.passwordChangeFailed')))
  } finally {
    passwordSubmitting.value = false
  }
}

async function logout() {
  auth.logout()
  await router.replace('/login')
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
.settings-page {
  color: #182230;
}

h2,
p {
  letter-spacing: 0;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  min-width: 0;
  padding: 18px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.profile-panel {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 56px;
  height: 56px;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: #e7ddff;
  color: #7657d8;
  font-size: 22px;
  font-weight: 800;
}

.profile-panel h2,
.panel h2 {
  margin: 0;
  font-size: 18px;
}

.profile-panel p {
  margin: 5px 0 0;
  color: #667085;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header .el-icon {
  color: #98a2b3;
  font-size: 22px;
}

.info-list {
  display: grid;
  gap: 12px;
  margin: 0;
}

.info-list div {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f6;
}

.info-list dt {
  color: #667085;
}

.info-list dd {
  margin: 0;
  color: #182230;
  font-weight: 700;
  text-align: right;
  overflow-wrap: anywhere;
}

.language-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.hint {
  margin: 12px 0 0;
  color: #667085;
  line-height: 1.6;
}

.security-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.security-actions .el-button {
  margin-left: 0;
}

.password-form {
  max-width: 360px;
}

@media (max-width: 860px) {
  .settings-grid,
  .profile-panel {
    grid-template-columns: 1fr;
  }

  .language-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
