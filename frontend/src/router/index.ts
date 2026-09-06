import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/public/HomeView.vue') },
        { path: 'login', name: 'login', component: () => import('@/views/public/LoginView.vue') },
        { path: 'register', name: 'register', component: () => import('@/views/public/RegisterView.vue') },
        { path: 'forgot-password', name: 'forgot-password', component: () => import('@/views/public/ForgotPasswordView.vue') }
      ]
    },
    {
      path: '/user',
      component: () => import('@/layouts/UserLayout.vue'),
      meta: { requiresAuth: true, accountType: 'CUSTOMER' },
      children: [
        { path: 'dashboard', name: 'user-dashboard', component: () => import('@/views/user/DashboardView.vue'), meta: { titleKey: 'menu.home', menuCode: 'dashboard' } },
        { path: 'promotion', name: 'user-promotion', component: () => import('@/views/user/DynamicPromotionView.vue'), meta: { titleKey: 'menu.promotion', menuCode: 'promotion' } },
        { path: 'applications', name: 'user-applications', component: () => import('@/views/user/ApplicationManagementView.vue'), meta: { titleKey: 'menu.applications', menuCode: 'applications' } },
        { path: 'orders/create', name: 'user-order-create', component: () => import('@/views/user/OrderCreateView.vue'), meta: { titleKey: 'orderCreate.title', activeMenu: '/user/promotion' } },
        { path: 'orders/:id', name: 'user-order-detail', component: () => import('@/views/user/OrderDetailView.vue'), meta: { titleKey: 'orderDetail.title', topTitleKey: 'menu.orders', menuCode: 'orders' } },
        { path: 'special-order-audits/:id', name: 'user-special-order-audit-detail', component: () => import('@/views/user/SpecialOrderAuditDetailView.vue'), meta: { titleKey: 'orderDetail.title', topTitleKey: 'menu.orders', menuCode: 'orders' } },
        { path: 'orders/apple', name: 'user-orders-apple', component: () => import('@/views/user/StoreOrdersView.vue'), meta: { storeType: 'APP_STORE', titleKey: 'menu.appleOrders', topTitleKey: 'menu.orders', menuCode: 'orders.apple' } },
        { path: 'orders/google', name: 'user-orders-google', component: () => import('@/views/user/StoreOrdersView.vue'), meta: { storeType: 'GOOGLE_PLAY', titleKey: 'menu.googleOrders', topTitleKey: 'menu.orders', menuCode: 'orders.google' } },
        { path: 'orders/ipad', name: 'user-orders-ipad', component: () => import('@/views/user/StoreOrdersView.vue'), meta: { storeType: 'IPAD_STORE', titleKey: 'menu.ipadOrders', topTitleKey: 'menu.orders', menuCode: 'orders.ipad' } },
        { path: 'orders/special', redirect: '/user/orders/apple' },
        { path: 'consumption-records', name: 'user-consumption-records', component: () => import('@/views/user/ConsumptionRecordsView.vue'), meta: { titleKey: 'menu.consumptionRecords' } },
        { path: 'transactions', name: 'user-transactions', component: () => import('@/views/user/TransactionsView.vue'), meta: { titleKey: 'menu.transactions' } },
        { path: 'settings', name: 'user-settings', component: () => import('@/views/user/AccountSettingsView.vue'), meta: { titleKey: 'menu.settings' } }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, accountType: 'ADMIN' },
      children: [
        { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { titleKey: 'menu.home', menuCode: 'dashboard' } },
        { path: 'customers', name: 'admin-customers', component: () => import('@/views/admin/CustomerManagementView.vue'), meta: { titleKey: 'menu.customers', menuCode: 'customers' } },
        { path: 'promotion', name: 'admin-promotion', component: () => import('@/views/admin/DynamicPromotionView.vue'), meta: { titleKey: 'menu.promotion', menuCode: 'promotion' } },
        { path: 'orders/create', name: 'admin-order-create', component: () => import('@/views/admin/OrderCreateView.vue'), meta: { titleKey: 'orderCreate.adminTitle', activeMenu: '/admin/promotion', menuCode: 'promotion' } },
        { path: 'orders/:id', name: 'admin-order-detail', component: () => import('@/views/admin/OrderDetailView.vue'), meta: { titleKey: 'orderDetail.title', topTitleKey: 'menu.orders', menuCode: 'orders' } },
        { path: 'special-order-audits/:id', name: 'admin-special-order-audit-detail', component: () => import('@/views/user/SpecialOrderAuditDetailView.vue'), meta: { titleKey: 'orderDetail.title', topTitleKey: 'menu.orderExecution', menuCode: 'orders.pendingReview' } },
        { path: 'applications', name: 'admin-applications', component: () => import('@/views/admin/ApplicationManagementView.vue'), meta: { titleKey: 'menu.applications', menuCode: 'applications' } },
        { path: 'pending-review-orders', name: 'admin-pending-review-orders', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { statusFilter: 'PENDING_REVIEW', titleKey: 'menu.pendingReviewOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orders.pendingReview' } },
        { path: 'pending-confirm-orders', name: 'admin-pending-confirm-orders', component: () => import('@/views/admin/PendingConfirmOrdersView.vue'), meta: { titleKey: 'menu.pendingConfirmOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orders.pendingConfirm' } },
        { path: 'orders/pending-execution', name: 'admin-orders-pending-execution', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { statusFilter: 'PENDING_EXECUTION', titleKey: 'menu.pendingExecutionOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orderExecution.pending' } },
        { path: 'orders/executing', name: 'admin-orders-executing', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { statusFilter: 'EXECUTING', titleKey: 'menu.executingOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orderExecution.executing' } },
        { path: 'orders/paused', name: 'admin-orders-paused', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { statusFilter: 'PAUSED', titleKey: 'menu.pausedOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orderExecution.executing' } },
        { path: 'orders/completed', name: 'admin-orders-completed', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { statusFilter: 'COMPLETED', titleKey: 'menu.completedOrders', topTitleKey: 'menu.orderExecution', menuCode: 'orderExecution.completed' } },
        { path: 'orders/apple', name: 'admin-orders-apple', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { storeType: 'APP_STORE', titleKey: 'menu.appleOrders', topTitleKey: 'menu.orders', menuCode: 'orders.apple' } },
        { path: 'orders/google', name: 'admin-orders-google', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { storeType: 'GOOGLE_PLAY', titleKey: 'menu.googleOrders', topTitleKey: 'menu.orders', menuCode: 'orders.google' } },
        { path: 'orders/ipad', name: 'admin-orders-ipad', component: () => import('@/views/admin/StoreOrdersView.vue'), meta: { storeType: 'IPAD_STORE', titleKey: 'menu.ipadOrders', topTitleKey: 'menu.orders', menuCode: 'orders.ipad' } },
        { path: 'audits', redirect: '/admin/orders/apple' },
        { path: 'finance', name: 'admin-finance', component: () => import('@/views/admin/FinanceManagementView.vue'), meta: { titleKey: 'menu.financeTransactions', topTitleKey: 'menu.finance', activeMenu: '/admin/finance', menuCode: 'finance.transactions' } },
        { path: 'finance/recharges', name: 'admin-finance-recharges', component: () => import('@/views/admin/FinanceManagementView.vue'), meta: { titleKey: 'menu.rechargeRecords', topTitleKey: 'menu.finance', activeMenu: '/admin/finance/recharges', transactionType: 'ADMIN_RECHARGE', menuCode: 'finance.recharges' } },
        { path: 'home-metrics', name: 'admin-home-metrics', component: () => import('@/views/admin/HomeMetricsConfigView.vue'), meta: { titleKey: 'menu.homeMetricsConfig', topTitleKey: 'menu.systemManagement', menuCode: 'system.homeMetrics' } },
        { path: 'pricing', name: 'admin-pricing', component: () => import('@/views/admin/OrderModulesView.vue'), meta: { titleKey: 'menu.pricing', topTitleKey: 'menu.systemManagement', menuCode: 'system.pricing' } },
        { path: 'pricing/regions', name: 'admin-region-pricing', component: () => import('@/views/admin/PricingView.vue'), meta: { titleKey: 'pricing.regionConfigTitle', topTitleKey: 'menu.systemManagement', activeMenu: '/admin/pricing', menuCode: 'system.pricing' } },
        { path: 'wallet-transaction-types', name: 'admin-wallet-transaction-types', component: () => import('@/views/admin/WalletTransactionTypeConfigView.vue'), meta: { titleKey: 'menu.walletTransactionTypeConfig', topTitleKey: 'menu.systemManagement', menuCode: 'system.walletTypes' } },
        { path: 'customer-service', name: 'admin-customer-service', component: () => import('@/views/admin/CustomerServiceConfigView.vue'), meta: { titleKey: 'menu.customerServiceConfig', topTitleKey: 'menu.systemManagement', menuCode: 'system.customerService' } },
        { path: 'order-notification', name: 'admin-order-notification', component: () => import('@/views/admin/OrderNotificationConfigView.vue'), meta: { superAdminOnly: true, titleKey: 'menu.mailConfig', topTitleKey: 'menu.systemManagement', menuCode: 'system.mail' } },
        { path: 'regions', name: 'admin-regions', component: () => import('@/views/admin/RegionView.vue'), meta: { titleKey: 'menu.regions', topTitleKey: 'menu.systemManagement', menuCode: 'system.regions' } },
        { path: 'admin-accounts', name: 'admin-accounts', component: () => import('@/views/admin/AdminAccountsView.vue'), meta: { superAdminOnly: true, titleKey: 'menu.adminAccounts', topTitleKey: 'menu.systemManagement', menuCode: 'system.adminAccounts' } },
        { path: 'roles', redirect: '/admin/admin-accounts' },
        { path: 'settings', name: 'admin-settings', component: () => import('@/views/admin/AccountSettingsView.vue'), meta: { titleKey: 'menu.settings', topTitleKey: 'menu.systemManagement', menuCode: 'system.settings' } }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.bootstrapped) {
    await auth.bootstrap()
  }

  if ((to.name === 'login' || to.name === 'register') && auth.isLoggedIn) {
    return auth.isAdmin ? { name: 'admin-dashboard' } : { name: 'user-dashboard' }
  }

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.accountType === 'CUSTOMER' && !auth.isCustomer) {
    return { name: 'login' }
  }
  if (to.meta.accountType === 'ADMIN' && !auth.isAdmin) {
    return { name: 'login' }
  }
  if (to.meta.superAdminOnly && !auth.isSuperAdmin) {
    return { name: 'admin-dashboard' }
  }
})

export default router
