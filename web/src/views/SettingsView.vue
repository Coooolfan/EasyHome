<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

// 用户信息类型定义
interface UserProfile {
  id: number
  username: string
  email: string
  phone: string
  createdAt?: string
  updatedAt?: string
}

// 表单数据
const userProfile = ref<UserProfile>({
  id: 0,
  username: '',
  email: '',
  phone: ''
})

// 编辑状态控制
const isEditing = ref(false)
const originalProfile = ref<UserProfile | null>(null)

// 密码修改表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 显示/隐藏密码
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// 状态管理
const loading = ref(false)
const profileLoading = ref(true)
const successMsg = ref('')
const errorMsg = ref('')
const passwordSuccessMsg = ref('')
const passwordErrorMsg = ref('')
const activeTab = ref('profile') // profile, security

// 加载用户资料
const loadUserProfile = async () => {
  profileLoading.value = true
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      errorMsg.value = '请先登录'
      return
    }

    // 使用正确的API端点获取用户信息
    const response = await axios.get('/api/user/getUserInfo', {
      headers: { 'Authorization': `Bearer ${token}` }
    })

    if (response.data && response.data.code === 'SUCCESS') {
      userProfile.value = response.data.data
      // 保存原始数据，用于取消编辑时恢复
      originalProfile.value = { ...response.data.data }
    } else {
      throw new Error(response.data?.message || '获取用户信息失败')
    }
  } catch (err: any) {
    console.error('获取用户信息失败:', err)
    errorMsg.value = err.message || '获取用户信息失败，请稍后重试'
  } finally {
    profileLoading.value = false
  }
}

// 开始编辑
const startEditing = () => {
  // 保存原始数据用于取消时恢复
  originalProfile.value = { ...userProfile.value }
  isEditing.value = true
  successMsg.value = ''
  errorMsg.value = ''
}

// 取消编辑
const cancelEditing = () => {
  // 恢复原始数据
  if (originalProfile.value) {
    userProfile.value = { ...originalProfile.value }
  }
  isEditing.value = false
  successMsg.value = ''
  errorMsg.value = ''
}

