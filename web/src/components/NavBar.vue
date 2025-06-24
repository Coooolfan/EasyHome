<script setup lang="ts">
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const currentPath = computed(() => route.path)

// 用户状态
const isLoggedIn = ref(false)
const username = ref('')

// 从本地存储加载用户信息
const loadUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsedInfo = JSON.parse(userInfo)
      if (parsedInfo && parsedInfo.username) {
        isLoggedIn.value = true
        username.value = parsedInfo.username
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

// 初始化加载
onMounted(() => {
  loadUserInfo()
  
  // 添加全局点击事件监听，用于关闭下拉菜单
  document.addEventListener('click', handleOutsideClick)
})

// 在组件卸载时清除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})

// 监听路由变化
watch(() => route.path, () => {
  showDropdown.value = false
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
  
  // 如果当前在需要登录的页面，退出后重定向到首页
  if (currentPath.value === '/view' || currentPath.value === '/sell' || 
      currentPath.value === '/profile' || currentPath.value === '/settings' || 
      currentPath.value === '/favorites') {
    router.push('/')
  }
}

// 控制下拉菜单
const showDropdown = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const toggleDropdown = (event: Event) => {
  event.stopPropagation()
  showDropdown.value = !showDropdown.value
}

const closeDropdown = () => {
  showDropdown.value = false
}

const handleOutsideClick = (event: Event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    showDropdown.value = false
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
</style>