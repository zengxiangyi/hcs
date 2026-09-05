import http from './http'

/** 用户-角色关联行（对齐后端 SysRoleUser 实体 / sysroleuser 表） */
export interface RoleUserRow {
  id: number
  roleCode: string
  userCode: string
  remark?: string
}

/** 按角色 code 查询用户 code 列表（GET /role?roleCode=） */
export interface RoleUserListResult {
  roleCode: string
  userCodes?: string[]
}

/** 按用户 code 查询角色 code 列表（GET /user?userCode=） */
export interface UserRoleListResult {
  userCode: string
  roleCodes?: string[]
}

/** 设置某角色的用户集合（POST /save，后端逐条去重新增） */
export interface RoleUserSaveParams {
  roleCode: string
  userCodes: string[]
}

/** 用户-角色关联接口（SysRoleUserController /sysRoleUser） */
export const roleUserAPI = {
  /** 查询某角色下的用户 code 列表 */
  listByRole: (roleCode: string) =>
    http.get<RoleUserListResult>('/api/sysRoleUser/role', { params: { roleCode } }),
  /** 查询某用户拥有的角色 code 列表 */
  listByUser: (userCode: string) =>
    http.get<UserRoleListResult>('/api/sysRoleUser/user', { params: { userCode } }),
  /** 保存某角色的用户集合（逐条新增、自动去重） */
  save: (data: RoleUserSaveParams) => http.post<string>('/api/sysRoleUser/save', data),
}
