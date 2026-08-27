<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { constValueAPI, type ConstValueRow, type ConstValueSaveParams } from '../../api/constValue'

defineOptions({ name: 'ConstValue' })

interface ConstValueForm {
  code: string
  name: string
  category: string
  mark: string
  remark: string
}

const loading = ref(false)
const tableData = ref<ConstValueRow[]>([])
const query = ref({ code: '', name: '', category: '' })

// 服务端分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function refresh() {
  loading.value = true
  try {
    const res = await constValueAPI.search({
      code: query.value.code || undefined,
      name: query.value.name || undefined,
      category: query.value.category || undefined,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    tableData.value = res.data.content
    total.value = res.data.total
  } catch (e) {
    ElMessage.error((e as Error).message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  refresh()
}
function handleReset() {
  query.value = { code: '', name: '', category: '' }
  currentPage.value = 1
  refresh()
}

// 分页 / 每页条数变化
function handlePageChange() {
  refresh()
}
function handleSizeChange() {
  currentPage.value = 1
  refresh()
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref(0)
const form = ref<ConstValueForm>({ code: '', name: '', category: '', mark: '', remark: '' })

function resetForm() {
  form.value = { code: '', name: '', category: '', mark: '', remark: '' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增常量值'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: ConstValueRow) {
  dialogTitle.value = '编辑常量值'
  editId.value = row.id
  form.value = { code: row.code, name: row.name, category: row.category, mark: row.mark, remark: row.remark }
  dialogVisible.value = true
}

function handleDelete(row: ConstValueRow) {
  ElMessageBox.confirm(`确认删除常量值「${row.name}（${row.code}）」？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await constValueAPI.remove(row.id)
        ElMessage.success('删除成功')
        refresh()
      } catch (e) {
        ElMessage.error((e as Error).message || '删除失败')
      }
    })
    .catch(() => {})
}

function handleSave() {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    const payload: ConstValueSaveParams = { ...form.value }
    try {
      if (editId.value) {
        await constValueAPI.update(editId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await constValueAPI.add(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      refresh()
    } catch (e) {
      ElMessage.error((e as Error).message || '保存失败')
    }
  })
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">常量值管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="编码">
        <el-input v-model="query.code" placeholder="编码" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="query.name" placeholder="名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="query.category" placeholder="分类" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="code" label="编码" min-width="120" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="category" label="分类" min-width="100" />
      <el-table-column prop="mark" label="标记" min-width="220" show-overflow-tooltip />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[5, 10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="编码" prop="code" :rules="[{ required: true, message: '请输入编码', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="如 SEX / STATE" />
        </el-form-item>
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如 基础 / 业务" />
        </el-form-item>
        <el-form-item label="标记" prop="mark">
          <el-input v-model="form.mark" placeholder="取值说明，如 0-男 1-女" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 页面级公共样式（.page-title / .query-form / .toolbar / .el-table / .pagination）
   已抽离至 src/style/common.css，全局复用。此处仅保留页面私有样式。 */
.sys-page {
  padding: 20px;
  color: #333;
  min-height: 100%;
}
</style>
