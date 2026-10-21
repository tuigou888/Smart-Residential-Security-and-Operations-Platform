<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="keyword" placeholder="路线名称" clearable style="width: 180px" />
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增路线</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="routeName" label="路线名称" width="180" />
      <el-table-column prop="routeCode" label="路线编码" width="140" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="pointCount" label="点位数" width="90" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑路线' : '新增路线'" width="720px">
      <el-form :model="form" label-width="92px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="路线编码" required>
              <el-input v-model="form.routeCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路线名称" required>
              <el-input v-model="form.routeName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="点位编排">
          <div class="route-editor">
            <div class="route-col">
              <div class="col-title">可选点位</div>
              <el-checkbox-group v-model="selectedIds">
                <div v-for="p in allPoints" :key="p.id" class="point-item">
                  <el-checkbox :value="p.id">{{ p.pointName }} ({{ p.pointCode }})</el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
            <div class="route-col">
              <div class="col-title">巡更顺序</div>
              <div v-for="(p, i) in orderedPoints" :key="p.id" class="order-item">
                <span class="order-no">{{ i + 1 }}</span>
                <span class="order-name">{{ p.pointName }}</span>
                <el-button link :disabled="i === 0" @click="move(i, -1)">上移</el-button>
                <el-button link :disabled="i === orderedPoints.length - 1" @click="move(i, 1)">下移</el-button>
              </div>
              <el-empty v-if="!orderedPoints.length" description="尚未选择点位" :image-size="60" />
            </div>
          </div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { patrolApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const rows = ref<any[]>([])
const allPoints = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = reactive<any>({})
const selectedIds = ref<number[]>([])

const orderedPoints = computed(() =>
  selectedIds.value.map((id) => allPoints.value.find((p) => p.id === id)).filter(Boolean)
)

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await patrolApi.routes({ pageNum: pageNum.value, pageSize, routeName: keyword.value || undefined, communityId: app.communityId })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadPoints() {
  allPoints.value = (await patrolApi.pointOptions(app.communityId)) || []
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  selectedIds.value = []
}

function openAdd() {
  resetForm()
  form.status = 1
  dialog.value = true
}

async function openEdit(row: any) {
  resetForm()
  const detail = await patrolApi.routeDetail(row.id)
  Object.assign(form, detail.route)
  selectedIds.value = detail.pointIds || []
  dialog.value = true
}

function move(index: number, offset: number) {
  const target = index + offset
  if (target < 0 || target >= selectedIds.value.length) return
  const list = [...selectedIds.value]
  ;[list[index], list[target]] = [list[target], list[index]]
  selectedIds.value = list
}

async function save() {
  if (!form.routeCode || !form.routeName) {
    ElMessage.warning('请填写路线编码与名称')
    return
  }
  saving.value = true
  try {
    await patrolApi.saveRoute({ route: { ...form, communityId: form.communityId || app.communityId }, pointIds: selectedIds.value })
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该路线?', '提示', { type: 'warning' })
  await patrolApi.removeRoute(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  await loadPoints()
  await load(1)
})
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
.route-editor { display: flex; gap: 16px; width: 100%; }
.route-col { flex: 1; border: 1px solid #e5e7eb; border-radius: 6px; padding: 8px; min-height: 220px; max-height: 320px; overflow: auto; }
.col-title { font-weight: 600; margin-bottom: 8px; }
.point-item { margin: 4px 0; }
.order-item { display: flex; align-items: center; gap: 8px; padding: 4px 0; border-bottom: 1px dashed #eee; }
.order-no { width: 22px; height: 22px; line-height: 22px; text-align: center; border-radius: 50%; background: #409eff; color: #fff; font-size: 12px; }
.order-name { flex: 1; }
</style>
