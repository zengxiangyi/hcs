<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'

defineOptions({ name: 'File' })

// 工艺分类选项（原型数据，后续接后端接口）
const categoryOptions = ['焊接', '切割', '冲压', '涂装', '装配', '检测']

// 文件行类型（对齐表格列）
interface FileRow {
  id: number
  fileName: string
  fileSize: number // 字节
  category: string
  uploader: string
  uploadTime: string
  // 原型用：用于预览
  url?: string
}

// 查询表单
interface QueryForm {
  fileName: string
  category: string
  startTime: string
  endTime: string
}

// 原型：本地模拟数据
const mockData: FileRow[] = [
  { id: 1, fileName: '焊接工艺规范_V1.docx', fileSize: 245760, category: '焊接', uploader: '张工', uploadTime: '2026-08-18 09:12' },
  { id: 2, fileName: '切割参数表.xlsx', fileSize: 153600, category: '切割', uploader: '李工', uploadTime: '2026-08-18 14:30' },
  { id: 3, fileName: '冲压模具图纸.pdf', fileSize: 1048576, category: '冲压', uploader: '王工', uploadTime: '2026-08-19 10:05' },
  { id: 4, fileName: '涂装工艺说明.pptx', fileSize: 524288, category: '涂装', uploader: '赵工', uploadTime: '2026-08-19 16:48' },
  { id: 5, fileName: '装配作业指导书.docx', fileSize: 307200, category: '装配', uploader: '孙工', uploadTime: '2026-08-20 08:22' },
  { id: 6, fileName: '质检标准手册.pdf', fileSize: 2097152, category: '检测', uploader: '周工', uploadTime: '2026-08-20 11:37' },
]

const query = ref<QueryForm>({
  fileName: '',
  category: '',
  startTime: '',
  endTime: '',
})

const tableData = ref<FileRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 格式化文件大小
function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

// 文件后缀对应的图标类型（原型用，可后续替换为真实图标）
function fileType(name: string): string {
  const ext = name.split('.').pop()?.toLowerCase() ?? ''
  if (['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(ext)) return '图片'
  if (['pdf'].includes(ext)) return 'PDF'
  if (['doc', 'docx'].includes(ext)) return '文档'
  if (['xls', 'xlsx'].includes(ext)) return '表格'
  if (['ppt', 'pptx'].includes(ext)) return '演示'
  return '文件'
}

// 原型：模拟拉取 + 过滤 + 分页
function fetchData() {
  loading.value = true
  try {
    const kw = query.value.fileName.trim().toLowerCase()
    let list = mockData.filter((r) => {
      const matchName = !kw || r.fileName.toLowerCase().includes(kw)
      const matchCat = !query.value.category || r.category === query.value.category
      const matchStart = !query.value.startTime || r.uploadTime >= query.value.startTime
      const matchEnd = !query.value.endTime || r.uploadTime <= query.value.endTime + ' 23:59'
      return matchName && matchCat && matchStart && matchEnd
    })
    total.value = list.length
    const start = (currentPage.value - 1) * pageSize.value
    tableData.value = list.slice(start, start + pageSize.value)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { fileName: '', category: '', startTime: '', endTime: '' }
  currentPage.value = 1
  fetchData()
}

function handlePageChange() {
  fetchData()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchData()
}

// 上传文件（原型：弹窗选择后加入本地列表）
const uploadVisible = ref(false)
const uploadFileList = ref<UploadUserFile[]>([])

function handleUpload() {
  uploadFileList.value = []
  uploadVisible.value = true
}

function handleUploadConfirm() {
  if (!uploadFileList.value.length) {
    ElMessage.warning('请选择至少一个文件')
    return
  }
  for (const f of uploadFileList.value) {
    mockData.unshift({
      id: Date.now() + Math.floor(Math.random() * 1000),
      fileName: f.name,
      fileSize: f.size ?? 0,
      category: query.value.category || '焊接',
      uploader: '当前用户',
      uploadTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
    })
  }
  ElMessage.success(`已上传 ${uploadFileList.value.length} 个文件`)
  uploadVisible.value = false
  fetchData()
}

// 预览
const previewVisible = ref(false)
const previewRow = ref<FileRow | null>(null)

function handlePreview(row: FileRow) {
  previewRow.value = row
  previewVisible.value = true
}

// 删除（带二次确认）
function handleDelete(row: FileRow) {
  ElMessageBox.confirm(`确认删除文件「${row.fileName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
    .then(() => {
      const idx = mockData.findIndex((r) => r.id === row.id)
      if (idx > -1) mockData.splice(idx, 1)
      ElMessage.success('已删除')
      if (tableData.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
      }
      fetchData()
    })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<template>
  <div class="file-page">
    <h3 class="page-title">文件上传管理</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="文件名称">
        <el-input
          v-model="query.fileName"
          placeholder="请输入文件名称"
          clearable
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="工艺分类">
        <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 140px">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="query.startTime"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DD HH:mm"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="query.endTime"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleUpload">上传文件</el-button>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="fileName" label="文件名称" min-width="220">
        <template #default="{ row }">
          <span class="file-name">
            <el-tag size="small" effect="plain" type="info">{{ fileType(row.fileName) }}</el-tag>
            {{ row.fileName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" min-width="120">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="category" label="工艺分类" min-width="110">
        <template #default="{ row }">
          <el-tag>{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploader" label="上传人" min-width="100" />
      <el-table-column prop="uploadTime" label="上传时间" min-width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <!-- 上传 Dialog -->
    <el-dialog v-model="uploadVisible" title="上传文件" width="480px">
      <el-upload
        v-model:file-list="uploadFileList"
        drag
        multiple
        :auto-upload="false"
        style="width: 100%"
      >
        <div style="padding: 20px 0">
          <el-icon :size="48" color="#c0c4cc"><UploadFilled /></el-icon>
          <div style="margin-top: 8px">拖拽文件到此处，或点击选择文件</div>
          <div style="font-size: 12px; color: #909399; margin-top: 4px">支持任意格式</div>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUploadConfirm">确定上传</el-button>
      </template>
    </el-dialog>

    <!-- 预览 Dialog -->
    <el-dialog v-model="previewVisible" :title="previewRow?.fileName || '预览'" width="720px">
      <div v-if="previewRow" class="preview-body">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文件名称">{{ previewRow.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatSize(previewRow.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="工艺分类">{{ previewRow.category }}</el-descriptions-item>
          <el-descriptions-item label="上传人">{{ previewRow.uploader }}</el-descriptions-item>
          <el-descriptions-item label="上传时间" :span="2">{{ previewRow.uploadTime }}</el-descriptions-item>
        </el-descriptions>
        <div class="preview-placeholder">
          <el-icon :size="64" color="#c0c4cc"><Document /></el-icon>
          <p>文件内容预览区（原型占位，接入后端后可展示图片 / PDF / 文档）</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.file-page {
  padding: 20px;
  color: #333;
}

.file-name {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.preview-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-placeholder {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  padding: 40px 0;
  text-align: center;
  color: #909399;
}
</style>
