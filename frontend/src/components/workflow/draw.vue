<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { flowNodeAPI, flowEdgeAPI, type FlowNode, type FlowEdge } from '../../api/flow'

defineOptions({ name: 'Draw' })

const router = useRouter()
function goBack() {
  router.back()
}

const route = useRoute()

/** URL 参数 flowGraph（流程图编码，标识当前流程图），由 flow.vue 跳转传入 */
const flowGraph = computed<string>(() => (route.query.flowGraph as string) || '')

/* ============================ 数据模型 ============================ */
/** 节点分类选项 */
const categoryOptions = [
  { value: 'start', label: '开始' },
  { value: 'task', label: '任务' },
  { value: 'decision', label: '判定' },
  { value: 'end', label: '结束' },
]

/** 节点形状选项 */
const shapeOptions = [
  { value: 'rect', label: '矩形' },
  { value: 'round', label: '圆角矩形' },
  { value: 'diamond', label: '菱形' },
  { value: 'circle', label: '圆形' },
]

/** 连线分类选项 */
const edgeCategoryOptions = [
  { value: 'normal', label: '普通' },
  { value: 'condition', label: '条件分支' },
  { value: 'default', label: '默认' },
]

/** 表格主键自增计数（仅用于前端未保存行的临时 id） */
let nodeSeq = 1
let edgeSeq = 1

/** 节点行工厂 */
function createNode(): FlowNode {
  return {
    id: `N${Date.now()}_${nodeSeq++}`,
    flowGraph: flowGraph.value,
    code: '',
    name: '',
    category: 'task',
    shape: 'rect',
    axis: '0,0',
    color: '#409EFF',
    operator: '',
    roleList: '',
    userList: '',
  }
}

/** 连线行工厂 */
function createEdge(): FlowEdge {
  return {
    id: `E${Date.now()}_${edgeSeq++}`,
    flowGraph: flowGraph.value,
    code: '',
    name: '',
    category: 'normal',
    fromNode: '',
    toNode: '',
    cond: '',
    remark: '',
    color: '#67C23A',
    axis: '',
  }
}

const nodes = ref<FlowNode[]>([])
const edges = ref<FlowEdge[]>([])

/* ============================ 只读展示辅助 ============================ */
/** 选项 code -> 名称 */
function optionLabel(options: { value: string; label: string }[], value: string): string {
  return options.find((o) => o.value === value)?.label || value || '-'
}

/** 节点 id -> 显示名（名称优先，其次编号） */
function nodeLabel(id: string): string {
  const n = nodes.value.find((x) => x.id === id)
  if (!n) return id || '-'
  return n.name || n.code || n.id
}

/* ============================ 节点弹窗（表格只读，唯一编辑入口） ============================ */
const nodeDialogVisible = ref(false)
const nodeDialogTitle = ref('新增节点')
const nodeFormRef = ref()
const nodeForm = ref<FlowNode>(createNode())
/** 正在编辑的行下标，null 表示新增 */
let editingNodeIndex: number | null = null

const nodeRules = {
  code: [{ required: true, message: '请输入节点编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
}

function openNodeAdd() {
  editingNodeIndex = null
  nodeDialogTitle.value = '新增节点'
  nodeForm.value = createNode()
  nodeDialogVisible.value = true
}

function openNodeEdit(index: number) {
  editingNodeIndex = index
  nodeDialogTitle.value = '编辑节点'
  nodeForm.value = { ...nodes.value[index] }
  nodeDialogVisible.value = true
}

function submitNode() {
  if (!nodeFormRef.value) return
  nodeFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    if (editingNodeIndex === null) {
      nodes.value.push({ ...nodeForm.value })
    } else {
      nodes.value[editingNodeIndex] = { ...nodeForm.value }
    }
    nodeDialogVisible.value = false
  })
}

function removeNode(index: number) {
  const n = nodes.value[index]
  nodes.value.splice(index, 1)
  // 同步清理引用该节点的连线
  edges.value = edges.value.filter((e) => e.fromNode !== n.id && e.toNode !== n.id)
}

/* ============================ 连线弹窗 ============================ */
const edgeDialogVisible = ref(false)
const edgeDialogTitle = ref('新增连线')
const edgeFormRef = ref()
const edgeForm = ref<FlowEdge>(createEdge())
/** 正在编辑的行下标，null 表示新增 */
let editingEdgeIndex: number | null = null

