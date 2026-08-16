<script setup lang="ts">
import { ref } from 'vue';
import MenuBar from './MenuBar.vue'
import router from '../router/index.ts';

const username = ref('admin')

function handleLogout() {
  console.log('退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="title">
    <div class="left">
        <h2>欢迎使用Vue3.0+Vite2.0+TypeScript开发的后台管理系统</h2>
    </div>
    <div class="right">
      <div>
        <h2>当前用户：{{username}}</h2>
      </div>
      <div>
        <el-button type="danger" size="primary" @click="handleLogout">退出</el-button>
      </div>
    </div>
  </div>
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
:global(#app) {
  width: 100%;
  max-width: none;
  margin: 0;
  border-inline: none;
  text-align: left;
}

.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  padding: 0 20px;
  background-color: #f5f5f5;
}

.title .left {
  /* 靠左对齐 */
  display: flex;
  font-size: 16px;
  align-items: center;
}

.title .right {
  /* 靠右对齐 */
  display: flex;
  font-size: 14px;
  align-items: center;
  gap: 16px;
}

.layout {
  display: flex;
  width: 100%;
  height: calc(100vh - 50px);
  overflow: hidden;
}

.layout-left {
  flex: 0 0 15%;
  width: 15%;
  background-color: #cccccc;
}

.layout-right {
  flex: 1;
  background-color: #ffffff;
  overflow: auto;
}
</style>
