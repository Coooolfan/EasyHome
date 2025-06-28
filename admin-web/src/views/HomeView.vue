<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <!-- 左侧 Logo -->
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><House /></el-icon>
          <span class="logo-text">Eazy Home</span>
        </div>
      </div>
      
      <!-- 右侧操作区 -->
      <div class="header-right">
        <div class="header-actions">
          <!-- 搜索 -->
          <el-tooltip content="搜索" placement="bottom">
            <el-button class="action-btn" circle>
              <el-icon><Search /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 通知 -->
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <el-button class="action-btn" circle @click="showNotifications">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </el-tooltip>
          
          <!-- 设置 -->
          <el-tooltip content="系统设置" placement="bottom">
            <el-button class="action-btn" circle @click="handleSystemSettings">
              <el-icon><Setting /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 用户头像 - 修改为首字母头像 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar">
              <el-avatar :size="35" class="header-user-avatar">
                {{ getAvatarText(userInfo.username) }}
              </el-avatar>
              <span class="username">{{ userInfo.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>账户设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 其余代码保持不变 -->
    <el-container class="main-container">
      <!-- 左侧导航栏 -->
      <el-aside width="260px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          :collapse="false"
        >
          <!-- 数据看板 -->
          <el-menu-item index="/dashboard" class="menu-item">
            <el-icon class="menu-icon"><DataAnalysis /></el-icon>
            <span class="menu-title">数据看板</span>
          </el-menu-item>

          <!-- 功能管理 -->
          <el-sub-menu index="function"  class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><Setting /></el-icon>
              <span class="menu-title">功能管理</span>
            </template>
            
            <el-menu-item index="/user-manage" class="sub-menu-item">
              <el-icon class="sub-menu-icon"><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 业务管理 -->
          <el-sub-menu index="business" class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><House /></el-icon>
              <span class="menu-title">业务管理</span>
            </template>
            
            <el-menu-item index="/house-manage" class="sub-menu-item">
              <el-icon class="sub-menu-icon"><OfficeBuilding /></el-icon>
              <span>房源管理</span>
            </el-menu-item>
            
            <!-- 卖房管理菜单 -->
            <el-menu-item index="/sell-house-manage" class="sub-menu-item">
              <el-icon class="sub-menu-icon"><DocumentChecked /></el-icon>
              <span>卖房审核</span>
            </el-menu-item>

            <!--  新增预约管理菜单 -->
            <el-menu-item index="/reservation-manage" class="sub-menu-item">
              <el-icon class="sub-menu-icon"><Calendar /></el-icon>
              <span>预约管理</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主体内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 通知模态框 -->
    <el-dialog
      v-model="notificationVisible"
      title="消息通知"
      width="600px"
      class="notification-dialog"
      :before-close="() => notificationVisible = false"
    >
      <div class="notification-header">
        <div class="notification-stats">
          <span class="total-count">共 {{ notifications.length }} 条通知</span>
          <span class="unread-count" v-if="unreadCount > 0">{{ unreadCount }} 条未读</span>
        </div>
        <div class="notification-actions">
          <el-button text type="primary" @click="markAllAsRead" v-if="unreadCount > 0">
            全部已读
          </el-button>
          <el-button text type="danger" @click="clearAllNotifications">
            清空通知
          </el-button>
        </div>
      </div>

      <div class="notification-list" v-if="notifications.length > 0">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          :class="{ 'unread': !notification.read }"
          @click="markAsRead(notification.id)"
        >
          <div class="notification-icon" :style="{ color: getNotificationColor(notification.type) }">
            <el-icon><component :is="getNotificationIcon(notification.type)" /></el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">
              {{ notification.title }}
              <el-badge is-dot v-if="!notification.read" class="unread-dot" />
            </div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-time">{{ notification.time }}</div>
          </div>
          <div class="notification-controls">
            <el-button 
              text 
              type="danger" 
              size="small"
              @click.stop="deleteNotification(notification.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="notification-empty" v-else>
        <el-empty description="暂无通知" :image-size="120">
          <template #image>
            <el-icon class="empty-icon"><Bell /></el-icon>
          </template>
        </el-empty>
      </div>

      <template #footer>
        <div class="notification-footer">
          <el-button @click="notificationVisible = false">关闭</el-button>
          <el-button type="primary" @click="notificationVisible = false">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, User, ArrowDown, UserFilled, OfficeBuilding, 
  DocumentChecked, Calendar, Bell, Search, Setting, SwitchButton,
  DataAnalysis
} from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isSuperAdmin = computed(() => userStore.userInfo.role === 'role_admin')
const canAccessFunction = computed(() => userStore.userInfo.role === 'role_admin' || userStore.userInfo.role === 'admin')

const userInfo = ref({
  username: userStore.userInfo.username || '管理员',
  avatar: ''
})

// 🔧 添加获取头像显示文字的函数（与个人中心保持一致）
const getAvatarText = (name: string) => {
  if (!name) return 'A'
  // 如果是中文，取第一个字符
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(0)
  }
  // 如果是英文，取第一个字母的大写
  return name.charAt(0).toUpperCase()
}

