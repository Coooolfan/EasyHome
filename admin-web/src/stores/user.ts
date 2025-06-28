import { defineStore } from 'pinia'
import axios from 'axios'

export interface UserInfo {
  id: number
  username: string
  role: string
  status: string
  // 其他用户信息字段
}

export const useUserStore = defineStore('user', {
  state: () => ({
    isLoggedIn: false,
    userInfo: {
      id: 0,
      username: '',
      role: '',
      status: ''
    } as UserInfo
  }),
  
  actions: {
    // 初始化用户状态 - 从本地存储恢复
    initUser() {
      // 检查本地存储中的token
      const token = localStorage.getItem('admin_token')
      
      if (token) {
        // 如果有token，尝试从本地存储获取用户信息
        const userInfoStr = localStorage.getItem('admin_user_info')
        
        if (userInfoStr) {
          try {
            // 尝试解析存储的用户信息
            const userInfo = JSON.parse(userInfoStr)
            this.isLoggedIn = true
            this.userInfo = userInfo
            
            // 设置axios默认头部包含token用于API请求
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
            return true
          } catch (error) {
            console.error('解析用户信息失败:', error)
            this.logout() // 如果解析失败，执行登出操作
            return false
          }
        } else {
          // 如果有token但没有用户信息，尝试获取用户信息
          return this.fetchUserInfo()
        }
      } else {
        this.isLoggedIn = false
        delete axios.defaults.headers.common['Authorization']
        return false
      }
    },
    
    // 从后端获取用户信息
    async fetchUserInfo() {
      try {
        const response = await axios.get('/api/admin/user-info')
        
        if (response.data && response.data.code === 'SUCCESS') {
          this.isLoggedIn = true
          this.userInfo = response.data.data
          
          // 保存到本地存储
          localStorage.setItem('admin_user_info', JSON.stringify(this.userInfo))
          return true
        } else {
          throw new Error('获取用户信息失败')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.logout()
        return false
      }
    },
    
    // 登出操作
    logout() {
      this.isLoggedIn = false
      this.userInfo = {
        id: 0,
        username: '',
        role: '',
        status: ''
      }
      
      // 清除本地存储的认证信息
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user_info')
      delete axios.defaults.headers.common['Authorization']
    }
  }
})