const edgeRules = {
  code: [{ required: true, message: '请输入连线代码', trigger: 'blur' }],
  fromNode: [{ required: true, message: '请选择开始节点', trigger: 'change' }],
  toNode: [{ required: true, message: '请选择目标节点', trigger: 'change' }],
}

function openEdgeAdd() {
  editingEdgeIndex = null
  edgeDialogTitle.value = '新增连线'
  edgeForm.value = createEdge()
  edgeDialogVisible.value = true
}

function openEdgeEdit(index: number) {
  editingEdgeIndex = index
  edgeDialogTitle.value = '编辑连线'
  edgeForm.value = { ...edges.value[index] }
  edgeDialogVisible.value = true
}

function submitEdge() {
  if (!edgeFormRef.value) return
  edgeFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    if (editingEdgeIndex === null) {
      edges.value.push({ ...edgeForm.value })
    } else {
      edges.value[editingEdgeIndex] = { ...edgeForm.value }
    }
    edgeDialogVisible.value = false
  })
}

function removeEdge(index: number) {
  edges.value.splice(index, 1)
}

/* ============================ 加载 ============================ */
async function load() {
  if (!flowGraph.value) return
  try {
    const [nodeRes, edgeRes] = await Promise.all([
      flowNodeAPI.list(flowGraph.value),
      flowEdgeAPI.list(flowGraph.value),
    ])
    nodes.value = nodeRes.data || []
    edges.value = edgeRes.data || []
    // 用已加载数据的 id 重新校准自增基数
    const maxNode = nodes.value.reduce((m, n) => Math.max(m, extractSeq(n.id)), 0)
    const maxEdge = edges.value.reduce((m, e) => Math.max(m, extractSeq(e.id)), 0)
    nodeSeq = Math.max(nodeSeq, maxNode + 1)
    edgeSeq = Math.max(edgeSeq, maxEdge + 1)
  } catch (err) {
    ElMessage.error((err as Error).message || '加载失败')
  }
}

function extractSeq(id: string): number {
  const m = /_(\d+)$/.exec(id)
  return m ? Number(m[1]) : 0
}

/* 监听 flowGraph 变化重新加载 */
watch(flowGraph, () => {
  nodes.value = []
  edges.value = []
  nodeSeq = 1
  edgeSeq = 1
  load()
})

onMounted(() => {
  if (flowGraph.value) load()
})
</script>

