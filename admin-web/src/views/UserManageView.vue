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
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="全部" value="" />
            <el-option label="普通用户" value="role_user" />
            <el-option label="管理员" value="role_admin" />
          </el-select>
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
    <el-card class="table-card">
      <el-table 
        :data="userList" 
        style="width: 100%" 
        v-loading="loading"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'role_admin' ? 'danger' : 'info'" size="small">
              {{ scope.row.role === 'role_admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEnable" label="状态" width="130" align="center">
          <template #default="scope">
            <div class="status-wrapper">
              <el-tag :type="scope.row.isEnable ? 'success' : 'danger'" size="small">
                {{ scope.row.isEnable ? '正常' : '禁用' }}
              </el-tag>
              <el-button 
                type="text" 
                size="small" 
                @click.stop="handleToggleStatus(scope.row)"
                class="status-toggle-btn"
              >
                {{ scope.row.isEnable ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="150" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
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
                :disabled="scope.row.isEnable"
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
        v-if="pagination.total > 0"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 美化后的新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      @close="resetForm"
      destroy-on-close
      append-to-body
      center
      class="user-dialog"
    >
      <div class="dialog-content">
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="rules"
          label-position="top"
          class="dialog-form"
          label-width="auto"
        >
          <el-row :gutter="0" justify="center">
            <el-col :span="20">
              <el-form-item label="用户名" prop="username">
                <el-input 
                  v-model="userForm.username" 
                  :disabled="isEdit" 
                  placeholder="请输入用户名"
                  prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="20">
              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="userForm.phone" 
                  placeholder="请输入手机号"
                  prefix-icon="Iphone"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="20">
              <el-form-item label="邮箱" prop="email">
                <el-input 
                  v-model="userForm.email" 
                  placeholder="请输入邮箱"
                  prefix-icon="Message"
                />
              </el-form-item>
            </el-col>

            <el-col :span="20">
              <el-form-item label="角色" prop="role">
                <div class="role-container">
                  <el-radio-group v-model="userForm.role" size="large">
                    <el-radio-button label="role_user">
                      <el-icon><User /></el-icon> 普通用户
                    </el-radio-button>
                    <el-radio-button label="role_admin">
                      <el-icon><UserFilled /></el-icon> 管理员
                    </el-radio-button>
                  </el-radio-group>
                </div>
              </el-form-item>
            </el-col>
            
            <el-col :span="20" v-if="!isEdit">
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="userForm.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  show-password
                  prefix-icon="Lock"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="20">
              <el-form-item label="状态" prop="isEnable">
                <div class="status-switch">
                  <span :class="['status-label', {'active': userForm.isEnable === true}]">正常</span>
                  <el-switch
                    v-model="userForm.isEnable"
                    inline-prompt
                    class="status-switch-control"
                    active-color="#13ce66"
                    inactive-color="#ff4949"
                  />
                  <span :class="['status-label', {'active': userForm.isEnable === false}]">禁用</span>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="submit-btn">
            {{ isEdit ? '保存修改' : '创建用户' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 美化后的详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="用户详情" 
      width="520px" 
      center 
      class="user-dialog detail-dialog"
    >
      <div class="user-detail-header">
        <div class="avatar-container">
          <el-avatar :size="80" :icon="User" />
        </div>
        <div class="user-basic-info">
          <h3>{{ currentUser.username }}</h3>
          <div class="user-meta">
            <el-tag :type="currentUser.role === 'role_admin' ? 'danger' : 'info'" size="small">
              {{ currentUser.role === 'role_admin' ? '管理员' : '普通用户' }}
            </el-tag>
            <el-tag :type="currentUser.isEnable ? 'success' : 'danger'" size="small">
              {{ currentUser.isEnable ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <el-divider content-position="left">详细信息</el-divider>
      
      <div class="user-detail-content">
        <el-descriptions :column="1" border class="details-descriptions">
          <el-descriptions-item label="ID">
            <span class="info-text">{{ currentUser.id }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            <span class="info-text">{{ currentUser.phone || '未设置' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            <span class="info-text">{{ currentUser.email || '未设置' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            <span class="info-text">{{ formatDateTime(currentUser.createdAt) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="最后更新">
            <span class="info-text">{{ formatDateTime(currentUser.updatedAt) }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false" class="cancel-btn">关闭</el-button>
          <el-button type="primary" @click="handleEditFromDetail" class="submit-btn">编辑</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import axios from 'axios'
import { 
  Plus, View, Search, Refresh, FolderOpened, Edit, Delete, 
  User, UserFilled, Iphone, Message, Lock
} from '@element-plus/icons-vue'

// 修改接口定义以匹配后端返回的数据结构
interface User {
  id: number
  username: string
  password?: string
  phone: string
  email: string
  role: 'role_user' | 'role_admin'
  isEnable: boolean
  createdAt: string
  updatedAt: string
}

interface Pagination {
  current: number
  size: number
  total: number
}

interface UserQueryParams {
  username?: string
  phone?: string
  role?: string
  status?: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const userList = ref<User[]>([])
const currentUser = ref<User>({} as User)

// 分页参数
const pagination = reactive<Pagination>({
  current: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive<UserQueryParams>({
  username: '',
  phone: '',
  role: '',
  status: ''
})

// 用户表单 - 修改为匹配后端结构
const userForm = reactive({
  username: '',
  phone: '',
  email: '',
  role: 'role_user' as 'role_user' | 'role_admin',
  password: '',
  isEnable: true
})

// 时间格式化函数 - 修改为适用ISO日期格式
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch (e) {
    return dateStr
  }
}

const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
  }
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// 判断是否有搜索条件
const hasSearchCondition = computed(() => {
  return !!(searchForm.username || searchForm.phone || searchForm.role || searchForm.status)
})

// 表单验证规则 - 修改状态字段为isEnable
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
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  isEnable: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 修改加载用户列表函数 - 使用真实API
const loadUserList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const queryDTO: any = {}
    
    // 添加查询条件
    if (searchForm.username) queryDTO.username = searchForm.username
    if (searchForm.phone) queryDTO.phone = searchForm.phone
    if (searchForm.role) queryDTO.role = searchForm.role
    if (searchForm.status) queryDTO.isEnable = searchForm.status === 'active'

    // 调用后端接口获取用户列表
    const response = await axios.get('/api/admin/users', { 
      params: {
        current: pagination.current,
        size: pagination.size,
        userQueryDTO: queryDTO
      }
    })

    if (response.data && response.data.code === 'SUCCESS') {
      // 成功获取数据
      userList.value = response.data.data || []
      
      // 更新总数 - 根据实际API调整
      pagination.total = Array.isArray(response.data.data) ? response.data.data.length : 0
      
      console.log('✅ 成功加载用户列表:', userList.value.length)
    } else {
      throw new Error(response.data?.message || '获取用户列表失败')
    }
  } catch (error: any) {
    console.error('❌ 加载用户列表失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '加载用户列表失败')
    userList.value = []
  } finally {
    loading.value = false
  }
}

// 删除用户函数 - 使用真实API
const handleDelete = async (row: User) => {
  if (row.isEnable) {
    ElMessage.warning('请先禁用该用户后再删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, 
      '删除确认', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除用户API
    const response = await axios.delete('/api/user/delete', {
      params: { id: row.id }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('用户删除成功')
      // 重新加载用户列表
      loadUserList()
    } else {
      throw new Error(response.data?.message || '删除用户失败')
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('❌ 删除用户失败:', error)
    ElMessage.error(error.message || '删除用户失败')
  }
}

// 提交表单 - 添加/编辑用户 - 使用真实API
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          // 编辑现有用户 - 使用真实API
          const userUpdateDTO = {
            phone: userForm.phone,
            email: userForm.email,
            role: userForm.role,
            isEnable: userForm.isEnable
          }
          
          const response = await axios.put(`/api/admin/users/${currentUser.value.id}`, userUpdateDTO)
          
          if (response.data && response.data.code === 'SUCCESS') {
            ElMessage.success('用户信息更新成功')
            dialogVisible.value = false
            loadUserList() // 重新加载用户列表
          } else {
            throw new Error(response.data?.message || '更新用户失败')
          }
        } else {
          // 新增用户 - 使用真实API
          const userCreateDTO = {
            username: userForm.username,
            password: userForm.password,
            phone: userForm.phone,
            email: userForm.email,
            role: userForm.role,
            isEnable: userForm.isEnable
          }
          
          const response = await axios.post('/api/admin/users', userCreateDTO)
          
          if (response.data && response.data.code === 'SUCCESS') {
            ElMessage.success('用户创建成功')
            dialogVisible.value = false
            loadUserList() // 重新加载用户列表
          } else {
            throw new Error(response.data?.message || '创建用户失败')
          }
        }
      } catch (error: any) {
        console.error('❌ 操作失败:', error)
        ElMessage.error(error.response?.data?.message || error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 添加修改用户状态功能
const handleToggleStatus = async (row: User) => {
  try {
    const newStatus = !row.isEnable
    const confirmMessage = newStatus 
      ? `确定要启用用户 "${row.username}" 吗？` 
      : `确定要禁用用户 "${row.username}" 吗？${newStatus ? '' : '\n\n禁用后用户将无法登录系统'}`
    
    await ElMessageBox.confirm(
      confirmMessage,
      newStatus ? '启用确认' : '禁用确认',
      {
        confirmButtonText: newStatus ? '确定启用' : '确定禁用',
        cancelButtonText: '取消',
        type: newStatus ? 'info' : 'warning'
      }
    )
    
    // 调用修改用户状态API
    const response = await axios.put(`/api/admin/users/${row.id}/status`, null, {
      params: { isEnable: newStatus }
    })
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success(`用户${newStatus ? '启用' : '禁用'}成功`)
      // 重新加载用户列表
      loadUserList()
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('❌ 状态修改失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1 // 重置到第一页
  loadUserList()
}

// 处理页码变化
const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadUserList()
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1 // 重置到第一页
  loadUserList()
}

// 重置搜索条件
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    phone: '',
    role: '',
    status: ''
  })
  pagination.current = 1
  loadUserList()
  ElMessage.success('搜索条件已重置')
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

// 编辑用户 - 修改为匹配后端字段
const handleEdit = (row: User) => {
  isEdit.value = true
  dialogVisible.value = true
  currentUser.value = { ...row }
  
  // 重置表单并填充新值
  resetForm()
  Object.assign(userForm, {
    username: row.username,
    phone: row.phone,
    email: row.email,
    role: row.role,
    isEnable: row.isEnable
  })
}

// 从详情对话框跳转到编辑
const handleEditFromDetail = () => {
  detailDialogVisible.value = false
  handleEdit(currentUser.value)
}

// 查看用户详情
const handleView = (row: User) => {
  currentUser.value = { ...row }
  detailDialogVisible.value = true
}

// 重置表单 - 修改为匹配后端字段
const resetForm = () => {
  Object.assign(userForm, {
    username: '',
    phone: '',
    email: '',
    role: 'role_user',
    password: '',
    isEnable: true
  })
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 加载初始数据
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

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
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
  height: auto;
}

/* 表格样式优化 */
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

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
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

/* 修复下拉框样式 */
:deep(.el-select) {
  width: 140px;
}

:deep(.el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 6px;
  height: 34px;
}

:deep(.el-select .el-input__inner) {
  font-size: 13px;
  height: 34px;
  line-height: 34px;
  border: none;
  background: transparent;
  padding: 0 24px 0 12px !important;
  color: #606266;
}

:deep(.el-input) {
  width: 140px;
}

:deep(.el-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 6px;
  height: 34px;
}

:deep(.el-input .el-input__inner) {
  font-size: 13px;
  height: 34px;
  line-height: 34px;
  border: none;
  background: transparent;
  padding: 0 12px;
  color: #606266;
}

/* 改进对话框样式 */
.user-dialog {
  :deep(.el-dialog__body) {
    padding: 24px 24px 30px;
  }
  
  :deep(.el-dialog__header) {
    margin: 0;
    padding: 20px 30px;
    border-bottom: 1px solid #f0f0f0;
    text-align: center;
  }
  
  :deep(.el-dialog__title) {
    font-weight: 600;
    font-size: 18px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 16px 30px;
    border-top: 1px solid #f0f0f0;
    margin-top: 0;
  }
}

/* 对话框内容区域 */
.dialog-content {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

/* 改进对话框表单样式 */
.dialog-form {
  width: 100%;
  padding: 5px 0;
}

:deep(.dialog-form .el-form-item) {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

:deep(.dialog-form .el-form-item:last-child) {
  margin-bottom: 5px;
}

:deep(.dialog-form .el-form-item__label) {
  padding-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  color: #303133;
  text-align: left;
}

:deep(.dialog-form .el-input__wrapper),
:deep(.dialog-form .el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 0 12px;
  border-radius: 4px;
  height: 40px;
  width: 100%;
}

:deep(.dialog-form .el-input__inner) {
  height: 40px;
  font-size: 14px;
}

:deep(.dialog-form .el-input),
:deep(.dialog-form .el-select) {
  width: 100%;
}

/* 角色选择按钮组容器 */
.role-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 角色单选按钮组样式 */
:deep(.el-radio-group) {
  display: flex;
  justify-content: center;
  gap: 20px;
  width: auto;
}

:deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 40px;
  padding: 0 20px;
}

:deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 4px 0 0 4px;
}

:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 4px 4px 0;
}

/* 状态切换开关 */
.status-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  width: 100%;
  height: 40px;
}

.status-label {
  font-size: 14px;
  color: #909399;
  transition: color 0.3s;
}

.status-label.active {
  font-weight: 500;
  color: #303133;
}

.status-switch-control {
  transform: scale(1.2);
}

/* 详情对话框样式 */
.user-detail-header {
  display: flex;
  align-items: center;
  padding: 10px 0 20px;
}

.avatar-container {
  margin-right: 20px;
}

.user-basic-info {
  flex: 1;
}

.user-basic-info h3 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #303133;
}

.user-meta {
  display: flex;
  gap: 10px;
}

.user-detail-content {
  margin-top: 10px;
}

/* 详情描述列表 */
.details-descriptions {
  margin: 10px 0;
}

.details-descriptions :deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
  color: #303133;
  background-color: #fafafa;
}

.details-descriptions :deep(.el-descriptions__content) {
  padding: 12px 15px;
}

.info-text {
  color: #606266;
}

/* 表单按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.submit-btn {
  min-width: 100px;
}

.cancel-btn {
  min-width: 80px;
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

/* 状态切换按钮样式 */
.status-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.status-toggle-btn {
  padding: 2px 4px;
  font-size: 12px;
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

  :deep(.el-radio-group) {
    flex-direction: column;
    gap: 10px;
  }

  :deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-radius: 4px;
  }

  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 4px;
  }
}
</style>