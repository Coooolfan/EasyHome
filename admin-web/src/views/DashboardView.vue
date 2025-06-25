<template>
  <div class="dashboard">
    <h2>数据看板</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ dashboardData.totalUsers }}</h3>
              <p>总用户数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon house-icon">
              <el-icon><House /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ dashboardData.totalHouses }}</h3>
              <p>总房源数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ dashboardData.pendingHouses }}</h3>
              <p>待审核房源</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon admin-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ dashboardData.totalAdmins }}</h3>
              <p>管理员数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作 -->
    <el-row :gutter="20" class="recent-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近房源审核</span>
            </div>
          </template>
          <el-table :data="recentHouses" style="width: 100%">
            <el-table-column prop="title" label="房源标题" />
            <el-table-column prop="price" label="价格" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近用户注册</span>
            </div>
          </template>
          <el-table :data="recentUsers" style="width: 100%">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="registerTime" label="注册时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 定义接口类型
interface RecentHouse {
  title: string
  price: string
  status: 'pending' | 'approved' | 'rejected'
}

interface RecentUser {
  username: string
  email: string
  registerTime: string
}

const dashboardData = ref({
  totalUsers: 0,
  totalHouses: 0,
  pendingHouses: 0,
  totalAdmins: 0
})

// 为 ref 添加明确的类型声明
const recentHouses = ref<RecentHouse[]>([])
const recentUsers = ref<RecentUser[]>([])

const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已通过'
    case 'rejected': return '已拒绝'
    default: return '未知'
  }
}

const loadDashboardData = async () => {
  // 模拟数据加载
  dashboardData.value = {
    totalUsers: 1280,
    totalHouses: 856,
    pendingHouses: 23,
    totalAdmins: 8
  }
  
  // 现在类型匹配正确了
  recentHouses.value = [
    { title: '阳光花园三室两厅', price: '¥120万', status: 'pending' },
    { title: '中央公园精装修', price: '¥200万', status: 'approved' },
    { title: '市中心商圈住宅', price: '¥180万', status: 'pending' }
  ]
  
  recentUsers.value = [
    { username: '张三', email: 'zhangsan@email.com', registerTime: '2024-01-15' },
    { username: '李四', email: 'lisi@email.com', registerTime: '2024-01-14' },
    { username: '王五', email: 'wangwu@email.com', registerTime: '2024-01-13' }
  ]
}

onMounted(() => {
  loadDashboardData()
})
</script>


<style scoped>
.dashboard {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.user-icon { background: #409EFF; }
.house-icon { background: #67C23A; }
.pending-icon { background: #E6A23C; }
.admin-icon { background: #F56C6C; }

.stat-info h3 {
  margin: 0;
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-info p {
  margin: 5px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.recent-section {
  margin-top: 30px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}
</style>
