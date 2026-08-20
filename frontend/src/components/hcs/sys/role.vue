<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { roles, nextId } from './mock'
import { hasRight } from './permission'

defineOptions({ name: 'SysRole' })

interface RoleForm {
  name: string
  code: string
  remark: string
}

const tableData = ref([...roles])
const query = ref({ name: '' })

const canAdd = computed(() => hasRight('btn:role:add'))
const canEdit = computed(() => hasRight('btn:role:edit'))
const canDelete = computed(() => hasRight('btn:role:delete'))

function refresh() {
  tableData.value = roles.filter((r) => !query.value.name || r.name.includes(query.value.name.trim()))
}

function handleSearch() {
  refresh()
}
function handleReset() {
  query.value = { name: '' }
  refresh()
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref(0)
const form = ref<RoleForm>({ name: '', code: '', remark: '' })

function resetForm() {
  form.value = { name: '', code: '', remark: '' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  if (!canAdd.value) return ElMessage.warning('无新增权限')
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: (typeof roles)[number]) {
  if (!canEdit.value) return ElMessage.warning('无编辑权限')
  dialogTitle.value = '编辑角色'
  editId.value = row.id
  form.value = { name: row.name, code: row.code, remark: row.remark }
  dialogVisible.value = true
}

function handleDelete(row: (typeof roles)[number]) {
  if (!canDelete.value) return ElMessage.warning('无删除权限')
  ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示', { type: 'warning' })
    .then(() => {
      const idx = roles.findIndex((r) => r.id === row.id)
      if (idx > -1) roles.splice(idx, 1)
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
      const r = roles.find((x) => x.id === editId.value)
      if (r) Object.assign(r, form.value)
      ElMessage.success('修改成功')
    } else {
      roles.push({ id: nextId(), ...form.value })
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
    <h3 class="page-title">角色管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="角色名">
        <el-input v-model="query.name" placeholder="角色名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" :disabled="!canAdd" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="name" label="角色名" min-width="140" />
      <el-table-column prop="code" label="角色编码" min-width="140" />
      <el-table-column prop="remark" label="备注" min-width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link :disabled="!canEdit" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link :disabled="!canDelete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="角色名" prop="name" :rules="[{ required: true, message: '请输入角色名', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入角色名" />
        </el-form-item>
        <el-form-item label="编码" prop="code" :rules="[{ required: true, message: '请输入编码', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="如 admin / editor" />
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
.sys-page { padding: 20px; color: #333; }
.page-title { margin: 0 0 16px; font-size: 18px; color: #303133; }
.query-form { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
