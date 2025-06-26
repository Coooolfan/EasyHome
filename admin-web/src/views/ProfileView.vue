<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>

      <div class="profile-content">
        <div class="profile-left">
          <div class="avatar-section">
            <el-avatar :size="120" :src="userProfile.avatar" class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <el-button type="primary" size="small" class="upload-btn" @click="handleAvatarUpload">
              <el-icon><Camera /></el-icon>
              更换头像
            </el-button>
          </div>
        </div>

        <div class="profile-right">
          <el-form :model="userProfile" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userProfile.username" disabled />
            </el-form-item>
            
            <el-form-item label="真实姓名">
              <el-input v-model="userProfile.realName" />
            </el-form-item>
            
            <el-form-item label="邮箱">
              <el-input v-model="userProfile.email" type="email" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="userProfile.phone" />
            </el-form-item>
            
            <el-form-item label="角色">
              <el-tag :type="getRoleType(userProfile.role)">
                {{ getRoleText(userProfile.role) }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="注册时间">
              <el-input v-model="userProfile.createTime" disabled />
            </el-form-item>
            
            <el-form-item label="最后登录">
              <el-input v-model="userProfile.lastLogin" disabled />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updateProfile">
                <el-icon><Check /></el-icon>
                保存修改
              </el-button>
              <el-button @click="resetForm">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const userProfile = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: '',
  avatar: '',
  createTime: '',
  lastLogin: ''
})

const getRoleType = (role: string) => {
  switch (role) {
    case 'super_admin': return 'danger'
    case 'admin': return 'warning'
    default: return 'info'
  }
}

const getRoleText = (role: string) => {
  switch (role) {
    case 'super_admin': return '超级管理员'
    case 'admin': return '管理员'
    default: return '普通用户'
  }
}

const handleAvatarUpload = () => {
  ElMessage.info('头像上传功能开发中...')
}

const updateProfile = () => {
  // 模拟保存
  ElMessage.success('个人信息已更新')
}

const resetForm = () => {
  loadUserProfile()
  ElMessage.info('已重置为原始信息')
}

const loadUserProfile = () => {
  // 模拟加载用户信息
  const userInfo = userStore.userInfo
  Object.assign(userProfile, {
    username: userInfo.username || '管理员',
    realName: userInfo.realName || '张三',
    email: userInfo.email || 'admin@example.com',
    phone: userInfo.phone || '13800138000',
    role: userInfo.role || 'admin',
    avatar: userInfo.avatar || '',
    createTime: '2024-01-01 10:00:00',
    lastLogin: '2024-01-15 09:30:00'
  })
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  color: #409EFF;
}

.profile-content {
  display: flex;
  gap: 40px;
}

.profile-left {
  flex-shrink: 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.upload-btn {
  border-radius: 20px;
}

.profile-right {
  flex: 1;
}

.profile-form {
  max-width: 600px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
