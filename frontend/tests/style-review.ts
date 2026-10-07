import { View, Check, Edit, Delete, MoreFilled } from '@element-plus/icons-vue'
import { createApp, h } from 'vue'
import { ElButton, ElTable, ElTableColumn, ElMenu, ElMenuItem, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import 'element-plus/dist/index.css'
import '../src/styles/design-system.css'
import { i18n, supportedLocales } from '../src/i18n'
const app = createApp({ render() {
  return h('main', { style:'padding:24px' }, [h('h1', '多语言布局回归 · 实际组件与共享样式'), h('p','仅为样式验证样例，不连接业务接口。'), ...supportedLocales.map(locale => {
    const t = (key:string) => i18n.global.t(key, {}, {locale})
    return h('section', {style:'margin:24px 0;padding:20px;background:white;border:1px solid #dbe4f0;border-radius:12px'}, [h('h2',locale),
      h('div', {style:'display:flex;gap:24px;flex-wrap:wrap'}, [
        h('aside', {style:'width:240px;background:#0b1324;border-radius:12px'}, [h(ElMenu,{class:'shell-menu',style:'height:auto;width:240px'},()=>['menu.pendingConfirmOrders','menu.homeMetricsConfig','menu.pendingExecutionOrders'].map(key=>h(ElMenuItem,{index:key},()=>[h('span',t(key))])))]),
        h('div', {style:'flex:1;min-width:0'}, [h(ElTable,{data:[{name:t('ordersPage.types.KEYWORD_INSTALL')}]},()=>[
          h(ElTableColumn,{prop:'name',label:t('ordersPage.taskType'),'min-width':160}),
          h(ElTableColumn,{label:t('ordersPage.actions'),width:locale==='zh-CN'?240:360}, {default:()=>h('div',{class:'order-row-actions pending-row-actions'},[
            h(ElButton,{size:'small',text:true,type:'primary',icon:View},()=>t('ordersPage.actionDetail')),
            h(ElButton,{size:'small',type:'primary',icon:Check},()=>t('ordersPage.actionConfirm')),
            h(ElDropdown,{trigger:'click'},{default:()=>h(ElButton,{size:'small',icon:MoreFilled},()=>t('ordersPage.actionMore')),dropdown:()=>h(ElDropdownMenu,{},()=>['editOrder','actionCancel'].map(key=>h(ElDropdownItem,{icon:key==='actionCancel'?Delete:Edit,class:key==='actionCancel'?'danger-menu-item':''},()=>t('ordersPage.'+key))))})
          ])})
        ]),h('p','用户订单 · 190px / 窄屏 130px'),h('div',{style:'display:flex;gap:16px;flex-wrap:wrap'},[190,130].map(width=>h('div',{class:'order-row-actions',style:`width:${width-24}px;border:1px solid #dbe4f0;padding:12px;box-sizing:content-box`},['actionDetail','actionPayEdit','editOrder','actionRenew'].map(key=>h(ElButton,{size:'small',text:true,type:'primary'},()=>t('ordersPage.'+key)))))),h('p','地区操作 · 140px'),h('div',{style:'width:140px;box-sizing:border-box;padding:12px;border:1px solid #dbe4f0'},[h(ElButton,{size:'small',type:'primary',plain:true},()=>t('common.edit'))])])
      ])])
  })])
}})
app.use(i18n).mount('#app')
