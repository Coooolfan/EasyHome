<template>
  <div class="sell-house-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">卖房管理</h2>
      <div class="header-stats">
        <el-tag type="warning" size="large">待审核: {{ pendingCount }}</el-tag>
        <el-tag type="success" size="large">已通过: {{ approvedCount }}</el-tag>
        <el-tag type="danger" size="large">已拒绝: {{ rejectedCount }}</el-tag>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-row :gutter="16" class="search-row">
            <el-col :span="4">
              <el-form-item label="房源标题" class="form-item-custom">
                <el-input 
                  v-model="searchForm.title" 
                  placeholder="请输入房源标题" 
                  clearable
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="4">
              <el-form-item label="审核状态" class="form-item-custom">
                <el-select 
                  v-model="searchForm.status" 
                  placeholder="请选择审核状态"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="全部状态" value="" />
                  <el-option label="待审核" value="PENDING" />
                  <el-option label="已通过" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="4">
              <el-form-item label="价格范围" class="form-item-custom">
                <el-select 
                  v-model="searchForm.priceRange" 
                  placeholder="请选择价格范围"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="不限价格" value="" />
                  <el-option label="100万以下" value="0-100" />
                  <el-option label="100-200万" value="100-200" />
                  <el-option label="200-300万" value="200-300" />
                  <el-option label="300-500万" value="300-500" />
                  <el-option label="500万以上" value="500-999999" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="排序方式" class="form-item-custom">
                <el-select 
                  v-model="searchForm.sortBy" 
                  placeholder="请选择排序方式"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="最新提交" value="time-desc" />
                  <el-option label="价格从低到高" value="price-asc" />
                  <el-option label="价格从高到低" value="price-desc" />
                  <el-option label="面积从小到大" value="area-asc" />
                  <el-option label="面积从大到小" value="area-desc" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item class="button-group">
                <div class="button-container">
                  <el-button type="primary" @click="handleSearch" :icon="Search" class="search-btn">
                    搜索
                  </el-button>
                  <el-button @click="handleReset" :icon="RefreshRight" class="reset-btn">
                    重置
                  </el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-card class="table-card">
        <el-table :data="sellHouseList" style="width: 100%" v-loading="loading" :show-overflow-tooltip="true">
          <el-table-column prop="id" label="ID" width="60" />
          
          <el-table-column prop="title" label="房源标题" min-width="180" :show-overflow-tooltip="true" />
          
          <el-table-column prop="address" label="地址" min-width="150" :show-overflow-tooltip="true" />
          
          <el-table-column prop="price" label="总价" width="90">
            <template #default="scope">
              <span class="price">{{ scope.row.price }}万</span>
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
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="created_at" label="提交时间" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.created_at) }}
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleView(scope.row)"
                >
                  查看详情
                </el-button>
                <el-dropdown :onCommand="getRowCommandHandler(scope.row)" trigger="click">
                  <el-button type="info" size="small">
                    审核操作
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        v-if="scope.row.status === 'PENDING'"
                        command="approve"
                        :icon="Check"
                      >
                        审核通过
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.status === 'PENDING'"
                        command="reject"
                        :icon="Close"
                      >
                        审核拒绝
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.status === 'APPROVED'"
                        command="disable"
                        :icon="Lock"
                        divided
                      >
                        禁用房源
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.status === 'DISABLED'"
                        command="enable"
                        :icon="Unlock"
                      >
                        启用房源
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
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
    <el-dialog v-model="detailDialogVisible" title="房源详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房源ID">{{ currentHouse.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentHouse.user_id }}</el-descriptions-item>
        <el-descriptions-item label="房源标题" :span="2">{{ currentHouse.title }}</el-descriptions-item>
        <el-descriptions-item label="房源地址" :span="2">{{ currentHouse.address }}</el-descriptions-item>
        <el-descriptions-item label="总价">{{ currentHouse.price }}万</el-descriptions-item>
        <el-descriptions-item label="面积">{{ currentHouse.area }}㎡</el-descriptions-item>
        <el-descriptions-item label="单价">{{ currentHouse.unit_price }}元/㎡</el-descriptions-item>
        <el-descriptions-item label="户型">{{ currentHouse.rooms }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ currentHouse.floor }}</el-descriptions-item>
        <el-descriptions-item label="建造年份">{{ currentHouse.build_year }}年</el-descriptions-item>
        <el-descriptions-item label="朝向">{{ currentHouse.orientation }}</el-descriptions-item>
        <el-descriptions-item label="装修情况">{{ currentHouse.decoration }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusType(currentHouse.status)">
            {{ getStatusText(currentHouse.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentHouse.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentHouse.updated_at) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentHouse.reason" label="拒绝原因" :span="2">
          <el-text type="danger">{{ currentHouse.reason }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>房源标签</el-divider>
      <div class="house-tags">
        <el-tag 
          v-for="tag in parseHouseTags(currentHouse.tag)" 
          :key="tag" 
          style="margin-right: 8px; margin-bottom: 8px;"
        >
          {{ tag }}
        </el-tag>
        <el-empty v-if="!currentHouse.tag" description="暂无标签" />
      </div>
      
      <el-divider>房源图片</el-divider>
      <div class="house-images">
        <el-empty description="暂无图片" v-if="!currentHouse.image" />
        <div v-else class="image-preview">
          <el-image
            v-for="(img, index) in parseHouseImages(currentHouse.image)"
            :key="index"
            :src="img"
            :preview-src-list="parseHouseImages(currentHouse.image)"
            :initial-index="index"
            fit="cover"
            style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px; border-radius: 4px;"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" :title="auditDialogTitle" width="500px">
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
            :rows="4"
            placeholder="请输入审核不通过的具体原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-else label="审核备注">
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="3"
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
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { ArrowDown, Check, Close, Lock, Unlock, Search, RefreshRight,Plus, House } from '@element-plus/icons-vue'


interface SellHouse {
  id: number
  user_id: number
  title: string
  address: string
  price: number
  area: number
  unit_price: number
  rooms: string
  floor: string
  build_year: number
  orientation: string
  decoration: string
  tag: string
  image: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED'
  reason?: string
  created_at: string
  updated_at: string
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
  return dateStr.split(' ')[0]
}

const formatDateTime = (dateStr: string) => {
  return dateStr || ''
}

// 搜索表单
const searchForm = reactive({
  title: '',
  status: '',
  priceRange: '',
  sortBy: 'time-desc'
})

const auditForm = reactive({
  action: 'approve' as 'approve' | 'reject' | 'disable' | 'enable',
  reason: ''
})

const currentHouse = ref<SellHouse>({} as SellHouse)
const currentAuditHouse = ref<SellHouse>({} as SellHouse)
const sellHouseList = ref<SellHouse[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const auditDialogTitle = computed(() => {
  switch (auditForm.action) {
    case 'approve': return '审核通过'
    case 'reject': return '审核拒绝'
    case 'disable': return '禁用房源'
    case 'enable': return '启用房源'
    default: return '审核操作'
  }
})

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
    // 尝试解析 JSON 格式
    return JSON.parse(imageStr)
  } catch {
    // 如果不是 JSON，按逗号分割
    return imageStr.split(',').filter(img => img.trim())
  }
}

// 命令处理
const handleCommand = (command: string, row?: SellHouse) => {
  if (!row) return
  
  switch (command) {
    case 'approve':
      handleAudit(row, 'approve')
      break
    case 'reject':
      handleAudit(row, 'reject')
      break
    case 'disable':
      handleAudit(row, 'disable')
      break
    case 'enable':
      handleAudit(row, 'enable')
      break
  }
}

const getRowCommandHandler = (row: SellHouse) => {
  return (command: string) => {
    handleCommand(command, row)
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'DISABLED': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    case 'DISABLED': return '已禁用'
    default: return '未知'
  }
}

const loadSellHouseList = async () => {
  loading.value = true
  try {
    // 模拟数据
    const mockData: SellHouse[] = [
      {
        id: 1,
        user_id: 1001,
        title: '阳光花园精装三居室',
        address: '北京市朝阳区望京街道阳光花园小区',
        price: 650,
        area: 120,
        unit_price: 54167,
        rooms: '三室两厅',
        floor: '中层/共18层',
        build_year: 2015,
        orientation: '南北',
        decoration: '精装修',
        tag: '学区房,近地铁,南北通透',
        image: '["https://example.com/img1.jpg","https://example.com/img2.jpg"]',
        status: 'PENDING',
        created_at: '2024-01-15 10:30:00',
        updated_at: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        user_id: 1002,
        title: '中央公园豪华四居室',
        address: '北京市海淀区中关村大街中央公园',
        price: 1200,
        area: 180,
        unit_price: 66667,
        rooms: '四室两厅',
        floor: '高层/共25层',
        build_year: 2018,
        orientation: '南',
        decoration: '豪华装修',
        tag: '景观房,高端社区,配套齐全',
        image: '["https://example.com/img3.jpg"]',
        status: 'APPROVED',
        created_at: '2024-01-14 14:20:00',
        updated_at: '2024-01-14 16:45:00'
      },
      {
        id: 3,
        user_id: 1003,
        title: '老旧小区两居室',
        address: '北京市西城区老胡同小区',
        price: 300,
        area: 80,
        unit_price: 37500,
        rooms: '两室一厅',
        floor: '低层/共6层',
        build_year: 1990,
        orientation: '东',
        decoration: '简装修',
        tag: '老房子,需要翻新',
        image: '',
        status: 'REJECTED',
        reason: '房源信息不完整，缺少详细描述和图片',
        created_at: '2024-01-13 09:15:00',
        updated_at: '2024-01-13 11:30:00'
      }
    ]
    
    sellHouseList.value = mockData
    pagination.total = mockData.length
    
    // 更新统计数据
    pendingCount.value = mockData.filter(item => item.status === 'PENDING').length
    approvedCount.value = mockData.filter(item => item.status === 'APPROVED').length
    rejectedCount.value = mockData.filter(item => item.status === 'REJECTED').length
    
  } catch (error) {
    ElMessage.error('加载房源列表失败')
  } finally {
    loading.value = false
  }
}

const handleView = (row: SellHouse) => {
  currentHouse.value = row
  detailDialogVisible.value = true
}

const handleAudit = (row: SellHouse, action: 'approve' | 'reject' | 'disable' | 'enable') => {
  currentAuditHouse.value = row
  auditForm.action = action
  auditForm.reason = ''
  auditDialogVisible.value = true
}

const confirmAudit = async () => {
  try {
    if (auditForm.action === 'reject' && !auditForm.reason.trim()) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    
    const actionText = {
      approve: '审核通过',
      reject: '审核拒绝',
      disable: '禁用房源',
      enable: '启用房源'
    }[auditForm.action]
    
    ElMessage.success(`${actionText}成功`)
    auditDialogVisible.value = false
    loadSellHouseList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSearch = async () => {
  pagination.page = 1
  loading.value = true
  
  try {
    const queryParams = {
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title || undefined,
      status: searchForm.status || undefined,
      sortBy: searchForm.sortBy || 'time-desc',
      ...parsePriceRange(searchForm.priceRange)
    }
    
    console.log('搜索参数:', queryParams)
    await loadSellHouseList()
    
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    status: '',
    priceRange: '',
    sortBy: 'time-desc'
  })
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadSellHouseList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadSellHouseList()
}

onMounted(() => {
  loadSellHouseList()
})
</script>

<style scoped>
/* 继承之前的样式，这里只添加特殊的样式 */
.sell-house-manage-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  flex-shrink: 0;
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

.search-section {
  flex-shrink: 0;
  margin-bottom: 20px;
}

.search-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-form {
  margin: 0;
}

.search-row {
  margin-bottom: 0;
  align-items: flex-end;
}

.form-item-custom {
  margin-bottom: 0;
  width: 100%;
}

.form-item-custom :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
  margin-bottom: 8px;
  display: block;
  text-align: left;
  line-height: 1.2;
  height: auto;
  padding: 0;
}

.form-item-custom :deep(.el-form-item__content) {
  flex: 1;
  line-height: 1;
  margin: 0;
}

.button-group {
  margin-bottom: 0;
}

.button-group :deep(.el-form-item__content) {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  height: 34px;
  margin: 0;
}

.button-container {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 34px;
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

.price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.house-tags {
  min-height: 40px;
  padding: 8px 0;
}

.house-images {
  min-height: 120px;
  padding: 8px 0;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 输入框和选择框统一样式 */
:deep(.el-input) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    border-radius: 6px;
    height: 34px;
  }
  
  .el-input__inner {
    font-size: 13px;
    height: 34px;
    line-height: 34px;
    border: none;
    background: transparent;
    padding: 0 12px;
    color: #606266;
  }
}

:deep(.el-select) {
  width: 100%;
  min-width: 120px;
  
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    border-radius: 6px;
    height: 34px;
  }
  
  .el-input__inner {
    font-size: 13px;
    height: 34px;
    line-height: 34px;
    border: none;
    background: transparent;
    padding: 0 24px 0 12px !important;
    color: #606266;
  }
}

:deep(.el-button) {
  height: 34px;
  font-size: 13px;
  border-radius: 6px;
  padding: 0 16px;
}

.search-btn {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.reset-btn {
  background: #fff;
  border-color: #dcdfe6;
  color: #606266;
}
</style>
