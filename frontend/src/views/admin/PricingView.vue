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

    <section class="region-config-card">
      <div class="region-config-head">
        <div><h2>{{ t('pricing.regionConfigTitle') }}</h2><p>{{ t('pricing.regionConfigHelp') }}</p></div>
        <el-select v-model="activeOrderType" class="order-type-select">
          <el-option v-for="type in orderTypes" :key="type" :label="t(`ordersPage.types.${type}`)" :value="type" />
        </el-select>
      </div>
      <el-select v-model="activeRegionConfig.allowedRegionCodes" multiple filterable collapse-tags :max-collapse-tags="6" class="region-multi-select" :placeholder="t('pricing.selectRegions')">
        <el-option v-for="region in regions" :key="region.code" :label="`${region.nameZh} (${region.code})`" :value="region.code" />
      </el-select>
      <div v-if="activePriceCodes.length" class="override-list">
        <div class="override-title">{{ t('pricing.regionOverrideTitle') }}</div>
        <div v-for="code in activePriceCodes" :key="code" class="override-block">
          <strong>{{ t(`pricing.items.${code}.title`) }}</strong>
          <div class="override-grid">
            <label v-for="regionCode in activeRegionConfig.allowedRegionCodes" :key="`${code}-${regionCode}`">
              <span>{{ regionLabel(regionCode) }}</span>
              <el-input-number v-model="activeRegionConfig.regionPrices[code]![regionCode]" :placeholder="t('pricing.useDefaultPrice')" :min="0" :precision="4" :step="0.1" controls-position="right" />
            </label>
          </div>
        </div>
      </div>
      <el-empty v-else :description="t('pricing.manualPricingType')" :image-size="64" />
    </section>

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
        <div class="region-price-grid">
          <label class="region-price-field">
            <span>{{ t('pricing.otherRegionPrice') }}</span>
            <div class="price-control">
              <span class="currency-symbol">$</span>
              <el-input-number v-model="form[item.code]" class="price-input" :min="0" :precision="4" :step="0.1" controls-position="right" />
              <span class="currency-code">USD</span>
            </div>
          </label>
          <label class="region-price-field china-price-field">
            <span>{{ t('pricing.chinaRegionPrice') }}</span>
            <div class="price-control">
              <span class="currency-symbol">$</span>
              <el-input-number v-model="chinaForm[item.code]" class="price-input" :min="0" :precision="4" :step="0.1" controls-position="right" />
              <span class="currency-code">USD</span>
            </div>
          </label>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
import { getPricingConfig, updatePricingConfig, getAdminOrderTypeRegionPricing, updateOrderTypeRegionPricing, type OrderTypeRegionPricing, type PriceCode } from '@/api/pricing'
import { getAdminRegions, type AdminMarketRegion } from '@/api/regions'
import type { OrderType } from '@/api/orders'

const { t } = useI18n()

const priceItems: Array<{ code: PriceCode; icon: unknown; theme: string }> = [
  { code: 'KEYWORD_INSTALL', icon: Search, theme: 'blue' },
  { code: 'DOWNLOAD', icon: Download, theme: 'green' },
  { code: 'RATING_5', icon: Star, theme: 'orange' },
  { code: 'RATING_4', icon: Trophy, theme: 'purple' },
  { code: 'REVIEW_5', icon: ChatDotRound, theme: 'cyan' },
  { code: 'REVIEW_4', icon: TrendCharts, theme: 'yellow' }
]
const orderTypes: OrderType[] = ['KEYWORD_INSTALL', 'DOWNLOAD', 'RATING', 'REVIEW', 'RANK_GUARANTEE', 'CHART_RANK_GUARANTEE', 'KEYWORD_COVERAGE']
const activeOrderType = ref<OrderType>('KEYWORD_INSTALL')
const regions = ref<AdminMarketRegion[]>([])
const regionConfigs = ref<OrderTypeRegionPricing[]>([])
const activePriceCodes = computed<PriceCode[]>(() => ({
  KEYWORD_INSTALL: ['KEYWORD_INSTALL'], DOWNLOAD: ['DOWNLOAD'], RATING: ['RATING_5', 'RATING_4'], REVIEW: ['REVIEW_5', 'REVIEW_4']
} as Partial<Record<OrderType, PriceCode[]>>)[activeOrderType.value] || [])
const activeRegionConfig = computed(() => {
  let config = regionConfigs.value.find(item => item.orderType === activeOrderType.value)
  if (!config) {
    config = { orderType: activeOrderType.value, allowedRegionCodes: [], regionPrices: {} }
    regionConfigs.value.push(config)
  }
  activePriceCodes.value.forEach(code => { config!.regionPrices[code] ||= {} })
  return config
})

