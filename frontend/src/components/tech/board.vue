<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blueprintAPI, type TechBoardSaveDTO } from '../../api/blueprint'
import { techStepAPI } from '../../api/techStep'
import { useRouter } from 'vue-router'

const router = useRouter()

defineOptions({ name: 'Board' })

/** 一级工艺 → 二级工艺 层级关系 */
interface CraftNode {
  value: string
  label: string
  children: { value: string; label: string }[]
}
const craftTree: CraftNode[] = [
  {
    value: 'CH',
    label: '淬火',
    children: [
      { value: 'CH01', label: '双屏' },
      { value: 'CH02', label: '卧式中频' }
    ],
  },
  {
    value: 'ZH',
    label: '正火',
    children: [
      { value: 'ZH01', label: '正火球化' }
    ]
  }
]

const craftTree1: CraftNode[] = [
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
      { value: 'TH02', label: '卧式中频感应辊颈退火' }
    ],
  },
  {
    value: 'ZH',
    label: '正火',
    children: [
      { value: 'ZH01', label: '正火球化' }
    ]
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
  }
]

/** 基本信息表单：9 个输入项 + 1 个文本域+一级工艺+二级工艺 */
const basicForm = ref({
  code: '',
  name: '',
  graph: '',
  firstLevel: '',
  secondLevel: '',
  materialName: '',
  materialCode: '',
  weight: '',
  model:'',
  specs:'',
  customer:'',
  remark: ''
})

/** 一级工艺下拉选项 */
const firstOptions = computed(() => craftTree.map((n) => ({ value: n.value, label: n.label })))

/** 二级工艺下拉选项：随一级工艺联动 */
const secondOptions = computed(() => {
  const node = craftTree.find((n) => n.value === basicForm.value.firstLevel)
  return node ? node.children.map((c) => ({ value: c.value, label: c.label })) : []
})

/** 一级工艺变更：清空二级工艺 */
watch(() => basicForm.value.firstLevel, () => {
  basicForm.value.secondLevel = ''
})

/** 是否首检下拉选项 */
const firstCheckOptions = [
  { value: '1', label: '是' },
  { value: '0', label: '否' },
]

/** 技术要求表单：11 个独立输入项（数量类为数字输入，未填写为 null） */
const requirementForm = ref({
  isFirstCheck: '',
  testNum: null as number | null,
  coolTime: '',
  busbarNum: null as number | null,
  fallHead: '',
  quenching: '',
  attention: '',
  chamfer: '',
  lastHardness: '',
  firstHardness: '',
  hardnessDepth: ''
})

/** 数字输入值转为提交用的字符串，未填写统一为空串 */
function numToStr(v: number | null): string {
  return v == null ? '' : String(v)
}

/** 后端返回的字符串型数值转为数字输入框的值，空值/非法值统一为 null */
function strToNum(v: string | undefined): number | null {
  if (v == null || v === '') return null
  const n = Number(v)
  return Number.isNaN(n) ? null : n
}

// ---------- 工艺编制：工序动态表格 ----------
/** 工序下拉数据（与 step.vue 保持一致）：step-工序编号，stepName-工序名称 */
const stepMap = [
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
  { step: 'S16', stepName: '合格判定' },
  { step: 'S17', stepName: '冷处理' },
  { step: 'S18', stepName: '二次回火' },
  { step: 'S19', stepName: '二次回火测变形' },
  { step: 'S20', stepName: '矫直' },
]

/** 是否必需下拉选项：Y-是 / N-否 */
const stepNeedOptions = [
  { value: 'Y', label: '必选' },
  { value: 'N', label: '可选' },
]

/** 编码 → 名称 映射：board 自有工艺树优先，其余回退到完整工艺树 */
const stepFirstLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree1.forEach((n) => (map[n.value] = n.label))
  craftTree.forEach((n) => (map[n.value] = n.label))
  return map
})
const stepSecondLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  craftTree1.forEach((n) => n.children.forEach((c) => (map[c.value] = c.label)))
  craftTree.forEach((n) => n.children.forEach((c) => (map[c.value] = c.label)))
  return map
})

/** 工艺编制行：一/二级工艺 + 工序 + 工序编号 + 排序 + 是否必需 + 备注 */
interface StepRow {
  /** 后端工序 id，新增行为 0 */
  id: number
  firstLevel: string
  secondLevel: string
  /** 工序名称（选中工序编号后自动带出） */
  step: string
  /** 工序编号，如 S01 */
  stepCode: string
  sort: number | null
  /** 是否必需：Y-是 / N-否 */
  isNeed: string
  remark: string
  /** 是否已暂存：暂存后该行单元格只读 */
  saved: boolean
}

