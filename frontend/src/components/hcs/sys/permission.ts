/**
 * 权限控制中心：维护"当前登录用户"的权限集合，提供页面/按钮级权限判断。
 * 由 Login.vue 登录成功后调用 setCurrentUserRights() 写入；
 * 菜单过滤、路由守卫、按钮指令均依赖此处的权限集合。
 */
import { getRightCodesByUser, users } from './mock'

const STORAGE_KEY = 'current_user_rights'

/** 当前用户拥有的权限 code 列表（page:* / btn:*） */
let currentRightCodes: string[] = []

/** 当前登录用户 id（mock 场景，真实项目应由后端返回并缓存） */
let currentUserId = 0

export function setCurrentUser(userId: number) {
  currentUserId = userId
  currentRightCodes = getRightCodesByUser(userId)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(currentRightCodes))
}

/** 应用启动时尝试从缓存恢复（刷新页面后菜单/按钮权限不丢失） */
export function restoreCurrentUserRights() {
  const cached = localStorage.getItem(STORAGE_KEY)
  if (cached) {
    try {
      currentRightCodes = JSON.parse(cached)
    } catch {
      currentRightCodes = []
    }
  }
}

/** 判断当前用户是否拥有某个权限 code */
export function hasRight(code: string): boolean {
  return currentRightCodes.includes(code)
}

/** 判断当前用户是否拥有全部给定权限 code（用于按钮组合权限） */
export function hasAllRights(codes: string[]): boolean {
  return codes.every((c) => currentRightCodes.includes(c))
}

/** 取当前用户权限集合（主要用于调试/菜单过滤） */
export function getCurrentRightCodes(): string[] {
  return [...currentRightCodes]
}

export function getCurrentUserId(): number {
  return currentUserId
}

/** 当前用户是否为超级管理员（拥有 page:* 任一即视为可读全部页面，此处按是否含管理员角色处理） */
export function isAdmin(): boolean {
  const u = users.find((x) => x.id === currentUserId)
  return u?.username === 'admin'
}
