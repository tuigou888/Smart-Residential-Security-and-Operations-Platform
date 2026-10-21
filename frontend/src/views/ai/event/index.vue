<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="eventType" placeholder="事件类型" clearable style="width: 180px" />
        <el-select v-model="handleStatus" placeholder="处理状态" clearable style="width: 150px">
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
          <el-option label="误报" :value="2" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="eventTime" label="事件时间" width="170" />
      <el-table-column prop="eventType" label="事件类型" width="150" />
      <el-table-column prop="sceneId" label="场景ID" width="140" />
      <el-table-column prop="channelId" label="通道ID" width="140" />
      <el-table-column prop="confidence" label="置信度" width="100" />
      <el-table-column prop="handleStatus" label="处理状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.handleStatus === 0 ? 'warning' : row.handleStatus === 2 ? 'info' : 'success'">
            {{ statusText(row.handleStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.handleStatus === 0" link type="primary" @click="handle(row, 1)">已处理</el-button>
          <el-button v-if="row.handleStatus === 0" link type="info" @click="handle(row, 2)">误报</el-button>
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
import { aiApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const eventType = ref('')
const handleStatus = ref<number | undefined>()
const loading = ref(false)

function statusText(s: number) {
  return { 0: '未处理', 1: '已处理', 2: '误报' }[s] || s
}

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await aiApi.events({ pageNum: pageNum.value, pageSize, communityId: app.communityId, eventType: eventType.value || undefined, handleStatus: handleStatus.value })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handle(row: any, status: number) {
  await aiApi.handleEvent(row.id, status)
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
