<script setup lang="ts">
import MenuBar from './MenuBar.vue'
</script>

<template>
  <div class="layout">
    <aside class="layout-left">
      <!-- 左侧内容 -->
       <MenuBar />
    </aside>
    <main class="layout-right">
      <!-- 路由跳转后的新页面嵌入此处 -->
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100vh;
}

.layout-left {
  width: 20%;
  background-color: #cecece;
}

.layout-right {
  width: 80%;
  background-color: #ffffff;
}
</style>
