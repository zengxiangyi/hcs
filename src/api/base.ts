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

/** 重置密码：身份验证入参（手机号 + 邮箱） */
export interface VerifyIdentityParams {
  cellphone: string
  email: string
}

/** 重置密码：设置新密码入参 */
export interface ResetPasswordParams extends VerifyIdentityParams {
  newPassword: string
}

export const baseAPI = {
  /** 登录 */
  login: (data: LoginParams): Promise<ApiResponse<LoginResult>> =>
    http.post<LoginResult>('/api/auth/login', data) as Promise<ApiResponse<LoginResult>>,
  /** 查询用户信息 */
  getUserInfo: (): Promise<ApiResponse<UserInfo>> =>
    http.get<UserInfo>('/api/user/info') as Promise<ApiResponse<UserInfo>>,
  /** 重置密码：验证手机号 + 邮箱是否匹配 */
  verifyIdentity: (data: VerifyIdentityParams): Promise<ApiResponse<null>> =>
    http.post<null>('/api/auth/verify-identity', data) as Promise<ApiResponse<null>>,
  /** 重置密码：验证通过后设置新密码 */
  resetPassword: (data: ResetPasswordParams): Promise<ApiResponse<null>> =>
    http.post<null>('/api/auth/reset-password', data) as Promise<ApiResponse<null>>,
}
