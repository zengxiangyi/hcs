import http from './http'

/** 系统用户（SysUserController /sysUser，sysuser 表）行 */
export interface SysUserRow {
  id: number
  code: string
  name: string
  password?: string
  remark?: string
  email?: string
  department: string
  position?: string
  cellphone?: string
  state: '启用' | '禁用'
}

/** 系统用户列表查询参数（POST /search 请求体，分页 1 基） */
export interface SysUserListParams {
  code?: string
  name?: string
  department?: string
  state?: string
  page?: number
  pageSize?: number
}

/** 系统用户列表返回 */
export interface SysUserListResult {
  content: SysUserRow[]
  total: number
}

/** 系统用户新增/修改入参 */
export interface SysUserSaveParams {
  id?: number
  code: string
  name: string
  department: string
  position?: string
  cellphone?: string
  email?: string
  remark?: string
  state: '启用' | '禁用'
}

/** 系统用户接口（SysUserController /sysUser） */
export const sysUserAPI = {
  /** 用户列表（服务端分页） */
  search: (params?: SysUserListParams) => http.post<SysUserListResult>('/api/sysUser/search', params),
  /** 按 id 查询 */
  get: (id: number) => http.get<SysUserRow>(`/api/sysUser/${id}`),
  /** 按工号查询 */
  getByCode: (code: string) => http.get<SysUserRow>(`/api/sysUser/code/${code}`),
  /** 新增用户 */
  add: (data: SysUserSaveParams) => http.post<SysUserRow>('/api/sysUser/save', data),
  /** 修改用户（路由统一为 PUT /update，id 由请求体携带） */
  update: (id: number, data: SysUserSaveParams) => http.put<SysUserRow>('/api/sysUser/update', { ...data, id }),
  /** 删除用户（按工号级联删除角色绑定） */
  remove: (code: string) => http.delete<string>(`/api/sysUser/code/${code}`),
}
