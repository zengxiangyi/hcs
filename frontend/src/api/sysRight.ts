import http from './http'

/** 权限行（对齐后端 SysRight 实体 / sysright 表） */
export interface RightRow {
  id: number
  /** 编码 code（如 page:user / btn:user:add） */
  code: string
  /** 名称 name */
  name: string
  /** 分类 category：page 页面 / button 按钮 */
  category: string
  /** 备注 remark */
  remark: string
  /** 父级权限 code */
  parent: string
}

/** 权限列表查询参数（POST /search 请求体，分页 1 基） */
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

/** 权限接口（SysRightController /sysRight） */
export const rightAPI = {
  /** 权限列表（服务端分页） */
  search: (params?: RightListParams) => http.post<RightListResult>('/api/sysRight/search', params),
  /** 按 id 查询 */
  get: (id: number) => http.get<RightRow>(`/api/sysRight/${id}`),
  /** 按编码查询 */
  getByCode: (code: string) => http.get<RightRow>(`/api/sysRight/code/${code}`),
  /** 按分类查询 */
  listByCategory: (category: string) => http.get<RightRow[]>(`/api/sysRight/category/${category}`),
  /** 新增权限 */
  add: (data: RightSaveParams) => http.post<RightRow>('/api/sysRight/save', data),
  /** 修改权限（路由统一为 PUT /update，id 由请求体携带） */
  update: (id: number, data: RightSaveParams) => http.put<RightRow>('/api/sysRight/update', { ...data, id }),
  /** 删除权限（按编码级联删除角色权限绑定） */
  remove: (code: string) => http.delete<string>(`/api/sysRight/code/${code}`),
}
