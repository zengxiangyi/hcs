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

interface QueryForm {
  userName: string
  roleName: string
}

// 查询条件
const query = ref<QueryForm>({
  userName: '',
  roleName: ''
})

defineOptions({ name: 'Data1' })


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

function handleImport() {
  console.log('导入')
}
function handleExport() {
  console.log('导出')
}
function handleAdd() {
  console.log('新增')
}
function handleReset(){
  console.log('重置')
}
function handleSearch(){
  console.log('查询')
}
</script>

<template>
  <div class="data-page">
    <h3 class="page-title"></h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="姓名">
        <el-input
          v-model="query.userName"
          placeholder="姓名"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="角色">
        <el-input
          v-model="query.roleName"
          placeholder="角色"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <!-- 操作按钮-->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="success" @click="handleImport">导入</el-button>
      <el-button type="warning" @click="handleExport">导出</el-button>
    </div>
    <!-- 表格区 -->
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

.query-form {
  margin-bottom: 10px;
}

.toolbar {
  margin-bottom: 10px;
}
</style>
