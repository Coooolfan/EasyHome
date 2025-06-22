<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentPath = computed(() => route.path)
</script>

<template>
    <nav class="bg-blue-700 shadow-md">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex h-16">
                <div class="flex items-center">
                    <div class="text-2xl font-bold text-white">EasyHome</div>
                    <div class="ml-10 flex items-center space-x-4">
                        <router-link to="/" class="px-3 py-2 rounded-md text-sm font-medium"
                            :class="[currentPath === '/' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                            找房
                        </router-link>
                        <router-link to="/search" class="px-3 py-2 rounded-md text-sm font-medium"
                            :class="[currentPath === '/search' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                            查房
                        </router-link>
                        <router-link to="/view" class="px-3 py-2 rounded-md text-sm font-medium"
                            :class="[currentPath === '/view' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                            看房
                        </router-link>
                        <router-link to="/sell" class="px-3 py-2 rounded-md text-sm font-medium"
                            :class="[currentPath === '/sell' ? 'text-blue-800 bg-white' : 'text-white hover:bg-blue-600']">
                            卖房
                        </router-link>
                    </div>
                </div>
            </div>
        </div>
    </nav>
</template>