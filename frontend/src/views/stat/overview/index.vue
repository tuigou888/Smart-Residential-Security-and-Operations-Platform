<template>
  <div class="dashboard-page">
    <!-- 统计卡片网格 -->
    <div class="grid-4 mb16">
      <div v-for="c in cards" :key="c.label" class="stat-card">
        <div class="stat-icon" :style="{ background: c.bg }">
          <el-icon :size="26"><component :is="c.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-num">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 告警趋势 & 通行趋势 -->
    <div class="grid-2 mt16">
      <el-card class="page-card">
        <div class="panel-title">告警趋势</div>
        <EChart :option="alarmOpt" height="300px" />
      </el-card>
      <el-card class="page-card">
        <div class="panel-title">通行趋势</div>
        <EChart :option="accessOpt" height="300px" />
      </el-card>
    </div>

    <!-- 车辆出入 & 工单状态 -->
    <div class="grid-2 mt16">
      <el-card class="page-card">
        <div class="panel-title">车辆出入</div>
        <EChart :option="vehOpt" height="300px" />
      </el-card>
      <el-card class="page-card">
        <div class="panel-title">工单状态</div>
        <EChart :option="woOpt" height="300px" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Warning, User, Key, Van, Cpu, DataLine } from '@element-plus/icons-vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const ov = ref<any>({})
const alarm = ref<any>({})
const access = ref<any>({})
const veh = ref<any>({})
const wo = ref<any>({})

const cards = computed(() => [
  { label: '今日告警', value: ov.value.todayAlarm ?? 0, bg: '#fff1e6', icon: Warning },
  { label: '待处理', value: ov.value.pendingAlarm ?? 0, bg: '#ffe7e7', icon: Cpu },
  { label: '今日通行', value: ov.value.todayAccess ?? 0, bg: '#e6f4ff', icon: Key },
  { label: '今日访客', value: ov.value.todayVisitor ?? 0, bg: '#e8f8ee', icon: User },
  { label: '待办工单', value: ov.value.pendingWorkOrder ?? 0, bg: '#fff1e6', icon: Van },
  { label: '在线率', value: ov.value.deviceOnlineRate ?? 0, bg: '#eff6ff', icon: DataLine }
])

const g = { left: 45, right: 20, top: 30, bottom: 30 }
const alarmOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['告警', '已处理'], right: 10 }, grid: g,
  xAxis: { type: 'category', data: alarm.value.dates || [] }, yAxis: { type: 'value' },
  series: [
    { name: '告警', type: 'line', smooth: true, data: alarm.value.totals || [], areaStyle: { opacity: 0.15 } },
    { name: '已处理', type: 'line', smooth: true, data: alarm.value.handled || [] }
  ]
}))
const accessOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['通行', '拒绝'], right: 10 }, grid: g,
  xAxis: { type: 'category', data: access.value.dates || [] }, yAxis: { type: 'value' },
  series: [
    { name: '通行', type: 'bar', data: access.value.totals || [] },
    { name: '拒绝', type: 'bar', data: access.value.denied || [] }
  ]
}))
const vehOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['进场', '出场'], right: 10 }, grid: g,
  xAxis: { type: 'category', data: veh.value.dates || [] }, yAxis: { type: 'value' },
  series: [
    { name: '进场', type: 'bar', data: veh.value.inTotal || [] },
    { name: '出场', type: 'bar', data: veh.value.outTotal || [] }
  ]
}))
const woOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, grid: g,
  xAxis: { type: 'category', data: ['待派', '已派', '处理中', '待验', '完成', '关闭'] }, yAxis: { type: 'value' },
  series: [{ type: 'bar', data: [wo.value.status0 || 0, wo.value.status1 || 0, wo.value.status2 || 0, wo.value.status3 || 0, wo.value.status4 || 0, wo.value.status5 || 0], itemStyle: { color: '#36d1ff' } }]
}))

async function load() {
  const c = app.communityId
  const [o, a, ac, v, w] = await Promise.all([
    statApi.overview(c),
    statApi.alarmTrend(c, 7),
    statApi.accessTrend(c, 7),
    statApi.vehicleTrend(c, 7),
    statApi.workOrderSummary(c)
  ])
  ov.value = o
  alarm.value = a
  access.value = ac
  veh.value = v
  wo.value = w
}
onMounted(load)
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: var(--space-lg); }
.stat-content { display: flex; flex-direction: column; }
</style>