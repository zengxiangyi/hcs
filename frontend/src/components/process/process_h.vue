<script setup lang="ts">
import { ref, computed } from 'vue'

defineOptions({ name: 'ProcessFlowHorizontal' })

// 16 个步骤节点
interface StepNode {
  key: string
  title: string
  desc: string
}

const steps: StepNode[] = [
  { key: 's01', title: '工序定义', desc: '定义工序基础信息' },
  { key: 's02', title: '设备配置', desc: '关联加工设备' },
  { key: 's03', title: '工装夹具', desc: '配置工装与夹具' },
  { key: 's04', title: '刀具参数', desc: '设置刀具参数' },
  { key: 's05', title: '切削参数', desc: '设定切削用量' },
  { key: 's06', title: '工艺路线', desc: '编排工艺路线' },
  { key: 's07', title: '质检节点', desc: '设置检验工序' },
  { key: 's08', title: '首检要求', desc: '定义首检规则' },
  { key: 's09', title: '冷却工艺', desc: '配置冷却参数' },
  { key: 's10', title: '淬火工艺', desc: '设定淬火规范' },
  { key: 's11', title: '热处理', desc: '热处理参数' },
  { key: 's12', title: '表面处理', desc: '表面处理要求' },
  { key: 's13', title: '装配要求', desc: '装配工艺说明' },
  { key: 's14', title: '包装规范', desc: '包装与标识' },
  { key: 's15', title: '注意事项', desc: '安全与注意项' },
  { key: 's16', title: '审核发布', desc: '复核并发布' },
]

const activeIndex = ref(0)
const activeStep = computed(() => steps[activeIndex.value])

function selectStep(index: number) {
  activeIndex.value = index
}

function prev() {
  if (activeIndex.value > 0) activeIndex.value -= 1
}

function next() {
  if (activeIndex.value < steps.length - 1) activeIndex.value += 1
}
</script>

<template>
  <div class="process-flow">
    <h3 class="page-title">工艺执行流程（水平切换）</h3>

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

      <!-- 占位编辑表单：演示切换效果 -->
      <div class="editor-form">
        <el-form label-width="100px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="activeStep.title + '编号'">
                <el-input :placeholder="`请输入${activeStep.title}编号`" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="activeStep.title + '名称'">
                <el-input :placeholder="`请输入${activeStep.title}名称`" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="说明">
            <el-input type="textarea" :rows="4" :placeholder="`请填写${activeStep.title}相关说明`" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary">保存当前步骤</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="editor-footer">
        <el-button :disabled="activeIndex === 0" @click="prev">上一步</el-button>
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
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
</style>
