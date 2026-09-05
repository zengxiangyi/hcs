import http from './http'
import type { ApiResponse } from './http'

/** 流程图（表头）行（对齐后端 FlowGraph 实体 / flowgraph 表） */
export interface FlowGraphRow {
  /** 主键 id */
  id: number
  /** 流程图编号 */
  flowGraph: string
  /** 标题 */
  title: string
  /** 宽度 */
  width: number
  /** 高度 */
  height: number
  /** 备注 */
  remark: string
}

/** 流程图保存/编辑入参（edit 时 id 必填） */
export type FlowGraphSaveDTO = FlowGraphRow

/** 流程图查询参数（POST /search 请求体，分页 1 基） */
export interface FlowGraphQuery {
  flowGraph?: string
  title?: string
  page?: number
  pageSize?: number
}

/** 分页返回 */
export interface FlowGraphListResult {
  content: FlowGraphRow[]
  total: number
  page: number
  pageSize: number
}

/** 流程图接口（FlowGraphController /flowGraph） */
export const flowGraphAPI = {
  /** 分页查询 */
  search: (params?: FlowGraphQuery): Promise<ApiResponse<FlowGraphListResult>> =>
    http.post<FlowGraphListResult>('/api/flowGraph/search', params),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<FlowGraphRow>> =>
    http.get<FlowGraphRow>(`/api/flowGraph/${id}`),

  /** 按流程图编码查询（取第一条，不存在抛 404） */
  getByFlowGraph: (flowGraph: string): Promise<ApiResponse<FlowGraphRow>> =>
    http.get<FlowGraphRow>(`/api/flowGraph/flowGraph/${flowGraph}`),

  /** 新增流程图 */
  save: (data: FlowGraphSaveDTO): Promise<ApiResponse<FlowGraphRow>> =>
    http.post<FlowGraphRow>('/api/flowGraph/save', data),

  /** 编辑流程图（id 必填） */
  update: (data: FlowGraphSaveDTO): Promise<ApiResponse<FlowGraphRow>> =>
    http.put<FlowGraphRow>('/api/flowGraph/update', data),

  /** 删除流程图（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowGraph/${id}`),
}