const stepLoading = ref(false)
const stepRows = ref<StepRow[]>([])
/** 暂存区：点击行内「保存」后，把该行（引用）推入此处，随提交一起上传 */
const stepStagedRows = ref<StepRow[]>([])

/** 新增一行工序：继承当前一/二级工艺，排序顺延 */
function createStepRow(firstLevel: string, secondLevel: string): StepRow {
  return {
    id: 0,
    firstLevel,
    secondLevel,
    step: '',
    stepCode: '',
    sort: stepRows.value.length + 1,
    isNeed: 'Y',
    remark: '',
    saved: false,
  }
}

/** 按排序号正向（升序）重排工序行；排序号为空的行排在末尾 */
function sortStepRows(rows: StepRow[]): StepRow[] {
  return [...rows].sort(
    (a, b) => (a.sort ?? Number.MAX_SAFE_INTEGER) - (b.sort ?? Number.MAX_SAFE_INTEGER),
  )
}

/** 仅对内存中已有的工序行按排序号重排：不请求后端、不清空暂存区（行对象引用保持不变） */
function handleSortStep() {
  stepRows.value = sortStepRows(stepRows.value)
}

/** 初始化工序表格：按一/二级工艺加载工序模板并按排序号正向排序；无数据时给出一行空白供手工录入 */
async function initStepRows() {
  const { firstLevel, secondLevel } = basicForm.value
  if (!secondLevel) {
    stepRows.value = []
    stepStagedRows.value = []
    return
  }
  stepLoading.value = true
  try {
    const res = await techStepAPI.search({ firstLevel, secondLevel, page: 1, pageSize: 200 })
    const list = res.data.content ?? []
    // 重新初始化即清空暂存区
    stepStagedRows.value = []
    stepRows.value = list.length
      ? sortStepRows(
          list.map((r) => ({
            id: r.id,
            firstLevel: r.firstLevel || firstLevel,
            secondLevel: r.secondLevel || secondLevel,
            step: r.stepName ?? '',
            stepCode: r.step ?? '',
            sort: strToNum(r.sort),
            isNeed: r.isNeed || 'Y',
            remark: r.remark ?? '',
            saved: false,
          })),
        )
      : [createStepRow(firstLevel, secondLevel)]
  } catch (err) {
    ElMessage.error((err as Error).message || '工序加载失败')
    stepRows.value = [createStepRow(firstLevel, secondLevel)]
  } finally {
    stepLoading.value = false
  }
}

/** 二级工艺变更后重新初始化工序表格（一级变更会清空二级，同样会触发） */
watch(
  () => basicForm.value.secondLevel,
  () => {
    // 设定工艺编号和工艺名称
    basicForm.value.code = basicForm.value.firstLevel + basicForm.value.secondLevel
    basicForm.value.name = stepFirstLabelMap.value[basicForm.value.firstLevel] + stepSecondLabelMap.value[basicForm.value.secondLevel]
    initStepRows()
  },
  { immediate: true },
)

/** 选择工序编号后自动带出工序名称 */
function onStepCodeChange(row: StepRow) {
  const matched = stepMap.find((s) => s.step === row.stepCode)
  row.step = matched ? matched.stepName : ''
}

function handleAddStep() {
  stepRows.value = sortStepRows([
    ...stepRows.value,
    createStepRow(basicForm.value.firstLevel, basicForm.value.secondLevel),
  ])
}

/** 行内保存（暂存）：把当前行推入暂存区，并将该行置为只读 */
function handleSaveStep(row: StepRow) {
  if (row.saved) return
  if (!row.stepCode) {
    ElMessageBox.alert('排序:'+row.sort+' 请先选择工序')
    return
  }
  if (!stepStagedRows.value.includes(row)) {
    stepStagedRows.value.push(row)
  }
  row.saved = true
  ElMessage.success('已暂存')
}

/** 取消暂存：从暂存区移除该行，恢复可编辑 */
function handleEditStep(row: StepRow) {
  stepStagedRows.value = stepStagedRows.value.filter((r) => r !== row)
  row.saved = false
}

function handleDeleteStep(index: number) {
  const row = stepRows.value[index]
  if (row) {
    stepStagedRows.value = stepStagedRows.value.filter((r) => r !== row)
  }
  stepRows.value.splice(index, 1)
}

