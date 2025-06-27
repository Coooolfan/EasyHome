<template>
  <div class="house-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">房源管理</h2>
      <div class="header-stats">
        <el-tag type="primary" size="large">总房源: {{ pagination.total }}</el-tag>
        <el-button type="primary" @click="handleAdd" :icon="Plus">
          新增房源
        </el-button>
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
        
        <el-form-item label="户型">
          <el-select 
            v-model="searchForm.houseType" 
            placeholder="请选择户型"
            clearable
          >
            <el-option label="不限户型" value="" />
            <el-option label="1室" value="1室" />
            <el-option label="2室" value="2室" />
            <el-option label="3室" value="3室" />
            <el-option label="4室及以上" value="4室+" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布者">
          <el-input 
            v-model="searchForm.publisherName" 
            placeholder="请输入发布者" 
            clearable
          />
        </el-form-item>

        <el-form-item label="排序方式">
          <el-select 
            v-model="searchForm.sortBy" 
            placeholder="请选择排序方式"
            clearable
          >
            <el-option label="最新发布" value="time-desc" />
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
        <!-- 🔧 调整表格列宽，确保操作列不被遮挡 -->
        <!-- 🔧 移除固定列，调整表格布局 -->
<!-- 🔧 优化表格列宽分配，充分利用容器宽度 -->
<el-table :data="displayHouseList" style="width: 100%" v-loading="loading" :show-overflow-tooltip="true">
  <el-table-column prop="id" label="ID" width="60" />
  
  <!-- 🔧 房源标题列使用min-width让其自适应 -->
  <el-table-column prop="title" label="房源标题" min-width="250" :show-overflow-tooltip="true" />
  
  <el-table-column prop="price" label="总价" width="100" align="right">
    <template #default="scope">
      <span>{{ formatPrice(scope.row.price) }}万</span>
    </template>
  </el-table-column>
  
  <el-table-column prop="area" label="面积" width="80" align="center">
    <template #default="scope">
      {{ scope.row.area }}㎡
    </template>
  </el-table-column>
  
  <!-- 🔧 位置列使用min-width让其自适应 -->
  <el-table-column prop="location" label="位置" min-width="150" :show-overflow-tooltip="true" />
  
  <el-table-column prop="houseType" label="户型" width="90" />
  
  <el-table-column prop="publisherName" label="发布者" width="90" :show-overflow-tooltip="true" />
  
  <el-table-column prop="publishTime" label="发布时间" width="110" align="center">
    <template #default="scope">
      {{ formatDate(scope.row.publishTime) }}
    </template>
  </el-table-column>
  
  <!-- 🔧 操作列固定宽度 -->
  <!-- 操作列宽度调整为180px -->
