<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'TechBoard' })

/** 当前选中的 Tab，默认指向第一个 Tab（基本信息） */
const activeTab = ref('basic')

/** 18 个工艺按钮：标题与参数各不相同，后续按需补全每条数据 */
interface TechItem {
  title: string
  code: string
  disabled?: boolean
  remark?: string
}
const techList = ref<TechItem[]>([
  { title: '油冷调制', code: 'GY-01' },
  { title: '正火调制', code: 'GY-02' },
  { title: '喷雾调制', code: 'GY-03' },
  { title: '工频感应淬火', code: 'GY-04' },
  { title: '双频感应淬火', code: 'GY-05' },
  { title: '立式中频感应淬火', code: 'GY-06' },
  { title: '整体感应淬火', code: 'GY-07' },
  { title: '森辊整体淬火', code: 'GY-08' },
  { title: '卧式中频感应辊身淬火', code: 'GY-09' },
  { title: '卧式中频感应辊颈淬火', code: 'GY-10' },
  { title: '去应力退火', code: 'GY-11' },
  { title: '卧式中频感应辊颈退火', code: 'GY-12' },
  { title: '正火球化', code: 'GY-13' },
  { title: '高温回火', code: 'GY-14' },
  { title: '预热交织', code: 'GY-15' },
  { title: '冷处理', code: 'GY-16' },
  { title: '调制正火', code: 'GY-17' },
  { title: '工频感应正火', code: 'GY-18' },
])

/** 当前选中的工艺 code，确保每次只有一个按钮处于选中态 */
const selectedCode = ref<string>('')

/** 基本信息表单：8 个输入项 + 1 个文本域 */
const basicForm = ref({
  field1: '',
  field2: '',
  field3: '',
  field4: '',
  field5: '',
  field6: '',
  field7: '',
  field8: '',
  remark: '',
})

