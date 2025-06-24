<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import HouseDetailModal from '@/components/HouseDetail.vue'

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

// 定义分页响应类型
interface PageResponse {
  records: House[]
  current: number
  total: number
  size: number
}

// 定义新的API响应类型
interface ApiResponse {
  code: string
  message: string
  data: PageResponse
  timestamp: number
}

// 搜索和筛选状态
const searchQuery = ref('')
const filters = reactive({
  priceRange: '',
  houseType: '',
  area: '',
  district: '',
  sortBy: 'time-desc'
})

// 分页状态
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 房源数据和状态
const houses = ref<House[]>([])
const filteredHouses = ref<House[]>([])
const loading = ref(false)
const error = ref('')

// 模态框状态
const showDetailModal = ref(false)
const selectedHouse = ref<House | null>(null)

// 查看房源详情
const viewHouseDetail = (house: House) => {
  selectedHouse.value = house
  showDetailModal.value = true
}

// 关闭详情模态框
const closeDetailModal = () => {
  showDetailModal.value = false
}

// 获取房源数据
const fetchHouses = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 构建查询参数
    const queryParams: any = {
      sortBy: filters.sortBy
    }
    
    // 只在有值时添加参数
    if (searchQuery.value?.trim()) {
      queryParams.title = searchQuery.value.trim()
    }
    
    if (filters.houseType) {
      queryParams.rooms = filters.houseType
    }
    
    // 处理价格范围
    if (filters.priceRange) {
      const [min, max] = filters.priceRange.split('-').map(Number)
      queryParams.minPrice = min
      queryParams.maxPrice = max
    }
    
    // 构建请求参数
    const params = new URLSearchParams()
    
    // 添加分页参数
    params.append('current', pagination.current.toString())
    params.append('size', pagination.size.toString())
    
    // 添加查询参数
    Object.entries(queryParams).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value))
      }
    })
    
    console.log('请求参数:', params.toString())
    
    // 发送请求
    const response = await axios.get<ApiResponse>(`/api/houses/page?${params.toString()}`)
    console.log('API响应:', response.data)
    
    // 处理新的响应结构
    if (response.data.code === 'SUCCESS' && response.data.data) {
      const pageData = response.data.data
      houses.value = pageData.records || []
      filteredHouses.value = houses.value
      
      // 更新分页信息
      pagination.total = pageData.total
      pagination.current = pageData.current
    } else {
      throw new Error(response.data.message || '请求失败，未返回有效数据')
    }
    
  } catch (err: any) {
    console.error('获取房源数据失败:', err)
    
    // 更详细的错误信息处理
    if (err.response) {
      console.error('错误状态码:', err.response.status)
      console.error('错误响应数据:', err.response.data)
      // 检查是否有嵌套错误信息
      const errorMessage = err.response.data?.message || err.response.data?.data?.message || err.response.statusText
      error.value = `服务器错误 (${err.response.status}): ${errorMessage}`
    } else if (err.request) {
      console.error('请求超时或网络错误:', err.request)
      error.value = '网络连接失败，请检查网络或后端服务是否正常运行'
    } else {
      console.error('请求配置错误:', err.message)
      error.value = `请求失败: ${err.message}`
    }
    
    houses.value = []
    filteredHouses.value = []
  } finally {
    loading.value = false
  }
}

// 搜索功能
const handleSearch = () => {
  pagination.current = 1
  fetchHouses()
}

// 分页处理
const handlePageChange = (page: number) => {
  pagination.current = page
  fetchHouses()
}

// 格式化价格
const formatPrice = (price: number) => {
  if (price >= 10000) {
    return (price / 10000).toFixed(0) + '万'
  }
  return price.toString()
}

// 重置筛选
const resetFilters = () => {
  searchQuery.value = ''
  filters.priceRange = ''
  filters.houseType = ''
  filters.area = ''
  filters.district = ''
  filters.sortBy = 'time-desc'
  pagination.current = 1
  fetchHouses()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchHouses()
})
</script>

