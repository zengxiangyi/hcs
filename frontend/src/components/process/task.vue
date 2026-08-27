<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'Task' })

/** 工序任务行 */
interface JobRow {
  id: number
  jobName: string
  productName: string
  processName: string
  status: string
  createTime: string
}

/** 查询表单 */
interface QueryForm {
  jobName: string
  productName: string
  status: string
}

// 状态选项
const statusOptions = ['待处理', '进行中', '已完成', '已取消']

// 模拟数据（实际接入后端时替换为接口返回）
function mockJobs(): JobRow[] {
  const list: JobRow[] = []
  const products = ['手机外壳', '电路板', '电池模组', '摄像头组件']
  const processes = ['注塑', '焊接', '组装', '检测']
  const statuses = statusOptions
  for (let i = 1; i <= 86; i++) {
    list.push({
      id: i,
      jobName: `工序任务-${String(i).padStart(3, '0')}`,
      productName: products[i % products.length],
      processName: processes[i % processes.length],
      status: statuses[i % statuses.length],
      createTime: `2026-08-${String((i % 28) + 1).padStart(2, '0')} 10:${String(i % 60).padStart(2, '0')}:00`,
    })
  }
  return list
}

const allJobs = ref<JobRow[]>(mockJobs())

// 查询条件
const query = ref<QueryForm>({
  jobName: '',
  productName: '',
  status: '',
})

// 服务端分页（此处为前端模拟分页）
const tableData = ref<JobRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 过滤 + 分页
function fetchData() {
  loading.value = true
  try {
    const keyword = query.value.jobName.trim()
    const product = query.value.productName.trim()
    const status = query.value.status

    const filtered = allJobs.value.filter((row) => {
      const matchName = !keyword || row.jobName.includes(keyword)
      const matchProduct = !product || row.productName.includes(product)
      const matchStatus = !status || row.status === status
      return matchName && matchProduct && matchStatus
    })

    total.value = filtered.length
    const start = (currentPage.value - 1) * pageSize.value
    tableData.value = filtered.slice(start, start + pageSize.value)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { jobName: '', productName: '', status: '' }
  currentPage.value = 1
  fetchData()
}

function handlePageChange() {
  fetchData()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchData()
}

function statusTagType(status: string): 'info' | 'warning' | 'success' | 'danger' {
  switch (status) {
    case '待处理':
      return 'info'
    case '进行中':
      return 'warning'
    case '已完成':
      return 'success'
    case '已取消':
      return 'danger'
    default:
      return 'info'
  }
}

function handleEdit(row: JobRow) {
  ElMessage.info(`编辑：${row.jobName}`)
}

function handleDelete(row: JobRow) {
  const idx = allJobs.value.findIndex((j) => j.id === row.id)
  if (idx > -1) allJobs.value.splice(idx, 1)
  if (tableData.value.length === 1 && currentPage.value > 1) {
    currentPage.value -= 1
  }
  ElMessage.success(`已删除：${row.jobName}`)
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <div class="job-page">
    <h3 class="page-title">工序任务管理</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="任务名称">
        <el-input
          v-model="query.jobName"
          placeholder="任务名称"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="产品">
        <el-input
          v-model="query.productName"
          placeholder="产品名称"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="jobName" label="任务名称" min-width="160" />
      <el-table-column prop="productName" label="产品" min-width="120" />
      <el-table-column prop="processName" label="工序" min-width="120" />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<style scoped>
.job-page {
  padding: 20px;
  color: #333;
}
</style>
