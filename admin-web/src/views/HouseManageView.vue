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
        
        <el-form-item label="房源地址">
          <el-input 
            v-model="searchForm.address" 
            placeholder="请输入地址" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="价格区间">
          <div class="range-input">
            <el-input-number 
              v-model="searchForm.minPrice" 
              :min="0" 
              :precision="0"
              placeholder="最低价" 
              :controls="false"
              size="small"
            />
            <span class="range-separator">-</span>
            <el-input-number 
              v-model="searchForm.maxPrice" 
              :min="0" 
              :precision="0"
              placeholder="最高价" 
              :controls="false"
              size="small"
            />
            <span class="range-unit">元</span>
          </div>
        </el-form-item>
        
        <el-form-item label="面积区间">
          <div class="range-input">
            <el-input-number 
              v-model="searchForm.minArea" 
              :min="0" 
              :precision="0"
              placeholder="最小" 
              :controls="false"
              size="small"
            />
            <span class="range-separator">-</span>
            <el-input-number 
              v-model="searchForm.maxArea" 
              :min="0" 
              :precision="0"
              placeholder="最大" 
              :controls="false"
              size="small"
            />
            <span class="range-unit">㎡</span>
          </div>
        </el-form-item>
        
        <el-form-item label="户型">
          <el-select 
            v-model="searchForm.rooms" 
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
        
        <el-form-item label="装修">
          <el-select 
            v-model="searchForm.decoration" 
            placeholder="请选择装修"
            clearable
          >
            <el-option label="不限装修" value="" />
            <el-option label="毛坯" value="毛坯" />
            <el-option label="简装" value="简装" />
            <el-option label="精装" value="精装" />
            <el-option label="豪装" value="豪装" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="朝向">
          <el-select 
            v-model="searchForm.orientation" 
            placeholder="请选择朝向"
            clearable
          >
            <el-option label="不限朝向" value="" />
            <el-option label="东" value="东" />
            <el-option label="南" value="南" />
            <el-option label="西" value="西" />
            <el-option label="北" value="北" />
            <el-option label="东南" value="东南" />
            <el-option label="东北" value="东北" />
            <el-option label="西南" value="西南" />
            <el-option label="西北" value="西北" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="建造年份">
          <div class="range-input">
            <el-input-number 
              v-model="searchForm.minYear" 
              :min="1980" 
              :max="2025"
              :precision="0"
              placeholder="最早" 
              :controls="false"
              size="small"
            />
            <span class="range-separator">-</span>
            <el-input-number 
              v-model="searchForm.maxYear" 
              :min="1980" 
              :max="2025"
              :precision="0"
              placeholder="最晚" 
              :controls="false"
              size="small"
            />
            <span class="range-unit">年</span>
          </div>
        </el-form-item>

        <el-form-item label="排序方式">
          <el-select 
            v-model="searchForm.sortBy" 
            placeholder="请选择排序方式"
            clearable
          >
            <el-option label="最新发布" value="createdAt-desc" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="面积从小到大" value="area-asc" />
            <el-option label="面积从大到小" value="area-desc" />
            <el-option label="建造年份新到旧" value="buildYear-desc" />
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
        <el-table :data="displayHouseList" style="width: 100%" v-loading="loading" :show-overflow-tooltip="true">
          <el-table-column prop="id" label="ID" width="60" />
          
          <!-- 房源标题列使用min-width让其自适应 -->
          <el-table-column prop="title" label="房源标题" min-width="200" :show-overflow-tooltip="true" />
          
          <el-table-column prop="price" label="总价" width="100" align="right">
            <template #default="scope">
              <span>{{ formatPrice(scope.row.price) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="unitPrice" label="单价" width="100" align="right">
            <template #default="scope">
              <span>{{ scope.row.unitPrice }}元/㎡</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="area" label="面积" width="80" align="center">
            <template #default="scope">
              {{ scope.row.area }}㎡
            </template>
          </el-table-column>
          
          <!-- 位置列使用min-width让其自适应 -->
          <el-table-column prop="address" label="地址" min-width="150" :show-overflow-tooltip="true" />
          
          <el-table-column prop="rooms" label="户型" width="90" />
          
          <el-table-column prop="decoration" label="装修" width="80" />
          
          <el-table-column prop="createdAt" label="发布时间" width="110" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          
          <!-- 操作列固定宽度 -->
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

    <!-- 新增/编辑对话框 -->
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
              <el-input-number 
                v-model="houseForm.price" 
                :min="0" 
                :precision="1"
                :step="10"
                placeholder="元"
                style="width: 100%"
                :controls="false"
              >
                <template #append>元</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积" prop="area">
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

        <!-- 单价 - 自动计算 -->
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number 
            v-model="houseForm.unitPrice" 
            :min="0" 
            :step="100"
            :precision="0"
            placeholder="元/㎡"
            style="width: 100%"
            :controls="false"
          >
            <template #append>元/㎡</template>
          </el-input-number>
        </el-form-item>

        <!-- 第二行：房型和地址 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户型" prop="rooms">
              <el-select v-model="houseForm.rooms" placeholder="请选择户型" style="width: 100%">
                <el-option label="一室一厅" value="1室1厅" />
                <el-option label="两室一厅" value="2室1厅" />
                <el-option label="三室两厅" value="3室2厅" />
                <el-option label="四室两厅" value="4室2厅" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址" prop="address">
              <el-input v-model="houseForm.address" placeholder="请输入地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：楼层和建造年份 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input v-model="houseForm.floor" placeholder="请输入楼层，如：5/18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建造年份" prop="buildYear">
              <el-input-number 
                v-model="houseForm.buildYear" 
                :min="1980" 
                :max="2025"
                :precision="0"
                :controls="false"
                style="width: 100%"
                placeholder="建造年份"
              >
                <template #append>年</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第四行：装修和朝向 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="装修" prop="decoration">
              <el-select v-model="houseForm.decoration" placeholder="请选择装修" style="width: 100%">
                <el-option label="毛坯" value="毛坯" />
                <el-option label="简装" value="简装" />
                <el-option label="精装" value="精装" />
                <el-option label="豪装" value="豪装" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="朝向" prop="orientation">
              <el-select v-model="houseForm.orientation" placeholder="请选择朝向" style="width: 100%">
                <el-option label="东" value="东" />
                <el-option label="南" value="南" />
                <el-option label="西" value="西" />
                <el-option label="北" value="北" />
                <el-option label="东南" value="东南" />
                <el-option label="东北" value="东北" />
                <el-option label="西南" value="西南" />
                <el-option label="西北" value="西北" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 图片URL -->
        <el-form-item label="图片URL" prop="image">
          <el-input v-model="houseForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        
        <!-- 标签 -->
        <el-form-item label="标签">
          <el-select
            v-model="houseForm.tag"
            multiple
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option label="近地铁" value="近地铁" />
            <el-option label="学区房" value="学区房" />
            <el-option label="南北通透" value="南北通透" />
            <el-option label="电梯房" value="电梯房" />
            <el-option label="拎包入住" value="拎包入住" />
            <el-option label="首次出租" value="首次出租" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <!-- 编辑时显示删除按钮 -->
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
            <el-button 
              type="primary" 
              @click="handleSubmit"
              :loading="submitLoading"
            >
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
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ currentHouse.userId }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">房源标题</span>
              <span class="info-value">{{ currentHouse.title }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">总价</span>
              <span class="info-value">{{ formatPrice(currentHouse.price) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单价</span>
              <span class="info-value">{{ currentHouse.unitPrice }}元/㎡</span>
            </div>
            <div class="info-item">
              <span class="info-label">面积</span>
              <span class="info-value">{{ currentHouse.area }}㎡</span>
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
              <span class="info-label">朝向</span>
              <span class="info-value">{{ currentHouse.orientation }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">装修</span>
              <span class="info-value">{{ currentHouse.decoration }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">建造年份</span>
              <span class="info-value">{{ currentHouse.buildYear }}年</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间</span>
              <span class="info-value">{{ formatDateTime(currentHouse.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ formatDateTime(currentHouse.updatedAt) }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">详细地址</span>
              <span class="info-value">{{ currentHouse.address }}</span>
            </div>
            <div class="info-item full-width" v-if="currentHouse.tag && currentHouse.tag.length > 0">
              <span class="info-label">标签</span>
              <div class="tag-list">
                <el-tag 
                  v-for="(tag, index) in currentHouse.tag" 
                  :key="index" 
                  size="small"
                  class="house-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 房源图片区域 -->
        <div class="info-section">
          <h3 class="section-title">房源图片</h3>
          <div class="images-container">
            <div v-if="!currentHouse.image" class="empty-state">
              暂无图片
            </div>
            <div v-else class="image-gallery">
              <div class="house-image-placeholder">
                <img 
                  v-if="currentHouse.image" 
                  :src="currentHouse.image" 
                  alt="房源图片"
                  class="house-image"
                />
                <div v-else class="no-image">暂无图片</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, View, Delete, Search, RefreshRight, Edit } from '@element-plus/icons-vue'
import axios from 'axios'

// 修改房源接口定义
interface House {
  id: number
  title: string
  address: string
  price: number
  unitPrice: number
  area: number
  rooms: string
  floor: string
  buildYear?: number | undefined // 修改为可选或允许undefined
  orientation: string
  decoration: string
  image: string
  createdAt: string
  updatedAt: string
  tag: string[]
  userId: number
}

// 定义API响应接口
interface HouseResponse {
  code: string
  message: string
  data: {
    size: number
    records: House[]
    current: number
    total: number
  }
  timestamp: number
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false) // 提交加载状态

// 时间格式化函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toISOString().split('T')[0]
  } catch (e) {
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
  } catch (e) {
    return dateStr
  }
}

// 搜索表单 - 适配后端DTO
const searchForm = reactive({
  title: '',
  address: '',
  minPrice: 0,
  maxPrice: 0,
  minArea: 0,
  maxArea: 0,
  rooms: '',
  decoration: '',
  orientation: '',
  minYear: 0, 
  maxYear: 2025, // 设置最晚年份默认为2025
  sortBy: 'createdAt-desc'
})

// 编辑或新增的房源表单
const houseForm = reactive({
  id: 0,
  title: '',
  address: '',
  price: 0,
  unitPrice: 0,
  area: 0,
  rooms: '',
  floor: '',
  buildYear: undefined, // 修改为undefined，去除默认值
  orientation: '',
  decoration: '',
  image: '',
  tag: [] as string[],
  userId: 0,
  createdAt: '',
  updatedAt: ''
})

// 当价格或面积改变时，自动计算单价
watch([() => houseForm.price, () => houseForm.area], ([price, area]) => {
  if (price > 0 && area > 0) {
    houseForm.unitPrice = Math.round((price * 10000) / area)
  }
})

const currentHouse = ref<House>({} as House)

// 数据管理
const displayHouseList = ref<House[]>([])

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑房源' : '新增房源')

// 表单验证规则
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
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' }
  ],
  rooms: [
    { required: true, message: '请选择户型', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' }
  ],
  floor: [
    { required: true, message: '请输入楼层', trigger: 'blur' }
  ],
  buildYear: [
    { required: true, message: '请输入建造年份', trigger: 'blur' }
  ],
  decoration: [
    { required: true, message: '请选择装修', trigger: 'change' }
  ],
  orientation: [
    { required: true, message: '请选择朝向', trigger: 'change' }
  ]
}

// API函数 - 获取房源列表
const fetchHouseList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: Record<string, any> = {
      current: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    // 移除值为空的参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === 0 || params[key] === null) {
        delete params[key]
      }
    })
    
    const response = await axios.get<HouseResponse>('/api/houses/page', { params })
    
    // 从响应中获取数据
    const { data } = response
    if (data.code === 'SUCCESS') {
      // 更新列表和分页信息
      displayHouseList.value = data.data.records
      pagination.total = data.data.total
      pagination.page = data.data.current
      pagination.size = data.data.size
    } else {
      ElMessage.error(data.message || '获取房源列表失败')
    }
  } catch (error) {
    console.error('获取房源列表失败:', error)
    ElMessage.error('获取房源列表失败')
  } finally {
    loading.value = false
  }
}

