<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dataAPI } from '../../api/data'
import type { UserRow } from '../../api/data'

// 查询条件
const query = ref({
  keyword: '',
  dept: '',
  status: '',
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
      keyword: query.value.keyword.trim(),
      dept: query.value.dept,
      status: query.value.status,
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
  query.value = { keyword: '', dept: '', status: '' }
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
  name: '',
  role: '',
  dept: '',
  status: '启用',
})

function handleEdit(row: UserRow) {
  dialogTitle.value = '编辑用户'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleDelete(row: UserRow) {
  try {
    const res = await dataAPI.deleteUser(row.id)
    ElMessage.success(res.msg || `已删除：${row.name}`)
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
  form.value = { id: 0, name: '', role: '', dept: '', status: '启用' }
  dialogVisible.value = true
}

async function handleSave() {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (form.value.id) {
        // 修改
        await dataAPI.updateUser(form.value.id, {
          name: form.value.name,
          role: form.value.role,
          dept: form.value.dept,
          status: form.value.status,
        })
        ElMessage.success('修改成功')
      } else {
        // 新增
        await dataAPI.addUser({
          name: form.value.name,
          role: form.value.role,
          dept: form.value.dept,
          status: form.value.status,
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

function handleImport() {
  ElMessage.info('导入')
}

function handleExport() {
  ElMessage.info('导出')
}

onMounted(fetchData)
</script>

<template>
  <div class="data-page">
    <h3 class="page-title">数据2</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="关键字">
        <el-input
          v-model="query.keyword"
          placeholder="姓名 / 角色 / ID"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.dept" placeholder="全部部门" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
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
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="role" label="角色" min-width="120" />
      <el-table-column prop="dept" label="部门" min-width="120" />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
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
        <el-form-item label="姓名" prop="name" :rules="[{ required: true, message: '请输入姓名', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role" :rules="[{ required: true, message: '请输入角色', trigger: 'blur' }]">
          <el-input v-model="form.role" placeholder="请输入角色" />
        </el-form-item>
        <el-form-item label="部门" prop="dept" :rules="[{ required: true, message: '请选择部门', trigger: 'change' }]">
          <el-select v-model="form.dept" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
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