<template>
    <main class="bg-gray-50 min-h-screen">
        <!-- 搜索头部 -->
        <div class="bg-white shadow-sm border-b">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                <div class="text-center mb-6">
                    <h1 class="text-3xl font-bold text-gray-900 mb-2">二手房查询</h1>
                    <p class="text-gray-600">找到心仪的家，从这里开始</p>
                </div>
                
                <!-- 搜索栏 -->
                <div class="max-w-4xl mx-auto">
                    <div class="flex gap-4">
                        <div class="flex-1">
                            <input 
                                v-model="searchQuery"
                                type="text" 
                                placeholder="请输入小区名、地址或关键词" 
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                @keyup.enter="handleSearch"
                                :disabled="loading"
                            >
                        </div>
                        <button 
                            @click="handleSearch"
                            :disabled="loading"
                            class="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium disabled:opacity-50"
                        >
                            {{ loading ? '搜索中...' : '搜索' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div class="lg:grid lg:grid-cols-4 lg:gap-8">
                <!-- 筛选侧边栏 -->
                <div class="lg:col-span-1">
                    <div class="bg-white rounded-lg shadow-sm p-6 sticky top-8">
                        <div class="flex items-center justify-between mb-6">
                            <h3 class="text-lg font-semibold text-gray-900">筛选条件</h3>
                            <button 
                                @click="resetFilters"
                                class="text-sm text-blue-600 hover:text-blue-800"
                                :disabled="loading"
                            >
                                重置
                            </button>
                        </div>

                        <!-- 价格筛选 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">价格范围</h4>
                            <select 
                                v-model="filters.priceRange" 
                                @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading"
                            >
                                <option value="">不限</option>
                                <option value="0-1000000">100万以下</option>
                                <option value="1000000-1500000">100-150万</option>
                                <option value="1500000-2000000">150-200万</option>
                                <option value="2000000-9999999">200万以上</option>
                            </select>
                        </div>

                        <!-- 户型筛选 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">户型</h4>
                            <select 
                                v-model="filters.houseType" 
                                @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading"
                            >
                                <option value="">不限</option>
                                <option value="1室">1室</option>
                                <option value="2室">2室</option>
                                <option value="3室">3室</option>
                                <option value="4室">4室及以上</option>
                            </select>
                        </div>

                        <!-- 排序 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">排序方式</h4>
                            <select 
                                v-model="filters.sortBy" 
                                @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading"
                            >
                                <option value="time-desc">最新发布</option>
                                <option value="price-asc">价格从低到高</option>
                                <option value="price-desc">价格从高到低</option>
                                <option value="area-asc">面积从小到大</option>
                                <option value="area-desc">面积从大到小</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- 房源列表 -->
                <div class="lg:col-span-3 mt-8 lg:mt-0">
                    <!-- 结果统计 -->
                    <div class="mb-6" v-if="!loading && !error">
                        <p class="text-gray-600">
                            共找到 <span class="font-semibold text-blue-600">{{ pagination.total || filteredHouses.length }}</span> 套房源
                        </p>
                    </div>

                    <!-- 错误提示 -->
                    <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <div class="flex">
                            <div class="flex-shrink-0">
                                <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                                </svg>
                            </div>
                            <div class="ml-3">
                                <h3 class="text-sm font-medium text-red-800">
                                    加载失败
                                </h3>
                                <div class="mt-2 text-sm text-red-700">
                                    <p>{{ error }}</p>
                                </div>
                                <div class="mt-4">
                                    <button 
                                        @click="fetchHouses"
                                        class="text-sm bg-red-100 text-red-800 hover:bg-red-200 px-3 py-1 rounded-md"
                                    >
                                        重试
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 加载状态 -->
                    <div v-if="loading" class="text-center py-12">
                        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                        <p class="mt-4 text-gray-600">加载中...</p>
                    </div>

                    <!-- 房源卡片列表 -->
                    <div v-else class="space-y-6">
                        <div 
                            v-for="house in filteredHouses" 
                            :key="house.id"
                            class="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden cursor-pointer"
                        >
                            <div class="md:flex">
                                <!-- 房源图片 -->
                                <div class="md:w-1/3" @click="viewHouseDetail(house)">
                                    <img 
                                        :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop'" 
                                        :alt="house.title"
                                        class="w-full h-48 md:h-full object-cover"
                                    >
                                </div>
                                
                                <!-- 房源信息 -->
                                <div class="md:w-2/3 p-6">
                                    <div class="flex justify-between items-start mb-2">
                                        <h3 
                                          @click="viewHouseDetail(house)"
                                          class="text-xl font-semibold text-gray-900 hover:text-blue-600 cursor-pointer"
                                        >
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
                                    
                                    <p class="text-gray-600 mb-3">{{ house.address }}</p>
                                    
                                    <!-- 房源详情 -->
                                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4 text-sm">
                                        <div>
                                            <span class="text-gray-500">户型：</span>
                                            <span class="font-medium">{{ house.rooms }}</span>
                                        </div>
                                        <div>
                                            <span class="text-gray-500">面积：</span>
                                            <span class="font-medium">{{ house.area }}㎡</span>
                                        </div>
                                        <div>
                                            <span class="text-gray-500">楼层：</span>
                                            <span class="font-medium">{{ house.floor }}</span>
                                        </div>
                                        <div>
                                            <span class="text-gray-500">年代：</span>
                                            <span class="font-medium">{{ house.buildYear }}年</span>
                                        </div>
                                    </div>
                                    
                                    <!-- 标签 -->
                                    <div class="flex flex-wrap gap-2 mb-4" v-if="house.tag && house.tag.length > 0">
                                        <span 
                                            v-for="tag in house.tag" 
                                            :key="tag"
                                            class="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded-full"
                                        >
                                            {{ tag }}
                                        </span>
                                    </div>
                                    
                                    <!-- 操作按钮 -->
                                    <div class="flex gap-3">
                                        <button 
                                          @click="viewHouseDetail(house)"
                                          class="flex-1 px-4 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition-colors"
                                        >
                                            查看详情
                                        </button>
                                        <button class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
                                            预约看房
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 空状态 -->
                    <div v-if="!loading && !error && filteredHouses.length === 0" class="text-center py-12">
                        <div class="text-gray-400 mb-4">
                            <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-4m-2 0h-4m-2 0H3m2-4h2m2-4h2m2-4h2" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无匹配房源</h3>
                        <p class="text-gray-500">请尝试调整搜索条件或筛选器</p>
                    </div>

                    <!-- 分页 -->
                    <div class="mt-8 flex justify-center" v-if="!loading && !error && filteredHouses.length > 0 && pagination.total > pagination.size">
                        <nav class="flex items-center space-x-2">
                            <button 
                                @click="handlePageChange(pagination.current - 1)"
                                :disabled="pagination.current <= 1"
                                class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md disabled:opacity-50"
                            >
                                上一页
                            </button>
                            
                            <button 
                                v-for="page in Math.min(5, Math.ceil(pagination.total / pagination.size))" 
                                :key="page"
                                @click="handlePageChange(page)"
                                :class="[
                                    'px-3 py-2 text-sm rounded-md',
                                    page === pagination.current 
                                        ? 'bg-blue-600 text-white' 
                                        : 'text-gray-500 hover:text-gray-700 border border-gray-300'
                                ]"
                            >
                                {{ page }}
                            </button>
                            
                            <button 
                                @click="handlePageChange(pagination.current + 1)"
                                :disabled="pagination.current >= Math.ceil(pagination.total / pagination.size)"
                                class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md disabled:opacity-50"
                            >
                                下一页
                            </button>
                        </nav>
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
    </main>
</template>

<style scoped>
/* 如果需要额外的样式可以在这里添加 */
</style>