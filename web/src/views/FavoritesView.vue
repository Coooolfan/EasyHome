<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import HouseDetailModal from '@/components/HouseDetail.vue';

// 定义房源类型（与SearchView相同）
interface House {
  id: number;
  title: string;
  address: string;
  price: number;
  unitPrice: number;
  area: number;
  rooms: string;
  floor: string;
  buildYear: number;
  orientation: string;
  decoration: string;
  image: string;
  createdAt?: string;
  updatedAt?: string;
  tag: string[];
}

const router = useRouter();
const houses = ref<House[]>([]);
const favoriteIds = ref<number[]>([]);
const loading = ref(false);
const error = ref('');

// 模态框状态
const showDetailModal = ref(false);
const selectedHouse = ref<House | null>(null);

// 加载收藏的房源
const loadFavoriteHouses = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      error.value = '请先登录';
      houses.value = [];
      loading.value = false;
      return;
    }

    // 使用后端API获取收藏列表
    const response = await axios.get('/api/favorites/list', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (response.data && response.data.code === 'SUCCESS') {
      houses.value = response.data.data || [];
      // 更新收藏ID列表，用于界面展示
      favoriteIds.value = houses.value.map(house => house.id);
    } else {
      throw new Error(response.data?.message || '获取收藏房源失败');
    }
  } catch (err: any) {
    console.error('获取收藏房源失败:', err);
    error.value = err.response?.data?.message || err.message || '获取收藏房源失败，请稍后重试';
    houses.value = [];
  } finally {
    loading.value = false;
  }
};

// 取消收藏
const removeFromFavorites = async (houseId: number) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      error.value = '请先登录';
      return;
    }
    
    // 调用后端API取消收藏
    const response = await axios.delete(`/api/favorites/remove?houseId=${houseId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (response.data && response.data.code === 'SUCCESS') {
      // 从列表中移除
      const index = favoriteIds.value.indexOf(houseId);
      if (index > -1) {
        favoriteIds.value.splice(index, 1);
        houses.value = houses.value.filter(h => h.id !== houseId);
      }
    } else {
      throw new Error(response.data?.message || '取消收藏失败');
    }
  } catch (err: any) {
    console.error('取消收藏失败:', err);
    error.value = err.response?.data?.message || err.message || '取消收藏失败，请稍后重试';
  }
};

// 查看房源详情
const viewHouseDetail = (house: House) => {
  selectedHouse.value = house;
  showDetailModal.value = true;
};

// 关闭详情模态框
const closeDetailModal = () => {
  showDetailModal.value = false;
};

// 格式化价格
const formatPrice = (price: number) => {
  if (price >= 10000) {
    return (price / 10000).toFixed(1) + '万';
  }
  return price.toString();
};

onMounted(() => {
  loadFavoriteHouses();
});
</script>


<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">我的收藏</h1>
        <p class="text-gray-600">查看您收藏的所有房源</p>
      </div>
      
      <!-- 加载中 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-flex items-center px-4 py-2 bg-white shadow-sm rounded-md text-blue-600">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          正在加载收藏房源...
        </div>
      </div>
      
      <!-- 错误提示 -->
      <div v-else-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>
          <div class="ml-auto pl-3">
            <button @click="loadFavoriteHouses" class="text-sm font-medium text-blue-600 hover:text-blue-500">
              重试
            </button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="houses.length === 0" class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="text-gray-400 mb-4">
          <svg class="mx-auto h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
        </div>
        <h3 class="text-xl font-medium text-gray-900 mb-2">您还没有收藏任何房源</h3>
        <p class="text-gray-500 max-w-md mx-auto mb-6">
          浏览房源列表，点击心形图标即可将心仪的房源加入收藏夹
        </p>
        <router-link 
          to="/search" 
          class="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
        >
          查看房源列表
        </router-link>
      </div>
      
      <!-- 收藏列表 -->
      <div v-else class="space-y-6">
        <div 
          v-for="house in houses" 
          :key="house.id"
          class="bg-white rounded-xl shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden border border-gray-100 group"
        >
          <div class="md:flex">
            <!-- 房源图片 -->
            <div class="md:w-1/3 relative overflow-hidden" @click="viewHouseDetail(house)">
              <img 
                :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop'" 
                :alt="house.title"
                class="w-full h-60 md:h-full object-cover transform group-hover:scale-105 transition-transform duration-500 cursor-pointer"
              >
              <!-- 取消收藏按钮 -->
              <button 
                @click.stop="removeFromFavorites(house.id)" 
                class="absolute top-3 right-3 bg-white bg-opacity-80 p-2 rounded-full hover:bg-opacity-100 transition-all duration-200 focus:outline-none"
              >
                <svg 
                  class="h-5 w-5 text-red-500 fill-current"
                  xmlns="http://www.w3.org/2000/svg" 
                  viewBox="0 0 20 20" 
                  fill="currentColor"
                >
                  <path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd" />
                </svg>
              </button>
            </div>
            
            <!-- 房源信息 -->
            <div class="md:w-2/3 p-6">
              <div class="flex justify-between items-start mb-2">
                <h3 
                  @click="viewHouseDetail(house)"
                  class="text-xl font-semibold text-gray-900 hover:text-blue-600 cursor-pointer group-hover:text-blue-600 transition-colors duration-200"
                >
                  {{ house.title }}
                </h3>
                <div class="text-right">
                  <div class="text-2xl font-bold text-red-600">
                    {{ formatPrice(house.price) }}
                  </div>
                  <div class="text-sm text-gray-500">
                    {{ house.unitPrice }} 元/平米
                  </div>
                </div>
              </div>
              
              <p class="text-gray-600 mb-4">{{ house.address }}</p>
              
              <!-- 房屋属性 -->
              <div class="flex flex-wrap gap-2 mb-4">
                <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                  <span class="text-gray-500 text-xs">面积</span>
                  <span class="font-medium text-gray-900">{{ house.area }}㎡</span>
                </div>
                <div class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                  <span class="text-gray-500 text-xs">户型</span>
                  <span class="font-medium text-gray-900">{{ house.rooms }}</span>
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
                <span 
                  v-for="tag in house.tag" 
                  :key="tag"
                  class="px-2 py-0.5 bg-blue-50 text-blue-700 text-xs rounded-md border border-blue-100"
                >
                  {{ tag }}
                </span>
              </div>
              
              <!-- 操作按钮 -->
              <div class="flex justify-end space-x-4 mt-4">
                <button 
                  @click="viewHouseDetail(house)"
                  class="flex items-center text-blue-600 hover:text-blue-800 transition-colors"
                >
                  <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  查看详情
                </button>
                <router-link :to="`/view?houseId=${house.id}`"
                  class="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center justify-center"
                >
                  <svg class="h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  预约看房
                </router-link>
              </div>
            </div>
          </div>
        </div>
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