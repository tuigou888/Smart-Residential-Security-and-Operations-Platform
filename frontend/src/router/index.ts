import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import type { MenuItem } from '@/store/user'

const componentMap: Record<string, () => Promise<any>> = {
  'dashboard/index': () => import('@/views/dashboard/index.vue'),
  'screen/index': () => import('@/views/screen/index.vue'),
  'alarm/list/index': () => import('@/views/alarm/list/index.vue'),
  'alarm/stat/index': () => import('@/views/alarm/stat/index.vue'),
  'workorder/list/index': () => import('@/views/workorder/list/index.vue'),
  'workorder/stat/index': () => import('@/views/workorder/stat/index.vue'),
  'iot/device/index': () => import('@/views/iot/device/index.vue'),
  'iot/device/status': () => import('@/views/iot/device/status.vue'),
  'video/live/index': () => import('@/views/video/live/index.vue'),
  'stat/overview/index': () => import('@/views/stat/overview/index.vue'),
  'stat/alarm/index': () => import('@/views/stat/alarm/index.vue'),
  'stat/access/index': () => import('@/views/stat/access/index.vue'),
  'stat/device/index': () => import('@/views/stat/device/index.vue'),
  'system/menu/index': () => import('@/views/system/menu/index.vue'),
  'access/remote/index': () => import('@/views/access/remote/index.vue')
}

const GenericView = () => import('@/views/generic/index.vue')
const Placeholder = () => import('@/views/generic/placeholder.vue')

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: []
  },
  { path: '/:pathMatch(.*)*', component: Placeholder }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function resolveComponent(menu: MenuItem): () => Promise<any> {
  if (menu.component && componentMap[menu.component]) return componentMap[menu.component]
  if (menu.component) return GenericView
  return Placeholder
}

async function ensureRoutes() {
  const user = useUserStore()
  if (!user.menus.length) {
    await user.fetchInfo()
  }
  const layout = router.getRoutes().find((r) => r.name === 'Layout')
  if (!layout) return 0
  const addRoutes: any[] = []
  const walk = (menus: MenuItem[]) => {
    for (const m of menus) {
      if (m.menuType === 'C' && m.path) {
        const name = m.path
        if (!router.hasRoute(name)) {
          addRoutes.push({
            name,
            path: m.path.replace(/^\//, ''),
            component: resolveComponent(m),
            meta: { title: m.menuName, icon: m.icon, component: m.component }
          })
        }
      }
      if (m.children) walk(m.children)
    }
  }
  walk(user.buildMenuTree())
  if (addRoutes.length) {
    addRoutes.forEach((r) => router.addRoute('Layout', r))
  }
  return addRoutes.length
}

router.beforeEach(async (to, from, next) => {
  const user = useUserStore()
  if (to.path === '/login') {
    if (user.isLoggedIn) return next('/dashboard')
    return next()
  }
  if (!user.isLoggedIn) {
    return next('/login')
  }
  try {
    if (!user.menus.length) {
      await user.fetchInfo()
    }
    const added = await ensureRoutes()
    if (added > 0) {
      return next({ ...to, replace: true })
    }
    return next()
  } catch (e) {
    return next('/login')
  }
})

export default router
