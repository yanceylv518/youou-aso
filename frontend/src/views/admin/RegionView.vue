<template>
  <section class="region-page">
    <header class="toolbar">
      <p class="page-note">{{ t('regions.subtitle') }}</p>
      <el-button :icon="Refresh" :loading="loading" @click="loadRegions">
        {{ t('ordersPage.refresh') }}
      </el-button>
    </header>

    <el-alert class="notice" type="info" show-icon :closable="false">
      {{ t('regions.notice') }}
    </el-alert>

    <el-table v-loading="loading" class="region-table" :data="pagedRegions" :empty-text="t('regions.empty')">
      <el-table-column prop="code" :label="t('regions.code')" width="100" />
      <el-table-column prop="nameZh" :label="t('regions.nameZh')" min-width="160" />
      <el-table-column prop="nameEn" :label="t('regions.nameEn')" min-width="190" />
      <el-table-column :label="t('regions.enabled')" width="130" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" :loading="savingCode === row.code" @change="saveRegion(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="'App Store'" width="130" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.supportsAppStore" :loading="savingCode === row.code" @change="saveRegion(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="'Google Play'" width="140" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.supportsGooglePlay" :loading="savingCode === row.code" @change="saveRegion(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="'iPad Store'" width="130" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.supportsIpadStore" :loading="savingCode === row.code" @change="saveRegion(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" :label="t('regions.sortOrder')" width="110" align="right" />
    </el-table>
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="regions.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handlePageSizeChange"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getAdminRegions, updateAdminRegion, type AdminMarketRegion } from '@/api/regions'

const { t } = useI18n()
const loading = ref(false)
const savingCode = ref('')
const regions = ref<AdminMarketRegion[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20
})
const pagedRegions = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return regions.value.slice(start, start + pagination.pageSize)
})

onMounted(loadRegions)

async function loadRegions() {
  loading.value = true
  try {
    regions.value = await getAdminRegions()
    clampPage()
  } catch {
    ElMessage.error(t('regions.loadFailed'))
  } finally {
    loading.value = false
  }
}

function handlePageSizeChange() {
  pagination.page = 1
}

function clampPage() {
  const maxPage = Math.max(1, Math.ceil(regions.value.length / pagination.pageSize))
  if (pagination.page > maxPage) {
    pagination.page = maxPage
  }
}

async function saveRegion(row: AdminMarketRegion) {
  savingCode.value = row.code
  try {
    const updated = await updateAdminRegion(row.code, {
      enabled: row.enabled,
      supportsAppStore: row.supportsAppStore,
      supportsGooglePlay: row.supportsGooglePlay,
      supportsIpadStore: row.supportsIpadStore
    })
    const index = regions.value.findIndex((region) => region.code === updated.code)
    if (index >= 0) {
      regions.value[index] = updated
    }
    ElMessage.success(t('regions.saved'))
  } catch {
    ElMessage.error(t('regions.saveFailed'))
    await loadRegions()
  } finally {
    savingCode.value = ''
  }
}
</script>

<style scoped>
.region-page {
  color: #182230;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

p {
  letter-spacing: 0;
}

.page-note {
  max-width: 720px;
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.notice {
  margin-bottom: 18px;
}

.region-table {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.region-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
  font-weight: 600;
}

.region-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-top: 0;
  border-radius: 0 0 8px 8px;
  background: #ffffff;
}

@media (max-width: 760px) {
  .toolbar {
    flex-direction: column;
  }
}
</style>
