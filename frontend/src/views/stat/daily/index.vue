<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期"
          end-placeholder="结束日期" style="width: 260px" />
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button :icon="RefreshRight" @click="generate">生成昨日汇总</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="statDate" label="日期" width="130" />
      <el-table-column prop="alarmTotal" label="告警" width="90" />
      <el-table-column prop="alarmHandled" label="已处理告警" width="110" />
      <el-table-column prop="accessTotal" label="门禁通行" width="110" />
      <el-table-column label="车辆进出" width="120">
        <template #default="{ row }">{{ row.vehicleInTotal }} / {{ row.vehicleOutTotal }}</template>
      </el-table-column>
      <el-table-column prop="visitorTotal" label="访客" width="90" />
      <el-table-column label="工单" width="120">
        <template #default="{ row }">{{ row.workOrderTotal }} / {{ row.workOrderFinished }}</template>
      </el-table-column>
      <el-table-column label="巡更" width="120">
        <template #default="{ row }">{{ row.patrolTotal }} / 漏 {{ row.patrolMissing }}</template>
      </el-table-column>
      <el-table-column prop="deviceOnlineRate" label="设备在线率(%)" width="140" />
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="pageNum"
        :page-size="pageSize" @current-change="load" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight, Search } from '@element-plus/icons-vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const range = ref<[string, string] | null>(null)
const loading = ref(false)

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await statApi.dailyList({
      pageNum: pageNum.value, pageSize, communityId: app.communityId,
      begin: range.value?.[0], end: range.value?.[1]
    })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function generate() {
  await ElMessageBox.confirm('确认生成昨日汇总数据?', '提示', { type: 'warning' })
  await statApi.generateDaily({ communityId: app.communityId, date: yesterday() })
  ElMessage.success('生成成功')
  load(1)
}

function yesterday() {
  const d = new Date()
  d.setDate(d.getDate() - 1)
  return d.toISOString().slice(0, 10)
}

onMounted(() => load(1))
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
</style>
