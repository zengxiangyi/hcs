<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskProcessAPI, type TaskRow, type TaskListParams } from '../../api/taskProcess'

defineOptions({ name: 'Task' })

/** 审批状态选项 */
const auditStateOptions = ['待审批', '已通过', '已驳回']
/** 状态选项 */
const stateOptions = ['待处理', '进行中', '已完成', '已取消']

// 查询条件
const query = reactive<TaskListParams>({
  transfer: '',
  blueprint: '',
  auditState: '',
  step: '',
  state: '',
})

// 表格数据
const tableData = ref<TaskRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

function fetchData() {
  loading.value = true
  const params: TaskListParams = {
    ...query,
    page: currentPage.value,
    pageSize: pageSize.value,
  }
  taskProcessAPI
    .search(params)
    .then((res) => {
      tableData.value = res.data.content
      total.value = res.data.total
    })
    .catch((err) => {
      ElMessage.error(err.message || '加载失败')
    })
    .finally(() => {
      loading.value = false
    })
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.transfer = ''
  query.blueprint = ''
  query.auditState = ''
  query.step = ''
  query.state = ''
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

function auditTagType(v: string): 'info' | 'success' | 'danger' {
  switch (v) {
    case '已通过':
      return 'success'
    case '已驳回':
      return 'danger'
    default:
      return 'info'
  }
}

function stateTagType(v: string): 'info' | 'warning' | 'success' | 'danger' {
  switch (v) {
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

function handleDelete(row: TaskRow) {
  ElMessageBox.confirm(`确认删除该任务（ID: ${row.id}）？`, '提示', {
    type: 'warning',
  })
    .then(() => {
      taskProcessAPI
        .remove(row.id)
        .then(() => {
          ElMessage.success('删除成功')
          if (tableData.value.length === 1 && currentPage.value > 1) {
            currentPage.value -= 1
          }
          fetchData()
        })
        .catch((err) => ElMessage.error(err.message || '删除失败'))
    })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<template>
  <div class="task-page">
    <h3 class="page-title">产品流程</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="调拨单">
        <el-input v-model="query.transfer" placeholder="调拨单" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="蓝本工艺编号">
        <el-input v-model="query.blueprint" placeholder="蓝本工艺编号" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="工序">
        <el-input v-model="query.step" placeholder="工序" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="审批状态">
        <el-select v-model="query.auditState" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="s in auditStateOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.state" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="s in stateOptions" :key="s" :label="s" :value="s" />
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
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="transfer" label="调拨单" min-width="140" />
      <el-table-column prop="blueprint" label="蓝本工艺编号" min-width="160" />
      <el-table-column prop="auditState" label="审批状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="auditTagType(row.auditState)">{{ row.auditState || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="step" label="工序" min-width="120" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="stateTagType(row.state)">{{ row.state || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createUser" label="创建人" min-width="120" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleDelete(row as TaskRow)">进入流程</el-button>
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
.task-page {
  padding: 20px;
  color: #333;
}
.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
}
.query-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
