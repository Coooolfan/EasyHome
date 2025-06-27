<script setup lang="ts">
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const currentPath = computed(() => route.path)

// 用户状态
const isLoggedIn = ref(false)
const username = ref('')

// 通知相关
interface Notification {
  id: number
  userId: number
  title: string
  content: string
  isRead: boolean
  createdAt: string
}

const notifications = ref<Notification[]>([])
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const showNotifications = ref(false)
const notificationRef = ref<HTMLElement | null>(null)
const loadingNotifications = ref(false)

// 从本地存储加载用户信息
const loadUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsedInfo = JSON.parse(userInfo)
      if (parsedInfo && parsedInfo.username) {
        isLoggedIn.value = true
        username.value = parsedInfo.username
        // 如果用户已登录，加载未读通知
        fetchUnreadNotifications()
      } else {
        isLoggedIn.value = false
        username.value = ''
      }
    } catch (e) {
      console.error('Failed to parse user info:', e)
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
    username.value = ''
  }
  
  // 如果用户未登录且当前页面是需要登录的页面，重定向到登录页
  if (!isLoggedIn.value && (currentPath.value === '/view' || currentPath.value === '/sell')) {
    router.push({
      path: '/login',
      query: { redirect: currentPath.value } // 保存原始目的地以便登录后重定向
    })
  }
}

// 获取未读通知
const fetchUnreadNotifications = async () => {
  if (!isLoggedIn.value) return
  
  try {
    loadingNotifications.value = true
    const token = localStorage.getItem('token')
    
    if (!token) return
    
    // 修改为使用/unread接口只获取未读通知
    const response = await axios.get('/api/notification/unread', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      notifications.value = response.data.data
    }
  } catch (error) {
    console.error('Failed to fetch unread notifications:', error)
  } finally {
    loadingNotifications.value = false
  }
}

// 标记通知为已读
const markAsRead = async (notificationId: number, event: Event) => {
  // 阻止事件冒泡，避免触发父元素的点击事件
  event.stopPropagation()
  
  try {
    const token = localStorage.getItem('token')
    
    if (!token) return
    
    // 调用更新接口
    await axios.put('/api/notification', null, {
      headers: { 'Authorization': `Bearer ${token}` },
      params: { id: notificationId }
    })
    
    // 从未读列表中移除此通知
    notifications.value = notifications.value.filter(n => n.id !== notificationId)
  } catch (error) {
    console.error('Failed to mark notification as read:', error)
  }
}

// 删除通知
const deleteNotification = async (notificationId: number, event: Event) => {
  // 阻止事件冒泡
  event.stopPropagation()
  
  try {
    const token = localStorage.getItem('token')
    
    if (!token) return
    
    // 调用删除通知API
    await axios.delete('/api/notification', {
      headers: { 'Authorization': `Bearer ${token}` },
      params: { id: notificationId }
    })
    
    // 从本地列表中移除通知
    notifications.value = notifications.value.filter(n => n.id !== notificationId)
  } catch (error) {
    console.error('Failed to delete notification:', error)
  }
}

// 标记所有通知为已读
const markAllAsRead = async () => {
  try {
    const token = localStorage.getItem('token')
    
    if (!token) return
    
    // 使用批量标记接口
    await axios.put('/api/notification/all', null, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    // 清空未读通知列表
    notifications.value = []
  } catch (error) {
    console.error('Failed to mark all notifications as read:', error)
  }
}

// 切换通知面板显示状态
const toggleNotifications = (event: Event) => {
  event.stopPropagation()
  showNotifications.value = !showNotifications.value
  
  if (showNotifications.value) {
    // 关闭用户下拉菜单
    showDropdown.value = false
    // 重新获取最新未读通知
    fetchUnreadNotifications()
  }
}

// 关闭通知面板
const closeNotifications = () => {
  showNotifications.value = false
}

// 定时轮询未读通知
let notificationTimer: number | null = null

const startNotificationPolling = () => {
  if (notificationTimer !== null) return
  
  // 每分钟检查一次未读通知
  notificationTimer = window.setInterval(async () => {
    if (isLoggedIn.value && !showNotifications.value) {
      await fetchUnreadNotifications()
    }
  }, 60000) // 60秒
}

const stopNotificationPolling = () => {
  if (notificationTimer !== null) {
    window.clearInterval(notificationTimer)
    notificationTimer = null
  }
}

// 初始化加载
onMounted(() => {
  loadUserInfo()
  
  // 添加全局点击事件监听，用于关闭下拉菜单和通知面板
  document.addEventListener('click', handleOutsideClick)
  
  // 启动通知轮询
  startNotificationPolling()
})

// 在组件卸载时清除事件监听和定时器
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
  stopNotificationPolling()
})

// 监听路由变化
watch(() => route.path, () => {
  showDropdown.value = false
  showNotifications.value = false
  // 每次路由变化时重新检查用户状态
  loadUserInfo()
})

