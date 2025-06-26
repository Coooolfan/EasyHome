<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const currentStep = ref(1)
const totalSteps = 4
const submitting = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)

// 表单数据结构
const formData = reactive({
  // 步骤1: 基本信息
  title: '',
  address: '',
  price: null as number | null,
  area: null as number | null,
  
  // 步骤2: 房屋详情
  rooms: '',
  floor: '',
  build_year: null as number | null,
  orientation: '',
  decoration: '',
  
  // 步骤3: 房屋标签和图片
  tag: [] as string[],
  image: '',
})

// 字段选项
const orientationOptions = ['东', '南', '西', '北', '东南', '东北', '西南', '西北', '南北', '四向']
const decorationOptions = ['毛坯', '简装', '中装', '精装', '豪装']
const roomOptions = ['1室', '2室', '3室', '4室', '5室及以上']
const tagOptions = [
  '地铁房', '学区房', '精装修', '南北通透', '满五唯一', 
  '低总价', '高楼层', '电梯房', '近公园', '首付低',
  '近商圈', '安静', '带花园', '带车位', '投资回报高'
]

// 计算单价
const calculateUnitPrice = () => {
  if (formData.price && formData.area && formData.area > 0) {
    return Math.round(formData.price / formData.area)
  }
  return 0
}

// 验证当前步骤
const validateCurrentStep = () => {
  switch (currentStep.value) {
    case 1:
      return !!formData.title && !!formData.address && 
             !!formData.price && !!formData.area &&
             formData.price > 0 && formData.area > 0
    case 2:
      return !!formData.rooms && !!formData.floor
    case 3:
      return !!formData.image
    default:
      return true
  }
}

// 下一步
const nextStep = () => {
  if (validateCurrentStep()) {
    if (currentStep.value < totalSteps) {
      currentStep.value++
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 验证图片文件
const validateImageFile = (file: File) => {
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    errorMessage.value = '请上传图片文件'
    return false
  }
  
  // 验证文件大小（限制为10MB）
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    errorMessage.value = '图片大小不能超过10MB'
    return false
  }
  
  return true
}

// 处理图片上传
const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    if (!validateImageFile(file)) return
    
    uploading.value = true
    uploadProgress.value = 0
    errorMessage.value = ''
    
    try {
      // 创建FormData对象
      const formDataUpload = new FormData()
      formDataUpload.append('file', file)
      
      // 发送上传请求到后端API
      const response = await axios.post('/api/common/upload', formDataUpload, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          // 计算上传进度
          uploadProgress.value = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 100)
          )
        }
      })
      
      // 处理成功响应 - 适应新的返回格式
      if (response.data && response.data.code === "SUCCESS") {
        formData.image = response.data.data  // 保存返回的图片URL
      } else {
        throw new Error(response.data?.message || '上传失败')
      }
    } catch (error: any) {
      console.error('图片上传失败', error)
      errorMessage.value = error.message || '图片上传失败，请重试'
    } finally {
      uploading.value = false
    }
  }
}

// 处理拖放图片上传
const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  const file = event.dataTransfer?.files[0]
  
  if (file && file.type.startsWith('image/')) {
    if (!validateImageFile(file)) return
    
    uploading.value = true
    uploadProgress.value = 0
    errorMessage.value = ''
    
    try {
      // 创建FormData对象
      const formDataUpload = new FormData()
      formDataUpload.append('file', file)
      
      // 发送上传请求到后端API
      const response = await axios.post('/api/common/upload', formDataUpload, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          // 计算上传进度
          uploadProgress.value = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 100)
          )
        }
      })
      
      // 处理成功响应 - 适应新的返回格式
      if (response.data && response.data.code === "SUCCESS") {
        formData.image = response.data.data  // 保存返回的图片URL
      } else {
        throw new Error(response.data?.message || '上传失败')
      }
    } catch (error: any) {
      console.error('图片上传失败', error)
      errorMessage.value = error.message || '图片上传失败，请重试'
    } finally {
      uploading.value = false
    }
  }
}


// 处理拖拽区域事件
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
}

// 处理标签的选择与取消
const toggleTag = (tag: string) => {
  const index = formData.tag.indexOf(tag)
  if (index > -1) {
    formData.tag.splice(index, 1)
  } else {
    formData.tag.push(tag)
  }
}

