<template>
  <section class="support-config-page">
    <div v-loading="loading" class="config-grid">
      <section class="config-card">
        <div class="card-heading">
          <div>
            <h2>{{ t('supportConfig.title') }}</h2>
            <p>{{ t('supportConfig.subtitle') }}</p>
          </div>
        </div>

        <el-form label-position="top" class="config-form">
          <el-form-item :label="t('supportConfig.serviceName')" required>
            <el-input v-model.trim="form.serviceName" maxlength="120" show-word-limit />
          </el-form-item>
          <el-form-item :label="t('supportConfig.qrCodeUrl')">
            <el-input
              v-model.trim="form.qrCodeUrl"
              maxlength="1024"
              :placeholder="t('supportConfig.qrCodeUrlPlaceholder')"
            />
          </el-form-item>
          <el-form-item :label="t('supportConfig.contactHint')">
            <el-input
              v-model.trim="form.contactHint"
              type="textarea"
              :rows="4"
              maxlength="512"
              show-word-limit
              :placeholder="t('supportConfig.contactHintPlaceholder')"
            />
          </el-form-item>
          <div class="channel-section">
            <div class="channel-section-heading">
              <strong>{{ t('supportConfig.channels') }}</strong>
              <span>{{ t('supportConfig.channelsHint') }}</span>
            </div>
            <div class="channel-list">
              <div class="channel-config-row">
                <div class="channel-config-title"><strong>{{ t('supportConfig.email') }}</strong><el-checkbox v-model="form.emailVisible">{{ t('supportConfig.showChannel') }}</el-checkbox></div>
                <el-input
                  v-model.trim="form.email"
                  maxlength="254"
                  :placeholder="t('supportConfig.emailPlaceholder')"
                />
              </div>
              <div class="channel-config-row">
                <div class="channel-config-title"><strong>{{ t('supportConfig.phone') }}</strong><el-checkbox v-model="form.phoneVisible">{{ t('supportConfig.showChannel') }}</el-checkbox></div>
                <el-input
                  v-model.trim="form.phone"
                  maxlength="40"
                  :placeholder="t('supportConfig.phonePlaceholder')"
                />
              </div>
              <div class="channel-config-row">
                <div class="channel-config-title"><strong>{{ t('supportConfig.telegramQr') }}</strong><el-checkbox v-model="form.telegramQrVisible">{{ t('supportConfig.showChannel') }}</el-checkbox></div>
                <div class="qr-upload-control">
                  <div class="qr-upload-preview"><img v-if="form.telegramQrUrl" :src="form.telegramQrUrl" alt="Telegram" /><el-icon v-else><UploadFilled /></el-icon></div>
                  <div class="qr-upload-actions">
                    <el-button :loading="telegramUploading" @click="telegramFileInput?.click()"><el-icon><UploadFilled /></el-icon>{{ t('supportConfig.uploadQr') }}</el-button>
                    <span>{{ t('supportConfig.uploadQrHint') }}</span>
                  </div>
                  <input ref="telegramFileInput" hidden type="file" accept="image/png,image/jpeg,image/webp,image/gif" @change="event => handleQrFileChange('telegram', event)" />
                </div>
              </div>
              <div class="channel-config-row">
                <div class="channel-config-title"><strong>{{ t('supportConfig.wechatQr') }}</strong><el-checkbox v-model="form.wechatQrVisible">{{ t('supportConfig.showChannel') }}</el-checkbox></div>
                <div class="qr-upload-control">
                  <div class="qr-upload-preview"><img v-if="form.wechatQrUrl" :src="form.wechatQrUrl" :alt="t('supportConfig.wechatQr')" /><el-icon v-else><UploadFilled /></el-icon></div>
                  <div class="qr-upload-actions">
                    <el-button :loading="wechatUploading" @click="wechatFileInput?.click()"><el-icon><UploadFilled /></el-icon>{{ t('supportConfig.uploadQr') }}</el-button>
                    <span>{{ t('supportConfig.uploadQrHint') }}</span>
                  </div>
                  <input ref="wechatFileInput" hidden type="file" accept="image/png,image/jpeg,image/webp,image/gif" @change="event => handleQrFileChange('wechat', event)" />
                </div>
              </div>
            </div>
          </div>
        </el-form>

        <div class="form-actions">
          <span class="updated-at">{{ t('supportConfig.updatedAt') }}: {{ formatDate(config?.updatedAt) }}</span>
          <el-button type="primary" :loading="saving" @click="saveConfig">
            {{ t('supportConfig.save') }}
          </el-button>
        </div>
      </section>

      <aside class="preview-card">
        <h2>{{ t('supportConfig.preview') }}</h2>
        <div class="qr-preview">
          <img v-if="previewQrUrl" :src="previewQrUrl" :alt="t('wallet.customerServiceQr')" />
          <span v-else>{{ t('wallet.qrNotConfigured') }}</span>
        </div>
        <strong>{{ form.serviceName || '-' }}</strong>
        <p>{{ form.contactHint || t('wallet.scanCustomerServiceQr') }}</p>
        <div v-if="configuredDisplayChannels.length" class="channel-preview-list">
          <div
            v-for="channel in configuredDisplayChannels"
            :key="channel.type"
            class="channel-preview-item"
          >
            <span>{{ channel.badge }}</span>
            <div><strong>{{ channel.label }}</strong><small v-if="channel.kind === 'text'">{{ channel.value }}</small></div>
            <img v-if="channel.kind === 'qr'" :src="channel.value" :alt="channel.label" />
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getAdminCustomerServiceConfig,
  uploadCustomerServiceQrImage,
  updateAdminCustomerServiceConfig,
  type CustomerServiceConfig
} from '@/api/support'

