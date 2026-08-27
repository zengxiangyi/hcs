// 权限中心：保存当前用户的权限编码集合，并提供 hasRight 判定。
// 权限数据由后端在登录后下发（见 Login.vue 调用 setCurrentUser(rightCodes)），
// 不再依赖本地 mock 数据。

const STORAGE_KEY = 'rights'

let currentRightCodes: string[] = []

/** 设置当前用户的权限编码集合（登录/刷新权限时调用，rightCodes 来自后端） */
export function setCurrentUser(rightCodes: string[]): void {
  currentRightCodes = rightCodes ?? []
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(currentRightCodes))
  } catch {
    // 忽略存储异常（如隐私模式）
  }
}

/** 恢复本地持久化的权限编码集合（应用启动 / 刷新后调用） */
export function restoreCurrentUserRights(): void {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    currentRightCodes = raw ? (JSON.parse(raw) as string[]) : []
  } catch {
    currentRightCodes = []
  }
}

/** 判断当前用户是否拥有某权限编码（如 'btn:user:add' / 'page:user'） */
export function hasRight(code: string): boolean {
  return currentRightCodes.includes(code)
}