const handleMenuSelect = (index: string) => {
  router.push(index)
}

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile') // 跳转到个人中心
      break
    case 'settings':
      router.push('/settings') // 跳转到系统设置
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 调用后端登出接口
        const token = localStorage.getItem('admin_token')
        if (token) {
          try {
            await axios.post('/api/admin/logout', null, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            })
          } catch (error) {
            console.error('调用登出接口出错', error)
            // 即使API调用失败，也继续前端登出流程
          }
        }
        
        // 前端清理登录状态
        localStorage.removeItem('admin_token')
        await userStore.logout()
        
        router.push('/login')
        ElMessage.success('退出登录成功')
      } catch {
        // 用户取消
      }
      break
  }
}

// 添加系统设置按钮点击事件
const handleSystemSettings = () => {
  router.push('/settings')
}

// 通知相关数据
interface NotificationItem {
  id: number
  title: string
  content: string
  type: 'info' | 'success' | 'warning' | 'error'
  time: string
  read: boolean
  avatar?: string
}

const notificationVisible = ref(false)
const notifications = ref<NotificationItem[]>([])

// 未读通知数量
const unreadCount = computed(() => {
  return notifications.value.filter(item => !item.read).length
})

// 显示通知面板
const showNotifications = () => {
  notificationVisible.value = true
}

// 标记通知为已读
const markAsRead = (id: number) => {
  const notification = notifications.value.find(item => item.id === id)
  if (notification && !notification.read) {
    notification.read = true
  }
}

// 标记所有通知为已读
const markAllAsRead = () => {
  notifications.value.forEach(item => {
    item.read = true
  })
}

// 删除通知
const deleteNotification = (id: number) => {
  const index = notifications.value.findIndex(item => item.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
  }
}

// 清空所有通知
const clearAllNotifications = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    notifications.value = []
    ElMessage.success('已清空所有通知')
  } catch {
    // 用户取消
  }
}

// 获取通知类型图标
const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'success': return 'SuccessFilled'
    case 'warning': return 'WarningFilled'
    case 'error': return 'CircleCloseFilled'
    default: return 'InfoFilled'
  }
}

// 获取通知类型颜色
const getNotificationColor = (type: string) => {
  switch (type) {
    case 'success': return '#67C23A'
    case 'warning': return '#E6A23C'
    case 'error': return '#F56C6C'
    default: return '#409EFF'
  }
}

// 加载通知数据
const loadNotifications = () => {
  // 模拟通知数据
  notifications.value = [
    {
      id: 1,
      title: '新用户注册',
      content: '用户"张三"刚刚注册了账号，请及时审核。',
      type: 'info',
      time: '2分钟前',
      read: false,
      avatar: ''
    },
    {
      id: 2,
      title: '房源审核通过',
      content: '房源"阳光花园三室两厅"已通过审核，现已上线。',
      type: 'success',
      time: '5分钟前',
      read: false,
      avatar: ''
    },
    {
      id: 3,
      title: '系统维护通知',
      content: '系统将于今晚22:00-23:00进行维护升级，请提前做好准备。',
      type: 'warning',
      time: '10分钟前',
      read: true,
      avatar: ''
    },
    {
      id: 4,
      title: '房源举报',
      content: '房源"市中心商圈住宅"收到用户举报，请及时处理。',
      type: 'error',
      time: '1小时前',
      read: false,
      avatar: ''
    },
    {
      id: 5,
      title: '数据备份完成',
      content: '今日数据备份已完成，备份文件已保存至云端。',
      type: 'success',
      time: '2小时前',
      read: true,
      avatar: ''
    }
  ]
}

