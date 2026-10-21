<template>
  <div ref="el" :style="{ width: '100%', height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{ option: any; height?: string }>()
const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let ro: ResizeObserver | null = null
let rafId = 0

function render() {
  if (!el.value) return
  if (!chart) chart = echarts.init(el.value)
  chart.setOption(props.option || {}, true)
}

// 容器尺寸变化(如侧边栏折叠)时同步重绘图表,用 rAF 合并连续触发的 resize
function onResize() {
  cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => chart?.resize())
}

onMounted(() => {
  nextTick(() => {
    render()
    if (el.value && typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(onResize)
      ro.observe(el.value)
    }
  })
})
watch(() => props.option, () => render(), { deep: true })
onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  ro?.disconnect()
  ro = null
  chart?.dispose()
  chart = null
})
</script>
