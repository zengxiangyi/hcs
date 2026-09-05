import http from './http'
import type { ApiResponse } from './http'

/** 流程历史记录行（对齐后端 FlowHistory 实体 / flowhistory 表） */
export interface FlowHistoryRow {
  /** 主键 id */
  id: number
  /** 流程编号 */
  workflow: string
  /** 流程图编号 */
  flowGraph: string
  /** 连线编码 */
  edge: string
  /** 起始节点编码 */
  fromNode: string
  /** 目标节点编码 */
  toNode: string
  /** 处理时间 */
  dealTime: string
  /** 处理人工号 */
  dealUser: string
  /** 处理人姓名（引擎当前不写入，可能为 null） */
  userName: string | null
  /** 动作 */
  action: string
  /** 备注 */
  remark: string
  /** 处理说明（引擎当前不写入，可能为 null） */
  note: string | null
}

/** 流程历史接口（FlowHistoryController /flowHistory） */
export const flowHistoryAPI = {
  /** 按流程编号查询历史记录 */
  listByWorkflow: (workflow: string): Promise<ApiResponse<FlowHistoryRow[]>> =>
    http.get<FlowHistoryRow[]>(`/api/flowHistory/workflow/${encodeURIComponent(workflow)}`),
}
