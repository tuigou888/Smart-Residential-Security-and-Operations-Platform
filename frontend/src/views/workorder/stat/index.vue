<template>
  <el-card class="page-card">
    <div class="flex-between mb16">
      <div class="panel-title" style="border: none; padding: 0; margin: 0">工单统计</div>
    </div>
    <EChart :option="opt" height="340px" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const s = ref<any>({})

const opt = computed(() => ({
  tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: ['待派', '已派', '处理中', '待验', '完成', '关闭'] }, yAxis: { type: 'value' },
  series: [{ type: 'bar', data: [s.value.status0 || 0, s.value.status1 || 0, s.value.status2 || 0, s.value.status3 || 0, s.value.status4 || 0, s.value.status5 || 0], itemStyle: { color: '#36d1ff' } }]
}))

onMounted(async () => { s.value = await statApi.workOrderSummary(app.communityId) })
</script>

<style scoped>
.mb16 { margin-bottom: var(--space-lg); }
</style>