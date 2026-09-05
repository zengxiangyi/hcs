<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { transferAPI } from '../../api/transferOrder'
import type { TransferRow, TransferCreateParams } from '../../api/transferOrder'
import { blueprintAPI } from '../../api/blueprint'
import { taskProcessAPI } from '../../api/taskProcess'

defineOptions({ name: 'Transfer' })

// ===================== 调拨单（上方表格） =====================
const transferQuery = ref({
  code: '',
  name: '',
  state: '',
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
    code: transferQuery.value.code.trim(),
    name: transferQuery.value.name.trim(),
    state: transferQuery.value.state,
    ...overrides,
  }
}

// 拉取调拨单列表（对接后端 /api/transfer/search）
async function fetchTransferData() {
  transferLoading.value = true
  try {
    const res = await transferAPI.search(
      buildTransferParams({ page: transferPage.value, pageSize: transferPageSize.value })
    )
    transferData.value = res.data?.content ?? []
    transferTotal.value = res.data?.total ?? 0
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
  transferQuery.value = { code: '', name: '', state: '' }
  transferPage.value = 1
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

// ===================== 新增/编辑调拨单（对话框） =====================
const dialogVisible = ref(false)
const submitting = ref(false)
const editId = ref<number | null>(null)

// 新增调拨单表单（与后端表结构对齐）
const emptyForm: TransferCreateParams = {
  code: '',
  name: '',
  category: '',
  transferDate: '',
  materialCode: '',
  num: 1,
  weight: 0,
  material: '',
  rollNum: '',
  outProcess: '',
  inProcess: '',
  outRoom: '',
  inRoom: '',
  remark: '',
  prompt: '',
  quenching: '',
  supplier: '',
  createUser: '',
  createTime: '',
  state: '',
}
const form = ref<TransferCreateParams>({ ...emptyForm })
const formRef = ref()

function handleAdd() {
  editId.value = null
  form.value = { ...emptyForm }
  dialogVisible.value = true
}

// 编辑：回填表单，复用新增对话框
function handleEdit(row: TransferRow) {
  editId.value = row.id
  form.value = { ...emptyForm, ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editId.value != null) {
      await transferAPI.update({ ...form.value })
      ElMessage.success('编辑调拨单成功')
    } else {
      await transferAPI.create({ ...form.value })
      ElMessage.success('新增调拨单成功')
    }
    dialogVisible.value = false
    fetchTransferData()
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '保存失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: TransferRow) {
  try {
    await ElMessageBox.confirm(
      `确定删除调拨单「${row.code}」吗？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return // 用户取消
  }
  try {
    await transferAPI.delete(row.id)
    ElMessage.success('删除成功')
    // 若删除的是当前选中行，清空选中
    if (selectedTransfer.value?.id === row.id) {
      selectedTransfer.value = null
    }
    fetchTransferData()
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '删除失败'
    ElMessage.error(msg)
  }
}

// ===================== 蓝本信息（下方表格） =====================
// 蓝本信息行数据（对接 /api/blueprint/search）
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

// 拉取蓝本信息列表（对接后端 /api/blueprint/search）
async function fetchBlueprintData() {
  blueprintLoading.value = true
  try {
    const res = await blueprintAPI.search({ page: 1, pageSize: 99999 })
    const list = res.data?.content ?? []
    blueprintData.value = list
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '获取蓝本信息失败'
    ElMessage.error(msg)
  } finally {
    blueprintLoading.value = false
  }
}

// 将蓝本绑定到当前选中的调拨单
async function handleBindBlueprint(row: BlueprintRow) {
  if (!selectedTransfer.value) {
    ElMessage.warning('请先在上方调拨单表格中选择一条调拨单')
    return
  }
  if (row.bound) {
    ElMessage.warning(`蓝本「${row.blueprintName}」已绑定调拨单 ${row.boundTransferNo}`)
    return
  }
  try {

    await taskProcessAPI.bind({
      transfer: selectedTransfer.value.code,
      blueprint: row.code,
    })
    row.bound = true
    row.boundTransferNo = selectedTransfer.value.code
    ElMessage.success(`已将蓝本「${row.blueprintName}」绑定至调拨单 ${selectedTransfer.value.code}`)
  } catch (err) {
    const msg = err instanceof Error && err.message ? err.message : '绑定失败'
    ElMessage.error(msg)
  }
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
          <el-input v-model="transferQuery.code" placeholder="调拨单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="transferQuery.name" placeholder="名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="transferQuery.state" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="s in transferStatusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleTransferSearch">查询</el-button>
          <el-button @click="handleTransferReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <!-- 新增调拨单对话框 -->
      <el-dialog v-model="dialogVisible" :title="editId != null ? '编辑调拨单' : '新增调拨单'" width="60%" :close-on-click-modal="false">
        <el-form ref="formRef" :model="form" label-width="100px" class="add-form">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="调拨单号" prop="code" :rules="[{ required: true, message: '请输入调拨单号', trigger: 'blur' }]">
                <el-input v-model="form.code" readonly placeholder="调拨单号" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
                <el-input v-model="form.name" placeholder="产品名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="调拨类型" prop="category">
                <el-input v-model="form.category" placeholder="调拨类型" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="调拨日期" prop="transferDate">
                <el-date-picker v-model="form.transferDate" type="date" placeholder="调拨日期" style="width: 100%" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="物料编码" prop="materialCode">
                <el-input v-model="form.materialCode" placeholder="物料编码" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="创建人" prop="createUser">
                <el-input v-model="form.createUser" placeholder="创建人" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="创建时间" prop="createTime">
                <el-date-picker v-model="form.createTime" type="date" placeholder="创建日期" style="width: 100%" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="数量" prop="num">
                <el-input-number v-model="form.num" :min="1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="单重" prop="weight">
                <el-input v-model="form.weight" placeholder="单重" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="材质" prop="material">
                <el-input v-model="form.material" placeholder="材质" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="辊号" prop="rollNum">
                <el-input v-model="form.rollNum" placeholder="辊号" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="调出工序组" prop="outProcess">
                <el-input v-model="form.outProcess" placeholder="调出工序组" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="调入工序组" prop="inProcess">
                <el-input v-model="form.inProcess" placeholder="调入工序组" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="调出仓库" prop="outRoom">
                <el-input v-model="form.outRoom" placeholder="调出仓库" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="调入仓库" prop="inRoom">
                <el-input v-model="form.inRoom" placeholder="调入仓库" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="质量提示" prop="prompt">
                <el-input v-model="form.prompt" placeholder="质量提示" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="淬火设备" prop="quenching">
                <el-input v-model="form.quenching" placeholder="淬火设备" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="供应商" prop="supplier">
                <el-input v-model="form.supplier" placeholder="供应商" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
          </div>
        </template>
      </el-dialog>

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
        <el-table-column prop="code" label="调拨单号" min-width="160" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="category" label="调拨类型" min-width="110" />
        <el-table-column prop="transferDate" label="调拨日期" min-width="120" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="num" label="数量" width="90" />
        <el-table-column prop="weight" label="单重" width="90" />
        <el-table-column prop="material" label="材质" min-width="110" />
        <el-table-column prop="rollNum" label="辊号" min-width="100" />
        <el-table-column prop="outProcess" label="调出工序组" min-width="120" />
        <el-table-column prop="inProcess" label="调入工序组" min-width="120" />
        <el-table-column prop="outRoom" label="调出仓库" min-width="120" />
        <el-table-column prop="inRoom" label="调入仓库" min-width="120" />
        <el-table-column prop="remark" label="急件说明" min-width="120" />
        <el-table-column prop="prompt" label="质量提示" min-width="120" />
        <el-table-column prop="quenching" label="淬火设备" min-width="120" />
        <el-table-column prop="supplier" label="供应商" min-width="120" />
        <el-table-column prop="createUser" label="创建人" min-width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="state" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.state === '已完成' ? 'success' : row.state === '已取消' ? 'info' : 'warning'">
              {{ row.state }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row as TransferRow)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row as TransferRow)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="transferPage"
        v-model:page-size="transferPageSize"
        :total="transferTotal"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="fetchTransferData"
        @size-change="handleTransferSizeChange"
      />
    </section>
    <!-- 当前选中调拨单提示 -->
    <div class="selected-tip" v-if="selectedTransfer">
      当前选中调拨单：<strong>{{ selectedTransfer.code }}</strong>
      （{{ selectedTransfer.name }}）
    </div>

    <!-- 下方：蓝本信息 -->
    <section class="block">
      <h3 class="block-title">蓝本信息</h3>

      <el-table v-loading="blueprintLoading" :data="blueprintData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="code" label="蓝本工艺编码" width="120" />
        <el-table-column prop="name" label="蓝本名称" min-width="140" />
        <el-table-column prop="graph" label="图号" width="110" />
        <el-table-column prop="firstLevel" label="一级分类" width="110">
        </el-table-column>
        <el-table-column prop="secondLevel" label="二级分类" width="110">
        </el-table-column>
        <el-table-column prop="materialName" label="物料名称" min-width="120" />
        <el-table-column prop="materialCode" label="材料编码" width="120" />
        <el-table-column prop="weight" label="单重" width="100" />
        <el-table-column prop="version" label="版本" width="90" />
        <el-table-column prop="boundTransferNo" label="已绑定调拨单" min-width="160">
          <template #default="{ row }">
            <span v-if="row.boundTransferNo">{{ row.boundTransferNo }}</span>
            <el-tag v-else type="info">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleBindBlueprint(row as BlueprintRow)">绑定</el-button>
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

.selected-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.dialog-footer .el-button {
  min-width: 96px;
}
</style>
