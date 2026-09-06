<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTextFormatter, createStateFormatter, type TagType } from '../../utils/enum'
import { workflowAPI, type WorkflowRow } from '../../api/workflow'
import { flowEngineAPI } from '../../api/flowEngine'

defineOptions({ name: 'Send' })

const router = useRouter()

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

// 查询条件
const query = ref<{
  startTimeStart: string
  startTimeEnd: string
}>({
  startTimeStart: '',
  startTimeEnd: '',
})

// 表格数据 + 分页
const tableData = ref<WorkflowRow[]>([])
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
  A: '蓝本工艺',
  B: '产品工艺',
  C: '产品认证'
}

/** 根据分类 key 取展示文本，未匹配时回退为原值（公共方法生成） */
const formatCategory = createTextFormatter(categoryMap)

/**
 * 状态枚举映射（key -> { 展示文本, tag 类型 }）
 * key 为后端存储/返回的值，value.label 为展示文本，value.type 为 el-tag 颜色类型。
 * 如需调整状态项或配色，直接修改此对象即可。
 */
const stateMap: Record<string, { label: string; type: TagType }> = {
  S: { label: '待处理', type: 'primary' },
  E: { label: '已结束', type: 'success' },
  C: { label: '已取消', type: 'info' },
  D: { label: '处理中', type: 'danger' },
}

/** 根据状态 key 取展示文本 / tag 类型，未匹配时文本回退原值、类型回退 warning（公共方法生成） */
const { label: formatStateLabel, type: formatStateType } = createStateFormatter(stateMap)

// 拉取列表
async function fetchData() {
  loading.value = true
  try {
    // 后端 GET /sender 不接收查询参数，处理人取自当前登录用户，固定返回前 30 条
    const res = await workflowAPI.sender({ page: currentPage.value, pageSize: pageSize.value })
    // 用户无实例时后端可能返回 data: null
    const data = res.data
    if (!data) {
      tableData.value = []
      total.value = 0
      return
    }
    tableData.value = data.content
    total.value = data.total
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
  query.value = {startTimeStart: '', startTimeEnd: '' }
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

function handleCancel(code: string){
  // 弹出对话框输入撤销原因
  ElMessageBox.prompt('请输入撤销原因', '撤销', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(({ value }) => {
    cancelTask(code, value)
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

async function cancelTask(code: string,reason: string){
  try {
    await flowEngineAPI.cancel({ workflow: code, reason: reason })
    ElMessage.success('已撤销')
    fetchData()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '操作失败'))
  }
}

// 查看：携带流程编号跳转到 instance.vue
function seeProcess(row: WorkflowRow) {
  router.push({ name: 'Instance', query: { workflow: row.code } })
}

onMounted(fetchData)
</script>

<template>
  <div class="approval-page">
    <h3 class="page-title">我发起的</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="发起开始时间">
        <el-date-picker
          v-model="query.startTimeStart"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="发起截止时间">
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
      <el-table-column prop="id" label="ID" v-if="false" width="80" />
      <el-table-column prop="code" label="任务编号" min-width="140" />
      <el-table-column prop="name" label="任务名称" min-width="160" />
      <el-table-column prop="category" label="分类" min-width="120">
        <template #default="{ row }">
          {{ formatCategory(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetCode" label="目标编号" v-if="false" min-width="140" />
      <el-table-column prop="sender" label="发起人" min-width="120" />
      <el-table-column prop="startTime" label="发起时间" min-width="180" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="formatStateType(row.state)">{{ formatStateLabel(row.state) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="flowGraph" v-if="false" label="流程图编号" min-width="140" />
      <el-table-column prop="endTime" label="结束时间" min-width="180" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="seeProcess(row as WorkflowRow)">查看</el-button>
          <el-button size="small" v-if="row.state==='A'" type="warning" @click="handleCancel(row.code)">撤销</el-button>
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
</style>
