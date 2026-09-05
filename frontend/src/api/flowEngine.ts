import http from './http'
import type { ApiResponse } from './http'

/** 流程引擎接口（FlowEngineController /flowEngine） */
export const flowEngineAPI = {
  /**
   * 处理节点：沿指定连线流转当前流程实例
   * workflow = 流程编号，flowGraph = 流程图编号，edge = 连线编码（须为流程图中定义的真实连线编码）
   */
  deal: (params: { workflow: string; flowGraph: string; edge: string }): Promise<ApiResponse<string>> =>
    http.post<string>('/api/flowEngine/deal', params),
}
