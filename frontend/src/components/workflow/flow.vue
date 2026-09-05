<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowGraphAPI, type FlowGraphRow, type FlowGraphSaveDTO } from '../../api/flowGraph'
import router from '../../router'

defineOptions({ name: 'Flow' })

/** 列表数据 */
const list = ref<FlowGraphRow[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

/** 查询条件 */
const query = ref({
  flowGraph: '',
  title: '',
})

/** 弹窗表单 */
const dialogVisible = ref(false)
const dialogTitle = ref('新增流程图')
const formRef = ref()
const form = ref<FlowGraphSaveDTO>({
  id: 0,
  flowGraph: '',
  title: '',
  width: 0,
  height: 0,
  remark: '',
})

const rules = {
  flowGraph: [{ required: true, message: '请输入流程图编号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

/** 加载列表 */
async function loadList() {
  loading.value = true
  try {
    const res = await flowGraphAPI.search({
      flowGraph: query.value.flowGraph || undefined,
      title: query.value.title || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = res.data.content
    total.value = res.data.total
  } catch (err) {
    ElMessage.error((err as Error).message || '查询失败')
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function onSearch() {
  page.value = 1
  loadList()
}

/** 重置查询 */
function onReset() {
  query.value = { flowGraph: '', title: '' }
  page.value = 1
  loadList()
}

/** 分页变化 */
function onPageChange(p: number) {
  page.value = p
  loadList()
}
function onSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  loadList()
}

/** 打开新增弹窗 */
function openAdd() {
  dialogTitle.value = '新增流程图'
  form.value = { id: 0, flowGraph: '', title: '', width: 0, height: 0, remark: '' }
  dialogVisible.value = true
}

/** 打开编辑弹窗 */
function openEdit(row: FlowGraphRow) {
  dialogTitle.value = '编辑流程图'
  form.value = { ...row }
  dialogVisible.value = true
}

function openShow(flowGraph: string) {
  console.log(flowGraph)
  // 跳转到draw.vue 传递flowgraph参数。
  router.push({ name: 'Draw', query: { flowGraph:flowGraph } })
}

/** 提交表单（新增/编辑） */
async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (form.value.id) {
        await flowGraphAPI.update(form.value)
        ElMessage.success('编辑成功')
      } else {
        await flowGraphAPI.save(form.value)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadList()
    } catch (err) {
      ElMessage.error((err as Error).message || '保存失败')
    }
  })
}

/** 删除 */
async function onRemove(row: FlowGraphRow) {
  try {
    await ElMessageBox.confirm(`确认删除流程图「${row.title}」？`, '提示', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await flowGraphAPI.remove(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (err) {
    ElMessage.error((err as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<template>
  <div class="graph-table">
    <h3>流程图管理</h3>
    <!-- 查询栏 -->
    <div class="graph-query">
      <el-input v-model="query.flowGraph" placeholder="流程图编号" clearable style="width: 180px" @keyup.enter="onSearch" />
      <el-input v-model="query.title" placeholder="标题" clearable style="width: 180px" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增</el-button>
    </div>
    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="id" label="id" width="80" />
      <el-table-column prop="flowGraph" label="流程图编号" min-width="140" />
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="width" label="宽度" width="100" />
      <el-table-column prop="height" label="高度" width="100" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openShow(row.flowGraph)">查看</el-button>
          <el-button size="small" type="primary" @click="openEdit(row as FlowGraphRow)">编辑</el-button>
          <el-button size="small" type="danger" @click="onRemove(row as FlowGraphRow)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="graph-pager">
      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="流程图编号" prop="flowGraph">
          <el-input v-model="form.flowGraph" placeholder="请输入流程图编号" :readonly="form.id > 0" :clearable="form.id === 0" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="宽度">
          <el-input-number v-model="form.width" :min="0" />
        </el-form-item>
        <el-form-item label="高度">
          <el-input-number v-model="form.height" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.graph-table {
  padding: 16px;
}
.graph-query {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.graph-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>