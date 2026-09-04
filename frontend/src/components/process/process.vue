<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { stepFormMap } from './steps'
import type { StepForm, StepFormExpose } from './steps/shared'

defineOptions({ name: 'Process' })

// 16 个步骤节点
interface StepNode {
  key: string
  title: string
  desc: string
}

const steps: StepNode[] = [
  { key: 's01', title: '工艺制定', desc: '定义工序基础信息' },
  { key: 's02', title: '辊颈硬度检测', desc: '关联加工设备' },
  { key: 's03', title: '箱炉预热', desc: '配置工装与夹具' },
  { key: 's04', title: '机床淬火', desc: '设置刀具参数' },
  { key: 's05', title: '续冷', desc: '设定切削用量' },
  { key: 's06', title: '首检', desc: '编排工艺路线' },
  { key: 's07', title: '测变形', desc: '设置检验工序' },
  { key: 's08', title: '暂焖', desc: '定义首检规则' },
  { key: 's09', title: '冷处理', desc: '配置冷却参数' },
  { key: 's10', title: '一次回火(辊身回火）', desc: '设定淬火规范' },
  { key: 's11', title: '测变形', desc: '热处理参数' },
  { key: 's12', title: '矫直', desc: '表面处理要求' },
  { key: 's13', title: '除应力', desc: '装配工艺说明' },
  { key: 's14', title: '包装规范', desc: '包装与标识' },
  { key: 's15', title: '硬度叫检', desc: '安全与注意项' },
  { key: 's16', title: '检硬度', desc: '复核并发布' }
]

const activeIndex = ref(0)
const activeStep = computed(() => steps[activeIndex.value])

// 每个步骤各自持有一份表单数据，由父级持有引用，切换步骤时不丢失已填内容
const formData = reactive<Record<string, StepForm>>(
  Object.fromEntries(steps.map((s) => [s.key, {}]))
)

// 当前步骤对应的表单组件
const currentForm = computed(() => stepFormMap[activeStep.value.key])

// 动态组件的模板引用，统一收口为 StepFormExpose 后使用
const stepRef = ref<any>(null)
function currentStepForm(): StepFormExpose | null {
  return (stepRef.value ?? null) as StepFormExpose | null
}

function selectStep(index: number) {
  activeIndex.value = index
}

function prev() {
  if (activeIndex.value > 0) activeIndex.value -= 1
}

function next() {
  if (activeIndex.value < steps.length - 1) activeIndex.value += 1
}

async function saveCurrentStep() {
  const ok = await currentStepForm()?.validate()
  if (!ok) {
    ElMessage.warning(`「${activeStep.value.title}」必填项未填写完整`)
    return
  }
  // TODO: 对接后端保存接口
  ElMessage.success(`「${activeStep.value.title}」保存成功`)
  console.log('step data', activeStep.value.key, formData[activeStep.value.key])
}

function resetCurrentStep() {
  currentStepForm()?.reset()
}
</script>

<template>
  <div class="process-flow">
    <h3 class="page-title">工序流程</h3>

    <!-- 顶部水平页签 -->
    <div class="step-tabs">
      <div
        v-for="(step, idx) in steps"
        :key="step.key"
        class="step-tab"
        :class="{ active: idx === activeIndex, done: idx < activeIndex }"
        @click="selectStep(idx)"
      >
        <span class="step-no">{{ String(idx + 1).padStart(2, '0') }}</span>
        <span class="step-text">
          <span class="step-title">{{ step.title }}</span>
          <span class="step-desc">{{ step.desc }}</span>
        </span>
      </div>
    </div>

    <!-- 下方编辑窗口 -->
    <section class="editor-panel">
      <div class="editor-header">
        <div>
          <span class="editor-tag">步骤 {{ activeIndex + 1 }}/{{ steps.length }}</span>
          <h4 class="editor-title">{{ activeStep.title }}</h4>
        </div>
        <span class="editor-desc">{{ activeStep.desc }}</span>
      </div>

      <!-- 按 activeIndex 切换，只渲染当前步骤的表单 -->
      <div class="editor-form">
        <component
          :is="currentForm"
          :key="activeStep.key"
          ref="stepRef"
          v-model="formData[activeStep.key]"
        />
      </div>

      <div class="editor-footer">
        <el-button :disabled="activeIndex === 0" @click="prev">上一步</el-button>
        <el-button @click="resetCurrentStep">重置本步</el-button>
        <el-button type="primary" @click="saveCurrentStep">保存当前步骤</el-button>
        <el-button
          type="primary"
          :disabled="activeIndex === steps.length - 1"
          @click="next"
        >
          下一步
        </el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.process-flow {
  padding: 20px;
  color: #333;
}

/* 顶部水平页签 */
.step-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  padding: 12px;
  margin-bottom: 16px;
}

.step-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  border: 1px solid transparent;
  border-radius: 6px;
  transition: all 0.2s;
  background: #fff;
}

.step-tab:hover {
  background: #f0f7ff;
}

.step-tab.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.step-tab.done .step-no {
  background: #67c23a;
  color: #fff;
}

.step-no {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border-radius: 50%;
  background: #dcdfe6;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-tab.active .step-no {
  background: #409eff;
}

.step-text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.step-title {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
}

.step-desc {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 下方编辑窗口 */
.editor-panel {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px;
  background: #fff;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.editor-tag {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.editor-title {
  margin: 6px 0 0;
  font-size: 16px;
  color: #303133;
}

.editor-desc {
  font-size: 13px;
  color: #909399;
}

.editor-form {
  min-height: 220px;
}

.editor-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
</style>
