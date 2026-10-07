<template>
  <section v-loading="loading" class="module-pricing">
    <div class="heading"><div><h2>{{t('pricing.regionConfigTitle')}}</h2><p>{{t('pricing.regionConfigHelp')}}</p></div><el-button type="primary" :loading="saving" :disabled="!activeConfig || !activeDirty" @click="save">{{t('pricing.save')}}</el-button></div>
    <el-alert :closable="false" type="info" :title="t('pricing.notice')" />
    <el-select v-model="activeModuleId" class="module-select" filterable :placeholder="t('orderModulesAdmin.name')"><el-option v-for="module in modules" :key="module.id" :value="module.id" :label="moduleName(module) + ' · ' + t(`ordersPage.types.${module.orderType}`)"/></el-select>
    <template v-if="activeModule && activeConfig">
      <div class="stores"><span>{{t('pricing.supportedStores')}}:</span><el-tag v-for="store in activeModule.storeTypes" :key="store">{{storeLabels[store]}}</el-tag></div>
      <p v-if="activePriceCodes.length">{{t('pricing.moduleDefault')}}: {{t('pricing.otherRegionPrice')}} ${{Number(activeModule.unitPrice).toFixed(4)}} / {{t('pricing.chinaRegionPrice')}} ${{Number(activeModule.chinaUnitPrice).toFixed(4)}}</p>
      <el-select v-model="activeConfig.allowedRegionCodes" multiple filterable collapse-tags :max-collapse-tags="8" class="region-select" :placeholder="t('pricing.selectRegions')"><el-option v-for="region in availableRegions" :key="region.code" :value="region.code" :label="regionLabel(region.code)"/></el-select>
      <el-alert v-if="removedRegions[activeModule.id]?.length" type="warning" :closable="false" :title="t('pricing.removedRegionsHint', { regions: removedRegions[activeModule.id]!.map(regionLabel).join('、') })" />
      <p class="hint">{{t('pricing.emptyRegionsHint')}}</p>
      <div class="price-filters">
        <el-input v-model="regionSearch" clearable :placeholder="t('visual.regionSearch')" :aria-label="t('visual.regionSearch')" />
        <el-select v-model="priceScope" :aria-label="t('visual.priceScope')"><el-option v-for="key in ['allPrices','overrides','defaults']" :key="key" :value="key" :label="t('visual.' + key)" /></el-select>
        <el-checkbox v-model="changedOnly">{{t('visual.changedOnly')}}</el-checkbox>
      </div>
      <p class="hint">{{t('visual.unitPrecision')}}</p>
      <div v-for="code in activePriceCodes" :key="code" class="price-block"><h3>{{t(`pricing.items.${code}.title`)}}</h3><div class="price-grid"><label v-for="region in visibleRegions(code)" :key="region"><span>{{regionLabel(region)}}</span><el-input-number v-model="activeConfig.regionPrices[code]![region]" :min="0.0001" :precision="4" :step="0.1" controls-position="right" :placeholder="t('pricing.useDefaultPrice')"/></label></div></div>
      <el-alert v-if="!activePriceCodes.length" type="info" :closable="false" :title="t('pricing.manualPricingType')"/>
    </template>
    <el-empty v-else :description="t('orderModulesAdmin.empty')"/>
    <footer class="save-bar"><span role="status">{{t(hasChanges ? 'visual.unsaved' : 'visual.saved')}}</span><el-button type="primary" :loading="saving" :disabled="!activeConfig || !activeDirty" @click="save">{{t('pricing.save')}}</el-button></footer>
  </section>
