import http from './http'
import type { ApiResponse } from './http'

/** 流程实例当前节点行 */
export interface InstanceRow {
  /** 主键 id */
  id: number
  /** 流程编号 */
  workflow: string
  /** 流程图编号 */
  flowGraph: string
  /** 当前节点编码 */
  flowNode: string
  /** 开始时间 */
  startTime: string
  /** 备注 */
  remark: string
}

/** 流程实例历史记录行 */
export interface FlowHistoryRow {
  /** 主键 id */
  id: number
  /** 流程编号 */
  workflow: string
  /** 流程图编号 */
  flowGraph: string
  /** 节点编码 */
  flowNode: string
  /** 处理时间 */
  dealTime: string
  /** 处理人工号 */
  dealUser: string
  /** 处理人姓名 */
  userName: string
  /** 动作 */
  action: string
  /** 备注 */
  remark: string
  /** 处理说明 */
  note: string
}

/** 流程变更入参（按 id 更新，可只传需变更的字段） */
export interface InstanceUpdateDTO {
  /** 主键 id（必填） */
  id: number
  /** 流程编号 */
  workflow?: string
  /** 流程图编号 */
  flowGraph?: string
  /** 节点编码 */
  flowNode?: string
  /** 备注 */
  remark?: string
}

export interface WorkflowRow {
id: number
code: string
name: string
flowGraph: string
category: string
targetCode: string
sender: string
state: string
startTime: string
endTime: string
remark: string
}

/** 流程实例接口 */
export const instanceAPI = {

  detail: (workflow: string): Promise<ApiResponse<WorkflowRow>> =>
    http.get<WorkflowRow>(`/api/workflow/code/${workflow}`),

  /**
   * 流程实例当前节点（按流程编号查询）
   * 一个流程实例可能存在多个并行处理的当前节点，因此返回数组
   */
  current: (workflow: string): Promise<ApiResponse<InstanceRow[]>> =>
    http.get<InstanceRow[]>(`/api/flowCurrent/workflow/${workflow}`),

  /** 流程实例历史记录*/
  history: (workflow: string): Promise<ApiResponse<FlowHistoryRow[]>> =>
    http.get<FlowHistoryRow[]>(`/api/flowHistory/workflow/${workflow}`),
}
