import http from './http'

/** 角色行（对齐 mock.ts 的 Role 结构） */
export interface RoleRow {
  id: number
  /** 名称 name */
  name: string
  /** 编码 code */
  code: string
  /** 分类 category（系统 / 业务） */
  category: string
  /** 备注 remark */
  remark: string
}

/** 角色列表查询参数 */
export interface RoleListParams {
  name?: string
  code?: string
  category?: string
  page?: number
  pageSize?: number
}

/** 角色列表返回 */
export interface RoleListResult {
  content: RoleRow[]
  total: number
  page: number
  pageSize: number
}

/** 角色新增/修改入参 */
export interface RoleSaveParams {
  id?: number
  name: string
  code: string
  category: string
  remark?: string
}

/** 角色接口 */
export const roleAPI = {
  /** 角色列表（服务端分页） */
  list: (params?: RoleListParams) => http.get<RoleListResult>('/api/sysRole/list', { params }),
  /** 新增角色 */
  add: (data: RoleSaveParams) => http.post<RoleRow>('/api/sysRole/save', data),
  /** 修改角色 */
  update: (id: number, data: RoleSaveParams) => http.put<RoleRow>(`/api/sysRole/${id}`, data),
  /** 删除角色 */
  remove: (id: number) => http.delete<null>(`/api/sysRole/${id}`),
}
