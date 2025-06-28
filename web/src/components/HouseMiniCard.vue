<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

// 定义房源类型（参考后端House.java）
interface House {
  id: number
  title: string
  address: string
  price: number
  unitPrice: number
  area: number
  rooms: string
  floor: string
  buildYear: number
  orientation: string
  decoration: string
  image: string
  createdAt?: string
  updatedAt?: string
  tag: string[]
  userId?: number
}

// 定义组件属性
const props = defineProps<{
  houseId: number
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'viewDetail', house: House): void
  (e: 'toggleFavorite', houseId: number): void
}>()

// 房源数据和状态
const house = ref<House | null>(null)
const loading = ref(true)
const error = ref('')

// 格式化价格
const formatPrice = (price: number) => {
  if (price >= 10000) {
    return (price / 10000).toFixed(0) + '万'
  }
  return price.toString()
}

// 查看详情
const viewDetail = () => {
  if (house.value) {
    emit('viewDetail', house.value)
  }
}

// 切换收藏状态
const toggleFavorite = (event: Event) => {
  event.stopPropagation()
  if (house.value) {
    emit('toggleFavorite', house.value.id)
  }
}

// 获取房源详情
const fetchHouseDetail = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 调用后端API获取房源详情
    const response = await axios.get(`/api/houses/getById/${props.houseId}`)
    
    if (response.data && response.data.code === 'SUCCESS') {
      house.value = response.data.data
    } else {
      throw new Error(response.data.message || '获取房源详情失败')
    }
  } catch (err: any) {
    console.error('获取房源详情失败:', err)
    error.value = err.message || '获取房源详情失败'
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取房源详情
onMounted(() => {
  fetchHouseDetail()
})
</script>

<template>
  <div class="bg-white rounded-lg shadow-sm hover:shadow transition-shadow duration-200 overflow-hidden border border-gray-100 group">
    <!-- 加载状态 -->
    <div v-if="loading" class="p-4 flex items-center justify-center h-32">
      <div class="animate-spin h-5 w-5 border-2 border-blue-500 rounded-full border-t-transparent"></div>
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="p-4 text-red-500 text-center text-sm h-32 flex items-center justify-center">
      <div>
        <svg class="h-6 w-6 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        {{ error }}
      </div>
    </div>
    
    <!-- 房源信息 -->
    <div v-else-if="house" class="flex" @click="viewDetail">
      <!-- 房源图片 -->
      <div class="w-1/3 relative">
        <img :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=200&h=150&fit=crop'"
          :alt="house.title"
          class="w-full h-24 object-cover">
        <!-- 收藏按钮 -->
        <button @click="toggleFavorite"
          class="absolute top-1 right-1 bg-white bg-opacity-80 p-1 rounded-full hover:bg-opacity-100 transition-all duration-200 focus:outline-none">
          <svg class="h-4 w-4 text-gray-400 hover:text-red-500"
            xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="currentColor">
            <path fill-rule="evenodd"
              d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z"
              clip-rule="evenodd" />
          </svg>
        </button>
      </div>

      <!-- 房源简要信息 -->
      <div class="w-2/3 p-3">
        <h3 class="text-sm font-medium text-gray-900 truncate">{{ house.title }}</h3>
        
        <div class="flex justify-between items-center mt-1">
          <span class="text-xs text-gray-500">{{ house.rooms }} | {{ house.area }}㎡</span>
          <span class="text-sm font-bold text-red-600">{{ formatPrice(house.price) }}</span>
        </div>
        
        <p class="text-xs text-gray-500 mt-1 truncate">{{ house.address }}</p>
        
        <!-- 标签（最多显示2个） -->
        <div class="flex flex-wrap gap-1 mt-1" v-if="house.tag && house.tag.length > 0">
          <span v-for="(tag, index) in house.tag.slice(0, 2)" :key="tag"
            class="px-1.5 py-0.5 bg-blue-50 text-blue-700 text-xs rounded border border-blue-100 text-[10px]">
            {{ tag }}
          </span>
          <span v-if="house.tag.length > 2" class="text-[10px] text-gray-400">+{{ house.tag.length - 2 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style> 