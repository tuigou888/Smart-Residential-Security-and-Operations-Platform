<template>
  <div class="page-card">
    <el-result icon="info" title="功能建设中" sub-title="该模块接口已就绪，前端页面持续完善中。">
      <template #extra>
        <el-button type="primary" @click="goDashboard">返回工作台</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
function goDashboard() {
  router.push('/dashboard')
}
</script>
