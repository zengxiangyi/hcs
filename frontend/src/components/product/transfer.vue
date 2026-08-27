<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'Transfer' })

// ===================== 调拨单（上方表格） =====================
// 调拨单查询条件
interface TransferQuery {
  transferNo: string
  productName: string
  status: string
}

// 调拨单行数据
interface TransferRow {
  id: number
  transferNo: string
  productName: string
  sourceWarehouse: string
  targetWarehouse: string
  quantity: number
  status: string
  createTime: string
}

const transferQuery = ref<TransferQuery>({
  transferNo: '',
  productName: '',
  status: '',
})

const transferStatusOptions = ['待调拨', '调拨中', '已完成', '已取消']

const transferData = ref<TransferRow[]>([])
const transferTotal = ref(0)
const transferPage = ref(1)
const transferPageSize = ref(10)
const transferLoading = ref(false)

// 当前选中的调拨单（点击查询/行选中后，用于与下方蓝本绑定）
const selectedTransfer = ref<TransferRow | null>(null)

function buildTransferParams(overrides: Partial<{ page: number; pageSize: number }> = {}) {
  return {
    transferNo: transferQuery.value.transferNo.trim(),
    productName: transferQuery.value.productName.trim(),
    status: transferQuery.value.status,
    ...overrides,
  }
}

// 拉取调拨单列表（此处用本地模拟数据，接入后端时替换为 API 调用）
async function fetchTransferData() {
  transferLoading.value = true
  try {
    // TODO: 接入后端接口，例如 await productAPI.getTransfers(buildTransferParams({ page: transferPage.value, pageSize: transferPageSize.value }))
    const mock: TransferRow[] = [
      { id: 1, transferNo: 'DB-20260801-001', productName: '热轧辊坯', sourceWarehouse: '一车间库', targetWarehouse: '二车间库', quantity: 120, status: '待调拨', createTime: '2026-08-01 09:12' },
      { id: 2, transferNo: 'DB-20260801-002', productName: '冷轧支撑辊', sourceWarehouse: '成品库', targetWarehouse: '热处理库', quantity: 36, status: '调拨中', createTime: '2026-08-01 10:30' },
      { id: 3, transferNo: 'DB-20260802-003', productName: '锻钢轧辊', sourceWarehouse: '原料库', targetWarehouse: '一车间库', quantity: 80, status: '已完成', createTime: '2026-08-02 14:05' },
      { id: 4, transferNo: 'DB-20260803-004', productName: '复合轧辊', sourceWarehouse: '二车间库', targetWarehouse: '成品库', quantity: 50, status: '已取消', createTime: '2026-08-03 16:48' },
    ]
    transferData.value = mock
    transferTotal.value = mock.length
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '获取调拨单失败'
    ElMessage.error(msg)
  } finally {
    transferLoading.value = false
  }
}

function handleTransferSearch() {
  transferPage.value = 1
  fetchTransferData()
}

function handleTransferReset() {
  transferQuery.value = { transferNo: '', productName: '', status: '' }
  transferPage.value = 1
  fetchTransferData()
}

function handleTransferPageChange() {
  fetchTransferData()
}

function handleTransferSizeChange() {
  transferPage.value = 1
  fetchTransferData()
}

// 选中调拨单行：记录当前调拨单，供下方蓝本绑定使用
function handleTransferRowClick(row: TransferRow) {
  selectedTransfer.value = row
}

// ===================== 蓝本信息（下方表格） =====================
// 蓝本信息行数据
interface BlueprintRow {
  id: number
  blueprintNo: string
  blueprintName: string
  spec: string
  material: string
  version: string
  bound: boolean
  boundTransferNo: string
}

const blueprintData = ref<BlueprintRow[]>([])
const blueprintLoading = ref(false)

