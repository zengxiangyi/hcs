<script setup lang="ts">
import { ref, computed, watch, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { techStepAPI, type TechStepRow, type TechStepSaveParams } from '../../api/techStep'

defineOptions({ name: 'Step' })

/** 一级工艺 → 二级工艺 层级关系（与 board.vue 保持一致） */
interface CraftNode {
  value: string
  label: string
  children: { value: string; label: string }[]
}
const craftTree: CraftNode[] = [
  {
    value: 'TZ',
    label: '调质',
    children: [
      { value: 'TZ01', label: '油冷调质' },
      { value: 'TZ02', label: '喷雾调质' },
      { value: 'TZ03', label: '正火调质' },
    ],
  },
  {
    value: 'CH',
    label: '辊身淬火',
    children: [
      { value: 'CH01', label: '工频感应淬火' },
      { value: 'CH02', label: '立式中频感应淬火' },
      { value: 'CH03', label: '双频感应淬火' },
      { value: 'CH04', label: '整体感应淬火' },
      { value: 'CH05', label: '森辊整体淬火' },
      { value: 'CH06', label: '卧式中频感应辊身淬火' },
    ],
  },
  {
    value: 'TH',
    label: '退火',
    children: [
      { value: 'TH01', label: '去应力退火' },
      { value: 'TH02', label: '卧式中频感应辊颈退火' },
    ],
  },
  {
    value: 'ZH',
    label: '正火',
    children: [{ value: 'ZH01', label: '正火球化' }],
  },
  {
    value: 'TP',
    label: '临时',
    children: [
      { value: 'TP01', label: '高温回火' },
      { value: 'TP02', label: '预热矫直' },
      { value: 'TP03', label: '回火' },
      { value: 'TP04', label: '冷处理' },
      { value: 'TP05', label: '调质正火' },
      { value: 'TP06', label: '工频感应正火' },
      { value: 'TP07', label: '整体感应正火' },
    ],
  },
]

/** 编码 → 中文 映射，用于表格回显 */
const firstLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree.forEach((n) => (map[n.value] = n.label))
  return map
})
const secondLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree.forEach((n) => n.children.forEach((c) => (map[c.value] = c.label)))
  return map
})

/** 是否必需下拉选项：Y-是 / N-否 */
const isNeedOptions = [
  { value: 'Y', label: '是' },
  { value: 'N', label: '否' },
]

const stepMap=[
  { step: 'S01', stepName: '辊颈硬度检测' },
  { step: 'S02', stepName: '箱炉预热' },
  { step: 'S03', stepName: '机床淬火' },
  { step: 'S04', stepName: '续冷' },
  { step: 'S05', stepName: '首检' },
  { step: 'S06', stepName: '测变形' },
  { step: 'S07', stepName: '暂焖' },
  { step: 'S08', stepName: '冷处理' },
  { step: 'S09', stepName: '淬颈' },
  { step: 'S10', stepName: '一次回火(辊身回火）' },
  { step: 'S11', stepName: '测变形' },
  { step: 'S12', stepName: '矫直' },
  { step: 'S13', stepName: '除应力' },
  { step: 'S14', stepName: '硬度叫检' },
  { step: 'S15', stepName: '检硬度' },
  { step: 'S16', stepName: '合格' },
  { step: 'S17', stepName: '冷处理' },
  { step: 'S18', stepName: '二次回火' },
  { step: 'S19', stepName: '二次回火测变形' },
  { step: 'S20', stepName: '矫直' }
]

// ---------- 查询区 ----------
const query = ref({ firstLevel: '', secondLevel: '' })

/** 查询区：一级工艺下拉选项 */
const queryFirstOptions = computed(() => craftTree.map((n) => ({ value: n.value, label: n.label })))

/** 查询区：二级工艺下拉选项（随一级工艺联动） */
const querySecondOptions = computed(() => {
  const node = craftTree.find((n) => n.value === query.value.firstLevel)
  return node ? node.children.map((c) => ({ value: c.value, label: c.label })) : []
})

/** 一级工艺变更：清空二级工艺（参考 board.vue 的联动逻辑） */
watch(
  () => query.value.firstLevel,
  () => {
    query.value.secondLevel = ''
  },
)

