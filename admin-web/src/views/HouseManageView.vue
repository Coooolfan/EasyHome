<template>
  <div class="house-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">房源管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增房源
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-row :gutter="16" class="search-row">
            <el-col :span="5">
              <el-form-item label="房源标题" class="form-item-custom">
                <el-input 
                  v-model="searchForm.title" 
                  placeholder="请输入房源标题" 
                  clearable
                />
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
                  <el-option label="100-150万" value="100-150" />
                  <el-option label="150-200万" value="150-200" />
                  <el-option label="200-300万" value="200-300" />
                  <el-option label="300-500万" value="300-500" />
                  <el-option label="500万以上" value="500-999999" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="4">
              <el-form-item label="户型" class="form-item-custom">
                <el-select 
                  v-model="searchForm.houseType" 
                  placeholder="请选择户型"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="不限户型" value="" />
                  <el-option label="1室" value="1" />
                  <el-option label="2室" value="2" />
                  <el-option label="3室" value="3" />
                  <el-option label="4室及以上" value="4+" />
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
                  <el-option label="最新发布" value="time-desc" />
                  <el-option label="价格从低到高" value="price-asc" />
                  <el-option label="价格从高到低" value="price-desc" />
                  <el-option label="面积从小到大" value="area-asc" />
                  <el-option label="面积从大到小" value="area-desc" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="7">
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
        <el-table :data="houseList" style="width: 100%" v-loading="loading" :show-overflow-tooltip="true">
          <el-table-column prop="id" label="ID" width="60" />
          
          <el-table-column prop="title" label="房源标题" min-width="160" :show-overflow-tooltip="true" />
          
          <el-table-column prop="price" label="价格" width="90">
            <template #default="scope">
              <el-tooltip 
                :content="`￥${scope.row.price.toLocaleString()}元`" 
                placement="top"
                effect="dark"
              >
                <span class="price">￥{{ formatPrice(scope.row.price) }}万</span>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="area" label="面积" width="80">
            <template #default="scope">
              {{ scope.row.area }}㎡
            </template>
          </el-table-column>
          
          <el-table-column prop="location" label="位置" width="120" :show-overflow-tooltip="true" />
          
          <el-table-column prop="houseType" label="户型" width="100" :show-overflow-tooltip="true" />
          
          <el-table-column prop="publisherName" label="发布者" width="90" :show-overflow-tooltip="true" />
          
          <el-table-column prop="publishTime" label="发布时间" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.publishTime) }}
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleEdit(scope.row)"
                >
                  编辑
                </el-button>
                <el-dropdown :onCommand="getRowCommandHandler(scope.row)" trigger="click">
                  <el-button type="info" size="small">
                    更多
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        command="view"
                        :icon="View"
                      >
                        查看详情
                      </el-dropdown-item>
                      <el-dropdown-item 
                        command="delete"
                        :icon="Delete"
                        divided
                      >
                        删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="houseForm"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房源标题" prop="title">
              <el-input v-model="houseForm.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格(万)" prop="price">
              <el-input-number v-model="houseForm.price" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="area">
              <el-input-number v-model="houseForm.area" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房型" prop="houseType">
              <el-select v-model="houseForm.houseType" style="width: 100%">
                <el-option label="一室一厅" value="1室1厅" />
                <el-option label="两室一厅" value="2室1厅" />
                <el-option label="三室两厅" value="3室2厅" />
                <el-option label="四室两厅" value="4室2厅" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="位置" prop="location">
          <el-input v-model="houseForm.location" />
        </el-form-item>

        <el-form-item label="房源描述" prop="description">
          <el-input 
            v-model="houseForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入房源详细描述..."
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布者" prop="publisherName">
              <el-input v-model="houseForm.publisherName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="houseForm.contactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="房源详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房源ID">{{ currentHouse.id }}</el-descriptions-item>
        <el-descriptions-item label="房源标题">{{ currentHouse.title }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentHouse.price }}万</el-descriptions-item>
        <el-descriptions-item label="面积">{{ currentHouse.area }}㎡</el-descriptions-item>
        <el-descriptions-item label="房型">{{ currentHouse.houseType }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentHouse.location }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentHouse.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentHouse.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">{{ formatDateTime(currentHouse.publishTime) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>房源描述</el-divider>
      <p>{{ currentHouse.description || '暂无描述' }}</p>
      
      <el-divider>房源图片</el-divider>
      <div class="house-images">
        <el-empty description="暂无图片" v-if="!currentHouse.images || currentHouse.images.length === 0" />
        <!-- 这里可以展示房源图片 -->
      </div>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, ArrowDown, View, Delete, Search, RefreshRight } from '@element-plus/icons-vue'

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
  sortBy: 'time-desc'
})

