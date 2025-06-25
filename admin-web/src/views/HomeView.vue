<template>
  <div class="home-container">
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="250px" class="sidebar">
        <div class="sidebar-header">
          <h3>管理控制台</h3>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <!-- 看板 -->
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>

          <!-- 功能管理 -->
          <el-sub-menu index="function" v-if="canAccessFunction">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>功能管理</span>
            </template>
            
            <el-menu-item index="/admin-manage" v-if="isSuperAdmin">
              <el-icon><UserFilled /></el-icon>
              <span>管理员管理</span>
            </el-menu-item>
            
            <el-menu-item index="/user-manage">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 业务管理 -->
          <el-sub-menu index="business">
            <template #title>
              <el-icon><House /></el-icon>
              <span>业务管理</span>
            </template>
            
            <el-menu-item index="/house-manage">
              <el-icon><OfficeBuilding /></el-icon>
              <span>房源管理</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isSuperAdmin = computed(() => userStore.userInfo.role === 'super_admin')
const canAccessFunction = computed(() => userStore.userInfo.role === 'super_admin' || userStore.userInfo.role === 'admin')

const handleMenuSelect = (index: string) => {
  router.push(index)
}

onMounted(() => {
  // 默认跳转到数据看板
  if (route.path === '/') {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.home-container {
  height: calc(100vh - 60px);
}

.sidebar {
  background-color: #304156;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  background-color: #263445;
  color: #bfcbd9;
  border-bottom: 1px solid #1f2d3d;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 120px);
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
