import http from './http'

/** 常量值行（对齐 mock.ts 的 ConstValue 结构） */
export interface ConstValueRow {
  id: number
  /** 编码 code */
  code: string
  /** 名称 name */
  name: string
  /** 分类 category */
  category: string
  /** 标记 mark（取值说明） */
  mark: string
  /** 备注 remark */
  remark: string
}

/** 常量值列表查询参数 */
export interface ConstValueListParams {
  code?: string
  name?: string
  category?: string
  page?: number
  pageSize?: number
}

/** 常量值列表返回 */
export interface ConstValueListResult {
  content: ConstValueRow[]
  total: number
  page: number
  pageSize: number
}

/** 常量值新增/修改入参 */
export interface ConstValueSaveParams {
  id?: number
  code: string
  name: string
  category: string
  mark?: string
  remark?: string
}

/** 常量值接口 */
export const constValueAPI = {
  /** 常量值列表（服务端分页） */
  search: (params?: ConstValueListParams) =>
    http.post<ConstValueListResult>('/api/constValue/search', params),
  /** 新增常量值 */
  add: (data: ConstValueSaveParams) => http.post<ConstValueRow>('/api/constValue/save', data),
  /** 修改常量值 */
  update: (id: number, data: ConstValueSaveParams) =>
    http.put<ConstValueRow>(`/api/constValue/${id}`, data),
  /** 删除常量值 */
  remove: (id: number) => http.delete<null>(`/api/constValue/${id}`),
}
