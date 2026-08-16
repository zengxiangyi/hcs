<script setup lang="ts">
defineProps<{ title?: string }>()
</script>

<template>
  <div class="placeholder-page">
    <h2>{{ title }}</h2>
    <p>这是一个占位页面，用于展示路由跳转后的内容。</p>
  </div>
</template>

<style scoped>
.placeholder-page {
  padding: 24px;
  color: #333;
}
</style>
