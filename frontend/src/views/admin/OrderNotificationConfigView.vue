<template>
  <section class="mail-config-page">
    <div v-loading="loading" class="config-grid">
      <section class="config-card">
        <div class="card-heading">
          <div>
            <h2>{{ t('mailConfig.title') }}</h2>
            <p>{{ t('mailConfig.subtitle') }}</p>
          </div>
        </div>

        <el-alert class="notice" :title="t('mailConfig.notice')" type="info" :closable="false" show-icon />

        <el-form label-position="top" class="config-form">
          <div class="form-section">
            <h3>{{ t('mailConfig.smtpSection') }}</h3>
            <div class="form-row">
              <el-form-item :label="t('mailConfig.smtpHost')">
                <el-input v-model.trim="form.smtpHost" maxlength="255" :placeholder="t('mailConfig.smtpHostPlaceholder')" />
              </el-form-item>
              <el-form-item :label="t('mailConfig.smtpPort')" required>
                <el-input-number v-model="form.smtpPort" :min="1" :max="65535" controls-position="right" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item :label="t('mailConfig.username')">
                <el-input v-model.trim="form.username" maxlength="255" />
              </el-form-item>
              <el-form-item :label="t('mailConfig.fromAddress')">
                <el-input v-model.trim="form.fromAddress" maxlength="255" :placeholder="t('mailConfig.fromAddressPlaceholder')" />
              </el-form-item>
            </div>
            <el-form-item :label="passwordLabel">
              <el-input
                v-model="form.password"
                type="password"
                maxlength="512"
                show-password
                :placeholder="passwordPlaceholder"
              />
            </el-form-item>
            <div class="switch-row">
              <el-checkbox v-model="form.smtpAuth">{{ t('mailConfig.smtpAuth') }}</el-checkbox>
              <el-checkbox v-model="form.startTlsEnabled">{{ t('mailConfig.startTlsEnabled') }}</el-checkbox>
              <el-checkbox v-model="form.sslEnabled">{{ t('mailConfig.sslEnabled') }}</el-checkbox>
            </div>
          </div>

          <div class="form-section">
            <div class="section-heading">
              <h3>{{ t('mailConfig.orderNotificationSection') }}</h3>
              <el-switch
                v-model="form.orderNotificationEnabled"
                :active-text="t('mailConfig.enabled')"
                :inactive-text="t('mailConfig.disabled')"
              />
            </div>
            <el-form-item :label="t('mailConfig.orderNotificationRecipients')" required>
              <el-input
                v-model="form.orderNotificationRecipients"
                type="textarea"
                :rows="5"
                maxlength="1024"
                show-word-limit
                :placeholder="t('mailConfig.recipientsPlaceholder')"
              />
            </el-form-item>
          </div>
        </el-form>

        <div class="form-actions">
          <span class="updated-at">{{ t('mailConfig.updatedAt') }}: {{ formatDate(config?.updatedAt) }}</span>
          <el-button type="primary" :loading="saving" @click="saveConfig">
            {{ t('mailConfig.save') }}
          </el-button>
        </div>
      </section>

      <aside class="preview-card">
        <h2>{{ t('mailConfig.preview') }}</h2>
        <div class="status-row">
          <span>{{ t('mailConfig.smtpStatus') }}</span>
          <el-tag :type="form.smtpHost ? 'success' : 'info'">
            {{ form.smtpHost ? t('mailConfig.configured') : t('mailConfig.usingFallback') }}
          </el-tag>
        </div>
        <div class="status-row">
          <span>{{ t('mailConfig.passwordStatus') }}</span>
          <el-tag :type="config?.passwordConfigured || form.password ? 'success' : 'info'">
            {{ config?.passwordConfigured || form.password ? t('mailConfig.configured') : t('mailConfig.notConfigured') }}
          </el-tag>
        </div>
        <div class="status-row">
          <span>{{ t('mailConfig.orderNotificationSection') }}</span>
          <el-tag :type="form.orderNotificationEnabled ? 'success' : 'info'">
            {{ form.orderNotificationEnabled ? t('mailConfig.enabled') : t('mailConfig.disabled') }}
          </el-tag>
        </div>
        <div class="recipient-list">
          <span v-if="recipientList.length === 0" class="empty">{{ t('mailConfig.noRecipients') }}</span>
          <el-tag v-for="recipient in recipientList" :key="recipient" effect="plain">
            {{ recipient }}
          </el-tag>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getMailConfig, updateMailConfig, type MailConfig } from '@/api/support'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const config = ref<MailConfig | null>(null)