/** 技术要求表单：12 个独立输入项 */
const requirementForm = ref({
  rfield1: '',
  rfield2: '',
  rfield3: '',
  rfield4: '',
  rfield5: '',
  rfield6: '',
  rfield7: '',
  rfield8: '',
  rfield9: '',
  rfield10: '',
  rfield11: '',
  rfield12: '',
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

/** 新增一行 */
function addTempRow() {
  tempRows.value.push(createTempRow())
}

/** 删除指定行 */
function removeTempRow(index: number) {
  if (tempRows.value.length <= 1) {
    ElMessage.warning('至少保留一行')
    return
  }
  tempRows.value.splice(index, 1)
}

/** 点击工艺按钮：切换选中态并执行业务逻辑 */
function onTechClick(item: TechItem) {
  selectedCode.value = item.code
  // TODO: 按 item.code 等业务参数处理
}

/** 保存：校验必填项后提交当前表单数据 */
function onSave() {
  if (!selectedCode.value) {
    ElMessage.warning('请先选择一项工艺')
    return
  }
  const payload = {
    code: selectedCode.value,
    basic: basicForm.value,
    requirement: requirementForm.value,
    template: tempRows.value,
  }
  // TODO: 调用后端保存接口，例如 await saveTechBoard(payload)
  console.log('保存工艺看板：', payload)
  ElMessage.success('保存成功')
}

/** 取消：重置选中态与表单数据 */
function onCancel() {
  selectedCode.value = ''
  basicForm.value = {
    field1: '', field2: '', field3: '', field4: '',
    field5: '', field6: '', field7: '', field8: '',
    remark: '',
  }
  requirementForm.value = {
    rfield1: '', rfield2: '', rfield3: '', rfield4: '',
    rfield5: '', rfield6: '', rfield7: '', rfield8: '',
    rfield9: '', rfield10: '', rfield11: '', rfield12: '',
  }
  tempRows.value = [createTempRow()]
  ElMessage.info('已取消')
}
</script>

<template>
  <!-- 18个工艺按钮：每行6个，行间距30px，宽120px高40px，圆角矩形边框 -->
  <div class="tech-grid">
    <button
      v-for="item in techList"
      :key="item.code"
      type="button"
      class="tech-btn"
      :class="{ 'tech-btn--primary': selectedCode === item.code }"
      :disabled="item.disabled"
      @click="onTechClick(item)"
    >
      {{ item.title }}
    </button>
  </div>

  <div class="tech-board">
    <el-tabs v-model="activeTab" class="board-tabs">
      <el-tab-pane label="基本信息" name="basic">
        <div class="tab-body">
          <!-- 基本信息内容区 -->
          <div class="basic-form">
            <div class="basic-grid">
              <div class="basic-item">
                <label class="basic-label">蓝本工艺编号</label>
                <el-input v-model="basicForm.field1" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">工艺类型</label>
                <el-input v-model="basicForm.field2" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">图号</label>
                <el-input v-model="basicForm.field3" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">物料名称</label>
                <el-input v-model="basicForm.field4" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">客户名称</label>
                <el-input v-model="basicForm.field5" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">规格</label>
                <el-input v-model="basicForm.field6" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">材质</label>
                <el-input v-model="basicForm.field7" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">单重</label>
                <el-input v-model="basicForm.field8" placeholder="请输入" clearable />
              </div>
            </div>
            <div class="basic-textarea">
              <label class="basic-label">工艺备注</label>
              <el-input
                v-model="basicForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
                resize="vertical"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="技术要求" name="requirement">
        <div class="tab-body">
          <!-- 技术要求内容区 -->
          <div class="basic-form">
            <div class="basic-grid">
              <div class="basic-item">
                <label class="basic-label">是否首检</label>
                <el-input v-model="requirementForm.rfield1" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">首检硬度要求</label>
                <el-input v-model="requirementForm.rfield2" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">完工检硬度要求</label>
                <el-input v-model="requirementForm.rfield3" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">母线数量</label>
                <el-input v-model="requirementForm.rfield4" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">测点数量</label>
                <el-input v-model="requirementForm.rfield5" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">冷却时间 (min)</label>
                <el-input v-model="requirementForm.rfield6" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">硬化层深度 (mm)</label>
                <el-input v-model="requirementForm.rfield7" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">辊身倒角</label>
                <el-input v-model="requirementForm.rfield8" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">身颈落差</label>
                <el-input v-model="requirementForm.rfield9" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">淬火部位</label>
                <el-input v-model="requirementForm.rfield10" placeholder="请输入" clearable />
              </div>
              <div class="basic-item">
                <label class="basic-label">注意事项</label>
                <el-input v-model="requirementForm.rfield11" placeholder="请输入" clearable />
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="温度参数" name="template">
        <div class="tab-body">
          <!-- 编制模板内容区 -->
          <div class="temp-table">
            <div class="temp-table__toolbar">
              <el-button type="primary" @click="addTempRow">增加行</el-button>
            </div>
            <el-table :data="tempRows" border style="width: 100%">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="段号" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.segNo" placeholder="请输入段号" />
                </template>
              </el-table-column>
              <el-table-column label="温度" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.temp" placeholder="请输入温度" />
                </template>
              </el-table-column>
              <el-table-column label="时间" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.time" placeholder="请输入时间" />
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="200">
                <template #default="{ row }">
                  <el-input v-model="row.remark" placeholder="请输入备注" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link @click="removeTempRow($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <!-- 输入温度 -->
        </div>
      </el-tab-pane>
      <el-tab-pane label="淬火" name="other">
        <div class="tab-body">
          <!-- 其他内容区 -->
          <slot name="other">其他</slot>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
  <!-- 底部按钮-->
   <div class="bottom-btn">
    <el-button type="primary" @click="onSave">保存</el-button>
    <el-button @click="onCancel">取消</el-button>
  </div>
</template>

<style scoped>
.tech-grid {
  display: grid;
  grid-template-columns: repeat(6, 150px);
  row-gap: 20px;
  column-gap: 16px;
  margin-bottom: 16px;
  margin-left: 20px;
  margin-top: 20px;
}

.tech-btn {
  width: 150px;
  height: 40px;
  padding: 0;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background-color: #fff;
  color: #303133;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.tech-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.tech-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  border-color: #ebeef5;
  color: #c0c4cc;
}

.tech-btn--primary {
  border-color: #409eff;
  color: #409eff;
  background: #ebeef5;
}

.tech-board {
  padding: 16px;
  color: #303133;
  margin-left: 20px;
  margin-top:20px;
}

.board-tabs {
  width: 100%;
}

.tab-body {
  padding: 16px 4px;
  min-height: 240px;
}

.basic-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width:900px;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
</style>
