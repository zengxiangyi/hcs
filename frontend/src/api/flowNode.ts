import http from './http'
import type { ApiResponse, PageResult } from './http'

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
  /**
   * 坐标/尺寸（JSON 键为小写 x/y/w/h——Java 属性名来自 getX() 等标准 getter；
   * 大写 X/Y/W/H 仅是 DB 列名）。前端读写必须用小写。
   */
  x: string
  y: string
  w: string
  h: string
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
export type FlowNodeListResult = PageResult<FlowNode>

/** 流程节点接口（FlowNodeController /flowNode） */
export const flowNodeAPI = {
  /** 按流程图编码查询节点列表 */
  listByFlowGraph: (flowGraph: string): Promise<ApiResponse<FlowNode[]>> =>
    http.get<FlowNode[]>(`/api/flowNode/flowGraph/${encodeURIComponent(flowGraph)}`),

  /** 新增节点 */
  save: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.post<FlowNode>('/api/flowNode/save', data),

  /**
   * 编辑节点。注意：后端按 flowGraph+code 匹配（不使用 id），匹配不到时静默不落库；
   * 因此不能用于改 code 的场景（code 在设计器中编辑态只读）。
   */
  update: (data: FlowNodeSaveDTO): Promise<ApiResponse<FlowNode>> =>
    http.put<FlowNode>('/api/flowNode/update', data),

  /** 删除节点（按 id，后端级联删除两端连线） */
  remove: (id: number | string): Promise<ApiResponse<null>> =>
    http.delete<null>(`/api/flowNode/${id}`),
}
