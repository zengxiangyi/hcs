import http from './http'
import type { PageResult } from './http'

/** 工艺工序行（对齐后端 TechStep 实体 / techstep 表） */
export interface TechStepRow {
  /** 主键，表格中隐藏列 */
  id: number
  /** 一级工艺（编码，如 TZ / CH） */
  firstLevel: string
  /** 二级工艺（编码，如 TZ01 / CH01） */
  secondLevel: string
  /** 工序编号 */
  step: string
  /** 工序名称 */
  stepName: string
  /** 排序 */
  sort: string
  /** 是否必需：Y-是 / N-否 */
  isNeed: string
  /** 备注 */
  remark: string
}

/** 工艺工序列表查询参数 */
export interface TechStepListParams {
  firstLevel?: string
  secondLevel?: string
  page?: number
  pageSize?: number
}

/** 工艺工序列表返回（服务端分页） */
export type TechStepListResult = PageResult<TechStepRow>

/** 工艺工序新增入参（POST /save 后端忽略 id，一律按新增处理） */
export interface TechStepSaveParams {
  id?: number
  firstLevel: string
  secondLevel: string
  step: string
  stepName: string
  sort: string
  isNeed: string
  remark?: string
}

/** 工艺工序修改入参（PUT /update 后端强制要求 id，缺失即 400） */
export interface TechStepUpdateParams extends TechStepSaveParams {
  id: number
}

/** 工艺工序接口（TechStepController /techstep） */
export const techStepAPI = {
  /** 工序列表（服务端分页） */
  search: (params?: TechStepListParams) =>
    http.post<TechStepListResult>('/api/techstep/search', params),
  /** 新增工序（后端忽略 id） */
  add: (data: TechStepSaveParams) => http.post<TechStepRow>('/api/techstep/save', data),
  /** 修改工序（id 必填） */
  update: (data: TechStepUpdateParams) =>
    http.put<TechStepRow>('/api/techstep/update', data),
  /** 删除工序 */
  remove: (id: number) => http.delete<null>(`/api/techstep/${id}`),
  /** 批量新增工序。注意：step=工序编号、stepName=工序名称（contract.md 第 6 条），调用方需自行映射，勿直接传看板行 */
  batchSave: (data: TechStepSaveParams[]) => http.post<string>('/api/techstep/batchSave', data),
}
