<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

// 定义房源类型
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
}

// 定义组件属性
const props = defineProps<{
  house: House
  isFavorite: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'viewDetail', house: House): void
  (e: 'toggleFavorite', houseId: number): void
}>()

// 格式化价格
const formatPrice = (price: number) => {
    if (price >= 10000) {
        return (price / 10000).toFixed(0) + '万'
    }
    return price.toString()
}

// 查看详情
const viewDetail = () => {
  emit('viewDetail', props.house)
}

// 切换收藏状态
const toggleFavorite = (event: Event) => {
  event.stopPropagation()
  emit('toggleFavorite', props.house.id)
}
</script>

<template>
  <div class="bg-white rounded-xl shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden border border-gray-100 group">
    <div class="md:flex">
      <!-- 房源图片 -->
      <div class="md:w-1/3 relative overflow-hidden" @click="viewDetail">
        <img :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop'"
          :alt="house.title"
          class="w-full h-60 md:h-full object-cover transform group-hover:scale-105 transition-transform duration-500">
        <!-- 收藏按钮 -->
        <button @click="toggleFavorite"
          class="absolute top-3 right-3 bg-white bg-opacity-80 p-2 rounded-full hover:bg-opacity-100 transition-all duration-200 focus:outline-none">
          <svg :class="[
            'h-5 w-5 transition-colors duration-200',
            isFavorite ? 'text-red-500 fill-current' : 'text-gray-400 hover:text-red-500'
          ]" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
            :fill="isFavorite ? 'currentColor' : 'none'"
            stroke="currentColor">
            <path fill-rule="evenodd"
              d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z"
              clip-rule="evenodd" />
          </svg>
        </button>
      </div>

      <!-- 房源信息 -->
      <div class="md:w-2/3 p-6">
        <div class="flex justify-between items-start mb-2">
          <h3 @click="viewDetail"
            class="text-xl font-semibold text-gray-900 hover:text-blue-600 cursor-pointer group-hover:text-blue-600 transition-colors duration-200">
            {{ house.title }}
          </h3>
          <div class="text-right">
            <div class="text-2xl font-bold text-red-600">
              {{ formatPrice(house.price) }}
            </div>
            <div class="text-sm text-gray-500">
              {{ house.unitPrice }}元/㎡
            </div>
          </div>
        </div>

        <p class="text-gray-600 mb-4 flex items-center">
          <svg class="h-4 w-4 text-gray-400 mr-1" xmlns="http://www.w3.org/2000/svg"
            fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          {{ house.address }}
        </p>

        <!-- 房源详情列表 -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
            <span class="text-gray-500 text-xs">户型</span>
            <span class="font-medium text-gray-900">{{ house.rooms }}</span>
          </div>
          <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
            <span class="text-gray-500 text-xs">面积</span>
            <span class="font-medium text-gray-900">{{ house.area }}㎡</span>
          </div>
          <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
            <span class="text-gray-500 text-xs">楼层</span>
            <span class="font-medium text-gray-900">{{ house.floor }}</span>
          </div>
          <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
            <span class="text-gray-500 text-xs">装修</span>
            <span class="font-medium text-gray-900">{{ house.decoration || '未知' }}</span>
          </div>
        </div>

        <!-- 标签 -->
        <div class="flex flex-wrap gap-2 mb-4" v-if="house.tag && house.tag.length > 0">
          <span v-for="tag in house.tag" :key="tag"
            class="px-2 py-0.5 bg-blue-50 text-blue-700 text-xs rounded-md border border-blue-100">
            {{ tag }}
          </span>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-3">
          <button @click="viewDetail"
            class="flex-1 px-4 py-2.5 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors flex items-center justify-center">
            <svg class="h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none"
              viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            查看详情
          </button>
          <router-link :to="`/view?houseId=${house.id}`"
            class="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center justify-center">
            <svg class="h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none"
              viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            预约看房
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 图片悬停效果 */
.group:hover .group-hover\:scale-105 {
  transform: scale(1.05);
}
</style> 