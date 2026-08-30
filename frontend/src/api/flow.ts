import http from './http'
import type { ApiResponse } from './http'

/** 流程节点 */
export interface FlowNode {
  /** 节点唯一标识 */
  id: number
  /** 所属流程图编码 */
  flowGraph: string
  /** 节点编码 */
  code: string
  /** 节点名称 */
  name: string
  /** 节点分类：start/task/decision/end */
  category: string
  /** 节点形状：rect/round/diamond/circle */
  shape: string
  /** 坐标，格式 "x,y" */
  X: string
  Y: string
  W: string
  H: string
  /** 节点颜色 */
  color: string
  /** 操作人 */
  operator: string
  /** 角色列表（逗号分隔） */
  roleList: string
  /** 用户列表（逗号分隔） */
  userList: string
}

/** 流程连线 */
export interface FlowEdge {
  /** 连线唯一标识 */
  id: number
  /** 所属流程图编码 */
  flowGraph: string
  /** 连线编码 */
  code: string
  /** 连线名称 */
  name: string
  /** 连线分类：normal/condition/default */
  category: string
  /** 源节点 id */
  fromNode: string
  /** 目标节点 id */
  toNode: string
  /** 条件表达式 */
  cond: string
  /** 备注 */
  remark: string
  /** 连线颜色 */
  color: string
  /** 连线坐标点数组（可选，用于弯曲/标签定位） */
  axis: string
}

/** 流程图（表头）行 */
export interface GraphRow {
  /** 主键 id */
  id: number
  /** 流程图编号 */
  flowGraph: string
  /** 标题 */
  title: string
  /** 宽度 */
  width: number
  /** 高度 */
  heght: number
  /** 备注 */
  remark: string
}

/** 流程图保存/编辑入参（edit 时使用，id 必填） */
export type GraphSaveDTO = GraphRow

/** 流程图查询参数 */
export interface GraphSearchParams {
  /** 流程图编号（模糊） */
  flowGraph?: string
  /** 标题（模糊） */
  title?: string
  /** 页码，从 1 开始 */
  page?: number
  /** 每页条数 */
  pageSize?: number
}

/** 流程图分页返回 */
export interface GraphSearchResult {
  content: GraphRow[]
  total: number
  page: number
  pageSize: number
}

/** 节点新增/编辑入参（新增时 id 可省略） */
export type FlowNodeSaveDTO = Omit<FlowNode, 'id'> & { id?: number}

/** 连线新增/编辑入参（新增时 id 可省略） */
export type FlowEdgeSaveDTO = Omit<FlowEdge, 'id'> & { id?: number }

/**
 * 流程节点接口
 * 路径约定同 flowGraph（/api/flowNode/*），待后端实现后生效
 */
export const flowNodeAPI = {
  /** 按 flowGraph 查询节点列表（query 参数 flowGraph） */
  list: (flowGraph: string): Promise<ApiResponse<FlowNode[]>> =>
    http.get<FlowNode[]>(`/api/flowNode/flowGraph/${flowGraph}`),

  /** 新增节点 */
  save: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.post<FlowNode>('/api/flowNode/save', data),

  /** 编辑节点（按 id 更新，id 必填） */
  update: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.put<FlowNode>('/api/flowNode/update', data),

  /** 删除节点（按 id） */
  remove: (id: string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowNode/${id}`),

  /** 批量保存某流程图下的全部节点 */
  saveBatch: (flowGraph: string, nodes: FlowNodeSaveDTO[]): Promise<ApiResponse<FlowNode[]>> =>
    http.post<FlowNode[]>('/api/flowNode/batch', { flowGraph, nodes }),
}

/** 流程连线接口（路径约定 /api/flowEdge/*，待后端实现后生效） */
export const flowEdgeAPI = {
  /** 按 flowGraph 查询连线列表（query 参数 flowGraph） */
  list: (flowGraph: string): Promise<ApiResponse<FlowEdge[]>> =>
    http.get<FlowEdge[]>(`/api/flowEdge/flowGraph/${flowGraph}`),

  /** 新增连线 */
  save: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.post<FlowEdge>('/api/flowEdge/save', data),

  /** 编辑连线（按 id 更新，id 必填） */
  update: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.put<FlowEdge>('/api/flowEdge/update', data),

  /** 删除连线（按 id） */
  remove: (id: string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowEdge/${id}`),

  /** 批量保存某流程图下的全部连线 */
  saveBatch: (flowGraph: string, edges: FlowEdgeSaveDTO[]): Promise<ApiResponse<FlowEdge[]>> =>
    http.post<FlowEdge[]>('/api/flowEdge/batch', { flowGraph, edges }),
}

/** 流程图（表头）接口，路径 /api/flowGraph/*，待后端实现后生效 */
export const graphAPI = {
  /** 查询流程图列表（分页） */
  search: (params?: GraphSearchParams): Promise<ApiResponse<GraphSearchResult>> =>
    http.post<GraphSearchResult>('/api/flowGraph/search', params),

  /** 新增流程图 */
  save: (data: GraphSaveDTO): Promise<ApiResponse<GraphRow>> =>
    http.post<GraphRow>('/api/flowGraph/save', data),

  /** 编辑流程图 */
  edit: (data: GraphSaveDTO): Promise<ApiResponse<GraphRow>> =>
    http.put<GraphRow>('/api/flowGraph/update', data),

  /** 删除流程图（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowGraph/${id}`),
}

export const flowAPI = {
  /** 节点 */
  node: flowNodeAPI,

  /** 连线 */
  edge: flowEdgeAPI,

  /** 流程图 */
  graph: graphAPI,
}