const form = reactive<Record<PriceCode, number>>({
  KEYWORD_INSTALL: 0,
  DOWNLOAD: 0,
  RATING_5: 0,
  RATING_4: 0,
  REVIEW_5: 0,
  REVIEW_4: 0
})

const chinaForm = reactive<Record<PriceCode, number>>({
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
    const [configs, regionPricing, regionList] = await Promise.all([getPricingConfig(), getAdminOrderTypeRegionPricing(), getAdminRegions()])
    regionConfigs.value = regionPricing
    regions.value = regionList.filter(region => region.enabled)
    configs.forEach((config) => {
      form[config.code] = Number(config.unitPrice)
      chinaForm[config.code] = Number(config.chinaUnitPrice)
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
      unitPrice: form[item.code],
      chinaUnitPrice: chinaForm[item.code]
    }))
    const configs = await updatePricingConfig({ items })
    regionConfigs.value = await updateOrderTypeRegionPricing(regionConfigs.value)
    configs.forEach((config) => {
      form[config.code] = Number(config.unitPrice)
      chinaForm[config.code] = Number(config.chinaUnitPrice)
    })
    ElMessage.success(t('pricing.saved'))
  } catch {
    ElMessage.error(t('pricing.saveFailed'))
  } finally {
    saving.value = false
  }
}

function regionLabel(code: string) {
  const region = regions.value.find(item => item.code === code)
  return region ? `${region.nameZh} (${code})` : code
}
</script>

<style scoped>
.pricing-page {
  color: #0f172a;
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
  color: #64748b;
  line-height: 1.6;
}

.notice {
  margin-bottom: 18px;
}

.region-config-card {
  margin-bottom: 18px;
  padding: 20px;
  border: 1px solid #dbe4f0;
  border-radius: 10px;
  background: #fff;
}

.region-config-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.region-config-head h2 { margin: 0; font-size: 18px; }
.region-config-head p { margin: 6px 0 0; color: #64748b; }
.order-type-select { width: 240px; flex: 0 0 auto; }
.region-multi-select { width: 100%; }
.override-list { margin-top: 20px; }
.override-title { margin-bottom: 12px; color: #334155; font-weight: 700; }
.override-block + .override-block { margin-top: 18px; padding-top: 18px; border-top: 1px solid #eef2f7; }
.override-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 10px; }
.override-grid label { display: grid; gap: 6px; color: #64748b; font-size: 12px; }
.override-grid .el-input-number { width: 100%; }

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-height: 220px;
}

.price-card {
  padding: 18px;
  border: 1px solid #e2e8f0;
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
  color: #0f172a;
  font-size: 17px;
}

.price-card p {
  margin: 5px 0 0;
  color: #64748b;
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
  color: #334155;
  font-weight: 700;
}

.currency-symbol {
  border-right: 1px solid #e2e8f0;
  font-size: 16px;
}

.currency-code {
  border-left: 1px solid #e2e8f0;
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
  border-color: #e2e8f0;
}

.blue {
  background: #dbeafe;
  color: #2563eb;
}

.green {
  background: #f0fdf4;
  color: #16a34a;
}

.orange {
  background: #fffbeb;
  color: #b45309;
}

.purple {
  background: #f5f3ff;
  color: #7c3aed;
}

.cyan {
  background: #d6f4f5;
  color: #43b8c3;
}

.yellow {
  background: #fffbeb;
  color: #b45309;
}

@media (max-width: 860px) {
  .toolbar {
    flex-direction: column;
  }

  .pricing-grid {
    grid-template-columns: 1fr;
  }
  .region-config-head { flex-direction: column; }
  .order-type-select { width: 100%; }
  .override-grid { grid-template-columns: 1fr 1fr; }
}

.region-price-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.region-price-field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.region-price-field > span {
  color: #64748b;
  font-size: 12px;
  font-weight: 650;
}

.china-price-field > span {
  color: #b45309;
}

@media (max-width: 640px) {
  .region-price-grid { grid-template-columns: 1fr; }
  .override-grid { grid-template-columns: 1fr; }
}</style>
