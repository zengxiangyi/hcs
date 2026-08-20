/**
 * @fastify/jwt 声明合并：定义 JWT payload 中保存的登录用户信息。
 * 启用后 `request.user` 将获得该类型。
 */
declare module '@fastify/jwt' {
  /** 登录用户信息 —— payload 与 user 共用同一结构 */
  interface AuthUser {
    /** 账号 ID */
    id: number
    /** 姓名 */
    name: string | null
    /** 用户名 */
    username: string
  }

  interface FastifyJWT {
    payload: AuthUser
    user: AuthUser
  }
}

export {}
