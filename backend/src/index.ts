import 'dotenv/config'
import Fastify, { FastifyError } from 'fastify'
import cors from '@fastify/cors'
import jwt from '@fastify/jwt'
import { ZodError } from 'zod'
import { userRoutes } from './routes/users.js'
import { authRoutes } from './routes/auth.js'
import { fail, success } from './utils/response.js'

const app = Fastify({
  logger: true,
})

await app.register(cors, {
  origin: true,
  credentials: true,
})

// JWT 插件：登录后签发 token（含用户信息），有效期 8 小时
await app.register(jwt, {
  secret: process.env.JWT_SECRET || 'dev-secret-change-me',
  sign: { expiresIn: '8h' },
})

// 无需 token 的健康检查（服务探活）
const PUBLIC_PATHS = ['/api/health']

// 全局鉴权钩子：除登录相关接口（/api/auth/*）和健康检查外，其余请求必须携带有效 JWT
app.addHook('preHandler', async (request, reply) => {
  const path = request.url.split('?')[0]
  // 登录相关接口（login / verify-identity / reset-password 及后续新增的 auth 接口）不校验 token
  if (path.startsWith('/api/auth/') || PUBLIC_PATHS.includes(path)) return

  try {
    await request.jwtVerify()
  } catch {
    // 401 在前端被约定为 token 失效，会清理本地凭证并跳转登录页
    return reply.code(401).send(fail(401, '登录已过期，请重新登录'))
  }
})

await app.register(userRoutes)
await app.register(authRoutes)

// 根路径健康检查 —— 与统一响应结构保持一致
app.get('/api/health', async () => {
  return success({ status: 'ok' })
})

// 统一错误处理：Zod 校验错误 -> 400
app.setErrorHandler<FastifyError>((error, request, reply) => {
  if (error instanceof ZodError) {
    return reply.status(400).send(fail(400, error.issues.map(i => i.message).join('; ')))
  }
  app.log.error(error)
  // error 为 FastifyError，自带可选 statusCode；<500 的错误按原状态码透传，其余统一 500
  const statusCode = error.statusCode
  if (statusCode && statusCode < 500) {
    return reply.status(statusCode).send(fail(statusCode, error.message))
  }
  return reply.status(500).send(fail(500, '服务器内部错误'))
})

const port = Number(process.env.PORT) || 8080

try {
  await app.listen({ port, host: '0.0.0.0' })
  app.log.info(`Server running at http://localhost:${port}`)
} catch (err) {
  app.log.error(err)
  process.exit(1)
}
