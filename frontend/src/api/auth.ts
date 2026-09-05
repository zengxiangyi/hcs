import http from './http'

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
  /** 无角色用户登录时后端不下发，可能为 null */
  rights?: string[]
  /** 无角色用户登录时后端不下发，可能为 null */
  roles?: string[]
}

/** 重置密码：身份验证入参（后端按 username + email + cellphone 全部校验） */
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
    http.post<LoginResult>('/api/auth/login', data),

  /** 重置密码第一步：验证用户名 + 邮箱 + 手机号是否匹配 */
  verifyIdentity: (data: VerifyIdentityParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/verify', data),

  /** 重置密码第二步：验证通过后设置新密码 */
  resetPassword: (data: ResetPasswordParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/resetPassword', data),

  /** 注册新账号 */
  register: (data: RegisterParams): Promise<ApiResponse<string>> =>
    http.post<string>('/api/auth/register', data),
}
