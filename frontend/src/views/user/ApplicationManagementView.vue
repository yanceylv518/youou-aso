<template>
  <section class="app-page">
    <header class="table-toolbar">
      <div class="filters">
        <label class="filter-item">
          <span>{{ t('applications.store') }}</span>
          <el-select v-model="filters.storeType" clearable :placeholder="t('applications.allStores')" class="filter-select">
            <template #prefix>
              <span class="select-prefix">
                <StoreIcon v-if="filters.storeType" :store-type="filters.storeType" size="sm" />
                <span v-else class="all-mini">ALL</span>
              </span>
            </template>
            <el-option :label="t('applications.allStores')" value="">
              <span class="option-row"><span class="all-mini">ALL</span>{{ t('applications.allStores') }}</span>
            </el-option>
            <el-option v-for="store in storeFilterOptions" :key="store" :label="storeLabel(store)" :value="store">
              <span class="option-row"><StoreIcon :store-type="store" size="sm" />{{ storeLabel(store) }}</span>
            </el-option>
          </el-select>
        </label>
        <label class="filter-item">
          <span>{{ t('applications.app') }}</span>
          <el-input
            v-model.trim="filters.keyword"
            clearable
            :placeholder="t('applications.keywordPlaceholder')"
            class="filter-keyword"
            @keyup.enter="searchData"
          />
        </label>
        <el-button type="primary" class="search-button" :icon="Search" @click="searchData">
          {{ t('applications.search') }}
        </el-button>
        <el-button class="clear-button" :icon="Delete" @click="resetFilters">{{ t('applications.reset') }}</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">
        {{ t('applications.addApp') }}
      </el-button>
    </header>

    <el-table v-loading="loading" :data="apps" class="app-table" :empty-text="t('applications.empty')">
      <el-table-column :label="t('applications.app')" min-width="320">
        <template #default="{ row }">
            <div class="app-cell">
              <div class="app-icon">
              <img v-if="row.appIconUrl" :src="row.appIconUrl" :alt="row.appName" />
              <span v-else>{{ row.appName.slice(0, 1).toUpperCase() }}</span>
            </div>
            <div class="app-info">
              <div class="app-name">{{ row.appName }}</div>
              <div class="muted">{{ row.appIdentifier }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('applications.store')" min-width="220">
        <template #default="{ row }">
          <span class="cell-with-icon">
            <StoreIcon :store-type="row.storeType" size="md" />
            {{ storeLabel(row.storeType) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('applications.category')" min-width="180">
        <template #default="{ row }">
          {{ formatAppCategory(row.category, locale) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('applications.actions')" width="110" fixed="right" align="center">
        <template #default="{ row }">
          <el-tooltip :content="t('applications.createOrder')" placement="top">
            <el-button
              circle
              :aria-label="t('applications.createOrder')"
              :icon="Plus"
              text
              @click="createOrder(row)"
            />
          </el-tooltip>
          <el-tooltip :content="t('applications.delete')" placement="top">
            <el-button
              circle
              :aria-label="t('applications.delete')"
              :icon="Delete"
              text
              class="danger-icon"
              @click="confirmDelete(row)"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="loadData"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog append-to-body v-model="dialogVisible" :title="t('applications.addApp')" width="720px" @closed="resetDialog">
      <el-form class="add-app-form" label-position="right" label-width="116px" @submit.prevent>
        <el-form-item :label="t('applications.store')">
          <el-radio-group v-model="form.storeType" class="store-tabs">
            <el-radio-button label="APP_STORE">
              <span class="store-option">
                <StoreIcon store-type="APP_STORE" size="sm" />
                App Store
              </span>
            </el-radio-button>
            <el-radio-button label="GOOGLE_PLAY">
              <span class="store-option">
                <StoreIcon store-type="GOOGLE_PLAY" size="sm" />
                Google Play
              </span>
            </el-radio-button>
            <el-radio-button label="IPAD_STORE">
              <span class="store-option">
                <StoreIcon store-type="IPAD_STORE" size="sm" />
                iPad Store
              </span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('applications.region')">
          <el-select
            v-model="form.regionCode"
            class="add-app-control"
            filterable
            :filter-method="filterRegions"
            @visible-change="handleRegionDropdownVisibility"
            :loading="regionLoading"
            :placeholder="t('applications.selectRegion')"
          >
            <template #prefix>
              <span v-if="form.regionCode" class="flag-icon">
                <img :src="flagUrl(form.regionCode)" :alt="form.regionCode" />
              </span>
            </template>
            <el-option
              v-for="region in prioritizedRegions"
              :key="region.code"
              :label="regionOptionLabel(region)"
              :value="region.code"
            >
              <span class="option-row">
                <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                <strong>{{ region.code }}</strong>
                <span>{{ locale.startsWith('zh') ? region.nameZh : region.nameEn }}</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('applications.appName')">
          <el-input
            v-model.trim="form.appName"
            class="add-app-control app-search-input"
            maxlength="255"
            :placeholder="t('applications.manualAppNamePlaceholder')"
            @input="handleAppQueryInput"
            @keyup.enter="searchAppsByName"
          >
            <template #suffix>
              <button type="button" class="input-search-button" :disabled="searching" @click="searchAppsByName">
                <el-icon v-if="activeSearchField !== 'name'"><Search /></el-icon>
                <span v-else class="searching-dot"></span>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="manualIdentifierLabel">
          <el-input
            v-model.trim="form.appIdentifier"
            class="add-app-control app-search-input"
            maxlength="255"
            :placeholder="manualIdentifierPlaceholder"
            @input="handleAppQueryInput"
            @keyup.enter="searchAppByIdentifier"
          >
            <template #suffix>
              <button type="button" class="input-search-button" :disabled="searching" @click="searchAppByIdentifier">
                <el-icon v-if="activeSearchField !== 'identifier'"><Search /></el-icon>
                <span v-else class="searching-dot"></span>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="t('applications.appIcon')">
          <div class="icon-upload-row">
            <button
              type="button"
              class="icon-preview"
              :aria-label="t('applications.uploadIcon')"
              :disabled="iconUploading"
              @click="triggerIconFileInput"
            >
              <img
                v-if="form.appIconUrl && !iconLoadFailed"
                :src="form.appIconUrl"
                :alt="form.appName || form.appIdentifier"
                @load="iconLoadFailed = false"
                @error="handleIconPreviewError"
              />
              <el-icon v-else class="icon-upload-placeholder"><UploadFilled /></el-icon>
            </button>
            <div class="icon-upload-actions">
              <span v-if="showIconLoadFailed" class="icon-upload-tip">{{ t('applications.iconLoadFailed') }}</span>
              <el-button :loading="iconUploading" @click="triggerIconFileInput">{{ t('applications.uploadIcon') }}</el-button>
              <input
                ref="iconFileInput"
                class="visually-hidden-file"
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                @change="handleIconFileChange"
              />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div v-if="searchResults.length > 0" class="search-results">
        <button
          v-for="result in searchResults"
          :key="`${result.storeType}-${result.regionCode}-${result.appIdentifier}`"
          type="button"
          class="result-item"
          :class="{ selected: selectedApp?.appIdentifier === result.appIdentifier }"
          @click="selectSearchResult(result)"
        >
          <span class="result-icon">
            <img v-if="result.appIconUrl" :src="result.appIconUrl" :alt="result.appName" />
            <span v-else>{{ result.appName.slice(0, 1).toUpperCase() }}</span>
          </span>
          <span class="result-main">
            <strong>{{ result.appName }}</strong>
            <span>{{ result.developerName || result.bundleId || result.appIdentifier }}</span>
            <span v-if="result.category" class="result-category">
              {{ t('applications.category') }}：{{ formatAppCategory(result.category, locale) }}
            </span>
          </span>
          <span class="result-id">{{ result.appIdentifier }}</span>
        </button>
      </div>
      <el-alert
        v-if="selectedAppAlreadyExists"
        class="duplicate-app-alert"
        :title="t('applications.appAlreadyAdded')"
        :description="t('applications.appAlreadyAddedTip')"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-empty
        v-if="hasSearched && searchResults.length === 0 && !form.appIdentifier.trim()"
        :description="t('applications.noSearchResults')"
        :image-size="80"
      />
      <p class="dialog-tip">{{ inputHelpText }}</p>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('applications.cancel') }}</el-button>
        <el-tooltip :disabled="!saveDisabledReason" :content="saveDisabledReason" placement="top">
          <span class="save-button-wrap">
            <el-button
              type="primary"
              :disabled="!!saveDisabledReason"
              :loading="submitting"
              @click="submitApp"
            >
              {{ t('applications.verifyAndSave') }}
            </el-button>
          </span>
        </el-tooltip>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, UploadFilled } from '@element-plus/icons-vue'
import { formatAppCategory } from '@/utils/appCategory'
import StoreIcon from '@/components/StoreIcon.vue'
import {
  createCustomerApp,
  deleteCustomerApp,
  getCustomerApps,
  getCustomerAppsPage,
  getEnabledRegions,
  searchStoreApps,
  uploadAppIcon,
  type CustomerApp,
  type MarketRegion,
  type StoreAppSearchResult,
  type StoreType
} from '@/api/applications'

const router = useRouter()
const { t, locale } = useI18n()
const loading = ref(false)
const regionLoading = ref(false)
const searching = ref(false)
const activeSearchField = ref<'name' | 'identifier' | null>(null)
const submitting = ref(false)
const iconUploading = ref(false)
const iconLoadFailed = ref(false)
const showIconLoadFailed = computed(() => iconLoadFailed.value)
const dialogVisible = ref(false)
const apps = ref<CustomerApp[]>([])
const optionApps = ref<CustomerApp[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const allRegions = ref<MarketRegion[]>([])
const regions = ref<MarketRegion[]>([])
const regionSearchQuery = ref('')
const prioritizedRegions = computed(() => prioritizeRegions(regions.value, regionSearchQuery.value))
const searchResults = ref<StoreAppSearchResult[]>([])
const selectedApp = ref<StoreAppSearchResult | null>(null)
const hasSearched = ref(false)
const suppressDialogReset = ref(false)
const iconFileInput = ref<HTMLInputElement | null>(null)
const filters = reactive<{
  keyword: string
  storeType: StoreType | ''
}>({
  keyword: '',
  storeType: ''
})
const form = reactive<CreateForm>({
  storeType: 'APP_STORE',
  regionCode: '',
  keyword: '',
  appIdentifier: '',
  appName: '',
  appIconUrl: '',
  category: null
})

interface CreateForm {
  storeType: StoreType
  regionCode: string
  keyword: string
  appIdentifier: string
  appName: string
  appIconUrl: string
  category: string | null
}


const inputHelpText = computed(() => {
  if (form.storeType === 'GOOGLE_PLAY') return t('applications.googlePlayInputHelp')
  if (form.storeType === 'IPAD_STORE') return t('applications.ipadInputHelp')
  return t('applications.appleInputHelp')
})

const manualIdentifierLabel = computed(() => {
  if (form.storeType === 'GOOGLE_PLAY') return t('applications.googlePlayIdentifierShort')
  return t('applications.identifierShort')
})

const manualIdentifierPlaceholder = computed(() => {
  if (form.storeType === 'GOOGLE_PLAY') return t('applications.googlePlayManualIdentifierPlaceholder')
  return t('applications.manualIdentifierPlaceholder')
})

const storeFilterOptions = computed(() => {
  const stores = new Set<StoreType>()
  optionApps.value.forEach((app) => stores.add(app.storeType))
  return Array.from(stores)
})

const selectedAppAlreadyExists = computed(() => {
  const selectedIdentifier = form.appIdentifier.trim()
  if (!selectedIdentifier) {
    return false
  }
  const selectedRegionCode = form.regionCode.trim().toUpperCase()
  return optionApps.value.some((app) => {
    return app.storeType === form.storeType
      && app.appIdentifier.trim() === selectedIdentifier
      && (app.regionCodes || [app.regionCode]).some((code) => code.trim().toUpperCase() === selectedRegionCode)
  })
})

const saveDisabledReason = computed(() => {
  if (!form.regionCode || !form.appIdentifier.trim() || !form.appName.trim()) {
    return t('applications.manualRequiredFields')
  }
  if (selectedAppAlreadyExists.value) {
    return t('applications.appAlreadyAddedTip')
  }
  return ''
})

async function loadData() {
  loading.value = true
  try {
    const [appPage, optionAppList, regionList] = await Promise.all([
      getCustomerAppsPage({
        keyword: filters.keyword,
        storeType: filters.storeType,
        page: pagination.page,
        pageSize: pagination.pageSize
      }),
      getCustomerApps(),
      getEnabledRegions()
    ])
    apps.value = appPage.items
    pagination.total = appPage.total
    optionApps.value = optionAppList
    allRegions.value = regionList
    if (regions.value.length === 0) {
      await loadRegions()
    }
  } finally {
    loading.value = false
  }
}

async function loadRegions() {
  regionLoading.value = true
  try {
    const regionList = await getEnabledRegions(form.storeType)
    regions.value = regionList
    if (!regionList.some((region) => region.code === form.regionCode)) {
      form.regionCode = regionList[0]?.code || ''
    }
    return regionList
  } catch (error) {
    ElMessage.error(errorMessage(error, t('applications.regionLoadFailed')))
    regions.value = []
    form.regionCode = ''
    return []
  } finally {
    regionLoading.value = false
  }
}

async function searchAppsByName() {
  const keyword = form.appName.trim()
  if (!form.regionCode || !keyword) {
    ElMessage.warning(t('applications.searchRequired'))
    return
  }
  searching.value = true
  activeSearchField.value = 'name'
  hasSearched.value = false
  selectedApp.value = null
  try {
    searchResults.value = await searchStoreApps({
      storeType: form.storeType,
      regionCode: form.regionCode,
      keyword,
      limit: 10
    })
    hasSearched.value = true
  } catch (error) {
    searchResults.value = []
    hasSearched.value = true
    ElMessage.error(errorMessage(error, t('applications.searchFailed')))
  } finally {
    searching.value = false
    activeSearchField.value = null
  }
}

async function searchAppByIdentifier() {
  const keyword = form.appIdentifier.trim()
  if (!form.regionCode || !keyword) {
    ElMessage.warning(t('applications.searchRequired'))
    return
  }
  searching.value = true
  activeSearchField.value = 'identifier'
  hasSearched.value = false
  selectedApp.value = null
  searchResults.value = []
  try {
    const results = await searchStoreApps({
      storeType: form.storeType,
      regionCode: form.regionCode,
      keyword,
      limit: 10
    })
    const normalizedKeyword = keyword.toLowerCase()
    const matched = results.find((result) => {
      return result.appIdentifier.toLowerCase() === normalizedKeyword
        || (result.bundleId || '').toLowerCase() === normalizedKeyword
        || (result.externalAppId || '').toLowerCase() === normalizedKeyword
    }) || results[0]
    if (matched) {
      selectSearchResult(matched)
      hasSearched.value = false
      return
    }
    hasSearched.value = true
  } catch (error) {
    hasSearched.value = true
    ElMessage.error(errorMessage(error, t('applications.searchFailed')))
  } finally {
    searching.value = false
    activeSearchField.value = null
  }
}
async function submitApp() {
  if (selectedAppAlreadyExists.value) {
    ElMessage.warning(t('applications.appAlreadyAdded'))
    return
  }
  const appIdentifier = form.appIdentifier.trim()
  if (!form.regionCode || !appIdentifier) {
    ElMessage.warning(t('applications.requiredFields'))
    return
  }
  submitting.value = true
  try {
    const payload = {
      storeType: form.storeType,
      regionCode: form.regionCode,
      appIdentifier,
      category: form.category || null,
      appName: form.appName.trim(),
      appIconUrl: form.appIconUrl.trim() || null
    }
    await createCustomerApp(payload)
    ElMessage.success(t('applications.saved'))
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('applications.saveFailed')))
  } finally {
    submitting.value = false
  }
}

function handleIconPreviewError() {
  iconLoadFailed.value = Boolean(form.appIconUrl.trim() && (form.appName.trim() || form.appIdentifier.trim()))
}

function triggerIconFileInput() {
  iconFileInput.value?.click()
}

async function handleIconFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type)) {
    ElMessage.warning(t('applications.iconTypeNotAllowed'))
    return
  }
  if (file.size > 1024 * 1024) {
    ElMessage.warning(t('applications.iconTooLarge'))
    return
  }
  iconUploading.value = true
  try {
    const result = await uploadAppIcon(file)
    form.appIconUrl = result.url
    iconLoadFailed.value = false
  } catch (error) {
    ElMessage.error(errorMessage(error, t('applications.iconUploadFailed')))
  } finally {
    iconUploading.value = false
  }
}

