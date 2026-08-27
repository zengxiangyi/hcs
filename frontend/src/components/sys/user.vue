<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { sysUserAPI, type SysUserRow, type SysUserSaveParams } from '../../api/user'

defineOptions({ name: 'User' })

interface UserForm {
  code: string
  name: string
  department: string
  position: string
  cellphone: string
  email: string
  remark: string
  state: '启用' | '禁用'
}

const deptOptions = ['技术部', '内容部', '市场部', '设计部', '人事部', '财务部']
const statusOptions = ['启用', '禁用']

const query = ref({ code: '', name: '', department: '' })

// 服务端分页 + 过滤
const tableData = ref<SysUserRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

// 从后端拉取列表
async function refresh() {
  loading.value = true
  try {
    const res = await sysUserAPI.search({
      code: query.value.code.trim() || undefined,
      name: query.value.name.trim() || undefined,
      department: query.value.department || undefined,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    tableData.value = res.data.content
    total.value = res.data.total
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '获取用户列表失败'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  refresh()
}

function handleReset() {
  query.value = { code: '', name: '', department: '' }
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
const form = ref<UserForm>({ code: '', name: '', department: '技术部', position: '', cellphone: '', email: '', remark: '', state: '启用' })

function resetForm() {
  form.value = { code: '', name: '', department: '技术部', position: '', cellphone: '', email: '', remark: '', state: '启用' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: SysUserRow) {
  dialogTitle.value = '编辑用户'
  editId.value = row.id
  form.value = { code: row.code, name: row.name, department: row.department, position: row.position ?? '', cellphone: row.cellphone ?? '', email: row.email ?? '', remark: row.remark ?? '', state: row.state }
  dialogVisible.value = true
}

async function handleDelete(row: SysUserRow) {
  try {
    await sysUserAPI.remove(row.id)
    ElMessage.success('删除成功')
    // 删除后若当前页已空，回退一页避免空白
    if (tableData.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }
    refresh()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, '删除失败'))
  }
}

function handleSave() {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    const payload: SysUserSaveParams = { ...form.value }
    try {
      if (editId.value) {
        await sysUserAPI.update(editId.value, { ...payload, id: editId.value })
        ElMessage.success('修改成功')
      } else {
        await sysUserAPI.add(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      refresh()
    } catch (err) {
      ElMessage.error(getErrorMessage(err, editId.value ? '修改失败' : '新增失败'))
    }
  })
}

onMounted(refresh)
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">用户管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="工号">
        <el-input v-model="query.code" placeholder="工号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="姓名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.department" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="code" label="工号" min-width="120" />
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="department" label="部门" min-width="120" />
      <el-table-column prop="position" label="岗位" min-width="120" />
      <el-table-column prop="cellphone" label="手机号" min-width="140" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === '启用' ? 'success' : 'danger'">{{ row.state }}</el-tag>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="工号" prop="code" :rules="[{ required: true, message: '请输入工号', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name" :rules="[{ required: true, message: '请输入姓名', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department" :rules="[{ required: true, message: '请选择部门', trigger: 'change' }]">
          <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-input v-model="form.position" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="手机号" prop="cellphone">
          <el-input v-model="form.cellphone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明" />
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
</style>
