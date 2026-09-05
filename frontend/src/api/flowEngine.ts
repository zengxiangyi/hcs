import http from './http'
import type { ApiResponse } from './http'

/** 流程引擎接口（FlowEngineController /flowEngine） */
export const flowEngineAPI = {
  /** 发起流程：flowType 为流程分类（必填） */
  start: (flowType: string): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/start', { flowType }),

  /**
   * 处理节点：沿指定连线流转当前流程实例
   * workflow = 流程编号，flowGraph = 流程图编号，edge = 连线编码
   */
  deal: (params: { workflow: string; flowGraph: string; edge: string }): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/deal', params),

  /** 查询某流程图下的当前流转状态（返回结构由后端 Map 组织） */
  current: (flowGraph: string): Promise<ApiResponse<Record<string, unknown>>> =>
    http.get<Record<string, unknown>>(`/api/flowEngine/flowGraph/${flowGraph}`),
}
