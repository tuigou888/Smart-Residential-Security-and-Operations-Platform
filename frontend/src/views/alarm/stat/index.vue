<template>
  <div class="dashboard-page">
    <!-- 统计卡片网格 -->
    <div class="grid-4 mb16">
      <div v-for="c in cards" :key="c.label" class="stat-card">
        <div class="stat-icon" :style="{ background: c.bg }"><el-icon :size="26"><component :is="c.icon" /></el-icon></div>
        <div class="stat-content">
          <div class="stat-num">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 告警趋势 -->
    <el-card class="page-card">
      <div class="panel-title">近7天告警趋势</div>
      <EChart :option="alarmOption" height="300px" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import EChart from '@/views/generic/EChart.vue'
import { alarmApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const ov = ref<any>({})
const alarm = ref<any>({})

const cards = computed(() => [
  { label: '今日告警', value: ov.value.todayAlarm ?? 0, icon: Warning, bg: '#fff1e6' },
  { label: '待处理告警', value: ov.value.pendingAlarm ?? 0, icon: Warning, bg: '#ffe7e7' }
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

async function load() {
  const c = app.communityId
  const [o, a] = await Promise.all([
    alarmApi.summary(c),
    alarmApi.trend(c, 7)
  ])
  ov.value = o
  alarm.value = a
}
onMounted(load)
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: var(--space-lg); }
.stat-content { display: flex; flex-direction: column; }
</style>