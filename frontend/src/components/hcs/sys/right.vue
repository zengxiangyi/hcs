<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { rights, nextId, type RightType } from './mock'

defineOptions({ name: 'SysRight' })

interface RightForm {
  code: string
  name: string
  type: RightType
  module: string
}

const tableData = ref([...rights])
const query = ref({ module: '', type: '' as '' | RightType })

const moduleOptions = computed(() => [...new Set(rights.map((r) => r.module))])
const typeOptions: { label: string; value: RightType }[] = [
  { label: '页面权限', value: 'page' },
  { label: '按钮权限', value: 'btn' },
]

function refresh() {
  tableData.value = rights.filter(
    (r) =>
      (!query.value.module || r.module === query.value.module) &&
      (!query.value.type || r.type === query.value.type)
  )
}

function handleSearch() {
  refresh()
}
function handleReset() {
  query.value = { module: '', type: '' }
  refresh()
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref(0)
const form = ref<RightForm>({ code: '', name: '', type: 'page', module: '系统管理' })

function resetForm() {
  form.value = { code: '', name: '', type: 'page', module: '系统管理' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增权限'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: (typeof rights)[number]) {
  dialogTitle.value = '编辑权限'
  editId.value = row.id
  form.value = { code: row.code, name: row.name, type: row.type, module: row.module }
  dialogVisible.value = true
}

function handleDelete(row: (typeof rights)[number]) {
  ElMessageBox.confirm(`确认删除权限「${row.name}」？`, '提示', { type: 'warning' })
    .then(() => {
      const idx = rights.findIndex((r) => r.id === row.id)
      if (idx > -1) rights.splice(idx, 1)
      ElMessage.success('删除成功')
      refresh()
    })
    .catch(() => {})
}

function handleSave() {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    if (editId.value) {
      const r = rights.find((x) => x.id === editId.value)
      if (r) Object.assign(r, form.value)
      ElMessage.success('修改成功')
    } else {
      rights.push({ id: nextId(), ...form.value })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    refresh()
  })
}

onMounted(refresh)
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">权限管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="模块">
        <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 140px">
          <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 140px">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
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
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="name" label="权限名称" min-width="160" />
      <el-table-column prop="code" label="权限标识" min-width="180" />
      <el-table-column prop="module" label="模块" min-width="120" />
      <el-table-column prop="type" label="类型" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'page' ? 'primary' : 'warning'">
            {{ row.type === 'page' ? '页面' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="如 用户-新增" />
        </el-form-item>
        <el-form-item label="标识" prop="code" :rules="[{ required: true, message: '请输入标识', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="如 btn:user:add" />
        </el-form-item>
        <el-form-item label="模块" prop="module" :rules="[{ required: true, message: '请输入模块', trigger: 'blur' }]">
          <el-input v-model="form.module" placeholder="如 系统管理" />
        </el-form-item>
        <el-form-item label="类型" prop="type" :rules="[{ required: true }]">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
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
.page-title { margin: 0 0 16px; font-size: 18px; color: #303133; }
.query-form { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
