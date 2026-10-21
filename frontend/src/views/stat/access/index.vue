<template>
  <el-card class="page-card">
    <div class="panel-title">通行分析</div>
    <EChart :option="opt" height="340px" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const t = ref<any>({})

const opt = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['通行', '拒绝'], right: 10 }, grid: { left: 45, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: t.value.dates || [] }, yAxis: { type: 'value' },
  series: [
    { name: '通行', type: 'bar', data: t.value.totals || [] },
    { name: '拒绝', type: 'bar', data: t.value.denied || [] }
  ]
}))

onMounted(async () => { t.value = await statApi.accessTrend(app.communityId, 7) })
</script>
