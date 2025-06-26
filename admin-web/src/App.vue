<template>
  <div id="app">
    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
// 移除所有导航相关代码，让 HomeView 自己处理导航
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow: hidden; /* 防止页面级别的滚动条 */
}

#app {
  height: 100vh;
  overflow: hidden; /* 防止应用级别的滚动条 */
}
</style>
