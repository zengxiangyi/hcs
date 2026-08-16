import type { FastifyInstance } from 'fastify'
import { z } from 'zod'
import { eq } from 'drizzle-orm'
import { db } from '../db/client.js'
import { accounts } from '../db/schema.js'
import { success, fail } from '../utils/response.js'

const loginSchema = z.object({
  username: z.string().min(1, '请输入用户名'),
  password: z.string().min(1, '请输入密码'),
})

export async function authRoutes(fastify: FastifyInstance) {
  // 登录
  fastify.post('/api/auth/login', async (request, reply) => {
    const body = loginSchema.parse(request.body)

    const account = await db
      .select()
      .from(accounts)
      .where(eq(accounts.username, body.username))
      .limit(1)

    if (!account.length || account[0].password !== body.password) {
      return reply.status(401).send(fail(401, '用户名或密码错误'))
    }

    // 简易 token（测试用）
    const token = Buffer.from(`${body.username}:${Date.now()}`).toString('base64')
    return success({
      token,
      user: { id: account[0].id, name: account[0].name, username: account[0].username },
    })
  })
}