// 添加房源API - 使用正确的接口地址
const addHouse = async (house: Omit<House, 'id' | 'createdAt' | 'updatedAt'>) => {
  try {
    // 准备发送的数据，确保格式符合API要求
    const houseDTO = {
      title: house.title,
      address: house.address,
      price: house.price,
      unitPrice: house.unitPrice,
      area: house.area,
      rooms: house.rooms,
      floor: house.floor,
      buildYear: house.buildYear || 2000, // 提供默认值避免null
      orientation: house.orientation,
      decoration: house.decoration,
      image: house.image,
      tag: house.tag // API期望数组格式
    }
    
    // 使用正确的API路径
    const response = await axios.post('/api/houses/add', houseDTO)
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('添加房源成功')
      return true
    } else {
      ElMessage.error(response.data?.message || '添加房源失败')
      return false
    }
  } catch (error) {
    console.error('添加房源失败:', error)
    ElMessage.error('添加房源失败')
    return false
  }
}

// 更新房源API - 使用正确的接口地址和参数
const updateHouse = async (house: House) => {
  try {
    // 准备发送的数据，确保格式符合API要求
    const houseDTO = {
      title: house.title,
      address: house.address,
      price: house.price,
      unitPrice: house.unitPrice,
      area: house.area,
      rooms: house.rooms,
      floor: house.floor,
      buildYear: house.buildYear || 2000, // 提供默认值避免null
      orientation: house.orientation,
      decoration: house.decoration,
      image: house.image,
      tag: house.tag // API期望数组格式
    }
    
    // 使用正确的API路径，id作为query参数
    const response = await axios.put('/api/houses/update', houseDTO, {
      params: { id: house.id }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('更新房源成功')
      return true
    } else {
      ElMessage.error(response.data?.message || '更新房源失败')
      return false
    }
  } catch (error) {
    console.error('更新房源失败:', error)
    ElMessage.error('更新房源失败')
    return false
  }
}

// 删除房源API - 使用正确的接口地址
const deleteHouse = async (id: number) => {
  try {
    const response = await axios.delete('/api/houses/remove-house', {
      params: { id }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('删除房源成功')
      return true
    } else {
      ElMessage.error(response.data?.message || '删除房源失败')
      return false
    }
  } catch (error) {
    console.error('删除房源失败:', error)
    ElMessage.error('删除房源失败')
    return false
  }
}

// 格式化价格
const formatPrice = (price: number) => {
  if (!price) return '0'
  return price.toString()
}

// 搜索操作
const handleSearch = async () => {
  pagination.page = 1
  await fetchHouseList()
}

// 重置筛选条件
const handleReset = () => {
  // 重置搜索表单
  Object.assign(searchForm, {
    title: '',
    address: '',
    minPrice: 0,
    maxPrice: 0,
    minArea: 0,
    maxArea: 0,
    rooms: '',
    decoration: '',
    orientation: '',
    minYear: 0,
    maxYear: 2025, // 保持最晚年份默认为2025
    sortBy: 'createdAt-desc'
  })
  
  // 重置分页并重新获取数据
  pagination.page = 1
  fetchHouseList()
  
  ElMessage.info('搜索条件已重置')
}

// 分页操作
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchHouseList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchHouseList()
}

// 新增房源
const handleAdd = () => {
  isEdit.value = false
  // 先重置表单，再显示对话框，确保表单内容已完全清空
  resetForm()
  dialogVisible.value = true
}

// 查看房源详情
const handleView = (row: House) => {
  currentHouse.value = row
  detailDialogVisible.value = true
}

// 删除房源
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
    
    const success = await deleteHouse(row.id)
    if (success) {
      // 如果是在编辑对话框中删除，关闭对话框
      if (dialogVisible.value) {
        dialogVisible.value = false
      }
      
      // 刷新列表
      fetchHouseList()
    }
  } catch {
    ElMessage.info('已取消删除')
  }
}

