import http from './http'
import type { ApiResponse } from './http'

/** 流程图配置行（对齐后端 FlowGraph 实体 / flowgraph 表） */
export interface FlowGraphRow {
  id: number
  flowGraph: string
  title: string
  width: string
  height: string
  remark: string
}

/** 流程节点行（对齐后端 FlowNode 实体 / flownode 表） */
export interface FlowNodeRow {
  id: number
  flowGraph: string
  code: string
  name: string
  category: string
  shape: string
  color: string
  operator: string
  roleList: string
  userList: string
  x: string
  y: string
  w: string
  h: string
}

/** 流程连线行（对齐后端 FlowEdge 实体 / flowedge 表） */
export interface FlowEdgeRow {
  id: number
  code: string
  name: string
  color: string
  fromNode: string
  toNode: string
  axis: string
  flowGraph: string
  category: string
  cond: string
  remark: string
}

/** 流程图数据（getFlowGraph 返回：图 + 节点 + 连线） */
export interface FlowGraphData {
  graph: FlowGraphRow | null
  nodes: FlowNodeRow[]
  edges: FlowEdgeRow[]
}

/** 流程引擎接口（FlowEngineController /flowEngine） */
export const flowEngineAPI = {
  /**
   * 启动新流程：返回新建的流程实例编码（workflow.code）
   * flowType = 流程图编号（注意：Controller 请求体 key 为 flowType，非 flowGraph）
   */
  start: (params: { flowType: string }): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/start', params),

  /**
   * 处理节点：沿指定连线流转当前流程实例
   * workflow = 流程编号，flowGraph = 流程图编号，edge = 连线编码（须为流程图中定义的真实连线编码）
   */
  deal: (params: { workflow: string; flowGraph: string; edge: string }): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/deal', params),

  /**
   * 获取流程图：图 + 节点 + 连线（GET 路径段 flowGraph）
   */
  flowGraph: (flowGraph: string): Promise<ApiResponse<FlowGraphData>> =>
    http.get<FlowGraphData>(`/api/flowEngine/flowGraph/${encodeURIComponent(flowGraph)}`),

  /**
   * 撤回流程：终止指定实例并清理当前节点
   * workflow = 流程编号，reason = 撤回原因
   */
  cancel: (params: { workflow: string; reason: string }): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/cancel', params),
}
