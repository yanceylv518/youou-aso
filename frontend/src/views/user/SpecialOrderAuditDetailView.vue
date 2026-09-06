<template>
  <section class="audit-detail-page">
    <div class="detail-toolbar">
      <el-button :icon="ArrowLeft" @click="router.back()">{{ t('orderDetail.back') }}</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="7" animated />
    <el-empty v-else-if="loadFailed" :description="t('specialAudit.loadFailed')" />
    <el-empty v-else-if="!audit" :description="t('specialAudit.empty')" />

    <template v-else>
      <section class="hero-card">
        <div class="app-icon">
          <img v-if="audit.appIconUrl" :src="audit.appIconUrl" :alt="audit.appName" />
          <span v-else>{{ audit.appName.slice(0, 1).toUpperCase() }}</span>
        </div>
        <div class="hero-copy">
          <span class="eyebrow">{{ t('specialAudit.auditNo') }} · {{ audit.auditNo }}</span>
          <h1>{{ audit.appName }}</h1>
          <p>{{ typeLabel(audit.orderType) }}</p>
        </div>
        <el-tag :type="statusType(audit.status)" effect="light">{{ t(`specialAudit.statuses.${audit.status}`) }}</el-tag>
      </section>

      <div class="detail-grid">
        <section class="detail-card">
          <h2>{{ t('orderDetail.basicInfo') }}</h2>
          <dl class="info-list">
            <div><dt>{{ t('ordersPage.store') }}</dt><dd>{{ storeLabel(audit.storeType) }}</dd></div>
            <div><dt>{{ t('ordersPage.region') }}</dt><dd>{{ audit.regionCode || '-' }}</dd></div>
            <div v-if="isAdmin"><dt>{{ t('orderDetail.customer') }}</dt><dd>{{ audit.customerId }}</dd></div>
            <div><dt>{{ t('ordersPage.createdAt') }}</dt><dd>{{ formatDateTime(audit.createdAt) }}</dd></div>
            <div><dt>{{ t('ordersPage.status') }}</dt><dd>{{ t(`specialAudit.statuses.${audit.status}`) }}</dd></div>
            <div><dt>{{ t('orderCreate.contactType') }}</dt><dd>{{ audit.contactType || '-' }}</dd></div>
            <div><dt>{{ t('orderCreate.contactValue') }}</dt><dd class="contact-value">{{ audit.contactValue || '-' }}</dd></div>
          </dl>
        </section>

        <section class="detail-card">
          <h2>{{ t('specialAudit.content') }}</h2>
          <div class="content-block">
            <span>{{ t('orderCreate.requestedContent') }}</span>
            <p>{{ audit.requestedContent || '-' }}</p>
          </div>
          <div v-if="audit.negotiatedContent" class="content-block">
            <span>{{ t('specialAudit.content') }}</span>
            <p>{{ audit.negotiatedContent }}</p>
          </div>
          <div v-if="audit.negotiatedPrice !== null" class="price-row">
            <span>{{ t('specialAudit.price') }}</span>
            <strong>${{ Number(audit.negotiatedPrice).toFixed(2) }}</strong>
          </div>
          <div v-if="audit.cancelReason" class="content-block danger">
            <span>{{ t('specialAudit.cancelReason') }}</span>
            <p>{{ audit.cancelReason }}</p>
          </div>
        </section>
      </div>

      <section v-if="audit.items?.length" class="detail-card">
        <h2>{{ t('orderDetail.itemDetails') }}</h2>
        <el-table :data="audit.items" class="detail-table">
          <el-table-column prop="regionCode" :label="t('ordersPage.region')" min-width="120" />
          <el-table-column :label="audit.orderType === 'CHART_RANK_GUARANTEE' ? t('orderCreate.chartType') : t('orderCreate.keyword')" min-width="220">
            <template #default="{ row }">{{ audit.orderType === 'CHART_RANK_GUARANTEE' ? (row.chartType || row.keyword || '-') : (row.keyword || '-') }}</template>
          </el-table-column>
          <el-table-column v-if="['RANK_GUARANTEE', 'CHART_RANK_GUARANTEE'].includes(audit.orderType)" :label="t('orderCreate.targetRank')" min-width="140">
            <template #default="{ row }">{{ row.targetRank ?? '-' }}</template>
          </el-table-column>
          <el-table-column v-if="['RANK_GUARANTEE', 'CHART_RANK_GUARANTEE'].includes(audit.orderType)" :label="t('orderCreate.unitPrice')" min-width="120" align="right">
            <template #default="{ row }">{{ row.unitPrice == null ? '-' : `$${Number(row.unitPrice).toFixed(2)}` }}</template>
          </el-table-column>
          <el-table-column v-if="['RANK_GUARANTEE', 'CHART_RANK_GUARANTEE'].includes(audit.orderType)" :label="t('orderCreate.executionDays')" min-width="100" align="right">
            <template #default="{ row }">{{ row.executionDays ?? '-' }}</template>
          </el-table-column>
          <el-table-column v-if="['RANK_GUARANTEE', 'CHART_RANK_GUARANTEE'].includes(audit.orderType)" :label="t('ordersPage.amount')" min-width="120" align="right">
            <template #default="{ row }">{{ row.amount == null ? '-' : `$${Number(row.amount).toFixed(2)}` }}</template>
          </el-table-column>
          <el-table-column v-if="audit.orderType === 'KEYWORD_COVERAGE'" :label="t('orderCreate.currentRank')" min-width="240">
            <template #default="{ row }">{{ row.coverageNote || '-' }}</template>
          </el-table-column>
        </el-table>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAdminSpecialAudit, getCustomerSpecialAudit, type SpecialAuditStatus, type SpecialOrderAudit } from '@/api/specialOrderAudits'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const loading = ref(false)
