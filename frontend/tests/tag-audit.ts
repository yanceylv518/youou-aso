import {createApp,h} from 'vue'
import {ElTable,ElTableColumn,ElTag} from 'element-plus'
import 'element-plus/dist/index.css'
import '../src/styles/design-system.css'
import {i18n,supportedLocales} from '../src/i18n'
const groups=[['首页订单状态',130,'ordersPage.statuses'],['首页待审核需求',120,'specialAudit.statuses'],['订单明细完成状态',120,'ordersPage.statuses'],['财务收支方向',110,'wallet.directions']]
createApp({render(){return h('main',{style:'padding:24px'},[h('h1','首页状态标签修复验证'),h('section',{style:'width:240px;padding:20px;box-sizing:border-box;background:white'},[h('h2','长邮箱验证'),h(ElTag,{class:'wrapping-tag'},()=> 'orders.notifications.for.multilingual.layout.verification@example-mail-service.test')]),...supportedLocales.flatMap(locale=>groups.map(([name,width,path])=>{const msg=i18n.global.getLocaleMessage(locale);const labels=String(path).split('.').reduce((v,k)=>v[k],msg);const rows=Object.keys(labels).filter(key=>name!=='订单明细完成状态'||key==='COMPLETED').map(key=>({text:i18n.global.t(`${path}.${key}`,{},{locale})}));return h('section',{style:'width:420px;display:inline-block;vertical-align:top;margin:12px'},[h('h2',`${locale} ${name}`),h(ElTable,{data:rows},()=>[h(ElTableColumn,{label:'原状态列',width:Number(width)},{default:({row})=>h(ElTag,{class:String(name).startsWith('首页')?'order-status-tag':''},()=>row.text)}),h(ElTableColumn,{prop:'text',label:'完整文案'})])])}))])}}).use(i18n).mount('#app')