onMounted(() => {
  // 默认跳转到数据看板
  if (route.path === '/') {
    router.push('/dashboard')
  }
  // 加载通知数据
  loadNotifications()
})
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.logo-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 8px;
}

.logo-text {
  background: linear-gradient(135deg, #409EFF 0%, #36CFC9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f7fa;
  color: #606266;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #409EFF;
  color: white;
  transform: translateY(-1px);
}

.notification-badge {
  margin-right: 8px;
}

.user-avatar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: 8px;
}

.user-avatar:hover {
  background: #f5f7fa;
}

/* 🔧 顶部导航栏头像样式 - 与个人中心保持一致 */
.header-user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #fff !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s ease;
}

/* 主容器 */
.main-container {
  flex: 1;
  height: calc(100vh - 60px);
}

/* 左侧导航栏 */
.sidebar {
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.05);
}

.sidebar-menu {
  border: none;
  padding: 20px 0;
  height: 100%;
}

/* 一级菜单项 */
.menu-item {
  height: 50px;
  margin: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  padding-left: 20px !important;
}

.menu-item:hover {
  background: #f0f9ff !important;
  color: #409EFF !important;
}

.menu-item.is-active {
  background: linear-gradient(135deg, #409EFF 0%, #409EFF 100%) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 子菜单容器 */
.sub-menu {
  margin: 8px 16px;
  border-radius: 8px;
  overflow: hidden;
}

/* 子菜单项 */
.sub-menu-item {
  height: 44px;
  margin: 0 !important;
  border-radius: 6px;
  padding-left: 60px !important; /* 增加左边距，形成层级关系 */
  transition: all 0.3s ease;
}

.sub-menu-item:hover {
  background: #f0f9ff !important;
  color: #409EFF !important;
}

.sub-menu-item.is-active {
  background: #409EFF !important;
  color: white !important;
  position: relative;
}

/* 子菜单激活状态的左边线条 */
.sub-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: white;
  border-radius: 2px;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  min-width: 18px;
}

.sub-menu-icon {
  font-size: 16px;
  margin-right: 12px;
  min-width: 16px;
}

.menu-title {
  font-weight: 500;
}

/* 主体内容 */
.main-content {
  background: #f8fafc;
  padding: 24px;
  overflow-y: auto;
}

/* Element Plus 样式覆盖 */
:deep(.el-sub-menu .el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  padding-left: 20px !important;
  border-radius: 8px;
  margin: 0;
  transition: all 0.3s ease;
}

:deep(.el-sub-menu .el-sub-menu__title:hover) {
  background: #f0f9ff !important;
  color: #409EFF !important;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #409EFF !important;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.el-menu--vertical .el-menu-item) {
  padding-left: 20px !important;
}

:deep(.el-badge__content) {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 16px;
}

/* 菜单展开/收起动画 */
:deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  transition: transform 0.3s ease;
  margin-right: 8px;
}

:deep(.el-sub-menu.is-opened .el-sub-menu__icon-arrow) {
  transform: rotateZ(180deg);
}

/* 通知对话框样式 */
:deep(.notification-dialog) {
  border-radius: 12px;
}

:deep(.notification-dialog .el-dialog__header) {
  padding: 20px 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.notification-dialog .el-dialog__body) {
  padding: 0;
  max-height: 500px;
  overflow: hidden;
}

:deep(.notification-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f5;
  background: #fafafa;
}

.notification-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-count {
  font-size: 14px;
  color: #606266;
}

.unread-count {
  font-size: 12px;
  color: #E6A23C;
  background: #FDF6EC;
  padding: 2px 8px;
  border-radius: 10px;
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 24px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.notification-item:hover {
  background: #f8fafc;
}

.notification-item.unread {
  background: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.notification-item.unread:hover {
  background: #e6f4ff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin-bottom: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-controls {
  opacity: 0;
  transition: opacity 0.3s ease;
}

.notification-item:hover .notification-controls {
  opacity: 1;
}

.unread-dot {
  margin-left: auto;
}

.notification-empty {
  padding: 40px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #ddd;
  margin-bottom: 16px;
}

.notification-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 滚动条样式 */
.notification-list::-webkit-scrollbar {
  width: 6px;
}

.notification-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.notification-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.menu-badge {
  position: absolute;
  top: 8px;
  right: 12px;
  z-index: 1;
}

.sub-menu-item {
  position: relative;
}
</style>
