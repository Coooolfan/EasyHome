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
        <el-table-column prop="createTime" label="创建时间" />
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="adminForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="adminForm.password" type="password" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="adminForm.role" style="width: 100%">
            <el-option label="普通管理员" value="admin" />
            <el-option label="超级管理员" value="super_admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="adminForm.status" style="width: 100%">
            <el-option label="启用" value="active" />
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
        <el-descriptions-item label="创建时间">{{ currentAdmin.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ currentAdmin.lastLoginTime || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Admin {
  id: number
  username: string
  email: string
  role: 'admin' | 'super_admin'
  status: 'active' | 'disabled'
  createTime: string
  lastLoginTime?: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  username: '',
  status: ''
})

// 修复 adminForm 的类型声明
const adminForm = reactive({
  username: '',
  email: '',
  password: '',
  role: 'admin' as 'admin' | 'super_admin',
  status: 'active' as 'active' | 'disabled'
})

const currentAdmin = ref<Admin>({} as Admin)
const adminList = ref<Admin[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑管理员' : '新增管理员')

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const loadAdminList = async () => {
  loading.value = true
  try {
    // 修复 mockData 的类型声明
    const mockData: Admin[] = [
      {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'super_admin',
        status: 'active',
        createTime: '2024-01-01 10:00:00',
        lastLoginTime: '2024-01-15 14:30:00'
      },
      {
        id: 2,
        username: 'manager1',
        email: 'manager1@example.com',
        role: 'admin',
        status: 'active',
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
  Object.assign(adminForm, row)
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
    
    // 调用删除API
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
        // 调用保存API
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

// 修复 resetForm 的类型声明
const resetForm = () => {
  Object.assign(adminForm, {
    username: '',
    email: '',
    password: '',
    role: 'admin' as 'admin' | 'super_admin',
    status: 'active' as 'active' | 'disabled'
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
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
