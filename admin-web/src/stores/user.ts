import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
interface UserInfo {
  id: number
  username: string
  role: 'super_admin' | 'admin'
  email?: string
  status: 'active' | 'disabled'
}

interface LoginForm {
  username: string
  password: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo>({
    id: 0,
    username: '',
    role: 'admin',
    status: 'active'
  })

  const isLoggedIn = ref(false)

  const login = async (loginForm: LoginForm) => {
    // 这里应该调用真实的登录API
    // 现在先模拟登录逻辑
    if (loginForm.username === 'admin' && loginForm.password === '123456') {
      userInfo.value = {
        id: 1,
        username: 'admin',
        role: 'super_admin',
        email: 'admin@example.com',
        status: 'active'
      }
      isLoggedIn.value = true
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      localStorage.setItem('isLoggedIn', 'true')
    } else {
      throw new Error('登录失败')
    }
  }

  const logout = () => {
    userInfo.value = {
      id: 0,
      username: '',
      role: 'admin',
      status: 'active'
    }
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLoggedIn')
  }

  const initUser = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    const savedLoginStatus = localStorage.getItem('isLoggedIn')
    
    if (savedUserInfo && savedLoginStatus === 'true') {
      userInfo.value = JSON.parse(savedUserInfo)
      isLoggedIn.value = true
    }
  }

  return {
    userInfo,
    isLoggedIn,
    login,
    logout,
    initUser
  }
})
