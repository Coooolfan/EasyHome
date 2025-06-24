<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const isLogin = ref(true) // true = 登录模式, false = 注册模式
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const rememberMe = ref(false)

// 显示/隐藏密码
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '', // 仅注册时使用
  email: '' // 仅注册时使用
})

// 切换密码可见性
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 验证状态
const isUsernameValid = computed(() => {
  if (!form.username) return null
  return form.username.length >= 4
})

const isPasswordValid = computed(() => {
  if (!form.password) return null
  return form.password.length >= 6
})

const isEmailValid = computed(() => {
  if (!form.email) return null
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailReg.test(form.email)
})

const isPasswordMatch = computed(() => {
  if (!form.confirmPassword) return null
  return form.password === form.confirmPassword
})

// 验证规则
const validateForm = () => {
  if (!form.username) {
    errorMsg.value = '请输入用户名'
    return false
  }
  if (!form.password) {
    errorMsg.value = '请输入密码'
    return false
  }

  if (!isLogin.value) { // 注册验证
    if (form.password.length < 6) {
      errorMsg.value = '密码长度至少6位'
      return false
    }
    if (form.password !== form.confirmPassword) {
      errorMsg.value = '两次输入密码不一致'
      return false
    }
    if (!form.email) {
      errorMsg.value = '请输入邮箱'
      return false
    }
    const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailReg.test(form.email)) {
      errorMsg.value = '邮箱格式不正确'
      return false
    }
  }

  errorMsg.value = ''
  return true
}

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  errorMsg.value = ''
  successMsg.value = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) return

  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    if (isLogin.value) {
      // 登录请求
      const response = await axios.post('/api/user/login', {
        username: form.username,
        password: form.password
      })

      console.log('登录响应:', response.data) // 调试用

      // 新的响应格式检查
      if (response.data && response.data.code === "SUCCESS" && response.data.data) {
        // 从data字段获取token
        const token = response.data.data

        if (token) {
          // 保存用户信息和token
          const userInfo = {
            username: form.username,
          }
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          localStorage.setItem('token', token)

          // 设置axios的默认Authorization请求头
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

          successMsg.value = '登录成功，即将跳转...'

          // 登录成功后跳转
          setTimeout(() => {
            router.push('/')
          }, 1000)
        } else {
          console.error('返回的token为空:', response.data)
          errorMsg.value = '登录失败: 未能获取有效的授权信息'
        }
      } else {
        // 处理错误响应
        const message = response.data?.message || '登录失败，请检查用户名和密码'
        console.error('登录失败:', message)
        errorMsg.value = message
      }
    } else {
      // 注册请求
      const response = await axios.post('/api/user/register', {
        username: form.username,
        password: form.password,
        email: form.email
      })

      // 适配新的响应格式
      if (response.data && response.data.code === "SUCCESS") {
        // 注册成功，切换到登录模式
        isLogin.value = true
        successMsg.value = '注册成功，请登录'
        // 清空表单，保留用户名
        const username = form.username
        form.password = ''
        form.confirmPassword = ''
        form.email = ''
        form.username = username
      } else {
        errorMsg.value = response.data.message || '注册失败，请稍后再试'
      }
    }
  } catch (error: any) {
    console.error('请求失败:', error)

    // 检查是否有错误响应
    if (error.response) {
      // 尝试从嵌套的响应结构中提取错误消息
      const data = error.response.data
      let errorMessage = '请求错误'

      if (data) {
        if (data.message) {
          // 直接使用顶层message
          errorMessage = data.message
        } else if (data.data && typeof data.data === 'string') {
          // 使用data字段中的错误信息
          errorMessage = data.data
        }
      }

      errorMsg.value = `${errorMessage} (${error.response.status})`
    } else if (error.request) {
      errorMsg.value = '网络连接失败，请检查您的网络'
    } else {
      errorMsg.value = error.message || (isLogin.value ? '登录过程中出现错误' : '注册过程中出现错误')
    }
  } finally {
    loading.value = false
  }
}

// 表单重置
const resetForm = () => {
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.email = ''
  errorMsg.value = ''
}
</script>

