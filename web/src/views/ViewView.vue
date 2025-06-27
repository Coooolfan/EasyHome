<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

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

// 表单数据
const form = ref({
    houseId: '',
    date: '',
    time: '',
    name: '',
    phone: '',
    remarks: ''
})

// 加载状态
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const selectedHouse = ref<House | null>(null)
const houses = ref<House[]>([])

// 获取路由对象
const route = useRoute()

// 页面加载时获取房源列表和预选房源
onMounted(async () => {
    // 获取路由中的房源ID
    const houseId = route.query.houseId as string
    
    if (houseId) {
        form.value.houseId = houseId
        await fetchHouseDetails(houseId)
    }
    
    // 获取所有可选房源列表
    await fetchHouses()
})

// 获取指定房源详情
const fetchHouseDetails = async (id: string) => {
    try {
        loading.value = true
        errorMsg.value = ''
        
        // 修改为后端实际提供的API端点
        const response = await axios.get(`/api/houses/getById/${id}`)
        
        if (response.data && response.data.code === 'SUCCESS') {
            selectedHouse.value = response.data.data
        } else {
            throw new Error(response.data?.message || '获取房源详情失败')
        }
    } catch (err: any) {
        console.error('获取房源详情失败:', err)
        errorMsg.value = err.message || '获取房源详情失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

// 获取可选房源列表
const fetchHouses = async () => {
    try {
        // 修改为正确的API端点
        const response = await axios.get('/api/houses/published')
        
        if (response.data && response.data.code === 'SUCCESS') {
            houses.value = response.data.data
        } else {
            throw new Error(response.data?.message || '获取房源列表失败')
        }
    } catch (err: any) {
        console.error('获取房源列表失败:', err)
        errorMsg.value = err.message || '获取房源列表失败，请稍后重试'
    }
}

// 房源选择改变
const handleHouseChange = async (event: Event) => {
    const select = event.target as HTMLSelectElement
    const id = select.value
    if (id) {
        form.value.houseId = id
        await fetchHouseDetails(id)
    } else {
        selectedHouse.value = null
    }
}

// 提交看房预约
const submitAppointment = async () => {
    // 表单验证
    if (!form.value.houseId) {
        errorMsg.value = '请选择房源'
        return
    }
    if (!form.value.date) {
        errorMsg.value = '请选择看房日期'
        return
    }
    if (!form.value.time) {
        errorMsg.value = '请选择看房时间'
        return
    }
    if (!form.value.name) {
        errorMsg.value = '请输入您的姓名'
        return
    }
    if (!form.value.phone) {
        errorMsg.value = '请输入联系电话'
        return
    }
    
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(form.value.phone)) {
        errorMsg.value = '请输入有效的手机号码'
        return
    }
    
    try {
        loading.value = true
        errorMsg.value = ''
        successMsg.value = ''
        
        const token = localStorage.getItem('token')
        if (!token) {
            errorMsg.value = '请先登录'
            return
        }
        
        // 创建一个包含用户ID的请求数据
        const requestData = {
            ...form.value
        }
        
        // 发送请求，使用包含用户ID的数据
        const response = await axios.post('/api/appointment/submit', requestData, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        
        if (response.data && response.data.code === 'SUCCESS') {
            successMsg.value = '看房预约已提交，我们将尽快与您联系'
            // 重置表单
            form.value = {
                houseId: form.value.houseId, // 保留当前房源
                date: '',
                time: '',
                name: '',
                phone: '',
                remarks: ''
            }
        } else {
            throw new Error(response.data?.message || '提交预约失败')
        }
    } catch (err: any) {
        console.error('提交预约失败:', err)
        errorMsg.value = err.message || '提交预约失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

// 格式化房源选项显示
const formatHouseOption = (house: House) => {
    return `${house.title} - ${house.area}㎡ - ${(house.price / 10000).toFixed(0)}万`
}

// 今天的日期（用于date输入的min属性）
const today = new Date().toISOString().split('T')[0]

// 时间段选项
const timeOptions = [
    { value: '9:00-10:00', label: '上午 9:00-10:00' },
    { value: '10:00-11:00', label: '上午 10:00-11:00' },
    { value: '11:00-12:00', label: '上午 11:00-12:00' },
    { value: '14:00-15:00', label: '下午 14:00-15:00' },
    { value: '15:00-16:00', label: '下午 15:00-16:00' },
    { value: '16:00-17:00', label: '下午 16:00-17:00' },
    { value: '17:00-18:00', label: '下午 17:00-18:00' }
]
</script>

<template>
    <div class="min-h-screen bg-gradient-to-b from-blue-50 to-white py-12">
        <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
            <!-- 页面标题 -->
            <div class="mb-10 text-center">
                <h1 class="text-3xl font-bold text-gray-900">预约看房</h1>
                <div class="mt-2 h-1 w-20 bg-blue-500 mx-auto rounded-full"></div>
                <p class="mt-4 text-gray-600">选择心仪房源，安排专业顾问陪您实地看房</p>
            </div>

            <!-- 提示消息 -->
            <transition name="fade" mode="out-in">
                <div v-if="successMsg" class="mb-6 bg-green-50 border-l-4 border-green-500 p-4 rounded-md">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-green-500" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <p class="text-sm text-green-700">{{ successMsg }}</p>
                        </div>
                    </div>
                </div>

                <div v-else-if="errorMsg" class="mb-6 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <p class="text-sm text-red-700">{{ errorMsg }}</p>
                        </div>
                    </div>
                </div>
            </transition>

            <div class="bg-white rounded-xl shadow-lg overflow-hidden">
                <div class="p-6">
                    <div class="grid grid-cols-1 lg:grid-cols-5 gap-6">
                        <!-- 左侧表单 -->
                        <div class="lg:col-span-3 space-y-6">
                            <h2 class="text-xl font-semibold text-gray-800 border-b pb-2">预约信息</h2>
                            
                            <!-- 房源选择 -->
                            <div>
                                <label for="property" class="block text-sm font-medium text-gray-700">选择房源</label>
                                <div class="mt-1 relative">
                                    <select id="property" name="property" v-model="form.houseId" @change="handleHouseChange"
                                        class="block w-full pl-3 pr-10 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm">
                                        <option value="">请选择一套房源</option>
                                        <option v-for="house in houses" :key="house.id" :value="house.id">
                                            {{ formatHouseOption(house) }}
                                        </option>
                                    </select>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <!-- 看房日期 -->
                                <div>
                                    <label for="date" class="block text-sm font-medium text-gray-700">看房日期</label>
                                    <div class="mt-1">
                                        <input type="date" name="date" id="date" v-model="form.date" :min="today"
                                            class="block w-full pl-3 pr-3 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm">
                                    </div>
                                </div>

                                <!-- 看房时间 -->
                                <div>
                                    <label for="time" class="block text-sm font-medium text-gray-700">看房时间</label>
                                    <div class="mt-1 relative">
                                        <select id="time" name="time" v-model="form.time"
                                            class="block w-full pl-3 pr-10 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm">
                                            <option value="">请选择时间段</option>
                                            <option v-for="option in timeOptions" :key="option.value" :value="option.value">
                                                {{ option.label }}
                                            </option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <!-- 联系人姓名 -->
                                <div>
                                    <label for="name" class="block text-sm font-medium text-gray-700">您的姓名</label>
                                    <div class="mt-1">
                                        <input type="text" name="name" id="name" v-model="form.name"
                                            class="block w-full pl-3 pr-3 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm"
                                            placeholder="请输入您的姓名">
                                    </div>
                                </div>

                                <!-- 联系电话 -->
                                <div>
                                    <label for="phone" class="block text-sm font-medium text-gray-700">联系电话</label>
                                    <div class="mt-1">
                                        <input type="tel" name="phone" id="phone" v-model="form.phone"
                                            class="block w-full pl-3 pr-3 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm"
                                            placeholder="请输入您的手机号码">
                                    </div>
                                </div>
                            </div>

                            <!-- 备注信息 -->
                            <div>
                                <label for="remarks" class="block text-sm font-medium text-gray-700">备注信息</label>
                                <div class="mt-1">
                                    <textarea id="remarks" name="remarks" rows="3" v-model="form.remarks"
                                        class="block w-full pl-3 pr-3 py-3 text-base text-gray-700 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 rounded-lg shadow-sm"
                                        placeholder="如有特殊要求，请在此说明"></textarea>
                                </div>
                            </div>

                            <!-- 提交按钮 -->
                            <div class="mt-6">
                                <button type="button" @click="submitAppointment"
                                    class="w-full inline-flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-lg shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                                    :disabled="loading">
                                    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    {{ loading ? '提交中...' : '提交看房预约' }}
                                </button>
                            </div>
                        </div>

                        <!-- 右侧房源预览和信息 -->
                        <div class="lg:col-span-2">
                            <h2 class="text-xl font-semibold text-gray-800 border-b pb-2 mb-4">房源信息</h2>

                            <!-- 已选房源预览 -->
                            <div v-if="selectedHouse" class="space-y-4">
                                <div class="aspect-w-16 aspect-h-9 rounded-lg overflow-hidden">
                                    <img :src="selectedHouse.image" :alt="selectedHouse.title" class="w-full h-full object-cover">
                                </div>

                                <div class="space-y-2">
                                    <h3 class="text-lg font-semibold text-gray-800">{{ selectedHouse.title }}</h3>
                                    <p class="text-gray-600 flex items-center text-sm">
                                        <svg class="h-4 w-4 text-gray-400 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                        </svg>
                                        {{ selectedHouse.address }}
                                    </p>

                                    <div class="grid grid-cols-2 gap-2 mt-3">
                                        <div class="bg-blue-50 p-2 rounded text-center">
                                            <p class="text-xs text-gray-500">面积</p>
                                            <p class="text-sm font-medium text-gray-800">{{ selectedHouse.area }}㎡</p>
                                        </div>
                                        <div class="bg-blue-50 p-2 rounded text-center">
                                            <p class="text-xs text-gray-500">户型</p>
                                            <p class="text-sm font-medium text-gray-800">{{ selectedHouse.rooms }}</p>
                                        </div>
                                        <div class="bg-blue-50 p-2 rounded text-center">
                                            <p class="text-xs text-gray-500">楼层</p>
                                            <p class="text-sm font-medium text-gray-800">{{ selectedHouse.floor }}</p>
                                        </div>
                                        <div class="bg-blue-50 p-2 rounded text-center">
                                            <p class="text-xs text-gray-500">朝向</p>
                                            <p class="text-sm font-medium text-gray-800">{{ selectedHouse.orientation || '暂无' }}</p>
                                        </div>
                                    </div>

                                    <div class="mt-3">
                                        <p class="text-xs text-gray-500">总价</p>
                                        <p class="text-xl font-semibold text-red-600">{{ (selectedHouse.price / 10000).toFixed(0) }}万</p>
                                        <p class="text-xs text-gray-500">单价: {{ selectedHouse.unitPrice }}元/平</p>
                                    </div>
                                </div>
                            </div>

                            <!-- 未选择房源提示 -->
                            <div v-else class="bg-gray-50 p-6 rounded-lg text-center h-full flex flex-col items-center justify-center">
                                <svg class="h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                                </svg>
                                <p class="mt-4 text-gray-600">请从左侧选择您想看的房源</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 看房须知 -->
            <div class="mt-8 bg-white rounded-xl shadow-md p-6">
                <div class="flex items-center mb-4">
                    <svg class="h-5 w-5 text-blue-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <h2 class="text-lg font-semibold text-gray-800">看房须知</h2>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <div class="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100 text-blue-600">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                        </div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-900">提前预约</p>
                            <p class="mt-1 text-sm text-gray-500">请提前一天预约看房，我们会安排专业顾问陪同</p>
                        </div>
                    </div>

                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <div class="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100 text-blue-600">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2" />
                                </svg>
                            </div>
                        </div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-900">携带证件</p>
                            <p class="mt-1 text-sm text-gray-500">看房时请携带有效身份证件</p>
                        </div>
                    </div>

                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <div class="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100 text-blue-600">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                            </div>
                        </div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-900">取消变更</p>
                            <p class="mt-1 text-sm text-gray-500">如需取消或更改预约，请提前2小时联系我们</p>
                        </div>
                    </div>

                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <div class="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100 text-blue-600">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                            </div>
                        </div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-900">看房时间</p>
                            <p class="mt-1 text-sm text-gray-500">看房时间为每日9:00-18:00</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* 强制设置aspect-ratio以保持图片比例 */
.aspect-w-16 {
    position: relative;
    padding-bottom: 56.25%; /* 16:9 比例 */
}

.aspect-w-16 > img {
    position: absolute;
    height: 100%;
    width: 100%;
    top: 0;
    left: 0;
    object-fit: cover;
}

select {
    appearance: none;
    background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3E%3C/svg%3E");
    background-position: right 0.5rem center;
    background-repeat: no-repeat;
    background-size: 1.5em 1.5em;
    padding-right: 2.5rem;
}

/* 修复输入框和选择器在不同浏览器中的一致性问题 */
input, select, textarea {
    appearance: none;
}
</style>