const { t } = useI18n()
const LEGACY_DEFAULT_SERVICE_NAME = 'Youou-ASO Support'
const LEGACY_DEFAULT_CONTACT_HINT = 'Scan the QR code to contact customer service for recharge.'
const ZH_DEFAULT_SERVICE_NAME = 'Youou-ASO 客服'
const ZH_DEFAULT_CONTACT_HINT = '请扫码联系客服完成充值，到账后余额将更新。'
const loading = ref(false)
const saving = ref(false)
const telegramUploading = ref(false)
const wechatUploading = ref(false)
const telegramFileInput = ref<HTMLInputElement | null>(null)
const wechatFileInput = ref<HTMLInputElement | null>(null)
const config = ref<CustomerServiceConfig | null>(null)
const form = reactive({
  serviceName: '',
  qrCodeUrl: '',
  contactHint: '',
  email: '',
  teamsUrl: '',
  telegramUrl: '',
  whatsappUrl: '',
  emailVisible: false,
  phone: '',
  phoneVisible: false,
  telegramQrUrl: '',
  telegramQrVisible: false,
  wechatQrUrl: '',
  wechatQrVisible: false,
  enabled: false
})

const previewQrUrl = computed(() => (form.enabled && form.qrCodeUrl ? form.qrCodeUrl : ''))
const configuredChannels = computed(() => [
  form.email ? { type: 'email', badge: '@', label: form.email, href: `mailto:${form.email}` } : null,
  form.teamsUrl ? { type: 'teams', badge: 'T', label: 'Microsoft Teams', href: form.teamsUrl } : null,
  form.telegramUrl ? { type: 'telegram', badge: '➤', label: 'Telegram', href: form.telegramUrl } : null,
  form.whatsappUrl ? { type: 'whatsapp', badge: 'W', label: 'WhatsApp', href: form.whatsappUrl } : null
].filter((channel): channel is NonNullable<typeof channel> => Boolean(channel)))

const configuredDisplayChannels = computed(() => [
  form.emailVisible && form.email ? { type: 'email', badge: '@', label: t('supportConfig.email'), value: form.email, kind: 'text' } : null,
  form.phoneVisible && form.phone ? { type: 'phone', badge: 'P', label: t('supportConfig.phone'), value: form.phone, kind: 'text' } : null,
  form.telegramQrVisible && form.telegramQrUrl ? { type: 'telegram', badge: 'T', label: 'Telegram', value: form.telegramQrUrl, kind: 'qr' } : null,
  form.wechatQrVisible && form.wechatQrUrl ? { type: 'wechat', badge: 'W', label: t('supportConfig.wechatQr'), value: form.wechatQrUrl, kind: 'qr' } : null
].filter((channel): channel is NonNullable<typeof channel> => Boolean(channel)))

