<template>
  <section v-loading="loading" class="metrics-config-page">
    <header class="page-intro">
      <div>
        <h2>{{ t('homeMetricsConfig.title') }}</h2>
        <p>{{ t('homeMetricsConfig.subtitle') }}</p>
      </div>
      <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
    </header>

    <div class="metric-grid">
      <article class="metric-card blue">
        <span class="metric-index">01</span>
        <strong>{{ form.appsValue || '-' }}</strong>
        <h3>{{ t('home.metrics.apps') }}</h3>
        <p>{{ t('home.metrics.appsNote') }}</p>
        <el-input v-model.trim="form.appsValue" maxlength="32" :placeholder="t('homeMetricsConfig.valuePlaceholder')" />
      </article>
      <article class="metric-card green">
        <span class="metric-index">02</span>
        <strong>{{ form.satisfactionValue || '-' }}</strong>
        <h3>{{ t('home.metrics.satisfaction') }}</h3>
        <p>{{ t('home.metrics.satisfactionNote') }}</p>
        <el-input v-model.trim="form.satisfactionValue" maxlength="32" :placeholder="t('homeMetricsConfig.valuePlaceholder')" />
      </article>
      <article class="metric-card purple">
        <span class="metric-index">03</span>
        <strong>{{ t('home.metrics.yearsValue', { count: form.experienceYears }) }}</strong>
        <h3>{{ t('home.metrics.experience') }}</h3>
        <p>{{ t('home.metrics.experienceNote') }}</p>
        <el-input-number v-model="form.experienceYears" :min="0" :max="999" controls-position="right" />
      </article>
      <article class="metric-card yellow">
        <span class="metric-index">04</span>
        <strong>{{ form.teamValue || '-' }}</strong>
        <h3>{{ t('home.metrics.team') }}</h3>
        <p>{{ t('home.metrics.teamNote') }}</p>
        <el-input v-model.trim="form.teamValue" maxlength="32" :placeholder="t('homeMetricsConfig.valuePlaceholder')" />
      </article>
    </div>

    <footer class="config-footer">
      <span>{{ t('homeMetricsConfig.updatedAt') }}：{{ formatDate(updatedAt) }}</span>
      <span>{{ t('homeMetricsConfig.previewTip') }}</span>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getAdminHomeMetrics, updateAdminHomeMetrics } from '@/api/support'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const updatedAt = ref<string | null>(null)
const form = reactive({ appsValue: '10,000+', satisfactionValue: '98.6%', experienceYears: 5, teamValue: '50+' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    apply(await getAdminHomeMetrics())
  } catch {
    ElMessage.error(t('homeMetricsConfig.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.appsValue || !form.satisfactionValue || !form.teamValue) {
    ElMessage.warning(t('homeMetricsConfig.required'))
    return
  }
  saving.value = true
  try {
    apply(await updateAdminHomeMetrics({ ...form }))
    ElMessage.success(t('homeMetricsConfig.saved'))
  } catch {
    ElMessage.error(t('homeMetricsConfig.saveFailed'))
  } finally {
    saving.value = false
  }
}

function apply(value: { appsValue: string; satisfactionValue: string; experienceYears: number; teamValue: string; updatedAt: string | null }) {
  form.appsValue = value.appsValue
  form.satisfactionValue = value.satisfactionValue
  form.experienceYears = value.experienceYears
  form.teamValue = value.teamValue
  updatedAt.value = value.updatedAt
}

function formatDate(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.metrics-config-page { color: #0f172a; }
.page-intro { display:flex; align-items:center; justify-content:space-between; gap:20px; margin-bottom:18px; padding:20px 22px; border:1px solid #e2e8f0; border-radius:10px; background:#fff; box-shadow:0 12px 30px rgb(15 23 42 / 5%); }
.page-intro h2 { margin:0 0 6px; font-size:18px; }
.page-intro p { margin:0; color:#64748b; }
.metric-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:16px; }
.metric-card { position:relative; overflow:hidden; padding:22px; border:1px solid #e2e8f0; border-radius:10px; background:#fff; box-shadow:0 12px 30px rgb(15 23 42 / 5%); }
.metric-card::before { position:absolute; inset:0 0 auto; height:4px; background:var(--accent); content:""; }
.metric-card.blue { --accent:#2563eb; } .metric-card.green { --accent:#16a34a; } .metric-card.purple { --accent:#7c3aed; } .metric-card.yellow { --accent:#d97706; }
.metric-index { position:absolute; top:20px; right:22px; color:#cbd5e1; font-size:12px; font-weight:800; }
.metric-card strong { display:block; margin-bottom:4px; color:var(--accent); font-size:32px; line-height:1.2; }
.metric-card h3 { margin:0 0 6px; font-size:16px; } .metric-card p { min-height:20px; margin:0 0 18px; color:#64748b; font-size:13px; }
.metric-card :deep(.el-input-number) { width:100%; }
.config-footer { display:flex; justify-content:space-between; gap:16px; margin-top:16px; padding:14px 18px; border:1px solid #e2e8f0; border-radius:8px; background:#f8fafc; color:#64748b; font-size:13px; }
@media (max-width: 800px) { .metric-grid { grid-template-columns:1fr; } .config-footer { flex-direction:column; } }
</style>
