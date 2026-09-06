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

/** 待办行（对齐后端 common.Todo）。字段全部来自 WorkflowMapper.queryTodo 的 SELECT 列映射，均为字符串。
 *  JSON key 与 Java 属性名一致（camelCase）：startTime（节点等待开始）/ beginTime（流程发起时间）区分，
 *  flowGraph（流程图编号）/ flowNode（节点编码）。 */
export interface TodoRow {
  /** 流程实例 code（= workflow.code，当前待办所属流程） */
  workflow: string
  /** 流程图编号 */
  flowGraph: string
  /** 流程节点编码 */
  flowNode: string
  /** 节点开始等待时间（flowcurrent.starttime） */
  startTime: string
  /** 备注（flowcurrent.remark） */
  remark: string
  /** 节点名称（flowNode.name） */
  nodeName: string
  /** 操作人分类 */
  operator: string
  /** 角色列表 */
  roleList: string
  /** 用户列表 */
  userList: string
  /** 流程编号（workflow.code） */
  code: string
  /** 流程名称 */
  name: string
  /** 流程分类 */
  category: string
  /** 目标对象 ID */
  targetCode: string
  /** 发起人 */
  sender: string
  /** 发起时间（workflow.starttime） */
  beginTime: string
  /** 流程状态 */
  state: string
}

/** 分页返回 */
export type WorkflowListResult = PageResult<WorkflowRow>
/** 待办分页返回（/workflow/todo 返回 PageResult<Todo>） */
export type TodoListResult = PageResult<TodoRow>

/** 流程实例接口（WorkflowController /workflow） */
export const workflowAPI = {
  /** 按流程编号查询 */
  getByCode: (code: string): Promise<ApiResponse<WorkflowRow>> =>
    http.get<WorkflowRow>(`/api/workflow/code/${encodeURIComponent(code)}`),

  /** 编辑流程实例（id 必填，常用于变更状态） */
  update: (data: WorkflowRow): Promise<ApiResponse<WorkflowRow>> =>
    http.put<WorkflowRow>('/api/workflow/update', data),

    /** 编辑流程实例（id 必填，常用于变更状态） */
  changeState: (data: {code: string, state: string}): Promise<ApiResponse<string>> =>
    http.post<string>('/api/workflow/changeState', data),

  /** 删除流程实例（按 id） */
  remove: (id: number): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/workflow/${id}`),

  /** 我发起的（处理人取自当前登录用户，无实例时 data 可能为 null） */
  sender: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/sender', query),

  /** 我的待办（POST 请求体查询；用户无角色时 data 可能为 null） */
  todo: (query?: WorkflowQuery): Promise<ApiResponse<TodoListResult | null>> =>
    http.post<TodoListResult | null>('/api/workflow/todo', query),

  /** 我的已办（POST 请求体查询；用户无角色时 data 可能为 null） */
  done: (query?: WorkflowQuery): Promise<ApiResponse<WorkflowListResult | null>> =>
    http.post<WorkflowListResult | null>('/api/workflow/done', query),
}