onMounted(() => {
  void loadConfig()
})

async function loadConfig() {
  loading.value = true
  try {
    const result = await getAdminCustomerServiceConfig()
    config.value = result
    form.serviceName = localizeServiceName(result.serviceName)
    form.qrCodeUrl = result.qrCodeUrl || ''
    form.contactHint = localizeContactHint(result.contactHint)
    form.email = result.email || ''
    form.emailVisible = result.emailVisible
    form.phone = result.phone || ''
    form.phoneVisible = result.phoneVisible
    form.telegramQrUrl = result.telegramQrUrl || ''
    form.telegramQrVisible = result.telegramQrVisible
    form.wechatQrUrl = result.wechatQrUrl || ''
    form.wechatQrVisible = result.wechatQrVisible
    form.teamsUrl = result.teamsUrl || ''
    form.telegramUrl = result.telegramUrl || ''
    form.whatsappUrl = result.whatsappUrl || ''
    form.enabled = result.enabled
  } catch {
    ElMessage.error(t('supportConfig.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  if (!form.serviceName.trim()) {
    ElMessage.warning(t('supportConfig.serviceNameRequired'))
    return
  }
  if (form.email.trim() && !isValidEmail(form.email)) {
    ElMessage.warning(t('supportConfig.invalidEmail'))
    return
  }
  const invalidUrlChannel = [
    { label: t('supportConfig.qrCodeUrl'), value: form.qrCodeUrl },
    { label: t('supportConfig.telegramQr'), value: form.telegramQrUrl },
    { label: t('supportConfig.wechatQr'), value: form.wechatQrUrl }
  ].find(({ value }) => value.trim() && !isValidHttpUrl(value))
  if (invalidUrlChannel) {
    ElMessage.warning(t('supportConfig.invalidChannelUrl', { channel: invalidUrlChannel.label }))
    return
  }
  const missingVisibleChannel = [
    { visible: form.emailVisible, label: t('supportConfig.email'), value: form.email },
    { visible: form.phoneVisible, label: t('supportConfig.phone'), value: form.phone },
    { visible: form.telegramQrVisible, label: t('supportConfig.telegramQr'), value: form.telegramQrUrl },
    { visible: form.wechatQrVisible, label: t('supportConfig.wechatQr'), value: form.wechatQrUrl }
  ].find(({ visible, value }) => visible && !value.trim())
  if (missingVisibleChannel) {
    ElMessage.warning(t('supportConfig.channelValueRequired', { channel: missingVisibleChannel.label }))
    return
  }
  saving.value = true
  try {
    const result = await updateAdminCustomerServiceConfig({
      serviceName: form.serviceName.trim(),
      qrCodeUrl: form.qrCodeUrl.trim() || undefined,
      contactHint: form.contactHint.trim() || undefined,
      email: form.email.trim() || undefined,
      emailVisible: form.emailVisible,
      phone: form.phone.trim() || undefined,
      phoneVisible: form.phoneVisible,
      telegramQrUrl: form.telegramQrUrl.trim() || undefined,
      telegramQrVisible: form.telegramQrVisible,
      wechatQrUrl: form.wechatQrUrl.trim() || undefined,
      wechatQrVisible: form.wechatQrVisible,
      enabled: form.enabled
    })
    config.value = result
    form.serviceName = localizeServiceName(result.serviceName)
    form.qrCodeUrl = result.qrCodeUrl || ''
    form.contactHint = localizeContactHint(result.contactHint)
    form.email = result.email || ''
    form.emailVisible = result.emailVisible
    form.phone = result.phone || ''
    form.phoneVisible = result.phoneVisible
    form.telegramQrUrl = result.telegramQrUrl || ''
    form.telegramQrVisible = result.telegramQrVisible
    form.wechatQrUrl = result.wechatQrUrl || ''
    form.wechatQrVisible = result.wechatQrVisible
    form.teamsUrl = result.teamsUrl || ''
    form.telegramUrl = result.telegramUrl || ''
    form.whatsappUrl = result.whatsappUrl || ''
    form.enabled = result.enabled
    ElMessage.success(t('supportConfig.saved'))
  } catch {
    ElMessage.error(t('supportConfig.saveFailed'))
  } finally {
    saving.value = false
  }
}

function isValidEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim())
}

