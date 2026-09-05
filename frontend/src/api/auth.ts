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

/** 登录返回（对齐后端 LoginOut：token + 用户信息 + 角色/权限编码列表） */
export interface LoginResult {
  token: string
  user: UserInfo
  rights: string[]
  roles: string[]
}


/** 重置密码：身份验证入参（后端按 username + email + cellphone 校验） */
export interface VerifyIdentityParams {
  username: string
  cellphone: string
  email: string
}

/** 重置密码：设置新密码入参（后端只收 username + password） */
export interface ResetPasswordParams {
  username: string
  password: string
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

  /** 重置密码：验证用户名 + 邮箱 + 手机号是否匹配 */
  verifyIdentity: (data: VerifyIdentityParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/verify', data) as Promise<ApiResponse<string>>,

  /** 重置密码：验证通过后设置新密码 */
  resetPassword: (data: ResetPasswordParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/resetPassword', data) as Promise<ApiResponse<string>>,
  
  /** 注册新账号 */
  register: (data: RegisterParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/register', data) as Promise<ApiResponse<string>>,
}
