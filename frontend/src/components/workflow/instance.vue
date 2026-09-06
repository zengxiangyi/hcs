<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { workflowAPI } from '../../api/workflow'
import { flowCurrentAPI, type FlowCurrentRow } from '../../api/flowCurrent'
import { flowHistoryAPI, type FlowHistoryRow } from '../../api/flowHistory'
import { flowNodeAPI, type FlowNode } from '../../api/flowNode'
import { flowEdgeAPI, type FlowEdge } from '../../api/flowEdge'

defineOptions({ name: 'Instance' })

const route = useRoute()
const router = useRouter()

/** 从 catch 的错误对象中提取用户可读信息 */
function getErrorMessage(err: unknown, fallback: string): string {
  return err instanceof Error && err.message ? err.message : fallback
}

/** URL 传入的流程编号，如 /web/workflow/instance?workflow=xxx */
const workflow = computed(() => {
  const raw = route.query.workflow
  return (Array.isArray(raw) ? raw[0] : raw)?.toString().trim() ?? ''
})

/* ---------------- 当前节点 ---------------- */

const currentLoading = ref(false)
/** 当前节点列表，并行流程可能同时存在多个待处理节点 */
const current = ref<FlowCurrentRow[]>([])

/** 加载流程实例当前节点信息 */
async function loadCurrent() {
  if (!workflow.value) {
    current.value = []
    return
  }
  currentLoading.value = true
  try {
    const res = await flowCurrentAPI.listByWorkflow(workflow.value)
    current.value = res.data ?? []
  } catch (err) {
    current.value = []
    ElMessage.error(getErrorMessage(err, '查询当前节点失败'))
  } finally {
    currentLoading.value = false
  }
}

/* ---------------- 历史操作记录 ---------------- */

const historyLoading = ref(false)
const history = ref<FlowHistoryRow[]>([])

/** 加载流程实例历史操作记录 */
async function loadHistory() {
  if (!workflow.value) {
    history.value = []
    return
  }
  historyLoading.value = true
  try {
    const res = await flowHistoryAPI.listByWorkflow(workflow.value)
    history.value = res.data ?? []
  } catch (err) {
    history.value = []
    ElMessage.error(getErrorMessage(err, '查询历史操作记录失败'))
  } finally {
    historyLoading.value = false
  }
}

/* ---------------- 流程图 ---------------- */

const flowGraphLoading = ref(false)
const nodes = ref<FlowNode[]>([])
const edges = ref<FlowEdge[]>([])

async function loadFlowGraph() {
  if (!workflow.value) {
    nodes.value = []
    edges.value = []
    return
  }
  flowGraphLoading.value = true
  try {
    const instance = await workflowAPI.getByCode(workflow.value)
    const flowGraph = instance.data?.flowGraph
    if (flowGraph) {
      const [nodeRes, edgeRes] = await Promise.all([
        flowNodeAPI.listByFlowGraph(flowGraph),
        flowEdgeAPI.listByFlowGraph(flowGraph),
      ])
      nodes.value = nodeRes.data || []
      edges.value = edgeRes.data || []
      // 渲染canvas
      renderCanvas()
    }
  } catch (err) {
    nodes.value = []
    edges.value = []
    ElMessage.error(getErrorMessage(err, '查询流程图失败'))
  } finally {
    flowGraphLoading.value = false
  }
}

/**
 * 依据 nodes.value / edges.value 数据绘制流程图。
 * 节点通过 code 互连，坐标取自 x/y，尺寸取自 w/h，形状取自 shape。
 */
async function renderCanvas() {
  const canvas = document.getElementById('flowmap') as HTMLCanvasElement
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
    WIDTH = Math.max(WIDTH, p.X + p.W)
    HEIGHT = Math.max(HEIGHT, p.Y + p.H)
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

type AxisPoint = { x: number; y: number }

/** 将 edge.axis（坐标点对象数组 [{x,y},...]）过滤为有效坐标点数组，无法解析时返回空数组 */
function parseAxis(axis: Array<{ x: number; y: number }>| undefined): AxisPoint[] {
  if (!Array.isArray(axis)) return []
  return axis.filter((p) => p && Number.isFinite(p.X) && Number.isFinite(p.Y))
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
    ctx.moveTo(points[0].X, points[0].Y)
    points.forEach((point) => ctx.lineTo(point.X, point.Y))
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
    drawArrow(ctx, bef.X, bef.Y, last.X, last.Y, color)
  } else {
    drawArrow(ctx, startX, startY, endX, endY, color)
  }

  // 绘制连线标签
    let labelX = (startX + endX) / 2
    let labelY = (startY + endY) / 2
    if (points.length > 0) {
      const mid = points[Math.floor(points.length / 2)]
      labelX = mid.X
      labelY = mid.Y -10
    }
    ctx.save()
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'bottom'
    ctx.fillStyle = '#606266'

    ctx.fillText(edge.name, labelX, labelY)
    ctx.restore()
}

