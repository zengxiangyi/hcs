<script setup lang="ts">
import { ref } from 'vue'

interface User {
  id: number
  name: string
  role: string
  dept: string
  status: string
  createTime: string
}

const tableData = ref<User[]>([
  { id: 1, name: '张三', role: '管理员', dept: '技术部', status: '启用', createTime: '2026-08-01 10:00' },
  { id: 2, name: '李四', role: '编辑', dept: '内容部', status: '启用', createTime: '2026-08-02 11:30' },
  { id: 3, name: '王五', role: '访客', dept: '市场部', status: '禁用', createTime: '2026-08-03 09:15' },
  { id: 4, name: '赵六', role: '管理员', dept: '技术部', status: '启用', createTime: '2026-08-05 14:20' },
  { id: 5, name: '钱七', role: '编辑', dept: '设计部', status: '启用', createTime: '2026-08-07 16:45' },
])

function handleEdit(row: User) {
  console.log('编辑', row)
}

function handleDelete(row: User) {
  console.log('删除', row)
}
</script>

<template>
  <div class="data-page">
    <h3 class="page-title">数据1</h3>
    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="role" label="角色" min-width="120" />
      <el-table-column prop="dept" label="部门" min-width="120" />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.data-page {
  padding: 20px;
  color: #333;
}

.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}
</style>
