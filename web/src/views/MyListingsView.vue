<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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

// 页面状态
const houses = ref<HousePublishRecord[]>([]);
const loading = ref<boolean>(true);
const error = ref<string>('');
const router = useRouter();

// 模态框状态 - 只保留查看详情模态框
const showViewModal = ref<boolean>(false);
const currentHouse = ref<HousePublishRecord | null>(null);

// 过滤条件
const filterStatus = ref<string>('all');
const sortOption = ref<string>('latest');

// 状态映射为中文
const statusMap: Record<string, string> = {
    'PENDING': '待审核',
    'RECEIVED': '已接收',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'PUBLISHED': '已发布'
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

// 加载房源列表
const loadHouses = async (): Promise<void> => {
    loading.value = true;
    error.value = '';

    try {
        const token = localStorage.getItem('token');
        if (!token) {
            router.push('/login?redirect=/my-listings');
            return;
        }

        const response = await axios.get('/api/houses/my-publish', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data && response.data.code === 'SUCCESS') {
            houses.value = response.data.data || [];
        } else {
            throw new Error(response.data?.message || '加载房源记录失败');
        }
    } catch (err: any) {
        console.error('加载房源记录失败:', err);
        error.value = err.message || '加载房源记录失败，请稍后重试';
    } finally {
        loading.value = false;
    }
}

// 过滤后的房源列表
const filteredAndSortedHouses = computed(() => {
    let result = [...houses.value];

    // 根据状态筛选
    if (filterStatus.value !== 'all') {
        result = result.filter(house => house.status === filterStatus.value);
    }

    // 根据选择的排序方式排序
    switch (sortOption.value) {
        case 'latest':
            result.sort((a, b) => {
                const dateA = new Date(a.createdAt || a.createTime || '');
                const dateB = new Date(b.createdAt || b.createTime || '');
                return dateB.getTime() - dateA.getTime();
            });
            break;
        case 'priceHigh':
            result.sort((a, b) => b.price - a.price);
            break;
        case 'priceLow':
            result.sort((a, b) => a.price - b.price);
            break;
        case 'areaLarge':
            result.sort((a, b) => b.area - a.area);
            break;
    }

    return result;
});

// 获取房源状态的展示形式
const getStatusDisplay = (status: string): string => {
    return statusMap[status] || status;
}

// 格式化日期
const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return '未知时间';

    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 格式化价格（万元）
const formatPrice = (price: number): string => {
    return (price / 10000).toFixed(1) + '万';
}

// 查看房源详情（打开模态框）
const viewHouse = (house: HousePublishRecord): void => {
    currentHouse.value = house;
    showViewModal.value = true;
}

// 关闭查看详情模态框
const closeViewModal = (): void => {
    showViewModal.value = false;
    setTimeout(() => {
        currentHouse.value = null;
    }, 300);
}

// 删除房源
const deleteHouse = async (id: number): Promise<void> => {
    if (!confirm('确定要删除此房源吗？该操作不可恢复。')) {
        return;
    }

    try {
        const token = localStorage.getItem('token');
        if (!token) return;

        // 修改为使用正确的API端点
        const response = await axios.delete(`/api/houses/remove-record?id=${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data && response.data.code === 'SUCCESS') {
            houses.value = houses.value.filter(h => h.id !== id);
            // 如果在模态框中，则关闭模态框
            if (showViewModal.value && currentHouse.value?.id === id) {
                closeViewModal();
            }
            alert('房源已成功删除');
        } else {
            throw new Error(response.data?.message || '删除失败');
        }
    } catch (err: any) {
        console.error('删除房源失败:', err);
        alert(err.message || '删除失败，请稍后重试');
    }
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
            const index = houses.value.findIndex(h => h.id === id);
            if (index !== -1) {
                houses.value[index].status = 'UNPUBLISHED';
                houses.value = [...houses.value]; // 触发视图更新
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

onMounted(() => {
    loadHouses();
});
</script>

<template>
    <div class="bg-gray-50 min-h-screen py-8">
        <div class="max-w-6xl mx-auto px-4">
            <!-- 页面标题和发布按钮 -->
            <div class="flex justify-between items-center mb-6">
                <h1 class="text-2xl font-bold text-gray-800">我的发布房源</h1>
                <router-link to="/sell"
                    class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
                    发布新房源
                </router-link>
            </div>

            <!-- 筛选和排序工具条 -->
            <div class="bg-white rounded-lg shadow-sm p-4 mb-6 flex flex-wrap items-center gap-4">
                <div class="flex items-center">
                    <label for="status-filter" class="text-gray-700 mr-2">状态:</label>
                    <select id="status-filter" v-model="filterStatus"
                        class="border border-gray-300 rounded px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="all">全部状态</option>
                        <option value="PENDING">待审核</option>
                        <option value="RECEIVED">已接收</option>
                        <option value="APPROVED">已通过</option>
                        <option value="REJECTED">已拒绝</option>
                        <option value="PUBLISHED">已发布</option>
                    </select>
                </div>

                <div class="flex items-center">
                    <label for="sort-option" class="text-gray-700 mr-2">排序:</label>
                    <select id="sort-option" v-model="sortOption"
                        class="border border-gray-300 rounded px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="latest">最新发布</option>
                        <option value="priceHigh">价格从高到低</option>
                        <option value="priceLow">价格从低到高</option>
                        <option value="areaLarge">面积从大到小</option>
                    </select>
                </div>

                <div class="ml-auto flex">
                    <button @click="loadHouses" class="flex items-center text-blue-600 hover:text-blue-800 px-3 py-1.5">
                        <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        刷新
                    </button>
                </div>
            </div>

            <!-- 加载中 -->
            <div v-if="loading" class="bg-white rounded-lg shadow-md p-8 text-center">
                <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600 mx-auto"></div>
                <p class="mt-4 text-gray-500">正在加载房源数据...</p>
            </div>

            <!-- 错误信息 -->
            <div v-else-if="error" class="bg-white rounded-lg shadow-md p-6">
                <div class="p-4 bg-red-50 text-red-700 rounded-lg border border-red-100">
                    {{ error }}
                    <button @click="loadHouses" class="ml-2 text-blue-600 hover:text-blue-800 underline">重试</button>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="filteredAndSortedHouses.length === 0"
                class="bg-white rounded-lg shadow-md p-10 text-center">
                <div class="text-gray-400 mb-4">
                    <svg class="w-16 h-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                            d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                    </svg>
                </div>
                <h3 class="text-lg font-medium text-gray-700 mb-2">
                    {{ filterStatus === 'all' ? '您还没有发布过房源' : '没有符合条件的房源' }}
                </h3>
                <p class="text-gray-500 mb-6">
                    {{ filterStatus === 'all' ? '发布您的第一个房源，开始您的买卖之旅' : '请尝试更改筛选条件' }}
                </p>
                <router-link v-if="filterStatus === 'all'" to="/sell"
                    class="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
                    立即发布房源
                </router-link>
            </div>

            <!-- 房源列表 -->
            <div v-else class="grid grid-cols-1 gap-6">
                <div v-for="house in filteredAndSortedHouses" :key="house.id"
                    class="bg-white rounded-lg shadow-sm overflow-hidden">
                    <div class="flex flex-col md:flex-row">
                        <!-- 房源图片 -->
                        <div class="md:w-64 h-48 md:h-auto flex-shrink-0 bg-gray-100 overflow-hidden cursor-pointer"
                            @click="viewHouse(house)">
                            <img v-if="house.image" :src="house.image" alt="房源图片" class="w-full h-full object-cover">
                            <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
                                <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                        d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                            </div>
                        </div>

                        <!-- 房源信息 -->
                        <div class="p-6 flex-1 flex flex-col">
                            <div class="flex justify-between items-start">
                                <h2 class="text-xl font-bold text-gray-800 mb-2 cursor-pointer hover:text-blue-600"
                                    @click="viewHouse(house)">
                                    {{ house.title }}
                                </h2>
                                <div
                                    :class="`px-3 py-1 text-xs font-medium rounded-full border ${statusColorClass(house.status)}`">
                                    {{ getStatusDisplay(house.status) }}
                                </div>
                            </div>

                            <p class="text-gray-600 mb-4">{{ house.address }}</p>

                            <div class="flex flex-wrap gap-x-4 gap-y-2 mb-4">
                                <div class="flex items-center text-gray-500">
                                    <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                    </svg>
                                    {{ house.rooms }}
                                </div>
                                <div class="flex items-center text-gray-500">
                                    <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5v-4m0 4h-4m4 0l-5-5" />
                                    </svg>
                                    {{ house.area }}平米
                                </div>
                                <div v-if="house.orientation" class="flex items-center text-gray-500">
                                    <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 11l3-3m0 0l3 3m-3-3v8m0-13a9 9 0 110 18 9 9 0 010-18z" />
                                    </svg>
                                    {{ house.orientation }}
                                </div>
                                <div v-if="house.floor" class="flex items-center text-gray-500">
                                    <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                    </svg>
                                    {{ house.floor }}
                                </div>
                            </div>

                            <div class="mt-auto">
                                <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
                                    <div>
                                        <span class="text-2xl font-bold text-blue-600">{{ formatPrice(house.price) }}</span>
                                        <span v-if="house.unitPrice" class="text-sm text-gray-500 ml-2">{{ house.unitPrice }}元/平</span>
                                    </div>

                                    <div class="flex gap-2">
                                        <!-- 查看按钮 -->
                                        <button @click="viewHouse(house)"
                                            class="px-3 py-1.5 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors">
                                            查看详情
                                        </button>

                                        <!-- 删除按钮 -->
                                        <button v-if="house.status === 'REJECTED'" @click="deleteHouse(house.id)"
                                            class="px-3 py-1.5 bg-red-50 text-red-600 rounded hover:bg-red-100 transition-colors">
                                            删除
                                        </button>

                                        <!-- 下架按钮 -->
                                        <button v-if="house.status === 'PUBLISHED'" @click="unpublishHouse(house.id)"
                                            class="px-3 py-1.5 bg-red-50 text-red-600 rounded hover:bg-red-100 transition-colors">
                                            下架
                                        </button>
                                    </div>
                                </div>

                                <!-- 提交时间 -->
                                <div class="mt-4 text-sm text-gray-500">
                                    提交时间: {{ formatDate(house.createdAt || house.createTime) }}
                                </div>
                            </div>

                            <!-- 被拒绝原因 -->
                            <div v-if="house.status === 'REJECTED' && house.reason"
                                class="mt-4 p-3 bg-red-50 text-red-700 text-sm rounded border border-red-100">
                                <div class="font-medium mb-1">拒绝原因:</div>
                                <div>{{ house.reason }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 底部信息 -->
            <div v-if="filteredAndSortedHouses.length > 0" class="mt-6 text-center text-gray-500 text-sm">
                共 {{ filteredAndSortedHouses.length }} 条房源记录
                <span v-if="filteredAndSortedHouses.length !== houses.length">
                    (共 {{ houses.length }} 条, 已筛选)
                </span>
            </div>
        </div>
    </div>

    <!-- 查看详情模态框 -->
    <div v-if="showViewModal" class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50 p-4 transition-opacity">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto animate-scale-in">
            <div class="p-6 border-b border-gray-200 flex justify-between items-center">
                <h2 class="text-xl font-bold text-gray-800">房源详情</h2>
                <button @click="closeViewModal" class="text-gray-500 hover:text-gray-700 focus:outline-none">
                    <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <div v-if="currentHouse" class="p-6">
                <!-- 房源详情内容 -->
                <div class="flex flex-col md:flex-row gap-6">
                    <!-- 房源图片 -->
                    <div class="md:w-1/2">
                        <img v-if="currentHouse.image" :src="currentHouse.image" alt="房源图片"
                            class="w-full h-64 object-cover rounded-lg">
                        <div v-else class="w-full h-64 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400">
                            <svg class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                        </div>
                    </div>

                    <!-- 房源信息 -->
                    <div class="md:w-1/2">
                        <h3 class="text-2xl font-bold text-gray-800 mb-3">{{ currentHouse.title }}</h3>

                        <div class="mb-4">
                            <div class="inline-block px-3 py-1 text-sm font-medium rounded-full border"
                                :class="statusColorClass(currentHouse.status)">
                                {{ getStatusDisplay(currentHouse.status) }}
                            </div>
                        </div>

                        <div class="space-y-4">
                            <div>
                                <h4 class="text-sm text-gray-500">地址</h4>
                                <p class="text-base text-gray-800">{{ currentHouse.address }}</p>
                            </div>

                            <div class="flex gap-8">
                                <div>
                                    <h4 class="text-sm text-gray-500">总价</h4>
                                    <p class="text-xl font-bold text-blue-600">{{ formatPrice(currentHouse.price) }}</p>
                                </div>
                                <div>
                                    <h4 class="text-sm text-gray-500">单价</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.unitPrice }}元/平</p>
                                </div>
                            </div>

                            <div class="flex gap-8">
                                <div>
                                    <h4 class="text-sm text-gray-500">面积</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.area }}平米</p>
                                </div>
                                <div>
                                    <h4 class="text-sm text-gray-500">户型</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.rooms }}</p>
                                </div>
                            </div>

                            <div v-if="currentHouse.floor || currentHouse.buildYear" class="flex gap-8">
                                <div v-if="currentHouse.floor">
                                    <h4 class="text-sm text-gray-500">楼层</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.floor }}</p>
                                </div>
                                <div v-if="currentHouse.buildYear">
                                    <h4 class="text-sm text-gray-500">建造年代</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.buildYear }}年</p>
                                </div>
                            </div>

                            <div v-if="currentHouse.orientation || currentHouse.decoration" class="flex gap-8">
                                <div v-if="currentHouse.orientation">
                                    <h4 class="text-sm text-gray-500">朝向</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.orientation }}</p>
                                </div>
                                <div v-if="currentHouse.decoration">
                                    <h4 class="text-sm text-gray-500">装修</h4>
                                    <p class="text-base text-gray-800">{{ currentHouse.decoration }}</p>
                                </div>
                            </div>

                            <!-- 标签 -->
                            <div v-if="currentHouse.tag && currentHouse.tag.length > 0">
                                <h4 class="text-sm text-gray-500 mb-1">标签</h4>
                                <div class="flex flex-wrap gap-2">
                                    <span v-for="tag in currentHouse.tag" :key="tag"
                                        class="px-2 py-1 bg-blue-50 text-blue-600 text-xs rounded-md">
                                        {{ tag }}
                                    </span>
                                </div>
                            </div>

                            <!-- 拒绝原因 -->
                            <div v-if="currentHouse.status === 'REJECTED' && currentHouse.reason"
                                class="p-3 bg-red-50 text-red-700 text-sm rounded border border-red-100">
                                <div class="font-medium mb-1">拒绝原因:</div>
                                <div>{{ currentHouse.reason }}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 提交时间 -->
                <div class="mt-6 text-sm text-gray-500">
                    提交时间: {{ formatDate(currentHouse.createdAt || currentHouse.createTime) }}
                </div>
                
                <!-- 操作按钮 -->
                <div v-if="currentHouse.status === 'REJECTED'" class="mt-4">
                    <p class="text-gray-600 mb-2">如果您想修改被拒绝的房源信息，可以删除当前记录并重新发布。</p>
                    <button @click="deleteHouse(currentHouse.id)" class="px-4 py-2 bg-red-50 text-red-600 rounded hover:bg-red-100 transition-colors">
                        删除此房源
                    </button>
                </div>
            </div>

            <div class="p-4 border-t border-gray-200 flex justify-end">
                <button @click="closeViewModal"
                    class="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors">
                    关闭
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.transition-colors {
    transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

.transition-opacity {
    transition: opacity 0.3s ease;
}

.animate-scale-in {
    animation: scale-in 0.3s ease-out;
}

@keyframes scale-in {
    from {
        opacity: 0;
        transform: scale(0.95);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}

select {
    appearance: none;
    background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3E%3C/svg%3E");
    background-position: right 0.5rem center;
    background-repeat: no-repeat;
    background-size: 1.5em 1.5em;
    padding-right: 2.5rem;
}
</style>