<template>
  <div class="flex min-h-screen overflow-hidden relative">
    <!-- 左边装饰元素 - 在中大屏幕显示 -->
    <div class="hidden md:block md:w-2/5 lg:w-1/3 bg-gradient-to-br from-blue-600 to-indigo-700 relative">
      <div class="h-full flex flex-col justify-center items-center px-8 relative z-10">
        <div class="text-white text-center">
          <div class="mb-8 flex justify-center">
            <div class="w-16 h-16 bg-white/10 backdrop-blur-sm rounded-xl flex items-center justify-center">
              <svg class="w-10 h-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
          </div>
          <h2 class="text-4xl font-bold mb-4">轻松找家</h2>
          <p class="text-xl text-blue-100">我们让找房变得简单</p>

          <div class="mt-12">
            <div class="bg-white/10 backdrop-blur-sm rounded-lg p-6 text-left">
              <div class="flex items-start mb-3">
                <div class="bg-white/20 rounded-full p-1 mr-3">
                  <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <p class="text-sm text-blue-50">千万房源任您挑选</p>
              </div>
              <div class="flex items-start mb-3">
                <div class="bg-white/20 rounded-full p-1 mr-3">
                  <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <p class="text-sm text-blue-50">专业经纪人一对一服务</p>
              </div>
              <div class="flex items-start">
                <div class="bg-white/20 rounded-full p-1 mr-3">
                  <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <p class="text-sm text-blue-50">安全可靠的交易保障</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 装饰形状 -->
      <div class="absolute inset-0 overflow-hidden">
        <div class="absolute bottom-0 left-0 w-full h-1/2 bg-gradient-to-t from-blue-700/30 to-transparent"></div>
        <div class="absolute -bottom-16 -right-16 w-48 h-48 rounded-full bg-indigo-500/30 blur-2xl"></div>
        <div class="absolute top-16 -left-8 w-36 h-36 rounded-full bg-blue-400/20 blur-xl"></div>
        <div class="absolute top-1/2 right-16 w-24 h-24 rounded-full bg-white/10"></div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="w-full md:w-3/5 lg:w-2/3 bg-gray-50 flex items-center justify-center py-6 px-4 sm:px-6">
      <div class="w-full max-w-md space-y-6">
        <!-- Logo在小屏幕上显示，大屏隐藏 -->
        <div class="md:hidden text-center mb-6">
          <router-link to="/" class="inline-flex items-center">
            <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center shadow-md">
              <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span class="ml-2 text-2xl font-bold text-blue-600">EasyHome</span>
          </router-link>
        </div>

        <!-- 标题和切换 -->
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-900">
            {{ isLogin ? '欢迎回来' : '创建新账户' }}
          </h2>
          <p class="mt-2 text-sm text-gray-600">
            {{ isLogin ? '登录您的账户以继续' : '填写以下信息创建账户' }}
          </p>
        </div>

        <!-- 卡片 -->
        <div class="bg-white rounded-xl shadow-md overflow-hidden">
          <div class="px-6 py-6">
            <!-- 成功提示 -->
            <div v-if="successMsg" class="mb-4 rounded-lg bg-green-50 p-3 border border-green-200 flex">
              <svg class="h-5 w-5 text-green-500 mr-3 flex-shrink-0" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="text-sm text-green-700">{{ successMsg }}</p>
            </div>

            <!-- 错误提示 -->
            <div v-if="errorMsg" class="mb-4 rounded-lg bg-red-50 p-3 border border-red-200 flex">
              <svg class="h-5 w-5 text-red-500 mr-3 flex-shrink-0" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="text-sm text-red-700">{{ errorMsg }}</p>
            </div>

            <!-- 表单 -->
            <form @submit.prevent="handleSubmit" class="space-y-5">
              <!-- 用户名 -->
              <div>
                <label for="username" class="block text-sm font-medium text-gray-700">用户名</label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <input v-model="form.username" id="username" name="username" type="text" required
                    class="block w-full pl-10 pr-10 py-2 border focus:outline-none sm:text-sm rounded-lg" :class="[
                      'focus:ring-2 focus:ring-offset-1',
                      isUsernameValid === false ? 'border-red-300 focus:ring-red-500' :
                        isUsernameValid === true ? 'border-green-300 focus:ring-green-500' :
                          'border-gray-300 focus:ring-blue-500'
                    ]" placeholder="请输入用户名" :disabled="loading" />

                  <div v-if="isUsernameValid !== null" class="absolute inset-y-0 right-0 pr-3 flex items-center">
                    <svg v-if="isUsernameValid" class="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <svg v-else class="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                </div>
                <p v-if="isUsernameValid === false" class="mt-1 text-xs text-red-600">用户名至少需要4个字符</p>
              </div>

              <!-- 密码 -->
              <div>
                <label for="password" class="block text-sm font-medium text-gray-700">密码</label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input v-model="form.password" id="password" name="password"
                    :type="showPassword ? 'text' : 'password'" required
                    class="block w-full pl-10 pr-10 py-2 border focus:outline-none sm:text-sm rounded-lg" :class="[
                      'focus:ring-2 focus:ring-offset-1',
                      isPasswordValid === false ? 'border-red-300 focus:ring-red-500' :
                        isPasswordValid === true ? 'border-green-300 focus:ring-green-500' :
                          'border-gray-300 focus:ring-blue-500'
                    ]" placeholder="请输入密码" :disabled="loading" />

                  <button type="button" @click="togglePasswordVisibility"
                    class="absolute inset-y-0 right-0 pr-3 flex items-center">
                    <svg v-if="!showPassword" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                    </svg>
                  </button>
                </div>
                <p v-if="isPasswordValid === false" class="mt-1 text-xs text-red-600">密码长度至少6位</p>
              </div>

              <!-- 注册时的额外字段 -->
              <template v-if="!isLogin">
                <!-- 确认密码 -->
                <div>
                  <label for="confirmPassword" class="block text-sm font-medium text-gray-700">确认密码</label>
                  <div class="mt-1 relative rounded-md shadow-sm">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                    </div>
                    <input v-model="form.confirmPassword" id="confirmPassword" name="confirmPassword"
                      :type="showConfirmPassword ? 'text' : 'password'" required
                      class="block w-full pl-10 pr-10 py-2 border focus:outline-none sm:text-sm rounded-lg" :class="[
                        'focus:ring-2 focus:ring-offset-1',
                        isPasswordMatch === false ? 'border-red-300 focus:ring-red-500' :
                          isPasswordMatch === true ? 'border-green-300 focus:ring-green-500' :
                            'border-gray-300 focus:ring-blue-500'
                      ]" placeholder="请再次输入密码" :disabled="loading" />

                    <button type="button" @click="toggleConfirmPasswordVisibility"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center">
                      <svg v-if="!showConfirmPassword" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                        stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      <svg v-else class="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                      </svg>
                    </button>
                  </div>
                  <p v-if="isPasswordMatch === false" class="mt-1 text-xs text-red-600">两次输入的密码不一致</p>
                </div>

                <!-- 邮箱 -->
                <div>
                  <label for="email" class="block text-sm font-medium text-gray-700">电子邮箱</label>
                  <div class="mt-1 relative rounded-md shadow-sm">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <input v-model="form.email" id="email" name="email" type="email" required
                      class="block w-full pl-10 pr-10 py-2 border focus:outline-none sm:text-sm rounded-lg" :class="[
                        'focus:ring-2 focus:ring-offset-1',
                        isEmailValid === false ? 'border-red-300 focus:ring-red-500' :
                          isEmailValid === true ? 'border-green-300 focus:ring-green-500' :
                            'border-gray-300 focus:ring-blue-500'
                      ]" placeholder="example@example.com" :disabled="loading" />

                    <div v-if="isEmailValid !== null" class="absolute inset-y-0 right-0 pr-3 flex items-center">
                      <svg v-if="isEmailValid" class="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24"
                        stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                      </svg>
                      <svg v-else class="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </div>
                  </div>
                  <p v-if="isEmailValid === false" class="mt-1 text-xs text-red-600">请输入有效的邮箱地址</p>
                </div>
              </template>

              <!-- 记住我 和 忘记密码 (仅登录时) -->
              <div v-if="isLogin" class="flex items-center justify-between">
                <div class="flex items-center">
                  <input v-model="rememberMe" id="remember-me" name="remember-me" type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded">
                  <label for="remember-me" class="ml-2 block text-sm text-gray-700">
                    记住我
                  </label>
                </div>

                <div class="text-sm">
                  <a href="#" class="font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200">
                    忘记密码?
                  </a>
                </div>
              </div>

              <!-- 提交按钮 -->
              <div>
                <button type="submit"
                  class="group relative w-full flex justify-center items-center py-2.5 px-4 border border-transparent rounded-lg text-sm font-medium text-white transition-all duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  :class="loading ? 'bg-blue-400 cursor-wait' : 'bg-blue-600 hover:bg-blue-700 shadow-md hover:shadow-lg'"
                  :disabled="loading">
                  <span class="absolute left-0 inset-y-0 flex items-center pl-3">
                    <svg v-if="loading" class="animate-spin h-5 w-5 text-white opacity-75" fill="none"
                      viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                      </path>
                    </svg>
                    <svg v-else class="h-5 w-5 text-blue-300 group-hover:text-blue-200" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                    </svg>
                  </span>
                  <span>{{ isLogin ? (loading ? '登录中...' : '登录') : (loading ? '注册中...' : '注册') }}</span>
                </button>
              </div>
            </form>

            <!-- 登录/注册切换 -->
            <div class="mt-5">
              <div class="relative">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-gray-200"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                  <span class="px-4 bg-white text-gray-500">
                    {{ isLogin ? '没有账户?' : '已有账户?' }}
                  </span>
                </div>
              </div>

              <div class="mt-4 text-center">
                <button @click="toggleMode" type="button"
                  class="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200">
                  <svg v-if="isLogin" class="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <svg v-else class="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M11 17l-5-5m0 0l5-5m-5 5h12" />
                  </svg>
                  {{ isLogin ? '创建新账户' : '使用已有账户登录' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 底部版权信息 -->
          <div class="px-6 py-3 bg-gray-50 border-t border-gray-100 text-xs text-center text-gray-500">
            <p>© {{ new Date().getFullYear() }} EasyHome. 保留所有权利</p>
          </div>
        </div>

        <!-- 返回首页链接 -->
        <div class="text-center">
          <router-link to="/"
            class="inline-flex items-center text-xs text-gray-500 hover:text-blue-600 transition-colors duration-200">
            <svg class="mr-1 h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            返回首页
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 优化的按钮样式 */
button:focus:not(.disabled) {
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

button:active:not(:disabled) {
  transform: translateY(1px);
}

/* 左侧装饰元素动画 - 更柔和的淡入效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.md\:block {
  animation: fadeIn 0.8s ease-out;
}

/* 输入框动态样式 */
input:focus {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

/* 调整滚动行为 */
html,
body {
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.3) transparent;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(59, 130, 246, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-track {
  background: transparent;
}
</style>