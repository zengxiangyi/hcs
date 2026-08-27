import http from './http'

/** 用户-角色关联行（按 code 关联） */
export interface RoleUserRow {
  roleCode: string
  userCode: string
}

/** 按角色 code 查询用户 code 列表 */
export interface RoleUserListResult {
  roleCode: string
  userCodes: string[]
}

/** 按用户 code 查询角色 code 列表 */
export interface UserRoleListResult {
  userCode: string
  roleCodes: string[]
}

/** 设置某角色的用户集合（全量替换，按 code 关联） */
export interface RoleUserSaveParams {
  roleCode: string
  userCodes: string[]
}

/** 用户-角色关联接口（基于 roleCode / userCode） */
export const roleUserAPI = {
  /** 查询某角色下的用户 code 列表 */
  listByRole: (roleCode: string) =>
    http.get<RoleUserListResult>('/api/sysRoleUser/role', { params: { roleCode } }),
  /** 查询某用户拥有的角色 code 列表 */
  listByUser: (userCode: string) =>
    http.get<UserRoleListResult>('/api/sysRoleUser/user', { params: { userCode } }),
  /** 保存某角色的用户集合（全量替换） */
  save: (data: RoleUserSaveParams) => http.post<null>('/api/sysRoleUser/save', data),
  /** 解除某用户在某角色下的关联 */
  remove: (roleCode: string, userCode: string) =>
    http.delete<null>('/api/sysRoleUser', { params: { roleCode, userCode } }),
}