async function confirmDelete(app: CustomerApp) {
  try {
    await ElMessageBox.confirm(
      t('applications.confirmDeleteMessage', { name: app.appName }),
      t('applications.confirmDeleteTitle'),
      { type: 'warning', confirmButtonText: t('applications.delete'), cancelButtonText: t('applications.cancel') }
    )
    await deleteCustomerApp(app.id)
    ElMessage.success(t('applications.deleted'))
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(errorMessage(error, t('applications.deleteFailed')))
    }
  }
}

function createOrder(app: CustomerApp) {
  router.push({
    name: 'user-order-create',
    query: {
      appId: String(app.id),
      storeType: app.storeType
    }
  })
}

function selectSearchResult(result: StoreAppSearchResult) {
  form.appIdentifier = result.appIdentifier
  form.appName = result.appName
  form.appIconUrl = result.appIconUrl || ''
  iconLoadFailed.value = false
  form.category = result.category
  form.keyword = result.appName
  selectedApp.value = result
  searchResults.value = []
}

function handleAppQueryInput() {
  selectedApp.value = null
  searchResults.value = []
  hasSearched.value = false
  form.category = null
  if (iconLoadFailed.value) {
    form.appIconUrl = ''
    iconLoadFailed.value = false
  }
}

function resetDialog() {
  form.keyword = ''
  form.appIdentifier = ''
  form.appName = ''
  form.appIconUrl = ''
  iconLoadFailed.value = false
  form.category = null
  searchResults.value = []
  selectedApp.value = null
  hasSearched.value = false
}