<el-table-column label="操作" width="180">
  <template #default="scope">
    <div class="action-buttons">
      <el-button 
        type="primary" 
        size="small" 
        @click="handleEdit(scope.row)"
        :icon="Edit"
      >
        编辑
      </el-button>
      <el-button 
        type="info" 
        size="small" 
        @click="handleView(scope.row)"
        :icon="View"
      >
        详情
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

    <!-- 🔧 优化新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="resetForm"
      class="house-form-dialog"
    >
      <el-form
        ref="formRef"
        :model="houseForm"
        :rules="rules"
        label-width="80px"
        class="house-form"
      >
        <!-- 房源标题 -->
        <el-form-item label="房源标题" prop="title">
          <el-input v-model="houseForm.title" placeholder="请输入房源标题" />
        </el-form-item>

        <!-- 第一行：价格和面积 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <!-- 🔧 修复数字输入框显示问题 -->
              <el-input-number 
                v-model="houseForm.price" 
                :min="0" 
                :precision="1"
                :step="10"
                placeholder="万元"
                style="width: 100%"
                :controls="false"
              >
                <template #append>万元</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积" prop="area">
              <!-- 🔧 修复数字输入框显示问题 -->
              <el-input-number 
                v-model="houseForm.area" 
                :min="0" 
                :step="1"
                placeholder="平方米"
                style="width: 100%"
                :controls="false"
              >
                <template #append>㎡</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：房型和位置 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房型" prop="houseType">
              <el-select v-model="houseForm.houseType" placeholder="请选择房型" style="width: 100%">
                <el-option label="一室一厅" value="1室1厅" />
                <el-option label="两室一厅" value="2室1厅" />
                <el-option label="三室两厅" value="3室2厅" />
                <el-option label="四室两厅" value="4室2厅" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置" prop="location">
              <el-input v-model="houseForm.location" placeholder="请输入位置" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：发布者和联系电话 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布者" prop="publisherName">
              <el-input v-model="houseForm.publisherName" placeholder="请输入发布者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="houseForm.contactPhone" placeholder="请输**系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 房源描述 -->
        <el-form-item label="房源描述" prop="description">
          <el-input 
            v-model="houseForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入房源详细描述..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <!-- 🔧 编辑时显示删除按钮 -->
          <div class="footer-left">
            <el-button 
              v-if="isEdit" 
              type="danger" 
              @click="handleDelete(houseForm)"
              :icon="Delete"
            >
              删除
            </el-button>
          </div>
          <div class="footer-right">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">
              {{ isEdit ? '更新' : '新增' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="房源详情" 
      width="800px"
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
              <span class="info-label">发布者</span>
              <span class="info-value">{{ currentHouse.publisherName }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">房源标题</span>
              <span class="info-value">{{ currentHouse.title }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">总价</span>
              <span class="info-value">{{ formatPrice(currentHouse.price) }}万</span>
            </div>
            <div class="info-item">
              <span class="info-label">面积</span>
              <span class="info-value">{{ currentHouse.area }}㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">单价</span>
              <span class="info-value">{{ calculateUnitPrice(currentHouse.price, currentHouse.area) }}元/㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">户型</span>
              <span class="info-value">{{ currentHouse.houseType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">位置</span>
              <span class="info-value">{{ currentHouse.location }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ currentHouse.contactPhone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间</span>
              <span class="info-value">{{ formatDateTime(currentHouse.publishTime) }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">房源描述</span>
              <span class="info-value">{{ currentHouse.description || '暂无描述' }}</span>
            </div>
          </div>
        </div>

        <!-- 房源图片区域 -->
        <div class="info-section">
          <h3 class="section-title">房源图片</h3>
          <div class="images-container">
            <div v-if="!currentHouse.images || currentHouse.images.length === 0" class="empty-state">
              暂无图片
            </div>
            <div v-else class="image-gallery">
              <div
                v-for="(img, index) in currentHouse.images"
                :key="index"
                class="house-image-placeholder"
              >
                图片 {{ index + 1 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// JavaScript 部分保持不变
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, View, Delete, Search, RefreshRight, Edit } from '@element-plus/icons-vue'

interface House {
  id: number
  title: string
  price: number
  area: number
  houseType: string
  location: string
  description: string
  publisherName: string
  contactPhone: string
  publishTime: string
  images?: string[]
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

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
  priceRange: '',
  houseType: '',
  publisherName: '',
  sortBy: 'time-desc'
})

const houseForm = reactive({
  id: 0,
  title: '',
  price: 0,
  area: 0,
  houseType: '',
  location: '',
  description: '',
  publisherName: '',
  contactPhone: '',
  publishTime: ''
})

const currentHouse = ref<House>({} as House)

// 数据管理 - 分离原始数据和显示数据
const originalHouseList = ref<House[]>([])
const displayHouseList = ref<House[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑房源' : '新增房源')

const rules = {
  title: [
    { required: true, message: '请输入房源标题', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' }
  ],
  houseType: [
    { required: true, message: '请选择房型', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入位置', trigger: 'blur' }
  ],
  publisherName: [
    { required: true, message: '请输入发布者', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输**系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 初始化模拟数据
const initMockData = () => {
  const mockData: House[] = [
    {
      id: 1,
      title: '阳光花园精装三居室',
      price: 650,
      area: 120,
      houseType: '3室2厅',
      location: '北京市朝阳区望京街道',
      description: '精装修，南北通透，采光好，交通便利',
      publisherName: '张先生',
      contactPhone: '13888888888',
      publishTime: '2024-01-15 10:30:00',
      images: ['house1.jpg', 'house2.jpg']
    },
    {
      id: 2,
      title: '中央公园豪华四居室',
      price: 1200,
      area: 180,
      houseType: '4室2厅',
      location: '北京市海淀区中关村大街',
      description: '豪华装修，配套设施齐全，高端社区',
      publisherName: '李女士',
      contactPhone: '13999999999',
      publishTime: '2024-01-14 14:20:00',
      images: ['house3.jpg', 'house4.jpg']
    },
    {
      id: 3,
      title: '市中心商圈住宅',
      price: 800,
      area: 95,
      houseType: '2室1厅',
      location: '北京市东城区王府井大街',
      description: '交通便利，周边配套完善，商务区核心',
      publisherName: '王先生',
      contactPhone: '13777777777',
      publishTime: '2024-01-13 15:00:00',
      images: ['house5.jpg']
    },
    {
      id: 4,
      title: '学区房精品小三居',
      price: 950,
      area: 105,
      houseType: '3室1厅',
      location: '北京市海淀区清华大学附近',
      description: '学区房，名校附近，投资首选',
      publisherName: '陈女士',
      contactPhone: '13666666666',
      publishTime: '2024-01-12 11:20:00',
      images: ['house6.jpg']
    },
    {
      id: 5,
      title: '现代简约两居室',
      price: 580,
      area: 88,
      houseType: '2室1厅',
      location: '北京市丰台区科技园区',
      description: '现代简约装修，年轻人首选',
      publisherName: '刘先生',
      contactPhone: '13555555555',
      publishTime: '2024-01-11 16:45:00',
      images: []
    }
  ]
  
  originalHouseList.value = mockData
  return mockData
}

// 搜索筛选函数
const filterHouses = (data: House[]) => {
  return data.filter(item => {
    // 房源标题筛选
    if (searchForm.title && searchForm.title.trim()) {
      const searchTitle = searchForm.title.trim().toLowerCase()
      const itemTitle = item.title.toLowerCase()
      if (!itemTitle.includes(searchTitle)) {
        return false
      }
    }
    
    // 发布者筛选
    if (searchForm.publisherName && searchForm.publisherName.trim()) {
      const searchPublisher = searchForm.publisherName.trim().toLowerCase()
      const itemPublisher = item.publisherName.toLowerCase()
      if (!itemPublisher.includes(searchPublisher)) {
        return false
      }
    }
    
    // 户型筛选
    if (searchForm.houseType && searchForm.houseType.trim()) {
      if (!item.houseType.includes(searchForm.houseType)) {
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
const sortHouses = (data: House[]) => {
  if (!searchForm.sortBy) return data
  
  const sortedData = [...data]
  
  switch (searchForm.sortBy) {
    case 'time-desc':
      return sortedData.sort((a, b) => new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime())
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
const updatePagination = (filteredData: House[]) => {
  // 先排序
  const sortedData = sortHouses(filteredData)
  
  // 计算分页
  const startIndex = (pagination.page - 1) * pagination.size
  const endIndex = startIndex + pagination.size
  
  // 更新显示数据和总数
  displayHouseList.value = sortedData.slice(startIndex, endIndex)
  pagination.total = sortedData.length
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

const loadHouseList = async () => {
  loading.value = true
  try {
    // 如果原始数据为空，初始化模拟数据
    if (originalHouseList.value.length === 0) {
      initMockData()
    }
    
    // 执行搜索和分页
    const allData = originalHouseList.value
    const filteredData = filterHouses(allData)
    updatePagination(filteredData)
    
  } catch (error) {
    ElMessage.error('加载房源列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (row: House) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(houseForm, row)
}

const handleView = (row: House) => {
  currentHouse.value = row
  detailDialogVisible.value = true
}

// 修改删除函数，支持从编辑对话框中删除
const handleDelete = async (row: House) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除房源"${row.title}"吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = originalHouseList.value.findIndex(h => h.id === row.id)
    if (index > -1) {
      originalHouseList.value.splice(index, 1)
      ElMessage.success('删除成功')
      
      // 如果是在编辑对话框中删除，关闭对话框
      if (dialogVisible.value) {
        dialogVisible.value = false
      }
      
      await loadHouseList()
    }
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 更新现有房源
          const index = originalHouseList.value.findIndex(h => h.id === houseForm.id)
          if (index > -1) {
            originalHouseList.value[index] = {
              ...originalHouseList.value[index],
              ...houseForm,
              publishTime: originalHouseList.value[index].publishTime // 保持原发布时间
            }
          }
          ElMessage.success('更新成功')
        } else {
          // 新增房源
          const newHouse: House = {
            ...houseForm,
            id: Date.now(), // 简单的ID生成
            publishTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
            images: []
          }
          originalHouseList.value.unshift(newHouse)
          ElMessage.success('新增成功')
        }
        
        dialogVisible.value = false
        await loadHouseList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const formatPrice = (price: number) => {
  if (!price) return '0'
  return price.toString()
}

const calculateUnitPrice = (price: number, area: number) => {
  if (!price || !area) return '0'
  return Math.round((price * 10000) / area).toLocaleString()
}

const handleSearch = async () => {
  pagination.page = 1
  await loadHouseList()
  
  const filteredCount = displayHouseList.value.length
  if (filteredCount === 0) {
    ElMessage.info('未找到符合条件的房源')
  } else {
    ElMessage.success(`找到 ${pagination.total} 条符合条件的房源`)
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    priceRange: '',
    houseType: '',
    publisherName: '',
    sortBy: 'time-desc'
  })
  pagination.page = 1
  loadHouseList()
  ElMessage.info('搜索条件已重置')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadHouseList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadHouseList()
}

const resetForm = () => {
  Object.assign(houseForm, {
    id: 0,
    title: '',
    price: 0,
    area: 0,
    houseType: '',
    location: '',
    description: '',
    publisherName: '',
    contactPhone: ''
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadHouseList()
})
</script>

<style scoped>
.house-manage-container {
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

/* 搜索表单样式 - 和审核页面一致 */
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

/* 🔧 优化表格布局，避免右侧空白和按钮被遮挡 */
.table-card :deep(.el-table) {
  flex: 1;
  overflow: auto;
  width: 100% !important;
  table-layout: fixed !important;
}

.table-card :deep(.el-table--fixed) {
  width: 100% !important;
}

.table-card :deep(.el-table__header-wrapper),
.table-card :deep(.el-table__body-wrapper) {
  width: 100% !important;
}

/* 🔧 强制表格使用固定布局 */
:deep(.el-table) {
  table-layout: fixed !important;
}

:deep(.el-table .el-table__cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-section {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}

/* 🔧 优化操作按钮布局 */
.action-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: nowrap !important; /* 强制不换行 */
  overflow: hidden; /* 防止溢出 */
}

.action-buttons :deep(.el-button) {
  flex-shrink: 0;
  white-space: nowrap;
  padding: 5px 10px;
  font-size: 12px;
  height: 28px;
  min-width: auto;
}

.action-buttons :deep(.el-button--small) {
  height: 28px;
  min-width: auto;
}

/* 🔧 优化编辑对话框样式 - 完美对齐 */
.house-form-dialog :deep(.el-dialog) {
  border-radius: 12px;
  max-height: 85vh;
}

.house-form-dialog :deep(.el-dialog__body) {
  max-height: 60vh;
  overflow-y: auto;
  padding: 24px;
}

/* 🔧 表单样式 - 统一标签宽度和对齐 */
.house-form {
  width: 100%;
}

.house-form :deep(.el-form-item) {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
}

.house-form :deep(.el-form-item__label) {
  width: 80px !important;
  text-align: right !important;
  padding-right: 12px !important;
  font-weight: 600 !important;
  color: #303133 !important;
  font-size: 14px !important;
  line-height: 32px !important;
  height: 32px !important;
  margin: 0 !important;
  flex-shrink: 0;
}

.house-form :deep(.el-form-item__content) {
  flex: 1;
  margin-left: 0 !important;
  line-height: 32px;
  min-height: 32px;
  display: flex;
  align-items: center;
}

/* 🔧 修复数字输入框样式 - 确保数字完整显示 */
.house-form :deep(.el-input),
.house-form :deep(.el-select) {
  width: 100%;
}

.house-form :deep(.el-input-number) {
  width: 100%;
}

.house-form :deep(.el-input-number .el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  height: 32px;
  padding: 0 !important;
}

.house-form :deep(.el-input-number .el-input__inner) {
  height: 30px !important;
  line-height: 30px !important;
  font-size: 14px !important;
  color: #606266 !important;
  border: none !important;
  background: transparent !important;
  padding: 0 8px !important;
  text-align: left !important;
  width: auto !important;
  flex: 1 !important;
}

/* 🔧 修复数字输入框后缀样式 */
.house-form :deep(.el-input-number .el-input-group__append) {
  background-color: #f5f7fa;
  border-left: 1px solid #dcdfe6;
  color: #909399;
  padding: 0 8px;
  font-size: 12px;
  border-radius: 0 6px 6px 0;
  height: 30px;
  line-height: 30px;
  display: flex;
  align-items: center;
}

.house-form :deep(.el-input .el-input__wrapper),
.house-form :deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  height: 32px;
}

.house-form :deep(.el-input .el-input__wrapper:hover),
.house-form :deep(.el-select .el-input__wrapper:hover),
.house-form :deep(.el-input-number .el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.house-form :deep(.el-input .el-input__wrapper.is-focus),
.house-form :deep(.el-select .el-input__wrapper.is-focus),
.house-form :deep(.el-input-number .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
}

.house-form :deep(.el-input .el-input__inner),
.house-form :deep(.el-select .el-input__inner) {
  height: 30px;
  line-height: 30px;
  font-size: 14px;
  color: #606266;
  border: none;
  background: transparent;
  padding: 0 12px;
}

.house-form :deep(.el-select .el-input__inner) {
  padding-right: 30px;
}

/* 🔧 文本域样式 */
.house-form :deep(.el-textarea) {
  width: 100%;
}

.house-form :deep(.el-textarea .el-textarea__inner) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 12px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.house-form :deep(.el-textarea .el-textarea__inner:hover) {
  border-color: #c0c4cc;
}

.house-form :deep(.el-textarea .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
  outline: none;
}

/* 🔧 优化对话框底部 - 左右分布 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

.footer-left {
  flex: 1;
}

.footer-right {
  display: flex;
  gap: 12px;
}

/* 详情对话框样式 */
.house-detail-dialog :deep(.el-dialog) {
  border-radius: 12px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.house-detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
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

.house-image-placeholder {
  width: 100%;
  height: 100px;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  background: #fafafa;
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

/* 🔧 响应式设计 - 优化小屏幕体验 */
@media (max-width: 1200px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
    align-items: stretch;
  }
  
  .action-buttons :deep(.el-button) {
    width: 100%;
    justify-content: center;
  }
}

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
    width: 100%;
    justify-content: space-between;
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
  
  .house-image-placeholder {
    height: 80px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .footer-left,
  .footer-right {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}
</style>



