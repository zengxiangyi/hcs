import http from './http'

/** 用户数据行（对齐 data2 表格列） */
export interface UserRow {
  id: number
  name: string
  role: string
  dept: string
  status: string
  createTime: string
}

/** 用户列表查询参数 */
export interface UserListParams {
  keyword?: string
  dept?: string
  status?: string
  page?: number
  pageSize?: number
}

/** 用户列表返回 */
export interface UserListResult {
  list: UserRow[]
  total: number
  page: number
  pageSize: number
}

/** 用户新增/修改入参 */
export interface UserSaveParams {
  id?: number
  name: string
  role: string
  dept: string
  status: string
}

export const dataAPI = {
  /** 用户列表 */
  getUsers: (params?: UserListParams) => http.get<UserListResult>('/api/users', { params }),
  /** 新增用户 */
  addUser: (data: UserSaveParams) => http.post<UserRow>('/api/users', data),
  /** 修改用户 */
  updateUser: (id: number, data: UserSaveParams) => http.put<UserRow>(`/api/users/${id}`, data),
  /** 删除用户 */
  deleteUser: (id: number) => http.delete<null>(`/api/users/${id}`),
}