function resetFilters() {
  filters.keyword = ''
  filters.storeType = ''
  pagination.page = 1
  void loadData()
}

function searchData() {
  pagination.page = 1
  void loadData()
}

function handlePageSizeChange() {
  pagination.page = 1
  void loadData()
}

function storeLabel(storeType: StoreType) {
  if (storeType === 'APP_STORE') return 'App Store'
  if (storeType === 'GOOGLE_PLAY') return 'Google Play'
  return 'iPad Store'
}

function regionLabel(code: string) {
  const region = allRegions.value.find((item) => item.code === code) || regions.value.find((item) => item.code === code)
  if (!region) return code
  return locale.value.startsWith('zh') ? region.nameZh : region.nameEn
}

function regionOptionLabel(region: MarketRegion) {
  const name = locale.value.startsWith('zh') ? region.nameZh : region.nameEn
  return `${region.code} · ${name}`
}

function filterRegions(query: string) {
  regionSearchQuery.value = query.trim().toLocaleLowerCase()
}

function handleRegionDropdownVisibility(visible: boolean) {
  if (!visible) regionSearchQuery.value = ''
}

function prioritizeRegions(list: MarketRegion[], query: string) {
  if (!query) return list
  return list
    .map((region, index) => ({ region, index, score: regionSearchScore(region, query) }))
    .filter((item) => item.score < 99)
    .sort((left, right) => left.score - right.score || left.index - right.index)
    .map((item) => item.region)
}

