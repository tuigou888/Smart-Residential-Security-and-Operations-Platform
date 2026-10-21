import { defineStore } from 'pinia'
import { authApi } from '@/api'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuType: string
  path: string
  component: string | null
  perms: string | null
  icon: string
  orderNum: number
  visible: number
  children?: MenuItem[]
}

interface UserInfo {
  userId: number
  username: string
  realName: string
  roles: string[]
  authorities: string[]
  communities: number[]
  dataScope: number
  menus: MenuItem[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    info: null as UserInfo | null,
    menus: [] as MenuItem[]
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    communityOptions: (s) => (s.info?.communities || []).map((id: number) => ({ id, label: '小区#' + id }))
  },
  actions: {
    async login(username: string, password: string) {
      const data: any = await authApi.login({ username, password })
      this.token = data.accessToken
      localStorage.setItem('accessToken', data.accessToken)
    },
    async fetchInfo() {
      const data: any = await authApi.info()
      const u = data.user || {}
      this.info = {
        userId: u.userId, username: u.username, realName: u.realName,
        roles: data.roles || [], authorities: data.permissions || [],
        communities: data.communities || [], dataScope: data.dataScope || 1,
        menus: data.menus || []
      }
      this.menus = data.menus || []
      return data
    },
    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        /* ignore */
      }
      this.token = ''
      this.info = null
      this.menus = []
      localStorage.removeItem('accessToken')
    },
    buildMenuTree(): MenuItem[] {
      const flat: MenuItem[] = []
      const collect = (arr: MenuItem[]) => {
        arr.forEach((m) => {
          flat.push(m)
          if (m.children && m.children.length) collect(m.children)
        })
      }
      collect(this.menus)
      const list = flat.filter((m) => m.visible !== 0 && m.menuType !== 'F')
      const map = new Map<number, MenuItem>()
      list.forEach((m) => map.set(m.id, { ...m, children: [] }))
      const tree: MenuItem[] = []
      map.forEach((node) => {
        const parent = map.get(node.parentId)
        if (parent) parent.children!.push(node)
        else tree.push(node)
      })
      const sortRec = (arr: MenuItem[]) => {
        arr.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
        arr.forEach((n) => n.children && sortRec(n.children))
      }
      sortRec(tree)
      return tree
    }
  }
})
