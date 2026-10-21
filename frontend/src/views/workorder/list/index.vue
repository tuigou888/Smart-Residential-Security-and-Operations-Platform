<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <el-form :inline="true" :model="q">
        <el-form-item label="标题"><el-input v-model="q.title" clearable placeholder="标题" style="width: 150px" /></el-form-item>
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
      <el-button type="primary" :icon="Plus" @click="openCreate">新建工单</el-button>
    </div>

    <el-table :data="rows" border v-loading="loading" style="width: 100%">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="orderTypeName" label="类型" width="100" />
      <el-table-column prop="priority" label="优先级" width="90" :formatter="priFmt" />
      <el-table-column prop="status" label="状态" width="100" :formatter="stFmt" />
      <el-table-column prop="assigneeName" label="处理人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex-between mt16">
      <span />
      <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
        :page-sizes="[10, 20, 50]" v-model:current-page="pageNum" v-model:page-size="pageSize"
        @current-change="load" @size-change="load(1)" />
    </div>

    <el-drawer v-model="detail" title="工单详情" size="560px">
      <template v-if="cur">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单号">{{ cur.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ cur.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ cur.orderTypeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ priText(cur.priority) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ stText(cur.status) }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ cur.assigneeName || '未派单' }}</el-descriptions-item>
          <el-descriptions-item label="内容">{{ cur.content }}</el-descriptions-item>
          <el-descriptions-item label="SLA截止">{{ cur.slaDeadline }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>流转记录</el-divider>
        <el-timeline v-if="flows.length">
          <el-timeline-item v-for="f in flows" :key="f.id" :timestamp="f.createTime">
            <b>{{ actionText(f.action) }}</b> · {{ f.operatorName }}
            <div style="color:#909399">{{ f.actionDetail }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无流转" />
        <el-divider />
        <div class="actions">
          <el-button v-if="cur.status === 0" type="primary" @click="act('assign')">派单</el-button>
          <el-button v-if="[0,1].includes(cur.status)" @click="act('accept')">接单</el-button>
          <el-button v-if="[1,2].includes(cur.status)" @click="act('handle')">处理</el-button>
          <el-button v-if="[1,2].includes(cur.status)" type="success" @click="act('finish')">完成</el-button>
          <el-button v-if="cur.status === 3" type="success" @click="act('verify')">验收</el-button>
          <el-button v-if="cur.status === 4" @click="act('evaluate')">评价</el-button>
          <el-button v-if="![5].includes(cur.status)" type="danger" @click="act('close')">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="actDlg" :title="actTitle" width="480px">
      <el-form label-width="84px">
        <el-form-item v-if="actType === 'assign'" label="处理人">
          <el-input v-model="actForm.assigneeName" placeholder="处理人姓名" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="actForm.detail" type="textarea" :rows="3" /></el-form-item>
        <el-form-item v-if="['handle','finish'].includes(actType)" label="附件URL"><el-input v-model="actForm.attachments" placeholder="可选" /></el-form-item>
        <el-form-item v-if="actType === 'evaluate'" label="评分">
          <el-rate :model-value="actForm.score" @update:model-value="(v: any) => (actForm.score = v)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAct">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createDlg" title="新建工单" width="520px">
      <el-form :model="cf" label-width="84px">
        <el-form-item label="标题"><el-input v-model="cf.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="cf.orderType" style="width: 100%">
            <el-option value="repair" label="报修" /><el-option value="inspect" label="巡检" />
            <el-option value="complaint" label="投诉" /><el-option value="alarm" label="告警工单" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="cf.priority" style="width: 100%">
            <el-option :value="1" label="紧急" /><el-option :value="2" label="重要" /><el-option :value="3" label="一般" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="cf.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View } from '@element-plus/icons-vue'
import { workOrderApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const q = reactive<any>({})
const statusOpts = [
  { value: 0, label: '待派单' }, { value: 1, label: '已派单' }, { value: 2, label: '处理中' },
  { value: 3, label: '待验收' }, { value: 4, label: '已完成' }, { value: 5, label: '已关闭' }
]

const detail = ref(false)
const cur = ref<any>(null)
const flows = ref<any[]>([])
const actDlg = ref(false)
const actType = ref('')
const actTitle = ref('')
const saving = ref(false)
const actForm = reactive<any>({ detail: '', attachments: '', score: 5, assigneeName: '' })
const actApiMap: any = {
  assign: workOrderApi.assign, accept: workOrderApi.accept, handle: workOrderApi.handle,
  finish: workOrderApi.finish, verify: workOrderApi.verify, evaluate: workOrderApi.evaluate, close: workOrderApi.close
}

const createDlg = ref(false)
const cf = reactive<any>({ title: '', orderType: 'repair', priority: 3, content: '' })

function priFmt(_: any, r: any) { return priText(r.priority) }
function priText(p: number) { return ({ 1: '紧急', 2: '重要', 3: '一般' } as any)[p] || p }
function stFmt(_: any, r: any) { return stText(r.status) }
function stText(s: number) { return ({ 0: '待派单', 1: '已派单', 2: '处理中', 3: '待验收', 4: '已完成', 5: '已关闭' } as any)[s] || s }
function actionText(a: string) { return ({ create: '创建', assign: '派单', accept: '接单', handle: '处理', verify: '验收', evaluate: '评价', upgrade: '关闭' } as any)[a] || a }

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value, communityId: app.communityId }
    Object.assign(params, q)
    const res = await workOrderApi.list(params)
    rows.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
function reset() { Object.keys(q).forEach((k) => (q[k] = '')); load(1) }

async function openDetail(row: any) {
  const d = await workOrderApi.detail(row.id)
  cur.value = d.order
  flows.value = d.flows || []
  detail.value = true
}
function act(type: string) {
  actType.value = type
  actTitle.value = ({ assign: '派单', accept: '接单', handle: '处理', finish: '完成', verify: '验收', evaluate: '评价', close: '关闭' } as any)[type]
  actForm.detail = ''
  actDlg.value = true
}
async function submitAct() {
  saving.value = true
  try {
    const payload: any = { orderId: cur.value.id, detail: actForm.detail, attachments: actForm.attachments, pass: true, score: actForm.score, assigneeName: actForm.assigneeName }
    await actApiMap[actType.value](payload)
    ElMessage.success('操作成功')
    actDlg.value = false
    detail.value = false
    load()
  } finally { saving.value = false }
}
function openCreate() { createDlg.value = true }
async function doCreate() {
  saving.value = true
  try {
    cf.communityId = app.communityId
    await workOrderApi.create(cf)
    ElMessage.success('工单已创建')
    createDlg.value = false
    load()
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.mt16 { margin-top: var(--space-lg); }
.actions { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