function regionSearchScore(region: MarketRegion, query: string) {
  const code = region.code.toLocaleLowerCase()
  if (code === query) return 0
  if (code.startsWith(query)) return 1
  if (code.includes(query)) return 2
  const names = [region.nameZh, region.nameEn].map((name) => name.toLocaleLowerCase())
  if (names.some((name) => name.startsWith(query))) return 3
  if (names.some((name) => name.includes(query))) return 4
  return 99
}

function flagUrl(code: string) {
  return `https://flagcdn.com/24x18/${code.toLowerCase()}.png`
}

function errorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { code?: string; message?: string } } }).response
    if (response?.data?.code === 'APP_ALREADY_EXISTS') {
      return t('applications.appAlreadyAdded')
    }
    return response?.data?.message || fallback
  }
  return fallback
}

async function openCreateDialog() {

  resetDialog()

  dialogVisible.value = true
  await loadRegions()
}

watch(
  () => form.storeType,
  async () => {
    if (suppressDialogReset.value) return
    searchResults.value = []
    selectedApp.value = null
    form.appIdentifier = ''
    form.appName = ''
    form.appIconUrl = ''
    iconLoadFailed.value = false
    form.category = null
    hasSearched.value = false
    await loadRegions()
  }
)

watch(
  () => form.regionCode,
  () => {
    if (suppressDialogReset.value) return
    searchResults.value = []
    selectedApp.value = null
    hasSearched.value = false
  }
)