// 更新用户资料
const updateProfile = async () => {
  try {
    loading.value = true
    errorMsg.value = ''
    successMsg.value = ''

    const token = localStorage.getItem('token')
    if (!token) {
      errorMsg.value = '请先登录'
      return
    }

    // 验证手机号
    if (userProfile.value.phone) {
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(userProfile.value.phone)) {
        errorMsg.value = '请输入有效的手机号码'
        return
      }
    }

    // 验证邮箱
    if (userProfile.value.email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(userProfile.value.email)) {
        errorMsg.value = '请输入有效的邮箱地址'
        return
      }
    }

    // 使用正确的更新接口
    const response = await axios.put('/api/user/updateProfile', {
      username: userProfile.value.username,
      email: userProfile.value.email,
      phone: userProfile.value.phone
    }, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.data && response.data.code === 'SUCCESS') {
      successMsg.value = '个人资料已更新'
      // 更新成功后，退出编辑模式
      isEditing.value = false
      // 更新原始数据
      originalProfile.value = { ...userProfile.value }
    } else {
      throw new Error(response.data?.message || '更新个人资料失败')
    }
  } catch (err: any) {
    console.error('更新个人资料失败:', err)
    errorMsg.value = err.message || '更新个人资料失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 更新密码
const updatePassword = async () => {
  try {
    loading.value = true
    passwordErrorMsg.value = ''
    passwordSuccessMsg.value = ''

    const token = localStorage.getItem('token')
    if (!token) {
      passwordErrorMsg.value = '请先登录'
      return
    }

    // 验证密码
    if (!passwordForm.value.currentPassword) {
      passwordErrorMsg.value = '请输入当前密码'
      return
    }
    if (!passwordForm.value.newPassword) {
      passwordErrorMsg.value = '请输入新密码'
      return
    }
    if (passwordForm.value.newPassword.length < 6) {
      passwordErrorMsg.value = '新密码长度不能少于6位'
      return
    }
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      passwordErrorMsg.value = '两次输入的密码不一致'
      return
    }

    // 使用URL参数而不是请求体，因为后端使用了@RequestParam注解
    const response = await axios.put(`/api/user/updatePassword?oldPassword=${encodeURIComponent(passwordForm.value.currentPassword)}&newPassword=${encodeURIComponent(passwordForm.value.newPassword)}`, null, {
      headers: { 
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data && response.data.code === 'SUCCESS') {
      passwordSuccessMsg.value = '密码已成功修改'
      // 清空密码表单
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      throw new Error(response.data?.message || '修改密码失败')
    }
  } catch (err: any) {
    console.error('修改密码失败:', err)
    // 改进错误处理，显示后端返回的具体错误信息
    passwordErrorMsg.value = err.response?.data?.message || err.message || '修改密码失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 切换密码可见性
const togglePasswordVisibility = (field: string) => {
  switch (field) {
    case 'current':
      showCurrentPassword.value = !showCurrentPassword.value
      break
    case 'new':
      showNewPassword.value = !showNewPassword.value
      break
    case 'confirm':
      showConfirmPassword.value = !showConfirmPassword.value
      break
  }
}

// 确认删除账号
const confirmDeleteAccount = () => {
  if (confirm('您确定要删除账号吗？此操作不可逆，您的所有数据将被永久删除。')) {
    deleteAccount()
  }
}

// 删除账号
const deleteAccount = async () => {
  try {
    loading.value = true
    errorMsg.value = ''

    const token = localStorage.getItem('token')
    if (!token) {
      errorMsg.value = '请先登录'
      return
    }

    const response = await axios.delete('/api/user/delete', {
      headers: { 'Authorization': `Bearer ${token}` }
    })

    if (response.data && response.data.code === 'SUCCESS') {
      // 清除所有用户相关的本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      // 显示成功消息
      alert('您的账号已成功删除')
      
      // 跳转到首页
      window.location.href = '/'
    } else {
      throw new Error(response.data?.message || '删除账号失败')
    }
  } catch (err: any) {
    console.error('删除账号失败:', err)
    errorMsg.value = err.response?.data?.message || err.message || '删除账号失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 页面加载时获取用户信息
onMounted(() => {
  loadUserProfile()
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">账号设置</h1>
        <p class="mt-1 text-sm text-gray-500">管理您的账号信息和安全设置</p>
      </div>

      <!-- 页签导航 -->
      <div class="mb-6 border-b border-gray-200">
        <nav class="flex -mb-px space-x-8">
          <button 
            @click="activeTab = 'profile'"
            :class="[
              activeTab === 'profile'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-base focus:outline-none'
            ]">
            个人资料
          </button>
          <button
            @click="activeTab = 'security'"
            :class="[
              activeTab === 'security'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-base focus:outline-none'
            ]">
            安全设置
          </button>
        </nav>
      </div>

      <!-- 加载中状态 -->
      <div v-if="profileLoading" class="bg-white shadow-md rounded-lg p-8 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
        <p class="mt-4 text-gray-500">正在加载用户信息...</p>
      </div>
      
      <!-- 内容区域 -->
      <div v-else>
        <!-- 个人资料标签页 -->
        <div v-if="activeTab === 'profile'" class="space-y-6">
          <!-- 个人资料表单 -->
          <div class="bg-white shadow-md rounded-lg overflow-hidden">
            <div class="px-6 py-6 sm:p-8">
              <!-- 标题和编辑按钮 -->
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-medium leading-6 text-gray-900">基本信息</h3>
                
                <div v-if="!isEditing">
                  <button 
                    @click="startEditing" 
                    class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200"
                  >
                    <svg class="-ml-1 mr-2 h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                    </svg>
                    修改资料
                  </button>
                </div>
              </div>
              
              <!-- 成功/错误提示 -->
              <div v-if="successMsg" class="mb-6 bg-green-50 border-l-4 border-green-400 p-4 rounded">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-green-700">{{ successMsg }}</p>
                  </div>
                </div>
              </div>

              <div v-if="errorMsg" class="mb-6 bg-red-50 border-l-4 border-red-400 p-4 rounded">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-red-700">{{ errorMsg }}</p>
                  </div>
                </div>
              </div>

              <div class="space-y-6">
                <!-- 用户名 -->
                <div>
                  <label for="username" class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
                  <div class="mt-1">
                    <div v-if="isEditing">
                      <input 
                        id="username" 
                        v-model="userProfile.username" 
                        type="text" 
                        class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      />
                    </div>
                    <div v-else class="bg-gray-50 px-4 py-3 rounded border border-gray-200">
                      {{ userProfile.username || '未设置' }}
                    </div>
                  </div>
                </div>

                <!-- 电子邮箱 -->
                <div>
                  <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                    电子邮箱
                    <span class="ml-1 text-xs text-gray-400">（用于接收通知和密码重置）</span>
                  </label>
                  <div class="mt-1">
                    <div v-if="isEditing">
                      <input 
                        id="email" 
                        v-model="userProfile.email" 
                        type="email" 
                        class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        placeholder="example@email.com"
                      />
                    </div>
                    <div v-else class="bg-gray-50 px-4 py-3 rounded border border-gray-200">
                      {{ userProfile.email || '未设置' }}
                    </div>
                  </div>
                </div>

                <!-- 手机号码 -->
                <div>
                  <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
                  <div class="mt-1">
                    <div v-if="isEditing">
                      <input 
                        id="phone" 
                        v-model="userProfile.phone" 
                        type="tel" 
                        class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        placeholder="请输入11位手机号"
                      />
                    </div>
                    <div v-else class="bg-gray-50 px-4 py-3 rounded border border-gray-200">
                      {{ userProfile.phone || '未设置' }}
                    </div>
                  </div>
                </div>

                <!-- 注册和更新时间 -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-if="userProfile.createdAt">
                    <label class="block text-sm font-medium text-gray-700 mb-1">注册时间</label>
                    <div class="mt-1 text-sm text-gray-500 bg-gray-50 px-4 py-3 rounded border border-gray-200">
                      {{ formatDate(userProfile.createdAt) }}
                    </div>
                  </div>
                  
                  <div v-if="userProfile.updatedAt">
                    <label class="block text-sm font-medium text-gray-700 mb-1">最后更新</label>
                    <div class="mt-1 text-sm text-gray-500 bg-gray-50 px-4 py-3 rounded border border-gray-200">
                      {{ formatDate(userProfile.updatedAt) }}
                    </div>
                  </div>
                </div>
                
                <!-- 编辑状态下的按钮组 -->
                <div v-if="isEditing" class="pt-4 flex space-x-4">
                  <button
                    @click="updateProfile"
                    :disabled="loading"
                    class="flex-1 inline-flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
                  >
                    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ loading ? '保存中...' : '保存修改' }}
                  </button>
                  
                  <button
                    @click="cancelEditing"
                    :disabled="loading"
                    class="flex-1 inline-flex justify-center items-center px-6 py-3 border border-gray-300 text-base font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
                  >
                    取消
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 安全设置标签页 -->
        <div v-if="activeTab === 'security'" class="space-y-6">
          <!-- 密码修改 -->
          <div class="bg-white shadow-md rounded-lg overflow-hidden">
            <div class="px-6 py-6 sm:p-8">
              <h3 class="text-xl font-medium leading-6 text-gray-900 mb-6">修改密码</h3>
              
              <!-- 成功/错误提示 -->
              <div v-if="passwordSuccessMsg" class="mb-6 bg-green-50 border-l-4 border-green-400 p-4 rounded">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-green-700">{{ passwordSuccessMsg }}</p>
                  </div>
                </div>
              </div>

              <div v-if="passwordErrorMsg" class="mb-6 bg-red-50 border-l-4 border-red-400 p-4 rounded">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-red-700">{{ passwordErrorMsg }}</p>
                  </div>
                </div>
              </div>

              <div class="space-y-6">
                <!-- 当前密码 -->
                <div>
                  <label for="currentPassword" class="block text-sm font-medium text-gray-700 mb-1">当前密码</label>
                  <div class="mt-1 relative">
                    <input
                      id="currentPassword"
                      v-model="passwordForm.currentPassword"
                      :type="showCurrentPassword ? 'text' : 'password'"
                      class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 pr-10"
                    />
                    <button
                      type="button"
                      @click="togglePasswordVisibility('current')"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                    >
                      <svg v-if="showCurrentPassword" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- 新密码 -->
                <div>
                  <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-1">新密码</label>
                  <div class="mt-1 relative">
                    <input
                      id="newPassword"
                      v-model="passwordForm.newPassword"
                      :type="showNewPassword ? 'text' : 'password'"
                      class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 pr-10"
                    />
                    <button
                      type="button"
                      @click="togglePasswordVisibility('new')"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                    >
                      <svg v-if="showNewPassword" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    </button>
                  </div>
                  <p class="mt-2 text-xs text-gray-500">密码长度至少为 6 位字符</p>
                </div>

                <!-- 确认新密码 -->
                <div>
                  <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">确认新密码</label>
                  <div class="mt-1 relative">
                    <input
                      id="confirmPassword"
                      v-model="passwordForm.confirmPassword"
                      :type="showConfirmPassword ? 'text' : 'password'"
                      class="block w-full px-4 py-3 text-base rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 pr-10"
                    />
                    <button
                      type="button"
                      @click="togglePasswordVisibility('confirm')"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                    >
                      <svg v-if="showConfirmPassword" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- 提交按钮 -->
                <div class="pt-4">
                  <button
                    @click="updatePassword"
                    :disabled="loading"
                    class="w-full inline-flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
                  >
                    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ loading ? '更新中...' : '更新密码' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 账号注销 -->
          <div class="bg-white shadow-md rounded-lg overflow-hidden">
            <div class="px-6 py-6 sm:p-8">
              <h3 class="text-xl font-medium leading-6 text-gray-900">删除账号</h3>
              <div class="mt-4 max-w-xl text-sm text-gray-500">
                <p>删除账号后，您的所有数据将被永久删除，此操作不可撤销。</p>
              </div>
              <div class="mt-6">
                <button
                  @click="confirmDeleteAccount"
                  type="button"
                  class="inline-flex items-center justify-center px-5 py-3 border border-transparent font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors duration-200"
                >
                  <svg class="mr-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  删除账号
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 表单元素统一样式 */
input, select, textarea {
  appearance: none;
}

/* 设置输入框高度和内边距 */
input[type="text"],
input[type="email"],
input[type="tel"],
input[type="password"],
textarea {
  min-height: 48px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式调整 */
@media (max-width: 640px) {
  input[type="text"],
  input[type="email"],
  input[type="tel"],
  input[type="password"],
  textarea {
    min-height: 44px;
    font-size: 16px; /* 防止iOS设备上输入框缩放 */
  }
}
</style>