<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { flowNodeAPI, flowEdgeAPI, type FlowNode, type FlowEdge } from '../../api/flow'

defineOptions({ name: 'Draw' })

const route = useRoute()
const router = useRouter()

function goBack() {
  router.back()
}

/** URL 参数 flowGraph（流程图编码，标识当前流程图），由 flow.vue 跳转传入 */
const flowGraph = computed<string>(() => (route.query.flowGraph as string) || '')

/* ============================ 数据模型 ============================ */
/** 节点分类选项 */
const categoryOptions = [
  { value: 'S', label: '开始' },
  { value: 'T', label: '任务' },
  { value: 'D', label: '判定' },
  { value: 'E', label: '结束' },
]

const operatorOptions = [
  { value: 'R', label: '角色' },
  { value: 'U', label: '用户' },
  { value: 'C', label: '角色或用户' },
  { value: 'N', label: '角色和用户' }
]

/** 节点形状选项 */
const shapeOptions = [
  { value: 'RECT', label: '矩形' },
  { value: 'CIRCLE', label: '圆形' },
]

/** 连线分类选项 */
const edgeCategoryOptions = [
  { value: 'A', label: '排他' },
  { value: 'B', label: '并行' },
  { value: 'C', label: '包容' },
]


/** 节点行工厂 */
function createNode(): FlowNode {
  return {
    id: -1,
    flowGraph: flowGraph.value,
    code: '',
    name: '',
    category: 'task',
    shape: 'rect',
    X: "0",
    Y: "0",
    W: "100",
    H: "50",
    color: '#409EFF',
    operator: '',
    roleList: '',
    userList: '',
  }
}

