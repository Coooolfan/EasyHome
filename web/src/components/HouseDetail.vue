<script setup lang="ts">
import { computed, watch, onUnmounted, ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

// 获取路由器实例
const router = useRouter();

// 为内容区域添加引用，指定正确的类型
const contentRef = ref<HTMLDivElement | null>(null);

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
    (e: 'favoriteChange', houseId: number, isFavorited: boolean): void
}>()

// 收藏状态
const isFavorite = ref(false);

// 标签页状态管理
const activeTab = ref('details') // 'details', 'description', 'contact'

// 分享面板状态
const showSharePanel = ref(false);

// 二维码相关状态
const showQrCodeImage = ref(false);
const qrCodeImageUrl = ref('');
const shareImageLoading = ref(false);

// 关闭模态框
const closeModal = () => {
    // 确保关闭前同步收藏状态
    if (props.house) {
        emit('favoriteChange', props.house.id, isFavorite.value);
    }
    emit('close')
}

// 切换标签
const switchTab = (tab: string) => {
    activeTab.value = tab
    // 切换标签时，重置滚动位置
    nextTick(() => {
        if (contentRef.value) {
            contentRef.value.scrollTop = 0;
        }
    });
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

// 检查房源是否已收藏
const checkFavoriteStatus = async () => {
    if (!props.house) return;
    
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            // 未登录时从本地存储检查
            const savedFavorites = localStorage.getItem('favoriteHouses');
            if (savedFavorites) {
                const favorites = JSON.parse(savedFavorites);
                isFavorite.value = favorites.includes(props.house.id);
            }
            return;
        }
        
        // 已登录，调用后端API检查
        const response = await axios.get(`/api/favorites/check?houseId=${props.house.id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.data && response.data.code === 'SUCCESS') {
            isFavorite.value = response.data.data;
        }
    } catch (err) {
        console.error('检查收藏状态失败:', err);
        // 出错时尝试从本地存储检查
        const savedFavorites = localStorage.getItem('favoriteHouses');
        if (savedFavorites && props.house) {
            const favorites = JSON.parse(savedFavorites);
            isFavorite.value = favorites.includes(props.house.id);
        }
    }
};

// 切换收藏状态
const toggleFavorite = async () => {
    if (!props.house) return;
    
    try {
        const token = localStorage.getItem('token');
        const oldStatus = isFavorite.value;
        
        if (!token) {
            // 未登录时处理本地收藏
            const savedFavorites = localStorage.getItem('favoriteHouses');
            let favorites = savedFavorites ? JSON.parse(savedFavorites) : [];
            
            if (isFavorite.value) {
                // 从收藏列表移除
                favorites = favorites.filter((id: number) => id !== props.house?.id);
            } else {
                // 添加到收藏列表
                favorites.push(props.house.id);
            }
            
            // 保存到本地存储
            localStorage.setItem('favoriteHouses', JSON.stringify(favorites));
            // 更新UI状态
            isFavorite.value = !oldStatus;
            
            // 通知父组件状态变化
            emit('favoriteChange', props.house.id, isFavorite.value);
            return;
        }
        
        // 已登录，调用后端API
        if (isFavorite.value) {
            // 取消收藏
            const response = await axios.delete(`/api/favorites/remove?houseId=${props.house.id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (response.data && response.data.code === 'SUCCESS') {
                isFavorite.value = false;
                // 通知父组件状态变化
                emit('favoriteChange', props.house.id, false);
            }
        } else {
            // 添加收藏
            const response = await axios.post(`/api/favorites/add?houseId=${props.house.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (response.data && response.data.code === 'SUCCESS') {
                isFavorite.value = true;
                // 通知父组件状态变化
                emit('favoriteChange', props.house.id, true);
            }
        }
    } catch (err) {
        console.error('操作收藏失败:', err);
    }
};

// 获取正确的分享链接
const getShareLink = () => {
  if (!props.house) return '';
  // 使用与预约看房相同的URL格式，确保链接可用
  return `${window.location.origin}/view?houseId=${props.house.id}`;
};

// 分享房源
const shareHouse = async () => {
  if (!props.house) return;
  
  // 构建分享内容
  const title = `${props.house.title} - ${formatPrice(props.house.price)}`;
  const text = `我在EasyHome发现了一套好房: ${props.house.rooms}, ${props.house.area}㎡, ${props.house.decoration}`;
  
  // 使用正确的URL
  const url = getShareLink();
  
  try {
    // 检查Web Share API是否可用
    if (navigator.share) {
      // 使用系统分享
      await navigator.share({
        title,
        text,
        url
      });
      console.log('分享成功');
      return;
    }
    
    // 如果Web Share API不可用，使用自定义分享面板
    showSharePanel.value = true;
    
  } catch (error) {
    console.error('分享失败:', error);
    // 使用自定义分享面板作为备选
    showSharePanel.value = true;
  }
};

// 关闭分享面板
const closeSharePanel = () => {
  showSharePanel.value = false;
};

// 复制分享链接到剪贴板
const copyShareLink = () => {
  if (!props.house) return;
  
  const shareUrl = getShareLink();
  
  // 使用Clipboard API
  navigator.clipboard.writeText(shareUrl)
    .then(() => {
      alert('链接已复制到剪贴板');
      closeSharePanel();
    })
    .catch(err => {
      console.error('无法复制链接:', err);
      // 备选方案：创建临时输入框
      const input = document.createElement('input');
      input.value = shareUrl;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      alert('链接已复制到剪贴板');
      closeSharePanel();
    });
};

// 分享到微信
const shareToWeChat = () => {
  if (!props.house) return;
  
  // 生成二维码而不是简单的提示
  generateShareImage();
};

// 分享到微博
const shareToWeibo = () => {
  if (!props.house) return;
  
  const title = `${props.house.title} - ${formatPrice(props.house.price)}`;
  const text = `我在EasyHome发现了一套好房: ${props.house.rooms}, ${props.house.area}㎡, ${props.house.address}`;
  const url = getShareLink();
  
  // 微博分享链接
  const weiboShareUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(url)}&title=${encodeURIComponent(text)}`;
  
  // 打开新窗口
  window.open(weiboShareUrl, '_blank');
  closeSharePanel();
};

// 生成房源二维码
const generateShareImage = async () => {
  if (!props.house) return;
  
  // 显示加载状态
  shareImageLoading.value = true;
  
  try {
    // 房源分享链接
    const shareUrl = getShareLink();
    
    // 使用在线API生成二维码图片URL
    const qrCodeApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(shareUrl)}`;
    
    // 设置二维码图片URL
    qrCodeImageUrl.value = qrCodeApiUrl;
    
    // 显示二维码弹窗
    showQrCodeImage.value = true;
    
    // 隐藏分享面板
    showSharePanel.value = false;
  } catch (error) {
    console.error('生成二维码失败:', error);
    alert('生成二维码失败，请稍后重试');
  } finally {
    shareImageLoading.value = false;
  }
};

// 关闭二维码弹窗
const closeQrCodeImage = () => {
  showQrCodeImage.value = false;
  qrCodeImageUrl.value = '';
};

// 下载二维码图片
const downloadQrCode = () => {
  if (!qrCodeImageUrl.value) return;
  
  // 创建一个隐藏的<a>元素来下载图片
  const link = document.createElement('a');
  link.href = qrCodeImageUrl.value;
  link.download = `EasyHome-房源-${props.house?.id}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 跳转到看房预约页面
const goToViewHouse = () => {
    if (props.house) {
        // 关闭当前模态框
        closeModal();
        
        // 跳转到看房页面并携带房屋ID
        router.push({
            path: '/view',
            query: { houseId: props.house.id.toString() }
        });
    }
}

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
    if (newVal && props.house) {
        // 重置到默认标签
        activeTab.value = 'details'
        
        // 重置分享面板状态
        showSharePanel.value = false
        showQrCodeImage.value = false
        
        // 检查收藏状态
        checkFavoriteStatus();
        
        // 重置滚动位置
        nextTick(() => {
            if (contentRef.value) {
                contentRef.value.scrollTop = 0;
            }
        });
    }
}, { immediate: true })

// 监听 house 属性变化
watch(() => props.house, (newVal) => {
    if (newVal) {
        checkFavoriteStatus();
    }
})

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
                :class="show ? 'scale-100 opacity-100' : 'scale-95 opacity-0'" @click.stop>
                <div v-if="house" class="flex flex-col h-full">
                    <!-- 模态框头部 - 使用更大的图片和渐变叠加 -->
                    <div class="relative">
                        <div class="relative h-80 overflow-hidden">
                            <img :src="house.image || 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=800&h=500&fit=crop'"
                                :alt="house.title"
                                class="w-full h-full object-cover transition-transform duration-700 hover:scale-110">
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
                            <span
                                class="w-8 h-8 flex items-center justify-center rounded-full bg-blue-100 text-blue-600">
                                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                                </svg>
                            </span>
                            <span>{{ house.rooms }}</span>
                        </div>
                        <div class="flex items-center space-x-2">
                            <span
                                class="w-8 h-8 flex items-center justify-center rounded-full bg-green-100 text-green-600">
                                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                                </svg>
                            </span>
                            <span>{{ house.area }}㎡</span>
                        </div>
                        <div class="flex items-center space-x-2">
                            <span
                                class="w-8 h-8 flex items-center justify-center rounded-full bg-amber-100 text-amber-600">
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
                            <button @click="switchTab('details')" :class="[
                                'py-4 px-1 border-b-2 text-sm font-medium mr-8',
                                activeTab === 'details'
                                    ? 'border-blue-600 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            ]">
                                房源详情
                            </button>
                            <button @click="switchTab('description')" :class="[
                                'py-4 px-1 border-b-2 text-sm font-medium mr-8',
                                activeTab === 'description'
                                    ? 'border-blue-600 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            ]">
                                房源描述
                            </button>
                        </nav>
                    </div>

                    <!-- 模态框内容区域 - 添加ref和增加底部内边距 -->
                    <div ref="contentRef" class="p-6 pb-32 overflow-y-auto flex-grow" style="max-height: calc(50vh);">
                        <!-- 详情标签页 -->
                        <div v-if="activeTab === 'details'">
                            <!-- 房屋详情 - 更有组织的信息展示 -->
                            <div class="bg-gray-50 rounded-xl p-6 shadow-inner">
                                <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">户型</p>
                                        <p class="font-medium text-lg mt-1">{{ house.rooms }}</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">建筑面积</p>
                                        <p class="font-medium text-lg mt-1">{{ house.area }}㎡</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">楼层</p>
                                        <p class="font-medium text-lg mt-1">{{ house.floor }}</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">朝向</p>
                                        <p class="font-medium text-lg mt-1">{{ house.orientation }}</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">装修</p>
                                        <p class="font-medium text-lg mt-1">{{ house.decoration }}</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">建筑年代</p>
                                        <p class="font-medium text-lg mt-1">{{ house.buildYear }}年</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">挂牌时间</p>
                                        <p class="font-medium text-lg mt-1">{{ formatDate(house.createdAt) }}</p>
                                    </div>
                                    <div
                                        class="bg-white rounded-lg p-4 shadow-sm transition-shadow duration-200 hover:shadow">
                                        <p class="text-xs text-gray-500 uppercase tracking-wider">更新时间</p>
                                        <p class="font-medium text-lg mt-1">{{ formatDate(house.updatedAt) }}</p>
                                    </div>
                                </div>
                            </div>

                            <!-- 标签 - 更好的视觉展示 -->
                            <div v-if="hasTags" class="mt-8">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" viewBox="0 0 24 24"
                                        stroke="currentColor">
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

                        <!-- 描述标签页 - 增加底部边距确保可见性 -->
                        <div v-else-if="activeTab === 'description'" class="space-y-6 mb-6">
                            <!-- 房源描述 - 更丰富的内容 -->
                            <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24"
                                        stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z">
                                        </path>
                                    </svg>
                                    房源概述
                                </h3>
                                <div class="text-gray-600 space-y-4">
                                    <p>这套位于<span class="text-blue-600 font-medium">{{ house.address
                                            }}</span>的精选房源，建筑面积<span class="text-blue-600 font-medium">{{ house.area
                                            }}</span>平米，<span class="text-blue-600 font-medium">{{ house.rooms
                                            }}</span>，
                                        <span class="text-blue-600 font-medium">{{ house.decoration }}</span>，楼层<span
                                            class="text-blue-600 font-medium">{{ house.floor }}</span>，
                                        朝向<span class="text-blue-600 font-medium">{{ house.orientation }}</span>，
                                        建筑年代<span class="text-blue-600 font-medium">{{ house.buildYear }}</span>年。
                                    </p>

                                    <p>{{ suggestedHouseholdSize }}，{{ priceLevel }}。该房源特点：{{ house.tag ?
                                        house.tag.join('，') : '暂无特点' }}。</p>

                                    <p>总价<span class="text-red-600 font-medium">{{ formatPrice(house.price) }}</span>，
                                        单价<span class="text-red-600 font-medium">{{ house.unitPrice }}</span>元/平米。</p>
                                </div>
                            </div>

                            <!-- 区域介绍 - 新增 -->
                            <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24"
                                        stroke="currentColor">
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

                            <!-- 推荐理由 - 新增足够的底部边距 -->
                            <div
                                class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-6 shadow-sm border border-blue-100">
                                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                    <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" viewBox="0 0 24 24"
                                        stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5">
                                        </path>
                                    </svg>
                                    推荐理由
                                </h3>
                                <div class="space-y-2">
                                    <div v-for="(feature, index) in mainFeatures" :key="index" class="flex items-start">
                                        <span
                                            class="inline-flex items-center justify-center flex-shrink-0 w-5 h-5 mr-3 bg-blue-600 rounded-full text-white text-xs">
                                            {{ index + 1 }}
                                        </span>
                                        <p class="text-gray-800">{{ feature }}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 底部操作条 - 固定在底部 -->
                    <div
                        class="absolute bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-4 flex items-center justify-between shadow-md z-10">
                        <!-- 分享和收藏按钮 -->
                        <div class="flex space-x-3">
                            <button
                                @click="shareHouse"
                                class="flex items-center space-x-1 px-3 py-2 bg-gray-100 rounded-md text-gray-700 hover:bg-gray-200 transition-colors">
                                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                                </svg>
                                <span>分享</span>
                            </button>
                            <button @click="toggleFavorite"
                                class="flex items-center space-x-1 px-3 py-2 rounded-md transition-colors"
                                :class="isFavorite ? 'bg-red-50 text-red-600' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'">
                                <svg class="h-5 w-5" :fill="isFavorite ? 'currentColor' : 'none'" viewBox="0 0 24 24"
                                    stroke="currentColor" :class="isFavorite ? 'text-red-500' : 'text-gray-700'">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                </svg>
                                <span>{{ isFavorite ? '已收藏' : '收藏' }}</span>
                            </button>
                        </div>

                        <!-- 预约看房按钮 - 使用goToViewHouse方法 -->
                        <button @click="goToViewHouse"
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

        <!-- 分享面板 -->
        <div v-if="showSharePanel" class="fixed inset-0 bg-black bg-opacity-60 backdrop-blur-sm z-[60] flex items-center justify-center p-4 transition-all duration-300"
            @click="closeSharePanel">
            <div class="bg-white rounded-xl max-w-sm w-full overflow-hidden shadow-2xl" @click.stop>
                <div class="p-6">
                    <div class="flex justify-between items-center mb-6">
                        <h3 class="text-lg font-semibold text-gray-900">分享此房源</h3>
                        <button @click="closeSharePanel" class="text-gray-400 hover:text-gray-500">
                            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    <!-- 分享链接 -->
                    <div class="mb-6">
                        <p class="text-sm text-gray-500 mb-2">复制链接分享给朋友</p>
                        <div class="flex items-center">
                            <input type="text" :value="getShareLink()" readonly
                                class="flex-1 border border-gray-300 rounded-l-md px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 bg-gray-50 text-gray-800 text-sm" />
                            <button @click="copyShareLink"
                                class="bg-blue-600 text-white px-4 py-2 rounded-r-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2">
                                复制
                            </button>
                        </div>
                    </div>

                    <!-- 分享方式 -->
                    <div class="grid grid-cols-4 gap-4">
                        <!-- 微信分享 -->
                        <div @click="shareToWeChat" class="flex flex-col items-center justify-center cursor-pointer">
                            <div class="w-12 h-12 bg-green-50 rounded-full flex items-center justify-center mb-1">
                                <svg class="w-6 h-6 text-green-600" fill="currentColor" viewBox="0 0 1024 1024">
                                    <path d="M692.8 412.3c11.5 0 22.9 0.9 34.2 2.3-30.7-143.1-183.9-249.6-358.8-249.6-195.5 0-354.9 133.4-354.9 304.1 0 98.7 53.8 179.6 143.8 242.4l-36 108.4 125.6-63c44.9 8.9 81.2 18.1 126.1 18.1 11.3 0 22.5-0.6 33.6-1.5-7-24-11.1-49.2-11.1-75.3 0.1-158 135.8-286.2 304.1-286.2v0.9l-0.3-0.6zM500.7 321.5c27.1 0 45.1 17.9 45.1 45.1 0 27.1-17.9 45.1-45.1 45.1-27.1 0-54.2-17.9-54.2-45.1 0.1-27.2 27.2-45.1 54.2-45.1zM281.5 411.7c-27.1 0-54.2-17.9-54.2-45.1 0-27.1 27.1-45.1 54.2-45.1 27.1 0 45.1 17.9 45.1 45.1 0 27.1-18 45.1-45.1 45.1z"></path>
                                    <path d="M1004.7 630.3c0-144.9-145-263-307.9-263-172.3 0-308 118-308 263 0 144.9 135.7 263 308 263 36 0 72.1-9.1 108.1-18.1l99 54.2-27.2-90.3c72.5-54.3 128-126.7 128-208.8zM619.1 585.3c-18 0-36-18-36-36 0-18 18-36 36-36s36 18 36 36c0 18-18 36-36 36zM801.1 585.3c-18 0-36-18-36-36 0-18 18-36 36-36s36 18 36 36c0 18-18 36-36 36z"></path>
                                </svg>
                            </div>
                            <span class="text-xs text-gray-600">微信</span>
                        </div>

                        <!-- 微博分享 -->
                        <div @click="shareToWeibo" class="flex flex-col items-center justify-center cursor-pointer">
                            <div class="w-12 h-12 bg-red-50 rounded-full flex items-center justify-center mb-1">
                                <svg class="w-6 h-6 text-red-500" fill="currentColor" viewBox="0 0 1024 1024">
                                    <path d="M851.4 590.2c-22.5-66.7-90.4-103.6-157.1-82.2-66.7 21.5-103.6 89.4-82.2 156.1 22.5 66.7 90.4 103.6 157.1 82.2 66.8-21.5 103.6-89.4 82.2-156.1zM778.1 677.8c-18.3 5.4-38-4.8-44.5-23.1-6.5-18.3 3.3-38 21.6-44.5 18.3-6.5 38.9 3.3 44.5 21.6 5.4 19.4-3.3 39.1-21.6 46zM815 631.2c-7.5 5.4-16.1 2.2-18.3-3.3-2.2-7.5 1.1-14 7.5-17.2 7.5-3.3 15 0 17.2 5.4 3.2 5.5 0 12.9-6.4 15.1zM661.3 462.3c-12.9-4.3-22.5-7.5-15-23.7 15-33.4 15-61.7 0-81.2-29-29-106.9-28-196.2 1.1 0 0-28 12.9-21.6-10.7 12.9-41 12.9-77.4-9.7-97.9-47.8-47.8-177.1 1.1-286.2 107.9C44 446.4 7.4 539 7.4 619c0 151.7 193.4 245.7 383.3 245.7 248.6 0 414.5-145.3 414.5-260.9 0-69.9-58.1-110-143.9-141.5zM402.2 791.6C229.5 800.2 90.9 721.6 83.4 614.7c-7.5-106.9 110.1-207 282.9-215.7 172.8-8.6 311.4 69.9 318.9 176.8 7.5 108.1-110.2 207.1-283 215.8zM567.4 521.7c-7.5-81.2-112.2-138.4-231.4-128.8-107.9 9.7-191.1 85.5-184.7 165.6 7.5 80.1 112.2 137.3 230.3 127.7 109-9.7 192.2-84.3 185.8-164.5z"></path>
                                    <path d="M443.3 672.7c-53.6 5.4-99.2-18.3-103.6-53.6-4.3-35.2 36-69.8 89.4-75.2 53.6-5.4 99.2 18.3 103.6 53.6 4.3 35.2-36 69.9-89.4 75.2z"></path>
                                </svg>
                            </div>
                            <span class="text-xs text-gray-600">微博</span>
                        </div>

                        <!-- 生成二维码 -->
                        <div @click="generateShareImage" class="flex flex-col items-center justify-center cursor-pointer">
                            <div class="w-12 h-12 bg-purple-50 rounded-full flex items-center justify-center mb-1">
                                <svg class="w-6 h-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
                                </svg>
                            </div>
                            <span class="text-xs text-gray-600">生成二维码</span>
                        </div>

                        <!-- 复制链接 -->
                        <div @click="copyShareLink" class="flex flex-col items-center justify-center cursor-pointer">
                            <div class="w-12 h-12 bg-blue-50 rounded-full flex items-center justify-center mb-1">
                                <svg class="w-6 h-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
                                </svg>
                            </div>
                            <span class="text-xs text-gray-600">复制链接</span>
                        </div>
                    </div>
                </div>

                <!-- 底部提示 -->
                <div class="bg-gray-50 px-6 py-3 text-center">
                    <p class="text-xs text-gray-500">分享后将携带您的个人标识</p>
                </div>
            </div>
        </div>
        
        <!-- 二维码弹窗 -->
        <div v-if="showQrCodeImage && house" class="fixed inset-0 bg-black bg-opacity-60 backdrop-blur-sm z-[70] flex items-center justify-center p-4 transition-all duration-300"
            @click="closeQrCodeImage">
            <div class="bg-white rounded-xl max-w-sm w-full overflow-hidden shadow-2xl" @click.stop>
                <div class="p-6">
                    <div class="flex justify-between items-center mb-6">
                        <h3 class="text-lg font-semibold text-gray-900">房源分享二维码</h3>
                        <button @click="closeQrCodeImage" class="text-gray-400 hover:text-gray-500">
                            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                    
                    <!-- 二维码图片 -->
                    <div class="flex flex-col items-center justify-center space-y-4">
                        <div class="p-3 border border-gray-200 rounded-lg shadow-sm bg-white">
                            <img v-if="qrCodeImageUrl && !shareImageLoading" :src="qrCodeImageUrl" alt="房源二维码" class="w-48 h-48 object-contain" />
                            <div v-else class="w-48 h-48 flex items-center justify-center bg-gray-100">
                                <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                            </div>
                        </div>
                        <p class="text-sm text-gray-600 text-center">扫描二维码查看房源详情</p>
                        
                        <!-- 房源简介 -->
                        <div class="w-full p-3 bg-gray-50 rounded-md text-xs text-gray-600">
                            <div class="font-medium text-gray-800 text-sm mb-1">{{ house.title }}</div>
                            <div class="flex justify-between">
                                <span>{{ house.rooms }}</span>
                                <span>{{ house.area }}㎡</span>
                                <span class="text-red-600">{{ formatPrice(house.price) }}</span>
                            </div>
                        </div>
                        
                        <!-- 下载按钮 -->
                        <button @click="downloadQrCode" class="w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors flex items-center justify-center">
                            <svg class="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                            </svg>
                            保存二维码
                        </button>
                    </div>
                </div>
                
                <!-- 底部提示 -->
                <div class="bg-gray-50 px-6 py-3 text-center">
                    <p class="text-xs text-gray-500">微信扫一扫，分享给好友</p>
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

/* 增加滚动优化 */
.overflow-y-auto {
    -webkit-overflow-scrolling: touch;
    /* 提高移动端滚动体验 */
    scroll-behavior: smooth;
    /* 平滑滚动效果 */
    scrollbar-width: thin;
    /* Firefox 自定义滚动条 */
}

/* Chrome/Safari 自定义滚动条 */
.overflow-y-auto::-webkit-scrollbar {
    width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background-color: rgba(156, 163, 175, 0.5);
    border-radius: 3px;
}
</style>