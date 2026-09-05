import http from './http'
import type { ApiResponse } from './http'

/** 流程连线行（对齐后端 FlowEdge 实体 / flowedge 表） */
export interface FlowEdge {
  /** 主键 id */
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
  /** 连线坐标点数组（用于弯曲/标签定位） */
  axis: string
}

/** 连线新增/编辑入参（新增时 id 可省略） */
export type FlowEdgeSaveDTO = Omit<FlowEdge, 'id'> & { id?: number }

/** 分页返回 */
export interface FlowEdgeListResult {
  content: FlowEdge[]
  total: number
  page: number
  pageSize: number
}

/** 流程连线接口（FlowEdgeController /flowEdge） */
export const flowEdgeAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10): Promise<ApiResponse<FlowEdgeListResult>> =>
    http.get<FlowEdgeListResult>(`/api/flowEdge/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<FlowEdge>> =>
    http.get<FlowEdge>(`/api/flowEdge/${id}`),

  /** 按流程图编码查询连线列表 */
  listByFlowGraph: (flowGraph: string): Promise<ApiResponse<FlowEdge[]>> =>
    http.get<FlowEdge[]>(`/api/flowEdge/flowGraph/${flowGraph}`),

  /** 新增连线 */
  save: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.post<FlowEdge>('/api/flowEdge/save', data),

  /** 编辑连线（按 id 更新，id 必填） */
  update: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.put<FlowEdge>('/api/flowEdge/update', data),

  /** 删除连线（按 id） */
  remove: (id: number | string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowEdge/${id}`),

  /** 按源节点查询 */
  listByFromNode: (fromNode: string): Promise<ApiResponse<FlowEdge[]>> =>
    http.get<FlowEdge[]>(`/api/flowEdge/from/${fromNode}`),

  /** 按目标节点查询 */
  listByToNode: (toNode: string): Promise<ApiResponse<FlowEdge[]>> =>
    http.get<FlowEdge[]>(`/api/flowEdge/to/${toNode}`),
}
