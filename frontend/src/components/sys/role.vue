<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { roleAPI, type RoleRow, type RoleSaveParams } from '../../api/sysRole'

defineOptions({ name: 'Role' })

interface RoleForm {
  id: number
  name: string
  code: string
  category: string
  remark: string
}

const loading = ref(false)
const tableData = ref<RoleRow[]>([])
const query = ref({ name: '' })

// 服务端分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function refresh() {
  loading.value = true
  try {
    const res = await roleAPI.search({
      name: query.value.name || undefined,
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
  query.value = { name: '' }
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
const form = ref<RoleForm>({ id:0,name: '', code: '', category: '', remark: '' })

function resetForm() {
  form.value = { id:0,name: '', code: '', category: '', remark: '' }
  editId.value = 0
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: RoleRow) {
  dialogTitle.value = '编辑角色'
  editId.value = row.id
  form.value = { id:row.id,name: row.name, code: row.code, category: row.category, remark: row.remark }
  dialogVisible.value = true
}

function handleDelete(row: RoleRow) {
  ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await roleAPI.remove(row.code)
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
    const payload: RoleSaveParams = { ...form.value }
    try {
      if (editId.value) {
        await roleAPI.update(editId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await roleAPI.add(payload)
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
    <h3 class="page-title">角色管理</h3>

    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="角色名称">
        <el-input v-model="query.name" placeholder="角色名称" clearable style="width: 180px" />
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
      <el-table-column type="id" v-if="false" label="id" width="60" />
      <el-table-column prop="name" label="角色名称" min-width="120" />
      <el-table-column prop="code" label="角色编码" min-width="120" />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column prop="remark" label="备注" min-width="300" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row as RoleRow)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as RoleRow)">删除</el-button>
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
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入角色名称', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code" :rules="[{ required: true, message: '请输入角色编码', trigger: 'blur' }]">
          <el-input v-model="form.code" placeholder="如 admin / editor" :readonly="form.id>0" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如 系统 / 业务" />
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
</style>
