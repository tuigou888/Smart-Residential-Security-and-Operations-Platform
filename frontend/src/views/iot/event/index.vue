<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="deviceId" placeholder="设备ID" clearable style="width: 150px" />
        <el-input v-model="eventCode" placeholder="事件编码" clearable style="width: 150px" />
        <el-select v-model="handleStatus" placeholder="处理状态" clearable style="width: 140px">
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="eventTime" label="事件时间" width="170" />
      <el-table-column prop="deviceId" label="设备ID" width="140" />
      <el-table-column prop="eventCode" label="事件编码" width="130" />
      <el-table-column prop="eventName" label="事件名称" width="150" />
      <el-table-column prop="eventData" label="事件数据" min-width="200" show-overflow-tooltip />
      <el-table-column prop="handleStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.handleStatus === 0 ? 'warning' : 'success'">
            {{ row.handleStatus === 0 ? '未处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.handleStatus === 0" link type="primary" @click="handle(row)">处置</el-button>
        </template>
      </el-table-column>
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
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { iotApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const deviceId = ref('')
const eventCode = ref('')
const handleStatus = ref<number | undefined>()
const loading = ref(false)

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await iotApi.deviceEvents({
      pageNum: pageNum.value, pageSize, communityId: app.communityId,
      deviceId: deviceId.value || undefined, eventCode: eventCode.value || undefined,
      handleStatus: handleStatus.value
    })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handle(row: any) {
  await iotApi.handleDeviceEvent(row.id)
  ElMessage.success('处置成功')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
</style>
