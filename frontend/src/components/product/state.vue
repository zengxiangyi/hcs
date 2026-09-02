<script setup lang="ts">
defineOptions({ name: 'State' })
</script>

<template>
  <div class="state-page">
    <h3 class="page-title">产品状态</h3>
    <el-empty description="功能建设中，敬请期待" />
  </div>
</template>

<style scoped>
.state-page {
  padding: 16px;
}
.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
}
</style>
