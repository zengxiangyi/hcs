import http from './http'

/** 系统用户新增/修改入参 */
export interface WorkflowParams {
  id: number
  code: string
  name: string
  category: string
  targetCode: string
  sender: string
  startTime: string
  state: string
  flowGraph: string
  endTime: string
  remark: string
}

/** 系统用户接口（后端已开发完毕） */
export const ApprovalAPI = {
  /** 修改流程状态 */
  update: (data: WorkflowParams) => http.put<string>('/api/workflow/update', data),
  /** 删除流程实例 */
  remove: (id: number) => http.delete<string>(`/api/workflow/${id}`),
}