/** 保存：校验必填项后提交当前表单数据 */
async function onSave() {
  debugger;
  try {
    // 数据校验
    if(stepRows.value.length === 0){
      ElMessageBox.alert('请先添加工序')
      return
    }
    // 组装数据
    const bluePrint: TechBoardSaveDTO = {
      code: basicForm.value.code,
      name: basicForm.value.name,
      graph: basicForm.value.graph,
      firstLevel: basicForm.value.firstLevel,
      secondLevel: basicForm.value.secondLevel,
      materialName: basicForm.value.materialName,
      materialCode: basicForm.value.materialCode,
      weight: basicForm.value.weight,
      model: basicForm.value.model,
      specs: basicForm.value.specs,
      customer: basicForm.value.customer,
      remark: basicForm.value.remark,
      isFirstCheck: requirementForm.value.isFirstCheck,
      testNum: numToStr(requirementForm.value.testNum),
      coolTime: requirementForm.value.coolTime,
      busbarNum: numToStr(requirementForm.value.busbarNum),
      fallHead: requirementForm.value.fallHead,
      quenching: requirementForm.value.quenching,
      attention: requirementForm.value.attention,
      chamfer: requirementForm.value.chamfer,
      lastHardness: requirementForm.value.lastHardness,
      firstHardness: requirementForm.value.firstHardness,
      hardnessDepth: requirementForm.value.hardnessDepth
    }
    await blueprintAPI.save(bluePrint)
    // 保存工序
    await techStepAPI.batchSave(stepRows.value)
    ElMessage.success('保存成功')
    // 进入到工序编制页面
    router.push({ name: 'Solo', query: { code: basicForm.value.code } })
  } catch (err) {
    ElMessage.error((err as Error).message || '保存失败')
  }
}

/** 取消：重置表单数据 */
function onCancel() {
  basicForm.value = {
    code: '', name: '', graph: '', materialCode: '', materialName: '',
    weight: '',firstLevel: '', secondLevel: '',customer:'',model:'', 
    specs:'',remark: ''
  }
  requirementForm.value = {
  isFirstCheck: '',testNum: null,coolTime: '',busbarNum: null,
  fallHead: '',quenching: '',attention: '',chamfer: '',
  lastHardness: '',firstHardness: '',hardnessDepth: ''
  }
  stepRows.value = []
  stepStagedRows.value = []
  ElMessage.info('已取消')
}

async function onSubmit(){
  // 数据校验
  // 组装数据
  try {
    // 组装数据
    const bluePrint: TechBoardSaveDTO = {
      code: basicForm.value.code,
      name: basicForm.value.name,
      graph: basicForm.value.graph,
      firstLevel: basicForm.value.firstLevel,
      secondLevel: basicForm.value.secondLevel,
      materialName: basicForm.value.materialName,
      materialCode: basicForm.value.materialCode,
      weight: basicForm.value.weight,
      model: basicForm.value.model,
      specs: basicForm.value.specs,
      customer: basicForm.value.customer,
      remark: basicForm.value.remark,
      isFirstCheck: requirementForm.value.isFirstCheck,
      testNum: numToStr(requirementForm.value.testNum),
      coolTime: requirementForm.value.coolTime,
      busbarNum: numToStr(requirementForm.value.busbarNum),
      fallHead: requirementForm.value.fallHead,
      quenching: requirementForm.value.quenching,
      attention: requirementForm.value.attention,
      chamfer: requirementForm.value.chamfer,
      lastHardness: requirementForm.value.lastHardness,
      firstHardness: requirementForm.value.firstHardness,
      hardnessDepth: requirementForm.value.hardnessDepth
    }
    await blueprintAPI.submit(bluePrint)
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error((err as Error).message || '保存失败')
  }
}

</script>