onMounted(loadData)
</script>

<style scoped>
.app-page {
  color: #0f172a;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.filter-keyword {
  width: 250px;
}

.filter-select {
  width: 150px;
}

.select-prefix {
  display: inline-flex;
  align-items: center;
}

.option-row,
.cell-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.all-mini {
  min-width: 20px;
  height: 20px;
  display: inline-grid;
  place-items: center;
  border-radius: 5px;
  background: linear-gradient(135deg, #65c7ff, #2478e8);
  color: #ffffff;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
}

.flag-icon {
  width: 22px;
  display: inline-grid;
  place-items: center;
  line-height: 1;
}

.flag-icon img {
  width: 22px;
  height: 16px;
  display: block;
  border-radius: 2px;
  box-shadow: 0 0 0 1px rgb(16 24 40 / 8%);
  object-fit: cover;
}

.search-button {
  background: #2563eb;
  border-color: #2563eb;
}

.search-button:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.clear-button {
  color: #334155;
}

.app-table {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 12px 30px rgb(16 24 40 / 4%);
}

.app-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.app-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #64748b;
  font-weight: 600;
}

.app-table :deep(.el-table__cell) {
  height: 80px;
}

.app-table :deep(.el-table__empty-block) {
  min-height: 96px;
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

.app-cell {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.app-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 800;
}

.app-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-name {
  font-weight: 700;
}

.app-info {
  min-width: 0;
}

.muted {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.danger-icon {
  color: #dc2626;
}

.dialog-tip {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.duplicate-app-alert {
  margin: -2px 0 14px;
}

.save-button-wrap {
  display: inline-flex;
  margin-left: 12px;
}

.add-app-form {
  max-width: 560px;
}

.store-tabs :deep(.el-radio-button__inner) {
  min-width: 150px;
}

.store-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.add-app-control {
  width: 360px;
  max-width: 100%;
}

.icon-upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.icon-preview {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 10px;
  border: 1px dashed #9ec5fe;
  background: #f4f8ff;
  color: #2563eb;
  font-weight: 800;
  padding: 0;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
  appearance: none;
}

.icon-preview:hover:not(:disabled) {
  border-color: #2563eb;
  background: #eff6ff;
}

.icon-preview:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgb(64 158 255 / 18%);
}

.icon-preview:disabled {
  cursor: wait;
  opacity: 0.72;
}

.icon-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: inherit;
}

.icon-upload-placeholder {
  font-size: 22px;
  color: #2563eb;
}

.icon-upload-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.icon-upload-tip {
  color: #dc2626;
  font-size: 13px;
}

.visually-hidden-file {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.input-search-button {
  width: 26px;
  height: 26px;
  display: inline-grid;
  place-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #98a2b3;
  cursor: pointer;
}

.input-search-button:hover {
  color: #2563eb;
}

.input-search-button:disabled {
  cursor: default;
  opacity: 0.7;
}

.searching-dot {
  width: 14px;
  height: 14px;
  border: 2px solid #d0d5dd;
  border-top-color: #2563eb;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.search-results {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
  max-height: 320px;
  overflow: auto;
}

.result-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) minmax(120px, auto);
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d7e0ea;
  border-radius: 8px;
  background: #ffffff;
  color: #0f172a;
  cursor: pointer;
  text-align: left;
}

