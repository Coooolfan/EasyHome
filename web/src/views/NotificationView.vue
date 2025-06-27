<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

// 通知相关
interface Notification {
  id: number
  userId: number
  title: string
  content: string
  isRead: boolean
  createdAt: string
}

// 定义标签类型
type TabType = 'all' | 'unread' | 'read';

// 页面状态
const notifications = ref<Notification[]>([])
const loadingNotifications = ref(true)
const error = ref<string | null>(null)
const activeTab = ref<TabType>('all')
const showDeleteModal = ref(false)
const notificationToDelete = ref<number | null>(null)

// 计算属性：过滤通知
const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  } else if (activeTab.value === 'unread') {
    return notifications.value.filter(n => !n.isRead)
  } else {
    return notifications.value.filter(n => n.isRead)
  }
})

// 计算属性：统计信息
const notificationCounts = computed(() => {
  const total = notifications.value.length
  const unread = notifications.value.filter(n => !n.isRead).length
  const read = total - unread
  
  return { total, unread, read }
})

// 获取所有通知
const fetchAllNotifications = async () => {
  try {
    loadingNotifications.value = true
    error.value = null
    
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('用户未登录')
    }
    
    const response = await axios.get('/api/notification', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      notifications.value = response.data.data.sort((a: Notification, b: Notification) => {
        // 按时间倒序排列，最新的在前面
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })
    } else {
      throw new Error('获取通知失败')
    }
  } catch (err) {
    console.error('Failed to fetch notifications:', err)
    error.value = err instanceof Error ? err.message : '获取通知失败，请稍后再试'
  } finally {
    loadingNotifications.value = false
  }
}

// 标记通知为已读
const markAsRead = async (notificationId: number) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    
    await axios.put('/api/notification', null, {
      headers: { 'Authorization': `Bearer ${token}` },
      params: { id: notificationId }
    })
    
    // 更新本地状态
    const index = notifications.value.findIndex(n => n.id === notificationId)
    if (index !== -1) {
      notifications.value[index].isRead = true
    }
  } catch (error) {
    console.error('Failed to mark notification as read:', error)
  }
}

// 标记所有通知为已读
const markAllAsRead = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    
    await axios.put('/api/notification/all', null, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    // 更新所有本地通知状态
    notifications.value = notifications.value.map(n => ({ ...n, isRead: true }))
  } catch (error) {
    console.error('Failed to mark all notifications as read:', error)
  }
}

// 删除通知确认
const confirmDelete = (notificationId: number) => {
  notificationToDelete.value = notificationId
  showDeleteModal.value = true
}

// 删除通知
const deleteNotification = async () => {
  if (!notificationToDelete.value) return
  
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    
    await axios.delete('/api/notification', {
      headers: { 'Authorization': `Bearer ${token}` },
      params: { id: notificationToDelete.value }
    })
    
    // 从列表中移除
    notifications.value = notifications.value.filter(n => n.id !== notificationToDelete.value)
    
    // 关闭模态框
    showDeleteModal.value = false
    notificationToDelete.value = null
  } catch (error) {
    console.error('Failed to delete notification:', error)
  }
}

