<template>
  <CrudPage v-if="cfg" :key="route.path" :title="cfg.title" :columns="cfg.columns" :fetch="cfg.fetch"
    :form-fields="cfg.formFields" :search-fields="cfg.searchFields" :create-api="cfg.createApi"
    :update-api="cfg.updateApi" :remove-api="cfg.removeApi" :add-text="cfg.addText" />
  <el-empty v-else description="未找到该模块配置" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import CrudPage from './CrudPage.vue'
import { moduleConfig } from '@/config/modules'

const route = useRoute()
const cfg = computed(() => moduleConfig[route.path])
</script>
