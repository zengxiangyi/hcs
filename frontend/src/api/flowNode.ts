import http from './http'
import type { ApiResponse } from './http'

/** 流程节点行（对齐后端 FlowNode 实体 / flownode 表） */
export interface FlowNode {
  /** 主键 id */
  id: number
  /** 所属流程图编码 */
  flowGraph: string
  /** 节点编码 */
  code: string
  /** 节点名称 */
  name: string
  /** 节点分类：start/task/decision/end */
  category: string
  /** 节点形状：rect/round/diamond/circle */
  shape: string
  /** 坐标（唯一保留大写的四列：X/Y/W/H） */
  X: string
  Y: string
  W: string
  H: string
  /** 节点颜色 */
  color: string
  /** 操作人 */
  operator: string
  /** 角色列表（逗号分隔） */
  roleList: string
  /** 用户列表（逗号分隔） */
  userList: string
}

/** 节点新增/编辑入参（新增时 id 可省略） */
export type FlowNodeSaveDTO = Omit<FlowNode, 'id'> & { id?: number }

/** 分页返回 */
export interface FlowNodeListResult {
  content: FlowNode[]
  total: number
  page: number
  pageSize: number
}

/** 流程节点接口（FlowNodeController /flowNode） */
export const flowNodeAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10): Promise<ApiResponse<FlowNodeListResult>> =>
    http.get<FlowNodeListResult>(`/api/flowNode/list?page=${page}&size=${size}`),

  /** 按 id 查询 */
  get: (id: number): Promise<ApiResponse<FlowNode>> =>
    http.get<FlowNode>(`/api/flowNode/${id}`),

  /** 按流程图编码查询节点列表 */
  listByFlowGraph: (flowGraph: string): Promise<ApiResponse<FlowNode[]>> =>
    http.get<FlowNode[]>(`/api/flowNode/flowGraph/${flowGraph}`),

  /** 新增节点 */
  save: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.post<FlowNode>('/api/flowNode/save', data),

  /** 编辑节点（按 id 更新，id 必填） */
  update: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.put<FlowNode>('/api/flowNode/update', data),

  /** 删除节点（按 id） */
  remove: (id: number | string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowNode/${id}`),

  /** 按节点分类查询 */
  listByCategory: (category: string): Promise<ApiResponse<FlowNode[]>> =>
    http.get<FlowNode[]>(`/api/flowNode/category/${category}`),
}