// 取消删除
const cancelDelete = () => {
  showDeleteModal.value = false
  notificationToDelete.value = null
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  
  // 判断是今天、昨天，还是其他日期
  const isToday = date.toDateString() === now.toDateString()
  const isYesterday = date.toDateString() === yesterday.toDateString()
  
  // 格式化时间部分
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const timeFormat = `${hours}:${minutes}`
  
  if (isToday) {
    return `今天 ${timeFormat}`
  } else if (isYesterday) {
    return `昨天 ${timeFormat}`
  } else {
    // 格式化为 MM月DD日 HH:MM
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日 ${timeFormat}`
  }
}

// 切换标签
const setActiveTab = (tab: TabType) => {
  activeTab.value = tab
}

// 初始化
onMounted(() => {
  fetchAllNotifications()
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold text-gray-900">消息中心</h1>
        <button 
          v-if="notificationCounts.unread > 0"
          @click="markAllAsRead" 
          class="bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-md text-sm font-medium transition-colors duration-200">
          全部标为已读
        </button>
      </div>
      
      <!-- 标签页切换 -->
      <div class="bg-white rounded-lg shadow mb-6 overflow-hidden">
        <div class="border-b border-gray-200">
          <nav class="flex -mb-px">
            <button 
              @click="setActiveTab('all')" 
              class="flex-1 px-4 py-3 text-center text-sm font-medium transition-colors duration-200 border-b-2"
              :class="activeTab === 'all' ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'">
              全部消息 
              <span class="ml-1 bg-gray-200 text-gray-700 px-1.5 py-0.5 rounded-full text-xs">
                {{ notificationCounts.total }}
              </span>
            </button>
            
            <button 
              @click="setActiveTab('unread')" 
              class="flex-1 px-4 py-3 text-center text-sm font-medium transition-colors duration-200 border-b-2"
              :class="activeTab === 'unread' ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'">
              未读消息
              <span 
                class="ml-1 px-1.5 py-0.5 rounded-full text-xs"
                :class="notificationCounts.unread > 0 ? 'bg-red-100 text-red-600' : 'bg-gray-200 text-gray-700'">
                {{ notificationCounts.unread }}
              </span>
            </button>
            
            <button 
              @click="setActiveTab('read')" 
              class="flex-1 px-4 py-3 text-center text-sm font-medium transition-colors duration-200 border-b-2"
              :class="activeTab === 'read' ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'">
              已读消息
              <span class="ml-1 bg-gray-200 text-gray-700 px-1.5 py-0.5 rounded-full text-xs">
                {{ notificationCounts.read }}
              </span>
            </button>
          </nav>
        </div>
        
        <!-- 消息内容区域 -->
        <div class="bg-white pb-2">
          <!-- 加载中状态 -->
          <div v-if="loadingNotifications" class="py-10 text-center">
            <div class="animate-spin rounded-full h-10 w-10 border-4 border-blue-200 border-t-blue-600 mx-auto mb-4"></div>
            <p class="text-gray-500">正在加载消息...</p>
          </div>
          
          <!-- 错误状态 -->
          <div v-else-if="error" class="py-10 text-center">
            <div class="bg-red-50 rounded-lg p-4 mx-auto max-w-md">
              <div class="flex">
                <div class="flex-shrink-0">
                  <svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">加载消息时出错</h3>
                  <div class="mt-2 text-sm text-red-700">
                    <p>{{ error }}</p>
                  </div>
                  <div class="mt-4">
                    <button 
                      @click="fetchAllNotifications" 
                      class="bg-red-50 text-red-800 hover:bg-red-100 px-3 py-1.5 rounded-md text-sm font-medium transition-colors duration-200">
                      重新加载
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 空消息状态 -->
          <div v-else-if="filteredNotifications.length === 0" class="py-12 text-center">
            <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p class="text-gray-500 text-lg">
              {{ activeTab === 'all' ? '暂无任何消息' : (activeTab === 'unread' ? '暂无未读消息' : '暂无已读消息') }}
            </p>
          </div>
          
          <!-- 消息列表 -->
          <div v-else class="divide-y divide-gray-200">
            <div 
              v-for="notification in filteredNotifications" 
              :key="notification.id" 
              :class="{'bg-blue-50': !notification.isRead && activeTab !== 'read'}"
              class="hover:bg-gray-50 transition-colors duration-200 py-4 px-4 sm:px-6 group">
              
              <!-- 消息头部：标题、日期、操作按钮 -->
              <div class="flex justify-between items-start">
                <div class="flex items-center space-x-3">
                  <!-- 未读标记 -->
                  <div v-if="!notification.isRead" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                  
                  <h3 class="text-base font-semibold text-gray-900 group-hover:text-blue-600 transition-colors duration-200">
                    {{ notification.title }}
                  </h3>
                </div>
                
                <div class="flex items-center">
                  <span class="text-sm text-gray-500 mr-3">{{ formatDate(notification.createdAt) }}</span>
                  
                  <!-- 操作按钮 -->
                  <div class="flex space-x-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                    <!-- 标记已读按钮 (仅对未读消息显示) -->
                    <button 
                      v-if="!notification.isRead"
                      @click="markAsRead(notification.id)" 
                      class="text-blue-600 hover:text-blue-800 transition-colors p-1 hover:bg-blue-50 rounded-full"
                      title="标记为已读">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                      </svg>
                    </button>
                    
                    <!-- 删除按钮 -->
                    <button 
                      @click="confirmDelete(notification.id)" 
                      class="text-red-500 hover:text-red-700 transition-colors p-1 hover:bg-red-50 rounded-full"
                      title="删除此消息">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- 消息内容 -->
              <p class="mt-2 text-sm text-gray-600">{{ notification.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 删除确认模态框 -->
  <div v-if="showDeleteModal" class="fixed inset-0 flex items-center justify-center z-50">
    <!-- 背景遮罩 -->
    <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"></div>
    
    <!-- 模态框内容 -->
    <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 z-10 overflow-hidden transform transition-all">
      <div class="px-6 py-5">
        <h3 class="text-lg font-medium leading-6 text-gray-900 mb-3">确认删除</h3>
        <p class="text-sm text-gray-500">
          您确定要删除这条消息吗？此操作无法撤销。
        </p>
      </div>
      <div class="bg-gray-50 px-6 py-3 flex justify-end space-x-3">
        <button 
          @click="cancelDelete" 
          class="bg-white border border-gray-300 rounded-md shadow-sm py-2 px-4 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none">
          取消
        </button>
        <button 
          @click="deleteNotification" 
          class="bg-red-600 border border-transparent rounded-md shadow-sm py-2 px-4 text-sm font-medium text-white hover:bg-red-700 focus:outline-none">
          删除
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 鼠标悬停效果 */
.group:hover .text-gray-900 {
  color: #2563eb;
}

/* 更好的滚动条样式 */
::-webkit-scrollbar {
  width: 5px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.5);
  border-radius: 3px;
}
</style>