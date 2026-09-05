import http from './http'

/** 角色-权限关联行（对齐后端 SysRoleRight 实体 / sysroleright 表） */
export interface RoleRightRow {
  id: number
  roleCode: string
  rightCode: string
  remark?: string
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
  /** 查询某角色下的权限编码列表 */
  listByRole: (roleCode: string) =>
    http.get<RoleRightListResult>('/api/sysRoleRight/role', { params: { roleCode } }),
  /** 保存某角色的权限集合（逐条新增、自动去重） */
  save: (data: RoleRightSaveParams) => http.post<string>('/api/sysRoleRight/save', data),
}