<template>
  <div class="tech-board">

    <!-- 工艺选择区块 -->
    <section class="board-section">
      <h3 class="section-title">工艺选择</h3>
      <div class="tech-list">
        <div class="tech-item">
          <label class="tech-label">一级工艺</label>
          <el-select v-model="basicForm.firstLevel" placeholder="请选择一级工艺" clearable>
            <el-option
              v-for="opt in firstOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
        <div class="tech-item">
          <label class="tech-label">二级工艺</label>
          <el-select v-model="basicForm.secondLevel" placeholder="请选择二级工艺" clearable :disabled="!basicForm.firstLevel">
            <el-option
              v-for="opt in secondOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
      </div>
    </section>


    <!-- 基本信息区块 -->
    <section class="board-section">
      <h3 class="section-title">基本信息</h3>
      <div class="basic-form">
        <div class="basic-grid">
          <div class="basic-item">
            <label class="basic-label">工艺编号</label>
            <el-input v-model="basicForm.code" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">工艺名称</label>
            <el-input v-model="basicForm.name" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">图号</label>
            <el-input v-model="basicForm.graph" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">物料编码</label>
            <el-input v-model="basicForm.materialCode" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">客户名称</label>
            <el-input v-model="basicForm.customer" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">规格</label>
            <el-input v-model="basicForm.model" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">材质</label>
            <el-input v-model="basicForm.specs" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">单重</label>
            <el-input v-model="basicForm.weight" placeholder="请输入" clearable />
          </div>
        </div>
        <div class="basic-textarea">
          <label class="basic-label">工艺备注</label>
          <el-input
            v-model="basicForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
            resize="vertical"
          />
        </div>
      </div>
    </section>

    <!-- 技术要求区块 -->
    <section class="board-section">
      <h3 class="section-title">技术要求</h3>
      <div class="basic-form">
        <div class="basic-grid">
          <div class="basic-item">
            <label class="basic-label">是否首检</label>
            <el-select v-model="requirementForm.isFirstCheck" placeholder="请选择" clearable>
              <el-option
                v-for="opt in firstCheckOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </div>
          <div class="basic-item">
            <label class="basic-label">首检硬度要求</label>
            <el-input v-model="requirementForm.firstHardness" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">完工检硬度要求</label>
            <el-input v-model="requirementForm.lastHardness" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">母线数量</label>
            <el-input-number
              v-model="requirementForm.busbarNum"
              :min="0"
              :precision="0"
              controls-position="right"
              style="width: 100%"
              placeholder="请输入"
            />
          </div>
          <div class="basic-item">
            <label class="basic-label">测点数量</label>
            <el-input-number
              v-model="requirementForm.testNum"
              :min="0"
              :precision="0"
              controls-position="right"
              style="width: 100%"
              placeholder="请输入"
            />
          </div>
          <div class="basic-item">
            <label class="basic-label">冷却时间 (min)</label>
            <el-input v-model="requirementForm.coolTime" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">硬化层深度 (mm)</label>
            <el-input v-model="requirementForm.hardnessDepth" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">辊身倒角</label>
            <el-input v-model="requirementForm.chamfer" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">身颈落差</label>
            <el-input v-model="requirementForm.fallHead" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">淬火部位</label>
            <el-input v-model="requirementForm.quenching" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">注意事项</label>
            <el-input v-model="requirementForm.attention" placeholder="请输入" clearable />
          </div>
        </div>
      </div>
    </section>

    <!-- 工艺编制：按二级工艺动态初始化工序明细 -->
    <section class="board-section">
      <h3 class="section-title">工序配置</h3>
      <div class="temp-table__toolbar">
        <el-button type="primary" size="small" :disabled="!basicForm.secondLevel" @click="handleAddStep">
          增加工序
        </el-button>
        <el-button size="small" :disabled="!basicForm.secondLevel" @click="handleSortStep">
          排序
        </el-button>
      </div>
      <el-table v-loading="stepLoading" :data="stepRows" border stripe size="small" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="firstLevel" label="一级工艺" min-width="110">
          <template #default="{ row }">
            {{ stepFirstLabelMap[row.firstLevel] || row.firstLevel }}
          </template>
        </el-table-column>
        <el-table-column prop="secondLevel" label="二级工艺" min-width="150">
          <template #default="{ row }">
            {{ stepSecondLabelMap[row.secondLevel] || row.secondLevel }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="110">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="1"
              :precision="0"
              :disabled="row.saved"
              size="small"
              controls-position="right"
              style="width: 100%"
              placeholder="排序"
            />
          </template>
        </el-table-column>
        <el-table-column prop="step" label="工序" v-if="false" min-width="150">
          <template #default="{ row }">
            <el-input
              v-model="row.step"
              placeholder="选择工序编号后自动带出"
              size="small"
              clearable
              :disabled="row.saved"
            />
          </template>
        </el-table-column>
        <el-table-column prop="stepCode" label="工序" min-width="170">
          <template #default="{ row }">
            <el-select
              v-model="row.stepCode"
              placeholder="请选择工序编号"
              clearable
              filterable
              size="small"
              @change="onStepCodeChange(row as StepRow)"
            >
              <el-option
                v-for="opt in stepMap"
                :key="opt.step"
                :label="`${opt.step} - ${opt.stepName}`"
                :value="opt.step"
              />
            </el-select>
          </template>
        </el-table-column>
        <!-- 工序类型：主干|分支-->
         
        <el-table-column prop="isNeed" label="选择类型" width="110">
          <template #default="{ row }">
            <el-select v-model="row.isNeed" size="small" :disabled="row.saved">
              <el-option
                v-for="opt in stepNeedOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160">
          <template #default="{ row }">
            <el-input
              v-model="row.remark"
              placeholder="请输入备注"
              size="small"
              clearable
              :disabled="row.saved"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row, $index }">
            <el-button
              v-if="!row.saved"
              size="small"
              type="primary"
              @click="handleSaveStep(row as StepRow)"
            >
              保存
            </el-button>
            <el-button
              v-else
              size="small"
              type="primary"
              @click="handleEditStep(row as StepRow)"
            >
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteStep($index)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="step-empty">请先选择二级工艺，工序明细将自动初始化</span>
        </template>
      </el-table>
    </section>

  </div>
  <!-- 底部按钮-->
   <div class="bottom-btn">
    <el-button type="primary" @click="onSave">工艺编制</el-button>
  </div>
