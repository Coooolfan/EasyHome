<script setup lang="ts">
</script>

<template>
    <main class="py-8">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center mb-12">
                <h1 class="text-4xl font-extrabold text-gray-900 sm:text-5xl sm:tracking-tight lg:text-6xl">
                    预约看房
                </h1>
                <p class="mt-5 max-w-xl mx-auto text-xl text-gray-500">
                    选择您感兴趣的房源，预约专业顾问带您实地看房
                </p>
            </div>

            <div class="bg-white shadow overflow-hidden rounded-lg mb-8">
                <div class="px-4 py-5 sm:p-6">
                    <div class="max-w-3xl mx-auto">
                        <div class="mb-6">
                            <label for="property" class="block text-sm font-medium text-gray-700 mb-2">选择房源</label>
                            <select id="property" name="property"
                                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                <option>请选择房源</option>
                                <option>高新区幸福小区 3室2厅 89㎡ 238万</option>
                                <option>滨江区江景花园 2室1厅 65㎡ 180万</option>
                                <option>西湖区湖畔公寓 4室2厅 120㎡ 350万</option>
                                <option>城东新区阳光花园 3室2厅 98㎡ 260万</option>
                            </select>
                        </div>

                        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                            <div>
                                <label for="date" class="block text-sm font-medium text-gray-700 mb-2">看房日期</label>
                                <input type="date" name="date" id="date"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>

                            <div>
                                <label for="time" class="block text-sm font-medium text-gray-700 mb-2">看房时间</label>
                                <select id="time" name="time"
                                    class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
                                    <option>上午 9:00-10:00</option>
                                    <option>上午 10:00-11:00</option>
                                    <option>上午 11:00-12:00</option>
                                    <option>下午 14:00-15:00</option>
                                    <option>下午 15:00-16:00</option>
                                    <option>下午 16:00-17:00</option>
                                    <option>下午 17:00-18:00</option>
                                </select>
                            </div>

                            <div>
                                <label for="name" class="block text-sm font-medium text-gray-700 mb-2">您的姓名</label>
                                <input type="text" name="name" id="name"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>

                            <div>
                                <label for="phone" class="block text-sm font-medium text-gray-700 mb-2">联系电话</label>
                                <input type="tel" name="phone" id="phone"
                                    class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border">
                            </div>
                        </div>

                        <div class="mt-6">
                            <label for="remarks" class="block text-sm font-medium text-gray-700 mb-2">备注</label>
                            <textarea id="remarks" name="remarks" rows="3"
                                class="focus:ring-blue-500 focus:border-blue-500 block w-full rounded-md sm:text-sm border-gray-300 p-2 border"></textarea>
                        </div>

                        <div class="mt-6 flex justify-center">
                            <button type="button"
                                class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                                提交预约
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="bg-gray-50 p-6 rounded-lg">
                <h2 class="text-xl font-semibold text-gray-900 mb-4">看房须知</h2>
                <ul class="list-disc pl-5 space-y-2 text-gray-600">
                    <li>请提前一天预约看房，我们会安排专业顾问陪同您看房</li>
                    <li>看房时请携带有效身份证件</li>
                    <li>如需取消或更改预约，请提前2小时联系我们</li>
                    <li>看房时间为每日9:00-18:00</li>
                    <li>如有特殊需求，可在备注中说明</li>
                </ul>
            </div>
        </div>
    </main>
</template>