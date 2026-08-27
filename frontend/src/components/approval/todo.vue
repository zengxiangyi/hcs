<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { createTextFormatter, createStateFormatter, type TagType } from '../../utils/enum'

defineOptions({ name: 'ApprovalTodo' })

/** workflow 行信息 */
interface WorkFlowRow {
  id: number
  code: string
  name: string
  category: string
  targetCode: string
  sender: string
  startTime: string
  state: string
  flowGraph: string
  endTime: string
  remark: string
}

/** 列表查询参数 */
interface WorkFlowListParams {
  targetCode?: string
  startTimeStart?: string
  startTimeEnd?: string
  page?: number
  pageSize?: number
}

/** 列表返回 */
interface WorkFlowListResult {
  content: WorkFlowRow[]
  total: number
  page: number
  pageSize: number
}

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

// 查询条件
const query = ref<{
  targetCode: string
  startTimeStart: string
  startTimeEnd: string
}>({
  targetCode: '',
  startTimeStart: '',
  startTimeEnd: '',
})

// 表格数据 + 分页
const tableData = ref<WorkFlowRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

/**
 * 分类枚举映射（key -> 展示文本）
 * key 为后端存储/返回的值，value 为表格中展示的中文文本。
 * 如需调整分类项，直接修改此对象即可。
 */
const categoryMap: Record<string, string> = {
  B: '蓝本工艺',
  C: '产品工艺'
}

/** 根据分类 key 取展示文本，未匹配时回退为原值（公共方法生成） */
const formatCategory = createTextFormatter(categoryMap)

/**
 * 状态枚举映射（key -> { 展示文本, tag 类型 }）
 * key 为后端存储/返回的值，value.label 为展示文本，value.type 为 el-tag 颜色类型。
 * 如需调整状态项或配色，直接修改此对象即可。
 */
const stateMap: Record<string, { label: string; type: TagType }> = {
  A: { label: '待处理', type: 'warning' },
  pending: { label: '待审批', type: 'warning' },
  approving: { label: '审批中', type: 'warning' },
  completed: { label: '已完成', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' },
}

/** 根据状态 key 取展示文本 / tag 类型，未匹配时文本回退原值、类型回退 warning（公共方法生成） */
const { label: formatStateLabel, type: formatStateType } = createStateFormatter(stateMap)

// 构建查询参数（列表与后续导出共用）
function buildQueryParams(overrides: Partial<WorkFlowListParams> = {}): WorkFlowListParams {
  return {
    targetCode: query.value.targetCode.trim(),
    startTimeStart: query.value.startTimeStart || undefined,
    startTimeEnd: query.value.startTimeEnd || undefined,
    ...overrides,
  }
}

// 拉取列表
async function fetchData() {
  loading.value = true
  try {
    const res = await http.get<WorkFlowListResult>('/api/workflow/list', {
      params: buildQueryParams({ page: currentPage.value, pageSize: pageSize.value }),
    })
    tableData.value = res.data.content
    total.value = res.data.total
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '获取数据失败'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { targetCode: '', startTimeStart: '', startTimeEnd: '' }
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

// 通过
async function handleApprove(row: WorkFlowRow) {
  try {
    await ElMessageBox.confirm(`确认通过任务「${row.name}」？`, '审批', {
      type: 'warning',
      confirmButtonText: '通过',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await http.post<null>(`/api/workflow/${row.id}/approve`)
    ElMessage.success('已通过')
    fetchData()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '操作失败'))
  }
}

// 驳回
async function handleReject(row: WorkFlowRow) {
  try {
    await ElMessageBox.confirm(`确认驳回任务「${row.name}」？`, '审批', {
      type: 'warning',
      confirmButtonText: '驳回',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await http.post<null>(`/api/workflow/${row.id}/reject`)
    ElMessage.success('已驳回')
    fetchData()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '操作失败'))
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="approval-page">
    <h3 class="page-title">我发起的</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="目标编号">
        <el-input
          v-model="query.targetCode"
          placeholder="请输入目标编号"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="发起开始时间">
        <el-date-picker
          v-model="query.startTimeStart"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="发起结束时间">
        <el-date-picker
          v-model="query.startTimeEnd"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="任务编号" min-width="140" />
      <el-table-column prop="name" label="任务名称" min-width="160" />
      <el-table-column prop="category" label="分类" min-width="120">
        <template #default="{ row }">
          {{ formatCategory(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetCode" label="目标编号" min-width="140" />
      <el-table-column prop="sender" label="发起人" min-width="120" />
      <el-table-column prop="startTime" label="发起时间" min-width="180" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="formatStateType(row.state)">{{ formatStateLabel(row.state) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="flowGraph" label="流程图编号" min-width="140" />
      <el-table-column prop="endTime" label="结束时间" min-width="180" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="handleApprove(row)">退回</el-button>
          <el-button size="small" type="success" @click="handleReject(row)">查看</el-button>
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
.approval-page {
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