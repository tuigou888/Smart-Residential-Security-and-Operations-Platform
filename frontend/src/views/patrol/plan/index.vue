<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="keyword" placeholder="计划名称" clearable style="width: 180px" />
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增计划</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="planName" label="计划名称" width="180" />
      <el-table-column label="路线" width="180">
        <template #default="{ row }">{{ routeName(row.routeId) }}</template>
      </el-table-column>
      <el-table-column prop="patrolType" label="类型" width="100">
        <template #default="{ row }">{{ typeText(row.patrolType) }}</template>
      </el-table-column>
      <el-table-column label="时间段" width="140">
        <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="intervalMinutes" label="间隔(分)" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="pageNum"
        :page-size="pageSize" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑计划' : '新增计划'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划名称" required>
          <el-input v-model="form.planName" />
        </el-form-item>
        <el-form-item label="巡更路线" required>
          <el-select v-model="form.routeId" style="width: 100%" placeholder="请选择路线">
            <el-option v-for="r in routes" :key="r.id" :label="r.routeName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.patrolType" style="width: 100%">
            <el-option label="日常" :value="1" />
            <el-option label="夜间" :value="2" />
            <el-option label="节假日" :value="3" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-input v-model="form.startTime" placeholder="20:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-input v-model="form.endTime" placeholder="06:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="间隔(分钟)">
          <el-input-number v-model="form.intervalMinutes" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="巡更人员ID">
          <el-input v-model="form.assignUserIds" placeholder="多个用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { patrolApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const routes = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = reactive<any>({})

function typeText(t: number) {
  return { 1: '日常', 2: '夜间', 3: '节假日' }[t] || t
}
function routeName(id: number) {
  return routes.value.find((r) => r.id === id)?.routeName || id
}

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await patrolApi.plans({ pageNum: pageNum.value, pageSize, planName: keyword.value || undefined, communityId: app.communityId })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  form.patrolType = 1
  form.startTime = '20:00'
  form.endTime = '06:00'
  form.intervalMinutes = 60
  form.status = 1
}

function openAdd() {
  resetForm()
  dialog.value = true
}
function openEdit(row: any) {
  resetForm()
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.planName || !form.routeId) {
    ElMessage.warning('请填写计划名称并选择路线')
    return
  }
  saving.value = true
  try {
    await patrolApi.savePlan({ ...form, communityId: form.communityId || app.communityId })
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该计划?', '提示', { type: 'warning' })
  await patrolApi.removePlan(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  routes.value = (await patrolApi.routeOptions(app.communityId)) || []
  await load(1)
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
</style>
