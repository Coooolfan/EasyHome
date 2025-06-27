import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: number
  username: string
  role: 'super_admin' | 'admin'
  email?: string
  status: 'active' | 'disabled'
  realName?: string  
  phone?: string     
  avatar?: string    
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
  
  //  预约管理相关状态
  const pendingReservationCount = ref(0)
  const canViewFullPhone = computed(() => {
    return userInfo.value.role === 'super_admin' || userInfo.value.role === 'admin'
  })

  const login = async (loginForm: LoginForm) => {
    // 现有登录逻辑保持不变
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
    // 清空预约相关状态
    pendingReservationCount.value = 0
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

  //  更新待审核预约数量
  const updatePendingReservationCount = (count: number) => {
    pendingReservationCount.value = count
  }

  return {
    userInfo,
    isLoggedIn,
    pendingReservationCount,
    canViewFullPhone,
    login,
    logout,
    initUser,
    updatePendingReservationCount
  }
})
