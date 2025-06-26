<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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

// 收藏房源列表
const favoriteHouses = ref<number[]>([])

// 查看房源详情
const viewHouseDetail = (house: House) => {
    selectedHouse.value = house
    showDetailModal.value = true
}

// 关闭详情模态框
const closeDetailModal = () => {
    showDetailModal.value = false
}

// 初始化收藏列表
const initFavorites = async () => {
    try {
        const token = localStorage.getItem('token')
        if (!token) {
            // 未登录时从本地存储加载收藏
            const savedFavorites = localStorage.getItem('favoriteHouses')
            if (savedFavorites) {
                favoriteHouses.value = JSON.parse(savedFavorites)
            }
            return
        }

        // 已登录，从后端获取收藏列表
        const response = await axios.get('/api/favorites/list', {
            headers: { 'Authorization': `Bearer ${token}` }
        })

        if (response.data && response.data.code === 'SUCCESS') {
            // 只保存ID列表用于界面显示
            favoriteHouses.value = response.data.data.map((house: House) => house.id)
        }
    } catch (err) {
        console.error('获取收藏列表失败:', err)
        // 出错时尝试从本地读取
        const savedFavorites = localStorage.getItem('favoriteHouses')
        if (savedFavorites) {
            favoriteHouses.value = JSON.parse(savedFavorites)
        }
    }
}

// 处理详情模态框中的收藏状态变化
const handleFavoriteChange = (houseId: number, isFav: boolean) => {
    // 如果已收藏但不在收藏列表中，添加它
    if (isFav && !favoriteHouses.value.includes(houseId)) {
        favoriteHouses.value.push(houseId);
    }
    // 如果已取消收藏但仍在收藏列表中，移除它
    else if (!isFav && favoriteHouses.value.includes(houseId)) {
        const index = favoriteHouses.value.indexOf(houseId);
        if (index > -1) {
            favoriteHouses.value.splice(index, 1);
        }
    }
    
    // 如果是未登录状态，还需要更新localStorage
    const token = localStorage.getItem('token');
    if (!token) {
        localStorage.setItem('favoriteHouses', JSON.stringify(favoriteHouses.value));
    }
}