// 处理登录/个人中心
const handleUserAction = () => {
  if (isLoggedIn.value) {
    // 已登录，跳转到个人中心
    router.push('/profile')
  } else {
    // 未登录，跳转到登录页
    router.push('/login')
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  isLoggedIn.value = false
  username.value = ''
  showDropdown.value = false
  notifications.value = []
  stopNotificationPolling()
  
  // 如果当前在需要登录的页面，退出后重定向到首页
  if (currentPath.value === '/view' || currentPath.value === '/sell' || 
      currentPath.value === '/profile' || currentPath.value === '/settings' || 
      currentPath.value === '/favorites' || currentPath.value === '/notifications') {
    router.push('/')
  }
}

// 控制下拉菜单
const showDropdown = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const toggleDropdown = (event: Event) => {
  event.stopPropagation()
  showDropdown.value = !showDropdown.value
  
  if (showDropdown.value) {
    // 关闭通知面板
    showNotifications.value = false
  }
}

const closeDropdown = () => {
  showDropdown.value = false
}

const handleOutsideClick = (event: Event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    showDropdown.value = false
  }
  
  if (notificationRef.value && !notificationRef.value.contains(event.target as Node)) {
    showNotifications.value = false
  }
}

// 处理受保护页面的点击
const handleProtectedRouteClick = (route: string, event: Event) => {
  if (!isLoggedIn.value) {
    event.preventDefault()
    router.push({
      path: '/login',
      query: { redirect: route }
    })
  }
}

// 格式化日期
const formatNotificationDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  
  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 如果是昨天
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 其他日期
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 跳转到消息中心
const goToNotificationCenter = () => {
  closeNotifications()
  router.push('/notifications')
}
</script>