.result-item:hover,
.result-item.selected {
  border-color: #2563eb;
  background: #f3f8ff;
}

.result-icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 800;
}

.result-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-main {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.result-main strong,
.result-main span,
.result-id {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-main span,
.result-id {
  color: #64748b;
  font-size: 13px;
}

.result-category {
  color: #334155;
  font-size: 13px;
}

.selected-app {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) minmax(120px, auto);
  align-items: center;
  gap: 12px;
  margin: 0 0 14px;
  padding: 12px;
  border: 1px solid #b8d7ff;
  border-radius: 8px;
  background: #f3f8ff;
}

.selected-icon {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 10px;
  background: #dbeafe;
  color: #2563eb;
  font-weight: 800;
}

.selected-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-main {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.selected-label {
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.selected-main strong,
.selected-main span,
.selected-id {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-main span,
.selected-id {
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 720px) {
  .table-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .filters,
  .filter-item,
  .filter-keyword,
  .filter-select {
    width: 100%;
  }

  .filter-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .result-item,
  .selected-app {
    grid-template-columns: 1fr;
  }

  .add-app-form {
    max-width: none;
  }

  .store-tabs {
    width: 100%;
  }

  .store-tabs :deep(.el-radio-button),
  .store-tabs :deep(.el-radio-button__inner),
  .add-app-control {
    width: 100%;
  }
}
</style>
