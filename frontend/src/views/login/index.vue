<template>
  <div class="login-bg">
    <el-card class="login-card">
      <div class="title">
        <el-icon color="#36d1ff" :size="26"><Lock /></el-icon>
        <span>智慧安防小区运营平台</span>
      </div>
      <div class="subtitle">统一安防运营管理 · 一站式指挥调度</div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="doLogin" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码"
            :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="submit" @click="doLogin">登 录</el-button>
      </el-form>
      <div class="tips">演示账号：admin / admin123（超管）　wuye / admin123（物业）　anbao / admin123（安保）</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function doLogin() {
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      await user.login(form.username, form.password)
      await user.fetchInfo()
      ElMessage.success('登录成功')
      router.replace('/dashboard')
    } catch (e: any) {
      /* message handled by interceptor */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-bg { height: 100vh; display: flex; align-items: center; justify-content: center;
  background: radial-gradient(circle at 30% 20%, #0b3a6b, #04162e 70%); }
.login-card { width: 380px; border-radius: 10px; padding: 12px 8px; }
.title { display: flex; align-items: center; gap: 10px; font-size: 20px; font-weight: 700; color: #1f2d3d; }
.subtitle { color: #909399; font-size: 13px; margin: 6px 0 18px; }
.submit { width: 100%; margin-top: 6px; }
.tips { margin-top: 14px; font-size: 12px; color: #b0b3b8; line-height: 1.6; }
</style>