</template>

<style scoped>
/* 布局思路：
   - 父级 .layout-right 为滚动容器（overflow:auto），board 内容由它整体滚动
   - 底部按钮 .bottom-btn 使用 sticky 贴底，且覆盖父级 min-height:100%
     从根本上修复“底部区域被无限拉长”的问题 */
.tech-board {
  min-height: 0;
  color: var(--color-text-main);
  margin-left: 20px;
  margin-top: 20px;
  margin-right: 20px;
  padding-bottom: 16px; /* 为贴底按钮留出呼吸空间 */
  box-sizing: border-box;
}

/* 区块卡片：更精致圆角与阴影，hover 轻微浮起，提升表单整体质感 */
.board-section {
  margin-bottom: 18px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(31, 56, 88, 0.06);
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.board-section:hover {
  border-color: #d9ecff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
}

.board-section:last-child {
  margin-bottom: 0;
}

.section-title {
  position: relative;
  margin: 0 0 18px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-main);
  letter-spacing: 0.5px;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, #409eff, #66b1ff);
}

.basic-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 24px;
  row-gap: 16px;
}

/* 表单单元格：卡片化容器，输入更聚焦，hover 高亮 */
.basic-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  padding: 3px 3px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.basic-item:hover {
  border-color: #c6e2ff;
  background: #fff;
  box-shadow: 0 1px 6px rgba(64, 158, 255, 0.1);
}

.basic-label {
  flex: 0 0 110px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-main);
  white-space: nowrap;
  text-align: right;
}

.basic-label::after {
  content: '：';
  color: var(--color-text-aux);
}

.basic-item :deep(.el-input),
.basic-item :deep(.el-select),
.basic-item :deep(.el-input-number) {
  flex: 1 1 auto;
  width: auto;
}

.basic-item :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.basic-item :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dfe3ea inset;
  transition: box-shadow 0.2s ease;
}

.basic-item :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 6px rgba(64, 158, 255, 0.15);
}

.basic-textarea {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 8px;
}

.basic-textarea :deep(.el-textarea) {
  flex: 1 1 auto;
  width: auto;
}

.basic-textarea :deep(.el-textarea__inner) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dfe3ea inset;
  transition: box-shadow 0.2s ease;
}

.basic-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 6px rgba(64, 158, 255, 0.15);
}

/* 底部按钮区：
   - min-height:auto 覆盖父级 .layout-right > * 的 min-height:100%，解决无限拉长
   - sticky bottom 让按钮在内容滚动时始终贴底可见 */
.bottom-btn {
  min-height: auto;        /* 关键修复点 */
  height: auto;
  position: sticky;
  bottom: 0;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
  margin: 0 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  border-radius: 12px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.bottom-btn :deep(.el-button) {
  min-width: 96px;
  border-radius: 6px;
}

.temp-table__toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.step-empty {
  font-size: 13px;
  color: var(--color-text-aux);
}

.tech-list {
  display: flex;
  flex-direction: row;
  gap: 24px;
  flex-wrap: wrap;
}

/* 工艺选择项：与表单单元格一致的卡片化风格 */
.tech-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #fafbfd;
  border: 1px solid #eef1f6;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.tech-item:hover {
  border-color: #c6e2ff;
  background: #fff;
  box-shadow: 0 1px 6px rgba(64, 158, 255, 0.1);
}

.tech-label {
  flex: 0 0 72px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-main);
  white-space: nowrap;
  text-align: right;
}

.tech-label::after {
  content: '：';
  color: var(--color-text-aux);
}

.tech-item :deep(.el-select) {
  width: 200px;
}
</style>
