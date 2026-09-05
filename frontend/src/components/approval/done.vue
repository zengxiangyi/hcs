<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createTextFormatter, createStateFormatter, type TagType } from '../../utils/enum'
import { workflowAPI, type WorkflowRow } from '../../api/workflow'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Done' })

const router = useRouter()

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

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
  S: { label: '暂停', type: 'warning' },
  E: { label: '结束', type: 'success' },
  D: { label: '处理中', type:'primary' },
  C: { label: '取消', type: 'info' },
  R: { label: '作废', type: 'danger' },
}

/** 根据状态 key 取展示文本 / tag 类型，未匹配时文本回退原值、类型回退 warning（公共方法生成） */
const { label: formatStateLabel, type: formatStateType } = createStateFormatter(stateMap)

// 拉取列表（dealUser/roleCode 由后端按当前登录用户覆盖，前端只传分页参数）
async function fetchData() {
  loading.value = true
  try {
    const res = await workflowAPI.done({ page: currentPage.value, pageSize: pageSize.value })
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

// 查看：携带流程编号跳转到 instance.vue
function seeProcess(row: WorkflowRow) {
  router.push({ name: 'Instance', query: { workflow: row.code } })
}

onMounted(fetchData)
</script>

<template>
  <div class="approval-page">
    <h3 class="page-title">我的已办</h3>

    <!-- 查询区：后端 WorkflowQuery 暂只支持分页，时间/编号过滤待后端补齐后恢复 -->

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" v-if="false" label="ID" width="80" />
      <el-table-column prop="code" label="任务编号" min-width="140" />
      <el-table-column prop="name" label="任务名称" min-width="160" />
      <el-table-column prop="category" label="分类" min-width="120">
        <template #default="{ row }">
          {{ formatCategory(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetCode" v-if="false" label="目标编号" min-width="140" />
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