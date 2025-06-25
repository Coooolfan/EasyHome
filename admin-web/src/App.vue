<template>
  <div id="app">
    <!-- 导航栏 -->
    <el-header v-if="showNavbar" height="60px" class="navbar">
      <div class="nav-content">
        <h2>二手房交易管理系统</h2>
        <div class="nav-right">
          <span>欢迎，{{ userInfo.username }}</span>
          <el-button @click="logout" type="primary" size="small">退出登录</el-button>
        </div>
      </div>
    </el-header>
    
    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 只有在非登录页面显示导航栏
const showNavbar = computed(() => route.name !== 'Login')
const userInfo = computed(() => userStore.userInfo)

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,.12);
}

.nav-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>