<template>
  <nav class="bg-blue-700 shadow-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <!-- 左侧菜单 -->
        <div class="flex items-center">
          <router-link to="/" class="flex items-center text-2xl font-bold text-white">
            <svg class="w-8 h-8 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            EasyHome
          </router-link>
          <div class="ml-10 flex items-center space-x-4">
            <!-- 公开菜单项 -->
            <router-link to="/" class="px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              :class="[currentPath === '/' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              找房
            </router-link>
            <router-link to="/search" class="px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              :class="[currentPath === '/search' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              查房
            </router-link>
            
            <!-- 需登录才显示的菜单项 -->
            <template v-if="isLoggedIn">
              <router-link to="/view" class="px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
                :class="[currentPath === '/view' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                看房
              </router-link>
              <router-link to="/sell" class="px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
                :class="[currentPath === '/sell' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                卖房
              </router-link>
            </template>
          </div>
        </div>

        <!-- 右侧用户区域 -->
        <div class="flex items-center">
          <!-- 通知图标 - 仅在登录后显示 -->
          <div v-if="isLoggedIn" ref="notificationRef" class="relative mr-3">
            <button @click="toggleNotifications" 
              class="p-2 rounded-full text-white hover:bg-blue-600 transition-colors duration-200 focus:outline-none">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <!-- 可爱的未读通知数量徽章 -->
              <span v-if="unreadCount > 0" 
                    class="notification-badge absolute -top-1 -right-1 flex items-center justify-center min-w-5 h-5 px-1 text-xs font-bold text-white bg-red-500 rounded-full transform scale-bounce shadow-glow">
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </button>
            
            <!-- 通知下拉面板 - 只显示未读消息 -->
            <div v-if="showNotifications" 
                class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-solid py-2 z-50 border border-gray-200 max-h-96 overflow-y-auto notification-panel">
              <div class="px-4 py-2 border-b border-gray-100 flex justify-between items-center sticky top-0 bg-white z-10">
                <h3 class="text-lg font-medium text-gray-700">未读消息</h3>
                <button v-if="unreadCount > 0" @click="markAllAsRead" 
                  class="text-xs text-blue-600 hover:text-blue-800 transition-colors duration-200 bg-blue-50 hover:bg-blue-100 px-2 py-1 rounded-md">
                  全部已读
                </button>
              </div>
              
              <!-- 通知加载中 -->
              <div v-if="loadingNotifications" class="px-4 py-6 text-center text-gray-500">
                <svg class="animate-spin h-6 w-6 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <p>加载通知中...</p>
              </div>
              
              <!-- 没有通知 -->
              <div v-else-if="notifications.length === 0" class="px-4 py-6 text-center text-gray-500">
                <svg class="w-10 h-10 mx-auto mb-2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                        d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>暂无未读消息</p>
              </div>
              
              <!-- 未读通知列表 -->
              <div v-else>
                <div v-for="notification in notifications" :key="notification.id" 
                     class="px-4 py-3 hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition-colors duration-200 notification-item bg-blue-50">
                  
                  <div class="flex justify-between items-start">
                    <!-- 通知标题和指示器 -->
                    <div class="flex items-center">
                      <div class="w-2 h-2 bg-blue-600 rounded-full mr-2 pulse-animation"></div>
                      <h4 class="font-semibold text-gray-900">{{ notification.title }}</h4>
                    </div>
                    
                    <!-- 时间 -->
                    <div class="flex items-center">
                      <span class="text-xs text-gray-500 mr-2">{{ formatNotificationDate(notification.createdAt) }}</span>
                      
                      <!-- 操作按钮 -->
                      <div class="flex space-x-1">
                        <!-- 标记已读按钮 -->
                        <button @click="markAsRead(notification.id, $event)" 
                                class="text-blue-500 hover:text-blue-700 transition-colors p-1 rounded-full hover:bg-blue-100"
                                title="标记为已读">
                          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                          </svg>
                        </button>
                        
                        <!-- 删除按钮 -->
                        <button @click="deleteNotification(notification.id, $event)" 
                                class="text-red-500 hover:text-red-700 transition-colors p-1 rounded-full hover:bg-red-50"
                                title="删除通知">
                          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 通知内容 -->
                  <p class="mt-1 text-sm text-gray-600">{{ notification.content }}</p>
                </div>
              </div>
              
              <!-- 底部查看全部消息 -->
              <div class="px-4 py-2 border-t border-gray-100 mt-auto">
                <button @click="goToNotificationCenter"
                  class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-md text-sm font-medium transition-colors duration-200 flex items-center justify-center">
                  <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  查看全部消息
                </button>
              </div>
            </div>
          </div>

          <div v-if="!isLoggedIn" class="relative">
            <button @click="handleUserAction"
              class="ml-4 px-4 py-2 bg-white text-blue-700 hover:bg-blue-50 rounded-md text-sm font-medium shadow-sm transition-colors duration-200">
              登录/注册
            </button>
          </div>

          <!-- 已登录状态显示用户名和下拉菜单 -->
          <div v-else ref="dropdownRef" class="relative">
            <button @click="toggleDropdown"
              class="flex items-center ml-4 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium transition-colors duration-200">
              <div class="w-6 h-6 rounded-full bg-blue-400 text-white flex items-center justify-center mr-2">
                {{ username.charAt(0).toUpperCase() }}
              </div>
              <span class="mr-1">{{ username }}</span>
              <svg :class="showDropdown ? 'transform rotate-180' : ''" class="w-4 h-4 transition-transform"
                fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>

            <!-- 下拉菜单 - 简化版 -->
            <div v-if="showDropdown" 
                class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-solid py-1 z-50 border border-gray-200">
              
              <!-- 菜单项 - 简化版 -->
              <router-link to="/profile" @click="closeDropdown"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                个人中心
              </router-link>
              
              <router-link to="/favorites" @click="closeDropdown"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                我的收藏
              </router-link>
              
              <router-link to="/notifications" @click="closeDropdown"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center">
                <span>消息中心</span>
                <span v-if="unreadCount > 0" 
                      class="ml-auto bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {{ unreadCount > 9 ? '9+' : unreadCount }}
                </span>
              </router-link>
              
              <router-link to="/settings" @click="closeDropdown"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                账号设置
              </router-link>
              
              <div class="border-t border-gray-100 my-1"></div>
              
              <button @click="logout"
                class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
/* 添加过渡效果 */
.router-link-active {
  font-weight: 600;
}

/* 确保下拉菜单显示在最顶层 */
.z-50 {
  z-index: 50 !important;
}

/* 加固的阴影，无透明效果 */
.shadow-solid {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
}

/* 确保菜单项背景不透明 */
.hover\:bg-gray-100:hover {
  background-color: #f3f4f6;
}

/* 通知面板最大高度 */
.max-h-96 {
  max-height: 24rem;
}

/* 通知面板的滚动条美化 */
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: rgba(156, 163, 175, 0.5) transparent;
}

.overflow-y-auto::-webkit-scrollbar {
  width: 5px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.5);
  border-radius: 3px;
}

/* 可爱的通知徽章样式 */
.notification-badge {
  animation: pulsate 1.5s ease-out infinite;
  box-shadow: 0 0 0 rgba(239, 68, 68, 0.6);
}

@keyframes pulsate {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
  }
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 6px rgba(239, 68, 68, 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
  }
}

/* 为未读通知添加的脉冲动画 */
.pulse-animation {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(0.95);
    opacity: 0.7;
  }
}

/* 通知面板入场动画 */
.notification-panel {
  animation: slideIn 0.2s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 通知项目悬停效果 */
.notification-item {
  cursor: pointer;
  position: relative;
}

.notification-item:hover .text-red-500 {
  opacity: 1;
}

.notification-item:hover .text-blue-500 {
  opacity: 1;
}

/* 按钮初始状态半透明 */
.notification-item .text-red-500,
.notification-item .text-blue-500 {
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

/* 阴影发光效果 */
.shadow-glow {
  box-shadow: 0 0 5px rgba(239, 68, 68, 0.5);
}

/* 最小宽度确保1-2位数字都能正常显示 */
.min-w-5 {
  min-width: 1.25rem;
}
</style>