<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <el-form :inline="true" :model="q">
        <el-form-item label="类型">
          <el-input v-model="q.alarmType" placeholder="如 intrusion" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="q.status" clearable placeholder="状态" style="width: 120px">
            <el-option v-for="s in statusOpts" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div>
        <el-button type="primary" :icon="Plus" @click="openCreate">模拟告警接入</el-button>
      </div>
    </div>

    <el-table :data="rows" border v-loading="loading" style="width: 100%">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="alarmNo" label="告警编号" width="170" />
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="alarmTypeName" label="类型" width="120" />
      <el-table-column prop="alarmLevel" label="级别" width="90" :formatter="lvFmt" />
      <el-table-column prop="sourceTypeName" label="来源" width="90" />
      <el-table-column prop="status" label="状态" width="100" :formatter="stFmt" />
      <el-table-column prop="occurTime" label="发生时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">处置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
        :page-sizes="[10, 20, 50]" v-model:current-page="pageNum" v-model:page-size="pageSize"
        @current-change="load" @size-change="load(1)" />
    </div>

    <el-drawer v-model="detail" title="告警详情与处置" size="520px">
      <template v-if="cur">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="告警编号">{{ cur.alarmNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ cur.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ cur.alarmTypeName }}</el-descriptions-item>
          <el-descriptions-item label="级别">{{ lvText(cur.alarmLevel) }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ cur.location }}</el-descriptions-item>
          <el-descriptions-item label="内容">{{ cur.content }}</el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ cur.occurTime }}</el-descriptions-item>
          <el-descriptions-item v-if="cur.snapshotUrl" label="抓拍">
            <el-image :src="cur.snapshotUrl" style="width: 200px" fit="cover" />
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ stText(cur.status) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <el-form label-width="80px" v-if="cur.status === 0 || cur.status === 1">
          <el-form-item label="处置方式">
            <el-select v-model="handleForm.handleType" style="width: 100%">
              <el-option :value="1" label="派单" />
              <el-option :value="2" label="电话联系" />
              <el-option :value="3" label="现场处置" />
              <el-option :value="4" label="复核关闭" />
            </el-select>
          </el-form-item>
          <el-form-item label="处置结果">
            <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="完成">
            <el-switch :model-value="handleForm.finish" @change="(v: any) => (handleForm.finish = v)" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="doHandle">提交处置</el-button>
            <el-button @click="markFalse">标记误报</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-drawer>

    <el-dialog v-model="createDlg" title="模拟告警接入" width="520px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="告警类型"><el-input v-model="createForm.alarmType" placeholder="intrusion" /></el-form-item>
        <el-form-item label="级别">
          <el-select v-model="createForm.alarmLevel" style="width: 100%">
            <el-option :value="1" label="紧急" /><el-option :value="2" label="重要" />
            <el-option :value="3" label="一般" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="createForm.title" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="createForm.location" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="createForm.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doCreate">接入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View } from '@element-plus/icons-vue'
import { alarmApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const q = reactive<any>({})
const statusOpts = [
  { value: 0, label: '未处理' }, { value: 1, label: '处理中' },
  { value: 2, label: '已处理' }, { value: 3, label: '误报' }
]

const detail = ref(false)
const cur = ref<any>(null)
const saving = ref(false)
const handleForm = reactive({ handleType: 3, handleResult: '', finish: true })

const createDlg = ref(false)
const createForm = reactive<any>({ alarmType: 'intrusion', alarmLevel: 3, title: '', location: '', content: '' })

function lvFmt(_: any, r: any) { return lvText(r.alarmLevel) }
function lvText(l: number) { return ({ 1: '紧急', 2: '重要', 3: '一般' } as any)[l] || l }
function stFmt(_: any, r: any) { return stText(r.status) }
function stText(s: number) { return ({ 0: '未处理', 1: '处理中', 2: '已处理', 3: '误报' } as any)[s] || s }

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value, communityId: app.communityId }
    Object.assign(params, q)
    const res = await alarmApi.list(params)
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function reset() { Object.keys(q).forEach((k) => (q[k] = '')); load(1) }

async function openDetail(row: any) {
  cur.value = await alarmApi.detail(row.id)
  detail.value = true
}
async function doHandle() {
  saving.value = true
  try {
    await alarmApi.handle({ alarmId: cur.value.id, handleType: handleForm.handleType, handleResult: handleForm.handleResult, finish: handleForm.finish })
    ElMessage.success('处置已提交')
    detail.value = false
    load()
  } finally { saving.value = false }
}
async function markFalse() {
  saving.value = true
  try {
    await alarmApi.markFalse({ alarmId: cur.value.id, reason: '误报' })
    ElMessage.success('已标记误报')
    detail.value = false
    load()
  } finally { saving.value = false }
}
function openCreate() { createDlg.value = true }
async function doCreate() {
  saving.value = true
  try {
    createForm.communityId = app.communityId
    createForm.sourceType = 2
    await alarmApi.receive(createForm)
    ElMessage.success('告警已接入')
    createDlg.value = false
    load()
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.mt16 { margin-top: var(--space-lg); }
</style>
