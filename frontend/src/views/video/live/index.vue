<template>
  <div class="page-card">
    <el-row :gutter="16">
      <el-col :span="7">
        <div class="panel-title">视频通道</div>
        <el-tree :data="tree" :props="{ label: 'label', children: 'children' }" @node-click="onNode" node-key="id"
          default-expand-all style="background:#fafafa;padding:8px;border-radius:6px" />
      </el-col>
      <el-col :span="17">
        <div class="panel-title">实时预览</div>
        <div v-if="cur" class="player">
          <div class="ph">视频流由流媒体服务(ZLMediaKit)推送，演示环境展示流地址</div>
          <el-descriptions :column="1" border class="mt16">
            <el-descriptions-item label="通道">{{ cur.channelName }}</el-descriptions-item>
            <el-descriptions-item label="国标编码">{{ cur.gbCode }}</el-descriptions-item>
            <el-descriptions-item label="实时流地址">
              <el-link type="primary" :href="liveUrl" target="_blank" v-if="liveUrl">{{ liveUrl }}</el-link>
              <span v-else>—</span>
            </el-descriptions-item>
          </el-descriptions>
          <el-button class="mt16" :icon="VideoPlay" @click="snap">截图</el-button>
        </div>
        <el-empty v-else description="请选择左侧通道" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import { videoApi } from '@/api'
import { useAppStore } from '@/store/app'

const app = useAppStore()
const tree = ref<any[]>([])
const cur = ref<any>(null)
const liveUrl = ref('')

async function load() {
  const t = await videoApi.channelTree(app.communityId)
  tree.value = (t || []).map((g: any) => ({
    id: 'g' + g.id, label: g.label, children: (g.children || []).map((c: any) => ({ id: c.id, label: c.label, channel: c }))
  }))
}
function onNode(node: any) {
  if (!node.channel) return
  cur.value = node.channel
  videoApi.live(node.channel.id).then((r: any) => (liveUrl.value = r?.playUrl || r?.url || r?.flv || '')).catch(() => (liveUrl.value = ''))
}
async function snap() {
  if (!cur.value) return
  await videoApi.ptz({ channelId: cur.value.id, direction: 'snapshot', speed: 4 }).catch(() => {})
  ElMessage.info('已触发截图')
}
onMounted(load)
</script>

<style scoped>
.player { background: #0b2138; border-radius: 8px; padding: 16px; color: #cfe8ff; min-height: 220px; }
.ph { opacity: .7; }
.mt16 { margin-top: var(--space-lg); }
</style>
