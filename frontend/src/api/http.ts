/**
 * Axios HTTP 客户端 — 统一请求/响应拦截
 */
import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'

/** 后端统一响应包装结构（对齐 com.baogang.info.common.ApiResponse） */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  message: string
}

/** 后端统一分页返回结构（对齐 com.baogang.info.common.PageResult，page 为 1 基页码） */
export interface PageResult<T> {
  content: T[]
  total: number
  page: number
  size: number
}

const http = axios.create({
  // ⚠ 注意：src/api/*.ts 中的路径已自带 /api 前缀（如 '/api/auth/login'），
  // 与后端 Spring Boot 的路由定义一一对应。因此这里只能配「后端 origin 根」
  // （如 http://<后端IP>:8080 或同源时的 '/'），不能再带 /api，否则会拼成 /api/api/...。
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

/** 401 跳转登录页的去重标记：并发请求同时 401 时只跳转一次 */
let loginRedirecting = false

/**
 * token 失效时**无刷新**跳登录页。
 *
 * 不能再用 window.location.href 整页跳转：那会让浏览器重新下载并执行整个应用，
 * dev 模式下是几百个未打包的 ES module 逐个请求，期间页面全白，
 * 表现就是「打开页面后一片空白、等几秒才出登录页」。
 *
 * 这里动态引入 router：避免 http.ts 与 router 模块形成静态循环依赖
 * （router 的子页面会反向 import api/*.ts → http.ts）。
 */
function redirectToLogin(): void {
  if (loginRedirecting) return
  loginRedirecting = true
  void import('../router')
    .then(({ default: router }) => {
      const current = router.currentRoute.value
      if (current.name === 'Login') return
      return router.replace({ name: 'Login', query: { redirect: current.fullPath } })
    })
    .catch(() => {})
    .finally(() => {
      loginRedirecting = false
    })
}

// 响应拦截：统一解包 + 错误处理
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 业务码非 200 视为失败（HTTP 200 + body code 400 的 Auth/绑定类业务校验走此通道）
    if (res && res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.message || '业务处理失败'))
    }
    return res as unknown as AxiosResponse
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // token 失效，清理本地凭证并跳登录页（无刷新跳转，见 redirectToLogin 注释）
      localStorage.removeItem('token')
      redirectToLogin()
    }

    // HTTP 非 2xx 时解包后端业务错误信息（body 为 { code, message }），
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
interface TypedHttp {
  get: <T>(url: string, config?: AxiosRequestConfig) => Promise<ApiResponse<T>>
  post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) => Promise<ApiResponse<T>>
  put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig) => Promise<ApiResponse<T>>
  delete: <T>(url: string, config?: AxiosRequestConfig) => Promise<ApiResponse<T>>
}

export default http as unknown as TypedHttp