// 拉取蓝本信息列表（此处用本地模拟数据，接入后端时替换为 API 调用）
async function fetchBlueprintData() {
  blueprintLoading.value = true
  try {
    // TODO: 接入后端接口，例如 await productAPI.getBlueprints()
    const mock: BlueprintRow[] = [
      { id: 1, blueprintNo: 'LB-2026-001', blueprintName: '热轧辊坯工艺蓝本', spec: 'Φ800×1600', material: '60CrMnMo', version: 'V2.1', bound: false, boundTransferNo: '' },
      { id: 2, blueprintNo: 'LB-2026-002', blueprintName: '冷轧支撑辊工艺蓝本', spec: 'Φ1200×2000', material: '9Cr2Mo', version: 'V1.4', bound: false, boundTransferNo: '' },
      { id: 3, blueprintNo: 'LB-2026-003', blueprintName: '锻钢轧辊工艺蓝本', spec: 'Φ600×1200', material: '86CrMoV7', version: 'V3.0', bound: true, boundTransferNo: 'DB-20260802-003' },
      { id: 4, blueprintNo: 'LB-2026-004', blueprintName: '复合轧辊工艺蓝本', spec: 'Φ750×1500', material: '复合层', version: 'V2.0', bound: false, boundTransferNo: '' },
    ]
    blueprintData.value = mock
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '获取蓝本信息失败'
    ElMessage.error(msg)
  } finally {
    blueprintLoading.value = false
  }
}

// 将蓝本绑定到当前选中的调拨单
function handleBindBlueprint(row: BlueprintRow) {
  if (!selectedTransfer.value) {
    ElMessage.warning('请先在上方调拨单表格中选择一条调拨单')
    return
  }
  if (row.bound) {
    ElMessage.warning(`蓝本「${row.blueprintName}」已绑定调拨单 ${row.boundTransferNo}`)
    return
  }
  row.bound = true
  row.boundTransferNo = selectedTransfer.value.transferNo
  // TODO: 接入后端绑定接口，例如 await productAPI.bindBlueprint({ transferId: selectedTransfer.value.id, blueprintId: row.id })
  ElMessage.success(`已将蓝本「${row.blueprintName}」绑定至调拨单 ${selectedTransfer.value.transferNo}`)
}

onMounted(() => {
  fetchTransferData()
  fetchBlueprintData()
})
</script>

<template>
  <div class="transfer-page">
    <!-- 上方：调拨单 -->
    <section class="block">
      <h3 class="block-title">调拨单</h3>

      <!-- 查询区 -->
      <el-form :inline="true" class="query-form" @submit.prevent>
        <el-form-item label="调拨单号">
          <el-input v-model="transferQuery.transferNo" placeholder="调拨单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="transferQuery.productName" placeholder="产品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="transferQuery.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="s in transferStatusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleTransferSearch">查询</el-button>
          <el-button @click="handleTransferReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 当前选中调拨单提示 -->
      <div class="selected-tip" v-if="selectedTransfer">
        当前选中调拨单：<strong>{{ selectedTransfer.transferNo }}</strong>
        （{{ selectedTransfer.productName }}）
      </div>

      <!-- 调拨单表格 -->
      <el-table
        v-loading="transferLoading"
        :data="transferData"
        border
        stripe
        highlight-current-row
        style="width: 100%"
        @row-click="handleTransferRowClick"
      >
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="transferNo" label="调拨单号" min-width="160" />
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="sourceWarehouse" label="调出仓库" min-width="120" />
        <el-table-column prop="targetWarehouse" label="调入仓库" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : row.status === '已取消' ? 'info' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="transferPage"
        v-model:page-size="transferPageSize"
        :total="transferTotal"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="handleTransferPageChange"
        @size-change="handleTransferSizeChange"
      />
    </section>

    <!-- 下方：蓝本信息 -->
    <section class="block">
      <h3 class="block-title">蓝本信息</h3>

      <el-table v-loading="blueprintLoading" :data="blueprintData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="blueprintNo" label="蓝本编号" min-width="150" />
        <el-table-column prop="blueprintName" label="蓝本名称" min-width="200" />
        <el-table-column prop="spec" label="规格" min-width="120" />
        <el-table-column prop="material" label="材质" min-width="120" />
        <el-table-column prop="version" label="版本" width="90" />
        <el-table-column prop="boundTransferNo" label="已绑定调拨单" min-width="160">
          <template #default="{ row }">
            <span v-if="row.boundTransferNo">{{ row.boundTransferNo }}</span>
            <el-tag v-else type="info">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleBindBlueprint(row)">绑定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.transfer-page {
  padding: 20px;
  color: #333;
}

.block {
  margin-bottom: 28px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.block-title {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
  border-left: 4px solid #1e90ff;
  padding-left: 8px;
}

.query-form {
  margin-bottom: 16px;
}

.selected-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
