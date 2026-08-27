<script setup lang="ts">
import { ref, computed, watch, shallowRef, type Component } from 'vue'
import { ElMessage } from 'element-plus'
import { createEmptyPlan, type PlanModel } from './plan/types'
import TZ01 from './plan/TZ01.vue'
import { techAPI, type TechBoardSaveDTO } from '../../api/tech'

defineOptions({ name: 'Board' })

/** 一级工艺 → 二级工艺 层级关系 */
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

/** 二级工艺 → 编制方案组件 映射表（每种方案独立组件） */
const planComponentMap: Record<string, Component> = {
  TZ01: TZ01,
  // investment / die / free / die-forge / roll / ... 后续在此注册
}

/** 当前选中的编制方案组件（无则留空） */
const currentPlan = computed(() => planComponentMap[basicForm.value.secondLevel] ?? null)

/** 工艺编制数据：随二级工艺切换而重新初始化 */
const planModel = shallowRef<PlanModel>(createEmptyPlan())

/** 一级工艺变更：清空并重置二级工艺 */
watch(() => basicForm.value.firstLevel, () => {
  basicForm.value.secondLevel = ''
  planModel.value = createEmptyPlan()
})

/** 二级工艺变更：重置编制方案数据，避免脏数据残留 */
watch(() => basicForm.value.secondLevel, () => {
  planModel.value = createEmptyPlan()
})

/** 技术要求表单：11 个独立输入项 */
const requirementForm = ref({
  isFirstCheck: '',
  testNum: '',
  coolTime: '',
  busbarNum: '',
  fallHead: '',
  quenching: '',
  attention: '',
  chamfer: '',
  lastHardness: '',
  firstHardness: '',
  hardnessDepth: ''
})

/** 编制模板动态表格：段号 / 温度 / 时间 / 备注，每行可编辑 */
interface TempRow {
  segNo: string
  temp: string
  time: string
  remark: string
}
const tempRows = ref<TempRow[]>([createTempRow()])

function createTempRow(): TempRow {
  return { segNo: '', temp: '', time: '', remark: '' }
}

/** 保存：校验必填项后提交当前表单数据 */
async function onSave() {
  const payload = {
    basic: basicForm.value,
    requirement: requirementForm.value,
    template: tempRows.value,
    plan: planModel.value,
  }
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
      testNum: requirementForm.value.testNum,
      coolTime: requirementForm.value.coolTime,
      busbarNum: requirementForm.value.busbarNum,
      fallHead: requirementForm.value.fallHead,
      quenching: requirementForm.value.quenching,
      attention: requirementForm.value.attention,
      chamfer: requirementForm.value.chamfer,
      lastHardness: requirementForm.value.lastHardness,
      firstHardness: requirementForm.value.firstHardness,
      hardnessDepth: requirementForm.value.hardnessDepth
    }
    await techAPI.save(bluePrint)
    ElMessage.success('保存成功')
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
  isFirstCheck: '',testNum: '',coolTime: '',busbarNum: '',
  fallHead: '',quenching: '',attention: '',chamfer: '',
  lastHardness: '',firstHardness: '',hardnessDepth: ''
  }
  tempRows.value = [createTempRow()]
  planModel.value = createEmptyPlan()
  ElMessage.info('已取消')
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
            <label class="basic-label">蓝本工艺编号</label>
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
            <label class="basic-label">物料名称</label>
            <el-input v-model="basicForm.materialName" placeholder="请输入" clearable />
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
            <el-input v-model="requirementForm.isFirstCheck" placeholder="请输入" clearable />
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
            <el-input v-model="requirementForm.busbarNum" placeholder="请输入" clearable />
          </div>
          <div class="basic-item">
            <label class="basic-label">测点数量</label>
            <el-input v-model="requirementForm.testNum" placeholder="请输入" clearable />
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

    <!-- 工艺编制：按二级工艺动态切换独立方案组件 -->
    <section class="board-section">
      <h3 class="section-title">工艺编制</h3>
      <component :is="currentPlan" v-if="currentPlan" v-model="planModel" />
      <el-empty v-else description="请选择二级工艺以加载对应的编制方案" />
    </section>

  </div>
  <!-- 底部按钮-->
   <div class="bottom-btn">
    <el-button type="primary" @click="onSave">保存</el-button>
    <el-button @click="onCancel">取消</el-button>
  </div>
</template>

<style scoped>
.tech-board {
  padding: 16px;
  color: #303133;
  margin-left: 20px;
  margin-top:20px;
}

.board-section {
  margin-bottom: 24px;
}

.board-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.basic-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width:900px;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  column-gap: 24px;
  row-gap: 16px;
}

.basic-item {
  display: flex;
  flex-direction: row;
  align-items: left;
  gap: 8px;
}

.basic-label {
  flex: 0 0 120px;
  font-size: 14px;
  white-space: nowrap;
  text-align: right;
}

.basic-item :deep(.el-input) {
  width: 200px;
}

.basic-textarea {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 8px;
}

.basic-textarea :deep(.el-textarea) {
  width: 660px;
}

.bottom-btn {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
  margin-left: 20px;
  margin-bottom:40px;
}

.temp-table__toolbar {
  margin-bottom: 12px;
}

.tech-list {
  display: flex;
  flex-direction: row;
  gap: 24px;
}

.tech-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.tech-label {
  flex: 0 0 72px;
  font-size: 14px;
  white-space: nowrap;
  text-align: right;
}

.tech-item :deep(.el-select) {
  width: 200px;
}
</style>
