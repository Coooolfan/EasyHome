<template>
  <div class="house-manage">
    <div class="page-header">
      <h2>房源管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增房源
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房源标题">
          <el-input v-model="searchForm.title" placeholder="请输入房源标题" />
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input-number v-model="searchForm.minPrice" placeholder="最低价" style="width: 120px" />
          <span style="margin: 0 10px">-</span>
          <el-input-number v-model="searchForm.maxPrice" placeholder="最高价" style="width: 120px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="审核拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="房源状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房源列表 -->
    <el-card>
      <el-table :data="houseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="房源标题" min-width="200" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">
            <span class="price">¥{{ scope.row.price }}万</span>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积" width="100">
          <template #default="scope">
            {{ scope.row.area }}㎡
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="150" />
        <el-table-column prop="publisherName" label="发布者" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="房源状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        
        <!-- 修改后的操作列 -->
        <el-table-column label="操作" width="200" fixed="right">
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
                      v-if="scope.row.auditStatus === 'pending'"
                      command="approve"
                      :icon="Check"
                    >
                      审核通过
                    </el-dropdown-item>
                    <el-dropdown-item 
                      v-if="scope.row.auditStatus === 'pending'"
                      command="reject"
                      :icon="Close"
                    >
                      审核拒绝
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="toggleStatus"
                      :icon="scope.row.status === 'active' ? Lock : Unlock"
                      divided
                    >
                      {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="delete"
                      :icon="Delete"
                      :disabled="scope.row.status === 'active'"
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

      <!-- 分页 -->
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
    </el-card>

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

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="审核状态" prop="auditStatus">
              <el-select v-model="houseForm.auditStatus" style="width: 100%">
                <el-option label="待审核" value="pending" />
                <el-option label="审核通过" value="approved" />
                <el-option label="审核拒绝" value="rejected" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房源状态" prop="status">
              <el-select v-model="houseForm.status" style="width: 100%">
                <el-option label="启用" value="active" />
                <el-option label="禁用" value="disabled" />
              </el-select>
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
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentHouse.auditStatus)">
            {{ getAuditStatusText(currentHouse.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房源状态">
          <el-tag :type="currentHouse.status === 'active' ? 'success' : 'danger'">
            {{ currentHouse.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentHouse.publishTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentHouse.auditTime || '未审核' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>房源描述</el-divider>
      <p>{{ currentHouse.description || '暂无描述' }}</p>
      
      <!-- 如果有图片的话可以在这里显示 -->
      <el-divider>房源图片</el-divider>
      <div class="house-images">
        <el-empty description="暂无图片" v-if="!currentHouse.images || currentHouse.images.length === 0" />
        <!-- 这里可以展示房源图片 -->
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="审核房源" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input 
            v-model="auditForm.remark" 
            type="textarea" 
            :rows="4"
            placeholder="请输入审核意见..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAudit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, ArrowDown, View, Check, Close, Lock, Unlock, Delete } from '@element-plus/icons-vue'

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
  auditStatus: 'pending' | 'approved' | 'rejected'
  status: 'active' | 'disabled'
  publishTime: string
  auditTime?: string
  images?: string[]
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  title: '',
  minPrice: null,
  maxPrice: null,
  auditStatus: '',
  status: ''
})

const houseForm = reactive({
  title: '',
  price: 0,
  area: 0,
  houseType: '',
  location: '',
  description: '',
  publisherName: '',
  contactPhone: '',
  auditStatus: 'pending',
  status: 'active'
})

const auditForm = reactive({
  result: 'approved',
  remark: ''
})

const currentHouse = ref<House>({} as House)
const currentAuditHouse = ref<House>({} as House)
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

// 处理下拉菜单命令 - 修改为接收行数据的方式
const handleCommand = (command: string, row?: House) => {
  console.log('handleCommand called with:', command, row)
  if (!row) {
    console.error('Row data is missing')
    return
  }
  
  switch (command) {
    case 'view':
      console.log('Calling handleView')
      handleView(row)
      break
    case 'approve':
      handleAudit(row, 'approved')
      break
    case 'reject':
      handleAudit(row, 'rejected')
      break
    case 'toggleStatus':
      handleToggleStatus(row)
      break
    case 'delete':
      handleDelete(row)
      break
    default:
      console.log('Unknown command:', command)
  }
}

// 为特定行创建处理函数
const getRowCommandHandler = (row: House) => {
  return (command: string) => {
    console.log('Row command handler called:', command, row)
    handleCommand(command, row)
  }
}
// 切换房源状态
const handleToggleStatus = async (row: House) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}这个房源吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`房源${action}成功`)
  } catch {
    ElMessage.info('已取消操作')
  }
}

const getAuditStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

const getAuditStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已通过'
    case 'rejected': return '已拒绝'
    default: return '未知'
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
        auditStatus: 'approved' as 'pending' | 'approved' | 'rejected',
        status: 'active' as 'active' | 'disabled',
        publishTime: '2024-01-15 10:00:00',
        auditTime: '2024-01-16 14:30:00',
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
        auditStatus: 'pending' as 'pending' | 'approved' | 'rejected',
        status: 'active' as 'active' | 'disabled',
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
        auditStatus: 'rejected' as 'pending' | 'approved' | 'rejected',
        status: 'disabled' as 'active' | 'disabled',
        publishTime: '2024-01-13 15:00:00',
        auditTime: '2024-01-14 11:00:00',
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
  console.log('handleView called with:', row)
  currentHouse.value = row
  detailDialogVisible.value = true
  console.log('detailDialogVisible set to:', detailDialogVisible.value)
}

const handleAudit = (row: House, result: 'approved' | 'rejected') => {
  currentAuditHouse.value = row
  auditForm.result = result
  auditForm.remark = ''
  auditDialogVisible.value = true
}

const confirmAudit = async () => {
  try {
    ElMessage.success(`房源${auditForm.result === 'approved' ? '审核通过' : '审核拒绝'}`)
    auditDialogVisible.value = false
    loadHouseList()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleDelete = async (row: House) => {
  if (row.status === 'active') {
    ElMessage.warning('请先禁用该房源后再删除')
    return
  }
  
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

const handleSearch = () => {
  pagination.page = 1
  loadHouseList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    minPrice: null,
    maxPrice: null,
    auditStatus: '',
    status: ''
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
    contactPhone: '',
    auditStatus: 'pending',
    status: 'active'
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadHouseList()
})
</script>

<style scoped>
.house-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.house-images {
  min-height: 100px;
  padding: 20px 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
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
}
</style>
