import http from './http'

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
export interface TechStepListResult {
  content: TechStepRow[]
  total: number
  page: number
  pageSize: number
}

/** 工艺工序新增/修改入参：id 缺省或 0 为新增，> 0 为修改 */
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

/** 工艺工序接口 */
export const techStepAPI = {
  /** 工序列表（服务端分页） */
  search: (params?: TechStepListParams) =>
    http.post<TechStepListResult>('/api/techstep/search', params),
  /** 新增工序（id = 0） */
  add: (data: TechStepSaveParams) => http.post<TechStepRow>('/api/techstep/save', data),
  /** 修改工序（id > 0） */
  update: (data: TechStepSaveParams) =>
    http.put<TechStepRow>('/api/techstep/update', data),
  /** 删除工序 */
  remove: (id: number) => http.delete<null>(`/api/techstep/${id}`),
}
