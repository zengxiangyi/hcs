import http from './http'

/** 权限行（对齐 mock.ts 的 Right 结构） */
export interface RightRow {
  id: number
  /** 编码 code（如 page:user / btn:user:add） */
  code: string
  /** 名称 name */
  name: string
  /** 分类 category（原 module 字段）：page 页面 / button 按钮 */
  category: string
  /** 备注 remark */
  remark: string
}

/** 权限列表查询参数 */
export interface RightListParams {
  code?: string
  name?: string
  category?: string
  page?: number
  pageSize?: number
}

/** 权限列表返回 */
export interface RightListResult {
  content: RightRow[]
  total: number
  page: number
  pageSize: number
}

/** 权限新增/修改入参 */
export interface RightSaveParams {
  id?: number
  code: string
  name: string
  category: string
  remark?: string
}

/** 权限接口 */
export const rightAPI = {
  /** 权限列表（服务端分页） */
  search: (params?: RightListParams) => http.post<RightListResult>('/api/sysRight/search', params),
  /** 新增权限 */
  add: (data: RightSaveParams) => http.post<RightRow>('/api/sysRight/save', data),
  /** 修改权限 */
  update: (id: number, data: RightSaveParams) => http.put<RightRow>(`/api/sysRight/${id}`, data),
  /** 删除权限 */
  remove: (id: number) => http.delete<null>(`/api/sysRight/${id}`),
}