</template>
<script setup lang="ts">
import { localizedModule } from '@/utils/moduleLocalization'
import {computed,onMounted,onBeforeUnmount,ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRoute,onBeforeRouteLeave} from 'vue-router'
import {localizedRegion} from '@/utils/presentation'
import {ElMessage,ElMessageBox} from 'element-plus'
import {getAdminOrderModules,type OrderModuleConfig} from '@/api/orderModules'
import {getAdminRegions,type AdminMarketRegion} from '@/api/regions'
import {getAdminOrderTypeRegionPricing,updateOrderTypeRegionPricing,type OrderTypeRegionPricing,type PriceCode} from '@/api/pricing'
import type {OrderType} from '@/api/orders'
import {availableModuleRegions,reconcileModulePricing,pricingSnapshot} from '@/utils/modulePricing'
const {t,locale}=useI18n(),route=useRoute()
const loading=ref(false),saving=ref(false),modules=ref<OrderModuleConfig[]>([]),regions=ref<AdminMarketRegion[]>([]),configs=ref<OrderTypeRegionPricing[]>([]),activeModuleId=ref<number>()
const storeLabels={APP_STORE:'App Store',GOOGLE_PLAY:'Google Play',IPAD_STORE:'iPad Store'}
const moduleName=(module:OrderModuleConfig)=>localizedModule(module, 'moduleName', locale.value)
const activeModule=computed(()=>modules.value.find(m=>m.id===activeModuleId.value))
const activePriceCodes=computed<PriceCode[]>(()=>({KEYWORD_INSTALL:['KEYWORD_INSTALL'],DOWNLOAD:['DOWNLOAD'],RATING:['RATING_5','RATING_4'],REVIEW:['REVIEW_5','REVIEW_4']} as Partial<Record<OrderType,PriceCode[]>>)[activeModule.value?.orderType as OrderType]||[])
const activeConfig=computed(()=>configs.value.find(c=>c.orderModuleId===activeModuleId.value))
const removedRegions=ref<Record<number,string[]>>({})
const availableRegions=computed(()=>availableModuleRegions(activeModule.value,regions.value))
function applyConfigs(values:OrderTypeRegionPricing[]){
  const removed:Record<number,string[]>={}
  configs.value=values.map(value=>{
    const result=reconcileModulePricing(value,modules.value.find(module=>module.id===value.orderModuleId),regions.value)
    removed[value.orderModuleId]=result.removed
    return result.config
  })
  removedRegions.value=removed
}
function regionLabel(code:string){return localizedRegion(code,locale.value)}
onMounted(async()=>{loading.value=true;try{const [m,r,c]=await Promise.all([getAdminOrderModules(),getAdminRegions(),getAdminOrderTypeRegionPricing()]);modules.value=m;regions.value=r;applyConfigs(c);rememberSaved();activeModuleId.value=m.some(v=>v.id===Number(route.query.moduleId))?Number(route.query.moduleId):m[0]?.id}catch{ElMessage.error(t('pricing.loadFailed'))}finally{loading.value=false}})
const regionSearch=ref(''),changedOnly=ref(false),priceScope=ref('allPrices')
const snapshots=ref<Record<number,string>>({})
function rememberSaved(){snapshots.value=Object.fromEntries(configs.value.map(c=>[c.orderModuleId,pricingSnapshot(c)]))}
const hasChanges=computed(()=>configs.value.some(c=>pricingSnapshot(c)!==snapshots.value[c.orderModuleId]))
const activeDirty=computed(()=>activeConfig.value && pricingSnapshot(activeConfig.value)!==snapshots.value[activeConfig.value.orderModuleId])
function visibleRegions(code:PriceCode){
 const config=activeConfig.value
 if(!config)return []
 const saved=JSON.parse(snapshots.value[config.orderModuleId]||'{}') as Partial<OrderTypeRegionPricing>
 const search=regionSearch.value.trim().toLocaleLowerCase()
 return config.allowedRegionCodes.filter(region=>{
  const value=config.regionPrices[code]?.[region]
  const changed=!saved.allowedRegionCodes?.includes(region)||(value??null)!==(saved.regionPrices?.[code]?.[region]??null)
  return (!search||regionLabel(region).toLocaleLowerCase().includes(search))&&(!changedOnly.value||changed)&&(priceScope.value==='allPrices'||(priceScope.value==='defaults'?value==null:value!=null))
 })
}
async function save(){
 if(!activeConfig.value)return
 const payload=JSON.parse(pricingSnapshot(activeConfig.value)) as OrderTypeRegionPricing
 saving.value=true
 try{
  const returned=await updateOrderTypeRegionPricing([payload])
  const updated=returned.find(c=>c.orderModuleId===payload.orderModuleId)||payload
  const normalized=reconcileModulePricing(updated,modules.value.find(m=>m.id===payload.orderModuleId),regions.value).config
  const index=configs.value.findIndex(c=>c.orderModuleId===payload.orderModuleId)
  // Other modules may also have unsaved edits: do not replace them with the response.
  if(pricingSnapshot(configs.value[index]!)===pricingSnapshot(payload))configs.value[index]=normalized
  snapshots.value[payload.orderModuleId]=pricingSnapshot(normalized)
  ElMessage.success(t('pricing.saved'))
 }catch{ElMessage.error(t('pricing.saveFailed'))}finally{saving.value=false}
}
onBeforeRouteLeave(async()=>{if(!hasChanges.value)return true;try{await ElMessageBox.confirm(t('visual.discard'),t('visual.unsaved'),{confirmButtonText:t('visual.leave'),cancelButtonText:t('common.cancel'),type:'warning'});return true}catch{return false}})
function preventUnload(event:BeforeUnloadEvent){if(hasChanges.value){event.preventDefault();event.returnValue=''}}
onMounted(()=>window.addEventListener('beforeunload',preventUnload))
onBeforeUnmount(()=>window.removeEventListener('beforeunload',preventUnload))
</script>
<style scoped>
.module-pricing{padding:24px;background:#fff;border:1px solid #dbe4f0;border-radius:12px}.heading{display:flex;justify-content:space-between;align-items:center;gap:20px}.heading h2{margin:0}.heading p,.hint{color:#64748b;line-height:1.6}.module-select{width:100%;max-width:600px;margin:24px 0 16px}.stores{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:20px}.region-select{width:100%}.price-block{margin-top:24px}.price-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.price-grid label{display:grid;gap:8px}.price-grid .el-input-number{width:100%}.hint{font-size:13px}@media(max-width:800px){.price-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:500px){.price-grid{grid-template-columns:1fr}.heading{align-items:flex-start}}
.price-filters{display:flex;flex-wrap:wrap;gap:12px;align-items:center;margin:20px 0}.price-filters .el-input{width:min(320px,100%)}.price-filters .el-select{width:200px}.price-grid label{min-width:0}.price-grid label>span{overflow-wrap:anywhere}.save-bar{position:sticky;bottom:0;display:flex;align-items:center;justify-content:space-between;gap:16px;margin:24px -12px -12px;padding:16px;background:#fff;border-top:1px solid var(--app-border);box-shadow:0 -4px 16px #0f172a08;z-index:5}
</style>
