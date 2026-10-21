<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-form :inline="true" :model="query">
          <el-form-item v-for="f in searchFields" :key="f.prop" :label="f.label">
            <el-input v-if="!f.type || f.type === 'input'" v-model="query[f.prop]" :placeholder="'请输入' + f.label"
              clearable style="width: 160px" />
            <el-select v-else-if="f.type === 'select'" v-model="query[f.prop]" :placeholder="'请选择'" clearable
              style="width: 160px">
              <el-option v-for="o in f.options" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div v-if="createApi">
        <el-button type="primary" :icon="Plus" @click="openAdd">{{ addText }}</el-button>
      </div>
    </div>

    <el-table :data="rows" border stripe v-loading="loading" style="width: 100%">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label"
        :width="c.width" :min-width="c.minWidth" :formatter="c.formatter" show-overflow-tooltip>
        <template #default="scope" v-if="c.slot">
          <component :is="c.slot" :row="scope.row" />
        </template>
      </el-table-column>
      <el-table-column v-if="createApi || removeApi" label="操作" :width="120" fixed="right">
        <template #default="scope">
          <el-button v-if="createApi" link type="primary" :icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="removeApi" link type="danger" :icon="Delete"
            @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
        :page-sizes="[10, 20, 50]" v-model:current-page="pageNum" v-model:page-size="pageSize"
        @current-change="load" @size-change="load(1)" />
    </div>

    <el-dialog v-model="dialog" :title="dialogTitle" width="560px">
      <el-form :model="formData" label-width="92px">
        <el-form-item v-for="f in formFields" :key="f.prop" :label="f.label" :required="f.required">
          <el-input v-if="!f.type || f.type === 'input'" v-model="formData[f.prop]" :placeholder="'请输入' + f.label" />
          <el-input v-else-if="f.type === 'textarea'" v-model="formData[f.prop]" type="textarea" :rows="3" />
          <el-input v-else-if="f.type === 'number'" v-model.number="formData[f.prop]" type="number" />
          <el-select v-else-if="f.type === 'select'" v-model="formData[f.prop]" clearable style="width: 100%">
            <el-option v-for="o in f.options" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-date-picker v-else-if="f.type === 'date'" v-model="formData[f.prop]" type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps<{
  title: string
  columns: any[]
  fetch: (params: any) => Promise<{ list: any[]; total: number }>
  formFields?: any[]
  searchFields?: any[]
  createApi?: (d: any) => Promise<any>
  updateApi?: (d: any) => Promise<any>
  removeApi?: (id: any) => Promise<any>
  addText?: string
  idKey?: string
}>()

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive<any>({})
const idKey = props.idKey || 'id'

const dialog = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const isEdit = ref(false)
const formData = reactive<any>({})

onMounted(() => load(1))

function buildParams() {
  const p: any = { pageNum: pageNum.value, pageSize: pageSize.value }
  Object.keys(query).forEach((k) => {
    if (query[k] !== '' && query[k] !== null && query[k] !== undefined) p[k] = query[k]
  })
  return p
}

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await props.fetch(buildParams())
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.keys(query).forEach((k) => (query[k] = ''))
  load(1)
}

function openAdd() {
  Object.keys(formData).forEach((k) => delete formData[k])
  isEdit.value = false
  dialogTitle.value = '新增' + props.title
  dialog.value = true
}

function openEdit(row: any) {
  Object.keys(formData).forEach((k) => delete formData[k])
  Object.assign(formData, row)
  isEdit.value = true
  dialogTitle.value = '编辑' + props.title
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    const api = isEdit.value ? props.updateApi : props.createApi
    if (!api) return
    await api(formData)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该记录?', '提示', { type: 'warning' })
  try {
    await props.removeApi!(row[idKey])
    ElMessage.success('删除成功')
    load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

defineExpose({ load })
</script>

<style scoped>
.mb16 { margin-bottom: var(--space-lg); }
.mt16 { margin-top: var(--space-lg); }
</style>