const audit = ref<SpecialOrderAudit | null>(null)
const loadFailed = ref(false)
const isAdmin = computed(() => route.name === 'admin-special-order-audit-detail')

onMounted(async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    audit.value = isAdmin.value ? await getAdminSpecialAudit(id) : await getCustomerSpecialAudit(id)
  } catch {
    loadFailed.value = true
    ElMessage.error(t('specialAudit.loadFailed'))
  } finally {
    loading.value = false
  }
})

function typeLabel(type: string) {
  return t(`ordersPage.types.${type}`)
}

function storeLabel(store: string) {
  if (store === 'GOOGLE_PLAY') return 'Google Play'
  if (store === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
}

function statusType(status: SpecialAuditStatus) {
  if (status === 'CANCELLED') return 'danger'
  if (status === 'APPROVED_WAIT_SUBMIT') return 'warning'
  if (status === 'SUBMITTED') return 'success'
  return 'info'
}

function formatDateTime(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.audit-detail-page { display: grid; gap: 18px; }
.detail-toolbar { display: flex; justify-content: space-between; }
.hero-card, .detail-card { background: #fff; border: 1px solid #dfe7f2; border-radius: 14px; box-shadow: 0 12px 30px rgba(36, 64, 104, .07); }
.hero-card { display: flex; align-items: center; gap: 16px; padding: 24px; }
.app-icon { width: 58px; height: 58px; flex: 0 0 58px; border-radius: 14px; display: grid; place-items: center; overflow: hidden; color: #2468f2; font-size: 22px; font-weight: 700; background: #eef5ff; }
.app-icon img { width: 100%; height: 100%; object-fit: cover; }
.hero-copy { min-width: 0; flex: 1; }
.eyebrow { color: #67809f; font-size: 13px; }
.hero-copy h1 { margin: 5px 0 3px; color: #142033; font-size: 24px; }
.hero-copy p { margin: 0; color: #5d708d; }
.detail-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 18px; }
.detail-card { padding: 22px; }
.detail-card h2 { margin: 0 0 18px; color: #172238; font-size: 18px; }
.info-list { margin: 0; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.info-list div { padding: 14px 0; border-bottom: 1px solid #edf1f6; }
.info-list dt, .content-block span, .price-row span { color: #73839c; font-size: 13px; }
.info-list dd { margin: 6px 0 0; color: #172238; font-weight: 600; }
.contact-value { overflow-wrap: anywhere; user-select: text; }
.content-block { padding: 14px; border-radius: 10px; background: #f7f9fc; }
.content-block + .content-block, .content-block + .price-row { margin-top: 12px; }
.content-block p { margin: 7px 0 0; color: #344760; line-height: 1.65; white-space: pre-wrap; }
.content-block.danger { background: #fff4f3; }
.price-row { display: flex; justify-content: space-between; align-items: center; padding: 14px; border-radius: 10px; background: #f1f7ff; }
.price-row strong { color: #1765e8; font-size: 20px; }
.detail-table { border-radius: 10px; overflow: hidden; }
@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } .info-list { grid-template-columns: 1fr; } }
</style>
