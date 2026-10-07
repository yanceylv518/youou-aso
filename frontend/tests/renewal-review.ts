import { createApp, h, nextTick, ref } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory, RouterView } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '../src/styles/design-system.css'
import { i18n } from '../src/i18n'
import { http } from '../src/api/http'
import OrderCreate from '../src/views/user/OrderCreateView.vue'

// Isolated fixtures: real creation component, no real account or business writes.
const types = ['KEYWORD_INSTALL','DOWNLOAD','RATING','REVIEW','RANK_GUARANTEE','CHART_RANK_GUARANTEE','KEYWORD_COVERAGE','KEYWORD_INSTALL']
const names = ['关键词安装','下载量','星级评分','用户评价','关键词保排名','榜单保排名','关键词覆盖','关键词安装（高级）']
const modules = types.map((orderType, index) => ({ id: index + 1, orderType, moduleName: names[index], enabled: true, sortOrder:index, storeTypes:['APP_STORE'], unitPrice:1, chinaUnitPrice:1 }))
const special = (type:string) => ['RANK_GUARANTEE','CHART_RANK_GUARANTEE','KEYWORD_COVERAGE'].includes(type)
const makeOrder = (id:number) => {
  const module = modules[(id-1)%8]
  return { id, orderNo:`TEST-${id}`, orderType:module.orderType, orderModuleId:id===18?999:id>8?null:module.id, orderModuleName:id===17?null:module.moduleName,
    status:'COMPLETED', sourceAuditId:special(module.orderType)?id:null, storeType:'APP_STORE', customerAppId:1, regionCode:'US',
    orderStartDate:'2026-01-01', orderEndDate:'2026-01-01', executionHours:1, totalDays:1,
    items:[{itemName:'保留的关键词',itemType:module.orderType==='RATING'?'RATING_5':module.orderType==='REVIEW'?'REVIEW_5':module.orderType,regionCode:'US',quantity:10}], events:[], commentDetails:[] }
}
const audits = Array.from({length:16},(_,i)=>makeOrder(i+1)).filter(o=>o.sourceAuditId).map(o=>({ ...o, orderModuleId:o.orderModuleId, items:[{regionCode:'US',keyword:'保留的关键词',chartType:'top',targetRank:3,coverageNote:''}] }))
http.defaults.adapter = async config => {
  if (config.method !== 'get') throw new Error('Regression fixture forbids writes')
  const url = config.url || ''
  let data:unknown = []
  if (url === '/customer/order-modules') data = modules
  else if (url === '/customer/apps') data = [{id:1,storeType:'APP_STORE',appName:'续单测试应用',appIdentifier:'test.fixture',regionCode:'US',regionCodes:['US']}]
  else if (url === '/regions/enabled') data = [{code:'US',nameZh:'美国',nameEn:'United States',supportsAppStore:true,supportsGooglePlay:true,supportsIpadStore:true}]
  else if (url === '/customer/pricing/regions') data = modules.map(m=>({orderModuleId:m.id,orderType:m.orderType,allowedRegionCodes:['US'],regionPrices:{}}))
  else if (url === '/customer/special-order-audits') data = audits
  else if (/^\/customer\/orders\/\d+$/.test(url)) data = makeOrder(Number(url.split('/').pop()))
  else if (url !== '/customer/pricing' && !url.includes('attachments')) throw new Error('Unexpected fixture request: '+url)
  return {config,status:200,statusText:'OK',headers:{},data:{success:true,data}}
}
const router = createRouter({history:createMemoryHistory(),routes:[{path:'/',component:OrderCreate,name:'user-order-create'}]})
const results = ref<string[]>([])
const running = ref(false)
async function runAll() {
  running.value = true; results.value = []
  for (let id=1;id<=18;id++) {
    await router.push({path:'/',query:{renewOrderId:String(id)}})
    const expected=id>16?undefined:names[(id-1)%8]
    for(let attempt=0;attempt<60;attempt++) {
      await new Promise(resolve=>setTimeout(resolve,50)); await nextTick()
      const selected=document.querySelector('[role="tab"][aria-selected="true"]')?.textContent?.trim()
      const heading=document.querySelector('.selected-module-card strong')?.textContent?.trim()
      if(selected===expected && heading===expected) break
    }
    // Observe after watchers settle, not only when the initial route selection appears.
    await new Promise(resolve=>setTimeout(resolve,150)); await nextTick()
    const selected=document.querySelector('[role="tab"][aria-selected="true"]')?.textContent?.trim()
    const heading=document.querySelector('.selected-module-card strong')?.textContent?.trim()
    if(id>16) {
      const warning=document.querySelector('.el-alert--warning')?.textContent?.includes('请重新选择服务类型')
      const submit=Array.from(document.querySelectorAll('button')).find(button=>button.textContent?.trim()==='提交订单')
      results.value.push(`${!selected && !heading && warning && submit?.disabled?'PASS':'FAIL'} ${id===17?'旧订单类型不明确':'原服务已停用'}：明确提示重新选择，禁止误提交`)
      continue
    }
    const keyword=Array.from(document.querySelectorAll('input')).some(input=>input.value==='保留的关键词')
    const needsKeyword=['KEYWORD_INSTALL','RANK_GUARANTEE','KEYWORD_COVERAGE'].includes(types[(id-1)%8])
    results.value.push(`${selected===expected && heading===expected && (!needsKeyword||keyword)?'PASS':'FAIL'} ${id>8?'旧订单':'原模块'} ${expected}：选中 ${selected||'无'}，回填 ${heading||'无'}${needsKeyword?`，关键词${keyword?'保留':'丢失'}`:''}`)
  }
  running.value = false
}
i18n.global.locale.value='zh-CN'
await router.push({path:'/',query:{renewOrderId:'1'}})
createApp({setup:()=>()=>h('div',[
  h('section',{style:'background:#fff;padding:20px;position:relative;z-index:1'},[h('h1','普通 / 特殊续单服务选择回归'),h('p','真实创建页组件，隔离测试数据，不连接业务接口。'),h('button',{onClick:runAll,disabled:running.value},running.value?'检查中':'检查全部 18 场景'),...results.value.map(text=>h('p',text))]),
  h(RouterView)
])}).use(createPinia()).use(router).use(i18n).use(ElementPlus).mount('#app')