const form = reactive({
  smtpHost: '',
  smtpPort: 587,
  username: '',
  password: '',
  fromAddress: '',
  smtpAuth: true,
  startTlsEnabled: false,
  sslEnabled: true,
  orderNotificationRecipients: '',
  orderNotificationEnabled: false
})

const recipientList = computed(() => parseRecipients(form.orderNotificationRecipients))
const passwordLabel = computed(() => config.value?.passwordConfigured ? t('mailConfig.passwordConfigured') : t('mailConfig.password'))
const passwordPlaceholder = computed(() => config.value?.passwordConfigured ? t('mailConfig.passwordKeepPlaceholder') : '')

onMounted(() => {
  void loadConfig()
})

async function loadConfig() {
  loading.value = true
  try {
    const result = await getMailConfig()
    config.value = result
    form.smtpHost = result.smtpHost || ''
    form.smtpPort = result.smtpPort || 587
    form.username = result.username || ''
    form.password = ''
    form.fromAddress = result.fromAddress || ''
    form.smtpAuth = result.smtpAuth
    form.startTlsEnabled = result.startTlsEnabled
    form.sslEnabled = result.sslEnabled
    form.orderNotificationRecipients = result.orderNotificationRecipients || ''
    form.orderNotificationEnabled = result.orderNotificationEnabled
  } catch {
    ElMessage.error(t('mailConfig.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  const recipients = recipientList.value
  if (form.fromAddress && !isValidEmail(form.fromAddress)) {
    ElMessage.warning(t('mailConfig.invalidEmail'))
    return
  }
  if (form.orderNotificationEnabled && recipients.length === 0) {
    ElMessage.warning(t('mailConfig.recipientsRequired'))
    return
  }
  if (recipients.some((recipient) => !isValidEmail(recipient))) {
    ElMessage.warning(t('mailConfig.invalidEmail'))
    return
  }

  saving.value = true
  try {
    const result = await updateMailConfig({
      smtpHost: form.smtpHost.trim() || undefined,
      smtpPort: form.smtpPort,
      username: form.username.trim() || undefined,
      password: form.password || undefined,
      keepExistingPassword: Boolean(config.value?.passwordConfigured && !form.password),
      fromAddress: form.fromAddress.trim() || undefined,
      smtpAuth: form.smtpAuth,
      startTlsEnabled: form.startTlsEnabled,
      sslEnabled: form.sslEnabled,
      orderNotificationRecipients: recipients.join(',') || undefined,
      orderNotificationEnabled: form.orderNotificationEnabled
    })
    config.value = result
    form.password = ''
    form.orderNotificationRecipients = result.orderNotificationRecipients || ''
    ElMessage.success(t('mailConfig.saved'))
  } catch {
    ElMessage.error(t('mailConfig.saveFailed'))
  } finally {
    saving.value = false
  }
}

function parseRecipients(value: string) {
  return Array.from(new Set(
    value
      .split(/[,;\n\r]+/)
      .map((item) => item.trim())
      .filter(Boolean)
  ))
}

function isValidEmail(value: string) {
  return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value)
}

function formatDate(value: string | null | undefined) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.mail-config-page {
  color: #182230;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  align-items: start;
}

.config-card,
.preview-card {
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.config-card {
  padding: 20px;
}

.card-heading,
.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.card-heading {
  margin-bottom: 16px;
}

h2,
h3,
p {
  letter-spacing: 0;
}

h2,
h3 {
  margin: 0;
}

h2 {
  font-size: 18px;
}

h3 {
  font-size: 15px;
}

.card-heading p {
  margin: 6px 0 0;
  color: #667085;
  line-height: 1.6;
}

.notice,
.form-section {
  margin-bottom: 18px;
}

.form-section {
  padding-top: 18px;
  border-top: 1px solid #eef2f6;
}

.form-section:first-of-type {
  border-top: 0;
  padding-top: 0;
}

.form-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 14px;
}

.switch-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

.updated-at {
  color: #667085;
  font-size: 13px;
}

.preview-card {
  padding: 20px;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 18px 0;
  color: #667085;
  font-weight: 700;
}

.recipient-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty {
  color: #98a2b3;
  font-size: 13px;
}

@media (max-width: 920px) {
  .config-grid,
  .form-row {
    grid-template-columns: 1fr;
  }

  .card-heading,
  .section-heading,
  .form-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
