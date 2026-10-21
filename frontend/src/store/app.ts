import { defineStore } from 'pinia'
import { useUserStore } from './user'

export const useAppStore = defineStore('app', {
  state: () => ({
    selectedCommunity: null as number | null,
    collapsed: false
  }),
  getters: {
    communityId: (s) => s.selectedCommunity
  },
  actions: {
    initCommunity() {
      const user = useUserStore()
      if (!this.selectedCommunity && user.info?.communities?.length) {
        this.selectedCommunity = user.info.communities[0]
      }
    },
    setCommunity(id: number) {
      this.selectedCommunity = id
    }
  }
})
