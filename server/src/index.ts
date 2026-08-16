import 'dotenv/config'
import Fastify from 'fastify'
import cors from '@fastify/cors'
import sensible from '@fastify/sensible'
import { ZodError } from 'zod'
import { userRoutes } from './routes/users.js'
import { authRoutes } from './routes/auth.js'
import { fail } from './utils/response.js'

const app = Fastify({
  logger: true,
})

await app.register(cors, {
  origin: true,
  credentials: true,
})

await app.register(sensible)

await app.register(userRoutes)
await app.register(authRoutes)

// 根路径健康检查
app.get('/api/health', async () => {
  return { code: 200, data: { status: 'ok' }, msg: 'success' }
})

// 统一错误处理：Zod 校验错误 -> 400
app.setErrorHandler((error, request, reply) => {
  if (error instanceof ZodError) {
    return reply.status(400).send(fail(400, error.issues.map(i => i.message).join('; ')))
  }
  app.log.error(error)
  if (error.statusCode && error.statusCode < 500) {
    return reply.status(error.statusCode).send(fail(error.statusCode, error.message))
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
