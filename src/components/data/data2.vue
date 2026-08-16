<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface User {
  id: number
  name: string
  role: string
  dept: string
  status: string
  createTime: string
}

// 全量 mock 数据
const allData = ref<User[]>([
  { id: 1, name: '张三', role: '管理员', dept: '技术部', status: '启用', createTime: '2026-08-01 10:00' },
  { id: 2, name: '李四', role: '编辑', dept: '内容部', status: '启用', createTime: '2026-08-02 11:30' },
  { id: 3, name: '王五', role: '访客', dept: '市场部', status: '禁用', createTime: '2026-08-03 09:15' },
  { id: 4, name: '赵六', role: '管理员', dept: '技术部', status: '启用', createTime: '2026-08-05 14:20' },
  { id: 5, name: '钱七', role: '编辑', dept: '设计部', status: '禁用', createTime: '2026-08-07 16:45' },
  { id: 6, name: '孙八', role: '访客', dept: '市场部', status: '启用', createTime: '2026-08-08 09:00' },
  { id: 7, name: '周九', role: '管理员', dept: '人事部', status: '启用', createTime: '2026-08-10 13:40' },
  { id: 8, name: '吴十', role: '编辑', dept: '财务部', status: '禁用', createTime: '2026-08-12 17:25' },
])

// 查询条件
const query = ref({
  keyword: '',
  dept: '',
  status: '',
})

const statusOptions = ['启用', '禁用']
const deptOptions = ['技术部', '内容部', '市场部', '设计部', '人事部', '财务部']

// 按条件过滤后的结果
const filteredData = computed<User[]>(() => {
  return allData.value.filter(item => {
    const kw = query.value.keyword.trim()
    const matchKw =
      !kw ||
      item.name.includes(kw) ||
      item.role.includes(kw) ||
      String(item.id).includes(kw)
    const matchDept = !query.value.dept || item.dept === query.value.dept
    const matchStatus = !query.value.status || item.status === query.value.status
    return matchKw && matchDept && matchStatus
  })
})

// 分页
const currentPage = ref(1)
const pageSize = ref(5)

const pagedData = computed<User[]>(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() {
  currentPage.value = 1
  ElMessage.success(`查询完成，共 ${filteredData.value.length} 条结果`)
}

function handleReset() {
  query.value = { keyword: '', dept: '', status: '' }
  currentPage.value = 1
}

function handleEdit(row: User) {
  ElMessage.info(`编辑：${row.name}`)
}

function handleDelete(row: User) {
  allData.value = allData.value.filter(item => item.id !== row.id)
  ElMessage.success(`已删除：${row.name}`)
}
</script>

<template>
  <div class="data-page">
    <h3 class="page-title">数据2</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="关键字">
        <el-input
          v-model="query.keyword"
          placeholder="姓名 / 角色 / ID"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.dept" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="pagedData" border stripe style="width: 100%">
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

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="filteredData.length"
      :page-sizes="[5, 8]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
    />
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
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
