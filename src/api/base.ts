import http from './http'
import type { ApiResponse } from './http'

/** 用户信息 */
export interface UserInfo {
  id: number
  name: string
  username?: string
}

/** 登录入参 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录返回 */
export interface LoginResult {
  token: string
  user: UserInfo
}

export const baseAPI = {
  /** 登录 */
  login: (data: LoginParams): Promise<ApiResponse<LoginResult>> =>
    http.post<LoginResult>('/api/auth/login', data) as Promise<ApiResponse<LoginResult>>,
  /** 查询用户信息 */
  getUserInfo: (): Promise<ApiResponse<UserInfo>> =>
    http.get<UserInfo>('/api/user/info') as Promise<ApiResponse<UserInfo>>,
}