/** 连线行工厂 */
function createEdge(): FlowEdge {
  return {
    id: -1,
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

/** code -> 节点映射，避免表格每行渲染都做一次 O(n) 线性查找 */
const nodeMapByCode = computed(() => {
  const map = new Map<string, FlowNode>()
  nodes.value.forEach((n) => {
    if (n.code) map.set(n.code, n)
  })
  return map
})

/** 节点 id -> 展示名（表格只读展示用，兼容行内存储 code 的历史数据） */
function nodeLabel(value: string): string {
  if (!value) return '-'
  const match = nodeMapByCode.value.get(value)
  if (!match) return value
  return match.name ? `${match.name}（${match.code}）` : match.code
}

/* ============================ 节点弹窗（表格只读，唯一编辑入口） ============================ */
const nodeDialogVisible = ref(false)
const nodeDialogTitle = ref('新增节点')
const nodeFormRef = ref()
/** 用工厂函数初始化，保证表单对象始终存在，模板中 v-model 不会出现“可能为未定义” */
const nodeForm = ref<FlowNode>(createNode())
/** 正在编辑的行下标，null 表示新增 */
let editingNodeIndex: number | null = null

const nodeRules = {
  code: [{ required: true, message: '请输入节点编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
}

function openNodeEdit(index: number) {
  const row = nodes.value[index]
  if (!row) return
  editingNodeIndex = index
  nodeDialogTitle.value = '编辑节点'
  nodeForm.value = { ...row }
  nodeDialogVisible.value = true
}

function submitNode() {
  if (!nodeFormRef.value || !nodeForm.value) return
  const form = { ...nodeForm.value }
  nodeFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    const idx = editingNodeIndex
    // 下标合法时覆盖原行，否则按新增处理，避免越界下标把数组撑大产生空洞
    if (idx !== null && idx >= 0 && idx < nodes.value.length) {
      nodes.value[idx] = form
    } else {
      nodes.value.push(form)
    }
    nodeDialogVisible.value = false
    renderCanvas()
  })
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

function openEdgeEdit(index: number) {
  const row = edges.value[index]
  if (!row) return
  editingEdgeIndex = index
  edgeDialogTitle.value = '编辑连线'
  edgeForm.value = { ...row }
  edgeDialogVisible.value = true
}

function submitEdge() {
  if (!edgeFormRef.value || !edgeForm.value) return
  const form = { ...edgeForm.value }
  edgeFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    const idx = editingEdgeIndex
    if (idx !== null && idx >= 0 && idx < edges.value.length) {
      edges.value[idx] = form
    } else {
      edges.value.push(form)
    }
    edgeDialogVisible.value = false
    renderCanvas()
  })
}

/* ============================ 加载 ============================ */
async function load() {
  nodes.value = []
  edges.value = []
  if (!flowGraph.value) return
  try {
    const [nodeRes, edgeRes] = await Promise.all([
      flowNodeAPI.list(flowGraph.value),
      flowEdgeAPI.list(flowGraph.value),
    ])
    nodes.value = nodeRes.data || []
    edges.value = edgeRes.data || []
    // 渲染canvas
    renderCanvas()
  } catch (err) {
    ElMessage.error((err as Error).message || '加载失败')
  }
}

/** 将 FlowNode 的坐标/尺寸字符串解析为数字，非法时返回默认值 */
function parseNum(value: string | undefined, fallback: number): number {
  const n = parseFloat(value || '')
  return Number.isFinite(n) ? n : fallback
}

/** 将 FlowNode 的 X/Y 字符串（"x,y"）解析为数字坐标，非法时用默认值 */
function parsePoint(value: string | undefined, fallback: number): number {
  const n = parseFloat(value || '')
  return Number.isFinite(n) ? n : fallback
}

/**
 * 依据 nodes.value / edges.value 数据绘制流程图。
 * 节点通过 code 互连，坐标取自 X/Y，尺寸取自 W/H，形状取自 shape。
 */
async function renderCanvas() {
  const canvas = document.getElementById('flow-canvas') as HTMLCanvasElement
  if (!canvas) return

  const ratio = window.devicePixelRatio || 1
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 依据数据自动计算画布尺寸
  const PAD = 40
  const DEFAULT_W = 120
  const DEFAULT_H = 60
  const positions = nodes.value.map((n) => ({
    x: parsePoint(n.X, 50),
    y: parsePoint(n.Y, 50),
    w: parseNum(n.W, DEFAULT_W),
    h: parseNum(n.H, DEFAULT_H),
  }))

  let WIDTH = 0
  let HEIGHT = 0
  positions.forEach((p) => {
    WIDTH = Math.max(WIDTH, p.x + p.w)
    HEIGHT = Math.max(HEIGHT, p.y + p.h)
  })
  WIDTH += PAD
  HEIGHT += PAD
  WIDTH = Math.max(WIDTH, 200)
  HEIGHT = Math.max(HEIGHT, 200)

  // 画布初始化与高清适配
  canvas.width = WIDTH * ratio
  canvas.height = HEIGHT * ratio
  canvas.style.width = WIDTH + 'px'
  canvas.style.height = HEIGHT + 'px'

  ctx.setTransform(ratio, 0, 0, ratio, 0, 0)
  ctx.clearRect(0, 0, WIDTH, HEIGHT)
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  // 先绘制节点
  nodes.value.forEach((node) => renderNode(ctx, node, DEFAULT_W, DEFAULT_H))

    // 再绘制连线
  edges.value.forEach((edge) => renderEdge(ctx, nodes.value, edge, DEFAULT_W, DEFAULT_H))
}

/** 根据形状绘制节点背景（矩形/圆角矩形/菱形/圆形） */
function traceNodePath(ctx: CanvasRenderingContext2D, node: FlowNode, x: number, y: number, w: number, h: number) {
  const cx = x + w / 2
  const cy = y + h / 2
  ctx.beginPath()
  switch (node.shape) {
    case 'round':
      ctx.roundRect(x, y, w, h, Math.min(12, h / 2))
      break
    case 'diamond':
      ctx.moveTo(cx, y)
      ctx.lineTo(x + w, cy)
      ctx.lineTo(cx, y + h)
      ctx.lineTo(x, cy)
      ctx.closePath()
      break
    case 'CIRCLE':
      ctx.arc(cx,cy, w/2, 0, Math.PI * 2)
      break
    default:
      ctx.roundRect(x, y, w, h, 8)
  }
}

/** 绘制节点（支持透明度参数） */
function renderNode(ctx: CanvasRenderingContext2D, node: FlowNode, DEFAULT_W: number, DEFAULT_H: number, alpha = 1.0) {
  const x = parsePoint(node.X, 50)
  const y = parsePoint(node.Y, 50)
  const w = parseNum(node.W, DEFAULT_W)
  const h = parseNum(node.H, DEFAULT_H)
  const text = node.name || node.code || '-'
  const color = node.color || '#409EFF'

  ctx.save()
  ctx.globalAlpha = alpha

  // 绘制节点背景
  traceNodePath(ctx, node, x, y, w, h)
  ctx.fillStyle = color
  ctx.shadowColor = 'rgba(0,0,0,0.2)'
  ctx.shadowBlur = 8
  ctx.shadowOffsetY = 4
  ctx.fill()
  ctx.shadowColor = 'transparent'

  // 节点描边
  ctx.strokeStyle = 'rgba(255,255,255,0.6)'
  ctx.lineWidth = 2
  ctx.stroke()

  // 绘制文本
  ctx.fillStyle = '#fff'
  ctx.font = '14px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, x + w / 2, y + h / 2, w - 8)
  ctx.restore()
}


type AxisPoint = { x: number; y: number }

/** 将 edge.axis（坐标点对象数组 [{x,y},...]）过滤为有效坐标点数组，无法解析时返回空数组 */
function parseAxis(axis: Array<{ x: number; y: number }>| undefined): AxisPoint[] {
  if (!Array.isArray(axis)) return []
  return axis.filter((p) => p && Number.isFinite(p.x) && Number.isFinite(p.y))
}

/** 将 edge.axis（JSON 字符串）解析为有效坐标点数组，非法 JSON 或缺失时返回空数组 */
function parseAxisString(axis: string | undefined): AxisPoint[] {
  if (!axis) return []
  try {
    return parseAxis(JSON.parse(axis))
  } catch {
    // axis 来自后端/用户录入，可能是脏数据：解析失败退化为直线，不能中断整张图的渲染
    return []
  }
}

/** 在给定端点坐标上绘制箭头 */
function drawArrow(ctx: CanvasRenderingContext2D, fromX: number, fromY: number, toX: number, toY: number, color: string) {
  const angle = Math.atan2(toY - fromY, toX - fromX)
  const arrowLen = 12
  ctx.beginPath()
  ctx.moveTo(toX, toY)
  ctx.lineTo(toX - arrowLen * Math.cos(angle - Math.PI / 6), toY - arrowLen * Math.sin(angle - Math.PI / 6))
  ctx.lineTo(toX - arrowLen * Math.cos(angle + Math.PI / 6), toY - arrowLen * Math.sin(angle + Math.PI / 6))
  ctx.closePath()
  ctx.fillStyle = color
  ctx.fill()
}

/** 绘制连线（fromNode/toNode 存的是节点 code） */
function renderEdge(
  ctx: CanvasRenderingContext2D,
  nodes: FlowNode[],
  edge: FlowEdge,
  DEFAULT_W: number,
  DEFAULT_H: number,
) {
  const fromNode = nodes.find((n) => n.code === edge.fromNode)
  const toNode = nodes.find((n) => n.code === edge.toNode)
  if (!fromNode || !toNode) return

  const fromW = parseNum(fromNode.W, DEFAULT_W)
  const fromH = parseNum(fromNode.H, DEFAULT_H)
  const toH = parseNum(toNode.H, DEFAULT_H)

  // 起点取 from 节点右边缘中点，终点取 to 节点左边缘中点
  const startX = parsePoint(fromNode.X, 50) + fromW
  const startY = parsePoint(fromNode.Y, 50) + fromH / 2
  const endX = parsePoint(toNode.X, 50)
  const endY = parsePoint(toNode.Y, 50) + toH / 2

  const color = edge.color || '#67C23A'
  // 使用 axis 折线坐标（若有），否则绘制直线
  const points = parseAxisString(edge.axis)
  // 绘制连线路径
  ctx.beginPath()
  if (points.length > 0) {
    ctx.moveTo(points[0].x, points[0].y)
    points.forEach((point) => ctx.lineTo(point.x, point.y))
  } else {
    ctx.moveTo(startX, startY)
    ctx.lineTo(endX, endY)
  }
  ctx.strokeStyle = color
  ctx.lineWidth = 2
  ctx.stroke()

  // 绘制箭头：折线至少需要 2 个点才能确定方向，否则退化为节点直连箭头（避免 points[-1] 越界）
  if (points.length >= 2) {
    const len = points.length
    const bef = points[len - 2]
    const last = points[len - 1]
    drawArrow(ctx, bef.x, bef.y, last.x, last.y, color)
  } else {
    drawArrow(ctx, startX, startY, endX, endY, color)
  }

  // 绘制连线标签
    let labelX = (startX + endX) / 2
    let labelY = (startY + endY) / 2
    if (points.length > 0) {
      const mid = points[Math.floor(points.length / 2)]
      labelX = mid.x
      labelY = mid.y -10
    }
    ctx.save()
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'bottom'
    ctx.fillStyle = '#606266'

    ctx.fillText(edge.name, labelX, labelY)
    ctx.restore()
}

/* 首次进入在挂载后加载（确保 #flow-canvas 已渲染），flowGraph 变化时重新加载 */
onMounted(load)
watch(flowGraph, load)
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
      </div>

      <el-table :data="nodes" border size="small" class="flow-table">
        <el-table-column prop="flowGraph" label="流程图编号" width="80" show-overflow-tooltip />
        <el-table-column prop="code" label="节点编号" width="80" show-overflow-tooltip />
        <el-table-column prop="name" label="节点名称" width="80" show-overflow-tooltip />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ optionLabel(categoryOptions, row.category) }}</template>
        </el-table-column>
        <el-table-column prop="X" label="X坐标" width="60" show-overflow-tooltip />
        <el-table-column prop="Y" label="Y坐标" width="60" show-overflow-tooltip />
        <el-table-column prop="W" label="宽度" width="60" show-overflow-tooltip />
        <el-table-column prop="H" label="高度" width="60" show-overflow-tooltip />
        <el-table-column prop="shape" label="图形" width="80" show-overflow-tooltip />
        <el-table-column label="操作人分类" width="80">
          <template #default="{ row }">{{ optionLabel(operatorOptions, row.operator) }}</template>
        </el-table-column>
        <el-table-column prop="roleList" label="角色" width="80" show-overflow-tooltip />
        <el-table-column prop="userList" label="执行人" width="80" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ $index }">
            <el-button type="primary" link size="small" @click="openNodeEdit($index)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 连线定义（只读，编辑走弹窗） -->
    <div class="flow-section">
      <div class="flow-section__head">
        <span class="flow-section__title">连线定义</span>
        <span class="tip">共 {{ edges.length }} 条连线（表格只读，编辑请点击操作按钮）</span>
      </div>

      <el-table :data="edges" border size="small" class="flow-table">
        <el-table-column prop="flowGraph" label="流程图编号" width="80" show-overflow-tooltip />
        <el-table-column prop="code" label="代码" width="80" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" width="80" show-overflow-tooltip />
        <el-table-column label="执行分类" width="80">
          <template #default="{ row }">{{ optionLabel(edgeCategoryOptions, row.category) }}</template>
        </el-table-column>
        <el-table-column label="开始节点" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ nodeLabel(row.fromNode) }}</template>
        </el-table-column>
        <el-table-column label="目标节点" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ nodeLabel(row.toNode) }}</template>
        </el-table-column>
        <el-table-column prop="cond" label="条件" width="80" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" width="80" show-overflow-tooltip />
        <el-table-column label="颜色" width="60">
          <template #default="{ row }">
            <span class="color-dot" :style="{ background: row.color }" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ $index }">
            <el-button type="primary" link size="small" @click="openEdgeEdit($index)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="flow-section">
      <div class="flow-section__head">
        <span class="flow-section__title">流程图显示</span>
      </div>
      <div class="flow-section__body">
          <canvas id="flow-canvas"></canvas>
      </div>
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
        <el-form-item label="X坐标">
          <el-input v-model="nodeForm.X" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="Y坐标">
          <el-input v-model="nodeForm.Y" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="宽度">
          <el-input v-model="nodeForm.W" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="高度">
          <el-input v-model="nodeForm.H" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="nodeForm.color" />
        </el-form-item>
        <el-form-item label="操作人分类">
          <el-select v-model="nodeForm.operator" placeholder="请选择操作人分类" clearable style="width: 100%">
            <el-option v-for="o in operatorOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
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
            <el-option v-for="n in nodes" :key="n.code" :label="n.name" :value="n.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点" prop="toNode">
          <el-select v-model="edgeForm.toNode" filterable placeholder="请选择目标节点" style="width: 100%">
            <el-option v-for="n in nodes" :key="n.code" :label="n.name" :value="n.code" />
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
