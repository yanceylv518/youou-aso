<template>
  <section class="modules-page">
    <div class="toolbar">
      <div><h2>{{t('orderModulesAdmin.title')}}</h2><p>{{t('orderModulesAdmin.subtitle')}}</p></div>
      <div class="toolbar-actions"><el-button @click="openRegionPricing">{{t('pricing.regionConfigTitle')}}</el-button><el-button type="primary" @click="openCreate">+ {{t('orderModulesAdmin.add')}}</el-button></div>
    </div>
    <div v-loading="loading" class="module-grid">
      <article v-for="item in modules" :key="item.id" class="module-card" :class="{disabled:!item.enabled}">
        <div class="card-head"><div><span class="type">{{typeLabel(item.orderType)}}</span><h3>{{moduleLabel(item)}}</h3></div><el-switch :model-value="item.enabled" @change="toggle(item,$event)"/></div>
        <div class="module-stores"><el-tag v-for="store in item.storeTypes" :key="store" size="small">{{storeOptions.find(s=>s.value===store)?.label}}</el-tag></div>
        <p class="module-description">{{descriptionLabel(item)}}</p>
        <div v-if="!isAudited(item.orderType)" class="prices"><div><span>{{t('orderModulesAdmin.otherRegion')}}</span><strong>${{money(item.unitPrice)}}</strong></div><div class="china"><span>{{t('orderModulesAdmin.chinaRegion')}}</span><strong>${{money(item.chinaUnitPrice)}}</strong></div></div>
        <div v-else class="audit-price">{{t('orderModulesAdmin.quotedAfterReview')}}</div>
        <div class="card-foot"><el-button link type="primary" @click="router.push({name:'admin-region-pricing',query:{moduleId:item.id}})">{{t('pricing.regionConfigTitle')}}</el-button><span>{{t('orderModulesAdmin.sort')}} {{item.sortOrder}}</span><div><el-button link type="primary" @click="openEdit(item)">{{t('orderModulesAdmin.edit')}}</el-button><el-button link type="danger" @click="remove(item)">{{t('orderModulesAdmin.delete')}}</el-button></div></div>
      </article>
      <el-empty v-if="!loading&&!modules.length" :description="t('orderModulesAdmin.empty')"/>
    </div>

    <el-dialog append-to-body v-model="visible" :title="t(editingId?'orderModulesAdmin.editTitle':'orderModulesAdmin.addTitle')" width="920px" class="module-dialog" align-center destroy-on-close>
      <el-form label-position="top" class="module-form">
        <section class="form-section language-section">
          <div class="section-heading">
            <div><h3>{{t('orderModulesAdmin.languageContent')}}</h3><p>{{activeLocaleField.label}}</p></div>
            <div class="section-actions"><span v-if="translationError" class="translation-error">{{t('orderModulesAdmin.translationFailed')}}</span><el-button size="small" :loading="translating" :disabled="!activeSourceHasContent" @click="autoFillTranslations(true)">{{t('orderModulesAdmin.autoTranslate')}}</el-button><span class="completion">{{completedLocaleCount}}/{{localeFields.length}}</span></div>
          </div>
          <el-tabs v-model="activeLocale" class="locale-tabs" stretch>
            <el-tab-pane v-for="field in localeFields" :key="field.code" :name="field.code">
              <template #label><span class="locale-tab-label">{{field.shortLabel}}<i :class="{complete:localeComplete(field)}"/></span></template>
            </el-tab-pane>
          </el-tabs>
          <div class="locale-fields">
            <el-form-item :label="t('orderModulesAdmin.name')" required><el-input v-model="form[activeLocaleField.nameKey]" maxlength="100" show-word-limit @input="scheduleAutoTranslation" @blur="autoFillTranslations()"/></el-form-item>
            <el-form-item :label="t('orderModulesAdmin.description')" required><el-input v-model="form[activeLocaleField.descriptionKey]" type="textarea" :rows="4" maxlength="500" show-word-limit resize="none" @input="scheduleAutoTranslation" @blur="autoFillTranslations()"/></el-form-item>
          </div>
        </section>
        <section class="form-section settings-section">
          <h3>{{t('orderModulesAdmin.orderType')}}</h3>
          <el-form-item :label="t('orderModulesAdmin.orderType')" required><el-select v-model="form.orderType" style="width:100%" @change="applyDescriptionDefaults(true)"><el-option v-for="type in types" :key="type" :label="typeLabel(type)" :value="type"/></el-select></el-form-item>
          <el-form-item :label="t('pricing.supportedStores')" required><el-checkbox-group v-model="form.storeTypes"><el-checkbox v-for="store in storeOptions" :key="store.value" :value="store.value">{{store.label}}</el-checkbox></el-checkbox-group></el-form-item>
          <div v-if="!isAudited(form.orderType)" class="form-prices"><el-form-item :label="t('orderModulesAdmin.otherPrice')"><el-input-number v-model="form.unitPrice" :min="0.0001" :step="0.0001" :precision="4"/></el-form-item><el-form-item :label="t('orderModulesAdmin.chinaPrice')"><el-input-number v-model="form.chinaUnitPrice" :min="0.0001" :step="0.0001" :precision="4"/></el-form-item></div>
          <div class="form-prices compact-fields"><el-form-item :label="t('orderModulesAdmin.sort')"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item><el-form-item :label="t('orderModulesAdmin.enabled')"><div class="switch-field"><el-switch v-model="form.enabled"/></div></el-form-item></div>
        </section>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button @click="visible=false">{{t('orderModulesAdmin.cancel')}}</el-button><el-button type="primary" :loading="saving" @click="save">{{t('orderModulesAdmin.save')}}</el-button></div></template>
    </el-dialog>
  </section>
