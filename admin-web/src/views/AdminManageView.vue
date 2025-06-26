<template>
  <div class="admin-manage">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增管理员
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="状态">
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

    <!-- 管理员列表 -->
    <el-card>
      <el-table :data="adminList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'super_admin' ? 'danger' : 'primary'">
              {{ scope.row.role === 'super_admin' ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>查看详情
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.status === 'active'"
            >
              删除
            </el-button>
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

    <!-- 新增/编辑对话框 - 完全自定义 -->
    <el-dialog
      v-model="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :draggable="false"
      :show-close="false"
      :before-close="handleDialogClose"
      class="admin-dialog"
      @open="handleDialogOpen"
      @close="handleDialogClose"
    >
      <!-- 完全自定义头部 -->
      <template #header>
        <div class="dialog-header">
          <div class="header-left">
            <div class="header-icon">
              <el-icon size="20"><UserFilled /></el-icon>
            </div>
            <div>
              <h4 class="dialog-title">
                {{ dialogTitle }}
              </h4>
              <p class="dialog-subtitle">
                {{ isEdit ? '修改管理员信息' : '创建新的管理员账户' }}
              </p>
            </div>
          </div>
          <el-button 
            @click="closeDialog" 
            :icon="Close" 
            text 
            class="close-btn"
            title="关闭"
          />
        </div>
      </template>

      <div class="dialog-content">
        <el-form
          ref="formRef"
          :model="adminForm"
          :rules="rules"
          label-width="120px"
          class="admin-form"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input 
                    v-model="adminForm.username" 
                    :disabled="isEdit"
                    placeholder="请输入用户名"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="邮箱地址" prop="email">
                  <el-input 
                    v-model="adminForm.email" 
                    placeholder="请输入邮箱地址"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input 
                    v-model="adminForm.realName" 
                    placeholder="请输入真实姓名"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input 
                    v-model="adminForm.phone" 
                    placeholder="请输入手机号码"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Phone /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 账户设置 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              <span>账户设置</span>
            </div>
            
            <el-row :gutter="24" v-if="!isEdit">
              <el-col :span="12">
                <el-form-item label="登录密码" prop="password">
                  <el-input 
                    v-model="adminForm.password" 
                    type="password" 
                    placeholder="请输入登录密码"
                    size="large"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="adminForm.confirmPassword" 
                    type="password" 
                    placeholder="请再次输入密码"
                    size="large"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="管理员角色" prop="role">
                  <el-select 
                    v-model="adminForm.role" 
                    placeholder="请选择管理员角色"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option 
                      label="普通管理员" 
                      value="admin"
                    >
                      <div class="option-item">
                        <el-icon><User /></el-icon>
                        <div>
                          <div>普通管理员</div>
                          <div class="option-desc">拥有基本管理权限</div>
                        </div>
                      </div>
                    </el-option>
                    <el-option 
                      label="超级管理员" 
                      value="super_admin"
                    >
                      <div class="option-item">
                        <el-icon><UserFilled /></el-icon>
                        <div>
                          <div>超级管理员</div>
                          <div class="option-desc">拥有所有管理权限</div>
                        </div>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="账户状态" prop="status">
                  <el-select 
                    v-model="adminForm.status" 
                    placeholder="请选择账户状态"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option label="启用" value="active">
                      <div class="option-item">
                        <el-icon color="#67C23A"><CircleCheck /></el-icon>
                        <span>启用</span>
                      </div>
                    </el-option>
                    <el-option label="禁用" value="disabled">
                      <div class="option-item">
                        <el-icon color="#F56C6C"><CircleClose /></el-icon>
                        <span>禁用</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 权限设置 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><Key /></el-icon>
              <span>权限设置</span>
            </div>
            
            <el-form-item label="管理权限" prop="permissions">
              <el-checkbox-group v-model="adminForm.permissions" class="permission-group">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-checkbox label="user_manage" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">用户管理</div>
                        <div class="permission-desc">管理系统用户</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                  <el-col :span="8">
                    <el-checkbox label="house_manage" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">房源管理</div>
                        <div class="permission-desc">管理房源信息</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                  <el-col :span="8">
                    <el-checkbox label="sell_house_manage" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">卖房管理</div>
                        <div class="permission-desc">审核卖房申请</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-checkbox label="admin_manage" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">管理员管理</div>
                        <div class="permission-desc">管理其他管理员</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                  <el-col :span="8">
                    <el-checkbox label="system_settings" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">系统设置</div>
                        <div class="permission-desc">配置系统参数</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                  <el-col :span="8">
                    <el-checkbox label="data_export" class="permission-item">
                      <div class="permission-content">
                        <div class="permission-title">数据导出</div>
                        <div class="permission-desc">导出系统数据</div>
                      </div>
                    </el-checkbox>
                  </el-col>
                </el-row>
              </el-checkbox-group>
            </el-form-item>
          </div>

          <!-- 备注信息 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><EditPen /></el-icon>
              <span>备注信息</span>
            </div>
            
            <el-form-item label="备注说明" prop="remark">
              <el-input 
                v-model="adminForm.remark" 
                type="textarea" 
                :rows="4"
                placeholder="请输入备注信息（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="handleSubmit" size="large">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="管理员详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentAdmin.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentAdmin.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentAdmin.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentAdmin.role === 'super_admin' ? 'danger' : 'primary'">
            {{ currentAdmin.role === 'super_admin' ? '超级管理员' : '普通管理员' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentAdmin.status === 'active' ? 'success' : 'danger'">
            {{ currentAdmin.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentAdmin.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ formatDateTime(currentAdmin.lastLoginTime) || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { 
  Plus, View, User, UserFilled, Message, Phone, Setting, Lock, 
  Key, EditPen, Close, Check, CircleCheck, CircleClose 
} from '@element-plus/icons-vue'

interface Admin {
  id: number
  username: string
  email: string
  realName?: string
  phone?: string
  role: 'admin' | 'super_admin'
  status: 'active' | 'disabled'
  permissions?: string[]
  remark?: string
  createTime: string
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
  status: ''
})

// 扩展 adminForm 字段
const adminForm = reactive({
  username: '',
  email: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'admin' as 'admin' | 'super_admin',
  status: 'active' as 'active' | 'disabled',
  permissions: [] as string[],
  remark: ''
})

const currentAdmin = ref<Admin>({} as Admin)
const adminList = ref<Admin[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑管理员' : '新增管理员')

// 验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== adminForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 模态框打开时锁定背景滚动
const handleDialogOpen = () => {
  document.body.style.overflow = 'hidden'
}

// 模态框关闭时恢复背景滚动
const handleDialogClose = () => {
  document.body.style.overflow = ''
  resetForm()
}

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
}

// 其他方法保持不变...
const loadAdminList = async () => {
  loading.value = true
  try {
    const mockData: Admin[] = [
      {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        realName: '系统管理员',
        phone: '13800138000',
        role: 'super_admin',
        status: 'active',
        permissions: ['user_manage', 'house_manage', 'admin_manage', 'system_settings'],
        createTime: '2024-01-01 10:00:00',
        lastLoginTime: '2024-01-15 14:30:00'
      },
      {
        id: 2,
        username: 'manager1',
        email: 'manager1@example.com',
        realName: '张经理',
        phone: '13900139000',
        role: 'admin',
        status: 'active',
        permissions: ['user_manage', 'house_manage'],
        createTime: '2024-01-05 10:00:00',
        lastLoginTime: '2024-01-14 09:15:00'
      }
    ]
    
    adminList.value = mockData
    pagination.total = mockData.length
  } catch (error) {
    ElMessage.error('加载管理员列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (row: Admin) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(adminForm, {
    ...row,
    confirmPassword: ''
  })
}

const handleView = (row: Admin) => {
  currentAdmin.value = row
  detailDialogVisible.value = true
}

const handleDelete = async (row: Admin) => {
  if (row.status === 'active') {
    ElMessage.warning('请先禁用该管理员后再删除')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该管理员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('删除成功')
    loadAdminList()
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
        loadAdminList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleSearch = () => {
  pagination.page = 1
  loadAdminList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    status: ''
  })
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadAdminList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadAdminList()
}

const resetForm = () => {
  Object.assign(adminForm, {
    username: '',
    email: '',
    realName: '',
    phone: '',
    password: '',
    confirmPassword: '',
    role: 'admin' as 'admin' | 'super_admin',
    status: 'active' as 'active' | 'disabled',
    permissions: [],
    remark: ''
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadAdminList()
})
</script>

<style scoped>
.admin-manage {
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
  margin-top: 2px;
  justify-content: flex-end;
}

/* 对话框样式 */
.admin-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

/* 完全隐藏默认头部 */
.admin-dialog :deep(.el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
  border-bottom: none !important;
}

.admin-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.admin-dialog :deep(.el-dialog__footer) {
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

/* 自定义对话框头部 */
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: white;
}

.dialog-subtitle {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
}

.close-btn {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: scale(1.1);
}

/* 对话框内容 */
.dialog-content {
  padding: 32px;
  max-height: 65vh;
  overflow-y: auto;
}

.admin-form {
  width: 100%;
}

/* 表单分组 */
.form-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
}

.section-title .el-icon {
  color: #667eea;
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

:deep(.el-input--large .el-input__wrapper) {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

:deep(.el-input--large .el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

:deep(.el-input--large .el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-select--large .el-input__wrapper) {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
}

/* 选项样式 */
.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-desc {
  font-size: 12px;
  color: #64748b;
}

/* 权限设置 */
.permission-group {
  width: 100%;
}

.permission-item {
  width: 100%;
  margin-bottom: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.permission-item:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.permission-item.is-checked {
  border-color: #667eea;
  background: #f0f4ff;
}

.permission-content {
  margin-left: 8px;
}

.permission-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.permission-desc {
  font-size: 12px;
  color: #64748b;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  min-width: 100px;
  border-radius: 8px;
  font-weight: 600;
}

/* 滚动条样式 */
.dialog-content::-webkit-scrollbar {
  width: 6px;
}

.dialog-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dialog {
    width: 95% !important;
    margin: 5vh auto;
  }
  
  .dialog-content {
    padding: 20px;
    max-height: 60vh;
  }
  
  .form-section {
    padding: 16px;
  }
  
  .permission-item {
    padding: 12px;
  }
}
</style>
