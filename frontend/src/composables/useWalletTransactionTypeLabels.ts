import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getAdminWalletTransactionTypeConfigs,
  getCustomerWalletTransactionTypeConfigs,
  type WalletTransactionType,
  type WalletTransactionTypeConfig
} from '@/api/wallet'

export function useWalletTransactionTypeLabels(scope: 'admin' | 'customer') {
  const { locale, t } = useI18n()
  const configs = ref<WalletTransactionTypeConfig[]>([])
  const loadingTypeConfigs = ref(false)

  const configMap = computed(() => {
    return new Map(configs.value.map((config) => [config.transactionType, config]))
  })

  async function loadTransactionTypeConfigs() {
    loadingTypeConfigs.value = true
    try {
      configs.value = scope === 'admin'
        ? await getAdminWalletTransactionTypeConfigs()
        : await getCustomerWalletTransactionTypeConfigs()
    } catch {
      configs.value = []
    } finally {
      loadingTypeConfigs.value = false
    }
  }

  function transactionTypeLabel(type: WalletTransactionType | string | null | undefined) {
    if (!type) return '-'
    const config = configMap.value.get(type as WalletTransactionType)
    if (!config) return t(`wallet.types.${type}`)
    return locale.value === 'en-US' ? config.displayNameEn : config.displayNameZh
  }

  return {
    configs,
    loadingTypeConfigs,
    loadTransactionTypeConfigs,
    transactionTypeLabel
  }
}
