import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
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
          path: '/admin-manage',
          name: 'AdminManage',
          component: () => import('../views/AdminManageView.vue'),
          meta: { requiresSuperAdmin: true }
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
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.initUser()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresSuperAdmin && userStore.userInfo.role !== 'super_admin') {
    next('/dashboard')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
