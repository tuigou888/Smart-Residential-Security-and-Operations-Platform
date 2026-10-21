<template>
  <div class="page-card">
    <el-row :gutter="16" class="mb16">
      <el-col :span="6"><el-card shadow="never"><div class="kpi"><span>应巡点位</span><b>{{ stat.expected || 0 }}</b></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="kpi"><span>实巡打卡</span><b class="ok">{{ stat.actual || 0 }}</b></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="kpi"><span>漏巡</span><b class="danger">{{ stat.missing || 0 }}</b></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="kpi"><span>异常</span><b class="warn">{{ stat.abnormal || 0 }}</b></div></el-card></el-col>
    </el-row>

    <div class="flex-between mb16">
      <div class="search">
        <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" placeholder="统计日期" style="width: 160px" @change="loadStat" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="load(1)">
          <el-option label="正常" :value="1" />
          <el-option label="异常" :value="2" />
          <el-option label="漏巡" :value="3" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button :icon="RefreshRight" @click="markMissing">执行漏巡判定</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="planId" label="计划ID" width="120" />
      <el-table-column prop="routeId" label="路线ID" width="120" />
      <el-table-column prop="pointId" label="点位ID" width="120" />
      <el-table-column prop="userName" label="巡更人员" width="120" />
      <el-table-column prop="checkMethod" label="方式" width="90">
        <template #default="{ row }">{{ methodText(row.checkMethod) }}</template>
      </el-table-column>
      <el-table-column prop="checkTime" label="打卡时间" min-width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 3 ? 'danger' : 'warning'">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="exceptionDesc" label="异常描述" min-width="160" show-overflow-tooltip />
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
import { patrolApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const stat = ref<any>({})
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const status = ref<number | undefined>()
const date = ref(new Date().toISOString().slice(0, 10))
const loading = ref(false)

function statusText(s: number) {
  return { 1: '正常', 2: '异常', 3: '漏巡' }[s] || s
}
function methodText(m: number) {
  return { 1: 'NFC', 2: '二维码', 3: 'GPS' }[m] || m
}

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await patrolApi.records({ pageNum: pageNum.value, pageSize, communityId: app.communityId, status: status.value })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStat() {
  stat.value = (await patrolApi.recordStat(app.communityId, date.value)) || {}
}

async function markMissing() {
  await ElMessageBox.confirm(`确认对 ${date.value} 执行漏巡判定?`, '提示', { type: 'warning' })
  const count = await patrolApi.markMissing(date.value)
  ElMessage.success(`判定完成，新增漏巡 ${count} 条`)
  loadStat()
  load(1)
}

onMounted(async () => {
  await Promise.all([loadStat(), load(1)])
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
.kpi { display: flex; flex-direction: column; gap: 6px; }
.kpi span { color: #6b7280; font-size: 13px; }
.kpi b { font-size: 26px; }
.ok { color: #16a34a; }
.danger { color: #dc2626; }
.warn { color: #d97706; }
</style>
