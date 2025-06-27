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
            <el-avatar :size="120" class="user-avatar">
              {{ getAvatarText(userProfile.username) }}
            </el-avatar>
            <div class="user-info">
              <div class="user-name">{{ userProfile.username }}</div>
              <div class="user-role">{{ getRoleText(userProfile.role) }}</div>
            </div>
          </div>
        </div>

        <div class="profile-right">
          <el-form :model="userProfile" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userProfile.username" disabled />
            </el-form-item>
            
            <el-form-item label="邮箱">
              <el-input v-model="userProfile.email" type="email" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="userProfile.phone" />
            </el-form-item>
            
            <el-form-item label="角色">
              <span class="role-text">{{ getRoleText(userProfile.role) }}</span>
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
import { User, Check, Refresh } from '@element-plus/icons-vue'

// 定义用户信息接口
interface UserProfile {
  username: string
  email: string
  phone: string
  role: string
  avatar: string
  createTime: string
  lastLogin: string
}

const userStore = useUserStore()

const userProfile = reactive<UserProfile>({
  username: '',
  email: '',
  phone: '',
  role: '',
  avatar: '',
  createTime: '',
  lastLogin: ''
})

// 获取头像显示文字（首字母）
const getAvatarText = (name: string) => {
  if (!name) return 'A'
  // 如果是中文，取第一个字符
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(0)
  }
  // 如果是英文，取第一个字母的大写
  return name.charAt(0).toUpperCase()
}

const getRoleText = (role: string) => {
  switch (role) {
    case 'super_admin': return '超级管理员'
    case 'admin': return '管理员'
    default: return '普通用户'
  }
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
  // 从 userStore 获取用户信息，使用安全的属性访问
  const userInfo = userStore.userInfo
  
  Object.assign(userProfile, {
    username: userInfo.username || '管理员',
    email: userInfo.email || 'admin@example.com',
    phone: (userInfo as any).phone || '13800138000',
    role: userInfo.role || 'admin',
    avatar: (userInfo as any).avatar || '',
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
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-icon {
  color: #409EFF;
  font-size: 18px;
}

.profile-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.profile-left {
  flex-shrink: 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.user-avatar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 48px;
  font-weight: 600;
}

.user-info {
  text-align: center;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-role {
  font-size: 14px;
  color: #6b7280;
  font-weight: normal;
}

.profile-right {
  flex: 1;
}

.profile-form {
  max-width: 600px;
}

.role-text {
  font-size: 14px;
  color: #374151;
  font-weight: normal;
  padding: 4px 0;
  display: inline-block;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .avatar-section {
    padding: 16px;
  }
  
  .profile-form {
    max-width: 100%;
  }
  
  .user-avatar {
    font-size: 36px;
  }
}
</style>
