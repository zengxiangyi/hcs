import http from './http'
import type { ApiResponse } from './http'

/** 流程实例行（对齐后端 Workflow 实体 / workflow 表） */
export interface WorkflowRow {
  id: number
  code: string
  name: string
  category: string
  targetCode: string
  sender: string
  state: string
  startTime: string
  endTime: string
  flowGraph: string
  remark: string
}

/** 流程实例新增/编辑入参（编辑时 id 必填） */
export type WorkflowSaveDTO = Omit<WorkflowRow, 'id'> & { id?: number }

/** 待办/已办查询参数（POST 请求体，分页 1 基） */
export interface WorkflowQuery {
  targetCode?: string
  startTimeStart?: string
  startTimeEnd?: string
  page?: number
  pageSize?: number
}

/** 分页返回 */
export interface WorkflowListResult {
  content: WorkflowRow[]
  total: number
  page: number
  pageSize: number
}

/** 流程实例接口（WorkflowController /workflow） */
export const workflowAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10): Promise<ApiResponse<WorkflowListResult>> =>
    http.get<WorkflowListResult>(`/api/workflow/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<WorkflowRow>> =>
    http.get<WorkflowRow>(`/api/workflow/${id}`),

  /** 按流程编号查询 */
  getByCode: (code: string): Promise<ApiResponse<WorkflowRow>> =>
    http.get<WorkflowRow>(`/api/workflow/code/${code}`),

  /** 新增流程实例 */
  save: (data: WorkflowSaveDTO): Promise<ApiResponse<WorkflowRow>> =>
    http.post<WorkflowRow>('/api/workflow/save', data),

  /** 编辑流程实例（id 必填，常用于变更状态） */
  update: (data: WorkflowSaveDTO): Promise<ApiResponse<WorkflowRow>> =>
    http.put<WorkflowRow>('/api/workflow/update', data),

  /** 删除流程实例（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/workflow/${id}`),

  /** 按状态查询 */
  listByState: (state: string): Promise<ApiResponse<WorkflowRow[]>> =>
    http.get<WorkflowRow[]>(`/api/workflow/state/${state}`),

  /** 按流程图编号查询 */
  listByFlowGraph: (flowGraph: string): Promise<ApiResponse<WorkflowRow[]>> =>
    http.get<WorkflowRow[]>(`/api/workflow/flowGraph/${flowGraph}`),

  /** 我发起的（处理人取自当前登录用户，后端固定取前 30 条） */
  sender: (): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.get<WorkflowListResult | null>('/api/workflow/sender'),

  /** 我的待办（POST 请求体查询） */
  todo: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/todo', query),

  /** 我的已办（POST 请求体查询） */
  done: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/done', query),
}
