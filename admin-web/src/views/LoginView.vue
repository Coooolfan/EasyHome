<template>
  <div class="login-container">
    <!-- 左侧标题区域 - 全屏左半部分 -->
    <div class="title-section" :class="{ 'slide-in': isLoaded }">
      <div class="title-content">
        <h1 class="main-title">Eazy Home</h1>
        <p class="subtitle">二手房交易管理平台</p>
        <div class="decorative-elements">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 - 全屏右半部分 -->
    <div class="login-section" :class="{ 'slide-in': isLoaded }">
      <div class="login-form">
        <h2>管理员登录</h2>
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入账号" size="large" prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" prefix-icon="Lock"
              show-password />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="login-btn">
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// 页面加载状态
const isLoaded = ref(false)

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 直接使用axios调用新的后端接口
        const response = await axios.post('/api/admin/login', {
          username: loginForm.username,
          password: loginForm.password
        });

        // 在登录成功部分添加代码:
        if (response.data && response.data.code === 'SUCCESS') {
          // 将token保存到localStorage
          const token = response.data.data.token;
          localStorage.setItem('admin_token', token);

          // 设置 axios 默认请求头
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

          // 从响应中获取用户信息
          const userInfo = response.data.data.userInfo || {
            username: loginForm.username,
            role: response.data.data.role || 'role_user',
            id: response.data.data.id || 0,
            status: response.data.data.status || 'active'
          };

          // 保存用户信息到本地存储
          localStorage.setItem('admin_user_info', JSON.stringify(userInfo));

          // 更新用户状态
          userStore.$patch({
            isLoggedIn: true,
            userInfo: userInfo
          });

          ElMessage.success('登录成功');

          // 根据用户角色跳转
          if (userInfo.role === 'role_admin') {
            router.push('/admin');
          } else {
            router.push('/dashboard');
          }
        }
      } catch (error: any) {
        console.error('登录错误:', error);
        ElMessage.error(error.response?.data?.message || error.message || '登录失败，请检查账号密码');
      } finally {
        loading.value = false;
      }
    }
  })
}

// 页面挂载时触发动画
onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 左侧标题区域 - 全屏左半部分 */
.title-section {
  width: 50%;
  height: 100vh;
  background: linear-gradient(135deg, #667cea 0%, #4ba2a2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 1s cubic-bezier(0.23, 1, 0.32, 1);
  clip-path: polygon(0 0, 85% 0, 100% 100%, 0 100%);
  /* 初始状态：从左侧隐藏 */
  transform: translateX(-100%);
  opacity: 0;
}

/* 页面加载后的动画状态 */
.title-section.slide-in {
  transform: translateX(0);
  opacity: 1;
}

.title-content {
  text-align: center;
  z-index: 2;
  padding: 0 60px;
  /* 内容初始状态 */
  transform: translateY(50px);
  opacity: 0;
  transition: all 1s cubic-bezier(0.23, 1, 0.32, 1) 0.4s;
}

.title-section.slide-in .title-content {
  transform: translateY(0);
  opacity: 1;
}

.main-title {
  font-size: 4rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  background: linear-gradient(45deg, #fff, #f0f8ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleGlow 3s ease-in-out infinite alternate;
}

.subtitle {
  font-size: 1.4rem;
  opacity: 0.9;
  margin-bottom: 2rem;
  font-weight: 300;
}

/* 装饰元素 */
.decorative-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 8s ease-in-out infinite;
  transform: scale(0);
  transition: transform 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}

.title-section.slide-in .circle {
  transform: scale(1);
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: 15%;
  left: 8%;
  animation-delay: 0s;
  transition-delay: 0.8s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  top: 65%;
  right: 12%;
  animation-delay: 2.5s;
  transition-delay: 1.2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 15%;
  animation-delay: 5s;
  transition-delay: 1.6s;
}

/* 右侧登录区域 - 全屏右半部分 */
.login-section {
  width: 50%;
  height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 1s cubic-bezier(0.23, 1, 0.32, 1);
  position: relative;
  /* 初始状态：从右侧隐藏 */
  transform: translateX(100%);
  opacity: 0;
}

.login-section.slide-in {
  transform: translateX(0);
  opacity: 1;
}

.login-form {
  width: 400px;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  /* 表单初始状态 */
  transform: translateY(30px);
  opacity: 0;
  transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1) 0.6s;
}

.login-section.slide-in .login-form {
  transform: translateY(0);
  opacity: 1;
}

.login-form h2 {
  font-size: x-large;
  color: #333;
  text-align: center;
  margin-bottom: 2rem;
  font-weight: 600;
}

/* Element Plus 组件样式优化 */
:deep(.el-form-item) {
  margin-bottom: 1.5rem;
}

:deep(.el-input__wrapper) {
  border-radius: 30px;
  border: 2px solid #e1e5e9;
  box-shadow: none;
  transition: all 0.3s ease;
  padding: 1px 20px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__inner) {
  font-size: 1rem;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #66c3ea 0%, #4b5ca2 100%);
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

/* 动画效果 */
@keyframes titleGlow {
  0% {
    text-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
  }

  100% {
    text-shadow: 0 0 40px rgba(255, 255, 255, 0.8), 0 0 60px rgba(255, 255, 255, 0.3);
  }
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .title-section {
    width: 100%;
    height: 40vh;
    clip-path: none;
    transform: translateY(-100%);
  }

  .title-section.slide-in {
    transform: translateY(0);
  }

  .login-section {
    width: 100%;
    height: 60vh;
    transform: translateY(100%);
  }

  .login-section.slide-in {
    transform: translateY(0);
  }

  .main-title {
    font-size: 2.5rem;
  }

  .login-form {
    width: 90%;
    padding: 40px 30px;
  }
}
</style>
