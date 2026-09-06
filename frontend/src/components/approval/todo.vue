<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createTextFormatter, createStateFormatter, type TagType } from '../../utils/enum'
import { workflowAPI, type TodoRow } from '../../api/workflow'
import { flowEngineAPI } from '../../api/flowEngine'

defineOptions({ name: 'Todo' })

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

// 表格数据 + 分页
const tableData = ref<TodoRow[]>([])
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

// 拉取列表（dealUser/roleCode 由后端按当前登录用户覆盖，前端只传分页参数）
async function fetchData() {
  loading.value = true
  try {
    const res = await workflowAPI.todo({ page: currentPage.value, pageSize: pageSize.value })
    // 后端在用户无角色/取不到用户名时返回 data: null
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

function handlePageChange() {
  fetchData()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchData()
}

/** 审批动作：通过 / 驳回 */
type ApproveMode = 'approve' | 'reject'

// 审批对话框状态
const dialogVisible = ref(false)
const dialogMode = ref<ApproveMode>('approve')
const currentRow = ref<TodoRow | null>(null)
const approveNote = ref('')
const submitting = ref(false)

const dialogTitle = computed(() => (dialogMode.value === 'approve' ? '通过任务' : '驳回任务'))

/** 打开审批对话框：先填写审批描述，确认后才发起请求 */
function openApproveDialog(row: TodoRow, mode: ApproveMode) {
  currentRow.value = row
  dialogMode.value = mode
  approveNote.value = ''
  dialogVisible.value = true
}

/**
 * 提交审批：后端无 /approve、/reject 端点，统一走流程引擎 POST /flowEngine/deal
 * （沿指定连线流转，参数 workflow/flowGraph/edge）。
 * 注意：edge 需传流程图中定义的连线编码，当前以审批模式占位（agree/reject），
 * 若流程图连线编码不同需在此调整；审批描述后端暂无对应参数。
 */
async function submitApprove() {
  const row = currentRow.value
  if (!row) return
  const isApprove = dialogMode.value === 'approve'
  // 驳回必须填写审批描述，通过时选填
  if (!isApprove && !approveNote.value.trim()) {
    ElMessage.warning('请填写审批描述')
    return
  }
  submitting.value = true
  try {
    await flowEngineAPI.deal({
      workflow: row.code,
      flowGraph: row.flowgraph,
      edge: isApprove ? 'agree' : 'reject',
    })
    ElMessage.success(isApprove ? '已通过' : '已驳回')
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '操作失败'))
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="approval-page">
    <h3 class="page-title">我的待办</h3>

    <!-- 查询区：后端 WorkflowQuery 暂只支持分页，时间/编号过滤待后端补齐后恢复 -->

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="code" label="任务编号" min-width="140" />
      <el-table-column prop="name" label="任务名称" min-width="160" />
      <el-table-column prop="category" label="分类" min-width="120">
        <template #default="{ row }">
          {{ formatCategory(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="nodeName" label="当前节点" min-width="140" />
      <el-table-column prop="sender" label="发起人" min-width="120" />
      <el-table-column prop="beginTime" label="发起时间" min-width="180" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="formatStateType(row.state)">{{ formatStateLabel(row.state) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="flowgraph" v-if="false" label="流程图编号" min-width="140" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openApproveDialog(row as TodoRow, 'approve')">同意</el-button>
          <el-button size="small" type="danger" @click="openApproveDialog(row as TodoRow, 'reject')">驳回</el-button>
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

    <!-- 审批对话框：填写审批描述后再提交 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
      @closed="currentRow = null"
    >
      <el-form label-width="90px">
        <el-form-item label="任务名称">
          <el-input :model-value="currentRow?.name" disabled />
        </el-form-item>
        <el-form-item label="审批描述" :required="dialogMode === 'reject'">
          <el-input
            v-model="approveNote"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            :placeholder="dialogMode === 'reject' ? '请填写驳回原因' : '请输入审批描述（选填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          :type="dialogMode === 'approve' ? 'success' : 'danger'"
          :loading="submitting"
          @click="submitApprove"
        >
          {{ dialogMode === 'approve' ? '同意' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.approval-page {
  padding: 20px;
  color: #333;
}
</style>