// ---------- 表格 + 分页 ----------
const loading = ref(false)
const tableData = ref<TechStepRow[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await techStepAPI.search({
      firstLevel: query.value.firstLevel || undefined,
      secondLevel: query.value.secondLevel || undefined,
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    tableData.value = res.data.content ?? []
    total.value = res.data.total ?? 0
  } catch (err) {
    ElMessage.error((err as Error).message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  query.value = { firstLevel: '', secondLevel: '' }
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

// ---------- 新增 / 修改对话框 ----------
/** 表单结构：id 为隐藏的唯一标识，id = 0 新增，id > 0 修改 */
interface TechStepForm {
  id: number
  firstLevel: string
  secondLevel: string
  step: string
  stepName: string
  sort: string
  isNeed: string
  remark: string
}

function createEmptyForm(): TechStepForm {
  return { id: 0, firstLevel: '', secondLevel: '', step: '', stepName: '', sort: '', isNeed: 'Y', remark: '' }
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = ref<TechStepForm>(createEmptyForm())

const rules = reactive<FormRules<TechStepForm>>({
  firstLevel: [{ required: true, message: '请选择一级工艺', trigger: 'change' }],
  secondLevel: [{ required: true, message: '请选择二级工艺', trigger: 'change' }],
  step: [{ required: true, message: '请选择工序编号', trigger: 'change' }],
  stepName: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  isNeed: [{ required: true, message: '请选择是否必需', trigger: 'change' }],
})

/** 弹窗内：一级工艺下拉选项 */
const formFirstOptions = computed(() => craftTree.map((n) => ({ value: n.value, label: n.label })))

/** 弹窗内：二级工艺下拉选项（随一级工艺联动） */
const formSecondOptions = computed(() => {
  const node = craftTree.find((n) => n.value === form.value.firstLevel)
  return node ? node.children.map((c) => ({ value: c.value, label: c.label })) : []
})

function resetForm() {
  form.value = createEmptyForm()
  formRef.value?.clearValidate()
}

function handleAdd() {
  dialogTitle.value = '新增工序'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: TechStepRow) {
  dialogTitle.value = '修改工序'
  // 整体赋值，避免 watch 在编辑回填时误清空二级工艺
  form.value = {
    id: row.id,
    firstLevel: row.firstLevel ?? '',
    secondLevel: row.secondLevel ?? '',
    step: row.step ?? '',
    stepName: row.stepName ?? '',
    sort: row.sort ?? '',
    isNeed: row.isNeed ?? 'N',
    remark: row.remark ?? '',
  }
  dialogVisible.value = true
}

/** 弹窗内一级工艺变更：清空二级工艺 */
function onFormFirstLevelChange() {
  form.value.secondLevel = ''
}

/** 选择工序编号后自动带出工序名称 */
function onStepChange(val: string) {
  const matched = stepMap.find((s) => s.step === val)
  form.value.stepName = matched ? matched.stepName : ''
}

async function handleDelete(row: TechStepRow) {
  try {
    await ElMessageBox.confirm(`确认删除工序「${row.stepName || row.step}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await techStepAPI.remove(row.id)
    ElMessage.success('删除成功')
    // 删除当前页最后一条时回退一页
    if (tableData.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }
    fetchData()
  } catch (err) {
    ElMessage.error((err as Error).message || '删除失败')
  }
}

function handleSave() {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    const payload: TechStepSaveParams = { ...form.value }
    try {
      if (form.value.id > 0) {
        await techStepAPI.update(payload)
        ElMessage.success('修改成功')
      } else {
        await techStepAPI.add(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (err) {
      ElMessage.error((err as Error).message || '保存失败')
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="tech-step-page">
    <h3 class="page-title">工艺工序管理</h3>

    <!-- 查询区 -->
    <el-form :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="一级工艺">
        <el-select v-model="query.firstLevel" placeholder="请选择一级工艺" clearable>
          <el-option
            v-for="opt in queryFirstOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="二级工艺">
        <el-select
          v-model="query.secondLevel"
          placeholder="请选择二级工艺"
          clearable
          :disabled="!query.firstLevel"
        >
          <el-option
            v-for="opt in querySecondOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">增加</el-button>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" v-if="false" width="80" />
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="firstLevel" label="一级工艺" min-width="120">
        <template #default="{ row }">
          {{ firstLabelMap[row.firstLevel] || row.firstLevel }}
        </template>
      </el-table-column>
      <el-table-column prop="secondLevel" label="二级工艺" min-width="160">
        <template #default="{ row }">
          {{ secondLabelMap[row.secondLevel] || row.secondLevel }}
        </template>
      </el-table-column>
      <el-table-column prop="step" label="工序编号" min-width="120" />
      <el-table-column prop="stepName" label="工序名称" min-width="160" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="isNeed" label="是否必需" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isNeed === 'Y' ? 'success' : 'info'">
            {{ row.isNeed === 'Y' ? '是' : row.isNeed === 'N' ? '否' : row.isNeed }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row as TechStepRow)">修改</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row as TechStepRow)">删除</el-button>
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

    <!-- 新增 / 修改对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @closed="resetForm()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- id 为隐藏的唯一标识：0 表示新增，> 0 表示修改 -->
        <el-form-item prop="id" v-show="false">
          <el-input v-model="form.id" />
        </el-form-item>
        <el-form-item label="一级工艺" prop="firstLevel">
          <el-select
            v-model="form.firstLevel"
            placeholder="请选择一级工艺"
            clearable
            style="width: 100%"
            @change="onFormFirstLevelChange"
          >
            <el-option
              v-for="opt in formFirstOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="二级工艺" prop="secondLevel">
          <el-select
            v-model="form.secondLevel"
            placeholder="请选择二级工艺"
            clearable
            :disabled="!form.firstLevel"
            style="width: 100%"
          >
            <el-option
              v-for="opt in formSecondOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工序" prop="step">
          <el-select
            v-model="form.step"
            placeholder="请选择工序"
            clearable
            filterable
            style="width: 100%"
            @change="onStepChange"
          >
            <el-option
              v-for="opt in stepMap"
              :key="opt.step"
              :label="opt.stepName"
              :value="opt.step"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工序名称" v-if="false" prop="stepName">
          <el-input v-model="form.stepName" readonly placeholder="请输入工序名称" clearable />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input v-model="form.sort" placeholder="请输入排序号" clearable />
        </el-form-item>
        <el-form-item label="是否必需" prop="isNeed">
          <el-select v-model="form.isNeed" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="opt in isNeedOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
/* 页面级公共样式（.page-title / .query-form / .toolbar / .pagination）
   已抽离至 src/style/common.css，此处仅保留页面私有样式。 */
.tech-step-page {
  padding: 20px;
  color: #333;
  min-height: 100%;
}

.query-form :deep(.el-select) {
  width: 200px;
}
</style>
