<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 定义房源记录接口
interface HousePublishRecord {
    id: number;
    userId: number;
    title: string;
    address: string;
    price: number;
    area: number;
    unitPrice: number;
    rooms: string;
    floor?: string;
    buildYear?: number;
    orientation?: string;
    decoration?: string;
    tag?: string[];
    image?: string;
    status: string;
    reason?: string;
    createTime?: string;
    createdAt?: string;
    updatedAt?: string;
}

interface UserInfo {
    username: string;
    avatar: string;
    joinDate: string;
}

const router = useRouter()

// 用户信息
const userInfo = ref<UserInfo>({
    username: '',
    avatar: '',
    joinDate: ''
})

// 房源记录列表及加载状态
const publishedHouses = ref<HousePublishRecord[]>([]);
const loading = ref<boolean>(true);
const error = ref<string>('');

// 状态映射为中文
const statusMap: Record<string, string> = {
    'PENDING': '待审核',
    'RECEIVED': '已接收',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
}

// 状态对应的颜色类
const statusColorClass = (status: string): string => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
        case 'RECEIVED': return 'bg-blue-100 text-blue-800 border-blue-200'
        case 'APPROVED': return 'bg-green-100 text-green-800 border-green-200'
        case 'REJECTED': return 'bg-red-100 text-red-800 border-red-200'
        case 'PUBLISHED': return 'bg-blue-600 text-white border-blue-700'
        default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
}

// 加载用户信息
const loadUserInfo = (): void => {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
        try {
            const parsedInfo = JSON.parse(storedUserInfo)
            userInfo.value = {
                username: parsedInfo.username || '用户',
                avatar: parsedInfo.avatar || '',
                joinDate: parsedInfo.joinDate || new Date().toISOString().split('T')[0]
            }
        } catch (e) {
            console.error('Failed to parse user info:', e)
        }
    } else {
        // 如果未登录，重定向到登录页
        router.push('/login?redirect=/profile')
    }
}

