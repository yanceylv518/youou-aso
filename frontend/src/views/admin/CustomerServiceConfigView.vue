<template>
  <section class="support-config-page">
    <div v-loading="loading" class="config-grid">
      <section class="config-card">
        <div class="card-heading">
          <div>
            <h2>{{ t('supportConfig.title') }}</h2>
            <p>{{ t('supportConfig.subtitle') }}</p>
          </div>
          <el-switch
            v-model="form.enabled"
            :active-text="t('supportConfig.enabled')"
            :inactive-text="t('supportConfig.disabled')"
          />
        </div>

        <el-form label-position="top" class="config-form">
          <el-form-item :label="t('supportConfig.serviceName')" required>
            <el-input v-model.trim="form.serviceName" maxlength="120" show-word-limit />
          </el-form-item>
          <el-form-item :label="t('supportConfig.qrCodeUrl')" required>
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
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  getAdminCustomerServiceConfig,
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
const config = ref<CustomerServiceConfig | null>(null)
const form = reactive({
  serviceName: '',
  qrCodeUrl: '',
  contactHint: '',
  enabled: false
})

const previewQrUrl = computed(() => (form.enabled && form.qrCodeUrl ? form.qrCodeUrl : ''))

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
  if (form.enabled && !form.qrCodeUrl.trim()) {
    ElMessage.warning(t('supportConfig.qrCodeRequired'))
    return
  }
  saving.value = true
  try {
    const result = await updateAdminCustomerServiceConfig({
      serviceName: form.serviceName.trim(),
      qrCodeUrl: form.qrCodeUrl.trim() || undefined,
      contactHint: form.contactHint.trim() || undefined,
      enabled: form.enabled
    })
    config.value = result
    form.serviceName = localizeServiceName(result.serviceName)
    form.qrCodeUrl = result.qrCodeUrl || ''
    form.contactHint = localizeContactHint(result.contactHint)
    form.enabled = result.enabled
    ElMessage.success(t('supportConfig.saved'))
  } catch {
    ElMessage.error(t('supportConfig.saveFailed'))
  } finally {
    saving.value = false
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
  color: #667085;
  line-height: 1.6;
}

.config-form {
  max-width: 760px;
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
  color: #667085;
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
  color: #182230;
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
}
</style>
