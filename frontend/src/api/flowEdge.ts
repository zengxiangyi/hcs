import http from './http'
import type { ApiResponse, PageResult } from './http'

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
export type FlowEdgeListResult = PageResult<FlowEdge>

/** 流程连线接口（FlowEdgeController /flowEdge） */
export const flowEdgeAPI = {
  /** 按流程图编码查询连线列表 */
  listByFlowGraph: (flowGraph: string): Promise<ApiResponse<FlowEdge[]>> =>
    http.get<FlowEdge[]>(`/api/flowEdge/flowGraph/${encodeURIComponent(flowGraph)}`),

  /** 新增连线 */
  save: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.post<FlowEdge>('/api/flowEdge/save', data),

  /**
   * 编辑连线。注意：后端按 flowGraph+code 匹配（不使用 id），匹配不到时静默不落库；
   * 因此不能用于改 code 的场景（code 在设计器中编辑态只读）。
   */
  update: (data: FlowEdgeSaveDTO): Promise<ApiResponse<FlowEdge>> =>
    http.put<FlowEdge>('/api/flowEdge/update', data),

  /** 删除连线（按 id） */
  remove: (id: number | string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowEdge/${id}`),
}