async function handleQrFileChange(channel: 'telegram' | 'wechat', event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type)) {
    ElMessage.warning(t('supportConfig.uploadTypeInvalid'))
    return
  }
  if (file.size > 1024 * 1024) {
    ElMessage.warning(t('supportConfig.uploadTooLarge'))
    return
  }
  const uploading = channel === 'telegram' ? telegramUploading : wechatUploading
  uploading.value = true
  try {
    const result = await uploadCustomerServiceQrImage(file)
    if (channel === 'telegram') form.telegramQrUrl = result.url
    else form.wechatQrUrl = result.url
    ElMessage.success(t('supportConfig.uploaded'))
  } catch {
    ElMessage.error(t('supportConfig.uploadFailed'))
  } finally {
    uploading.value = false
  }
}

function isValidHttpUrl(value: string) {
  if (value.trim().startsWith('/uploads/app-icons/') && !value.includes('..')) return true
  try {
    const url = new URL(value.trim())
    return (url.protocol === 'http:' || url.protocol === 'https:') && Boolean(url.hostname)
  } catch {
    return false
  }
}

function formatDate(value: string | null | undefined) {
  return value ? value.replace('T', ' ') : '-'
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
</script>

<style scoped>
.support-config-page {
  color: #0f172a;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  align-items: start;
}

.config-card,
.preview-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.config-card {
  padding: 20px;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

h2,
p {
  letter-spacing: 0;
}

h2 {
  margin: 0;
  font-size: 18px;
}

.card-heading p,
.preview-card p {
  margin: 6px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.config-form {
  max-width: 760px;
}

.channel-section {
  margin-top: 8px;
  padding: 16px;
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  background: #f8fafc;
}

.channel-section-heading {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-bottom: 14px;
}

.channel-section-heading strong {
  font-size: 14px;
}

.channel-section-heading span {
  color: #64748b;
  font-size: 12px;
}

.channel-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.channel-config-row {
  min-width: 0;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  background: #ffffff;
}

.channel-config-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 9px;
  color: #334155;
  font-size: 13px;
}

.qr-upload-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qr-upload-preview {
  width: 72px;
  height: 72px;
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  overflow: hidden;
  border: 1px dashed #bfccdc;
  border-radius: 9px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.qr-upload-preview img {
  width: 100%;
  height: 100%;
  padding: 4px;
  background: #ffffff;
  object-fit: contain;
}

.qr-upload-actions {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.qr-upload-actions span {
  color: #64748b;
  font-size: 11px;
  line-height: 1.4;
}

.channel-preview-list {
  width: 100%;
  display: grid;
  gap: 8px;
  margin-top: 16px;
}

.channel-preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  color: #334155;
  font-size: 12px;
}

.channel-preview-item > span {
  width: 24px;
  height: 24px;
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 7px;
  background: #e8f1ff;
  color: #2563eb;
  font-weight: 900;
}

.channel-preview-item > div {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

.channel-preview-item small {
  max-width: 100%;
  overflow: hidden;
  color: #64748b;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.channel-preview-item img {
  width: 54px;
  height: 54px;
  border-radius: 6px;
  object-fit: contain;
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
  color: #64748b;
  font-size: 13px;
}

.preview-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.qr-preview {
  width: 220px;
  height: 220px;
  display: grid;
  place-items: center;
  margin: 18px 0 14px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background:
    linear-gradient(90deg, rgb(24 34 48 / 8%) 1px, transparent 1px),
    linear-gradient(rgb(24 34 48 / 8%) 1px, transparent 1px),
    #f8fafc;
  background-size: 18px 18px;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.qr-preview img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: contain;
}

.preview-card strong {
  color: #0f172a;
  font-size: 16px;
}

@media (max-width: 920px) {
  .config-grid {
    grid-template-columns: 1fr;
  }

  .card-heading,
  .form-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .channel-list {
    grid-template-columns: 1fr;
  }
}
</style>
