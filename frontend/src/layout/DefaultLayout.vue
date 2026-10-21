<template>
  <el-container class="layout-root">
    <el-aside class="aside" :class="{ 'aside-collapsed': collapsed }">
      <div class="logo">
        <el-icon color="#36d1ff" :size="22"><Lock /></el-icon>
        <span v-show="!collapsed" class="logo-text">智慧安防运营平台</span>
      </div>
        <el-scrollbar>
          <el-menu :default-active="activePath" router background-color="#0b2138"
            text-color="#c5d6e6" active-text-color="#36d1ff" class="menu"
            :collapse-transition="false">
            <template v-for="m in menuTree" :key="m.id">
              <el-sub-menu v-if="m.children && m.children.length" :index="m.path || String(m.id)"
                :collapsible="false">
                <template #title>
                  <el-icon v-if="m.icon"><component :is="m.icon" /></el-icon>
                  <span class="menu-text">{{ m.menuName }}</span>
                </template>
              <el-menu-item v-for="c in m.children" :key="c.id" :index="c.path">
                <el-icon v-if="c.icon"><component :is="c.icon" /></el-icon>
                <template #title>{{ c.menuName }}</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="m.path || String(m.id)">
              <el-icon v-if="m.icon"><component :is="m.icon" /></el-icon>
              <template #title>{{ m.menuName }}</template>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="aside-collapsed-mask" v-if="collapsed" />
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-grid">
          <div class="header-left">
            <el-button text @click="toggleCollapsed">
              <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-center">
            <span class="work-title">{{ currentTitle || '工作台' }}</span>
          </div>
          <div class="header-right">
            <el-select v-model="app.selectedCommunity" placeholder="选择小区" style="width: 180px"
              @change="onCommunityChange">
              <el-option v-for="c in communityOpts" :key="c.id" :label="c.label" :value="c.id" />
            </el-select>
            <el-divider direction="vertical" />
            <el-badge :value="3" :hidden="true" class="notify-btn">
              <el-button text :icon="Bell" title="通知" />
            </el-badge>
            <el-dropdown @command="onCommand">
              <span class="user">
                <el-avatar :size="30" style="background:#36d1ff">{{ realName.charAt(0) }}</el-avatar>
                <span class="user-name">{{ realName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="screen">运营大屏</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="route.path + '#' + refreshKey" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { Fold, Expand, ArrowDown, Bell } from '@element-plus/icons-vue'
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const app = useAppStore()
const collapsed = ref(false)
const refreshKey = ref(0)

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}

const menuTree = computed(() => user.buildMenuTree())
const realName = computed(() => user.info?.realName || user.info?.username || '未登录')
const communityOpts = computed(() => user.communityOptions)
const activePath = computed(() => route.path)
const currentTitle = computed(() => (route.meta?.title as string) || '')

watch(
  () => user.menus,
  () => {},
  { immediate: true }
)

onMounted(() => {
  app.initCommunity()
})

function onCommunityChange() {
  // 切换小区后刷新当前页
  refreshKey.value++
}

async function onCommand(cmd: string) {
  if (cmd === 'logout') {
    await user.logout()
    router.replace('/login')
  } else if (cmd === 'screen') {
    router.push('/screen')
  }
}
</script>

<style scoped>
.layout-root { height: 100vh; display: flex; }
.el-container { height: 100%; }
.aside {
  background: #0b2138;
  flex-shrink: 0;
  width: 220px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: width .2s cubic-bezier(0.4, 0, 0.2, 1);
}
.aside-collapsed {
  width: 64px !important;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  color: #fff;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
}
.logo-text { transition: opacity .2s; opacity: 1; }
.aside-collapsed .logo-text { opacity: 0; }
.menu { border-right: none; }
.menu-text { white-space: nowrap; }
.aside-collapsed-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc(100% - 64px);
  background: #0b2138;
  display: none;
  z-index: 10;
}
.aside-collapsed .aside-collapsed-mask { display: block; }

.header {
  background: #fff;
  height: 64px;
  border-bottom: 1px solid #eaeef2;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.header-grid {
  height: 100%;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 0 24px;
  gap: 0 24px;
}

.header-left, .header-center, .header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left { justify-content: flex-start; }
.header-center { justify-content: center; }
.header-right { justify-content: flex-end; }

.work-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  opacity: 0.8;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}
.user:hover { background-color: #f5f7fa; }
.user-name {
  font-size: 14px;
  color: #303133;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notify-btn {
  margin-right: 4px;
}
.notify-btn .el-button {
  font-size: 18px;
}
.main { background: #f0f2f5; padding: 16px; }
</style>
