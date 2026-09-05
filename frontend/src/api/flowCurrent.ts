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

/** 流程当前节点新增/编辑入参（编辑时 id 必填） */
export type FlowCurrentSaveDTO = Omit<FlowCurrentRow, 'id'> & { id?: number }

/** 分页返回 */
export interface FlowCurrentListResult {
  content: FlowCurrentRow[]
  total: number
  page: number
  pageSize: number
}

/** 流程当前节点接口（FlowCurrentController /flowCurrent） */
export const flowCurrentAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10): Promise<ApiResponse<FlowCurrentListResult>> =>
    http.get<FlowCurrentListResult>(`/api/flowCurrent/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<FlowCurrentRow>> =>
    http.get<FlowCurrentRow>(`/api/flowCurrent/${id}`),

  /** 按流程编号查询当前节点（可能存在多个并行节点，返回数组） */
  listByWorkflow: (workflow: string): Promise<ApiResponse<FlowCurrentRow[]>> =>
    http.get<FlowCurrentRow[]>(`/api/flowCurrent/workflow/${workflow}`),

  /** 按当前节点编码查询 */
  listByNode: (flowNode: string): Promise<ApiResponse<FlowCurrentRow[]>> =>
    http.get<FlowCurrentRow[]>(`/api/flowCurrent/node/${flowNode}`),

  /** 新增 */
  save: (data: FlowCurrentSaveDTO): Promise<ApiResponse<FlowCurrentRow>> =>
    http.post<FlowCurrentRow>('/api/flowCurrent/save', data),

  /** 编辑（id 必填） */
  update: (data: FlowCurrentSaveDTO): Promise<ApiResponse<FlowCurrentRow>> =>
    http.put<FlowCurrentRow>('/api/flowCurrent/update', data),

  /** 删除（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowCurrent/${id}`),
}
