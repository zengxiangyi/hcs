import http from './http'
import type { PageResult } from './http'

/** 系统用户（SysUserController /sysUser，sysuser 表）行 */
export interface SysUserRow {
  id: number
  code: string
  name: string
  remark?: string
  email?: string
  department: string
  position?: string
  cellphone?: string
  /** 注册入口后端写 "A"，与「启用/禁用」并存，故按 string 接收（保存入参仍受控） */
  state: string
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
export type SysUserListResult = PageResult<SysUserRow>

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
  /** 新增用户 */
  add: (data: SysUserSaveParams) => http.post<SysUserRow>('/api/sysUser/save', data),
  /** 修改用户（路由统一为 PUT /update，id 由请求体携带） */
  update: (id: number, data: SysUserSaveParams) => http.put<SysUserRow>('/api/sysUser/update', { ...data, id }),
  /** 删除用户（按工号级联删除角色绑定） */
  remove: (code: string) => http.delete<string>(`/api/sysUser/code/${encodeURIComponent(code)}`),
}
