<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { StepForm } from './shared'

defineOptions({ name: 'StepS13' })

const form = defineModel<StepForm>({ required: true })
const formRef = ref()

const rules = {
  furnace: [{ required: true, message: '请输入炉号', trigger: 'blur' }],
  temp: [{ required: true, message: '请输入除应力温度', trigger: 'blur' }],
  hold: [{ required: true, message: '请输入保温时间', trigger: 'blur' }]
}

/** 明细行结构：段数 / 温度 / 时间 */
interface SegRow {
  segNo: string
  temp: string
  time: string
  /** 最近一次保存的数据快照，用于判断该行是否有未保存改动 */
  snapshot: string
}

/** 表格默认初始化行数 */
const DEFAULT_ROW_COUNT = 10

function createRow(segNo: string): SegRow {
  return { segNo, temp: '', time: '', snapshot: '' }
}

/** 默认 10 行，段数依次为 1段 ~ 10段 */
function createDefaultRows(): SegRow[] {
  return Array.from({ length: DEFAULT_ROW_COUNT }, (_, i) => createRow(`${i + 1}段`))
}

if (!Array.isArray(form.value.segments) || form.value.segments.length === 0) {
  form.value.segments = createDefaultRows()
}
/** 直接复用响应式数组，保证增删行后 el-table 正确刷新且同步回父级表单 */
const rows = reactive<SegRow[]>(form.value.segments as SegRow[])
form.value.segments = rows

/** 取参与比对 / 保存的字段 */
function pick(row: SegRow) {
  return { segNo: row.segNo, temp: row.temp, time: row.time }
}

/** 该行是否已保存且无改动 */
function isSaved(row: SegRow) {
  return row.snapshot !== '' && row.snapshot === JSON.stringify(pick(row))
}

/** 在指定行下方增加一行 */
function addRow(index: number) {
  rows.splice(index + 1, 0, createRow(`${rows.length + 1}段`))
}

/** 保存指定行（校验必填后记录数据快照） */
function saveRow(index: number) {
  const row = rows[index]
  if (!String(row.segNo ?? '').trim()) {
    ElMessage.warning('请输入段数')
    return
  }
  if (!String(row.temp ?? '').trim() || !String(row.time ?? '').trim()) {
    ElMessage.warning('请填写温度与时间后再保存')
    return
  }
  row.snapshot = JSON.stringify(pick(row))
  ElMessage.success(`第 ${index + 1} 行已保存`)
}

/** 删除指定行（仅剩一行时不允许删除） */
async function removeRow(index: number) {
  if (rows.length <= 1) {
    ElMessage.warning('至少保留一行')
    return
  }
  try {
    await ElMessageBox.confirm('确定删除该行吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  rows.splice(index, 1)
}

async function validate() {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

function reset() {
  formRef.value?.resetFields()
  rows.splice(0, rows.length, ...createDefaultRows())
}

defineExpose({ validate, reset })
</script>

<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <!-- 动态表格：段数 / 温度 / 时间，行支持增加、保存、删除 -->
    <el-table :data="rows" border>
      <el-table-column type="index" v-if="false" label="#" width="60" />
      <el-table-column label="段数" width="100">
        <template #default="{ row }">
          <el-text class="mx-1">{{ row.segNo }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="温度" width="200">
        <template #default="{ row }">
          <el-input v-model="row.temp" placeholder="请输入温度" />
        </template>
      </el-table-column>
      <el-table-column label="时间" width="200">
        <template #default="{ row }">
          <el-input v-model="row.time" placeholder="请输入时间" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="200">
        <template #default="{ row }">
          <el-tag :type="isSaved(row as SegRow) ? 'success' : 'info'" disable-transitions>
            {{ isSaved(row as SegRow) ? '已保存' : '未保存' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ $index }">
          <el-button type="primary" link @click="addRow($index)">增加</el-button>
          <el-button type="success" link @click="saveRow($index)">保存</el-button>
          <el-button type="danger" link @click="removeRow($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>
</template>
