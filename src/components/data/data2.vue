<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { dataAPI } from '../../api/data'
import type { UserRow } from '../../api/data'

// 查询条件
const query = ref({
  userName: '',
  roleName: '',
  department: '',
  state: '',
})

const statusOptions = ['启用', '禁用']
const deptOptions = ['技术部', '内容部', '市场部', '设计部', '人事部', '财务部']

// 服务端分页 + 过滤
const tableData = ref<UserRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)
const loading = ref(false)

// 从后端拉取列表
async function fetchData() {
  loading.value = true
  try {
    const res = await dataAPI.getUsers({
      userName: query.value.userName.trim(),
      roleName: query.value.roleName.trim(),
      department: query.value.department,
      state: query.value.state,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (err: any) {
    ElMessage.error(err?.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { userName: '', roleName:'',department: '', state: '' }
  currentPage.value = 1
  fetchData()
}

// 分页 / 每页条数变化时重新拉取
function handlePageChange() {
  fetchData()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchData()
}

// Dialog 表单状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 隐藏输入框：存储 id，用于区分新增/修改
const form = ref({
  id: 0,
  userName: '',
  roleName: '',
  department: '',
  state: '启用',
})

function handleEdit(row: UserRow) {
  dialogTitle.value = '编辑用户'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleDelete(row: UserRow) {
  try {
    const res = await dataAPI.deleteUser(row.id)
    ElMessage.success(res.msg || `已删除：${row.userName}`)
    // 若删除的是当前页最后一条且不是第一页，回退一页
    if (tableData.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  form.value = { id: 0, userName: '', roleName: '', department: '', state: '启用' }
  dialogVisible.value = true
}

async function handleSave() {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (form.value.id) {
        // 修改
        await dataAPI.updateUser(form.value.id, {
          userName: form.value.userName,
          roleName: form.value.roleName,
          department: form.value.department,
          state: form.value.state,
        })
        ElMessage.success('修改成功')
      } else {
        // 新增
        await dataAPI.addUser({
          userName: form.value.userName,
          roleName: form.value.roleName,
          department : form.value.department,
          state: form.value.state,
        })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (err: any) {
      ElMessage.error(err?.message || '保存失败')
    }
  })
}

function handleCancel() {
  dialogVisible.value = false
}

// ---------- 导入 ----------
const importDialogVisible = ref(false)
const importLoading = ref(false)

function handleImport() {
  importDialogVisible.value = true
}

async function handleImportConfirm(file: File) {
  importLoading.value = true
  try {
    const data = await file.arrayBuffer()
    const workbook = XLSX.read(data)
    const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
    // 按表头转为对象数组
    const rows = XLSX.utils.sheet_to_json<Record<string, any>>(firstSheet)

    let success = 0
    for (const row of rows) {
      const userName = String(row['姓名'] ?? '').trim()
      const roleName = String(row['角色'] ?? '').trim()
      const department = String(row['部门'] ?? '').trim()
      const state = String(row['状态'] ?? '启用').trim() || '启用'
      if (!userName || !roleName || !department) continue
      await dataAPI.addUser({ userName, roleName, department, state })
      success++
    }

    ElMessage.success(`导入成功 ${success} 条`)
    importDialogVisible.value = false
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.message || '导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

// ---------- 导出 ----------
async function handleExport() {
  try {
    const list = await dataAPI.getUsers({
      userName: query.value.userName.trim(),
      roleName: query.value.roleName.trim(),
      department: query.value.department,
      state: query.value.state,
      page: 1,
      pageSize: 99999,
    })
    const sheetData = list.data.list.map((row) => ({
      姓名: row.userName,
      角色: row.roleName,
      部门: row.department,
      状态: row.state,
      创建时间: row.createTime,
    }))
    const sheet = XLSX.utils.json_to_sheet(sheetData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, sheet, '用户数据')
    XLSX.writeFile(workbook, `用户数据_${Date.now()}.xlsx`)
    ElMessage.success(`导出 ${sheetData.length} 条`)
  } catch (err: any) {
    ElMessage.error(err?.message || '导出失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="data-page">
    <h3 class="page-title">数据2</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="姓名">
        <el-input
          v-model="query.userName"
          placeholder="姓名"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="角色">
        <el-input
          v-model="query.roleName"
          placeholder="角色"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.department" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.state" placeholder="全部状态" clearable style="width: 120px">
          <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮-->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="success" @click="handleImport">导入</el-button>
      <el-button type="warning" @click="handleExport">导出</el-button>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="userName" label="姓名" min-width="120" />
      <el-table-column prop="roleName" label="角色" min-width="120" />
      <el-table-column prop="department" label="部门" min-width="120" />
      <el-table-column prop="state" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === '启用' ? 'success' : 'danger'">{{ row.state }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 新增 / 修改 Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <!-- 隐藏输入框：存储 id -->
        <el-form-item prop="id">
          <el-input v-model="form.id" type="hidden" />
        </el-form-item>
        <el-form-item label="姓名" prop="userName" :rules="[{ required: true, message: '请输入姓名', trigger: 'blur' }]">
          <el-input v-model="form.userName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleName" :rules="[{ required: true, message: '请输入角色', trigger: 'blur' }]">
          <el-input v-model="form.roleName" placeholder="请输入角色" />
        </el-form-item>
        <el-form-item label="部门" prop="department" :rules="[{ required: true, message: '请选择部门', trigger: 'change' }]">
          <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
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
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入 Dialog -->
    <el-dialog v-model="importDialogVisible" title="导入用户数据" width="480px">
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="(uploadFile: any) => handleImportConfirm(uploadFile.raw)"
        :show-file-list="false"
        style="width: 100%"
      >
        <div style="padding: 20px 0">
          <el-icon :size="48" color="#c0c4cc"><UploadFilled /></el-icon>
          <div style="margin-top: 8px">拖拽文件到此处，或点击选择文件</div>
          <div style="font-size: 12px; color: #909399; margin-top: 4px">仅支持 .xlsx / .xls 格式</div>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false" :disabled="importLoading">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.data-page {
  padding: 20px;
  color: #333;
}

.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}

.query-form {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