// 加载用户发布的房源记录
const loadPublishedHouses = async (): Promise<void> => {
    loading.value = true
    error.value = ''

    try {
        const token = localStorage.getItem('token')
        if (!token) {
            throw new Error('未登录状态')
        }

        // 使用您提供的实际API路径
        const response = await axios.get('/api/houses/my-publish', {
            headers: { 'Authorization': `Bearer ${token}` }
        })

        if (response.data && response.data.code === 'SUCCESS') {
            publishedHouses.value = response.data.data || []
        } else {
            throw new Error(response.data?.message || '加载发布记录失败')
        }
    } catch (err: any) {
        console.error('加载发布记录失败:', err)
        error.value = err.message || '加载发布记录失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

// 获取房源状态的展示形式
const getStatusDisplay = (status: string): string => {
    return statusMap[status] || status
}

// 格式化日期
const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return '未知时间'

    const date = new Date(dateString)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查看详情
const viewHouseDetail = (id: number): void => {
    router.push(`/house/${id}`)
}

// 编辑房源
const editHouse = (id: number): void => {
    router.push(`/sell/edit/${id}`);
}

// 下架房源
const unpublishHouse = async (id: number): Promise<void> => {
    if (!confirm('确定要下架此房源吗？')) {
        return;
    }

    try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await axios.post(`/api/houses/${id}/unpublish`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data && response.data.code === 'SUCCESS') {
            // 更新房源状态
            const index = publishedHouses.value.findIndex(h => h.id === id);
            if (index !== -1) {
                publishedHouses.value[index].status = 'UNPUBLISHED';
                publishedHouses.value = [...publishedHouses.value]; // 触发视图更新
            }
            alert('房源已成功下架');
        } else {
            throw new Error(response.data?.message || '操作失败');
        }
    } catch (err: any) {
        console.error('下架房源失败:', err);
        alert(err.message || '操作失败，请稍后重试');
    }
}

// 格式化价格为万元
const formatPrice = (price: number): string => {
    return (price / 10000).toFixed(2) + '万';
}

onMounted(() => {
    loadUserInfo()
    loadPublishedHouses()
})
</script>

<template>
    <div class="bg-gray-50 min-h-screen pb-10">
        <!-- 顶部个人信息区 -->
        <div class="bg-blue-700 text-white pt-8 pb-20 px-4">
            <div class="max-w-5xl mx-auto">
                <div class="flex items-center">
                    <!-- 用户头像 -->
                    <div class="w-20 h-20 rounded-full bg-blue-500 text-white flex items-center justify-center text-2xl font-bold mr-5"
                        v-if="!userInfo.avatar">
                        {{ userInfo.username.charAt(0).toUpperCase() }}
                    </div>
                    <img v-else :src="userInfo.avatar" alt="用户头像" class="w-20 h-20 rounded-full object-cover mr-5">

                    <!-- 用户信息 -->
                    <div>
                        <h1 class="text-2xl font-bold">{{ userInfo.username }}</h1>
                        <p class="text-blue-100">加入时间：{{ userInfo.joinDate }}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 内容区块 -->
        <div class="max-w-5xl mx-auto -mt-12 px-4">
            <!-- 功能区块 -->
            <div class="bg-white rounded-xl shadow-md mb-6">
                <div class="p-6 border-b border-gray-100">
                    <h2 class="text-xl font-bold text-gray-800">我的功能</h2>
                    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
                        <router-link to="/my-listings"
                            class="flex flex-col items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                            <svg class="w-7 h-7 text-blue-600 mb-2" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                            </svg>
                            <span class="text-sm font-medium text-gray-800">我的房源</span>
                        </router-link>

                        <router-link to="/favorites"
                            class="flex flex-col items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                            <svg class="w-7 h-7 text-blue-600 mb-2" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                            </svg>
                            <span class="text-sm font-medium text-gray-800">我的收藏</span>
                        </router-link>

                        <router-link to="/settings"
                            class="flex flex-col items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                            <svg class="w-7 h-7 text-blue-600 mb-2" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            </svg>
                            <span class="text-sm font-medium text-gray-800">账号设置</span>
                        </router-link>

                        <router-link to="/sell"
                            class="flex flex-col items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                            <svg class="w-7 h-7 text-blue-600 mb-2" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <span class="text-sm font-medium text-gray-800">发布房源</span>
                        </router-link>
                    </div>
                </div>
            </div>

            <!-- 房源发布记录 -->
            <div class="bg-white rounded-xl shadow-md">
                <div class="p-6 border-b border-gray-100">
                    <div class="flex justify-between items-center">
                        <h2 class="text-xl font-bold text-gray-800">房源发布记录</h2>
                        <router-link to="/sell" class="text-sm text-blue-600 hover:text-blue-800 font-medium">
                            发布新房源
                        </router-link>
                    </div>

                    <!-- 加载中 -->
                    <div v-if="loading" class="py-10 flex justify-center">
                        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
                    </div>

                    <!-- 错误信息 -->
                    <div v-else-if="error" class="p-4 mt-4 bg-red-50 text-red-700 rounded-lg border border-red-100">
                        {{ error }}
                        <button @click="loadPublishedHouses"
                            class="ml-2 text-blue-600 hover:text-blue-800 underline">重试</button>
                    </div>

                    <!-- 空状态 -->
                    <div v-else-if="publishedHouses.length === 0" class="py-10 text-center">
                        <div class="text-gray-400 mb-3">
                            <svg class="w-16 h-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                            </svg>
                        </div>
                        <p class="text-gray-500 text-lg">您还没有发布过房源</p>
                        <router-link to="/sell"
                            class="inline-block mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                            立即发布
                        </router-link>
                    </div>

                    <!-- 房源列表部分的修改 -->
                    <div v-else class="mt-4 space-y-4">
                        <div v-for="house in publishedHouses.slice(0, 2)" :key="house.id"
                            class="border border-gray-200 rounded-lg hover:shadow-md transition-shadow overflow-hidden">
                            <div class="flex flex-col sm:flex-row">
                                <!-- 房源图片 -->
                                <div class="sm:w-36 h-24 sm:h-auto bg-gray-100 overflow-hidden">
                                    <img v-if="house.image" :src="house.image" alt="房源图片"
                                        class="w-full h-full object-cover">
                                    <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
                                        <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                        </svg>
                                    </div>
                                </div>

                                <!-- 房源信息（简化版本） -->
                                <div class="p-3 flex-1 flex flex-col">
                                    <div class="flex justify-between items-start">
                                        <h3 class="font-medium text-gray-800 truncate">{{ house.title }}</h3>
                                        <div
                                            :class="`px-2 py-0.5 text-xs font-medium rounded-md border ${statusColorClass(house.status)}`">
                                            {{ getStatusDisplay(house.status) }}
                                        </div>
                                    </div>

                                    <p class="text-gray-600 text-xs mt-1 truncate">{{ house.address }}</p>

                                    <div class="mt-auto flex items-center justify-between text-sm">
                                        <span class="font-medium text-blue-600">{{ (house.price / 10000).toFixed(1)
                                            }}万</span>
                                        <span class="text-xs text-gray-500">{{ formatDate(house.createdAt) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 查看更多按钮 -->
                        <router-link to="/my-listings"
                            class="block w-full py-2 mt-2 text-center text-sm text-blue-600 hover:text-blue-800 bg-blue-50 hover:bg-blue-100 rounded-md transition-colors">
                            查看全部房源
                            <span v-if="publishedHouses.length > 3">(共 {{ publishedHouses.length }} 条)</span>
                        </router-link>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 渐变背景效果 */
.bg-blue-700 {
    background-image: linear-gradient(45deg, #1d4ed8, #2563eb);
}

/* 悬停效果 */
.transition-shadow {
    transition: box-shadow 0.3s ease;
}

.transition-colors {
    transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

.hover\:shadow-md:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
</style>