</template>
<script setup lang="ts">
import { localizedModule } from '@/utils/moduleLocalization'
import{computed,onMounted,reactive,ref} from 'vue';
import{useI18n} from 'vue-i18n';
import{useRouter} from 'vue-router';
import{localeMessages,localeOptions}from'@/i18n';
import{ElMessage,ElMessageBox} from 'element-plus';
import type{OrderType} from '@/api/orders';
import{createOrderModule,deleteOrderModule,getAdminOrderModules,translateOrderModule,updateOrderModule,type OrderModuleConfig} from '@/api/orderModules';
const storeOptions=[{value:'APP_STORE',label:'App Store'},{value:'GOOGLE_PLAY',label:'Google Play'},{value:'IPAD_STORE',label:'iPad Store'}];
const router=useRouter();
const{t,locale}=useI18n(),nameFieldMap={'zh-CN':'moduleName','en-US':'moduleNameEn','ru-RU':'moduleNameRu','pt-PT':'moduleNamePt','es-ES':'moduleNameEs'}as const,descriptionFieldMap={'zh-CN':'moduleDescription','en-US':'moduleDescriptionEn','ru-RU':'moduleDescriptionRu','pt-PT':'moduleDescriptionPt','es-ES':'moduleDescriptionEs'}as const,localeFields=localeOptions.map(v=>({code:v.code,label:v.nativeLabel,shortLabel:v.code.split('-')[0].toUpperCase(),nameKey:nameFieldMap[v.code],descriptionKey:descriptionFieldMap[v.code]})),types:OrderType[]=['KEYWORD_INSTALL','DOWNLOAD','RATING','REVIEW','RANK_GUARANTEE','CHART_RANK_GUARANTEE','KEYWORD_COVERAGE'],modules=ref<OrderModuleConfig[]>([]),loading=ref(false),saving=ref(false),translating=ref(false),translationError=ref(false),visible=ref(false),editingId=ref<number|null>(null),activeLocale=ref(locale.value),form=reactive({storeTypes:['APP_STORE','GOOGLE_PLAY','IPAD_STORE'] as import('@/api/applications').StoreType[],moduleName:'',moduleNameEn:'',moduleNameRu:'',moduleNamePt:'',moduleNameEs:'',moduleDescription:'',moduleDescriptionEn:'',moduleDescriptionRu:'',moduleDescriptionPt:'',moduleDescriptionEs:'',orderType:'KEYWORD_INSTALL'as OrderType,unitPrice:0,chinaUnitPrice:0,enabled:true,sortOrder:0}),autoGenerated=reactive<Record<string,string>>({}),lastTranslationSource=ref(''),translationSequence=ref(0);let translationTimer:ReturnType<typeof setTimeout>|undefined;
const activeLocaleField=computed(()=>localeFields.find(v=>v.code===activeLocale.value)||localeFields[0]),activeSourceHasContent=computed(()=>Boolean(form[activeLocaleField.value.nameKey].trim()||form[activeLocaleField.value.descriptionKey].trim())),localeComplete=(v:(typeof localeFields)[number])=>Boolean(form[v.nameKey].trim()&&form[v.descriptionKey].trim()),completedLocaleCount=computed(()=>localeFields.filter(localeComplete).length);
const moduleLabel=(v:OrderModuleConfig)=>localizedModule(v, 'moduleName', locale.value),descriptionLabel=(v:OrderModuleConfig)=>localizedModule(v, 'moduleDescription', locale.value),typeLabel=(v:OrderType)=>t(`ordersPage.types.${v}`),isAudited=(v:OrderType)=>['RANK_GUARANTEE','CHART_RANK_GUARANTEE','KEYWORD_COVERAGE'].includes(v),money=(v:string|number|null)=>Number(v||0).toFixed(4);
function applyDescriptionDefaults(force=false){localeFields.forEach(field=>{const messages=localeMessages[field.code] as{promotionPage?:{descriptions?:Partial<Record<OrderType,string>>}},defaultValue=messages.promotionPage?.descriptions?.[form.orderType];if(defaultValue&&(force||!form[field.descriptionKey].trim())){form[field.descriptionKey]=defaultValue;if(field.code!==activeLocale.value)autoGenerated[field.descriptionKey]=defaultValue}})}
function scheduleAutoTranslation(){translationError.value=false;if(translationTimer)clearTimeout(translationTimer);translationTimer=setTimeout(()=>void autoFillTranslations(),900)}
async function autoFillTranslations(force=false){if(editingId.value!==null)return;if(translationTimer){clearTimeout(translationTimer);translationTimer=undefined}const sourceField=activeLocaleField.value,name=form[sourceField.nameKey].trim(),description=form[sourceField.descriptionKey].trim();if(!name&&!description)return;const signature=`${sourceField.code}\u0000${name}\u0000${description}`;if(!force&&signature===lastTranslationSource.value)return;const sequence=++translationSequence.value;translating.value=true;translationError.value=false;try{const translations=await translateOrderModule({sourceLocale:sourceField.code,moduleName:name,moduleDescription:description});if(sequence!==translationSequence.value)return;localeFields.forEach(field=>{const translated=translations[field.code];if(!translated||field.code===sourceField.code)return;if(translated.name&&(!form[field.nameKey].trim()||form[field.nameKey]===autoGenerated[field.nameKey])){form[field.nameKey]=translated.name;autoGenerated[field.nameKey]=translated.name}if(translated.description&&(!form[field.descriptionKey].trim()||form[field.descriptionKey]===autoGenerated[field.descriptionKey])){form[field.descriptionKey]=translated.description;autoGenerated[field.descriptionKey]=translated.description}});lastTranslationSource.value=signature}catch{lastTranslationSource.value='';translationError.value=true}finally{if(sequence===translationSequence.value)translating.value=false}}
function openRegionPricing(){void router.push({name:'admin-region-pricing'})}
async function load(){loading.value=true;
try{modules.value=await getAdminOrderModules()}catch{ElMessage.error(t('orderModulesAdmin.loadFailed'))}finally{loading.value=false}}function openCreate(){editingId.value=null;Object.keys(autoGenerated).forEach(key=>delete autoGenerated[key]);lastTranslationSource.value='';translationError.value=false;translationSequence.value++;activeLocale.value=locale.value;
Object.assign(form,{storeTypes:['APP_STORE','GOOGLE_PLAY','IPAD_STORE'],moduleName:'',moduleNameEn:'',moduleNameRu:'',moduleNamePt:'',moduleNameEs:'',moduleDescription:'',moduleDescriptionEn:'',moduleDescriptionRu:'',moduleDescriptionPt:'',moduleDescriptionEs:'',orderType:'KEYWORD_INSTALL',unitPrice:0,chinaUnitPrice:0,enabled:true,sortOrder:modules.value.length*10+10});
applyDescriptionDefaults();
visible.value=true}function openEdit(v:OrderModuleConfig){editingId.value=v.id;Object.keys(autoGenerated).forEach(key=>delete autoGenerated[key]);lastTranslationSource.value='';translationSequence.value++;
Object.assign(form,{...v,storeTypes:[...v.storeTypes],unitPrice:Number(v.unitPrice),chinaUnitPrice:Number(v.chinaUnitPrice)});
applyDescriptionDefaults();activeLocale.value=locale.value;visible.value=true}async function save(){if(!form.storeTypes.length)return ElMessage.warning(t('pricing.storeRequired'));const missingName=localeFields.find(v=>!form[v.nameKey].trim());if(missingName){activeLocale.value=missingName.code;return ElMessage.warning(t('orderModulesAdmin.nameRequired'))}const missingDescription=localeFields.find(v=>!form[v.descriptionKey].trim());if(missingDescription){activeLocale.value=missingDescription.code;return ElMessage.warning(t('orderModulesAdmin.descriptionRequired'))}if(!isAudited(form.orderType)&&(Number(form.unitPrice)<=0||Number(form.chinaUnitPrice)<=0))return ElMessage.warning(t('orderModulesAdmin.priceRequired'));
saving.value=true;
try{const saved=editingId.value?await updateOrderModule(editingId.value,{...form}):await createOrderModule({...form});
if(!editingId.value)void router.push({name:'admin-region-pricing',query:{moduleId:saved.id}});
visible.value=false;
await load();
ElMessage.success(t('orderModulesAdmin.saveSuccess'))}catch{ElMessage.error(t('orderModulesAdmin.saveFailed'))}finally{saving.value=false}}async function toggle(v:OrderModuleConfig,enabled:unknown){try{await updateOrderModule(v.id,{...v,enabled:Boolean(enabled)});
await load()}catch{ElMessage.error(t('orderModulesAdmin.statusFailed'))}}async function remove(v:OrderModuleConfig){try{await ElMessageBox.confirm(t('orderModulesAdmin.deleteConfirm',{name:v.moduleName}),t('orderModulesAdmin.deleteTitle'),{type:'warning'});
await deleteOrderModule(v.id);
await load()}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(t('orderModulesAdmin.deleteFailed'))}};
onMounted(load);
</script>
<style scoped>
.module-stores{display:flex;gap:6px;flex-wrap:wrap;margin-top:12px}
.modules-page{color:#0f172a}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:18px;padding:18px 20px;border:1px solid #dfe7f1;border-radius:12px;background:#fff;box-shadow:0 8px 24px rgb(15 23 42/5%)}.toolbar h2{margin:0;font-size:20px}.toolbar p{margin:6px 0 0;color:#64748b}.module-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;min-height:220px}.module-card{display:flex;min-height:280px;flex-direction:column;padding:18px;border:1px solid #dfe7f1;border-radius:12px;background:#fff;box-shadow:0 10px 28px rgb(15 23 42/6%)}.module-card.disabled{opacity:.65}.card-head,.card-foot{display:flex;align-items:center;justify-content:space-between;gap:12px}.type{color:#2563eb;font-size:12px;font-weight:700}.card-head h3{margin:6px 0 0;font-size:17px}.module-description{display:-webkit-box;overflow:hidden;min-height:42px;margin:14px 0 0;color:#64748b;font-size:13px;line-height:1.6;-webkit-box-orient:vertical;-webkit-line-clamp:2}.audit-price{margin:18px 0;padding:17px;border-radius:9px;background:#fffbeb;color:#92400e;font-weight:700;text-align:center}.prices{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin:18px 0}.prices div{padding:12px;border-radius:9px;background:#f8fafc}.prices .china{background:#fff7ed}.prices span{display:block;margin-bottom:7px;color:#64748b;font-size:12px}.prices strong{font-size:18px}.card-foot{margin-top:auto;padding-top:13px;border-top:1px solid #edf1f6;color:#94a3b8;font-size:12px}
.module-form{display:grid;grid-template-columns:minmax(0,1.55fr) minmax(280px,.85fr);gap:18px;align-items:start}.form-section{padding:20px;border:1px solid #e2e8f0;border-radius:12px;background:#fff}.language-section{padding-bottom:8px}.section-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:10px}.section-heading h3,.settings-section>h3{margin:0;color:#0f172a;font-size:15px}.section-heading p{margin:4px 0 0;color:#64748b;font-size:12px}.section-actions{display:flex;align-items:center;justify-content:flex-end;gap:8px}.translation-error{max-width:190px;color:#dc2626;font-size:12px;line-height:1.3}.completion{padding:4px 9px;border-radius:999px;background:#eff6ff;color:#2563eb;font-size:12px;font-weight:700}.locale-tabs{margin-bottom:12px}.locale-tabs :deep(.el-tabs__header){margin-bottom:0}.locale-tab-label{display:inline-flex;align-items:center;justify-content:center;gap:6px}.locale-tab-label i{width:6px;height:6px;border-radius:50%;background:#cbd5e1}.locale-tab-label i.complete{background:#22c55e}.locale-fields{padding-top:16px}.locale-fields :deep(.el-form-item:last-child){margin-bottom:10px}.settings-section>h3{margin-bottom:16px}.settings-section .form-prices{grid-template-columns:1fr}.form-prices{display:grid;grid-template-columns:1fr 1fr;gap:12px}.form-prices :deep(.el-input-number){width:100%}.compact-fields :deep(.el-form-item){margin-bottom:14px}.switch-field{display:flex;min-height:32px;align-items:center}.dialog-footer{display:flex;justify-content:flex-end;gap:8px}
:global(.module-dialog .el-dialog__header){margin:0;padding:20px 24px 16px;border-bottom:1px solid #e8edf4}:global(.module-dialog .el-dialog__body){padding:20px 24px}:global(.module-dialog .el-dialog__footer){padding:14px 24px 18px;border-top:1px solid #e8edf4;background:#fff}
@media(max-width:1000px){.module-grid{grid-template-columns:repeat(2,1fr)}.module-form{grid-template-columns:1fr}}@media(max-width:650px){.module-grid,.form-prices{grid-template-columns:1fr}.toolbar{align-items:flex-start;gap:12px}}
</style>
