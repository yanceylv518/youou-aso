<template>
  <section class="pricing-page">
    <div class="toolbar">
      <p class="page-note">{{ t('pricing.subtitle') }}</p>
      <el-button type="primary" :loading="saving" @click="savePricing">
        {{ t('pricing.save') }}
      </el-button>
    </div>

    <el-alert class="notice" type="info" :closable="false" show-icon>
      {{ t('pricing.notice') }}
    </el-alert>

    <div v-loading="loading" class="pricing-grid">
      <article v-for="item in priceItems" :key="item.code" class="price-card">
        <div class="price-card-header">
          <span class="price-icon" :class="item.theme">
            <component :is="item.icon" />
          </span>
          <div>
            <h2>{{ t(`pricing.items.${item.code}.title`) }}</h2>
            <p>{{ t(`pricing.items.${item.code}.description`) }}</p>
          </div>
        </div>
        <div class="price-control">
          <span class="currency-symbol">$</span>
          <el-input-number
            v-model="form[item.code]"
            class="price-input"
            :min="0"
            :precision="4"
            :step="0.1"
            controls-position="right"
          />
          <span class="currency-code">USD</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Download,
  Search,
  Star,
  TrendCharts,
  Trophy
} from '@element-plus/icons-vue'
import { getPricingConfig, updatePricingConfig, type PriceCode } from '@/api/pricing'

const { t } = useI18n()

const priceItems: Array<{ code: PriceCode; icon: unknown; theme: string }> = [
  { code: 'KEYWORD_INSTALL', icon: Search, theme: 'blue' },
  { code: 'DOWNLOAD', icon: Download, theme: 'green' },
  { code: 'RATING_5', icon: Star, theme: 'orange' },
  { code: 'RATING_4', icon: Trophy, theme: 'purple' },
  { code: 'REVIEW_5', icon: ChatDotRound, theme: 'cyan' },
  { code: 'REVIEW_4', icon: TrendCharts, theme: 'yellow' }
]

const form = reactive<Record<PriceCode, number>>({
  KEYWORD_INSTALL: 0,
  DOWNLOAD: 0,
  RATING_5: 0,
  RATING_4: 0,
  REVIEW_5: 0,
  REVIEW_4: 0
})

const loading = ref(false)
const saving = ref(false)

onMounted(() => {
  loadPricing()
})

async function loadPricing() {
  loading.value = true
  try {
    const configs = await getPricingConfig()
    configs.forEach((config) => {
      form[config.code] = Number(config.unitPrice)
    })
  } catch {
    ElMessage.error(t('pricing.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function savePricing() {
  saving.value = true
  try {
    const items = priceItems.map((item) => ({
      code: item.code,
      unitPrice: form[item.code]
    }))
    const configs = await updatePricingConfig({ items })
    configs.forEach((config) => {
      form[config.code] = Number(config.unitPrice)
    })
    ElMessage.success(t('pricing.saved'))
  } catch {
    ElMessage.error(t('pricing.saveFailed'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.pricing-page {
  color: #182230;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

h2,
p {
  letter-spacing: 0;
}

.page-note {
  max-width: 720px;
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.notice {
  margin-bottom: 18px;
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-height: 220px;
}

.price-card {
  padding: 18px;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.price-card-header {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  margin-bottom: 16px;
}

.price-icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
}

.price-icon svg {
  width: 24px;
  height: 24px;
}

.price-card h2 {
  margin: 0;
  color: #182230;
  font-size: 17px;
}

.price-card p {
  margin: 5px 0 0;
  color: #667085;
  line-height: 1.5;
}

.price-control {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) 58px;
  align-items: center;
  overflow: hidden;
  border: 1px solid #d0d7e2;
  border-radius: 6px;
  background: #ffffff;
}

.currency-symbol,
.currency-code {
  display: inline-flex;
  height: 38px;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  color: #344054;
  font-weight: 700;
}

.currency-symbol {
  border-right: 1px solid #e4e9f2;
  font-size: 16px;
}

.currency-code {
  border-left: 1px solid #e4e9f2;
  font-size: 12px;
}

.price-input {
  width: 100%;
}

.price-input :deep(.el-input__wrapper) {
  box-shadow: none;
}

.price-input :deep(.el-input-number__decrease),
.price-input :deep(.el-input-number__increase) {
  border-color: #e4e9f2;
}

.blue {
  background: #dbe8ff;
  color: #2f7df4;
}

.green {
  background: #d9f5e6;
  color: #37bd78;
}

.orange {
  background: #ffedcf;
  color: #f5a11c;
}

.purple {
  background: #e7ddff;
  color: #7657d8;
}

.cyan {
  background: #d6f4f5;
  color: #43b8c3;
}

.yellow {
  background: #fff0c7;
  color: #eba51f;
}

@media (max-width: 860px) {
  .toolbar {
    flex-direction: column;
  }

  .pricing-grid {
    grid-template-columns: 1fr;
  }
}
</style>
