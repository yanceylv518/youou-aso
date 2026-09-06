<template>
  <section class="region-page">
    <header class="toolbar">
      <div class="toolbar-copy">
        <p class="page-note">{{ t('regions.subtitle') }}</p>
        <p class="toolbar-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ t('regions.notice') }}</span>
        </p>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">{{ t('regions.add') }}</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="loadRegions">
        {{ t('ordersPage.refresh') }}
      </el-button>
      </div>
    </header>

    <section class="search-panel" :aria-label="t('regions.searchPlaceholder')">
      <div class="search-control">
        <el-input
          v-model="searchQuery"
          :prefix-icon="Search"
          clearable
          :placeholder="t('regions.searchPlaceholder')"
        />
        <span class="search-tip">{{ t('regions.searchTip') }}</span>
      </div>
      <span class="search-result">{{ t('regions.resultCount', { count: filteredRegions.length }) }}</span>
    </section>

    <el-table v-loading="loading" class="region-table" :data="pagedRegions" :empty-text="t('regions.empty')">
      <el-table-column prop="code" :label="t('regions.code')" width="100" />
      <el-table-column v-for="field in localeNameFields" :key="field.key" :prop="field.key" :label="field.label" min-width="160" />
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
      <el-table-column :label="t('ordersPage.actions')" width="100" fixed="right" align="center">
        <template #default="{ row }"><el-button class="edit-action" plain type="primary" size="small" @click="openEdit(row)">{{ t('common.edit') }}</el-button></template>
      </el-table-column>
    </el-table>
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="filteredRegions.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog append-to-body v-model="dialogVisible" :title="editingCode ? t('regions.edit') : t('regions.add')" width="520px">
      <el-form label-width="110px">
        <el-form-item :label="t('regions.code')"><el-input v-model="form.code" maxlength="2" /></el-form-item>
        <el-form-item v-for="field in localeNameFields" :key="field.key" :label="field.label"><el-input v-model="form[field.key]" /></el-form-item>
        <el-form-item :label="t('regions.sortOrder')"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item :label="t('regions.enabled')"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item label="App Store"><el-switch v-model="form.supportsAppStore" /></el-form-item>
        <el-form-item label="Google Play"><el-switch v-model="form.supportsGooglePlay" /></el-form-item>
        <el-form-item label="iPad Store"><el-switch v-model="form.supportsIpadStore" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="dialogSaving" @click="submitRegion">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { InfoFilled, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createAdminRegion, getAdminRegions, updateAdminRegion, type AdminMarketRegion } from '@/api/regions'
import { localeOptions } from '@/i18n'

const { t } = useI18n()
const loading = ref(false)
const savingCode = ref('')
const regions = ref<AdminMarketRegion[]>([])
const dialogVisible = ref(false)
const dialogSaving = ref(false)
const editingCode = ref('')
const searchQuery = ref('')
const form = reactive({ code: '', nameZh: '', nameEn: '', nameRu: '', namePt: '', nameEs: '', enabled: true, supportsAppStore: true, supportsGooglePlay: true, supportsIpadStore: true, sortOrder: 0 })
const nameFieldMap = { 'zh-CN':'nameZh', 'en-US':'nameEn', 'ru-RU':'nameRu', 'pt-PT':'namePt', 'es-ES':'nameEs' } as const
const localeNameFields = localeOptions.map((option) => ({ key: nameFieldMap[option.code], label: option.nativeLabel }))
const pagination = reactive({
  page: 1,
  pageSize: 20
})
const filteredRegions = computed(() => {
  const keyword = normalizeSearchText(searchQuery.value)
  if (!keyword) return regions.value
  return regions.value
    .map((region, index) => {
      const code = normalizeSearchText(region.code)
      const names = [region.nameZh, region.nameEn, region.nameRu, region.namePt, region.nameEs].map(normalizeSearchText)
      const rank = code === keyword ? 0 : code.startsWith(keyword) ? 1 : names.some((name) => name.startsWith(keyword)) ? 2 : 3
      return { region, index, rank, matched: rank < 3 || names.some((name) => name.includes(keyword)) }
    })
    .filter((item) => item.matched)
    .sort((left, right) => left.rank - right.rank || left.index - right.index)
    .map((item) => item.region)
})
const pagedRegions = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredRegions.value.slice(start, start + pagination.pageSize)
})

onMounted(loadRegions)
watch(searchQuery, () => { pagination.page = 1 })

function normalizeSearchText(value: string) {
  return String(value || '').trim().toLocaleLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

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
  const maxPage = Math.max(1, Math.ceil(filteredRegions.value.length / pagination.pageSize))
  if (pagination.page > maxPage) {
    pagination.page = maxPage
  }
}

async function saveRegion(row: AdminMarketRegion) {
  savingCode.value = row.code
  try {
    const updated = await updateAdminRegion(row.code, {
      code: row.code,
      nameZh: row.nameZh,
      nameEn: row.nameEn,
      nameRu: row.nameRu,
      namePt: row.namePt,
      nameEs: row.nameEs,
      enabled: row.enabled,
      supportsAppStore: row.supportsAppStore,
      supportsGooglePlay: row.supportsGooglePlay,
      supportsIpadStore: row.supportsIpadStore,
      sortOrder: row.sortOrder
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

function openCreate() {
  editingCode.value = ''
  Object.assign(form, { code: '', nameZh: '', nameEn: '', nameRu: '', namePt: '', nameEs: '', enabled: true, supportsAppStore: true, supportsGooglePlay: true, supportsIpadStore: true, sortOrder: regions.value.length + 1 })
  dialogVisible.value = true
}

function openEdit(row: AdminMarketRegion) {
  editingCode.value = row.code
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submitRegion() {
  form.code = form.code.trim().toUpperCase()
  if (!/^[A-Z]{2}$/.test(form.code) || localeNameFields.some((field) => !form[field.key].trim())) {
    ElMessage.warning(t('regions.formInvalid'))
    return
  }
  dialogSaving.value = true
  try {
    if (editingCode.value) await updateAdminRegion(editingCode.value, form)
    else await createAdminRegion(form)
    ElMessage.success(t('regions.saved'))
    dialogVisible.value = false
    await loadRegions()
  } catch {
    ElMessage.error(t('regions.saveFailed'))
  } finally {
    dialogSaving.value = false
  }
}
</script>

<style scoped>
.region-page {
  color: #0f172a;
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
  color: #64748b;
  line-height: 1.6;
}

.toolbar-actions { display: flex; gap: 10px; }

.search-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 14px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 6px 20px rgb(15 23 42 / 4%);
}

.search-control {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.search-control :deep(.el-input) {
  width: min(100%, 420px);
}

.search-tip,
.search-result {
  color: #64748b;
  font-size: 13px;
}

.search-result {
  flex: 0 0 auto;
  padding: 5px 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  font-weight: 650;
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
  color: #64748b;
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

  .search-panel,
  .search-control {
    align-items: stretch;
    flex-direction: column;
  }

  .search-control :deep(.el-input) {
    width: 100%;
  }

  .search-result {
    align-self: flex-start;
  }
}

.edit-action {
  min-width: 54px;
  margin: 0;
  border-radius: 7px;
  font-weight: 650;
}
.toolbar-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.toolbar-hint {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
}

.toolbar-hint .el-icon {
  flex: 0 0 auto;
  color: #94a3b8;
  font-size: 15px;
}</style>
