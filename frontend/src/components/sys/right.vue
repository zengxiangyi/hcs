<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { rightAPI, type RightRow, type RightSaveParams } from '../../api/sysRight'

defineOptions({ name: 'Right' })

interface RightForm {
  id: number
  code: string
  name: string
  category: string
  parent: string
  remark: string
}

const loading = ref(false)
const tableData = ref<RightRow[]>([])
const query = ref({ category: '', name: '', code: '' })

const categoryOptions = ref([
  { label: '按钮', value: 'button' },
  { label: '目录', value: 'directory' },
  { label: '页面', value: 'page' }
])

// 服务端分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function refresh() {
  loading.value = true
  try {
    const res = await rightAPI.search({
      category: query.value.category || undefined,
      name: query.value.name || undefined,
      code: query.value.code || undefined,
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
  query.value = { category: '', name: '', code: '' }
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
const form = ref<RightForm>({id:0, code: '', name: '', category: 'page', remark: '', parent: ''})

function resetForm() {
  form.value = {id:0,code: '', name: '', category: 'page', remark: '', parent: ''}
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增权限'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: RightRow) {
  dialogTitle.value = '编辑权限'
  editId.value = row.id
  form.value = {id:row.id, code: row.code, name: row.name, category: row.category, remark: row.remark, parent: row.parent}
  dialogVisible.value = true
}

function handleDelete(row: RightRow) {
  ElMessageBox.confirm(`确认删除权限「${row.name}」？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await rightAPI.remove(row.code)
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
    const payload: RightSaveParams = { ...form.value }
    try {
      if (editId.value) {
        await rightAPI.update(editId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await rightAPI.add(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      refresh()
    } catch (e) {
      ElMessage.error((e as Error).message || '保存失败')
    }
  })
}

onMounted(refresh)
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">权限管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="类型">
        <el-select v-model="query.category" placeholder="全部类型" clearable style="width: 140px">
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="query.name" placeholder="权限名称" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="标志">
        <el-input v-model="query.code" placeholder="权限标志" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增权限</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column type="id" v-if="false" label="id" width="60" />
      <el-table-column prop="name" label="权限名称" min-width="160" />
      <el-table-column prop="code" label="权限标识" min-width="180" />
      <el-table-column prop="category" label="类型" min-width="120">
        <template #default="{ row }">
          {{ categoryOptions.find((c) => c.value === row.category)?.label || row.category }}
        </template>
      </el-table-column>
      <el-table-column prop="parent" label="父级" min-width="160" />
      <el-table-column prop="remark" label="备注" min-width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row as RightRow)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as RightRow)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="如 用户-新增" />
        </el-form-item>
        <el-form-item label="标识" prop="code" :rules="[{ required: true, message: '请输入标识', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="如 btn:user:add" :readonly="form.id > 0" />
        </el-form-item>
        <el-form-item label="类型" prop="category" :rules="[{ required: true, message: '请选择类型', trigger: 'change' }]">
          <el-select v-model="form.category" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级" prop="parent">
          <el-input v-model="form.parent" placeholder="父级编码" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明" />
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
.sys-page { padding: 20px; color: #333; }
</style>
