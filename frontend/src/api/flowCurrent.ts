import http from './http'
import type { ApiResponse } from './http'

/** 流程实例当前节点行（对齐后端 FlowCurrent 实体 / flowcurrent 表） */
export interface FlowCurrentRow {
  /** 主键 id */
  id: number
  /** 流程编号 */
  workflow: string
  /** 流程图编号 */
  flowGraph: string
  /** 当前节点编码 */
  flowNode: string
  /** 开始时间 */
  startTime: string
  /** 备注 */
  remark: string
}

/** 流程当前节点接口（FlowCurrentController /flowCurrent） */
export const flowCurrentAPI = {
  /** 按流程编号查询当前节点（可能存在多个并行节点，返回数组） */
  listByWorkflow: (workflow: string): Promise<ApiResponse<FlowCurrentRow[]>> =>
    http.get<FlowCurrentRow[]>(`/api/flowCurrent/workflow/${encodeURIComponent(workflow)}`),
}
