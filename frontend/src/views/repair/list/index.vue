<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="keyword" placeholder="标题/报修编号" clearable style="width: 180px" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增报修</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="repairNo" label="报修编号" width="170" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="repairType" label="类型" width="90">
        <template #default="{ row }">{{ typeMap[row.repairType] || row.repairType }}</template>
      </el-table-column>
      <el-table-column prop="contactPhone" label="联系电话" width="130" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="160" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="primary" @click="dispatch(row)">派单</el-button>
          <el-button v-if="row.status === 1" link type="primary" @click="start(row)">开始处理</el-button>
          <el-button v-if="row.status === 2" link type="success" @click="finish(row)">完成</el-button>
          <el-button v-if="row.status === 3" link type="warning" @click="evaluate(row)">评价</el-button>
          <el-button link @click="view(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="pageNum"
        :page-size="pageSize" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" title="新增报修" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="业主ID" required>
          <el-input v-model.number="form.ownerId" />
        </el-form-item>
        <el-form-item label="房屋ID">
          <el-input v-model.number="form.roomId" />
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="form.repairType" style="width: 100%">
            <el-option v-for="(v, k) in typeMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="期望上门">
          <el-date-picker v-model="form.expectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="报修详情" width="560px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="报修编号">{{ current.repairNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ current.content }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[current.status] }}</el-descriptions-item>
        <el-descriptions-item label="关联工单">{{ current.workOrderId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ current.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { repairApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const statusMap: Record<number, string> = { 0: '待派单', 1: '已派单', 2: '处理中', 3: '已完成', 4: '已评价' }
const typeMap: Record<number, string> = { 1: '水电', 2: '门窗', 3: '电梯', 4: '其他' }
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const status = ref<number | undefined>()
const loading = ref(false)
const dialog = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const current = ref<any>(null)
const form = reactive<any>({})

function statusTag(s: number) {
  return ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'success' } as any)[s] || 'info'
}

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await repairApi.list({ pageNum: pageNum.value, pageSize, keyword: keyword.value || undefined, status: status.value, communityId: app.communityId })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.keys(form).forEach((k) => delete form[k])
  form.repairType = 1
  dialog.value = true
}

async function save() {
  if (!form.ownerId || !form.title) {
    ElMessage.warning('请填写业主ID与标题')
    return
  }
  saving.value = true
  try {
    await repairApi.create({ ...form, communityId: form.communityId || app.communityId })
    ElMessage.success('提交成功')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function dispatch(row: any) {
  await ElMessageBox.confirm('确认派单并生成关联工单?', '派单', { type: 'warning' })
  const { value } = await ElMessageBox.prompt('请输入处理人ID', '派单', { inputPattern: /^\d+$/, inputErrorMessage: '请输入数字ID' })
  await repairApi.dispatch({ id: row.id, assigneeId: Number(value), assigneeName: '处理人' + value })
  ElMessage.success('派单成功')
  load()
}

async function start(row: any) {
  await repairApi.start(row.id)
  ElMessage.success('已开始处理')
  load()
}

async function finish(row: any) {
  const { value } = await ElMessageBox.prompt('处理说明', '完成报修', { inputValue: '已处理完成' })
  await repairApi.finish(row.id, value)
  ElMessage.success('已完成')
  load()
}

async function evaluate(row: any) {
  const { value } = await ElMessageBox.prompt('评价内容', '评价', { inputValue: '服务满意' })
  await repairApi.evaluate(row.id, value)
  ElMessage.success('评价成功')
  load()
}

function view(row: any) {
  current.value = row
  detailVisible.value = true
}

onMounted(() => load(1))
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
</style>
