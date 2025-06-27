<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  currentPage: number
  totalPages: number
  totalItems: number
  pageSize: number
  maxVisibleButtons?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxVisibleButtons: 5
})

const emit = defineEmits<{
  (e: 'page-change', page: number): void
}>()

// 计算开始和结束页码
const pages = computed(() => {
  const range = []
  const startPage = Math.max(1, props.currentPage - Math.floor(props.maxVisibleButtons / 2))
  const endPage = Math.min(props.totalPages, startPage + props.maxVisibleButtons - 1)
  const adjustedStart = Math.max(1, endPage - props.maxVisibleButtons + 1)

  for (let i = adjustedStart; i <= endPage; i++) {
    range.push(i)
  }

  return range
})

// 获取页码显示范围
const startItem = computed(() => {
  return (props.currentPage - 1) * props.pageSize + 1
})

const endItem = computed(() => {
  return Math.min(props.currentPage * props.pageSize, props.totalItems)
})

// 页码变更处理
const changePage = (page: number) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('page-change', page)
  }
}
</script>

<template>
  <div class="flex flex-col items-center space-y-3 py-4 sm:flex-row sm:justify-between sm:space-y-0">
    <!-- 页码信息 -->
    <div class="text-sm text-gray-600">
      显示第 <span class="font-medium">{{ startItem }}</span> 到 <span class="font-medium">{{ endItem }}</span> 条，
      共 <span class="font-medium">{{ totalItems }}</span> 条记录
    </div>
    
    <!-- 分页控件 -->
    <nav class="flex space-x-1" aria-label="分页">
      <!-- 首页按钮 -->
      <button
        @click="changePage(1)"
        :disabled="currentPage === 1"
        :class="[
          'px-2 py-1 rounded text-sm',
          currentPage === 1 
            ? 'text-gray-400 cursor-not-allowed' 
            : 'text-blue-600 hover:bg-blue-50'
        ]"
        title="首页"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
        </svg>
      </button>
      
      <!-- 上一页按钮 -->
      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 1"
        :class="[
          'px-2 py-1 rounded text-sm',
          currentPage === 1 
            ? 'text-gray-400 cursor-not-allowed' 
            : 'text-blue-600 hover:bg-blue-50'
        ]"
        title="上一页"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      
      <!-- 页码按钮 -->
      <button
        v-for="page in pages"
        :key="page"
        @click="changePage(page)"
        :class="[
          'px-3 py-1 rounded text-sm',
          currentPage === page
            ? 'bg-blue-600 text-white'
            : 'text-gray-700 hover:bg-blue-50'
        ]"
      >
        {{ page }}
      </button>
      
      <!-- 下一页按钮 -->
      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        :class="[
          'px-2 py-1 rounded text-sm',
          currentPage === totalPages
            ? 'text-gray-400 cursor-not-allowed'
            : 'text-blue-600 hover:bg-blue-50'
        ]"
        title="下一页"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
      
      <!-- 末页按钮 -->
      <button
        @click="changePage(totalPages)"
        :disabled="currentPage === totalPages"
        :class="[
          'px-2 py-1 rounded text-sm',
          currentPage === totalPages
            ? 'text-gray-400 cursor-not-allowed'
            : 'text-blue-600 hover:bg-blue-50'
        ]"
        title="末页"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
        </svg>
      </button>
    </nav>
  </div>
</template>