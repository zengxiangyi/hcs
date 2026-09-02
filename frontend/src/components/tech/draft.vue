<script setup lang="ts">
import { ref, computed, watch, shallowRef, onMounted, type Component } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createEmptyPlan, type PlanModel } from './plan/types'
import TZ01 from './plan/TZ01.vue'
import TZ02 from './plan/TZ02.vue'
import TZ03 from './plan/TZ03.vue'
import CH01 from './plan/CH01.vue'
import CH02 from './plan/CH02.vue'
import CH03 from './plan/CH03.vue'
import CH04 from './plan/CH04.vue'
import CH05 from './plan/CH05.vue'
import CH06 from './plan/CH06.vue'
import TH01 from './plan/TH01.vue'
import TH02 from './plan/TH02.vue'
import ZH01 from './plan/ZH01.vue'
import TP01 from './plan/TP01.vue'
import TP02 from './plan/TP02.vue'
import TP03 from './plan/TP03.vue'
import TP04 from './plan/TP04.vue'
import TP05 from './plan/TP05.vue'
import TP06 from './plan/TP06.vue'
import TP07 from './plan/TP07.vue'
import { techAPI, type TechBoardSaveDTO } from '../../api/tech'

defineOptions({ name: 'Draft' })

const route = useRoute()

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
  TZ02: TZ02,
  TZ03: TZ03,
  CH01: CH01,
  CH02: CH02,
  CH03: CH03,
  CH04: CH04,
  CH05: CH05,
  CH06: CH06,
  TH01: TH01,
  TH02: TH02,
  ZH01: ZH01,
  TP01: TP01,
  TP02: TP02,
  TP03: TP03,
  TP04: TP04,
  TP05: TP05,
  TP06: TP06,
  TP07: TP07,
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

/** 接口返回的字符串转为数字输入的取值，空值/非数字统一为 null */
function strToNum(v: string | undefined): number | null {
  if (v == null || v === '') return null
  const n = Number(v)
  return Number.isNaN(n) ? null : n
}

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
  isFirstCheck: '',testNum: null,coolTime: '',busbarNum: null,
  fallHead: '',quenching: '',attention: '',chamfer: '',
  lastHardness: '',firstHardness: '',hardnessDepth: ''
  }
  tempRows.value = [createTempRow()]
  planModel.value = createEmptyPlan()
  ElMessage.info('已取消')
}

/** 通过 code和edition 加载蓝本编辑信息，回填表单 */
async function loadByCode(code: string,edition: string) {
  try {
    const res = await techAPI.getByCode(code,edition)
    const data = res.data
    if (!data) {
      ElMessage.warning(`未找到蓝本：${code}`)
      return
    }
    basicForm.value = {
      code: data.code || '',
      name: data.name || '',
      graph: data.graph || '',
      firstLevel: data.firstLevel || '',
      secondLevel: data.secondLevel || '',
      materialName: data.materialName || '',
      materialCode: data.materialCode || '',
      weight: data.weight || '',
      model: data.model || '',
      specs: data.specs || '',
      customer: data.customer || '',
      remark: data.remark || '',
    }
    requirementForm.value = {
      isFirstCheck: data.isFirstCheck || '',
      testNum: strToNum(data.testNum),
      coolTime: data.coolTime || '',
      busbarNum: strToNum(data.busbarNum),
      fallHead: data.fallHead || '',
      quenching: data.quenching || '',
      attention: data.attention || '',
      chamfer: data.chamfer || '',
      lastHardness: data.lastHardness || '',
      firstHardness: data.firstHardness || '',
      hardnessDepth: data.hardnessDepth || '',
    }
  } catch (err) {
    ElMessage.error((err as Error).message || '加载蓝本失败')
  }
}

onMounted(() => {
  const code = route.query.code as string | undefined
  const edition = route.query.edition as string | undefined
  if (code && edition) {
    loadByCode(code,edition)
  }
})
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
  padding: 20px 24px;
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
  gap: 16px;
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
  gap: 8px;
  padding: 4px 5px;
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
  margin-bottom: 12px;
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
