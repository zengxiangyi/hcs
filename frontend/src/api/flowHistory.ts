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
  /** 节点编码 */
  flowNode: string
  /** 处理时间 */
  dealTime: string
  /** 处理人工号 */
  dealUser: string
  /** 处理人姓名 */
  userName: string
  /** 动作 */
  action: string
  /** 备注 */
  remark: string
  /** 处理说明 */
  note: string
}

/** 流程历史新增/编辑入参（编辑时 id 必填） */
export type FlowHistorySaveDTO = Omit<FlowHistoryRow, 'id'> & { id?: number }

/** 分页返回 */
export interface FlowHistoryListResult {
  content: FlowHistoryRow[]
  total: number
  page: number
  pageSize: number
}

/** 流程历史接口（FlowHistoryController /flowHistory） */
export const flowHistoryAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10): Promise<ApiResponse<FlowHistoryListResult>> =>
    http.get<FlowHistoryListResult>(`/api/flowHistory/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<FlowHistoryRow>> =>
    http.get<FlowHistoryRow>(`/api/flowHistory/${id}`),

  /** 按流程编号查询历史记录 */
  listByWorkflow: (workflow: string): Promise<ApiResponse<FlowHistoryRow[]>> =>
    http.get<FlowHistoryRow[]>(`/api/flowHistory/workflow/${workflow}`),

  /** 按处理人工号查询 */
  listByDealUser: (dealUser: string): Promise<ApiResponse<FlowHistoryRow[]>> =>
    http.get<FlowHistoryRow[]>(`/api/flowHistory/user/${dealUser}`),

  /** 新增 */
  save: (data: FlowHistorySaveDTO): Promise<ApiResponse<FlowHistoryRow>> =>
    http.post<FlowHistoryRow>('/api/flowHistory/save', data),

  /** 编辑（id 必填） */
  update: (data: FlowHistorySaveDTO): Promise<ApiResponse<FlowHistoryRow>> =>
    http.put<FlowHistoryRow>('/api/flowHistory/update', data),

  /** 删除（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowHistory/${id}`),
}
