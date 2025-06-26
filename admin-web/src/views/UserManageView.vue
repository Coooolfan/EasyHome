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
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
         <!-- 修改注册时间列 -->
  <el-table-column prop="registerTime" label="注册时间" width="120">
    <template #default="scope">
      {{ formatDate(scope.row.registerTime) }}
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
        <!-- 详情页保持完整时间 -->
  <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.registerTime) }}</el-descriptions-item>
  <el-descriptions-item label="最后登录">{{ formatDateTime(currentUser.lastLoginTime) || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

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
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0] // 只返回日期部分，如 "2024-01-15"
}

const formatDateTime = (dateStr: string) => {
  return dateStr || '' // 返回完整时间用于详情页
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
  status: 'active'
})

const currentUser = ref<User>({} as User)
const userList = ref<User[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

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

const loadUserList = async () => {
  loading.value = true
  try {
    // 添加明确的类型声明
    const mockData: User[] = [
      {
        id: 1,
        username: 'user001',
        phone: '13888888888',
        email: 'user001@example.com',
        realName: '张三',
        status: 'active' as 'active' | 'disabled',
        registerTime: '2024-01-15 10:00:00',
        lastLoginTime: '2024-01-16 14:30:00'
      },
      {
        id: 2,
        username: 'user002',
        phone: '13999999999',
        email: 'user002@example.com',
        realName: '李四',
        status: 'active' as 'active' | 'disabled',
        registerTime: '2024-01-14 09:00:00',
        lastLoginTime: '2024-01-15 16:20:00'
      },
      {
        id: 3,
        username: 'user003',
        phone: '13777777777',
        email: 'user003@example.com',
        realName: '王五',
        status: 'disabled' as 'active' | 'disabled',
        registerTime: '2024-01-13 15:00:00',
        lastLoginTime: '2024-01-13 16:00:00'
      }
    ]
    
    userList.value = mockData
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
    
    // 调用删除API
    ElMessage.success('删除成功')
    loadUserList()
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
        loadUserList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    phone: '',
    status: ''
  })
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadUserList()
}

const resetForm = () => {
  Object.assign(userForm, {
    username: '',
    phone: '',
    email: '',
    realName: '',
    password: '',
    status: 'active'
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

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
