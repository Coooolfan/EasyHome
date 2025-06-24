<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const currentPath = computed(() => route.path)

// 用户状态
const isLoggedIn = ref(false)
const username = ref('')

// 从本地存储加载用户信息
onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const parsedInfo = JSON.parse(userInfo)
      if (parsedInfo && parsedInfo.username) {
        isLoggedIn.value = true
        username.value = parsedInfo.username
      }
    } catch (e) {
      console.error('Failed to parse user info:', e)
    }
  }
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
  router.push('/')
}

// 控制下拉菜单
const showDropdown = ref(false)
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}
const closeDropdown = () => {
  showDropdown.value = false
}
</script>

<template>
  <nav class="bg-blue-700 shadow-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <!-- 左侧菜单 -->
        <div class="flex items-center">
          <div class="text-2xl font-bold text-white">EasyHome</div>
          <div class="ml-10 flex items-center space-x-4">
            <router-link to="/" class="px-3 py-2 rounded-md text-sm font-medium"
              :class="[currentPath === '/' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              找房
            </router-link>
            <router-link to="/search" class="px-3 py-2 rounded-md text-sm font-medium"
              :class="[currentPath === '/search' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              查房
            </router-link>
            <router-link to="/view" class="px-3 py-2 rounded-md text-sm font-medium"
              :class="[currentPath === '/view' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              看房
            </router-link>
            <router-link to="/sell" class="px-3 py-2 rounded-md text-sm font-medium"
              :class="[currentPath === '/sell' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
              卖房
            </router-link>
          </div>
        </div>

        <!-- 右侧用户区域 -->
        <div class="flex items-center">
          <div v-if="!isLoggedIn" class="relative">
            <button @click="handleUserAction"
              class="ml-4 px-4 py-2 bg-white text-blue-700 hover:bg-blue-50 rounded-md text-sm font-medium shadow-sm transition-colors">
              登录/注册
            </button>
          </div>

          <!-- 已登录状态显示用户名和下拉菜单 -->
          <div v-else class="relative" @click.away="closeDropdown">
            <button @click="toggleDropdown"
              class="flex items-center ml-4 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium transition-colors">
              <span class="mr-2">{{ username }}</span>
              <svg :class="showDropdown ? 'transform rotate-180' : ''" class="w-4 h-4 transition-transform"
                fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>

            <!-- 下拉菜单 -->
            <div v-if="showDropdown"
              class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10 border border-gray-200">
              <router-link to="/profile"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-blue-50">
                个人中心
              </router-link>
              <router-link to="/settings"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-blue-50">
                设置
              </router-link>
              <button @click="logout"
                class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50">
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>