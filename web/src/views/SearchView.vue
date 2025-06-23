<script setup lang="ts">
import { ref, reactive } from 'vue'

// 搜索和筛选状态
const searchQuery = ref('')
const filters = reactive({
  priceRange: '',
  houseType: '',
  area: '',
  district: '',
  sortBy: 'price-asc'
})

// 模拟房源数据
const houses = ref([
  {
    id: 1,
    title: '高新区幸福小区 精装三居室',
    address: '高新区幸福小区3号楼2单元501',
    price: 1200000,
    unitPrice: 15000,
    area: 80,
    rooms: '3室2厅1卫',
    floor: '5/6层',
    year: 2018,
    orientation: '南北',
    decoration: '精装',
    image: 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop',
    tags: ['地铁房', '学区房', '精装修']
  },
  {
    id: 2,
    title: '市中心豪华公寓 拎包入住',
    address: '市中心CBD核心区A座2008',
    price: 2500000,
    unitPrice: 25000,
    area: 100,
    rooms: '2室2厅2卫',
    floor: '20/25层',
    year: 2020,
    orientation: '南向',
    decoration: '豪装',
    image: 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400&h=300&fit=crop',
    tags: ['豪华装修', '高层景观', '商务区']
  },
  {
    id: 3,
    title: '学区房 临近名校 交通便利',
    address: '教育路学府花园6号楼',
    price: 950000,
    unitPrice: 12000,
    area: 79,
    rooms: '2室1厅1卫',
    floor: '3/6层',
    year: 2015,
    orientation: '南向',
    decoration: '简装',
    image: 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=400&h=300&fit=crop',
    tags: ['学区房', '地铁房', '投资首选']
  }
])

const filteredHouses = ref(houses.value)

// 搜索功能
const handleSearch = () => {
  let result = houses.value
  
  if (searchQuery.value) {
    result = result.filter(house => 
      house.title.includes(searchQuery.value) || 
      house.address.includes(searchQuery.value)
    )
  }
  
  // 应用筛选器
  if (filters.priceRange) {
    const [min, max] = filters.priceRange.split('-').map(Number)
    result = result.filter(house => house.price >= min && house.price <= max)
  }
  
  if (filters.houseType) {
    result = result.filter(house => house.rooms.includes(filters.houseType))
  }
  
  // 排序
  if (filters.sortBy === 'price-asc') {
    result.sort((a, b) => a.price - b.price)
  } else if (filters.sortBy === 'price-desc') {
    result.sort((a, b) => b.price - a.price)
  } else if (filters.sortBy === 'area-desc') {
    result.sort((a, b) => b.area - a.area)
  }
  
  filteredHouses.value = result
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
  filters.sortBy = 'price-asc'
  filteredHouses.value = houses.value
}
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
                            >
                        </div>
                        <button 
                            @click="handleSearch"
                            class="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                        >
                            搜索
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
                            >
                                <option value="price-asc">价格从低到高</option>
                                <option value="price-desc">价格从高到低</option>
                                <option value="area-desc">面积从大到小</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- 房源列表 -->
                <div class="lg:col-span-3 mt-8 lg:mt-0">
                    <!-- 结果统计 -->
                    <div class="mb-6">
                        <p class="text-gray-600">
                            共找到 <span class="font-semibold text-blue-600">{{ filteredHouses.length }}</span> 套房源
                        </p>
                    </div>

                    <!-- 房源卡片列表 -->
                    <div class="space-y-6">
                        <div 
                            v-for="house in filteredHouses" 
                            :key="house.id"
                            class="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden cursor-pointer"
                        >
                            <div class="md:flex">
                                <!-- 房源图片 -->
                                <div class="md:w-1/3">
                                    <img 
                                        :src="house.image" 
                                        :alt="house.title"
                                        class="w-full h-48 md:h-full object-cover"
                                    >
                                </div>
                                
                                <!-- 房源信息 -->
                                <div class="md:w-2/3 p-6">
                                    <div class="flex justify-between items-start mb-2">
                                        <h3 class="text-xl font-semibold text-gray-900 hover:text-blue-600">
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
                                            <span class="font-medium">{{ house.year }}年</span>
                                        </div>
                                    </div>
                                    
                                    <!-- 标签 -->
                                    <div class="flex flex-wrap gap-2 mb-4">
                                        <span 
                                            v-for="tag in house.tags" 
                                            :key="tag"
                                            class="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded-full"
                                        >
                                            {{ tag }}
                                        </span>
                                    </div>
                                    
                                    <!-- 操作按钮 -->
                                    <div class="flex gap-3">
                                        <button class="flex-1 px-4 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition-colors">
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
                    <div v-if="filteredHouses.length === 0" class="text-center py-12">
                        <div class="text-gray-400 mb-4">
                            <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-4m-2 0h-4m-2 0H3m2-4h2m2-4h2m2-4h2" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无匹配房源</h3>
                        <p class="text-gray-500">请尝试调整搜索条件或筛选器</p>
                    </div>

                    <!-- 分页（可选） -->
                    <div class="mt-8 flex justify-center" v-if="filteredHouses.length > 0">
                        <nav class="flex items-center space-x-2">
                            <button class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md">
                                上一页
                            </button>
                            <button class="px-3 py-2 text-sm bg-blue-600 text-white rounded-md">1</button>
                            <button class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md">2</button>
                            <button class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md">3</button>
                            <button class="px-3 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-300 rounded-md">
                                下一页
                            </button>
                        </nav>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
/* 如果需要额外的样式可以在这里添加 */
</style>