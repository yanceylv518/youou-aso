<template>
  <section class="wallet-type-config-page">
    <div class="toolbar">
      <p class="page-note">{{ t('walletTypeConfig.subtitle') }}</p>
      <div class="toolbar-actions">
        <el-button :loading="loading" @click="loadConfigs">
          {{ t('ordersPage.refresh') }}
        </el-button>
        <el-button type="primary" :loading="saving" @click="saveConfigs">
          {{ t('walletTypeConfig.save') }}
        </el-button>
      </div>
    </div>

    <div class="config-card">
      <el-table v-loading="loading" :data="formItems" class="config-table" :empty-text="t('walletTypeConfig.empty')">
        <el-table-column :label="t('walletTypeConfig.code')" min-width="180">
          <template #default="{ row }">
            <code>{{ row.transactionType }}</code>
          </template>
        </el-table-column>
        <el-table-column :label="t('walletTypeConfig.displayNameZh')" min-width="220">
          <template #default="{ row }">
            <el-input
              v-model.trim="row.displayNameZh"
              maxlength="80"
              show-word-limit
              :placeholder="t('walletTypeConfig.displayNameZhPlaceholder')"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('walletTypeConfig.displayNameEn')" min-width="260">
          <template #default="{ row }">
            <el-input
              v-model.trim="row.displayNameEn"
              maxlength="120"
              show-word-limit
              :placeholder="t('walletTypeConfig.displayNameEnPlaceholder')"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('walletTypeConfig.updatedAt')" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  getAdminWalletTransactionTypeConfigs,
  updateAdminWalletTransactionTypeConfigs,
  type WalletTransactionTypeConfig
} from '@/api/wallet'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const formItems = ref<WalletTransactionTypeConfig[]>([])

onMounted(() => {
  loadConfigs()
})

async function loadConfigs() {
  loading.value = true
  try {
    formItems.value = await getAdminWalletTransactionTypeConfigs()
  } catch {
    ElMessage.error(t('walletTypeConfig.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveConfigs() {
  if (formItems.value.some((item) => !item.displayNameZh.trim() || !item.displayNameEn.trim())) {
    ElMessage.warning(t('walletTypeConfig.required'))
    return
  }
  saving.value = true
  try {
    formItems.value = await updateAdminWalletTransactionTypeConfigs({
      items: formItems.value.map((item) => ({
        transactionType: item.transactionType,
        displayNameZh: item.displayNameZh.trim(),
        displayNameEn: item.displayNameEn.trim()
      }))
    })
    ElMessage.success(t('walletTypeConfig.saved'))
  } catch {
    ElMessage.error(t('walletTypeConfig.saveFailed'))
  } finally {
    saving.value = false
  }
}

function formatDate(value: string | null | undefined) {
  return value ? value.replace('T', ' ') : '-'
}
</script>

<style scoped>
.wallet-type-config-page {
  color: #182230;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.page-note {
  max-width: 760px;
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.config-card {
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.config-table {
  width: 100%;
}

.config-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.config-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #667085;
  font-weight: 700;
}

code {
  color: #344054;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
}

@media (max-width: 860px) {
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
</style>