// 提交表单 - 添加加载状态
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 开始加载状态
        submitLoading.value = true
        
        // 如果单价为0，则自动计算
        if (houseForm.unitPrice === 0 && houseForm.price > 0 && houseForm.area > 0) {
          houseForm.unitPrice = Math.round((houseForm.price * 10000) / houseForm.area)
        }
        
        let success = false
        if (isEdit.value) {
          // 更新房源
          success = await updateHouse(houseForm)
        } else {
          // 新增房源
          success = await addHouse(houseForm)
        }
        
        if (success) {
          dialogVisible.value = false
          fetchHouseList() // 刷新列表
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        // 无论成功失败，结束加载状态
        submitLoading.value = false
      }
    }
  })
}

// 重置表单 - 确保彻底重置
const resetForm = () => {
  // 使用显式赋值确保每个字段都被重置
  houseForm.id = 0
  houseForm.title = ''
  houseForm.address = ''
  houseForm.price = 0
  houseForm.unitPrice = 0
  houseForm.area = 0
  houseForm.rooms = ''
  houseForm.floor = ''
  houseForm.buildYear = undefined
  houseForm.orientation = ''
  houseForm.decoration = ''
  houseForm.image = ''
  houseForm.tag = []
  houseForm.userId = 0
  houseForm.createdAt = ''
  houseForm.updatedAt = ''
  
  // 使用 nextTick 确保视图更新后再重置表单验证状态
  nextTick(() => {
    formRef.value?.resetFields()
  })
}