<template>
  <div class="flow-design">
    <div class="flow-head">
      <h3 class="flow-title">流程图设计</h3>
      <span class="flow-tag" v-if="flowGraph">flowGraph：{{ flowGraph }}</span>
      <span class="flow-tag flow-tag--warn" v-else>未传入 flowGraph 参数</span>
      <span class="flow-back" @click="goBack">返回</span>
    </div>

    <!-- 节点定义（只读，编辑走弹窗） -->
    <div class="flow-section">
      <div class="flow-section__head">
        <span class="flow-section__title">节点定义</span>
        <span class="tip">共 {{ nodes.length }} 个节点（表格只读，新增/编辑请点击操作按钮）</span>
        <el-button type="primary" size="small" @click="openNodeAdd">新增节点</el-button>
      </div>

      <el-table :data="nodes" border size="small" class="flow-table">
        <el-table-column type="index" label="#" width="48" />
        <el-table-column prop="id" label="id" width="150" show-overflow-tooltip />
        <el-table-column prop="flowGraph" label="flowGraph" width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="节点编号" width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="节点名称" width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ optionLabel(categoryOptions, row.category) }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人分类" width="120" show-overflow-tooltip />
        <el-table-column prop="roleList" label="角色" width="140" show-overflow-tooltip />
        <el-table-column prop="userList" label="执行人" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ $index }">
            <el-button type="primary" link size="small" @click="openNodeEdit($index)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeNode($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 连线定义（只读，编辑走弹窗） -->
    <div class="flow-section">
      <div class="flow-section__head">
        <span class="flow-section__title">连线定义</span>
        <span class="tip">共 {{ edges.length }} 条连线（表格只读，新增/编辑请点击操作按钮）</span>
        <el-button type="primary" size="small" @click="openEdgeAdd">新增连线</el-button>
      </div>

      <el-table :data="edges" border size="small" class="flow-table">
        <el-table-column type="index" label="序号" width="48" />
        <el-table-column prop="id" label="id" width="150" show-overflow-tooltip />
        <el-table-column prop="flowGraph" label="流程图编号" width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="代码" width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" width="140" show-overflow-tooltip />
        <el-table-column label="category" width="100">
          <template #default="{ row }">{{ optionLabel(edgeCategoryOptions, row.category) }}</template>
        </el-table-column>
        <el-table-column label="开始节点" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ nodeLabel(row.fromNode) }}</template>
        </el-table-column>
        <el-table-column label="目标节点" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ nodeLabel(row.toNode) }}</template>
        </el-table-column>
        <el-table-column prop="cond" label="条件" width="120" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" width="140" show-overflow-tooltip />
        <el-table-column label="颜色" width="80">
          <template #default="{ row }">
            <span class="color-dot" :style="{ background: row.color }" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ $index }">
            <el-button type="primary" link size="small" @click="openEdgeEdit($index)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeEdge($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 节点新增/编辑弹窗 -->
    <el-dialog v-model="nodeDialogVisible" :title="nodeDialogTitle" width="520px" append-to-body>
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeRules" label-width="100px">
        <el-form-item label="节点编号" prop="code">
          <el-input v-model="nodeForm.code" placeholder="请输入节点编号" clearable />
        </el-form-item>
        <el-form-item label="节点名称" prop="name">
          <el-input v-model="nodeForm.name" placeholder="请输入节点名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="nodeForm.category" style="width: 100%">
            <el-option v-for="o in categoryOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="形状">
          <el-select v-model="nodeForm.shape" style="width: 100%">
            <el-option v-for="o in shapeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="坐标">
          <el-input v-model="nodeForm.axis" placeholder="格式：x,y" clearable />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="nodeForm.color" />
        </el-form-item>
        <el-form-item label="操作人分类">
          <el-input v-model="nodeForm.operator" placeholder="请输入操作人分类" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="nodeForm.roleList" placeholder="多个角色用逗号分隔" clearable />
        </el-form-item>
        <el-form-item label="执行人">
          <el-input v-model="nodeForm.userList" placeholder="多个执行人用逗号分隔" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNode">确定</el-button>
      </template>
    </el-dialog>

    <!-- 连线新增/编辑弹窗 -->
    <el-dialog v-model="edgeDialogVisible" :title="edgeDialogTitle" width="520px" append-to-body>
      <el-form ref="edgeFormRef" :model="edgeForm" :rules="edgeRules" label-width="100px">
        <el-form-item label="代码" prop="code">
          <el-input v-model="edgeForm.code" placeholder="请输入连线代码" clearable />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="edgeForm.name" placeholder="请输入连线名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="edgeForm.category" style="width: 100%">
            <el-option v-for="o in edgeCategoryOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始节点" prop="fromNode">
          <el-select v-model="edgeForm.fromNode" filterable placeholder="请选择源节点" style="width: 100%">
            <el-option v-for="n in nodes" :key="n.id" :label="`${n.name || n.code || n.id}`" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点" prop="toNode">
          <el-select v-model="edgeForm.toNode" filterable placeholder="请选择目标节点" style="width: 100%">
            <el-option v-for="n in nodes" :key="n.id" :label="`${n.name || n.code || n.id}`" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件">
          <el-input v-model="edgeForm.cond" placeholder="条件表达式，可留空" clearable />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="edgeForm.remark" clearable />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="edgeForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="edgeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.flow-design {
  margin: 20px;
  color: var(--color-text-main);
}

.flow-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.flow-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.flow-tag {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 3px 10px;
  border-radius: 4px;
}

.flow-tag--warn {
  color: #e6a23c;
  background: #fdf6ec;
}

.flow-back {
  margin-left: auto;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
}
.flow-back:hover {
  text-decoration: underline;
}

.flow-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px 20px;
  box-shadow: 0 2px 8px rgba(31, 56, 88, 0.06);
  margin-bottom: 16px;
}

.flow-section:last-of-type {
  margin-bottom: 0;
}

.flow-section__head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.flow-section__title {
  font-size: 14px;
  font-weight: 600;
}

.tip {
  font-size: 12px;
  color: #909399;
}

.flow-table :deep(.el-table__cell) {
  padding: 4px 0;
}

.color-dot {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  vertical-align: middle;
}
</style>
