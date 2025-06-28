<template>
  <div class="reservation-manage">
    <div class="page-header">
      <h2>预约管理</h2>
      <el-button type="primary" @click="refreshList">
        <el-icon>
          <Refresh />
        </el-icon>刷新列表
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="房源ID">
          <el-input v-model="searchForm.houseId" placeholder="请输入房源ID" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.approvalStatus" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约记录列表</span>
          <div class="header-actions">
            <el-switch v-model="showFullPhone" :disabled="!canViewFullPhone" inactive-text="脱敏显示" active-text="完整显示"
              @change="handlePhoneDisplayChange" />
          </div>
        </div>
      </template>

      <!-- 🔧 审核状态和重复预约改为普通文字 -->
      <el-table :data="reservationList" style="width: 100%" v-loading="loading" :row-style="{ height: '48px' }"
        :show-overflow-tooltip="true">
        <el-table-column prop="id" label="预约ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column prop="houseId" label="房源ID" width="80" align="center" />

        <el-table-column prop="houseTitle" label="房源标题" max-width="20" :show-overflow-tooltip="true" />

        <!-- 🔧 审核状态 - 普通文字显示 -->
        <el-table-column prop="approvalStatus" label="审核状态" width="100" align="center">
          <template #default="scope">
            {{ getStatusText(scope.row.approvalStatus) }}
          </template>
        </el-table-column>

        <el-table-column prop="submitTime" label="提交时间" width="110" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>

        <!-- 🔧 重复预约 - 普通文字显示 -->
        <el-table-column prop="isDuplicate" label="重复预约" width="100" align="center">
          <template #default="scope">
            {{ scope.row.isDuplicate ? '重复' : '正常' }}
          </template>
        </el-table-column>

        <el-table-column label="联系电话" width="150">
          <template #default="scope">
            <div class="phone-display">
              <span>{{ getDisplayPhone(scope.row.phone) }}</span>
              <el-button v-if="!showFullPhone && canViewFullPhone" type="text" size="small"
                @click="showFullPhoneTemp(scope.row)" class="phone-toggle">
                <el-icon>
                  <View />
                </el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row)" :icon="View">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页保持不变 -->
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" class="pagination" />
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="1000px">
      <div class="detail-container">
        <!-- 左侧：预约信息 -->
        <div class="detail-left">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="预约ID">{{ currentReservation.id }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ currentReservation.userId }}</el-descriptions-item>
            <el-descriptions-item label="房源ID">{{ currentReservation.houseId }}</el-descriptions-item>
            <el-descriptions-item label="房源标题">{{ currentReservation.houseTitle }}</el-descriptions-item>
            <el-descriptions-item label="用户姓名">{{ currentReservation.userName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">
              <span class="phone-detail">
                {{ canViewFullPhone ? currentReservation.phone : maskPhone(currentReservation.phone) }}
                <el-button v-if="!canViewFullPhone" type="text" size="small" @click="requestPhonePermission">
                  申请查看完整号码
                </el-button>
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="审核状态">
              {{ getStatusText(currentReservation.approvalStatus) }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDateTime(currentReservation.submitTime)
            }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatDateTime(currentReservation.approvalTime) || '未审核'
            }}</el-descriptions-item>
            <el-descriptions-item label="重复预约检测">
              {{ currentReservation.isDuplicate ? '检测到重复预约' : '正常预约' }}
            </el-descriptions-item>
            <el-descriptions-item label="预约备注">
              {{ currentReservation.remark || '无备注' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 右侧：审核操作 -->
        <div class="detail-right">
          <div class="approval-section">
            <!-- 审核通过 -->
            <div class="approval-card">
              <div class="approval-header">
                <el-icon class="approval-icon success">
                  <CircleCheck />
                </el-icon>
                <span class="approval-title">审核通过</span>
              </div>
              <div class="approval-content">
                <div v-if="currentReservation.approvalStatus === 'PENDING'">
                  <p>确认该预约信息无误，通过审核申请</p>
                  <p>审核备注（可选）：</p>
                  <el-input v-model="approveRemark" type="textarea" :rows="3" placeholder="请输入审核备注..." maxlength="200"
                    show-word-limit style="margin-bottom: 12px;" />
                  <el-button type="success" size="large" @click="handleApprove" style="width: 100%;">
                    <el-icon>
                      <CircleCheck />
                    </el-icon>
                    确认通过
                  </el-button>
                </div>
                <el-tag v-else-if="currentReservation.approvalStatus === 'APPROVED'" type="success">
                  已通过审核
                </el-tag>
                <p v-else>该预约已被拒绝，无法通过审核</p>
              </div>
            </div>

            <!-- 拒绝预约 -->
            <div class="approval-card">
              <div class="approval-header">
                <el-icon class="approval-icon danger">
                  <CircleClose />
                </el-icon>
                <span class="approval-title">拒绝预约</span>
              </div>
              <div class="approval-content">
                <div v-if="currentReservation.approvalStatus === 'PENDING'">
                  <p>请填写拒绝原因：</p>
                  <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因..." maxlength="200"
                    show-word-limit style="margin-bottom: 12px;" />
                  <el-button type="danger" size="large" @click="handleReject" style="width: 100%;"
                    :disabled="!rejectReason.trim()">
                    <el-icon>
                      <CircleClose />
                    </el-icon>
                    确认拒绝
                  </el-button>
                </div>
                <el-tag v-else-if="currentReservation.approvalStatus === 'REJECTED'" type="danger">
                  已拒绝审核
                </el-tag>
                <p v-else>该预约已通过审核，无法拒绝</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false" size="large">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, View, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import axios from 'axios'


interface Reservation {
  id: number
  userId: number
  houseId: number
  houseTitle: string
  userName: string
  phone: string
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  submitTime: string
  approvalTime?: string
  isDuplicate: boolean
  remark?: string
}
interface ApiAppointmentRecord {
  id: number
  houseId: number
  name: string
  phone: string
  time: string
  date: string
  remarks: string
  status: string
  userId: number
  createdAt: string
  updatedAt: string
}

const userStore = useUserStore()
const loading = ref(false)
const detailDialogVisible = ref(false)
const showFullPhone = ref(false)
const rejectReason = ref('') // 拒绝原因
const approveRemark = ref('') // 审核通过备注

// 权限控制 - 基于用户角色判断是否可以查看完整手机号
const canViewFullPhone = computed(() => {
  return userStore.userInfo.role === 'super_admin' || userStore.userInfo.role === 'admin'
})

// 搜索表单
const searchForm = reactive({
  userId: '',
  houseId: '',
  approvalStatus: ''
})

const currentReservation = ref<Reservation>({} as Reservation)
const reservationList = ref<Reservation[]>([])
// 原始数据存储，用于搜索筛选
const originalReservationList = ref<Reservation[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 修改时间格式化 - 只显示日期
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 时间格式化 - 显示完整时间（用于详情页）
const formatDateTime = (dateStr: string | undefined) => {
  return dateStr || ''
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'CONFIRMED': return '已确认'
    case 'CANCELLED': return '已取消'
    case 'PENDING': return '待处理'
    default: return '未知状态'
  }
}

// 手机号脱敏
const maskPhone = (phone: string) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 获取显示的手机号
const getDisplayPhone = (phone: string) => {
  if (!phone) return ''
  return showFullPhone.value && canViewFullPhone.value ? phone : maskPhone(phone)
}

// 手机号显示切换
const handlePhoneDisplayChange = (value: boolean) => {
  if (value && !canViewFullPhone.value) {
    ElMessage.warning('您没有权限查看完整手机号')
    showFullPhone.value = false
    return
  }
  ElMessage.success(value ? '已切换到完整显示' : '已切换到脱敏显示')
}

// 临时显示完整手机号
const showFullPhoneTemp = (row: Reservation) => {
  ElMessageBox.alert(
    `完整手机号：${row.phone}`,
    '联系电话',
    {
      confirmButtonText: '知道了',
      type: 'info'
    }
  )
}

// 申请手机号查看权限
const requestPhonePermission = () => {
  ElMessage.info('权限申请功能开发中，请联系系统管理员')
}

// 搜索筛选函数
// 🔧 修复搜索筛选函数
const filterReservations = (data: Reservation[]) => {
  console.log('搜索条件:', searchForm)
  console.log('原始数据:', data)

  return data.filter(item => {
    // 用户ID筛选 - 去除空格并转换为字符串
    if (searchForm.userId && searchForm.userId.trim()) {
      const searchUserId = searchForm.userId.trim()
      const itemUserId = item.userId.toString()
      console.log('用户ID比较:', searchUserId, 'vs', itemUserId, '包含:', itemUserId.includes(searchUserId))
      if (!itemUserId.includes(searchUserId)) {
        return false
      }
    }

    // 房源ID筛选 - 去除空格并转换为字符串
    if (searchForm.houseId && searchForm.houseId.trim()) {
      const searchHouseId = searchForm.houseId.trim()
      const itemHouseId = item.houseId.toString()
      console.log('房源ID比较:', searchHouseId, 'vs', itemHouseId, '包含:', itemHouseId.includes(searchHouseId))
      if (!itemHouseId.includes(searchHouseId)) {
        return false
      }
    }

    // 审核状态筛选
    if (searchForm.approvalStatus && searchForm.approvalStatus.trim()) {
      console.log('状态比较:', searchForm.approvalStatus, 'vs', item.approvalStatus)
      if (item.approvalStatus !== searchForm.approvalStatus) {
        return false
      }
    }

    return true
  })
}


// 更新分页信息
const updatePagination = (filteredData: Reservation[]) => {
  pagination.total = filteredData.length
  const startIndex = (pagination.page - 1) * pagination.size
  const endIndex = startIndex + pagination.size
  reservationList.value = filteredData.slice(startIndex, endIndex)
}

// 修改加载预约列表函数 - 使用真实API并获取房源标题
const loadReservationList = async (isSearch = false) => {
  loading.value = true
  try {
    // 如果不是搜索操作且原始数据为空，则加载数据
    if (!isSearch || originalReservationList.value.length === 0) {
      // 调用真实API获取数据
      const response = await axios.get<{
        code: string
        message: string
        data: ApiAppointmentRecord[]
        timestamp: number
      }>('/api/appointment/list')

      if (response.data && response.data.code === 'SUCCESS') {
        // 将后端数据转换为前端使用的格式
        const appointments = await Promise.all(response.data.data.map(async (item: ApiAppointmentRecord) => {
          // 构造提交时间字符串 (日期 + 时间)
          const submitTime = `${item.date} ${item.time}`;

          // 获取房源标题
          let houseTitle = '未知房源';
          try {
            const houseResponse = await axios.get(`/api/houses/getById/${item.houseId}`);
            if (houseResponse.data && houseResponse.data.code === 'SUCCESS' && houseResponse.data.data) {
              houseTitle = houseResponse.data.data.title || `房源${item.houseId}`;
            }
          } catch (error) {
            console.error(`获取房源(ID: ${item.houseId})标题失败:`, error);
          }

          return {
            id: item.id,
            userId: item.userId,
            houseId: item.houseId,
            houseTitle: houseTitle, // 使用API获取的房源标题
            userName: item.name || '未知用户',
            phone: item.phone || '',
            approvalStatus: item.status === 'CONFIRMED' ? 'CONFIRMED' :
              item.status === 'CANCELLED' ? 'CANCELLED' : 'PENDING',
            submitTime: item.createdAt || submitTime,
            approvalTime: item.updatedAt !== item.createdAt ? item.updatedAt : undefined,
            isDuplicate: false, // 数据中没有重复标记，可能需要后端提供
            remark: item.remarks || ''
          } as Reservation
        }))

        // 更新原始数据
        originalReservationList.value = appointments

        console.log('✅ 成功加载预约列表:', appointments.length)
      } else {
        throw new Error(response.data?.message || '获取预约列表失败')
      }
    }

    // 根据搜索条件筛选数据
    const filteredData = filterReservations(originalReservationList.value)

    // 更新分页和显示数据
    updatePagination(filteredData)

  } catch (error) {
    console.error('❌ 加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'PENDING': return 'warning'
    default: return 'info'
  }
}

// 审核通过 - 包含备注信息 - 修改为使用真实API
const handleApprove = async () => {
  try {
    let confirmMessage = '确定要审核通过这个预约吗？'
    if (approveRemark.value.trim()) {
      confirmMessage += `\n\n审核备注：${approveRemark.value.trim()}`
    }

    await ElMessageBox.confirm(confirmMessage, '确认审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 构建请求参数
    const params = new URLSearchParams()
    params.append('id', currentReservation.value.id.toString())
    params.append('pass', 'true')
    
    // 添加备注（如果有）
    if (approveRemark.value.trim()) {
      params.append('reason', approveRemark.value.trim())
    }
    
    // 调用审核API
    const response = await axios.post('/api/admin/review/appointment/approve', params)
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('审核通过成功')
      detailDialogVisible.value = false
      approveRemark.value = '' // 清空备注
      
      // 刷新列表
      refreshList()
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error: any) {
    if (error.toString().includes('cancel')) {
      // 用户取消操作
      return
    }
    console.error('❌ 审核操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 拒绝预约 - 修改为使用真实API
const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    let confirmMessage = '确定要拒绝这个预约吗？'
    confirmMessage += `\n\n拒绝原因：${rejectReason.value.trim()}`
    
    await ElMessageBox.confirm(confirmMessage, '确认拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 构造请求参数
    const params = new URLSearchParams()
    params.append('id', currentReservation.value.id.toString())
    params.append('pass', 'false')
    params.append('reason', rejectReason.value.trim())
    
    // 调用拒绝API - 使用相同的接口但参数不同
    const response = await axios.post('/api/admin/review/appointment/approve', params)
    
    if (response.data && response.data.code === 'SUCCESS') {
      ElMessage.success('拒绝预约成功')
      detailDialogVisible.value = false
      rejectReason.value = '' // 清空原因
      
      // 刷新列表
      refreshList()
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error: any) {
    if (error.toString().includes('cancel')) {
      // 用户取消操作
      return
    }
    console.error('❌ 拒绝操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 查看详情
const handleViewDetail = (row: Reservation) => {
  currentReservation.value = row
  rejectReason.value = '' // 重置拒绝原因
  approveRemark.value = '' // 重置审核备注
  detailDialogVisible.value = true
}

// 搜索功能 - 实现真正的搜索筛选
const handleSearch = () => {
  console.log('开始搜索，搜索条件:', searchForm)

  // 验证搜索条件 - 修改验证逻辑
  const hasUserId = searchForm.userId && searchForm.userId.trim()
  const hasHouseId = searchForm.houseId && searchForm.houseId.trim()
  const hasStatus = searchForm.approvalStatus && searchForm.approvalStatus.trim()

  const hasSearchCondition = hasUserId || hasHouseId || hasStatus

  if (!hasSearchCondition) {
    ElMessage.warning('请输入至少一个搜索条件')
    return
  }

  // 重置到第一页
  pagination.page = 1

  // 执行搜索
  console.log('执行搜索，原始数据长度:', originalReservationList.value.length)
  const filteredData = filterReservations(originalReservationList.value)
  console.log('筛选后数据长度:', filteredData.length)

  // 更新分页和显示数据
  updatePagination(filteredData)

  // 搜索成功提示
  ElMessage.success(`搜索完成，找到 ${filteredData.length} 条记录`)
}


// 重置搜索 - 清空搜索条件并显示所有数据
const handleReset = () => {
  Object.assign(searchForm, {
    userId: '',
    houseId: '',
    approvalStatus: ''
  })

  // 重置分页
  pagination.page = 1

  // 显示所有数据
  updatePagination(originalReservationList.value)

  ElMessage.success('搜索条件已重置')
}

// 刷新列表 - 重新加载原始数据
const refreshList = () => {
  // 清空原始数据，强制重新加载
  originalReservationList.value = []

  // 重置搜索条件
  Object.assign(searchForm, {
    userId: '',
    houseId: '',
    approvalStatus: ''
  })

  // 重置分页
  pagination.page = 1

  loadReservationList(false)
  ElMessage.success('列表刷新成功')
}

// 分页函数 - 支持搜索状态下的分页
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1 // 重置到第一页

  // 重新筛选和分页
  const filteredData = filterReservations(originalReservationList.value)
  updatePagination(filteredData)
}

const handleCurrentChange = (page: number) => {
  pagination.page = page

  // 重新筛选和分页
  const filteredData = filterReservations(originalReservationList.value)
  updatePagination(filteredData)
}

onMounted(() => {
  loadReservationList()
})
</script>

<style scoped>
.reservation-manage {
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

/* 调整搜索表单布局 - 减少宽度，更紧凑 */
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

/* 按钮区域特殊处理 */
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

/* 调整输入控件宽度 - 更紧凑 */
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

/* 按钮样式调整 */
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.phone-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.phone-toggle {
  padding: 0;
  min-height: auto;
}

.phone-detail {
  display: flex;
  align-items: center;
  gap: 8px;
}


.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 表格行间距调整 */
:deep(.el-table .el-table__row) {
  height: 60px;
}

/* 🔧 表格单元格样式 */
:deep(.el-table .el-table__cell) {
  padding: 8px 8px !important;
  /* 减少左右内边距 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

:deep(.el-descriptions-item__label) {
  font-weight: 600;
}

/* 调整详情对话框样式 - 让左侧与右侧标题对齐 */
.detail-container {
  display: flex;
  gap: 30px;
  min-height: 500px;
  align-items: flex-start;
}

.detail-left {
  flex: 1;
  margin-top: 48px;
  /* 向下偏移，与右侧"审核操作"标题对齐 */
}

.detail-right {
  flex: 0 0 350px;
  border-left: 1px solid #ebeef5;
  padding-left: 30px;
}

.approval-section h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.approval-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.approval-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.approval-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.approval-icon.success {
  color: #67c23a;
  font-size: 20px;
}

.approval-icon.danger {
  color: #f56c6c;
  font-size: 20px;
}

.approval-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.approval-content p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

/* 修复 Element Plus 对话框滑动问题 */
:deep(.el-overlay-dialog) {
  padding-top: 2vh !important;
  padding-bottom: 2vh !important;
  overflow-y: auto !important;
  display: flex !important;
  align-items: flex-start !important;
  justify-content: center !important;
}

:deep(.el-dialog) {
  margin: 0 !important;
  max-height: 96vh !important;
  overflow: hidden !important;
  display: flex !important;
  flex-direction: column !important;
}

:deep(.el-dialog__header) {
  flex-shrink: 0 !important;
  padding: 20px 20px 0 20px !important;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: 20px !important;
  max-height: calc(96vh - 120px) !important;
}

:deep(.el-dialog__footer) {
  flex-shrink: 0 !important;
  padding: 0 20px 20px 20px !important;
}

/* 🔧 确保表格自适应 */
:deep(.el-table) {
  width: 100% !important;
  table-layout: fixed !important;
  /* 改为固定布局，确保列宽控制 */
}

/* 🔧 确保表格容器充分利用空间 */
:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  width: 100% !important;
}

:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  width: 100% !important;
}

/* 🔧 表格行高 */
:deep(.el-table .el-table__row) {
  height: 48px !important;
}

/* 🔧 优化标签样式 */
:deep(.el-tag--small) {
  height: 20px !important;
  line-height: 18px !important;
  padding: 0 6px !important;
  font-size: 11px !important;
  white-space: nowrap;
}

/* 🔧 优化按钮样式 */
:deep(.el-button--small) {
  height: 26px !important;
  padding: 4px 8px !important;
  font-size: 12px !important;
  border-radius: 4px !important;
}

/* 🔧 手机号显示区域 */
.phone-display {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.phone-toggle {
  padding: 2px !important;
  min-height: auto !important;
  height: 18px !important;
  width: 18px !important;
}

.phone-toggle :deep(.el-icon) {
  font-size: 12px;
}

/* 🔧 减少行高 */
:deep(.el-table .el-table__row) {
  height: 48px !important;
  /* 从60px减少到48px */
}

:deep(.el-table .el-table__cell) {
  padding: 8px 12px !important;
  /* 减少内边距 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

/* 🔧 表格头部文字不换行 */
:deep(.el-table .el-table__header .el-table__cell) {
  padding: 10px 8px !important;
  /* 减少左右内边距 */
  background-color: #fafafa;
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  white-space: nowrap !important;
  /* 强制不换行 */
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 🔧 优化标签样式 */
:deep(.el-tag--small) {
  height: 20px !important;
  line-height: 18px !important;
  padding: 0 6px !important;
  font-size: 11px !important;
}

/* 🔧 优化按钮样式 */
:deep(.el-button--small) {
  height: 26px !important;
  padding: 4px 10px !important;
  font-size: 12px !important;
  border-radius: 4px !important;
}

/* 🔧 手机号显示区域 */
.phone-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.phone-toggle {
  padding: 2px !important;
  min-height: auto !important;
  height: 20px !important;
}

.phone-toggle :deep(.el-icon) {
  font-size: 12px;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  :deep(.el-form--inline) {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  :deep(.el-form--inline .el-form-item) {
    width: 100%;
  }

  :deep(.el-form--inline .el-form-item:last-child) {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  :deep(.el-select),
  :deep(.el-input) {
    width: 100% !important;
  }

  :deep(.el-button) {
    width: 100%;
    justify-content: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 1200px) {
  .detail-container {
    flex-direction: column;
  }

  .detail-left {
    margin-top: 0;
    /* 移动端取消上边距 */
  }

  .detail-right {
    flex: none;
    border-left: none;
    border-top: 1px solid #ebeef5;
    padding-left: 0;
    padding-top: 30px;
  }

  :deep(.el-overlay-dialog) {
    padding: 1vh 10px !important;
  }

  :deep(.el-dialog) {
    width: calc(100vw - 20px) !important;
    max-width: none !important;
    max-height: 98vh !important;
  }

  :deep(.el-dialog__body) {
    max-height: calc(98vh - 120px) !important;
  }
}
</style>