// 切换收藏状态
const toggleFavorite = async (houseId: number) => {
    try {
        const token = localStorage.getItem('token')
        const isFav = isFavorite(houseId)

        if (!token) {
            // 未登录时处理本地收藏
            if (isFav) {
                // 从收藏列表移除
                const index = favoriteHouses.value.indexOf(houseId)
                if (index > -1) {
                    favoriteHouses.value.splice(index, 1)
                }
            } else {
                // 添加到收藏列表
                favoriteHouses.value.push(houseId)
            }
            // 保存到本地存储
            localStorage.setItem('favoriteHouses', JSON.stringify(favoriteHouses.value))
            return
        }

        // 已登录，调用后端API
        if (isFav) {
            // 取消收藏
            const response = await axios.delete(`/api/favorites/remove?houseId=${houseId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            })

            if (response.data && response.data.code === 'SUCCESS') {
                const index = favoriteHouses.value.indexOf(houseId)
                if (index > -1) {
                    favoriteHouses.value.splice(index, 1)
                }
            }
        } else {
            // 添加收藏
            const response = await axios.post(`/api/favorites/add?houseId=${houseId}`, null, {
                headers: { 'Authorization': `Bearer ${token}` }
            })

            if (response.data && response.data.code === 'SUCCESS') {
                favoriteHouses.value.push(houseId)
            }
        }
    } catch (err: any) {
        console.error('操作收藏失败:', err)
        // 可以在这里添加提示或其他错误处理
    }
}

// 检查房源是否已收藏
const isFavorite = (houseId: number): boolean => {
    return favoriteHouses.value.includes(houseId)
}

// 获取房源数据
const fetchHouses = async () => {
    try {
        loading.value = true
        error.value = ''

        // 构建查询参数
        const queryParams: Record<string, string> = {
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
            queryParams.minPrice = min.toString()
            queryParams.maxPrice = max.toString()
        }

        // 构建请求参数
        const params = new URLSearchParams()

        // 添加分页参数
        params.append('current', pagination.current.toString())
        params.append('size', pagination.size.toString())

        // 添加查询参数
        Object.entries(queryParams).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                params.append(key, value)
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
    scrollToTop()
}

// 格式化价格
const formatPrice = (price: number) => {
    if (price >= 10000) {
        return (price / 10000).toFixed(0) + '万'
    }
    return price.toString()
}

// 滚动到顶部
const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 创建一个可观察分页范围的计算属性
const pageRange = computed(() => {
    const totalPages = Math.ceil(pagination.total / pagination.size)
    const currentPage = pagination.current

    // 最多显示5个页码按钮
    let startPage = Math.max(1, currentPage - 2)
    let endPage = Math.min(totalPages, startPage + 4)

    // 调整起始页
    if (endPage - startPage < 4) {
        startPage = Math.max(1, endPage - 4)
    }

    const range: number[] = []
    for (let i = startPage; i <= endPage; i++) {
        range.push(i)
    }

    return range
})

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

// 页面加载时初始化
onMounted(() => {
    initFavorites()
    fetchHouses()
})
</script>

<template>
    <main class="bg-gray-50 min-h-screen">
        <!-- 搜索头部 -->
        <div class="bg-white shadow-sm border-b sticky top-0 z-40">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                <div class="text-center mb-6">
                    <h1 class="text-3xl font-bold text-gray-900 mb-2">寻找理想新家</h1>
                    <p class="text-gray-600">精选优质二手房源，一键开启安心置业之旅</p>
                </div>

                <!-- 搜索栏 -->
                <div class="max-w-4xl mx-auto">
                    <div class="flex gap-2 sm:gap-4">
                        <div class="flex-1 relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                            </div>
                            <input v-model="searchQuery" type="text" placeholder="请输入小区名、地址或关键词"
                                class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-shadow duration-200 shadow-sm hover:shadow"
                                @keyup.enter="handleSearch" :disabled="loading">
                        </div>
                        <button @click="handleSearch" :disabled="loading"
                            class="px-6 sm:px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium disabled:opacity-50 shadow-sm transition-all duration-200 hover:shadow flex items-center justify-center">
                            <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                    stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor"
                                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                </path>
                            </svg>
                            {{ loading ? '搜索中' : '搜索' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div class="lg:grid lg:grid-cols-4 lg:gap-8">
                <!-- 筛选侧边栏 -->
                <div class="lg:col-span-1">
                    <div class="bg-white rounded-lg shadow-sm p-6 lg:sticky lg:top-24">
                        <div class="flex items-center justify-between mb-6">
                            <h3 class="text-lg font-semibold text-gray-900">筛选条件</h3>
                            <button @click="resetFilters"
                                class="text-sm text-blue-600 hover:text-blue-800 transition-colors" :disabled="loading">
                                <span class="flex items-center">
                                    <svg class="h-3.5 w-3.5 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none"
                                        viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                    </svg>
                                    重置
                                </span>
                            </button>
                        </div>

                        <!-- 价格筛选 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">价格范围</h4>
                            <select v-model="filters.priceRange" @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading">
                                <option value="">不限价格</option>
                                <option value="0-1000000">100万以下</option>
                                <option value="1000000-1500000">100-150万</option>
                                <option value="1500000-2000000">150-200万</option>
                                <option value="2000000-3000000">200-300万</option>
                                <option value="3000000-5000000">300-500万</option>
                                <option value="5000000-9999999">500万以上</option>
                            </select>
                        </div>

                        <!-- 户型筛选 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">户型</h4>
                            <select v-model="filters.houseType" @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading">
                                <option value="">不限户型</option>
                                <option value="1室">1室</option>
                                <option value="2室">2室</option>
                                <option value="3室">3室</option>
                                <option value="4室">4室及以上</option>
                            </select>
                        </div>

                        <!-- 排序 -->
                        <div class="mb-6">
                            <h4 class="font-medium text-gray-900 mb-3">排序方式</h4>
                            <select v-model="filters.sortBy" @change="handleSearch"
                                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500"
                                :disabled="loading">
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
                    <div class="mb-6 flex justify-between items-center" v-if="!loading && !error">
                        <p class="text-gray-600">
                            共找到 <span class="font-semibold text-blue-600">{{ pagination.total || filteredHouses.length
                            }}</span> 套房源
                        </p>
                    </div>

                    <!-- 错误提示 -->
                    <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <div class="flex">
                            <div class="flex-shrink-0">
                                <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd"
                                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                                        clip-rule="evenodd" />
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
                                    <button @click="fetchHouses"
                                        class="text-sm bg-red-100 text-red-800 hover:bg-red-200 px-3 py-1 rounded-md transition-colors">
                                        重试
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 加载状态 -->
                    <div v-if="loading" class="text-center py-12">
                        <div
                            class="inline-flex items-center px-4 py-2 border border-transparent text-base leading-6 font-medium rounded-md text-white bg-blue-600 shadow-sm">
                            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg"
                                fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                    stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor"
                                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                </path>
                            </svg>
                            正在加载房源数据...
                        </div>
                    </div>

                    <!-- 房源卡片列表 -->
                    <div v-else class="space-y-6">
                        <div v-for="house in filteredHouses" :key="house.id"
                            class="bg-white rounded-xl shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden border border-gray-100 group">
                            <div class="md:flex">
                                <!-- 房源图片 -->
                                <div class="md:w-1/3 relative overflow-hidden" @click="viewHouseDetail(house)">
                                    <img :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop'"
                                        :alt="house.title"
                                        class="w-full h-60 md:h-full object-cover transform group-hover:scale-105 transition-transform duration-500">
                                    <!-- 收藏按钮 -->
                                    <button @click.stop="toggleFavorite(house.id)"
                                        class="absolute top-3 right-3 bg-white bg-opacity-80 p-2 rounded-full hover:bg-opacity-100 transition-all duration-200 focus:outline-none">
                                        <svg :class="[
                                            'h-5 w-5 transition-colors duration-200',
                                            isFavorite(house.id) ? 'text-red-500 fill-current' : 'text-gray-400 hover:text-red-500'
                                        ]" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                                            :fill="isFavorite(house.id) ? 'currentColor' : 'none'"
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
                                        <h3 @click="viewHouseDetail(house)"
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
                                        <div
                                            class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                                            <span class="text-gray-500 text-xs">户型</span>
                                            <span class="font-medium text-gray-900">{{ house.rooms }}</span>
                                        </div>
                                        <div
                                            class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                                            <span class="text-gray-500 text-xs">面积</span>
                                            <span class="font-medium text-gray-900">{{ house.area }}㎡</span>
                                        </div>
                                        <div
                                            class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                                            <span class="text-gray-500 text-xs">楼层</span>
                                            <span class="font-medium text-gray-900">{{ house.floor }}</span>
                                        </div>
                                        <div
                                            class="px-3 py-2 bg-gray-50 rounded-lg flex flex-col items-center justify-center">
                                            <span class="text-gray-500 text-xs">装修</span>
                                            <span class="font-medium text-gray-900">{{ house.decoration || '未知'
                                            }}</span>
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
                                        <button @click="viewHouseDetail(house)"
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
                    </div>

                    <!-- 空状态 -->
                    <div v-if="!loading && !error && filteredHouses.length === 0"
                        class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-100">
                        <div class="text-gray-400 mb-4">
                            <svg class="mx-auto h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9.5 7h.01M12.5 7h.01M15.5 7h.01M12 16.5V10" />
                            </svg>
                        </div>
                        <h3 class="text-xl font-medium text-gray-900 mb-2">暂无符合条件的房源</h3>
                        <p class="text-gray-500 max-w-md mx-auto mb-6">
                            暂时没有找到符合您要求的房源，您可以尝试调整搜索条件或者联系我们的置业顾问为您定制找房方案。
                        </p>
                        <button @click="resetFilters"
                            class="inline-flex items-center px-4 py-2.5 border border-transparent text-sm font-medium rounded-lg shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                            <svg class="mr-1.5 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none"
                                viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                            </svg>
                            重置筛选条件
                        </button>
                    </div>

                    <!-- 分页 -->
                    <div class="mt-8 flex justify-center"
                        v-if="!loading && !error && filteredHouses.length > 0 && pagination.total > pagination.size">
                        <nav class="relative z-0 inline-flex shadow-sm -space-x-px rounded-md overflow-hidden"
                            aria-label="分页">
                            <button @click="handlePageChange(pagination.current - 1)"
                                :disabled="pagination.current <= 1"
                                class="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed">
                                <span class="sr-only">上一页</span>
                                <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                                    fill="currentColor" aria-hidden="true">
                                    <path fill-rule="evenodd"
                                        d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                                        clip-rule="evenodd" />
                                </svg>
                            </button>

                            <button v-for="page in pageRange" :key="page" @click="handlePageChange(page)" :class="[
                                'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                                page === pagination.current
                                    ? 'bg-blue-50 border-blue-500 text-blue-600 z-10'
                                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                            ]">
                                {{ page }}
                            </button>

                            <button @click="handlePageChange(pagination.current + 1)"
                                :disabled="pagination.current >= Math.ceil(pagination.total / pagination.size)"
                                class="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed">
                                <span class="sr-only">下一页</span>
                                <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                                    fill="currentColor" aria-hidden="true">
                                    <path fill-rule="evenodd"
                                        d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                                        clip-rule="evenodd" />
                                </svg>
                            </button>
                        </nav>
                    </div>
                </div>
            </div>
        </div>

        <!-- 房源详情模态框 -->
        <HouseDetailModal :show="showDetailModal" :house="selectedHouse" @close="closeDetailModal"
            @favoriteChange="handleFavoriteChange" />
        <!-- 回到顶部按钮 -->
        <button v-show="!loading && filteredHouses.length > 0" @click="scrollToTop"
            class="fixed bottom-6 right-6 bg-blue-600 text-white p-3 rounded-full shadow-lg hover:bg-blue-700 transition-colors z-50"
            aria-label="回到顶部">
            <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
            </svg>
        </button>
    </main>
</template>

<style scoped>
/* 图片悬停效果 */
.group:hover .group-hover\:scale-105 {
    transform: scale(1.05);
}

/* 固定顶部搜索栏 */
.sticky {
    position: sticky;
    z-index: 40;
}

/* 淡入淡出动画 */
@keyframes fadeIn {
    0% {
        opacity: 0;
        transform: translateY(10px);
    }

    100% {
        opacity: 1;
        transform: translateY(0);
    }
}

.fade-in {
    animation: fadeIn 0.3s ease-out forwards;
}

/* 优化移动端体验 */
@media (max-width: 768px) {
    .md\:flex {
        flex-direction: column;
    }

    .md\:w-1\/3,
    .md\:w-2\/3 {
        width: 100%;
    }
}
</style>