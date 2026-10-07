<template>
  <section class="promotion-page">
    <div class="heading">
      <div><p>{{ t('dynamicPromotion.subtitle') }}</p></div>
    </div>
    <div v-loading="loading" class="grid">
      <article v-for="(item, index) in modules" :key="item.id" class="card">
        <div class="art" :class="`tone-${index % 5}`"><component :is="icon(item.orderType)" /></div>
        <div class="body">
          <h3>{{ moduleLabel(item) }}</h3>
          <p>{{ moduleDescription(item) }}</p>
          <el-button class="order-button" type="primary" plain @click="go(item)">{{ t(admin ? 'dynamicPromotion.createForCustomer' : 'dynamicPromotion.orderNow') }}</el-button>
        </div>
      </article>
      <el-empty v-if="!loading && !modules.length" :description="t('dynamicPromotion.empty')" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Download, EditPen, Medal, Search, Star, TrendCharts, Trophy } from '@element-plus/icons-vue'
import { getCustomerOrderModules, type OrderModuleConfig } from '@/api/orderModules'
import type { OrderType } from '@/api/orders'

const props = defineProps<{ admin?: boolean }>()
const { t, locale } = useI18n()
const router = useRouter()
const modules = ref<OrderModuleConfig[]>([])
const loading = ref(false)
const icons: Record<OrderType, unknown> = { KEYWORD_INSTALL: Search, DOWNLOAD: Download, RATING: Star, REVIEW: EditPen, RANK_GUARANTEE: Medal, CHART_RANK_GUARANTEE: Trophy, KEYWORD_COVERAGE: TrendCharts }
const moduleLabel = (item: OrderModuleConfig) => ({ 'zh-CN':item.moduleName, 'en-US':item.moduleNameEn, 'ru-RU':item.moduleNameRu, 'pt-PT':item.moduleNamePt, 'es-ES':item.moduleNameEs }[locale.value] || item.moduleNameEn)
const moduleDescription = (item: OrderModuleConfig) => ({ 'zh-CN':item.moduleDescription, 'en-US':item.moduleDescriptionEn, 'ru-RU':item.moduleDescriptionRu, 'pt-PT':item.moduleDescriptionPt, 'es-ES':item.moduleDescriptionEs }[locale.value] || item.moduleDescriptionEn)
const icon = (value: OrderType) => icons[value]
function go(value: OrderModuleConfig) { router.push({ name: props.admin ? 'admin-order-create' : 'user-order-create', query: { orderType: value.orderType, orderModuleId: String(value.id) } }) }
onMounted(async () => { loading.value = true; try { modules.value = (await getCustomerOrderModules()).sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id) } finally { loading.value = false } })
</script>

<style scoped>
.promotion-page{width:min(100%,1320px);margin:auto}.heading{margin-bottom:20px}.heading h2{margin:0;color:#0f172a;font-size:22px;line-height:1.35}.heading p{margin:6px 0 0;color:#64748b;font-size:14px}.grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:20px 22px;min-height:220px}.card{display:grid;grid-template-columns:64px minmax(0,1fr);gap:18px;min-height:178px;padding:24px;border:1px solid #e2e8f0;border-radius:16px;background:#fff;box-shadow:0 8px 24px rgb(15 23 42/4%);transition:border-color .2s ease,box-shadow .2s ease,transform .2s ease}.card:hover{border-color:#cbd5e1;box-shadow:0 14px 34px rgb(15 23 42/8%);transform:translateY(-2px)}.art{width:58px;height:58px;display:grid;place-items:center;border-radius:18px;box-shadow:none}.art :deep(svg){width:29px;height:29px}.tone-0{background:linear-gradient(145deg,#d9fbff,#bceff5);color:#0ea5b7}.tone-1{background:linear-gradient(145deg,#ffe4f2,#fbc5e0);color:#db2777}.tone-2{background:linear-gradient(145deg,#e0eeff,#c9dcff);color:#2563eb}.tone-3{background:linear-gradient(145deg,#eee9ff,#ddd2ff);color:#7c3aed}.tone-4{background:linear-gradient(145deg,#dcfce7,#bff2d1);color:#16a34a}.body{display:flex;min-width:0;flex-direction:column;align-items:flex-start}.body h3{margin:1px 0 7px;color:#172033;font-size:18px;line-height:1.35}.body p{display:-webkit-box;overflow:hidden;min-height:42px;margin:0 0 18px;color:#64748b;font-size:14px;line-height:1.55;-webkit-box-orient:vertical;-webkit-line-clamp:2}.order-button{min-width:104px;margin-top:auto;padding-inline:20px;border-radius:7px;font-weight:700;box-shadow:none}@media(max-width:1080px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:680px){.grid{grid-template-columns:1fr}.card{grid-template-columns:54px minmax(0,1fr);gap:14px;padding:20px}.art{width:52px;height:52px;border-radius:16px}}
</style>
