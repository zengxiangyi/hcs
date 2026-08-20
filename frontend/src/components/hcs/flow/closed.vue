<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Graph from './graph.vue'

defineOptions({ name: 'Closed' })

/** 已结束流程行数据类型（当前为占位数据，后续可对接审批流接口） */
interface ClosedFlowRow {
  flowNo: string
  flowName: string
  initiator: string
  startTime: string
  endTime: string
  status: string
}

/** 示例数据 —— 待后端提供流程接口后替换为接口拉取 */
const sampleData: ClosedFlowRow[] = [
  {
    flowNo: 'FL20260818001',
    flowName: '轧辊热处理工艺变更审批',
    initiator: '张工',
    startTime: '2026-08-12 09:30:00',
    endTime: '2026-08-18 16:00:00',
    status: '已结束',
  },
  {
    flowNo: 'FL20260818002',
    flowName: '新产品工艺参数定版审批',
    initiator: '李工',
    startTime: '2026-08-13 10:00:00',
    endTime: '2026-08-18 17:30:00',
    status: '已结束',
  },
  {
    flowNo: 'FL20260815003',
    flowName: '设备维护方案审批',
    initiator: '王工',
    startTime: '2026-08-10 08:45:00',
    endTime: '2026-08-15 15:20:00',
    status: '已结束',
  },
  {
    flowNo: 'FL20260812004',
    flowName: '退火工艺调整审批',
    initiator: '赵工',
    startTime: '2026-08-06 11:00:00',
    endTime: '2026-08-12 14:10:00',
    status: '已结束',
  },
]

const tableData = ref<ClosedFlowRow[]>(sampleData)
const total = ref(sampleData.length)
const loading = ref(false)

/** 流程详情对话框状态 */
const dialogVisible = ref(false)
const currentFlow = ref<ClosedFlowRow | null>(null)

/** 查看流程详情：弹出对话框显示流程图 */
function handleView(row: ClosedFlowRow) {
  currentFlow.value = row
  dialogVisible.value = true
}

onMounted(() => {
  // 后续可在此处调用流程接口拉取已结束流程数据
  loading.value = false
})
</script>

<template>
  <div class="closed-page">
    <h3 class="page-title">已结束流程</h3>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="flowNo" label="流程编号" min-width="150" />
      <el-table-column prop="flowName" label="流程名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="initiator" label="发起人" min-width="100" />
      <el-table-column prop="startTime" label="发起时间" min-width="180" />
      <el-table-column prop="endTime" label="结束时间" min-width="180" />
      <el-table-column prop="status" label="流程状态" min-width="100">
        <template #default="{ row }">
          <el-tag type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      :total="total"
      :page-sizes="[5, 10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
    />

    <!-- 流程详情对话框：展示流程图 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`流程详情：${currentFlow?.flowName ?? ''}`"
      width="600px"
      align-center
      destroy-on-close
    >
      <Graph />
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.closed-page {
  padding: 20px;
  color: #333;
}

.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