/** 刷新当前流程实例的当前节点与历史记录 */
function loadAll() {
  //当前节点
  loadCurrent()
  // 历史信息
  loadHistory()
  // 获取流程图
  loadFlowGraph()
}

function goBack() {
  router.back()
}
onMounted(loadAll)

// 地址栏 workflow 参数变化时（如从其它页面跳转过来）重新加载
watch(workflow, () => {
  loadAll()
})
</script>

<template>
  <div class="instance-page">
    <h3 class="page-title">
      流程实例信息查看
      <span v-if="workflow" class="workflow-no">流程编号：{{ workflow }}</span>
      <span><el-button type="success" @click="goBack">返回</el-button></span>
    </h3>
    <el-empty v-if="!workflow" description="缺少流程编号参数（workflow），请从流程列表页进入" />
    <template v-else>
      <!-- 当前节点信息（并行处理时可能有多条） -->
      <div class="section-title">
        当前节点信息
        <span class="section-tip">共 {{ current.length }} 个待处理节点</span>
      </div>
      <el-table
        :data="current"
        v-loading="currentLoading"
        border
        stripe
        style="width: 100%"
        :class="{ 'is-compact': !currentLoading && current.length < 3 }"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="flowGraph" label="流程图" min-width="140" show-overflow-tooltip />
        <el-table-column prop="flowNode" label="当前节点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <template #empty>
          <el-empty :image-size="64" description="暂无当前节点信息" />
        </template>
      </el-table>

      <!-- 历史操作记录 -->
      <div class="section-title">历史操作记录</div>
      <el-table
        :data="history"
        v-loading="historyLoading"
        border
        stripe
        style="width: 100%"
        :class="{ 'is-compact': !historyLoading && history.length < 3 }"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="flowGraph" label="流程图" min-width="140" show-overflow-tooltip />
        <el-table-column label="流转节点" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.fromNode }} → {{ row.toNode }}</template>
        </el-table-column>
        <el-table-column prop="dealTime" label="处理时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dealUser" label="处理人工号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="userName" label="处理人姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="action" label="动作" min-width="120" show-overflow-tooltip />
        <el-table-column prop="note" label="处理说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <template #empty>
          <el-empty :image-size="64" description="暂无历史操作记录" />
        </template>
      </el-table>

      <!-- 流程图信息 -->
      <div class="section-title">流程图信息</div>
      <canvas id="flowmap" v-loading="flowGraphLoading"></canvas>
    </template>
  </div>
</template>

<style scoped>
.instance-page {
  padding: 16px;
  color: #333;
}
.page-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin: 0 0 16px;
}
.workflow-no {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}
.section-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 16px 0 8px;
  font-weight: 600;
}
.section-tip {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

/* 表格：限定默认高度区间，超出后表体内部滚动，表头固定 */
.instance-page :deep(.el-table__body-wrapper) {
  min-height: 96px;
  max-height: 300px;
}
.instance-page :deep(.el-scrollbar__wrap) {
  max-height: 300px;
}

/* 数据为空或少于 3 条时压缩表体高度，避免留出大片空白 */
.instance-page :deep(.el-table.is-compact .el-table__body-wrapper) {
  min-height: 0;
}
.instance-page :deep(.el-table.is-compact .el-scrollbar__wrap) {
  max-height: none;
}
.instance-page :deep(.el-table.is-compact .el-table__empty-block) {
  min-height: 0;
}
.instance-page :deep(.el-table.is-compact .el-table__empty-text) {
  width: 100%;
  line-height: normal;
}
/* 紧凑模式下收窄空状态插画与内边距 */
.instance-page :deep(.el-table.is-compact .el-empty) {
  --el-empty-padding: 12px 0;
  --el-empty-description-margin-top: 8px;
}

/* 底部流程图画布：块级居中展示 */
#flowmap {
  display: block;
  position: relative;
  margin: 0 auto;
  max-width: 100%;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
</style>
