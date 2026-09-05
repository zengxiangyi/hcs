import http from './http'
import type { ApiResponse, PageResult } from './http'

/** 流程图（表头）行（对齐后端 FlowGraph 实体 / flowgraph 表） */
export interface FlowGraphRow {
  /** 主键 id */
  id: number
  /** 流程图编号 */
  flowGraph: string
  /** 标题 */
  title: string
  /** 宽度（后端 varchar，序列化为字符串） */
  width: string
  /** 高度（后端 varchar，序列化为字符串） */
  height: string
  /** 备注 */
  remark: string
}

/** 流程图新增入参（编辑时需携带 id） */
export type FlowGraphSaveDTO = Omit<FlowGraphRow, 'id'> & { id?: number }

/** 流程图查询参数（POST /search 请求体，分页 1 基） */
export interface FlowGraphQuery {
  flowGraph?: string
  title?: string
  page?: number
  pageSize?: number
}

/** 分页返回 */
export type FlowGraphListResult = PageResult<FlowGraphRow>

/** 流程图接口（FlowGraphController /flowGraph） */
export const flowGraphAPI = {
  /** 分页查询 */
  search: (params?: FlowGraphQuery): Promise<ApiResponse<FlowGraphListResult>> =>
    http.post<FlowGraphListResult>('/api/flowGraph/search', params),

  /** 新增流程图（后端忽略 id） */
  save: (data: FlowGraphSaveDTO): Promise<ApiResponse<FlowGraphRow>> =>
    http.post<FlowGraphRow>('/api/flowGraph/save', data),

  /** 编辑流程图（id 必填） */
  update: (data: FlowGraphSaveDTO & { id: number }): Promise<ApiResponse<FlowGraphRow>> =>
    http.put<FlowGraphRow>('/api/flowGraph/update', data),

  /** 删除流程图（按 id，后端级联删除节点与连线） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowGraph/${id}`),
}
