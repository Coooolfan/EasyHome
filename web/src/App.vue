<template>
  <NavBar v-if="!hideNavbar" />
  <router-view />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'

const route = useRoute()
const hideNavbar = computed(() => route.meta.hideNavbar)
</script>