const houseForm = reactive({
  title: '',
  price: 0,
  area: 0,
  houseType: '',
  location: '',
  description: '',
  publisherName: '',
  contactPhone: ''
})

const currentHouse = ref<House>({} as House)
const houseList = ref<House[]>([])

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
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 解析价格范围
const parsePriceRange = (priceRange: string) => {
  if (!priceRange) return { minPrice: null, maxPrice: null }
  
  const [min, max] = priceRange.split('-').map(p => parseInt(p))
  
  if (max >= 999999) {
    return { minPrice: min * 10000, maxPrice: null }
  }
  
  return { 
    minPrice: min * 10000, 
    maxPrice: max * 10000 
  }
}

// 简化的命令处理
const handleCommand = (command: string, row?: House) => {
  if (!row) return
  
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const getRowCommandHandler = (row: House) => {
  return (command: string) => {
    handleCommand(command, row)
  }
}

const loadHouseList = async () => {
  loading.value = true
  try {
    const mockData: House[] = [
      {
        id: 1,
        title: '阳光花园三室两厅',
        price: 1200000,
        area: 120,
        houseType: '三室两厅',
        location: '朝阳区',
        description: '精装修，南北通透，采光好',
        publisherName: '张先生',
        contactPhone: '13888888888',
        publishTime: '2024-01-15 10:00:00',
        images: ['house1.jpg', 'house2.jpg']
      },
      {
        id: 2,
        title: '中央公园精装修',
        price: 2000000,
        area: 150,
        houseType: '四室两厅',
        location: '海淀区',
        description: '豪华装修，配套设施齐全',
        publisherName: '李女士',
        contactPhone: '13999999999',
        publishTime: '2024-01-14 09:00:00',
        images: ['house3.jpg', 'house4.jpg']
      },
      {
        id: 3,
        title: '市中心商圈住宅',
        price: 1800000,
        area: 100,
        houseType: '两室一厅',
        location: '西城区',
        description: '交通便利，周边配套完善',
        publisherName: '王先生',
        contactPhone: '13777777777',
        publishTime: '2024-01-13 15:00:00',
        images: ['house5.jpg']
      }
    ]
    
    houseList.value = mockData
    pagination.total = mockData.length
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

const handleDelete = async (row: House) => {
  try {
    await ElMessageBox.confirm('确定要删除该房源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = houseList.value.findIndex(h => h.id === row.id)
    if (index > -1) {
      houseList.value.splice(index, 1)
      pagination.total--
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadHouseList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const formatPrice = (price: number) => {
  if (!price) return '0'
  
  if (price % 10000 === 0) {
    return (price / 10000).toString()
  }
  
  const result = (price / 10000).toFixed(1)
  return result.replace(/\.0$/, '')
}

const handleSearch = async () => {
  pagination.page = 1
  loading.value = true
  
  try {
    const queryParams = {
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title || undefined,
      houseType: searchForm.houseType || undefined,
      sortBy: searchForm.sortBy || 'time-desc',
      ...parsePriceRange(searchForm.priceRange)
    }
    
    console.log('搜索参数:', queryParams)
    await loadHouseList()
    
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    priceRange: '',
    houseType: '',
    sortBy: 'time-desc'
  })
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadHouseList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadHouseList()
}

const resetForm = () => {
  Object.assign(houseForm, {
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
/* 主容器样式 - 适配固定布局 */
.house-manage-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow: hidden;
}

/* 页面头部 */
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

/* 搜索区域 */
.search-section {
  flex-shrink: 0;
  margin-bottom: 20px;
}

.search-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 搜索表单 */
.search-form {
  margin: 0;
  
  .search-row {
    margin-bottom: 0;
    align-items: flex-end;
    
    .el-col {
      margin-bottom: 0;
    }
  }
}

/* 统一表单项样式 */
.form-item-custom {
  margin-bottom: 0;
  width: 100%;
  
  :deep(.el-form-item__label) {
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
  
  :deep(.el-form-item__content) {
    flex: 1;
    line-height: 1;
    margin: 0;
  }
}

/* 下拉框统一样式 */
:deep(.el-select) {
  width: 100%;
  min-width: 120px;
  
  .el-input {
    width: 100%;
    min-width: 120px;
  }
  
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    border-radius: 6px;
    height: 34px;
    min-width: 120px !important;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #409eff inset;
    }
  }
  
  .el-input__inner {
    font-size: 13px;
    height: 34px;
    line-height: 34px;
    border: none;
    background: transparent;
    padding: 0 24px 0 12px !important;
    color: #606266;
    min-width: 90px !important;
    width: calc(100% - 36px) !important;
    
    &::placeholder {
      color: #a8abb2;
      font-size: 13px;
    }
  }
  
  .el-input__suffix {
    right: 6px;
    height: 34px;
    display: flex;
    align-items: center;
    width: 20px;
    
    .el-input__suffix-inner {
      display: flex;
      align-items: center;
    }
  }
  
  .el-select__caret {
    color: #a8abb2;
    font-size: 14px;
    transition: transform 0.3s;
    
    &.is-reverse {
      transform: rotateZ(180deg);
    }
  }
}

/* 普通输入框统一样式 */
:deep(.el-input) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    border-radius: 6px;
    height: 34px;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #409eff inset;
    }
  }
  
  .el-input__inner {
    font-size: 13px;
    height: 34px;
    line-height: 34px;
    border: none;
    background: transparent;
    padding: 0 12px;
    color: #606266;
    
    &::placeholder {
      color: #a8abb2;
      font-size: 13px;
    }
  }
}

/* 按钮组样式 - 水平排列对齐 */
.button-group {
  margin-bottom: 0;
  
  :deep(.el-form-item__content) {
    display: flex;
    justify-content: flex-end;
    align-items: flex-end;
    height: 34px;
    margin: 0;
  }
}

.button-container {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 34px;
  
  .el-button {
    height: 34px;
    font-size: 13px;
    border-radius: 6px;
    padding: 0 16px;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    
    /* 搜索按钮样式优化 */
    &.search-btn {
      background: #409eff;
      border-color: #409eff;
      color: #fff;
      
      &:hover {
        background: #66b1ff;
        border-color: #66b1ff;
      }
      
      &:active {
        background: #3a8ee6;
        border-color: #3a8ee6;
      }
    }
    
    /* 重置按钮样式 */
    &.reset-btn {
      background: #fff;
      border-color: #dcdfe6;
      color: #606266;
      
      &:hover {
        color: #409eff;
        border-color: #c6e2ff;
        background-color: #ecf5ff;
      }
    }
  }
}

/* 表格区域 */
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

:deep(.table-card .el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

:deep(.el-table) {
  flex: 1;
  overflow: auto;
}

/* 确保表格可以滚动 */
:deep(.el-table__body-wrapper) {
  overflow-y: auto !important;
}

/* 分页区域 */
.pagination-section {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}

.pagination {
  margin: 0;
}

/* 表格内的价格样式 */
.price {
  color: #f56c6c;
  font-weight: 600;
  cursor: help;
  font-size: 13px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
}

/* 房源图片区域 */
.house-images {
  min-height: 100px;
  padding: 20px 0;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式优化 */
@media (max-width: 1400px) {
  .form-item-custom {
    :deep(.el-form-item__label) {
      font-size: 13px;
      margin-bottom: 6px;
    }
  }
  
  .search-form .search-row {
    .el-col:nth-child(2),
    .el-col:nth-child(3),
    .el-col:nth-child(4) {
      :deep(.el-select) {
        min-width: 110px !important;
        
        .el-input__inner {
          min-width: 80px !important;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    min-width: 60px;
  }
  
  .search-form .search-row {
    .el-col {
      margin-bottom: 16px;
    }
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-tooltip__trigger) {
  display: inline-block;
}

/* 滚动条样式 */
:deep(.el-table__body-wrapper)::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>


