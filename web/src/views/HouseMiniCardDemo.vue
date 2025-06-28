<script setup lang="ts">
import { ref } from 'vue'
import HouseMiniCard from '@/components/HouseMiniCard.vue'
import HouseDetailModal from '@/components/HouseDetail.vue'

// 模拟房源ID列表
const houseIds = ref([1, 2, 3, 4, 5, 6])

// 模态框状态
const showDetailModal = ref(false)
const selectedHouse = ref<any>(null)

// 查看房源详情
const viewHouseDetail = (house: any) => {
  selectedHouse.value = house
  showDetailModal.value = true
}

// 关闭详情模态框
const closeDetailModal = () => {
  showDetailModal.value = false
}

// 切换收藏状态
const toggleFavorite = async (houseId: number) => {
  console.log('切换收藏状态:', houseId)
  // 这里可以添加收藏/取消收藏的逻辑
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold mb-6">HouseMiniCard 演示</h1>
    
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="houseId in houseIds" :key="houseId">
        <HouseMiniCard 
          :houseId="houseId" 
          @viewDetail="viewHouseDetail" 
          @toggleFavorite="toggleFavorite"
        />
      </div>
    </div>
    
    <!-- 房源详情模态框 -->
    <HouseDetailModal 
      :show="showDetailModal" 
      :house="selectedHouse" 
      @close="closeDetailModal"
    />
  </div>
</template> 