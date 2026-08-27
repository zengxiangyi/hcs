import http from './http'

/** 用户数据行 */
export interface UserRow {
  id: number
  code: string
  name: string
  department: string
  position: string
  cellphone: string
  email: string
  remark: string
  state: string
  createTime: string
}

/** 用户列表查询参数 */
export interface UserListParams {
  userName?: string
  roleName?: string
  department?: string
  state?: string
  page?: number
  pageSize?: number
}

/** 用户列表返回 */
export interface UserListResult {
  content: UserRow[]
  total: number
  page: number
  pageSize: number
}

/** 用户新增/修改入参 */
export interface UserSaveParams {
  id?: number
  userName: string
  roleName: string
  department: string
  state: string
}

export const dataAPI = {
  /** 用户列表 */
  getUsers: (params?: UserListParams) => http.get<UserListResult>('/api/sysUser/list', { params }),
  /** 新增用户 */
  addUser: (data: UserSaveParams) => http.post<UserRow>('/api/sysUser/save', data),
  /** 修改用户 */
  updateUser: (id: number, data: UserSaveParams) => http.put<UserRow>(`/api/sysUser/${id}`, data),
  /** 删除用户 */
  deleteUser: (id: number) => http.delete<null>(`/api/sysUser/${id}`),
}

/** 系统用户（SysUser 表）行 */
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

/** 系统用户列表查询参数 */
export interface SysUserListParams {
  code?: string
  name?: string
  department?: string
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

/** 系统用户接口（后端已开发完毕） */
export const sysUserAPI = {
  /** 用户列表（服务端分页） */
  search: (params?: SysUserListParams) => http.post<SysUserListResult>('/api/sysUser/search', params),
  /** 新增用户 */
  add: (data: SysUserSaveParams) => http.post<SysUserRow>('/api/sysUser/save', data),
  /** 修改用户 */
  update: (id: number, data: SysUserSaveParams) => http.put<SysUserRow>(`/api/sysUser/${id}`, data),
  /** 删除用户 */
  remove: (id: number) => http.delete<null>(`/api/sysUser/${id}`),
}
