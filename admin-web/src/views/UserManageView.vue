<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="请输入用户名" 
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input 
            v-model="searchForm.phone" 
            placeholder="请输入手机号" 
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table 
        :data="filteredUserList" 
        style="width: 100%" 
        v-loading="loading"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
        stripe
      >
        <!-- 🔧 给每列设置合适的宽度 -->
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
       <el-table-column prop="status" label="状态" width="80" align="center">
  <template #default="scope">
    <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
      {{ scope.row.status === 'active' ? '正常' : '禁用' }}
    </el-tag>
  </template>
</el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="120" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <!-- 🔧 操作列设置固定宽度和对齐 -->
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="info" @click="handleView(scope.row)">
                <el-icon><View /></el-icon>查看详情
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDelete(scope.row)"
                :disabled="scope.row.status === 'active'"
              >
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </div>
          </template>
        </el-table-column>

        <!-- 自定义空状态 -->
        <template #empty>
          <div class="empty-state">
            <div class="empty-icon">
              <el-icon size="80" color="#dcdfe6">
                <component :is="hasSearchCondition ? 'Search' : 'FolderOpened'" />
              </el-icon>
            </div>
            <div class="empty-text">
              <h4>{{ hasSearchCondition ? '未找到相关用户' : '暂无用户数据' }}</h4>
              <p>{{ hasSearchCondition ? '请尝试调整搜索条件' : '系统中还没有用户，请先添加用户账户' }}</p>
            </div>
            <div class="empty-actions">
              <el-button v-if="hasSearchCondition" @click="handleReset">
                <el-icon><Refresh /></el-icon>
                清空搜索
              </el-button>
              <el-button type="primary" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增用户
              </el-button>
            </div>
          </div>
        </template>
      </el-table>

      <!-- 分页 - 只在有数据时显示 -->
      <el-pagination
        v-if="filteredUserList.length > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="filteredUserList.length"
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
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" style="width: 100%">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentUser.realName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 'active' ? 'success' : 'danger'">
            {{ currentUser.status === 'active' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.registerTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ formatDateTime(currentUser.lastLoginTime) || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { 
  Plus, View, Search, Refresh, FolderOpened, Edit, Delete
} from '@element-plus/icons-vue'

interface User {
  id: number
  username: string
  phone: string
  email: string
  realName: string
  status: 'active' | 'disabled'
  registerTime: string
  lastLoginTime?: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 时间格式化函数
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

const formatDateTime = (dateStr: string | undefined) => {
  return dateStr || ''
}

const searchForm = reactive({
  username: '',
  phone: '',
  status: ''
})

const userForm = reactive({
  username: '',
  phone: '',
  email: '',
  realName: '',
  password: '',
  status: 'active' as 'active' | 'disabled'
})

const currentUser = ref<User>({} as User)
const userList = ref<User[]>([])
const originalUserList = ref<User[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// 判断是否有搜索条件
const hasSearchCondition = computed(() => {
  return !!(searchForm.username.trim() || searchForm.phone.trim() || searchForm.status)
})

// 过滤后的用户列表
const filteredUserList = computed(() => {
  let filtered = [...userList.value]
  
  // 用户名搜索 - 支持用户名和真实姓名模糊匹配
  if (searchForm.username.trim()) {
    const searchTerm = searchForm.username.trim().toLowerCase()
    filtered = filtered.filter(item => 
      item.username.toLowerCase().includes(searchTerm) ||
      item.realName.toLowerCase().includes(searchTerm)
    )
  }
  
  // 手机号搜索 - 部分匹配
  if (searchForm.phone.trim()) {
    const searchPhone = searchForm.phone.trim()
    filtered = filtered.filter(item => 
      item.phone.includes(searchPhone)
    )
  }
  
  // 状态搜索
  if (searchForm.status) {
    filtered = filtered.filter(item => item.status === searchForm.status)
  }
  
  return filtered
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 切换用户状态（启用/禁用）
const toggleUserStatus = async (row: User) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  const newStatus = row.status === 'active' ? 'disabled' : 'active'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 更新用户状态
    const index = userList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      userList.value[index].status = newStatus
      
      // 同时更新原始列表
      const originalIndex = originalUserList.value.findIndex(item => item.id === row.id)
      if (originalIndex !== -1) {
        originalUserList.value[originalIndex].status = newStatus
      }
    }
    
    ElMessage.success(`用户已${action}`)
  } catch {
    // 用户取消操作
  }
}

const loadUserList = async () => {
  loading.value = true
  try {
    const mockData: User[] = [
      {
        id: 1,
        username: 'user001',
        phone: '13888888888',
        email: 'user001@example.com',
        realName: '张三',
        status: 'active',
        registerTime: '2024-01-15 10:00:00',
        lastLoginTime: '2024-01-16 14:30:00'
      },
      {
        id: 2,
        username: 'user002',
        phone: '13999999999',
        email: 'user002@example.com',
        realName: '李四',
        status: 'active',
        registerTime: '2024-01-14 09:00:00',
        lastLoginTime: '2024-01-15 16:20:00'
      },
      {
        id: 3,
        username: 'user003',
        phone: '13777777777',
        email: 'user003@example.com',
        realName: '王五',
        status: 'disabled',
        registerTime: '2024-01-13 15:00:00',
        lastLoginTime: '2024-01-13 16:00:00'
      },
      {
        id: 4,
        username: 'admin123',
        phone: '13666666666',
        email: 'admin123@example.com',
        realName: '管理员',
        status: 'active',
        registerTime: '2024-01-12 08:00:00',
        lastLoginTime: '2024-01-16 10:00:00'
      },
      {
        id: 5,
        username: 'test_user',
        phone: '13555555555',
        email: 'test@example.com',
        realName: '测试用户',
        status: 'disabled',
        registerTime: '2024-01-11 12:00:00',
        lastLoginTime: '2024-01-11 15:00:00'
      }
    ]
    
    originalUserList.value = [...mockData]
    userList.value = [...mockData]
    pagination.total = mockData.length
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (row: User) => {
  isEdit.value = true
  dialogVisible.value = true
  currentUser.value = row
  Object.assign(userForm, row)
}

const handleView = (row: User) => {
  currentUser.value = row
  detailDialogVisible.value = true
}

const handleDelete = async (row: User) => {
  if (row.status === 'active') {
    ElMessage.warning('请先禁用该用户后再删除')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 从列表中删除该记录
    userList.value = userList.value.filter(item => item.id !== row.id)
    
    // 同时从原始列表中删除
    originalUserList.value = originalUserList.value.filter(item => item.id !== row.id)
    
    // 更新分页信息
    pagination.total = userList.value.length
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑现有用户
          const index = userList.value.findIndex(item => item.id === currentUser.value.id)
          if (index !== -1) {
            // 创建一个新对象，合并当前用户信息和表单数据
            const updatedUser = {
              ...currentUser.value,
              phone: userForm.phone,
              email: userForm.email,
              realName: userForm.realName,
              status: userForm.status
            }
            
            // 更新列表中的用户
            userList.value[index] = updatedUser
            
            // 同时更新原始列表
            const originalIndex = originalUserList.value.findIndex(item => item.id === currentUser.value.id)
            if (originalIndex !== -1) {
              originalUserList.value[originalIndex] = updatedUser
            }
          }
          
          ElMessage.success('用户信息已更新')
        } else {
          // 新增用户
          const newId = Math.max(...userList.value.map(item => item.id), 0) + 1
          
          const newUser: User = {
            id: newId,
            username: userForm.username,
            phone: userForm.phone,
            email: userForm.email,
            realName: userForm.realName,
            status: userForm.status,
            registerTime: new Date().toLocaleString(),
            lastLoginTime: undefined
          }
          
          // 添加到列表
          userList.value.unshift(newUser)
          originalUserList.value.unshift(newUser)
          
          // 更新分页信息
          pagination.total = userList.value.length
          
          ElMessage.success('新用户已创建')
        }
        
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleSearch = () => {
  const filtered = filteredUserList.value
  pagination.page = 1
  
  if (filtered.length === 0 && hasSearchCondition.value) {
    ElMessage.warning('未找到符合条件的用户')
  } else if (filtered.length > 0 && hasSearchCondition.value) {
    ElMessage.success(`找到 ${filtered.length} 条结果`)
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    phone: '',
    status: ''
  })
  pagination.page = 1
  ElMessage.success('搜索条件已重置')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
}

const resetForm = () => {
  Object.assign(userForm, {
    username: '',
    phone: '',
    email: '',
    realName: '',
    password: '',
    status: 'active' as 'active' | 'disabled'
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-manage {
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

/* 修复表单布局 */
:deep(.el-form--inline) {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px;
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 8px !important;
  padding: 0 !important;
  line-height: 1.2;
  height: auto !important;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
  line-height: 1;
  display: flex;
  align-items: center;
  height: 34px;
}

/* 🔧 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table tbody tr:hover > td) {
  background-color: #f5f7fa !important;
}

/* 🔧 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap; /* 防止按钮换行 */
}

.action-buttons .el-button {
  margin: 0 !important;
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
  min-width: auto;
}

.action-buttons .el-button .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 状态单元格样式 */
.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.danger-text {
  color: #F56C6C;
}

.success-text {
  color: #67C23A;
}

:deep(.el-button--text) {
  padding: 0 4px;
  height: auto;
  font-size: 12px;
}

:deep(.el-button--text:hover) {
  background: transparent;
  opacity: 0.8;
}

/* 修复下拉框样式 */
:deep(.el-select) {
  width: 140px;
  
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

:deep(.el-input) {
  width: 140px;
  
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

:deep(.el-select-dropdown) {
  min-width: 140px;
}

:deep(.el-select-dropdown__item) {
  padding: 0 12px;
  height: 34px;
  line-height: 34px;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: visible;
  text-overflow: clip;
}

:deep(.el-button) {
  height: 34px;
  font-size: 13px;
  border-radius: 6px;
  padding: 0 16px;
  margin: 0 4px;
}

:deep(.el-button:first-child) {
  margin-left: 0;
}

:deep(.el-button:last-child) {
  margin-right: 0;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 空状态样式 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-text h4 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px 0;
}

.empty-text p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.empty-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.empty-actions .el-button {
  border-radius: 6px;
  margin: 0;
}

/* 对话框样式改进 */
:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

/* 表单样式优化 */
:deep(.el-dialog .el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

:deep(.el-dialog .el-input__wrapper),
:deep(.el-dialog .el-select .el-input__wrapper) {
  width: 100%;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-dialog .el-input),
:deep(.el-dialog .el-select) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-form--inline) {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  :deep(.el-form--inline .el-form-item) {
    width: 100%;
  }
  
  :deep(.el-select),
  :deep(.el-input) {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
