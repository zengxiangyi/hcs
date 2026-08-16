/**
 * Axios HTTP 客户端 — 统一请求/响应拦截
 */
import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'

/** 后端统一响应包装结构（按实际后端约定调整） */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  msg: string
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截：注入 token
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// 响应拦截：统一解包 + 错误处理
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 按后端约定判断业务码，此处以 code === 200 为例
    if (res && res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.msg || '业务处理失败'))
    }
    return res as unknown as AxiosResponse
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // token 失效，跳转登录并清理本地凭证
      localStorage.removeItem('token')
      if (window.location.pathname !== '/') {
        window.location.href = '/'
      }
    }
    return Promise.reject(error)
  }
)

export default http
