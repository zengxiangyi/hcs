import http from './http'

/** 角色-权限关联行（对齐后端 SysRoleRight 实体 / sysroleright 表） */
export interface RoleRightRow {
  id: number
  roleCode: string
  rightCode: string
}

/** 按角色 code 查询权限编码列表（GET /role?roleCode=） */
export interface RoleRightListResult {
  roleCode: string
  rightCodes?: string[]
}

/** 设置某角色的权限集合（POST /save，后端逐条去重新增） */
export interface RoleRightSaveParams {
  roleCode: string
  rightCodes: string[]
}

/** 角色-权限关联接口（SysRoleRightController /sysRoleRight） */
export const roleRightAPI = {
  /** 分页列表（1 基 page） */
  list: (page = 1, size = 10) =>
    http.get<{ content: RoleRightRow[]; total: number; page: number; pageSize: number }>(
      `/api/sysRoleRight/list?page=${page}&size=${size}`
    ),
  /** 查询某角色下的权限编码列表 */
  listByRole: (roleCode: string) =>
    http.get<RoleRightListResult>('/api/sysRoleRight/role', { params: { roleCode } }),
  /** 按权限编码查询关联了哪些角色 */
  listByRight: (rightCode: string) =>
    http.get<RoleRightRow[]>(`/api/sysRoleRight/right/${rightCode}`),
  /** 保存某角色的权限集合（逐条新增、自动去重） */
  save: (data: RoleRightSaveParams) => http.post<string>('/api/sysRoleRight/save', data),
  /** 修改单条关联（id 必填） */
  update: (data: RoleRightRow) => http.put<RoleRightRow>('/api/sysRoleRight/update', data),
  /** 删除单条关联（按 id） */
  remove: (id: number) => http.delete<null>(`/api/sysRoleRight/${id}`),
}
