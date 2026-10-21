<template>
  <el-card class="page-card">
    <div class="flex-between mb16">
      <div class="panel-title" style="border: none; padding: 0; margin: 0">菜单权限树</div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>
    <el-tree :data="tree" :props="{ label: 'menuName', children: 'children' }" node-key="id"
      default-expand-all :expand-on-click-node="false">
      <template #default="{ node, data }">
        <span>
          <el-icon v-if="data.icon" style="vertical-align: middle"><component :is="data.icon" /></el-icon>
          {{ node.label }}
          <el-tag v-if="data.perms" size="small" type="info" class="ml8">{{ data.perms }}</el-tag>
        </span>
      </template>
    </el-tree>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { sysApi } from '@/api'

const tree = ref<any[]>([])

async function load() {
  tree.value = await sysApi.menuTree()
}

onMounted(load)
</script>

<style scoped>
.mb16 { margin-bottom: var(--space-lg); }
.ml8 { margin-left: var(--space-sm); }
</style>