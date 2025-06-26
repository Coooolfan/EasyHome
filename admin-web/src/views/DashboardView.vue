<template>
  <div class="dashboard">
    <!-- 欢迎语 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">👋 欢迎回来，{{ userInfo.username }}</h1>
        <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
      </div>
       <div class="welcome-right">
      
        <el-button type="success" size="large" @click="showReportDialog">
          <el-icon><Document /></el-icon>
          查看报告
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stats-section">
      <el-col :span="6" v-for="(stat, index) in statsData" :key="index">
        <div class="stat-card" :class="stat.className">
          <div class="stat-card-content">
            <div class="stat-icon">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-value" :style="{ color: stat.color }">
                {{ stat.value }}
              </div>
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-trend" :class="stat.trendClass">
                <el-icon><component :is="stat.trendIcon" /></el-icon>
                <span>{{ stat.trend }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据图表和最近活动 -->
    <el-row :gutter="24" class="content-section">
      <!-- 数据图表 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><TrendCharts /></el-icon>
                <span class="header-title">数据趋势</span>
              </div>
              <el-button text type="primary" @click="refreshData">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
          </template>
          
          <!-- 真实图表容器 -->
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 最近活动 -->
      <el-col :span="8">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Clock /></el-icon>
                <span class="header-title">最近活动</span>
              </div>
            </div>
          </template>
          <div class="activity-list">
            <div class="activity-item" v-for="(activity, index) in recentActivities" :key="index">
              <div class="activity-icon" :class="activity.type">
                <el-icon><component :is="activity.icon" /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近数据表格 -->
    <el-row :gutter="24" class="table-section">
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><House /></el-icon>
                <span class="header-title">最近房源审核</span>
              </div>
              <el-button text type="primary" @click="viewAllHouses">
                      查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="recentHouses" style="width: 100%" class="modern-table">
            <el-table-column prop="title" label="房源标题" min-width="140">
              <template #default="scope">
                <div class="table-title">{{ scope.row.title }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100" align="right">
              <template #default="scope">
                <div class="price-text">{{ scope.row.price }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag 
                  :type="getStatusType(scope.row.status)" 
                  size="small"
                  class="status-tag"
                >
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><User /></el-icon>
                <span class="header-title">最近用户注册</span>
              </div>
               <el-button text type="primary" @click="viewAllUsers">
                  查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="recentUsers" style="width: 100%" class="modern-table">
            <el-table-column prop="username" label="用户名" min-width="100">
              <template #default="scope">
                <div class="user-info">
                  <el-avatar :size="24" class="user-avatar">
                    {{ scope.row.username.charAt(0) }}
                  </el-avatar>
                  <span class="username">{{ scope.row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="140" />
            <el-table-column prop="registerTime" label="注册时间" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加房源模态框 -->
    <el-dialog
      v-model="addHouseVisible"
      title="添加房源"
      width="800px"
      :before-close="handleCloseAddHouse"
    >
      <el-form :model="houseForm" :rules="houseRules" ref="houseFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房源标题" prop="title">
              <el-input v-model="houseForm.title" placeholder="请输入房源标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房源类型" prop="type">
              <el-select v-model="houseForm.type" placeholder="请选择房源类型" style="width: 100%">
                <el-option label="住宅" value="residential" />
                <el-option label="公寓" value="apartment" />
                <el-option label="别墅" value="villa" />
                <el-option label="商铺" value="shop" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" prop="price">
              <el-input v-model="houseForm.price" placeholder="请输入售价">
                <template #suffix>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="面积" prop="area">
              <el-input v-model="houseForm.area" placeholder="请输入面积">
                <template #suffix>㎡</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房间数" prop="rooms">
              <el-select v-model="houseForm.rooms" placeholder="请选择" style="width: 100%">
                <el-option label="1室" value="1" />
                <el-option label="2室" value="2" />
                <el-option label="3室" value="3" />
                <el-option label="4室" value="4" />
                <el-option label="5室及以上" value="5+" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所在城市" prop="city">
              <el-input v-model="houseForm.city" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="houseForm.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="房源描述" prop="description">
          <el-input
            v-model="houseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入房源描述"
          />
        </el-form-item>

        <el-form-item label="房源图片">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            multiple
            :auto-upload="false"
            :on-change="handleImageChange"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                jpg/png 文件，且不超过 500kb
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseAddHouse">取消</el-button>
          <el-button type="primary" @click="submitHouse" :loading="submitting">
            {{ submitting ? '提交中...' : '提交' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看报告模态框 -->
    <el-dialog
      v-model="reportVisible"
      title="系统报告"
      width="900px"
      :before-close="handleCloseReport"
    >
      <el-tabs v-model="activeReportTab" class="report-tabs" @tab-change="handleTabChange">

        <!-- 业务报告 -->
        <el-tab-pane label="业务报告" name="business">
          <div class="report-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="本月新增房源" :value="reportData.newHouses" suffix="套">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><House /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="本月成交量" :value="reportData.transactions" suffix="套">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><Check /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="成交金额" :value="reportData.amount" suffix="万元">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><Money /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>

            <el-divider />

            <h4>热门区域</h4>
            <el-table :data="reportData.hotAreas" style="width: 100%">
              <el-table-column prop="area" label="区域" width="200" />
              <el-table-column prop="count" label="房源数量" width="120" />
              <el-table-column prop="avgPrice" label="平均价格" width="150">
                <template #default="scope">
                  {{ scope.row.avgPrice }} 万元
                </template>
              </el-table-column>
              <el-table-column prop="trend" label="趋势">
                <template #default="scope">
                  <el-tag :type="scope.row.trend === 'up' ? 'success' : 'danger'">
                    {{ scope.row.trend === 'up' ? '上涨' : '下跌' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 用户报告 -->
        <el-tab-pane label="用户报告" name="user">
          <div class="report-content">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总用户数" :value="reportData.totalUsers" suffix="人">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><User /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="本月新增" :value="reportData.newUsers" suffix="人">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><UserFilled /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="活跃用户" :value="reportData.activeUsers" suffix="人">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><StarFilled /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="用户留存率" :value="reportData.retention" suffix="%">
                  <template #prefix>
                    <el-icon style="vertical-align: -0.125em"><TrendCharts /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>

            <el-divider />

            <h4>用户活跃度统计</h4>
            <!-- 真实用户图表容器 -->
            <div ref="userChartRef" class="user-chart-container"></div>
          </div>
        </el-tab-pane>

        <!-- 系统报告 -->
        <el-tab-pane label="系统报告" name="system">
          <div class="report-content">
            <el-descriptions title="系统状态" :column="2" border>
              <el-descriptions-item label="系统版本">v2.1.0</el-descriptions-item>
              <el-descriptions-item label="运行时间">15天 8小时 32分钟</el-descriptions-item>
              <el-descriptions-item label="CPU使用率">
                <el-progress :percentage="reportData.cpuUsage" />
              </el-descriptions-item>
              <el-descriptions-item label="内存使用率">
                <el-progress :percentage="reportData.memoryUsage" />
              </el-descriptions-item>
              <el-descriptions-item label="磁盘使用率">
                <el-progress :percentage="reportData.diskUsage" />
              </el-descriptions-item>
              <el-descriptions-item label="数据库状态">
                <el-tag type="success">正常</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider />

            <h4>系统日志</h4>
            <el-table :data="reportData.logs" style="width: 100%">
              <el-table-column prop="time" label="时间" width="180" />
              <el-table-column prop="level" label="级别" width="80">
                <template #default="scope">
                  <el-tag :type="getLogType(scope.row.level)" size="small">
                    {{ scope.row.level }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="消息" />
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportReport" type="primary">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
          <el-button @click="handleCloseReport">关闭</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>


<script setup lang="ts">
import { ref, onMounted, computed, reactive, nextTick, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { 
  Plus, Document, Refresh, TrendCharts, Clock, House, User,
  ArrowUp, ArrowDown, UserFilled, OfficeBuilding, Warning,
  Check, Money, StarFilled, Download, UploadFilled, ArrowRight
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()

const userInfo = computed(() => ({
  username: userStore.userInfo.username || '管理员'
}))

const viewAllUsers = () => {
  router.push('/user-manage')
}

const viewAllHouses = () => {
  router.push('/house-manage')
}

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 统计数据
const statsData = ref([
  {
    label: '总用户数',
    value: '1,280',
    icon: User,
    color: '#409EFF',
    className: 'user-stat',
    trend: '+12.5%',
    trendClass: 'trend-up',
    trendIcon: ArrowUp
  },
  {
    label: '总房源数',
    value: '856',
    icon: House,
    color: '#67C23A',
    className: 'house-stat',
    trend: '+8.2%',
    trendClass: 'trend-up',
    trendIcon: ArrowUp
  },
  {
    label: '待审核房源',
    value: '23',
    icon: Clock,
    color: '#E6A23C',
    className: 'pending-stat',
    trend: '-5.4%',
    trendClass: 'trend-down',
    trendIcon: ArrowDown
  },
  {
    label: '管理员数量',
    value: '8',
    icon: UserFilled,
    color: '#F56C6C',
    className: 'admin-stat',
    trend: '0%',
    trendClass: 'trend-neutral',
    trendIcon: ArrowUp
  }
])

// 最近活动
const recentActivities = ref([
  {
    title: '新用户注册',
    time: '2分钟前',
    icon: User,
    type: 'user'
  },
  {
    title: '房源审核通过',
    time: '5分钟前',
    icon: House,
    type: 'house'
  },
  {
    title: '系统设置更新',
    time: '10分钟前',
    icon: Warning,
    type: 'system'
  },
  {
    title: '新管理员加入',
    time: '1小时前',
    icon: UserFilled,
    type: 'admin'
  }
])

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

const refreshData = () => {
  // 刷新图表数据
  refreshCharts()
  // 其他数据刷新逻辑
  loadDashboardData()
  // 显示成功消息
  ElMessage.success('数据已刷新')
}


// 添加房源相关
const addHouseVisible = ref(false)
const submitting = ref(false)
const houseFormRef = ref<FormInstance>()

const houseForm = reactive({
  title: '',
  type: '',
  price: '',
  area: '',
  rooms: '',
  city: '',
  address: '',
  description: ''
})

const houseRules: FormRules = {
  title: [
    { required: true, message: '请输入房源标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择房源类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 查看报告相关
const reportVisible = ref(false)
const activeReportTab = ref('business')

const reportData = reactive({
  // 业务数据
  newHouses: 156,
  transactions: 89,
  amount: 2580,
  hotAreas: [
    { area: '市中心', count: 45, avgPrice: 89, trend: 'up' },
    { area: '高新区', count: 32, avgPrice: 76, trend: 'up' },
    { area: '老城区', count: 28, avgPrice: 45, trend: 'down' },
    { area: '开发区', count: 24, avgPrice: 58, trend: 'up' }
  ],
  // 用户数据
  totalUsers: 1285,
  newUsers: 126,
  activeUsers: 856,
  retention: 78.5,
  // 系统数据
  cpuUsage: 65,
  memoryUsage: 72,
  diskUsage: 45,
  logs: [
    { time: '2024-01-15 10:30:15', level: 'INFO', message: '用户登录成功' },
    { time: '2024-01-15 10:25:32', level: 'WARN', message: '数据库连接池使用率过高' },
    { time: '2024-01-15 10:20:18', level: 'INFO', message: '新房源添加成功' },
    { time: '2024-01-15 10:15:45', level: 'ERROR', message: '邮件发送失败' }
  ]
})

// 显示添加房源对话框
const showAddHouseDialog = () => {
  addHouseVisible.value = true
}

// 关闭添加房源对话框
const handleCloseAddHouse = () => {
  resetHouseForm()
  addHouseVisible.value = false
}
// 图表相关
const chartRef = ref<HTMLDivElement>()
const userChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null

// 趋势图表数据
const trendData = reactive({
  dates: ['01-09', '01-10', '01-11', '01-12', '01-13', '01-14', '01-15'],
  users: [42, 58, 63, 71, 89, 95, 126],
  houses: [23, 31, 28, 35, 42, 38, 45],
  transactions: [8, 12, 15, 18, 22, 28, 32]
})

// 用户活跃度数据
const userActivityData = reactive({
  hours: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
  activeUsers: [12, 8, 5, 15, 45, 78, 95, 102, 88, 76, 65, 35],
  newUsers: [2, 1, 0, 3, 8, 12, 15, 18, 12, 8, 5, 2]
})

// 初始化趋势图表
const initTrendChart = () => {
  if (!chartRef.value) return
  
  trendChart = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: '数据趋势',
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#606266'
      }
    },
    legend: {
      data: ['新增用户', '新增房源', '成交量'],
      bottom: 10,
      textStyle: {
        color: '#606266'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.dates,
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisLabel: {
        color: '#909399'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisLabel: {
        color: '#909399'
      },
      splitLine: {
        lineStyle: {
          color: '#f5f7fa'
        }
      }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: trendData.users,
        lineStyle: {
          color: '#409EFF',
          width: 3
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          opacity: 0.1,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: 'rgba(64, 158, 255, 0)' }
          ])
        }
      },
      {
        name: '新增房源',
        type: 'line',
        smooth: true,
        data: trendData.houses,
        lineStyle: {
          color: '#67C23A',
          width: 3
        },
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          opacity: 0.1,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67C23A' },
            { offset: 1, color: 'rgba(103, 194, 58, 0)' }
          ])
        }
      },
      {
        name: '成交量',
        type: 'line',
        smooth: true,
        data: trendData.transactions,
        lineStyle: {
          color: '#E6A23C',
          width: 3
        },
        itemStyle: {
          color: '#E6A23C'
        },
        areaStyle: {
          opacity: 0.1,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#E6A23C' },
            { offset: 1, color: 'rgba(230, 162, 60, 0)' }
          ])
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

/// 修改用户图表初始化函数
const initUserChart = () => {
  if (!userChartRef.value) return
  
  // 如果图表已存在，先销毁
  if (userChart) {
    userChart.dispose()
  }
  
  userChart = echarts.init(userChartRef.value)
  
  const option = {
    title: {
      text: '用户活跃度（24小时）',
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#303133'
      },
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#606266'
      }
    },
    legend: {
      data: ['活跃用户', '新增用户'],
      bottom: 20,
      textStyle: {
        color: '#606266'
      }
    },
    grid: {
      left: '8%',   // 增加左边距
      right: '8%',  // 增加右边距
      top: '20%',   // 增加上边距
      bottom: '20%', // 增加下边距
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: userActivityData.hours,
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisLabel: {
        color: '#909399',
        rotate: 45 // 旋转标签避免重叠
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '活跃用户',
        position: 'left',
        axisLine: {
          lineStyle: {
            color: '#409EFF'
          }
        },
        axisLabel: {
          color: '#909399'
        },
        splitLine: {
          lineStyle: {
            color: '#f5f7fa'
          }
        }
      },
      {
        type: 'value',
        name: '新增用户',
        position: 'right',
        axisLine: {
          lineStyle: {
            color: '#F56C6C'
          }
        },
        axisLabel: {
          color: '#909399'
        }
      }
    ],
    series: [
      {
        name: '活跃用户',
        type: 'bar',
        data: userActivityData.activeUsers,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#36CFC9' }
          ])
        },
        barWidth: '40%' // 调整柱状图宽度
      },
      {
        name: '新增用户',
        type: 'line',
        yAxisIndex: 1,
        data: userActivityData.newUsers,
        lineStyle: {
          color: '#F56C6C',
          width: 3
        },
        itemStyle: {
          color: '#F56C6C'
        },
        smooth: true,
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  }
  
  userChart.setOption(option)
// 确保图表适应容器大小
  setTimeout(() => {
    if (userChart) {
      userChart.resize()
    }
  }, 100)
}
// 添加标签页切换事件监听
const handleTabChange = (tabName: string) => {
  if (tabName === 'user') {
    // 当切换到用户报告标签页时，重新调整图表大小
    nextTick(() => {
      setTimeout(() => {
        if (userChart) {
          userChart.resize()
        }
      }, 100)
    })
  }
}
// 刷新图表
const refreshCharts = () => {
  // 模拟数据更新
  trendData.users = trendData.users.map(() => Math.floor(Math.random() * 100) + 20)
  trendData.houses = trendData.houses.map(() => Math.floor(Math.random() * 50) + 15)
  trendData.transactions = trendData.transactions.map(() => Math.floor(Math.random() * 30) + 5)
  
  userActivityData.activeUsers = userActivityData.activeUsers.map(() => Math.floor(Math.random() * 80) + 20)
  userActivityData.newUsers = userActivityData.newUsers.map(() => Math.floor(Math.random() * 15) + 1)
  
  // 重新渲染图表
  if (trendChart) {
    trendChart.setOption({
      series: [
        { data: trendData.users },
        { data: trendData.houses },
        { data: trendData.transactions }
      ]
    })
  }
  
  if (userChart) {
    userChart.setOption({
      series: [
        { data: userActivityData.activeUsers },
        { data: userActivityData.newUsers }
      ]
    })
  }
  
  ElMessage.success('图表数据已刷新')
}


onMounted(() => {
  loadDashboardData()
  
  // 初始化图表
  nextTick(() => {
    initTrendChart()
  })
  
  // 监听窗口大小变化
  const handleResize = () => {
    if (trendChart) {
      trendChart.resize()
    }
    if (userChart) {
      userChart.resize()
    }
  }
  
  window.addEventListener('resize', handleResize)
  
  // 组件卸载时清理
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (trendChart) {
      trendChart.dispose()
    }
    if (userChart) {
      userChart.dispose()
    }
  })
})



// 修改查看报告对话框中的用户图表初始化
// 保留这个完整的定义，删除另一个
// 修改显示报告对话框函数
const showReportDialog = () => {
  reportVisible.value = true
  
  // 等待对话框完全渲染后初始化图表
  nextTick(() => {
    setTimeout(() => {
      initUserChart()
      // 监听标签页切换
      setTimeout(() => {
        if (userChart) {
          userChart.resize()
        }
      }, 200)
    }, 300) // 增加延迟时间
  })
}

// 重置房源表单
const resetHouseForm = () => {
  if (houseFormRef.value) {
    houseFormRef.value.resetFields()
  }
  Object.assign(houseForm, {
    title: '',
    type: '',
    price: '',
    area: '',
    rooms: '',
    city: '',
    address: '',
    description: ''
  })
}

// 处理图片变化
const handleImageChange = (file: any) => {
  console.log('图片上传:', file)
  ElMessage.info('图片上传功能开发中...')
}

// 提交房源
const submitHouse = async () => {
  if (!houseFormRef.value) return
  
  try {
    const valid = await houseFormRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    
    // 模拟提交
    setTimeout(() => {
      submitting.value = false
      ElMessage.success('房源添加成功！')
      handleCloseAddHouse()
    }, 2000)
    
  } catch (error) {
    submitting.value = false
    console.error('表单验证失败:', error)
  }
}


// 关闭报告对话框
const handleCloseReport = () => {
  reportVisible.value = false
}

// 获取日志类型
const getLogType = (level: string) => {
  switch (level) {
    case 'ERROR': return 'danger'
    case 'WARN': return 'warning'
    case 'INFO': return 'info'
    default: return 'info'
  }
}

// 导出报告
const exportReport = () => {
  ElMessage.success('报告导出成功！')
}

const loadDashboardData = async () => {
  // 模拟数据加载
  recentHouses.value = [
    { title: '阳光花园三室两厅', price: '¥120万', status: 'pending' },
    { title: '中央公园精装修', price: '¥200万', status: 'approved' },
    { title: '市中心商圈住宅', price: '¥180万', status: 'pending' },
    { title: '湖景别墅', price: '¥350万', status: 'approved' }
  ]
  
  recentUsers.value = [
    { username: '张三', email: 'zhangsan@email.com', registerTime: '01-15' },
    { username: '李四', email: 'lisi@email.com', registerTime: '01-14' },
    { username: '王五', email: 'wangwu@email.com', registerTime: '01-13' },
    { username: '赵六', email: 'zhaoliu@email.com', registerTime: '01-12' }
  ]
}


</script>

<style scoped>
.dashboard {
  min-height: 100%;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #09aef4 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.welcome-right {
  display: flex;
  gap: 16px;
}

.welcome-right .el-button {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  height: 140px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.user-stat .stat-icon { background: linear-gradient(135deg, #409EFF, #36CFC9); }
.house-stat .stat-icon { background: linear-gradient(135deg, #67C23A, #52C41A); }
.pending-stat .stat-icon { background: linear-gradient(135deg, #E6A23C, #FAAD14); }
.admin-stat .stat-icon { background: linear-gradient(135deg, #F56C6C, #FF4D4F); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  gap: 4px;
}

.trend-up { color: #67C23A; }
.trend-down { color: #F56C6C; }
.trend-neutral { color: #909399; }

/* 内容区域 - 关键修改部分 */
.content-section {
  margin-bottom: 24px;
}

/* 统一两个卡片的高度 */
.chart-card, .activity-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  height: 420px; /* 设置固定高度 */
  display: flex;
  flex-direction: column;
}

/* 确保卡片头部高度一致 */
.chart-card :deep(.el-card__header),
.activity-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  height: 65px; /* 固定头部高度 */
  display: flex;
  align-items: center;
}

/* 卡片内容区域 */
.chart-card :deep(.el-card__body) {
  flex: 1;
  padding: 20px 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.activity-card :deep(.el-card__body) {
  flex: 1;
  padding: 20px 24px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: #409EFF;
  font-size: 18px;
}

.header-title {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

/* 图表区域 - 缩小高度 */
.chart-container {
  height: 320px; /* 从350px缩小到320px */
  width: 100%;
  flex: 1;
}

/* 活动列表 - 确保高度匹配 */
.activity-list {
  height: 320px; /* 与图表高度保持一致 */
  overflow-y: auto;
  padding-right: 6px; /* 为滚动条留出空间 */
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
}

.activity-item:hover {
  background-color: #f9f9f9;
  border-radius: 8px;
  margin: 0 -12px;
  padding: 16px 12px;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 16px;
  color: white;
  flex-shrink: 0;
}

.activity-icon.user {
  background-color: #e8f4ff;
  color: #409eff;
}

.activity-icon.house {
  background-color: #f0f9ff;
  color: #67c23a;
}

.activity-icon.system {
  background-color: #fef0e6;
  color: #e6a23c;
}

.activity-icon.admin {
  background-color: #fef0f0;
  color: #f56c6c;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 表格区域 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-table) {
  flex: 1;
  overflow: auto;
}

.pagination-section {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}

.modern-table {
  border-radius: 8px;
  overflow: hidden;
}

.table-title {
  font-weight: 500;
  color: #303133;
}

.price-text {
  font-weight: 600;
  color: #E6A23C;
}

.status-tag {
  border-radius: 4px;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  font-size: 12px;
  font-weight: 600;
}

.username {
  font-weight: 500;
  color: #303133;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.report-content {
  padding: 20px 0;
}

.report-content h4 {
  margin: 20px 0 16px 0;
  color: #303133;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder p {
  margin: 16px 0;
  font-size: 16px;
}

/* 上传组件样式 */
:deep(.upload-demo) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

/* 统计组件样式 */
:deep(.el-statistic) {
  text-align: center;
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 600;
}

/* 标签页样式 */
.report-tabs {
  margin-top: 20px;
}

/* 进度条样式 */
:deep(.el-progress-bar__outer) {
  background-color: #f5f7fa;
}

/* 图表容器样式 */
.user-chart-container {
  height: 400px;
  width: 100%;
  margin-top: 20px;
  min-height: 400px;
}

/* 模态框中的图表样式 */
.report-content .user-chart-container {
  height: 450px;
  width: 100%;
  margin-top: 20px;
}

/* 确保模态框图表适应容器 */
:deep(.el-dialog__body) .user-chart-container {
  width: 100% !important;
  height: 450px !important;
}

/* 图表加载状态 */
.chart-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 320px;
  color: #909399;
}

/* 活动列表滚动条样式 */
.activity-list::-webkit-scrollbar {
  width: 6px;
}

.activity-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.activity-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.activity-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .stats-section .el-col {
    margin-bottom: 16px;
  }
  
  .chart-card, .activity-card {
    height: auto;
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .welcome-section {
    padding: 24px;
  }
  
  .stat-card {
    padding: 20px;
    height: 120px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .content-section .el-col,
  .table-section .el-col {
    margin-bottom: 16px;
  }
  
  .chart-container,
  .activity-list {
    height: 280px;
  }
  
  .chart-card, .activity-card {
    height: auto;
    min-height: 350px;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f5f5f5;
}

:deep(.el-table tr:hover) {
  background: #f8fafc;
}

:deep(.el-button--text) {
  color: #409EFF;
  font-weight: 500;
}

:deep(.el-button--text:hover) {
  background: #ecf5ff;
}
</style>
