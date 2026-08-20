<template>
  <div class="graph-container">
    <canvas
      ref="canvasRef"
      class="flow-canvas"
      @mousedown="onMouseDown"
      @mousemove="onMouseMove"
      @mouseup="onMouseUp"
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

// 1. 数据模型定义
interface FlowNode {
  id: string
  x: number
  y: number
  width: number
  height: number
  text: string
  color: string
  log: string
}

interface FlowEdge {
  from: string
  to: string
  color: string
}

const nodes: FlowNode[] = [
  { id: 'start', x: 50, y: 100, width: 120, height: 60, text: '开始', color: '#4CAF50', log: '处理时间:2026-08-16 90:00:00' },
  { id: 'process', x: 200, y: 100, width: 120, height: 60, text: '处理数据', color: '#2196F3', log: '处理时间:2026-08-17 90:00:00' },
  { id: 'end', x: 350, y: 100, width: 120, height: 60, text: '结束', color: '#F44336', log: '' },
]

const edges: FlowEdge[] = [
  { from: 'start', to: 'process', color: '#4CAF50' },
  { from: 'process', to: 'end', color: '#CCCCCC' },
]

// 2. 画布初始化与高清适配
const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
const ratio = window.devicePixelRatio || 1
const WIDTH = 600
const HEIGHT = 300

// 交互状态
let selectedNode: FlowNode | null = null
let offsetX = 0
let offsetY = 0
let dragMoved = false
let logTarget: FlowNode | null = null

function render() {
  if (!ctx) return
  ctx.clearRect(0, 0, WIDTH, HEIGHT)
  edges.forEach(renderEdge)
  nodes.forEach(renderNode)
  renderLog(logTarget) // 在节点之上绘制 log 气泡
}

function renderNode(node: FlowNode) {
  if (!ctx) return
  ctx.save()
  // 绘制圆角矩形背景
  ctx.beginPath()
  ctx.roundRect(node.x, node.y, node.width, node.height, 8)
  ctx.fillStyle = node.color
  ctx.shadowColor = 'rgba(0,0,0,0.2)'
  ctx.shadowBlur = 8
  ctx.shadowOffsetY = 4
  ctx.fill()
  ctx.shadowColor = 'transparent' // 重置阴影，避免影响文字

  // 绘制文本
  ctx.fillStyle = '#fff'
  ctx.font = '16px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(node.text, node.x + node.width / 2, node.y + node.height / 2)
  ctx.restore()
}

function renderEdge(edge: FlowEdge) {
  if (!ctx) return
  const fromNode = nodes.find((n) => n.id === edge.from)
  const toNode = nodes.find((n) => n.id === edge.to)
  if (!fromNode || !toNode) return

  // 计算连线起点和终点（从节点中心出发）
  const startX = fromNode.x + fromNode.width
  const startY = fromNode.y + fromNode.height / 2
  const endX = toNode.x
  const endY = toNode.y + toNode.height / 2

  // 绘制连线
  ctx.beginPath()
  ctx.moveTo(startX, startY)
  ctx.lineTo(endX, endY)
  ctx.strokeStyle = edge.color
  ctx.lineWidth = 2
  ctx.stroke()

  // 绘制箭头
  const angle = Math.atan2(endY - startY, endX - startX)
  const arrowLen = 12
  ctx.beginPath()
  ctx.moveTo(endX, endY)
  ctx.lineTo(endX - arrowLen * Math.cos(angle - Math.PI / 6), endY - arrowLen * Math.sin(angle - Math.PI / 6))
  ctx.lineTo(endX - arrowLen * Math.cos(angle + Math.PI / 6), endY - arrowLen * Math.sin(angle + Math.PI / 6))
  ctx.closePath()
  ctx.fillStyle = '#999'
  ctx.fill()
}

// 4. 交互逻辑：拖拽节点 + 点击显示 log
function onMouseDown(e: MouseEvent) {
  if (!canvasRef.value) return
  const rect = canvasRef.value.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  dragMoved = false

  // 逆序遍历，确保优先选中上层节点
  for (let i = nodes.length - 1; i >= 0; i--) {
    const node = nodes[i]
    if (mx >= node.x && mx <= node.x + node.width && my >= node.y && my <= node.y + node.height) {
      selectedNode = node
      offsetX = mx - node.x
      offsetY = my - node.y
      canvasRef.value.style.cursor = 'grabbing'
      return
    }
  }
  // 点击空白区域 → 关闭 log 气泡
  if (logTarget) {
    logTarget = null
    render()
  }
  selectedNode = null
}

function onMouseMove(e: MouseEvent) {
  if (!selectedNode || !canvasRef.value) return
  const rect = canvasRef.value.getBoundingClientRect()
  const newX = e.clientX - rect.left - offsetX
  const newY = e.clientY - rect.top - offsetY
  // 只有真正发生位移时才视为拖动
  if (Math.abs(newX - selectedNode.x) > 2 || Math.abs(newY - selectedNode.y) > 2) {
    dragMoved = true
  }
  selectedNode.x = newX
  selectedNode.y = newY
  render() // 实时更新并重绘
}

function onMouseUp() {
  // 没有发生拖动 → 视为点击，显示节点 log
  if (selectedNode && !dragMoved) {
    showNodeLog(selectedNode)
  }
  selectedNode = null
  if (canvasRef.value) canvasRef.value.style.cursor = 'default'
}

// 5. 点击节点显示 log：在节点上方绘制一个信息浮层
function showNodeLog(node: FlowNode) {
  logTarget = logTarget === node ? null : node // 再次点击相同节点则收起
  render()
}

function renderLog(node: FlowNode | null) {
  if (!ctx) return
  if (!node || !node.log) return
  if (node.log.trim() === '') return

  const boxW = 200
  const boxH = 30
  const boxX = node.x + node.width / 2 - boxW / 2
  const boxY = node.y - boxH - 16

  ctx.save()
  // 气泡阴影
  ctx.shadowColor = 'rgba(0,0,0,0.25)'
  ctx.shadowBlur = 10
  ctx.shadowOffsetY = 3
  // 气泡背景
  ctx.fillStyle = 'rgba(255,255,255,0.97)'
  ctx.beginPath()
  ctx.roundRect(boxX, boxY, boxW, boxH, 8)
  ctx.fill()
  ctx.shadowColor = 'transparent'
  // 边框
  ctx.strokeStyle = '#ccc'
  ctx.lineWidth = 1
  ctx.stroke()
  // 小三角指向节点
  ctx.beginPath()
  ctx.moveTo(node.x + node.width / 2 - 8, boxY + boxH)
  ctx.lineTo(node.x + node.width / 2 + 8, boxY + boxH)
  ctx.lineTo(node.x + node.width / 2, boxY + boxH + 10)
  ctx.closePath()
  ctx.fillStyle = '#fff'
  ctx.fill()

  // 文字
  ctx.fillStyle = '#333'
  ctx.font = '12px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(node.log, boxX + boxW / 2, boxY + boxH / 2)
  ctx.restore()
}

onMounted(() => {
  if (!canvasRef.value) return
  ctx = canvasRef.value.getContext('2d')
  canvasRef.value.width = WIDTH * ratio
  canvasRef.value.height = HEIGHT * ratio
  canvasRef.value.style.width = WIDTH + 'px'
  canvasRef.value.style.height = HEIGHT + 'px'
  ctx?.scale(ratio, ratio)
  if (ctx) {
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
  }
  render()
})

onBeforeUnmount(() => {
  ctx = null
  canvasRef.value = null
})
</script>

<style scoped>
.graph-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.flow-canvas {
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  cursor: default;
}
</style>
