<template>
  <div class="page-card">
    <el-row :gutter="16" class="mb16">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="hover"><div class="stat-num">{{ s.value }}</div><div class="stat-label">{{ s.label }}</div></el-card>
      </el-col>
    </el-row>
    <div class="flex-between mb16">
      <el-form :inline="true" :model="q">
        <el-form-item label="名称"><el-input v-model="q.deviceName" clearable placeholder="设备名称" style="width: 150px" /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="load(1)">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table :data="rows" border v-loading="loading" style="width: 100%">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="deviceName" label="名称" width="160" />
      <el-table-column prop="deviceCode" label="编码" width="160" />
      <el-table-column prop="deviceTypeName" label="类型" width="120" />
      <el-table-column prop="installLocation" label="位置" min-width="160" />
      <el-table-column prop="onlineStatus" label="在线" width="90" :formatter="onFmt" />
      <el-table-column prop="runStatus" label="运行" width="90" :formatter="runFmt" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="restart(scope.row)">重启</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
        :page-sizes="[10, 20, 50]" v-model:current-page="pageNum" v-model:page-size="pageSize"
        @current-change="load" @size-change="load(1)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { iotApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const q = reactive<any>({})
const status = ref<any>({})
const typeStat = ref<any[]>([])

const statCards = computed(() => [
  { label: '在线设备', value: status.value.online ?? 0 },
  { label: '离线设备', value: status.value.offline ?? 0 },
  { label: '故障设备', value: status.value.fault ?? 0 },
  { label: '设备总数', value: status.value.total ?? 0 }
])
function onFmt(_: any, r: any) { return r.onlineStatus === 1 ? '在线' : '离线' }
function runFmt(_: any, r: any) { return ({ 1: '正常', 2: '故障', 3: '维保' } as any)[r.runStatus] || r.runStatus }

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value, communityId: app.communityId }
    Object.assign(params, q)
    const res = await iotApi.devices(params)
    rows.value = res.records || []
    total.value = res.total || 0
    status.value = await iotApi.deviceStatus(app.communityId)
    typeStat.value = await iotApi.deviceTypeStat(app.communityId)
  } finally { loading.value = false }
}
async function restart(row: any) {
  await iotApi.restart(row.id)
  ElMessage.success('重启指令已下发')
}
onMounted(load)
</script>

<style scoped>
.mb16 { margin-bottom: var(--space-lg); }
.mt16 { margin-top: var(--space-lg); }
.stat-num { font-size: 24px; font-weight: 700; }
.stat-label { color: #909399; font-size: 13px; }
</style>
