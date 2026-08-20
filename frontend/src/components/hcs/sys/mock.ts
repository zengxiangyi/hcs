/**
 * 用户-角色-权限 管理系统的本地 mock 数据源。
 * 仅用于前端演示，数据保存在内存中（刷新后重置）。
 * 真实接入后端时，将下列导出替换为 API 调用即可。
 */

// ---------- 类型定义 ----------
export interface User {
  id: number
  username: string
  nickname: string
  dept: string
  state: '启用' | '禁用'
}

export interface Role {
  id: number
  name: string
  code: string
  remark: string
}

/** 权限类型：page=页面查看权限，btn=按钮操作权限 */
export type RightType = 'page' | 'btn'

export interface Right {
  id: number
  /** 权限标识，如 page:data2 / btn:user:add */
  code: string
  name: string
  type: RightType
  /** 所属模块（用于分组展示），如 系统管理 / 数据管理 */
  module: string
}

// ---------- 模拟数据 ----------

export const users: User[] = [
  { id: 1, username: 'admin', nickname: '超级管理员', dept: '技术部', state: '启用' },
  { id: 2, username: 'zhangsan', nickname: '张三', dept: '内容部', state: '启用' },
  { id: 3, username: 'lisi', nickname: '李四', dept: '市场部', state: '启用' },
  { id: 4, username: 'wangwu', nickname: '王五', dept: '设计部', state: '禁用' },
]

export const roles: Role[] = [
  { id: 1, name: '管理员', code: 'admin', remark: '拥有全部权限' },
  { id: 2, name: '数据录入员', code: 'editor', remark: '可查看与编辑业务数据' },
  { id: 3, name: '访客', code: 'guest', remark: '仅可查看，无操作权限' },
]

export const rights: Right[] = [
  // 系统管理 - 页面
  { id: 1, code: 'page:user', name: '用户管理页面', type: 'page', module: '系统管理' },
  { id: 2, code: 'page:role', name: '角色管理页面', type: 'page', module: '系统管理' },
  { id: 3, code: 'page:right', name: '权限管理页面', type: 'page', module: '系统管理' },
  { id: 4, code: 'page:roleUser', name: '角色用户关联页面', type: 'page', module: '系统管理' },
  { id: 5, code: 'page:roleRight', name: '角色权限关联页面', type: 'page', module: '系统管理' },
  // 系统管理 - 按钮
  { id: 6, code: 'btn:user:add', name: '用户-新增', type: 'btn', module: '系统管理' },
  { id: 7, code: 'btn:user:edit', name: '用户-编辑', type: 'btn', module: '系统管理' },
  { id: 8, code: 'btn:user:delete', name: '用户-删除', type: 'btn', module: '系统管理' },
  { id: 9, code: 'btn:role:add', name: '角色-新增', type: 'btn', module: '系统管理' },
  { id: 10, code: 'btn:role:edit', name: '角色-编辑', type: 'btn', module: '系统管理' },
  { id: 11, code: 'btn:role:delete', name: '角色-删除', type: 'btn', module: '系统管理' },
  // 业务数据 - 页面与按钮
  { id: 12, code: 'page:data2', name: '蓝本查看页面', type: 'page', module: '业务数据' },
  { id: 13, code: 'btn:data2:export', name: '蓝本-导出', type: 'btn', module: '业务数据' },
  { id: 14, code: 'btn:data2:import', name: '蓝本-导入', type: 'btn', module: '业务数据' },
]

/** 用户 -> 角色 关联：userId -> roleId 列表 */
export const userRoleMap: Record<number, number[]> = {
  1: [1],
  2: [2],
  3: [3],
  4: [3],
}

/** 角色 -> 权限 关联：roleId -> rightId 列表 */
export const roleRightMap: Record<number, number[]> = {
  1: rights.map((r) => r.id), // 管理员拥有全部权限
  2: [12, 13, 14, 6, 7], // 录入员：可看数据并可导入导出、可操作用户
  3: [1, 2, 3, 4, 5, 12], // 访客：仅可看页面
}

// ---------- 工具：根据关联表计算能力 ----------

/** 取某用户所拥有的角色 id 列表 */
export function getRoleIdsByUser(userId: number): number[] {
  return userRoleMap[userId] ?? []
}

/** 取某角色所拥有的权限 code 列表 */
export function getRightCodesByRole(roleId: number): string[] {
  const ids = roleRightMap[roleId] ?? []
  return ids.map((id) => rights.find((r) => r.id === id)?.code).filter(Boolean) as string[]
}

/** 取某用户拥有的全部权限 code（去重合并其所有角色） */
export function getRightCodesByUser(userId: number): string[] {
  const roleIds = getRoleIdsByUser(userId)
  const set = new Set<string>()
  roleIds.forEach((rid) => getRightCodesByRole(rid).forEach((c) => set.add(c)))
  return [...set]
}

/** 简易自增 id 生成器 */
let _uid = 100
export function nextId(): number {
  return ++_uid
}