// 提交表单
const submitForm = async () => {
  if (!validateCurrentStep()) return
  
  submitting.value = true
  errorMessage.value = ''
  
  try {
    // 准备提交数据
    const submitData = {
      ...formData,
      unit_price: calculateUnitPrice(),
      tag: formData.tag.join(',') // 将标签数组转为逗号分隔的字符串
    }
    
    // 发送请求
    const response = await axios.post('/api/houses', submitData)
    
    // 处理成功响应
    successMessage.value = '房源发布成功！'
    setTimeout(() => {
      router.push('/my-listings') // 发布成功后跳转到我的房源列表
    }, 2000)
    
  } catch (error: any) {
    // 处理错误
    console.error('提交失败', error)
    errorMessage.value = error.response?.data?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

// 计算进度百分比
const progressPercentage = computed(() => {
  return ((currentStep.value - 1) / (totalSteps - 1)) * 100
})
</script>

<template>
    <main class="min-h-screen bg-gradient-to-b from-gray-50 to-white py-6">
        <div class="max-w-5xl mx-auto px-4">
            <div class="text-center mb-6">
                <h1 class="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-blue-400">
                    发布您的房源
                </h1>
                <p class="mt-2 text-base text-gray-600">
                    填写您的房屋信息，我们将帮助您快速找到买家
                </p>
            </div>

            <!-- 步骤进度指示器 - 采用简化的设计 -->
            <div class="mb-6">
                <div class="flex justify-between items-center">
                    <div 
                        v-for="step in totalSteps" 
                        :key="step"
                        class="flex flex-col items-center relative"
                        :class="step === totalSteps ? '' : 'flex-1'"
                    >
                        <div 
                            class="w-10 h-10 rounded-full flex items-center justify-center border-2 z-10 bg-white"
                            :class="[
                                step < currentStep 
                                    ? 'bg-blue-600 border-blue-600 text-white' 
                                    : step === currentStep
                                        ? 'bg-white border-blue-600 text-blue-600'
                                        : 'bg-white border-gray-300 text-gray-500'
                            ]"
                        >
                            <span v-if="step < currentStep">✓</span>
                            <span v-else class="text-base font-medium">{{ step }}</span>
                        </div>
                        <div class="text-center mt-1">
                            <span class="text-base" 
                                :class="step <= currentStep ? 'text-blue-600 font-medium' : 'text-gray-500'">
                                {{ step === 1 ? '基本信息' : 
                                   step === 2 ? '房屋详情' : 
                                   step === 3 ? '标签图片' : '确认提交' }}
                            </span>
                        </div>
                        <!-- 连接线 -->
                        <div 
                            v-if="step < totalSteps" 
                            class="absolute top-5 left-10 w-full h-0.5"
                            :class="step < currentStep ? 'bg-blue-600' : 'bg-gray-300'"
                        ></div>
                    </div>
                </div>
            </div>

            <div class="bg-white shadow-md rounded-xl overflow-hidden border border-gray-100">
                <div class="px-6 py-6">
                    <!-- 步骤 1: 基本信息 -->
                    <div v-if="currentStep === 1" class="animate-slideIn">
                        <div class="text-center mb-5">
                            <h2 class="text-2xl font-bold text-gray-800">房源基本信息</h2>
                        </div>

                        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                            <div class="form-group">
                                <label for="title" class="block text-base font-medium text-gray-700 mb-1">房源标题 <span class="text-red-500">*</span></label>
                                <input 
                                    v-model="formData.title" 
                                    type="text" 
                                    id="title"
                                    class="form-input mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                    placeholder="请输入吸引买家的标题"
                                    maxlength="200"
                                    required
                                >
                            </div>

                            <div class="form-group">
                                <label for="address" class="block text-base font-medium text-gray-700 mb-1">房源地址 <span class="text-red-500">*</span></label>
                                <input 
                                    v-model="formData.address" 
                                    type="text" 
                                    id="address"
                                    class="form-input mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                    placeholder="请输入详细地址"
                                    maxlength="300"
                                    required
                                >
                            </div>

                            <div class="form-group">
                                <label for="price" class="block text-base font-medium text-gray-700 mb-1">总价(元) <span class="text-red-500">*</span></label>
                                <div class="mt-1 relative rounded-lg shadow-sm">
                                    <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <span class="text-gray-500 text-lg">¥</span>
                                    </div>
                                    <input 
                                        v-model.number="formData.price" 
                                        type="number" 
                                        id="price"
                                        class="form-input block w-full pl-10 rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base py-3"
                                        placeholder="请输入房源售价"
                                        min="0"
                                        required
                                    >
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="area" class="block text-base font-medium text-gray-700 mb-1">面积(平方米) <span class="text-red-500">*</span></label>
                                <div class="mt-1 relative rounded-lg shadow-sm">
                                    <input 
                                        v-model.number="formData.area" 
                                        type="number" 
                                        id="area"
                                        class="form-input block w-full pr-12 rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base py-3"
                                        placeholder="请输入房屋面积"
                                        min="0"
                                        required
                                    >
                                    <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none">
                                        <span class="text-gray-500 text-base">㎡</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="mt-4">
                            <div class="form-group">
                                <label class="block text-base font-medium text-gray-700 mb-1">单价(元/平方米)</label>
                                <div class="mt-1 px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg font-medium text-gray-800 text-lg">
                                    ¥ {{ calculateUnitPrice().toLocaleString() }}
                                </div>
                                <p class="mt-2 text-sm text-gray-500">根据总价与面积自动计算</p>
                            </div>
                        </div>
                    </div>

                    <!-- 步骤 2: 房屋详情 -->
                    <div v-if="currentStep === 2" class="animate-slideIn">
                        <div class="text-center mb-5">
                            <h2 class="text-2xl font-bold text-gray-800">房屋详情信息</h2>
                        </div>
                        
                        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                            <div class="form-group">
                                <label for="rooms" class="block text-base font-medium text-gray-700 mb-1">户型 <span class="text-red-500">*</span></label>
                                <select 
                                    v-model="formData.rooms" 
                                    id="rooms"
                                    class="form-select mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                    required
                                >
                                    <option value="">请选择户型</option>
                                    <option v-for="room in roomOptions" :key="room" :value="room + '厅1卫'">
                                        {{ room }}厅1卫
                                    </option>
                                    <option value="其他">其他户型</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="floor" class="block text-base font-medium text-gray-700 mb-1">楼层 <span class="text-red-500">*</span></label>
                                <input 
                                    v-model="formData.floor" 
                                    type="text" 
                                    id="floor"
                                    class="form-input mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                    placeholder="如：5/18层"
                                    required
                                >
                            </div>

                            <div class="form-group">
                                <label for="build_year" class="block text-base font-medium text-gray-700 mb-1">建筑年代</label>
                                <input 
                                    v-model.number="formData.build_year" 
                                    type="number" 
                                    id="build_year"
                                    class="form-input mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                    placeholder="请输入建筑年代，如：2015"
                                    min="1900"
                                    :max="new Date().getFullYear()"
                                >
                            </div>

                            <div class="form-group">
                                <label for="orientation" class="block text-base font-medium text-gray-700 mb-1">朝向</label>
                                <select 
                                    v-model="formData.orientation" 
                                    id="orientation"
                                    class="form-select mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-shadow duration-200 text-base px-4 py-3"
                                >
                                    <option value="">请选择朝向</option>
                                    <option v-for="option in orientationOptions" :key="option" :value="option">
                                        {{ option }}
                                    </option>
                                </select>
                            </div>
                        </div>

                        <div class="mt-6">
                            <label class="block text-base font-medium text-gray-700 mb-2">装修情况</label>
                            <div class="flex flex-wrap gap-3">
                                <button 
                                    v-for="option in decorationOptions" 
                                    :key="option"
                                    type="button"
                                    @click="formData.decoration = option"
                                    :class="[
                                        'px-5 py-3 rounded-lg text-base transition-all duration-200',
                                        formData.decoration === option 
                                            ? 'bg-blue-100 text-blue-700 border-2 border-blue-300 font-medium'
                                            : 'bg-gray-100 text-gray-700 border border-gray-200 hover:bg-gray-200'
                                    ]"
                                >
                                    {{ option }}
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- 步骤 3: 标签和图片 -->
                    <div v-if="currentStep === 3" class="animate-slideIn">
                        <div class="text-center mb-5">
                            <h2 class="text-2xl font-bold text-gray-800">房源标签与图片</h2>
                        </div>
                        
                        <div class="mb-8">
                            <label class="block text-base font-medium text-gray-700 mb-3">房源特点标签</label>
                            <div class="flex flex-wrap gap-3">
                                <button
                                    v-for="tag in tagOptions" 
                                    :key="tag"
                                    type="button"
                                    @click="toggleTag(tag)"
                                    :class="[
                                        'px-4 py-2 rounded-full text-base font-medium transition-all duration-300',
                                        formData.tag.includes(tag)
                                            ? 'bg-blue-100 text-blue-700 border-2 border-blue-300 shadow-sm transform scale-105'
                                            : 'bg-gray-100 text-gray-700 border border-gray-200 hover:bg-gray-200'
                                    ]"
                                >
                                    {{ tag }}
                                    <span v-if="formData.tag.includes(tag)" class="ml-1">✓</span>
                                </button>
                            </div>
                        </div>

                        <div>
                            <label class="block text-base font-medium text-gray-700 mb-2">房源封面图片 <span class="text-red-500">*</span></label>
                            <div 
                                class="mt-2 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:bg-gray-50 transition-colors duration-200"
                                @dragover="handleDragOver"
                                @drop="handleDrop"
                            >
                                <!-- 上传进度显示 -->
                                <div v-if="uploading" class="w-full text-center py-8">
                                    <div class="flex flex-col items-center">
                                        <svg class="animate-spin h-10 w-10 text-blue-500 mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        <span class="text-lg font-medium text-gray-700">正在上传图片...</span>
                                        <div class="w-64 h-2 bg-gray-200 rounded-full mt-4 overflow-hidden">
                                            <div class="h-full bg-blue-500 rounded-full" :style="`width: ${uploadProgress}%`"></div>
                                        </div>
                                        <span class="text-sm text-gray-600 mt-2">{{ uploadProgress }}%</span>
                                    </div>
                                </div>
                                
                                <div class="space-y-2 text-center" v-else-if="!formData.image">
                                    <svg class="mx-auto h-16 w-16 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                                        <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                    <div class="flex flex-col items-center text-base text-gray-600">
                                        <label for="file-upload" class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500 py-2 px-4 border border-blue-200 hover:bg-blue-50 transition-colors text-lg">
                                            <span>上传图片</span>
                                            <input id="file-upload" name="file-upload" type="file" class="sr-only" accept="image/*" @change="handleImageUpload">
                                        </label>
                                        <p class="mt-2">或拖放图片到此处</p>
                                    </div>
                                    <p class="text-xs text-gray-500">支持JPG、PNG格式，图片大小不超过10MB</p>
                                </div>
                                
                                <div v-else class="relative w-full">
                                    <img :src="formData.image" class="mx-auto max-h-72 object-contain rounded" alt="房源图片预览">
                                    <div class="flex justify-center mt-4">
                                        <button 
                                            type="button" 
                                            class="inline-flex items-center px-4 py-2 bg-red-50 text-red-700 text-base rounded-md hover:bg-red-100 transition-colors"
                                            @click="formData.image = ''"
                                        >
                                            <svg class="mr-1.5 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                            </svg>
                                            删除图片
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <!-- 错误消息提示 -->
                            <div v-if="errorMessage" class="mt-2 text-sm text-red-600">
                                {{ errorMessage }}
                            </div>
                        </div>
                    </div>

                    <!-- 步骤 4: 确认信息 -->
                    <div v-if="currentStep === 4" class="animate-slideIn">
                        <div class="text-center mb-5">
                            <h2 class="text-2xl font-bold text-gray-800">确认房源信息</h2>
                        </div>
                        
                        <div class="bg-gray-50 p-6 rounded-xl border border-gray-200">                            
                            <div class="grid grid-cols-1 gap-y-4 sm:grid-cols-2 sm:gap-x-6">
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">房源标题</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.title || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">房源地址</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.address || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">总价</dt>
                                    <dd class="mt-1 text-lg font-medium text-red-600">{{ formData.price ? `¥ ${formData.price.toLocaleString()}` : '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">单价</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ calculateUnitPrice() ? `¥ ${calculateUnitPrice().toLocaleString()} / ㎡` : '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">面积</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.area ? `${formData.area} ㎡` : '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">户型</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.rooms || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">楼层</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.floor || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">建造年份</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.build_year || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">朝向</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.orientation || '-' }}</dd>
                                </div>
                                
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">装修情况</dt>
                                    <dd class="mt-1 text-lg font-medium text-gray-900">{{ formData.decoration || '-' }}</dd>
                                </div>
                            </div>
                            
                            <div class="mt-3">
                                <div class="flex flex-col pb-3">
                                    <dt class="text-sm font-medium text-gray-500">房源标签</dt>
                                    <dd class="mt-1 text-base">
                                        <div class="flex flex-wrap gap-1.5">
                                            <span 
                                                v-for="tag in formData.tag" 
                                                :key="tag" 
                                                class="px-3 py-1 bg-blue-50 text-blue-700 text-sm rounded-md border border-blue-100"
                                            >
                                                {{ tag }}
                                            </span>
                                            <span v-if="formData.tag.length === 0" class="text-gray-500">-</span>
                                        </div>
                                    </dd>
                                </div>
                                
                                <div class="flex mt-3">
                                    <div>
                                        <dt class="text-sm font-medium text-gray-500 mb-2">房源图片</dt>
                                        <dd class="mt-1">
                                            <div v-if="formData.image" class="overflow-hidden w-48 h-36 rounded-lg border border-gray-200">
                                                <img :src="formData.image" alt="房源图片" class="w-full h-full object-cover">
                                            </div>
                                            <div v-else class="text-lg text-gray-600">-</div>
                                        </dd>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 提交结果提示 -->
                        <div v-if="errorMessage || successMessage" class="mt-6">
                            <div v-if="errorMessage" class="rounded-md bg-red-50 p-4 border border-red-200 animate-fadeIn">
                                <div class="flex">
                                    <div class="flex-shrink-0">
                                        <svg class="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                                        </svg>
                                    </div>
                                    <div class="ml-3">
                                        <h3 class="text-base font-medium text-red-800">提交失败</h3>
                                        <div class="mt-2 text-base text-red-700">
                                            <p>{{ errorMessage }}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div v-if="successMessage" class="rounded-md bg-green-50 p-4 border border-green-200 animate-fadeIn">
                                <div class="flex">
                                    <div class="flex-shrink-0">
                                        <svg class="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                                        </svg>
                                    </div>
                                    <div class="ml-3">
                                        <h3 class="text-base font-medium text-green-800">提交成功</h3>
                                        <div class="mt-2 text-base text-green-700">
                                            <p>{{ successMessage }}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 导航按钮 -->
                    <div class="flex items-center justify-between pt-6">
                        <button
                            v-if="currentStep > 1"
                            type="button"
                            @click="prevStep"
                            class="btn-secondary inline-flex items-center px-5 py-3 border border-gray-300 shadow-sm text-base font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                        >
                            <svg class="mr-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                            </svg>
                            上一步
                        </button>
                        <div v-else></div>

                        <button
                            v-if="currentStep < totalSteps"
                            type="button"
                            @click="nextStep"
                            :disabled="!validateCurrentStep()"
                            class="btn-primary inline-flex items-center px-6 py-3 border border-transparent shadow-sm text-base font-medium rounded-lg text-white bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        >
                            下一步
                            <svg class="ml-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                            </svg>
                        </button>

                        <button
                            v-if="currentStep === totalSteps"
                            type="button"
                            @click="submitForm"
                            :disabled="submitting"
                            class="btn-submit inline-flex items-center px-8 py-3 border border-transparent text-lg font-medium rounded-lg shadow-md text-white bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        >
                            <svg v-if="submitting" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            {{ submitting ? '提交中...' : '提交发布' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
/* 步骤切换动画 */
.animate-slideIn {
  animation: slideIn 0.5s ease-out;
}

.animate-fadeIn {
  animation: fadeIn 0.5s ease-out;
}

@keyframes slideIn {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

/* 按钮效果 */
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.btn-secondary:hover {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px -1px rgba(0, 0, 0, 0.1), 0 2px 6px -1px rgba(0, 0, 0, 0.06);
}

/* 表单控件样式 */
.form-input:focus, 
.form-select:focus {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.25);
}
</style>