import http from './http'

/** 角色-权限关联行（按 roleCode / rightCode 关联） */
export interface RoleRightRow {
  roleCode: string
  rightCode: string
}

/** 按角色 code 查询权限编码列表 */
export interface RoleRightListResult {
  roleCode: string
  rightCodes: string[]
}

/** 设置某角色的权限集合（全量替换，按 code 关联） */
export interface RoleRightSaveParams {
  roleCode: string
  rightCodes: string[]
}

/** 角色-权限关联接口（基于 roleCode / rightCode） */
export const roleRightAPI = {
  /** 查询某角色下的权限编码列表 */
  list: (roleCode: string) =>
    http.get<RoleRightListResult>('/api/sysRoleRight/role', { params: { roleCode } }),
  /** 保存某角色的权限集合（全量替换） */
  save: (data: RoleRightSaveParams) => http.post<null>('/api/sysRoleRight/save', data),
  /** 解除某角色下某权限的关联 */
  remove: (roleCode: string, rightCode: string) =>
    http.delete<null>('/api/sysRoleRight', { params: { roleCode, rightCode } }),
}
