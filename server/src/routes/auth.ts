import type { FastifyInstance } from 'fastify'
import { z } from 'zod'
import { and, eq } from 'drizzle-orm'
import { db } from '../db/client.js'
import { accounts } from '../db/schema.js'
import { success, fail } from '../utils/response.js'

const loginSchema = z.object({
  username: z.string().min(1, '请输入用户名'),
  password: z.string().min(1, '请输入密码'),
})

/** 重置密码：身份验证入参（手机号 + 邮箱） */
const verifyIdentitySchema = z.object({
  cellphone: z.string().regex(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: z.string().email('邮箱格式不正确'),
})

/** 重置密码：设置新密码入参 */
const resetPasswordSchema = z.object({
  cellphone: z.string().regex(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: z.string().email('邮箱格式不正确'),
  newPassword: z.string().min(6, '新密码至少 6 位'),
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

  // 重置密码：验证手机号 + 邮箱是否匹配
  fastify.post('/api/auth/verify-identity', async (request, reply) => {
    const body = verifyIdentitySchema.parse(request.body)

    const account = await db
      .select({ id: accounts.id })
      .from(accounts)
      .where(and(eq(accounts.cellphone, body.cellphone), eq(accounts.email, body.email)))
      .limit(1)

    if (!account.length) {
      return reply.status(400).send(fail(400, '验证信息错误'))
    }
    return success(null, '验证成功')
  })

  // 重置密码：身份验证通过后更新密码
  fastify.post('/api/auth/reset-password', async (request, reply) => {
    const body = resetPasswordSchema.parse(request.body)

    const account = await db
      .select({ id: accounts.id })
      .from(accounts)
      .where(and(eq(accounts.cellphone, body.cellphone), eq(accounts.email, body.email)))
      .limit(1)

    if (!account.length) {
      return reply.status(400).send(fail(400, '验证信息错误'))
    }

    await db.update(accounts).set({ password: body.newPassword }).where(eq(accounts.id, account[0].id))
    return success(null, '密码重置成功')
  })
}
