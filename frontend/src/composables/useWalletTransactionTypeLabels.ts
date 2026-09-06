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
    const translationKey = `wallet.types.${type}`
    const translated = t(translationKey)
    if (translated !== translationKey) return translated
    if (!config) return type
    const fields = {
      'zh-CN': config.displayNameZh,
      'en-US': config.displayNameEn,
      'ru-RU': config.displayNameRu,
      'pt-PT': config.displayNamePt,
      'es-ES': config.displayNameEs
    }
    return fields[locale.value as keyof typeof fields] || config.displayNameEn
  }

  return {
    configs,
    loadingTypeConfigs,
    loadTransactionTypeConfigs,
    transactionTypeLabel
  }
}
