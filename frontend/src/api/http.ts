/**
 * Axios HTTP 客户端 — 统一请求/响应拦截
 */
import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'

/** 后端统一响应包装结构（按实际后端约定调整） */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  message: string
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 300000,
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
      return Promise.reject(new Error(res.message || '业务处理失败'))
    }
    return res as unknown as AxiosResponse
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // token 失效，跳转登录并清理本地凭证
      localStorage.removeItem('token')
      // BASE_URL 跟随部署路径（dev 与生产均为 /）
      const home = import.meta.env.BASE_URL || '/'
      if (window.location.pathname !== home) {
        window.location.href = home
      }
    }

    // HTTP 非 2xx 时解包后端业务错误信息（body 为 { code, msg }），
    // 使调用处能通过 err.message 拿到「验证信息错误」等业务提示
    const res = error.response?.data as ApiResponse | undefined
    if (res && typeof res.code === 'number' && res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return Promise.reject(error)
  }
)

/**
 * 类型化封装：因响应拦截器已把 AxiosResponse 解包为 ApiResponse<T>，
 * 故各方法直接返回 Promise<ApiResponse<T>>（T 为业务 data 类型）。
 * 调用处通过 res.data 取业务数据。
 */
export interface TypedHttp {
  get: <T>(url: string, config?: Record<string, unknown>) => Promise<ApiResponse<T>>
  post: <T>(url: string, data?: unknown, config?: Record<string, unknown>) => Promise<ApiResponse<T>>
  put: <T>(url: string, data?: unknown, config?: Record<string, unknown>) => Promise<ApiResponse<T>>
  delete: <T>(url: string, config?: Record<string, unknown>) => Promise<ApiResponse<T>>
}

export default http as unknown as TypedHttp
