import http from './http'
import type { ApiResponse, PageResult } from './http'

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

/** 待办/已办查询参数（POST 请求体，分页 1 基）。
 *  后端 dto.WorkflowQuery 目前仅支持分页（dealUser/roleCode 由后端按当前登录用户覆盖），
 *  targetCode/时间范围过滤待后端补齐后再扩展此类型。 */
export interface WorkflowQuery {
  page?: number
  pageSize?: number
}

/** 分页返回 */
export type WorkflowListResult = PageResult<WorkflowRow>

/** 流程实例接口（WorkflowController /workflow） */
export const workflowAPI = {
  /** 按流程编号查询 */
  getByCode: (code: string): Promise<ApiResponse<WorkflowRow>> =>
    http.get<WorkflowRow>(`/api/workflow/code/${encodeURIComponent(code)}`),

  /** 编辑流程实例（id 必填，常用于变更状态） */
  update: (data: WorkflowRow): Promise<ApiResponse<WorkflowRow>> =>
    http.put<WorkflowRow>('/api/workflow/update', data),

  /** 删除流程实例（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/workflow/${id}`),

  /** 我发起的（处理人取自当前登录用户，后端固定取前 30 条；无实例时 data 可能为 null） */
  sender: (): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.get<WorkflowListResult | null>('/api/workflow/sender'),

  /** 我的待办（POST 请求体查询；用户无角色时 data 可能为 null） */
  todo: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/todo', query),

  /** 我的已办（POST 请求体查询；用户无角色时 data 可能为 null） */
  done: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/done', query),
}
