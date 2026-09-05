import http from './http'
import type { ApiResponse } from './http'

/** 工序任务行 */
export interface TaskRow {
  /** id */
  id: number
  /** 调拨单 */
  transfer: string
  /** 蓝本工艺编号 */
  blueprint: string
  /** 审批状态 */
  auditState: string
  /** 工序 */
  step: string
  /** 状态 */
  state: string
  /** 创建人 */
  createUser: string
  /** 创建时间 */
  createTime: string
}

/** 工序任务列表查询参数 */
export interface TaskListParams {
  transfer?: string
  blueprint?: string
  auditState?: string
  step?: string
  state?: string
  page?: number
  pageSize?: number
}

/** 工序任务列表返回 */
export interface TaskListResult {
  content: TaskRow[]
  total: number
  page: number
  pageSize: number
}

/** 蓝本绑定入参（后端按 transfer/blueprint 编码字符串落库） */
export interface BindParams {
  /** 调拨单编码 */
  transfer: string
  /** 蓝本工艺编码 */
  blueprint: string
}

/** 工序任务接口 */
export const taskProcessAPI = {
  /** 列表（服务端分页） */
  search: (params?: TaskListParams) =>
    http.post<TaskListResult>('/api/taskprocess/search', params) as Promise<ApiResponse<TaskListResult>>,
  /** 新增 */
  add: (data: object) =>
    http.post<string>('/api/taskprocess/save', data) as Promise<ApiResponse<string>>,

  /** 绑定蓝本到调拨单 */
  bind: (data: BindParams) => http.post<string>('/api/taskprocess/bind', data),
  /** 修改 */
  update: (id: number, data: Omit<TaskRow, 'id'>) =>
    http.put<TaskRow>('/api/taskprocess/update', { ...data, id }) as Promise<ApiResponse<TaskRow>>,
  /** 删除 */
  remove: (id: number) =>
    http.delete<null>(`/api/taskprocess/${id}`) as Promise<ApiResponse<null>>,
}