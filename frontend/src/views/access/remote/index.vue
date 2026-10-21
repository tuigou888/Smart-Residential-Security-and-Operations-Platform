<template>
  <div class="page-card">
    <div class="panel-title">远程开门</div>
    <el-alert type="info" :closable="false" class="mb16" title="演示环境：开门指令下发至门禁设备，设备接入层对接后生效。" />
    <el-table :data="devices" border v-loading="loading">
      <el-table-column prop="deviceName" label="设备名称" width="180" />
      <el-table-column prop="deviceCode" label="设备编码" width="180" />
      <el-table-column prop="installLocation" label="安装位置" min-width="180" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="primary" :icon="Unlock" @click="open(scope.row)">远程开门</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Unlock } from '@element-plus/icons-vue'
import { secApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const loading = ref(false)
const devices = ref<any[]>([])

async function load() {
  loading.value = true
  try {
    devices.value = await secApi.accessDevices(app.communityId)
  } finally {
    loading.value = false
  }
}

async function open(row: any) {
  await secApi.remoteOpen({ deviceId: row.id, communityId: app.communityId, reason: '远程开门' })
  ElMessage.success('开门指令已下发')
}

onMounted(load)
</script>

<style scoped>
.mb16 { margin-bottom: var(--space-lg); }
</style>