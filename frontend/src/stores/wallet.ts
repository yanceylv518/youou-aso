import { defineStore } from 'pinia'
import { getCustomerWallet, type WalletOverview } from '@/api/wallet'

export const useWalletStore = defineStore('wallet', {
  state: () => ({
    overview: null as WalletOverview | null,
    loading: false
  }),
  getters: {
    balance: (state) => state.overview?.balance ?? null
  },
  actions: {
    setOverview(overview: WalletOverview | null) {
      this.overview = overview
    },
    async loadCustomerWallet() {
      this.loading = true
      try {
        const overview = await getCustomerWallet()
        this.overview = overview
        return overview
      } catch (error) {
        this.overview = null
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
