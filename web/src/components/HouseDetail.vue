<script setup lang="ts">
import { computed, watch, onUnmounted, ref } from 'vue';

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
    show: boolean
    house: House | null
}>()

// 定义事件
const emit = defineEmits<{
    (e: 'close'): void
}>()

// 标签页状态管理
const activeTab = ref('details') // 'details', 'description', 'contact'

// 关闭模态框
const closeModal = () => {
    emit('close')
}

// 切换标签
const switchTab = (tab: string) => {
    activeTab.value = tab
}

// 格式化价格
const formatPrice = (price: number) => {
    if (price >= 10000) {
        return (price / 10000).toFixed(0) + '万'
    }
    return price.toString()
}

// 格式化日期
const formatDate = (dateStr?: string) => {
    if (!dateStr) return '未知'
    const date = new Date(dateStr)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 计算是否有标签
const hasTags = computed(() => props.house?.tag && props.house.tag.length > 0)

// 提取房源标签的主要特点
const mainFeatures = computed(() => {
    if (!props.house?.tag || props.house.tag.length === 0) return []
    return props.house.tag.slice(0, 3)
})

// 根据房源面积计算合适户型
const suggestedHouseholdSize = computed(() => {
    if (!props.house) return ''
    const area = props.house.area
    if (area < 60) return '适合单身人士或小两口'
    if (area < 90) return '适合3口之家'
    if (area < 120) return '适合3-4口之家'
    return '适合大家庭'
})

// 计算房屋单价水平
const priceLevel = computed(() => {
    if (!props.house) return ''
    const unitPrice = props.house.unitPrice
    if (unitPrice < 10000) return '价格经济实惠'
    if (unitPrice < 15000) return '价格适中'
    if (unitPrice < 20000) return '价格较高'
    return '高端豪宅'
})

// 当模态框打开时阻止背景滚动
const stopBodyScroll = (isOpen: boolean) => {
    if (isOpen) {
        document.body.style.overflow = 'hidden'
    } else {
        document.body.style.overflow = ''
    }
}

// 监听 show 属性变化
watch(() => props.show, (newVal) => {
    stopBodyScroll(newVal)
    if (newVal) {
        // 重置到默认标签
        activeTab.value = 'details'
    }
}, { immediate: true })

// 组件卸载时恢复滚动
onUnmounted(() => {
    stopBodyScroll(false)
})
</script>

<template>
    <Teleport to="body">
        <!-- 模态框背景 - 添加了动画 -->
        <div v-if="show"
            class="fixed inset-0 bg-black bg-opacity-60 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-all duration-300"
            @click="closeModal">
            <!-- 模态框内容 - 优化了动画和阴影 -->
            <div class="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-hidden shadow-2xl transition-all duration-300 transform"
                :class="show ? 'scale-100 opacity-100' : 'scale-95 opacity-0'"
                @click.stop>
                <div v-if="house">
                    <!-- 模态框头部 - 使用更大的图片和渐变叠加 -->
                    <div class="relative">
                        <div class="relative h-80 overflow-hidden">
                            <img :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=800&h=500&fit=crop'"
                                :alt="house.title" class="w-full h-full object-cover transition-transform duration-700 hover:scale-110">
                            <!-- 图片上的渐变叠加 -->
                            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
                            
                            <!-- 信息叠加层 -->
                            <div class="absolute bottom-0 left-0 right-0 p-6 text-white">
                                <h2 class="text-3xl font-bold drop-shadow-md mb-2">{{ house.title }}</h2>
                                <div class="flex items-center space-x-2 text-sm opacity-90">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z">
                                        </path>
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                                    </svg>
                                    <span>{{ house.address }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- 价格标签 - 更醒目的价格展示 -->
                        <div class="absolute top-6 right-6 bg-red-600 text-white px-4 py-2 rounded-lg shadow-lg">
                            <div class="text-2xl font-bold">{{ formatPrice(house.price) }}</div>
                            <div class="text-xs opacity-90">{{ house.unitPrice }}元/㎡</div>
                        </div>
                        
                        <!-- 关闭按钮 - 改为左上角 -->
                        <button @click="closeModal"
                            class="absolute top-6 left-6 bg-white/90 backdrop-blur-sm rounded-full p-2 shadow-lg hover:bg-white transition-colors duration-200">
                            <svg class="h-6 w-6 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    <!-- 主要特点预览 - 新增 -->
                    <div class="bg-gray-50 px-6 py-3 grid grid-cols-3 gap-2 border-b border-gray-100">
                        <div class="flex items-center space-x-2">
                            <span class="w-8 h-8 flex items-center justify-center rounded-full bg-blue-100 text-blue-600">
                                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                                </svg>
                            </span>
                            <span>{{ house.rooms }}</span>
                        </div>
                        <div class="flex items-center space-x-2">
                            <span class="w-8 h-8 flex items-center justify-center rounded-full bg-green-100 text-green-600">
                                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                                </svg>
                            </span>
                            <span>{{ house.area }}㎡</span>
                        </div>
                        <div class="flex items-center space-x-2">
                            <span class="w-8 h-8 flex items-center justify-center rounded-full bg-amber-100 text-amber-600">
                                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
                                </svg>
                            </span>
                            <span>{{ house.decoration }}</span>
                        </div>
                    </div>
                    
                    <!-- 标签导航 - 新增 -->
                    <div class="border-b border-gray-200">
                        <nav class="flex px-6 -mb-px" aria-label="Tabs">
                            <button @click="switchTab('details')"
                                :class="[
                                    'py-4 px-1 border-b-2 text-sm font-medium mr-8',
                                    activeTab === 'details'
                                        ? 'border-blue-600 text-blue-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                ]">
                                房源详情
                            </button>
                            <button @click="switchTab('description')"
                                :class="[
                                    'py-4 px-1 border-b-2 text-sm font-medium mr-8',
                                    activeTab === 'description'
                                        ? 'border-blue-600 text-blue-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                ]">
                                房源描述
                            </button>
                            <button @click="switchTab('contact')"
                                :class="[
                                    'py-4 px-1 border-b-2 text-sm font-medium',
                                    activeTab === 'contact'
                                        ? 'border-blue-600 text-blue-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                ]">
                                联系咨询
                            </button>
                        </nav>
                    </div>

                    <!-- 模态框内容区域 -->
                    <div class="p-6 overflow-y-auto" style="max-height: 50vh;">
                        <!-- 详情标签页 -->
                        <div v-if="activeTab === 'details'">
                            <!-- 房屋详情 - 更有组织的信息展示 -->
                            <div class="bg-gray-50 rounded-xl p-6 shadow-inner">
                                <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">户型</p>
                                        <p class="font-medium text-lg mt-1">{{ house.rooms }}</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">建筑面积</p>
                                        <p class="font-medium text-lg mt-1">{{ house.area }}㎡</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">楼层</p>
                                        <p class="font-medium text-lg mt-1">{{ house.floor }}</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">朝向</p>
                                        <p class="font-medium text-lg mt-1">{{ house.orientation }}</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">装修</p>
                                        <p class="font-medium text-lg mt-1">{{ house.decoration }}</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">建筑年代</p>
                                        <p class="font-medium text-lg mt-1">{{ house.buildYear }}年</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">挂牌时间</p>
                                        <p class="font-medium text-lg mt-1">{{ formatDate(house.createdAt) }}</p>
                                    </div>
                                    <div class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">更新时间</p>
                                        <p class="font-medium text-lg mt-1">{{ formatDate(house.updatedAt) }}</p>
                                    </div>
                                </div>
                            </div>

                            <!-- 标签 - 更好的视觉展示 -->
                            <div v-if="hasTags" class="mt-8">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z">
                                        </path>
                                    </svg>
                                    房源特色
                                </h3>
                                <div class="flex flex-wrap gap-3">
                                    <span v-for="tag in house.tag" :key="tag"
                                        class="px-4 py-2 bg-gradient-to-r from-blue-50 to-blue-100 text-blue-700 text-sm rounded-full border border-blue-200 shadow-sm">
                                        {{ tag }}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- 描述标签页 -->
                        <div v-else-if="activeTab === 'description'" class="space-y-6">
                            <!-- 房源描述 - 更丰富的内容 -->
                            <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z">
                                        </path>
                                    </svg>
                                    房源概述
                                </h3>
                                <div class="text-gray-600 space-y-4">
                                    <p>这套位于<span class="text-blue-600 font-medium">{{ house.address }}</span>的精选房源，建筑面积<span 
                                        class="text-blue-600 font-medium">{{ house.area }}</span>平米，<span class="text-blue-600 font-medium">{{ house.rooms }}</span>，
                                        <span class="text-blue-600 font-medium">{{ house.decoration }}</span>，楼层<span class="text-blue-600 font-medium">{{ house.floor }}</span>，
                                        朝向<span class="text-blue-600 font-medium">{{ house.orientation }}</span>，
                                        建筑年代<span class="text-blue-600 font-medium">{{ house.buildYear }}</span>年。</p>
                                    
                                    <p>{{ suggestedHouseholdSize }}，{{ priceLevel }}。该房源特点：{{ house.tag ? house.tag.join('，') : '暂无特点' }}。</p>
                                    
                                    <p>总价<span class="text-red-600 font-medium">{{ formatPrice(house.price) }}</span>，
                                    单价<span class="text-red-600 font-medium">{{ house.unitPrice }}</span>元/平米。</p>
                                </div>
                            </div>

                            <!-- 区域介绍 - 新增 -->
                            <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z">
                                        </path>
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                                    </svg>
                                    区域介绍
                                </h3>
                                <div class="text-gray-600">
                                    <p>该房源位于城市核心区域，周边配套设施完善，交通便利。附近有多个公交站点，出行方便。区域内有多家商超、医院、学校等生活配套设施，居住舒适度高。</p>
                                </div>
                            </div>

                            <!-- 推荐理由 - 新增 -->
                            <div class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-6 shadow-sm border border-blue-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5">
                                        </path>
                                    </svg>
                                    推荐理由
                                </h3>
                                <div class="space-y-2">
                                    <div v-for="(feature, index) in mainFeatures" :key="index" 
                                        class="flex items-start">
                                        <span class="inline-flex items-center justify-center flex-shrink-0 w-5 h-5 mr-3 bg-blue-600 rounded-full text-white text-xs">
                                            {{ index + 1 }}
                                        </span>
                                        <p class="text-gray-800">{{ feature }}</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 联系标签页 -->
                        <div v-else-if="activeTab === 'contact'">
                            <!-- 联系信息 - 更专业的展示 -->
                            <div class="bg-gradient-to-b from-white to-gray-50 rounded-xl p-6 shadow-sm border border-gray-100">
                                <div class="flex flex-col items-center mb-6">
                                    <div class="h-20 w-20 rounded-full bg-blue-600 flex items-center justify-center mb-4">
                                        <svg class="h-10 w-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                        </svg>
                                    </div>
                                    <div class="text-center">
                                        <p class="font-medium text-xl mb-1">李经理</p>
                                        <p class="text-gray-600">资深置业顾问 · 5年经验</p>
                                    </div>
                                </div>
                                
                                <!-- 联系方式 -->
                                <div class="space-y-4">
                                    <div class="flex items-center p-3 bg-white rounded-lg border border-gray-100 shadow-sm">
                                        <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center mr-4">
                                            <svg class="w-5 h-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                    d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p class="text-sm text-gray-500">电话咨询</p>
                                            <p class="font-medium text-lg">13800138000</p>
                                        </div>
                                    </div>
                                    <div class="flex items-center p-3 bg-white rounded-lg border border-gray-100 shadow-sm">
                                        <div class="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center mr-4">
                                            <svg class="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                    d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p class="text-sm text-gray-500">微信咨询</p>
                                            <p class="font-medium text-lg">LiManager</p>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 预约看房表单 -->
                                <div class="mt-8 bg-white p-5 rounded-lg border border-gray-100 shadow-sm">
                                    <h4 class="text-lg font-medium text-gray-900 mb-4">预约看房</h4>
                                    <div class="space-y-4">
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">您的姓名</label>
                                            <input type="text" 
                                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
                                                placeholder="请输入您的姓名">
                                        </div>
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                                            <input type="tel" 
                                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
                                                placeholder="请输入您的联系电话">
                                        </div>
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">预约时间</label>
                                            <input type="date" 
                                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 底部操作条 - 固定在底部 -->
                    <div class="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 flex items-center justify-between">
                        <!-- 分享和收藏按钮 -->
                        <div class="flex space-x-3">
                            <button class="flex items-center space-x-1 px-3 py-2 bg-gray-100 rounded-md text-gray-700 hover:bg-gray-200 transition-colors">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                                </svg>
                                <span>分享</span>
                            </button>
                            <button class="flex items-center space-x-1 px-3 py-2 bg-gray-100 rounded-md text-gray-700 hover:bg-gray-200 transition-colors">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                </svg>
                                <span>收藏</span>
                            </button>
                        </div>
                        
                        <!-- 预约看房按钮 -->
                        <button @click="switchTab('contact')"
                            class="flex-1 max-w-[200px] ml-4 px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg hover:from-blue-700 hover:to-blue-800 transition-all shadow-md hover:shadow-lg font-medium">
                            <span class="flex items-center justify-center">
                                <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                                预约看房
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: all 0.3s ease-out;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: scale(0.95);
}
</style>