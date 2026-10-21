<template>
  <div class="page-card">
    <div class="panel-title">设备在线状态分布</div>
    <el-row :gutter="16">
      <el-col :span="10">
        <EChart :option="pieOption" height="320px" />
      </el-col>
      <el-col :span="14">
        <el-table :data="typeRows" border>
          <el-table-column prop="name" label="设备类型" />
          <el-table-column prop="value" label="数量" width="120" />
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import EChart from '@/views/generic/EChart.vue'
import { iotApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const status = ref<any>({})
const typeRows = ref<any[]>([])
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { name: '在线', value: status.value.online ?? 0, itemStyle: { color: '#36d1ff' } },
      { name: '离线', value: status.value.offline ?? 0, itemStyle: { color: '#909399' } },
      { name: '故障', value: status.value.fault ?? 0, itemStyle: { color: '#f56c6c' } }
    ]
  }]
}))
async function load() {
  status.value = await iotApi.deviceStatus(app.communityId)
  const t = await iotApi.deviceTypeStat(app.communityId)
  typeRows.value = t || []
}
onMounted(load)
</script>
