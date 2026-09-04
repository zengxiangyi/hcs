<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { sysUserAPI, type SysUserRow, type SysUserSaveParams } from '../../api/user'

defineOptions({ name: 'User' })

type UserState = SysUserSaveParams['state']

const DEPARTMENTS = ['技术部', '内容部', '市场部', '设计部', '人事部', '财务部']
const STATES: UserState[] = ['启用', '禁用']

const formRules = {
  code: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
}

/**
 * 后端 SysUserService.update 只落库 code/name/remark/department/position/state，
 * 不更新 email / cellphone，故表单暂不采集这两项，避免"填了存不进"。
 */
function createForm(): SysUserSaveParams {
  return { code: '', name: '', department: '', position: '', remark: '', state: '启用' }
}

function createQuery(): { code: string; name: string; department: string; state: UserState | '' } {
  return { code: '', name: '', department: '', state: '' }
}

const query = ref(createQuery())

// 服务端分页 + 过滤
const tableData = ref<SysUserRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)

let refreshSeq = 0

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

// 从后端拉取列表
async function refresh() {
  const seq = ++refreshSeq
  loading.value = true
  try {
    const res = await sysUserAPI.search({
      code: query.value.code.trim() || undefined,
      name: query.value.name.trim() || undefined,
      department: query.value.department || undefined,
      state: query.value.state || undefined,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    if (seq !== refreshSeq) return // 已有更新的请求发出，丢弃本次过期响应
    tableData.value = res.data?.content ?? []
    total.value = res.data?.total ?? 0
  } catch (err) {
    if (seq === refreshSeq) ElMessage.error(getErrorMessage(err, '获取用户列表失败'))
  } finally {
    if (seq === refreshSeq) loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  refresh()
}

function handleReset() {
  query.value = createQuery()
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

// 新增 / 编辑弹窗
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editId = ref(0)
const form = ref<SysUserSaveParams>(createForm())

const isEdit = computed(() => editId.value > 0)
const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

function resetForm() {
  form.value = createForm()
  editId.value = 0
}

function openDialog() {
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function handleAdd() {
  resetForm()
  openDialog()
}

function handleEdit(row: SysUserRow) {
  editId.value = row.id
  form.value = {
    code: row.code,
    name: row.name,
    department: row.department,
    position: row.position ?? '',
    remark: row.remark ?? '',
    state: row.state,
  }
  openDialog()
}

async function handleDelete(row: SysUserRow) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.name}」？该用户的角色关联会一并解除。`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await sysUserAPI.remove(row.code)
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

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请完善必填项')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await sysUserAPI.update(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await sysUserAPI.add(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    refresh()
  } catch (err) {
    ElMessage.error(getErrorMessage(err, isEdit.value ? '修改失败' : '新增失败'))
  } finally {
    saving.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div class="sys-page">
    <h3 class="page-title">用户管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent @keyup.enter="handleSearch">
      <el-form-item label="工号">
        <el-input v-model="query.code" placeholder="工号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="姓名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.department" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in DEPARTMENTS" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.state" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="s in STATES" :key="s" :label="s" :value="s" />
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

    <el-table v-loading="loading" :data="tableData" row-key="id" border stripe style="width: 100%">
      <el-table-column prop="code" label="工号" min-width="120" />
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="department" label="部门" min-width="120" />
      <el-table-column prop="position" label="岗位" min-width="120" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === '启用' ? 'success' : 'danger'">{{ row.state }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row as SysUserRow)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as SysUserRow)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="工号" prop="code">
          <el-input v-model="form.code" placeholder="请输入工号" :readonly="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in DEPARTMENTS" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-input v-model="form.position" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明" />
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-select v-model="form.state" style="width: 100%">
            <el-option v-for="s in STATES" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.sys-page { padding: 20px; color: #333; }
</style>
