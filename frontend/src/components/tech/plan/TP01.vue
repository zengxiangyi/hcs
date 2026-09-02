<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEmptyPlan, type PlanModel } from './types'

defineOptions({ name: 'TP01' })

/** 双向绑定父组件的方案数据 */
const model = defineModel<PlanModel>({ required: true })

/** 确保结构初始化，避免父组件传入空对象时报错 */
if (!model.value) model.value = createEmptyPlan()

type TZ01Row = PlanModel['rows'][number]

/** 编制明细表的行结构：段号 / 温度 / 时间 / 备注 */
function createRow(): TZ01Row {
  return { segNo: '', temp: '', time: '', remark: '' }
}

/**
 * 直接引用响应式数组。用 reactive 包裹以保证新增/删除行后 el-table 能正确刷新。
 * 若父组件传入的是 reactive 数组，reactive() 会直接复用同一引用，不会破坏双向绑定。
 */
const rows = reactive(model.value.rows)
const fields=reactive(model.value.fields)

/** 首次挂载时若没有行，补一行 */
if (rows.length === 0) rows.push(createRow())

/** 在指定行的下方插入一行 */
function insertRow(index: number) {
  rows.splice(index + 1, 0, createRow())
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
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  rows.splice(index, 1)
}
</script>

<template>
  <div class="tz01-plan">
    <!-- 编制明细表：段号 / 温度 / 时间 / 备注 -->
    <div class="plan-table">
      <el-table :data="rows" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="段号">
          <template #default="{ row }"><el-input v-model="row.segNo" placeholder="如：1" /></template>
        </el-table-column>
        <el-table-column label="温度">
          <template #default="{ row }"><el-input v-model="row.temp" placeholder="请输入" /></template>
        </el-table-column>
        <el-table-column label="时间">
          <template #default="{ row }"><el-input v-model="row.time" placeholder="请输入" /></template>
        </el-table-column>
        <el-table-column label="备注">
          <template #default="{ row }"><el-input v-model="row.remark" placeholder="请输入" /></template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ $index }">
            <el-button type="primary" @click="insertRow($index)">插入</el-button>
            <el-button type="danger" @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="note-row">
      <label class="basic-label">注意事项</label>
      <el-input
        v-model="fields.remark"
        type="textarea"
        :rows="1"
        placeholder="请输入备注信息"
        resize="vertical"
      />
    </div>
  </div>
</template>

<style scoped>
.plan-table__toolbar {
  margin-bottom: 12px;
}

/* 注意事项：label 与输入框同一行（父组件的 basic-label 为 scoped，此处需本地定义） */
.note-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
}

.note-row .basic-label {
  flex: 0 0 110px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-main);
  line-height: 32px;
  white-space: nowrap;
  text-align: right;
}

.note-row .basic-label::after {
  content: '：';
  color: var(--color-text-aux);
}

.note-row :deep(.el-textarea) {
  flex: 1 1 auto;
  width: auto;
}

.note-row :deep(.el-textarea__inner) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dfe3ea inset;
  transition: box-shadow 0.2s ease;
}

.note-row :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 6px rgba(64, 158, 255, 0.15);
}
</style>
