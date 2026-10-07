<template>
  <section class="promotion-page">
    <section class="service-section">
      <div class="section-heading"><div><h2>{{ t('promotionPage.standardTitle') }}</h2><p>{{ t('promotionPage.standardSubtitle') }}</p></div></div>
      <div class="service-grid standard-grid">
        <article v-for="service in standardServices" :key="service.id" class="service-card">
          <div class="service-art" :class="service.tone"><component :is="service.icon" class="service-icon" /></div>
          <div class="service-body"><div class="service-heading"><h3>{{ moduleName(service.module) }}</h3></div><p>{{ moduleDescription(service.module) }}</p><el-button type="primary" class="service-action" @click="goOrder(service)">{{ t('adminPromotion.createForCustomer') }}</el-button></div>
        </article>
      </div>
    </section>
    <section class="service-section">
      <div class="section-heading"><div><h2>{{ t('promotionPage.specialTitle') }}</h2><p>{{ t('promotionPage.specialSubtitle') }}</p></div></div>
      <div class="service-grid special-grid">
        <article v-for="service in specialServices" :key="service.id" class="service-card special-card">
          <div class="service-art" :class="service.tone"><component :is="service.icon" class="service-icon" /></div>
          <div class="service-body"><div class="service-heading"><h3>{{ moduleName(service.module) }}</h3><span class="service-badge">{{ t('promotionPage.auditBadge') }}</span></div><p>{{ moduleDescription(service.module) }}</p><el-button type="primary" plain class="service-action" @click="goOrder(service)">{{ t('adminPromotion.createForCustomer') }}</el-button></div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { localizedModule } from '@/utils/moduleLocalization'
import { computed, onMounted, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Download, EditPen, Medal, Search, Star, TrendCharts, Trophy } from '@element-plus/icons-vue'
import type { OrderType } from '@/api/orders'
import { getAdminOrderModules, type OrderModuleConfig } from '@/api/orderModules'

interface ServiceOption {
  id: number
  code: OrderType
  icon: Component
  tone: string
  special?: boolean
  module: OrderModuleConfig
}

const router = useRouter()
const { t, locale } = useI18n()
const modules = ref<OrderModuleConfig[]>([])

const presentation: Record<OrderType, { icon: Component; tone: string; special?: boolean }> = {
  KEYWORD_INSTALL: { icon: Search, tone: 'tone-cyan' }, DOWNLOAD: { icon: Download, tone: 'tone-pink' },
  RATING: { icon: Star, tone: 'tone-blue' }, REVIEW: { icon: EditPen, tone: 'tone-pink' },
  RANK_GUARANTEE: { icon: Medal, tone: 'tone-cyan', special: true },
  CHART_RANK_GUARANTEE: { icon: Trophy, tone: 'tone-purple', special: true },
  KEYWORD_COVERAGE: { icon: TrendCharts, tone: 'tone-green', special: true }
}
const services = computed<ServiceOption[]>(() => modules.value.filter((module) => module.enabled).map((module) => ({ id: module.id, code: module.orderType, module, ...presentation[module.orderType] })))
const standardServices = computed(() => services.value.filter((service) => !service.special))
const specialServices = computed(() => services.value.filter((service) => service.special))
onMounted(async () => { modules.value = await getAdminOrderModules() })
function localized(module: OrderModuleConfig, field: 'moduleName' | 'moduleDescription') {
  return localizedModule(module, field, locale.value)
}
const moduleName = (module: OrderModuleConfig) => localized(module, 'moduleName')
const moduleDescription = (module: OrderModuleConfig) => localized(module, 'moduleDescription')

function goOrder(service: ServiceOption) {
  router.push({
    name: 'admin-order-create',
    query: {
      orderType: service.code,
      orderModuleId: String(service.id)
    }
  })
}
</script>

<style scoped>
.promotion-page { width: min(100%, 1320px); margin: 0 auto; }
.service-section + .service-section { margin-top: 34px; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 14px; padding-inline: 2px; }
.section-heading h2 { margin: 0; color: #0f172a; font-size: 19px; letter-spacing: -.02em; }
.section-heading p { margin: 5px 0 0; color: #64748b; font-size: 13px; line-height: 1.5; }
.section-count { min-width: 28px; height: 28px; display: inline-grid; place-items: center; border-radius: 999px; background: #eaf2ff; color: #1d4ed8; font-size: 12px; font-weight: 750; }
.service-grid { display: grid; gap: 16px; }
.standard-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.special-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.service-card { min-width: 0; display: flex; flex-direction: column; overflow: hidden; border: 1px solid #dfe7f1; border-radius: 14px; background: #fff; box-shadow: 0 8px 24px rgb(15 23 42 / 5%); transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease; }
.service-card:hover { transform: translateY(-2px); border-color: #cbd9ea; box-shadow: 0 14px 30px rgb(15 23 42 / 8%); }
.service-art { height: 72px; display: flex; align-items: center; justify-content: center; }
.tone-cyan { background: #ecfeff; color: #0891b2; }
.tone-pink { background: #fdf2f8; color: #db2777; }
.tone-blue { background: #eff6ff; color: #2563eb; }
.tone-green { background: #f0fdf4; color: #16a34a; }
.tone-purple { background: #f5f3ff; color: #7c3aed; }
.service-icon { width: 32px; height: 32px; }
.service-body { display: flex; flex: 1; flex-direction: column; padding: 17px 16px 16px; }
.service-heading { min-height: 27px; display: flex; align-items: center; gap: 8px; }
.service-heading h3 { margin: 0; color: #0f172a; font-size: 17px; letter-spacing: -.015em; }
.service-badge { padding: 3px 7px; border: 1px solid #fde68a; border-radius: 999px; background: #fffbeb; color: #92400e; font-size: 11px; font-weight: 650; }
.service-body > p { min-height: 46px; margin: 8px 0 16px; color: #64748b; font-size: 13px; line-height: 1.65; }
.service-action { width: 100%; margin-top: auto; }
@media (max-width: 1100px) { .standard-grid, .special-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) { .section-heading { align-items: flex-start; } .standard-grid, .special-grid { grid-template-columns: 1fr; } }
</style>
