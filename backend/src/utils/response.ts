/**
 * 统一响应结构 —— 与前端 src/api/http.ts 的 ApiResponse 契约保持一致：
 * { code: number; data: T; msg: string }，成功 code === 200
 */

export interface ApiResponse<T = unknown> {
  code: number
  data: T
  msg: string
}

export function success<T>(data: T, msg = 'success'): ApiResponse<T> {
  return { code: 200, data, msg }
}

export function fail(code: number, msg: string): ApiResponse<null> {
  return { code, data: null, msg }
}
