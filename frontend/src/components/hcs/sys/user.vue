<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { users, roles, userRoleMap, getRoleIdsByUser, nextId } from './mock'
import { hasRight } from './permission'

defineOptions({ name: 'SysUser' })

interface UserForm {
  username: string
  nickname: string
  dept: string
  state: '启用' | '禁用'
}

const deptOptions = ['技术部', '内容部', '市场部', '设计部', '人事部', '财务部']
const statusOptions = ['启用', '禁用']

const tableData = ref([...users])
const query = ref({ username: '', dept: '' })

// 按钮权限
const canAdd = computed(() => hasRight('btn:user:add'))
const canEdit = computed(() => hasRight('btn:user:edit'))
const canDelete = computed(() => hasRight('btn:user:delete'))

function refresh() {
  tableData.value = users.filter(
    (u) =>
      (!query.value.username || u.username.includes(query.value.username.trim())) &&
      (!query.value.dept || u.dept === query.value.dept)
  )
}

function handleSearch() {
  refresh()
}

function handleReset() {
  query.value = { username: '', dept: '' }
  refresh()
}

// 角色展示：把用户所属角色 id 转为名称
function roleNamesOf(userId: number): string {
  return getRoleIdsByUser(userId)
    .map((rid) => roles.find((r) => r.id === rid)?.name)
    .filter(Boolean)
    .join('，')
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref(0)
const form = ref<UserForm>({ username: '', nickname: '', dept: '技术部', state: '启用' })

function resetForm() {
  form.value = { username: '', nickname: '', dept: '技术部', state: '启用' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  if (!canAdd.value) return ElMessage.warning('无新增权限')
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: (typeof users)[number]) {
  if (!canEdit.value) return ElMessage.warning('无编辑权限')
  dialogTitle.value = '编辑用户'
  editId.value = row.id
  form.value = { username: row.username, nickname: row.nickname, dept: row.dept, state: row.state }
  dialogVisible.value = true
}

function handleDelete(row: (typeof users)[number]) {
  if (!canDelete.value) return ElMessage.warning('无删除权限')
  const idx = users.findIndex((u) => u.id === row.id)
  if (idx > -1) {
    users.splice(idx, 1)
    delete userRoleMap[row.id]
  }
  ElMessage.success('删除成功')
  refresh()
}

function handleSave() {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    if (editId.value) {
      const u = users.find((x) => x.id === editId.value)
      if (u) Object.assign(u, form.value)
      ElMessage.success('修改成功')
    } else {
      const id = nextId()
      users.push({ id, ...form.value })
      userRoleMap[id] = [] // 新用户默认无角色
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
    <h3 class="page-title">用户管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="账号">
        <el-input v-model="query.username" placeholder="账号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.dept" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
        </el-select>
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
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="dept" label="部门" min-width="120" />
      <el-table-column label="所属角色" min-width="160">
        <template #default="{ row }">{{ roleNamesOf(row.id) || '—' }}</template>
      </el-table-column>
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === '启用' ? 'success' : 'danger'">{{ row.state }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link :disabled="!canEdit" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link :disabled="!canDelete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="账号" prop="username" :rules="[{ required: true, message: '请输入账号', trigger: 'blur' }]">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname" :rules="[{ required: true, message: '请输入昵称', trigger: 'blur' }]">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="部门" prop="dept" :rules="[{ required: true, message: '请选择部门', trigger: 'change' }]">
          <el-select v-model="form.dept" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-select v-model="form.state" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
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
