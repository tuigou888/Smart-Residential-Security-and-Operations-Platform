<template>
  <div class="page-card">
    <el-row :gutter="16">
      <!-- 设备在线率 -->
      <el-col :span="8">
        <el-card class="page-card">
          <div class="panel-title">设备在线率</div>
          <EChart :option="gaugeOpt" height="280px" />
        </el-card>
      </el-col>
      <!-- 运行状态 -->
      <el-col :span="8">
        <el-card class="page-card">
          <div class="panel-title">运行状态</div>
          <EChart :option="pieOpt" height="280px" />
        </el-card>
      </el-col>
      <!-- 类型分布 -->
      <el-col :span="8">
        <el-card class="page-card">
          <div class="panel-title">类型分布</div>
          <EChart :option="barOpt" height="280px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { statApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const d = ref<any>({})

const gaugeOpt = computed(() => ({
  series: [{ type: 'gauge', progress: { show: true }, detail: { formatter: '{value}%', fontSize: 24 },
    data: [{ value: d.value.onlineRate ?? 0, name: '在线率' }] }]
}))

const pieOpt = computed(() => ({
  tooltip: { trigger: 'item' }, legend: { bottom: 0 },
  series: [{ type: 'pie', radius: ['40%', '70%'], data: [
    { name: '在线', value: d.value.online ?? 0, itemStyle: { color: '#36d1ff' } },
    { name: '离线', value: d.value.offline ?? 0, itemStyle: { color: '#94a3b8' } },
    { name: '故障', value: d.value.fault ?? 0, itemStyle: { color: '#ef4444' } }]
  }]
}))

const barOpt = computed(() => ({
  tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: (d.value.byType || []).map((t: any) => t.name) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: (d.value.byType || []).map((t: any) => t.value) }]
}))

onMounted(async () => { d.value = await statApi.deviceOnlineRate(app.communityId) })
</script>