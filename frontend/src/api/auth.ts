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

/** 注册账号入参 */
export interface RegisterParams {
  username: string
  password: string
  cellphone: string
  email: string
}

export const baseAPI = {
  /** 登录 */
  login: (data: LoginParams): Promise<ApiResponse<LoginResult>> =>
    http.post<LoginResult>('/api/auth/login', data) as Promise<ApiResponse<LoginResult>>,

  /** 登出 */
  logout: (): Promise<ApiResponse<null>> =>
    http.post<null>('/api/auth/logout') as Promise<ApiResponse<null>>,

  /** 重置密码：验证手机号 + 邮箱是否匹配 */
  verifyIdentity: (data: VerifyIdentityParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/verify', data) as Promise<ApiResponse<string>>,

  /** 重置密码：验证通过后设置新密码 */
  resetPassword: (data: ResetPasswordParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/resetPassword', data) as Promise<ApiResponse<string>>,
  
  /** 注册新账号 */
  register: (data: RegisterParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/register', data) as Promise<ApiResponse<string>>,
}
