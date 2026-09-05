import http from './http'

/** 角色行（对齐后端 SysRole 实体 / sysrole 表） */
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

/** 角色列表查询参数（POST /search 请求体，分页 1 基） */
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

/** 角色接口（SysRoleController /sysRole） */
export const roleAPI = {
  /** 角色列表（服务端分页） */
  search: (params?: RoleListParams) => http.post<RoleListResult>('/api/sysRole/search', params),
  /** 按 id 查询 */
  get: (id: number) => http.get<RoleRow>(`/api/sysRole/${id}`),
  /** 按编码查询 */
  getByCode: (code: string) => http.get<RoleRow>(`/api/sysRole/code/${code}`),
  /** 按分类查询 */
  listByCategory: (category: string) => http.get<RoleRow[]>(`/api/sysRole/category/${category}`),
  /** 新增角色 */
  add: (data: RoleSaveParams) => http.post<RoleRow>('/api/sysRole/save', data),
  /** 修改角色（路由统一为 PUT /update，id 由请求体携带） */
  update: (id: number, data: RoleSaveParams) => http.put<RoleRow>('/api/sysRole/update', { ...data, id }),
  /** 删除角色（按编码级联删除权限/用户绑定） */
  remove: (code: string) => http.delete<string>(`/api/sysRole/code/${code}`),
}
