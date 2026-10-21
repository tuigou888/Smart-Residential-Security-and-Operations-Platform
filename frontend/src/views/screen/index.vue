<template>
  <div class="screen-wrap">
    <div class="screen-title">智慧安防小区运营指挥中心</div>
    <div class="time-text">{{ timeText }}</div>

    <el-row :gutter="16">
      <el-col :span="4" v-for="k in kpis" :key="k.label">
        <div class="screen-panel" style="text-align: center">
          <div class="screen-num">{{ k.value }}</div>
          <div class="panel-label">{{ k.label }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="screen-panel" style="text-align: center">
          <div class="screen-num" :style="{ color: safety.color }">{{ safety.score }}</div>
          <div class="panel-label">安全指数 · {{ safety.level }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="16">
        <div class="screen-panel">
          <div class="panel-title">实时告警</div>
          <el-table :data="alarms" size="small" :row-style="{ background: 'transparent', color: '#cfe8ff' }">
            <el-table-column prop="alarmNo" label="编号" width="170" />
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="alarmLevel" label="级别" width="80" :formatter="lv" />
            <el-table-column prop="status" label="状态" width="90" :formatter="st" />
            <el-table-column prop="occurTime" label="时间" min-width="150" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="screen-panel">
          <div class="panel-title">告警趋势</div>
          <EChart :option="alarmOpt" height="260px" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="8">
        <div class="screen-panel">
          <div class="panel-title">设备状态</div>
          <EChart :option="devOpt" height="240px" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="screen-panel">
          <div class="panel-title">通行趋势</div>
          <EChart :option="accessOpt" height="240px" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="screen-panel">
          <div class="panel-title">视频轮巡</div>
          <div class="rotate">
            <div v-for="c in channels" :key="c.id" class="tile">
              <div class="tile-name">{{ c.channelName }}</div>
              <div class="tile-dot">●</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { screenApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rt = ref<any>({})
const alarms = ref<any[]>([])
const dev = ref<any>({})
const channels = ref<any[]>([])
const timeText = ref('')
let timer: any = null

const kpis = computed(() => [
  { label: '今日告警', value: rt.value.todayAlarm ?? 0 },
  { label: '待处理告警', value: rt.value.pendingAlarm ?? 0 },
  { label: '今日通行', value: rt.value.todayAccess ?? 0 },
  { label: '今日车辆', value: (rt.value.todayVehicleIn ?? 0) + (rt.value.todayVehicleOut ?? 0) },
  { label: '今日访客', value: rt.value.todayVisitor ?? 0 },
  { label: '待办工单', value: rt.value.pendingWorkOrder ?? 0 }
])

const safety = computed(() => {
  const s: any = rt.value.safetyIndex || {}
  const score = s.score ?? 0
  const color = score >= 90 ? '#36d1ff' : score >= 75 ? '#7af9ff' : score >= 60 ? '#ffd166' : '#ff6b6b'
  return { score, level: s.level || '-', color }
})

const g = { left: 40, right: 16, top: 24, bottom: 24 }
const alarmOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { textStyle: { color: '#cfe8ff' }, data: ['告警', '已处理'], right: 10 }, grid: g,
  xAxis: { type: 'category', data: (rt.value.alarmTrend?.dates) || [], axisLabel: { color: '#cfe8ff' } },
  yAxis: { type: 'value', axisLabel: { color: '#cfe8ff' } },
  series: [
    { name: '告警', type: 'line', smooth: true, data: rt.value.alarmTrend?.totals || [], areaStyle: { opacity: 0.15 } },
    { name: '已处理', type: 'line', smooth: true, data: rt.value.alarmTrend?.handled || [] }
  ]
}))

const accessOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { textStyle: { color: '#cfe8ff' }, data: ['通行', '拒绝'], right: 10 }, grid: g,
  xAxis: { type: 'category', data: (rt.value.accessTrend?.dates) || [], axisLabel: { color: '#cfe8ff' } },
  yAxis: { type: 'value', axisLabel: { color: '#cfe8ff' } },
  series: [
    { name: '通行', type: 'bar', data: rt.value.accessTrend?.totals || [] },
    { name: '拒绝', type: 'bar', data: rt.value.accessTrend?.denied || [] }
  ]
}))

const devOpt = computed(() => ({
  tooltip: { trigger: 'item' }, legend: { textStyle: { color: '#cfe8ff' }, bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    label: { color: '#cfe8ff' },
    data: [
      { name: '在线', value: dev.value.online ?? 0, itemStyle: { color: '#36d1ff' } },
      { name: '离线', value: dev.value.offline ?? 0, itemStyle: { color: '#94a3b8' } },
      { name: '故障', value: dev.value.fault ?? 0, itemStyle: { color: '#ef4444' } }
    ]
  }]
}))

function lv(_: any, r: any) { return ({ 1: '紧急', 2: '重要', 3: '一般' } as any)[r.alarmLevel] || r.alarmLevel }
function st(_: any, r: any) { return ({ 0: '未处理', 1: '处理中', 2: '已处理', 3: '误报' } as any)[r.status] || r.status }

async function load() {
  const c = app.communityId
  const [r, a, d, rot] = await Promise.all([
    screenApi.realtime(c), screenApi.latestAlarm(c, 8), screenApi.deviceStatus(c), screenApi.rotation(c, 4)
  ])
  rt.value = r
  alarms.value = a || []
  dev.value = d || {}
  channels.value = (rot?.channels || []).map((x: any) => x.channel || x)
}

function tick() {
  const d = new Date()
  timeText.value = d.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  load()
  tick()
  timer = setInterval(() => { tick(); load() }, 30000)
})

onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.time-text {
  text-align: center;
  color: #93c5fd;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: var(--space-lg);
  font-family: var(--font-mono);
}
.rotate {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: var(--space-sm);
}
.tile {
  background: #06203c;
  border: 1px solid rgba(54, 209, 255, 0.25);
  border-radius: var(--radius-md);
  height: 90px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.tile-name {
  font-size: 13px;
  color: var(--color-text-on-dark);
  font-weight: 500;
}
.tile-dot {
  color: #36d1ff;
  font-size: 18px;
}
.mt16 {
  margin-top: var(--space-lg);
}
</style>