// 在handleEdit函数中可能需要做特殊处理
const handleEdit = (row: House) => {
  isEdit.value = true
  
  // 完整复制行数据，包括createdAt和updatedAt
  const houseData = {...row}
  
  // 如果buildYear是null，将其设为undefined以避免表单显示0
  if (houseData.buildYear === null) {
    houseData.buildYear = undefined
  }
  
  // 重置表单并设置数据
  resetForm()
  nextTick(() => {
    Object.assign(houseForm, houseData)
    dialogVisible.value = true
  })
}

// 组件挂载时加载数据
onMounted(() => {
  fetchHouseList()
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

/* 优化表格布局，避免右侧空白和按钮被遮挡 */
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

/* 强制表格使用固定布局 */
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

/* 优化操作按钮布局 */
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

/* 优化编辑对话框样式 - 完美对齐 */
.house-form-dialog :deep(.el-dialog) {
  border-radius: 12px;
  max-height: 85vh;
}

.house-form-dialog :deep(.el-dialog__body) {
  max-height: 60vh;
  overflow-y: auto;
  padding: 24px;
}

/* 表单样式 - 统一标签宽度和对齐 */
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

/* 修复数字输入框样式 - 确保数字完整显示 */
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

/* 修复数字输入框后缀样式 */
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

/* 文本域样式 */
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

/* 优化对话框底部 - 左右分布 */
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

/* 响应式设计 - 优化小屏幕体验 */
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

/* 新增样式 */
.house-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.house-tag {
  margin: 0;
}

.range-input {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.range-separator {
  font-size: 14px;
  color: #909399;
  padding: 0 4px;
}

.range-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}
</style>