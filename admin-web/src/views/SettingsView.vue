<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Setting /></el-icon>
              <span>系统设置</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="settings-tabs">
            <!-- 基础设置 -->
            <el-tab-pane label="基础设置" name="basic">
              <el-form :model="basicSettings" label-width="120px">
                <el-form-item label="系统名称">
                  <el-input v-model="basicSettings.systemName" />
                </el-form-item>
                
                <el-form-item label="系统描述">
                  <el-input v-model="basicSettings.systemDesc" type="textarea" :rows="3" />
                </el-form-item>
                
                <el-form-item label="管理员邮箱">
                  <el-input v-model="basicSettings.adminEmail" />
                </el-form-item>
                
                <el-form-item label="系统公告">
                  <el-input v-model="basicSettings.systemNotice" type="textarea" :rows="4" />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <el-form :model="securitySettings" label-width="120px">
                <el-form-item label="登录超时">
                  <el-select v-model="securitySettings.loginTimeout">
                    <el-option label="30分钟" value="30" />
                    <el-option label="1小时" value="60" />
                    <el-option label="2小时" value="120" />
                    <el-option label="8小时" value="480" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="密码强度">
                  <el-radio-group v-model="securitySettings.passwordStrength">
                    <el-radio label="low">低（6位以上）</el-radio>
                    <el-radio label="medium">中（8位包含数字字母）</el-radio>
                    <el-radio label="high">高（8位包含数字字母特殊字符）</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="登录验证码">
                  <el-switch v-model="securitySettings.enableCaptcha" />
                  <span class="form-help">开启后登录需要输入验证码</span>
                </el-form-item>
                
                <el-form-item label="登录日志">
                  <el-switch v-model="securitySettings.enableLoginLog" />
                  <span class="form-help">记录用户登录日志</span>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 邮件设置 -->
            <el-tab-pane label="邮件设置" name="email">
              <el-form :model="emailSettings" label-width="120px">
                <el-form-item label="SMTP服务器">
                  <el-input v-model="emailSettings.smtpHost" />
                </el-form-item>
                
                <el-form-item label="SMTP端口">
                  <el-input v-model="emailSettings.smtpPort" type="number" />
                </el-form-item>
                
                <el-form-item label="发件人邮箱">
                  <el-input v-model="emailSettings.fromEmail" />
                </el-form-item>
                
                <el-form-item label="邮箱密码">
                  <el-input v-model="emailSettings.emailPassword" type="password" show-password />
                </el-form-item>
                
                <el-form-item label="启用SSL">
                  <el-switch v-model="emailSettings.enableSSL" />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="saveEmailSettings">保存设置</el-button>
                  <el-button @click="testEmailSettings">测试连接</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicSettings = reactive({
  systemName: 'Eazy Home 二手房交易管理系统',
  systemDesc: '专业的二手房交易管理平台，提供房源管理、用户管理等完整功能。',
  adminEmail: 'admin@eazyhome.com',
  systemNotice: '系统维护通知：本系统将于每周日凌晨2:00-4:00进行例行维护，请提前保存数据。'
})

const securitySettings = reactive({
  loginTimeout: '60',
  passwordStrength: 'medium',
  enableCaptcha: true,
  enableLoginLog: true
})

const emailSettings = reactive({
  smtpHost: 'smtp.qq.com',
  smtpPort: 587,
  fromEmail: 'system@eazyhome.com',
  emailPassword: '',
  enableSSL: true
})

const saveBasicSettings = () => {
  ElMessage.success('基础设置已保存')
}

const saveSecuritySettings = () => {
  ElMessage.success('安全设置已保存')
}

const saveEmailSettings = () => {
  ElMessage.success('邮件设置已保存')
}

const testEmailSettings = () => {
  ElMessage.info('正在测试邮件连接...')
  setTimeout(() => {
    ElMessage.success('邮件连接测试成功')
  }, 2000)
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  color: #409EFF;
}

.settings-tabs {
  margin-top: 20px;
}

.form-help {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
