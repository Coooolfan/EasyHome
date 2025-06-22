<script setup lang="ts">
</script>

<template>
    <main class="py-8">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center mb-12">
                <h1 class="text-4xl font-extrabold text-gray-900 sm:text-5xl sm:tracking-tight lg:text-6xl">
                    找到您理想的家
                </h1>
                <p class="mt-5 max-w-xl mx-auto text-xl text-gray-500">
                    浏览我们精选的二手房源，找到适合您的理想住所
                </p>
            </div>

            <div class="bg-white shadow overflow-hidden rounded-lg mb-8">
                <div class="px-4 py-5 sm:p-6">
                    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                        <div class="col-span-1 sm:col-span-2 md:col-span-3 lg:col-span-4">
                            <label for="search" class="block text-sm font-medium text-gray-700">搜索</label>
                            <div class="mt-1 flex rounded-md shadow-sm">
                                <input type="text" name="search" id="search"
                                    class="focus:ring-blue-500 focus:border-blue-500 flex-1 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"
                                    placeholder="输入关键词、地址或小区名">
                            </div>
                        </div>

                        <div>
                            <label for="price" class="block text-sm font-medium text-gray-700">价格范围</label>
                            <select id="price" name="price"
                                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                <option>不限</option>
                                <option>100万以下</option>
                                <option>100-200万</option>
                                <option>200-300万</option>
                                <option>300-500万</option>
                                <option>500万以上</option>
                            </select>
                        </div>

                        <div>
                            <label for="area" class="block text-sm font-medium text-gray-700">面积</label>
                            <select id="area" name="area"
                                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                <option>不限</option>
                                <option>50㎡以下</option>
                                <option>50-70㎡</option>
                                <option>70-90㎡</option>
                                <option>90-110㎡</option>
                                <option>110-130㎡</option>
                                <option>130㎡以上</option>
                            </select>
                        </div>

                        <div>
                            <label for="rooms" class="block text-sm font-medium text-gray-700">户型</label>
                            <select id="rooms" name="rooms"
                                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                <option>不限</option>
                                <option>一室</option>
                                <option>二室</option>
                                <option>三室</option>
                                <option>四室</option>
                                <option>五室及以上</option>
                            </select>
                        </div>

                        <div>
                            <label for="location" class="block text-sm font-medium text-gray-700">区域</label>
                            <select id="location" name="location"
                                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                <option>不限</option>
                                <option>市中心</option>
                                <option>城东</option>
                                <option>城南</option>
                                <option>城西</option>
                                <option>城北</option>
                            </select>
                        </div>
                    </div>
                    <div class="mt-5 flex justify-center">
                        <button type="button"
                            class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                            开始搜索
                        </button>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                <div v-for="i in 6" :key="i" class="bg-white overflow-hidden shadow rounded-lg">
                    <div class="h-48 bg-gray-200"></div>
                    <div class="px-4 py-4 sm:px-6">
                        <h3 class="text-lg font-medium text-gray-900">示例房源 {{ i }}</h3>
                        <p class="mt-1 text-sm text-gray-500">3室2厅 · 89㎡ · 南北通透</p>
                        <p class="mt-1 text-lg font-semibold text-blue-600">238万</p>
                        <p class="mt-1 text-sm text-gray-500">高新区 · 幸福小区</p>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>
