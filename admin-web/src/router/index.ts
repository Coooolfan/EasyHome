import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),//去除hash模式"#"
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
        },
        {
          path: '/user-manage',
          name: 'UserManage',
          component: () => import('../views/UserManageView.vue')
        },
        {
          path: '/house-manage',
          name: 'HouseManage',
          component: () => import('../views/HouseManageView.vue')
        },
        {
          path: '/sell-house-manage',
          name: 'SellHouseManage',
          component: () => import('../views/SellHouseManageView.vue'),
          meta: { title: '卖房管理' }
        },
        // 预约管理路由
        {
          path: '/reservation-manage',
          name: 'ReservationManage',
          component: () => import('../views/AppointmentManageView.vue'),
          meta: { title: '预约管理' }
        },
        {
          path: '/profile',
          name: 'Profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { title: '个人中心', requiresAuth: true }
        },
        {
          path: '/settings',
          name: 'Settings',
          component: () => import('@/views/SettingsView.vue'),
          meta: { title: '系统设置', requiresAuth: true, roles: ['super_admin', 'admin'] }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 初始化用户状态 - 会从本地存储中恢复身份认证信息
  const isAuthenticated = userStore.initUser()
  
  // 如果路由需要认证但用户未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } }) // 保存原目标路径
  } 
  // 如果路由需要超级管理员权限但用户不是超级管理员
  else if (to.meta.requiresSuperAdmin && userStore.userInfo.role !== 'role_admin') {
    next('/dashboard') // 重定向到仪表盘
  } 
  // 如果用户已登录但尝试访问登录页
  else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/') // 重定向到首页
  } 
  // 检查角色权限
  else if (to.meta.roles && Array.isArray(to.meta.roles) && to.meta.roles.length > 0) {
    if (to.meta.roles.includes(userStore.userInfo.role)) {
      next() // 允许访问
    } else {
      next('/dashboard') // 重定向到仪表盘
    }
  } 
  // 其他情况，允许继续
  else {
    next()
  }
})

export default router
