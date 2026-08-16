import http from './http'
import type { ApiResponse } from './http'

/** 用户信息 */
export interface UserInfo {
  id: string
  name: string
  avatar?: string
}

export const baseAPI = {
  /** 查询用户信息 */
  getUserInfo: () => http.get<ApiResponse<UserInfo>>('/api/user/info'),
}
