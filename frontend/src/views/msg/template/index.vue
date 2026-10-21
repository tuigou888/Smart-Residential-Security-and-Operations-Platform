<template>
  <div class="page-card">
    <div class="flex-between mb16">
      <div class="search">
        <el-input v-model="keyword" placeholder="模板名称" clearable style="width: 180px" />
        <el-select v-model="channelType" placeholder="渠道" clearable style="width: 140px">
          <el-option v-for="(v, k) in channelMap" :key="k" :label="v" :value="Number(k)" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load(1)">查询</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增模板</el-button>
    </div>

    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="templateCode" label="模板编码" width="160" />
      <el-table-column prop="templateName" label="模板名称" width="180" />
      <el-table-column prop="channelType" label="渠道" width="100">
        <template #default="{ row }">{{ channelMap[row.channelType] }}</template>
      </el-table-column>
      <el-table-column prop="contentTemplate" label="模板内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="preview(row)">预览</el-button>
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑模板' : '新增模板'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板编码" required>
          <el-input v-model="form.templateCode" />
        </el-form-item>
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="form.channelType" style="width: 100%">
            <el-option v-for="(v, k) in channelMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" required>
          <el-input v-model="form.contentTemplate" type="textarea" :rows="4" placeholder="支持 {ownerName} 等变量" />
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

    <el-dialog v-model="previewVisible" title="模板预览" width="520px">
      <el-input v-model="previewText" type="textarea" :rows="4" readonly />
      <div class="mt16 hint">示例参数：ownerName=张三，communityName=锦绣家园，alarmType=周界入侵</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { msgApi } from '@/api'

const channelMap: Record<number, string> = { 1: '站内信', 2: '短信', 3: '推送', 4: '语音' }
const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const channelType = ref<number | undefined>()
const loading = ref(false)
const dialog = ref(false)
const previewVisible = ref(false)
const previewText = ref('')
const saving = ref(false)
const form = reactive<any>({})

async function load(p?: number) {
  if (p) pageNum.value = p
  loading.value = true
  try {
    const res = await msgApi.templates({ pageNum: pageNum.value, pageSize, templateName: keyword.value || undefined, channelType: channelType.value })
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.keys(form).forEach((k) => delete form[k])
  form.channelType = 1
  form.status = 1
  dialog.value = true
}
function openEdit(row: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.templateCode || !form.templateName || !form.contentTemplate) {
    ElMessage.warning('请填写编码、名称与内容')
    return
  }
  saving.value = true
  try {
    await msgApi.saveTemplate(form)
    ElMessage.success('保存成功')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该模板?', '提示', { type: 'warning' })
  await msgApi.removeTemplate(row.id)
  ElMessage.success('删除成功')
  load()
}

async function preview(row: any) {
  previewText.value = await msgApi.renderTemplate({
    templateId: row.id,
    params: { ownerName: '张三', communityName: '锦绣家园', alarmType: '周界入侵' }
  })
  previewVisible.value = true
}

onMounted(() => load(1))
</script>

<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.search { display: flex; gap: 8px; }
.hint { color: #6b7280; font-size: 12px; }
</style>
