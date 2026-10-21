<template>
  <div class="dashboard-page">
    <!-- 统计卡片行 -->
    <div class="grid-4 mb16">
      <div v-for="c in cards" :key="c.label" class="stat-card">
        <div class="stat-icon" :style="{ background: c.bg }"><el-icon :size="26"><component :is="c.icon" /></el-icon></div>
        <div class="stat-content">
          <div class="stat-num">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 告警 & 通行趋势 -->
    <div class="grid-2 mt16">
      <el-card class="page-card">
        <div class="panel-title">近7天告警趋势</div>
        <EChart :option="alarmOption" height="300px" />
      </el-card>
      <el-card class="page-card">
        <div class="panel-title">近7天通行趋势</div>
        <EChart :option="accessOption" height="300px" />
      </el-card>
    </div>

    <!-- 设备在线率 & 待办概览 -->
    <div class="grid-2 mt16">
      <el-card class="page-card">
        <div class="panel-title">设备在线率</div>
        <EChart :option="deviceOption" height="260px" />
      </el-card>
      <el-card class="page-card">
        <div class="panel-title">待办与概览</div>
        <el-descriptions :column="2" border class="descriptions-box">
          <el-descriptions-item label="小区总数">{{ ov.communityCount }}</el-descriptions-item>
          <el-descriptions-item label="房屋总数">{{ ov.roomCount }}</el-descriptions-item>
          <el-descriptions-item label="业主总数">{{ ov.ownerCount }}</el-descriptions-item>
          <el-descriptions-item label="今日访客">{{ ov.todayVisitor }}</el-descriptions-item>
          <el-descriptions-item label="未完成工单">{{ ov.pendingWorkOrder }}</el-descriptions-item>
          <el-descriptions-item label="超时工单">{{ ov.overdueWorkOrder }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Warning, User, Van, Key, Cpu, DataLine } from '@element-plus/icons-vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const ov = ref<any>({})
const alarm = ref<any>({})
const access = ref<any>({})
const device = ref<any>({})

const cards = computed(() => [
  { label: '今日告警', value: ov.value.todayAlarm ?? 0, icon: Warning, bg: '#fff1e6' },
  { label: '待处理告警', value: ov.value.pendingAlarm ?? 0, icon: Warning, bg: '#ffe7e7' },
  { label: '待办工单', value: ov.value.pendingWorkOrder ?? 0, icon: User, bg: '#e6f4ff' },
  { label: '今日通行', value: ov.value.todayAccess ?? 0, icon: Key, bg: '#e8f8ee' }
])

const baseGrid = { left: 40, right: 20, top: 30, bottom: 30 }
const alarmOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['告警', '已处理'], right: 10 },
  grid: baseGrid,
  xAxis: { type: 'category', data: alarm.value.dates || [] },
  yAxis: { type: 'value' },
  series: [
    { name: '告警', type: 'line', smooth: true, data: alarm.value.totals || [], areaStyle: { opacity: 0.15 } },
    { name: '已处理', type: 'line', smooth: true, data: alarm.value.handled || [] }
  ]
}))
const accessOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['通行', '拒绝'], right: 10 },
  grid: baseGrid,
  xAxis: { type: 'category', data: access.value.dates || [] },
  yAxis: { type: 'value' },
  series: [
    { name: '通行', type: 'bar', data: access.value.totals || [] },
    { name: '拒绝', type: 'bar', data: access.value.denied || [] }
  ]
}))
const deviceOption = computed(() => ({
  tooltip: { formatter: '{b}: {c}%' },
  series: [
    {
      type: 'gauge', radius: '90%', progress: { show: true }, axisLine: { lineStyle: { width: 14 } },
      detail: { formatter: '{value}%', fontSize: 26 }, data: [{ value: device.value.onlineRate ?? 0, name: '在线率' }]
    }
  ]
}))

async function load() {
  const c = app.communityId
  const [o, a, ac, d] = await Promise.all([
    statApi.overview(c),
    statApi.alarmTrend(c, 7),
    statApi.accessTrend(c, 7),
    statApi.deviceOnlineRate(c)
  ])
  ov.value = o
  alarm.value = a
  access.value = ac
  device.value = d
}
onMounted(load)
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: var(--space-lg); }
.stat-content { display: flex; flex-direction: column; }
.descriptions-box { width: 100%; }
</style>
