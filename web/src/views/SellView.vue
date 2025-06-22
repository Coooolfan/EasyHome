<script setup lang="ts">
</script>

<template>
    <main class="py-8">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center mb-12">
                <h1 class="text-4xl font-extrabold text-gray-900 sm:text-5xl sm:tracking-tight lg:text-6xl">
                    发布房源
                </h1>
                <p class="mt-5 max-w-xl mx-auto text-xl text-gray-500">
                    填写您的房屋信息，我们将帮助您快速找到买家
                </p>
            </div>

            <div class="bg-white shadow overflow-hidden rounded-lg mb-8">
                <div class="px-4 py-5 sm:p-6">
                    <div class="max-w-4xl mx-auto">
                        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                            <div>
                                <label for="title" class="block text-sm font-medium text-gray-700 mb-2">房源标题</label>
                                <input type="text" name="title" id="title"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                    placeholder="简洁明了的房源标题">
                            </div>

                            <div>
                                <label for="price" class="block text-sm font-medium text-gray-700 mb-2">期望售价（万元）</label>
                                <input type="number" name="price" id="price"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>

                            <div>
                                <label for="area" class="block text-sm font-medium text-gray-700 mb-2">建筑面积（㎡）</label>
                                <input type="number" name="area" id="area"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>

                            <div>
                                <label for="rooms" class="block text-sm font-medium text-gray-700 mb-2">户型</label>
                                <select id="rooms" name="rooms"
                                    class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                    <option>一室</option>
                                    <option>二室</option>
                                    <option>三室</option>
                                    <option>四室</option>
                                    <option>五室及以上</option>
                                </select>
                            </div>

                            <div>
                                <label for="floor" class="block text-sm font-medium text-gray-700 mb-2">所在楼层</label>
                                <div class="flex space-x-2">
                                    <input type="number" name="floor" id="floor"
                                        class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                        placeholder="当前楼层">
                                    <span class="flex items-center">/</span>
                                    <input type="number" name="total_floor" id="total_floor"
                                        class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                        placeholder="总楼层">
                                </div>
                            </div>

                            <div>
                                <label for="orientation"
                                    class="block text-sm font-medium text-gray-700 mb-2">房屋朝向</label>
                                <select id="orientation" name="orientation"
                                    class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                    <option>东</option>
                                    <option>南</option>
                                    <option>西</option>
                                    <option>北</option>
                                    <option>东南</option>
                                    <option>东北</option>
                                    <option>西南</option>
                                    <option>西北</option>
                                    <option>南北通透</option>
                                </select>
                            </div>

                            <div>
                                <label for="decoration"
                                    class="block text-sm font-medium text-gray-700 mb-2">装修情况</label>
                                <select id="decoration" name="decoration"
                                    class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                    <option>毛坯</option>
                                    <option>简装</option>
                                    <option>中等装修</option>
                                    <option>精装</option>
                                    <option>豪华装修</option>
                                </select>
                            </div>

                            <div>
                                <label for="age" class="block text-sm font-medium text-gray-700 mb-2">房龄（年）</label>
                                <input type="number" name="age" id="age"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>

                            <div class="sm:col-span-2">
                                <label for="address" class="block text-sm font-medium text-gray-700 mb-2">详细地址</label>
                                <input type="text" name="address" id="address"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                    placeholder="小区名称、楼栋号、单元号、门牌号">
                            </div>

                            <div class="sm:col-span-2">
                                <label for="description"
                                    class="block text-sm font-medium text-gray-700 mb-2">房源描述</label>
                                <textarea id="description" name="description" rows="4"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                    placeholder="详细描述房屋情况，如周边配套、交通状况、房屋特色等"></textarea>
                            </div>

                            <div class="sm:col-span-2">
                                <label class="block text-sm font-medium text-gray-700 mb-2">上传房屋照片</label>
                                <div
                                    class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md">
                                    <div class="space-y-1 text-center">
                                        <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none"
                                            viewBox="0 0 48 48" aria-hidden="true">
                                            <path
                                                d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                                                stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                        </svg>
                                        <div class="flex text-sm text-gray-600">
                                            <label for="file-upload"
                                                class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                                                <span>上传照片</span>
                                                <input id="file-upload" name="file-upload" type="file" class="sr-only"
                                                    multiple>
                                            </label>
                                            <p class="pl-1">或拖放文件</p>
                                        </div>
                                        <p class="text-xs text-gray-500">
                                            PNG, JPG, GIF 最大 10MB
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="mt-8">
                            <label class="block text-sm font-medium text-gray-700 mb-2">联系方式</label>
                            <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                                <div>
                                    <label for="contact_name"
                                        class="block text-sm font-medium text-gray-700 mb-2">联系人</label>
                                    <input type="text" name="contact_name" id="contact_name"
                                        class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                                </div>

                                <div>
                                    <label for="contact_phone"
                                        class="block text-sm font-medium text-gray-700 mb-2">联系电话</label>
                                    <input type="tel" name="contact_phone" id="contact_phone"
                                        class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                                </div>
                            </div>
                        </div>

                        <div class="mt-8 flex justify-center">
                            <button type="button"
                                class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                                发布房源
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>