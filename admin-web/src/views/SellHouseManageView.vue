<template>
  <div class="sell-house-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">卖房审核</h2>
      <div class="header-stats">
        <el-tag type="warning" size="large">待审核: {{ pendingCount }}</el-tag>
        <el-tag type="success" size="large">已通过: {{ approvedCount }}</el-tag>
        <el-tag type="danger" size="large">已拒绝: {{ rejectedCount }}</el-tag>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房源标题">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入房源标题" 
            clearable
          />
        </el-form-item>

        <el-form-item label="用户ID">
          <el-input 
            v-model="searchForm.userId" 
            placeholder="请输入用户ID" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="审核状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择审核状态"
            clearable
          >
            <el-option label="全部状态" value="" />
            <el-option label="待审核" value="RECEIVED" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="价格范围">
          <el-select 
            v-model="searchForm.priceRange" 
            placeholder="请选择价格范围"
            clearable
          >
            <el-option label="不限价格" value="" />
            <el-option label="100万以下" value="0-100" />
            <el-option label="100-200万" value="100-200" />
            <el-option label="200-300万" value="200-300" />
            <el-option label="300-500万" value="300-500" />
            <el-option label="500万以上" value="500-999999" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序方式">
          <el-select 
            v-model="searchForm.sortBy" 
            placeholder="请选择排序方式"
            clearable
          >
            <el-option label="最新提交" value="time-desc" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="面积从小到大" value="area-asc" />
            <el-option label="面积从大到小" value="area-desc" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">
            搜索
          </el-button>
          <el-button @click="handleReset" :icon="RefreshRight">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-card class="table-card">
        <el-table :data="displaySellHouseList" style="width: 100%" v-loading="loading" :show-overflow-tooltip="true">
          <el-table-column prop="id" label="ID" width="60" />
          
          <el-table-column prop="title" label="房源标题" min-width="180" :show-overflow-tooltip="true" />
          
          <el-table-column prop="userId" label="用户ID" width="80" />
          
          <el-table-column prop="address" label="地址" min-width="150" :show-overflow-tooltip="true" />
          
          <el-table-column prop="price" label="总价" width="90">
            <template #default="scope">
              <span>{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="area" label="面积" width="80">
            <template #default="scope">
              {{ scope.row.area }}㎡
            </template>
          </el-table-column>
          
          <el-table-column prop="rooms" label="户型" width="100" />
          
          <el-table-column prop="status" label="审核状态" width="100">
            <template #default="scope">
              <span>{{ getStatusText(scope.row.status) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="createdAt" label="提交时间" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          
          <!-- 操作列 - 根据状态显示不同按钮 -->
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
               
                
                <!-- 待审核状态显示审核操作 -->
                <el-dropdown 
                  v-if="scope.row.status === 'RECEIVED'" 
                  :onCommand="getRowCommandHandler(scope.row)" 
                  trigger="click"
                >
                  <el-button type="info" size="small">
                    审核操作
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="approve" :icon="Check">
                        审核通过
                      </el-dropdown-item>
                      <el-dropdown-item command="reject" :icon="Close">
                        审核拒绝
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                
                <!-- 已通过和已拒绝状态显示删除按钮 -->
                <el-button 
                  v-else-if="scope.row.status === 'APPROVED' || scope.row.status === 'REJECTED'"
                  type="danger" 
                  size="small" 
                  @click="handleDelete(scope.row)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="房源详情" 
      width="1000px"
      :close-on-click-modal="false"
      class="house-detail-dialog"
    >
      <div class="dialog-content">
        <!-- 基本信息区域 -->
        <div class="info-section">
          <h3 class="section-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">房源ID</span>
              <span class="info-value">{{ currentHouse.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ currentHouse.userId }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">房源标题</span>
              <span class="info-value">{{ currentHouse.title }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">房源地址</span>
              <span class="info-value">{{ currentHouse.address }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">总价</span>
              <span class="info-value">{{ currentHouse.price }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">面积</span>
              <span class="info-value">{{ currentHouse.area }}㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">单价</span>
              <span class="info-value">{{ currentHouse.unitPrice }}元/㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">户型</span>
              <span class="info-value">{{ currentHouse.rooms }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">楼层</span>
              <span class="info-value">{{ currentHouse.floor }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">建造年份</span>
              <span class="info-value">{{ currentHouse.buildYear }}年</span>
            </div>
            <div class="info-item">
              <span class="info-label">朝向</span>
              <span class="info-value">{{ currentHouse.orientation }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">装修情况</span>
              <span class="info-value">{{ currentHouse.decoration }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">审核状态</span>
              <span class="info-value">{{ getStatusText(currentHouse.status) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">提交时间</span>
              <span class="info-value">{{ formatDateTime(currentHouse.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ formatDateTime(currentHouse.updatedAt) }}</span>
            </div>
            <div class="info-item full-width" v-if="currentHouse.reason">
              <span class="info-label">拒绝原因</span>
              <span class="info-value error-text">{{ currentHouse.reason }}</span>
            </div>
          </div>
        </div>

        <!-- 房源标签区域 -->
        <div class="info-section">
          <h3 class="section-title">房源标签</h3>
          <div class="tags-container">
            <el-tag 
              v-for="tag in parseHouseTags(currentHouse.tag)" 
              :key="tag" 
              class="house-tag"
            >
              {{ tag }}
            </el-tag>
            <div v-if="!currentHouse.tag" class="empty-state">暂无标签</div>
          </div>
        </div>

        <!-- 房源图片区域 -->
        <div class="info-section">
          <h3 class="section-title">房源图片</h3>
          <div class="images-container">
            <div v-if="!currentHouse.image" class="empty-state">暂无图片</div>
            <div v-else class="image-gallery">
              <el-image
                v-for="(img, index) in parseHouseImages(currentHouse.image)"
                :key="index"
                :src="img"
                :preview-src-list="parseHouseImages(currentHouse.image)"
                :initial-index="index"
                fit="cover"
                class="house-image"
              />
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog 
      v-model="auditDialogVisible" 
      :title="auditDialogTitle" 
      width="500px"
      class="audit-dialog"
    >
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.action === 'approve' ? 'success' : 'danger'" size="large">
            {{ auditForm.action === 'approve' ? '通过审核' : '拒绝审核' }}
          </el-tag>
        </el-form-item>
        <el-form-item 
          v-if="auditForm.action === 'reject'" 
          label="拒绝原因" 
          :rules="[{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]"
        >
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入审核不通过的具体原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-else label="审核备注">
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="2"
            placeholder="可选择填写审核备注..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button 
            :type="auditForm.action === 'approve' ? 'success' : 'danger'" 
            @click="confirmAudit"
          >
            确认{{ auditForm.action === 'approve' ? '通过' : '拒绝' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Check, Close, Delete, Search, RefreshRight } from '@element-plus/icons-vue'
import axios from 'axios'

// 定义响应数据类型
interface HouseRecordResponse {
  code: string
  message: string
  data: HouseRecord[]
  timestamp: number
}

// 定义审核结果响应类型
interface AuditResponse {
  code: string
  message: string
  data: any
  timestamp: number
}

// 定义房源记录接口
interface HouseRecord {
  id: number
  userId: number
  title: string
  address: string
  price: number
  area: number
  unitPrice: number
  rooms: string
  floor: string
  buildYear: number
  orientation: string
  decoration: string
  tag: string
  image: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RECEIVED'
  reason?: string
  createdAt: string
  updatedAt: string
}

const loading = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)

// 统计数据
const pendingCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)

// 时间格式化函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    return dateStr.split('T')[0]
  } catch {
    return dateStr
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return dateStr
  }
}

// 搜索表单
const searchForm = reactive({
  title: '',
  userId: '',
  status: '',
  priceRange: '',
  sortBy: 'time-desc'
})

// 审核表单
const auditForm = reactive({
  action: 'approve' as 'approve' | 'reject',
  reason: ''
})

const currentHouse = ref<HouseRecord>({} as HouseRecord)
const currentAuditHouse = ref<HouseRecord>({} as HouseRecord)

// 数据管理 - 分离原始数据和显示数据
const originalSellHouseList = ref<HouseRecord[]>([])  // 原始完整数据
const displaySellHouseList = ref<HouseRecord[]>([])   // 当前显示的数据

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 审核对话框标题
const auditDialogTitle = computed(() => {
  switch (auditForm.action) {
    case 'approve': return '审核通过'
    case 'reject': return '审核拒绝'
    default: return '审核操作'
  }
})

// 搜索筛选函数
const filterSellHouses = (data: HouseRecord[]) => {
  console.log('🔍 搜索条件:', searchForm)
  
  return data.filter(item => {
    // 房源标题筛选
    if (searchForm.title && searchForm.title.trim()) {
      const searchTitle = searchForm.title.trim().toLowerCase()
      const itemTitle = item.title.toLowerCase()
      if (!itemTitle.includes(searchTitle)) {
        return false
      }
    }
    
    // 用户ID筛选
    if (searchForm.userId && searchForm.userId.trim()) {
      const searchUserId = searchForm.userId.trim()
      const itemUserId = item.userId.toString()
      if (!itemUserId.includes(searchUserId)) {
        return false
      }
    }
    
    // 审核状态筛选
    if (searchForm.status && searchForm.status.trim()) {
      if (item.status !== searchForm.status) {
        return false
      }
    }
    
    // 价格范围筛选
    if (searchForm.priceRange && searchForm.priceRange.trim()) {
      const { minPrice, maxPrice } = parsePriceRange(searchForm.priceRange)
      
      if (minPrice !== null && item.price < minPrice) {
        return false
      }
      if (maxPrice !== null && item.price > maxPrice) {
        return false
      }
    }
    
    return true
  })
}

// 排序函数
const sortSellHouses = (data: HouseRecord[]) => {
  if (!searchForm.sortBy) return data
  
  const sortedData = [...data]
  
  switch (searchForm.sortBy) {
    case 'time-desc':
      return sortedData.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    case 'price-asc':
      return sortedData.sort((a, b) => a.price - b.price)
    case 'price-desc':
      return sortedData.sort((a, b) => b.price - a.price)
    case 'area-asc':
      return sortedData.sort((a, b) => a.area - b.area)
    case 'area-desc':
      return sortedData.sort((a, b) => b.area - a.area)
    default:
      return sortedData
  }
}

// 分页处理函数
const updatePagination = (filteredData: HouseRecord[]) => {
  // 先排序
  const sortedData = sortSellHouses(filteredData)
  
  // 计算分页
  const startIndex = (pagination.page - 1) * pagination.size
  const endIndex = startIndex + pagination.size
  
  // 更新显示数据和总数
  displaySellHouseList.value = sortedData.slice(startIndex, endIndex)
  pagination.total = sortedData.length
  
  console.log('📄 分页处理:', {
    当前页: pagination.page,
    每页数量: pagination.size,
    总数: pagination.total,
    当前显示: displaySellHouseList.value.length
  })
}

// 统计数据更新函数
const updateStatistics = (data: HouseRecord[]) => {
  pendingCount.value = data.filter(item => item.status === 'RECEIVED').length
  approvedCount.value = data.filter(item => item.status === 'APPROVED').length
  rejectedCount.value = data.filter(item => item.status === 'REJECTED').length
}

// 解析价格范围
const parsePriceRange = (priceRange: string) => {
  if (!priceRange) return { minPrice: null, maxPrice: null }
  
  const [min, max] = priceRange.split('-').map(p => parseInt(p))
  
  if (max >= 999999) {
    return { minPrice: min, maxPrice: null }
  }
  
  return { minPrice: min, maxPrice: max }
}

// 解析房源标签
const parseHouseTags = (tagStr: string) => {
  if (!tagStr) return []
  return tagStr.split(',').filter(tag => tag.trim())
}

// 解析房源图片
const parseHouseImages = (imageStr: string) => {
  if (!imageStr) return []
  try {
    return JSON.parse(imageStr)
  } catch {
    return imageStr.split(',').filter(img => img.trim())
  }
}

// 命令处理 - approve和reject
const handleCommand = (command: string, row?: HouseRecord) => {
  if (!row) return
  
  switch (command) {
    case 'approve':
      handleAudit(row, 'approve')
      break
    case 'reject':
      handleAudit(row, 'reject')
      break
  }
}

const getRowCommandHandler = (row: HouseRecord) => {
  return (command: string) => {
    handleCommand(command, row)
  }
}

// 状态处理函数
const getStatusText = (status: string) => {
  switch (status) {
    // case 'RECEIVED': return '已接收'
    case 'RECEIVED': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    default: return '未知'
  }
}

// 加载房源列表 - 使用真实API
const loadSellHouseList = async () => {
  loading.value = true
  try {
    // 调用真实API获取数据
    const response = await axios.get<HouseRecordResponse>('/api/admin/review/house/received')
    
    if (response.data && response.data.code === 'SUCCESS') {
      // 更新原始数据
      originalSellHouseList.value = response.data.data
      
      // 执行搜索和分页
      const filteredData = filterSellHouses(originalSellHouseList.value)
      updatePagination(filteredData)
      updateStatistics(originalSellHouseList.value)
      
      console.log('✅ 数据加载完成:', {
        原始数据: originalSellHouseList.value.length,
        过滤后数据: filteredData.length,
        当前显示: displaySellHouseList.value.length
      })
    } else {
      throw new Error(response.data?.message || '获取数据失败')
    }
  } catch (error) {
    console.error('❌ 加载失败:', error)
    ElMessage.error('加载房源列表失败')
  } finally {
    loading.value = false
  }
}

const handleView = (row: HouseRecord) => {
  currentHouse.value = row
  detailDialogVisible.value = true
}

// 审核处理函数
const handleAudit = (row: HouseRecord, action: 'approve' | 'reject') => {
  currentAuditHouse.value = row
  auditForm.action = action
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 删除处理函数 - 使用真实API
const handleDelete = async (row: HouseRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除房源"${row.title}"吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 发送删除请求到后端
    const response = await axios.delete(`/api/admin/review/house/${row.id}`)
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('删除成功')
      // 重新加载数据
      await loadSellHouseList()
    } else {
      throw new Error(response.data?.message || '删除失败')
    }
  } catch (error: any) {
    if (error.toString().includes('cancel')) {
      ElMessage.info('已取消删除')
    } else {
      console.error('❌ 删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}
// 确认审核 - 修改为使用正确的API
const confirmAudit = async () => {
  try {
    if (auditForm.action === 'reject' && !auditForm.reason.trim()) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    
    // 构建请求参数 - 适配后端API
    const params = new URLSearchParams()
    params.append('id', currentAuditHouse.value.id.toString())
    params.append('pass', (auditForm.action === 'approve').toString())
    
    // 拒绝时必须有理由
    if (auditForm.action === 'reject') {
      params.append('reason', auditForm.reason)
    } else if (auditForm.reason.trim()) {
      // 通过且有备注时添加备注
      params.append('reason', auditForm.reason)
    }
    
    // 发送审核结果到后端 - 使用正确的API地址
    const response = await axios.post<AuditResponse>('/api/admin/review/house/approve', params)
    
    if (response.data && response.data.code === 'SUCCESS') {
      const actionText = auditForm.action === 'approve' ? '审核通过' : '审核拒绝'
      ElMessage.success(`${actionText}成功`)
      auditDialogVisible.value = false
      
      // 重新加载数据
      await loadSellHouseList()
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error: any) {
    console.error('❌ 审核操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 搜索处理
const handleSearch = async () => {
  console.log('🔍 执行搜索...')
  pagination.page = 1  // 重置到第一页
  
  // 使用前端筛选
  const filteredData = filterSellHouses(originalSellHouseList.value)
  updatePagination(filteredData)
  
  const filteredCount = filteredData.length
  if (filteredCount === 0) {
    ElMessage.info('未找到符合条件的房源')
  } else {
    ElMessage.success(`找到 ${filteredCount} 条符合条件的房源`)
  }
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    userId: '',
    status: '',
    priceRange: '',
    sortBy: 'time-desc'
  })
  pagination.page = 1
  
  // 使用前端筛选
  const filteredData = filterSellHouses(originalSellHouseList.value)
  updatePagination(filteredData)
  
  ElMessage.info('搜索条件已重置')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  
  // 使用前端分页
  const filteredData = filterSellHouses(originalSellHouseList.value)
  updatePagination(filteredData)
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  
  // 使用前端分页
  const filteredData = filterSellHouses(originalSellHouseList.value)
  updatePagination(filteredData)
}

onMounted(() => {
  loadSellHouseList()
})
</script>

<style scoped>
.sell-house-manage-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

/* 搜索表单样式 - 和预约管理页面一致 */
:deep(.el-form--inline) {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: 20px 24px;
  padding: 4px 0;
}

:deep(.el-form--inline .el-form-item) {
  margin: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
  flex-shrink: 0;
}

:deep(.el-form--inline .el-form-item:last-child) {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: flex-start;
  gap: 12px;
}

:deep(.el-form--inline .el-form-item:last-child .el-form-item__content) {
  margin: 0;
  height: auto;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  margin-bottom: 8px !important;
  padding: 0 !important;
  line-height: 1.4;
  height: auto !important;
  width: auto !important;
  text-align: left;
  display: block;
  white-space: nowrap;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: flex;
  align-items: center;
  height: 40px;
}

:deep(.el-input),
:deep(.el-select) {
  width: 160px !important;
}

:deep(.el-input .el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  height: 32px; 
}

:deep(.el-input .el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input .el-input__wrapper.is-focus),
:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
}

:deep(.el-input .el-input__inner),
:deep(.el-select .el-input__inner) {
  font-size: 13px;
  color: #606266;
  height: 34px;
  line-height: 34px;
}

:deep(.el-input .el-input__inner) {
  padding: 0 12px;
}

:deep(.el-select .el-input__inner) {
  padding: 0 28px 0 12px;
}

/* 按钮样式 */
:deep(.el-button) {
  height: 36px;
  padding: 0 20px;
  font-size: 13px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin: 0;
  flex-shrink: 0;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #4dabf7 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.25);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.35);
}

:deep(.el-button--default) {
  background: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

:deep(.el-button--default:hover) {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
}

.table-section {
  flex: 1;
  overflow: hidden;
  margin-bottom: 20px;
}

.table-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: nowrap;
}

.action-buttons :deep(.el-button) {
  flex-shrink: 0;
  white-space: nowrap;
}

.action-buttons :deep(.el-dropdown .el-button) {
  min-width: 80px;
}

/* 优化审核对话框样式 */
.audit-dialog :deep(.el-dialog) {
  border-radius: 12px;
  max-height: 70vh;
}

.audit-dialog :deep(.el-dialog__body) {
  max-height: 40vh;
  overflow-y: auto;
  padding: 20px 24px;
}

/* 优化文本域样式 */
:deep(.el-textarea .el-textarea__inner) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  font-size: 13px;
  line-height: 1.5;
  padding: 12px;
  resize: vertical;
  min-height: 60px;
}

/* 详情对话框样式 */
.house-detail-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-dialog__body) {
    flex: 1;
    overflow: hidden;
    padding: 0;
  }
}

.dialog-content {
  height: 70vh;
  overflow-y: auto;
  padding: 20px 24px;
}

.info-section {
  margin-bottom: 32px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
  align-items: start;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
}

.error-text {
  color: #f56c6c;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: flex-start;
}

.house-tag {
  margin: 0;
  font-size: 12px;
}

.images-container {
  min-height: 120px;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.house-image {
  width: 100%;
  height: 100px;
  border-radius: 8px;
  border: 2px solid #ebeef5;
  transition: all 0.3s ease;
  cursor: pointer;
}

.house-image:hover {
  border-color: #409eff;
  transform: scale(1.02);
}

.empty-state {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
}

/* 优化对话框底部按钮区域 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

/* 滚动条样式 */
.dialog-content::-webkit-scrollbar,
:deep(.el-table__body-wrapper)::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.dialog-content::-webkit-scrollbar-track,
:deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb,
:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb:hover,
:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-form--inline) {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  :deep(.el-form--inline .el-form-item) {
    width: 100%;
  }
  
  :deep(.el-input),
  :deep(.el-select) {
    width: 100% !important;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-stats {
    flex-wrap: wrap;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
    align-items: stretch;
  }
  
  .action-buttons :deep(.el-button) {
    width: 100%;
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .image-gallery {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
  
  .house-image {
    height: 80px;
  }
}
</style>