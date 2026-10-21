<template>
  <el-card class="page-card">
    <div class="panel-title">告警分析</div>
    <EChart :option="opt" height="340px" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const trend = ref<any>({})

const opt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['告警', '已处理'], right: 10 }, grid: { left: 45, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: trend.value.dates || [] }, yAxis: { type: 'value' },
  series: [
    { name: '告警', type: 'line', smooth: true, data: trend.value.totals || [], areaStyle: { opacity: 0.15 } },
    { name: '已处理', type: 'line', smooth: true, data: trend.value.handled || [] }
  ]
}))

onMounted(async () => { trend.value = await statApi.alarmTrend(app.communityId, 7) })
</script>
