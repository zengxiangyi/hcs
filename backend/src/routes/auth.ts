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

/** 注册账号入参：账号 + 密码 + 身份信息（用于日后忘记密码验证） */
const registerSchema = z.object({
  username: z.string().min(1, '请输入用户名'),
  password: z.string().min(6, '密码至少 6 位'),
  cellphone: z.string().regex(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: z.string().email('邮箱格式不正确'),
})

/** 忘记密码身份验证公共入参（手机号 + 邮箱） */
const contactSchema = z.object({
  cellphone: z.string().regex(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: z.string().email('邮箱格式不正确'),
})

/** 重置密码：身份验证入参 */
const verifyIdentitySchema = contactSchema

/** 重置密码：设置新密码入参（在身份验证入参基础上扩展新密码） */
const resetPasswordSchema = contactSchema.extend({
  newPassword: z.string().min(6, '新密码至少 6 位'),
})

/**
 * 按手机号 + 邮箱查找账号 —— verify-identity 与 reset-password 共用的身份验证查询。
 * 只需取 id：既可用于判断账号是否存在，也可供 reset-password 后续按 id 更新。
 */
async function findAccountByContact(cellphone: string, email: string) {
  return db
    .select({ id: accounts.id })
    .from(accounts)
    .where(and(eq(accounts.cellphone, cellphone), eq(accounts.email, email)))
    .limit(1)
}

export async function authRoutes(fastify: FastifyInstance) {
  // 注册新账号
  fastify.post('/api/auth/register', async (request, reply) => {
    const body = registerSchema.parse(request.body)

    // 用户名唯一性校验：已存在则拒绝注册
    const exist = await db
      .select({ id: accounts.id })
      .from(accounts)
      .where(eq(accounts.username, body.username))
      .limit(1)

    if (exist.length) {
      return reply.status(400).send(fail(400, '用户名已存在'))
    }

    await db.insert(accounts).values({
      username: body.username,
      password: body.password,
      name: body.username, // 前端注册未传昵称，默认取用户名
      cellphone: body.cellphone,
      email: body.email,
    })
    return success(null, '注册成功')
  })

  // 登录
  fastify.post('/api/auth/login', async (request, reply) => {
    const body = loginSchema.parse(request.body)

    const account = await db
      .select()
      .from(accounts)
      .where(eq(accounts.username, body.username))
      .limit(1)

    if (!account.length || account[0].password !== body.password) {
      // 登录失败属于业务错误，用 400 而非 401（401 在前端被定义为 token 失效，会触发整页跳转）
      return reply.status(400).send(fail(400, '用户名或密码错误'))
    }

    // 签发 JWT：用户信息写入 token，有效期 8 小时（见 index.ts 注册时 sign.expiresIn 配置）
    const token = await reply.jwtSign({
      id: account[0].id,
      name: account[0].name,
      username: account[0].username,
    })
    return success({
      token,
      user: { id: account[0].id, name: account[0].name, username: account[0].username },
    })
  })

  // 登出：JWT 为无状态 token，后端无需维护黑名单，仅作前端登出动作确认；
  // 真正的凭证清理由前端删除本地 token 完成
  fastify.post('/api/auth/logout', async () => {
    return success(null, '退出成功')
  })

  // 重置密码：验证手机号 + 邮箱是否匹配
  fastify.post('/api/auth/verify-identity', async (request, reply) => {
    const body = verifyIdentitySchema.parse(request.body)

    const account = await findAccountByContact(body.cellphone, body.email)

    if (!account.length) {
      return reply.status(400).send(fail(400, '验证信息错误'))
    }
    return success(null, '验证成功')
  })

  // 重置密码：身份验证通过后更新密码
  fastify.post('/api/auth/reset-password', async (request, reply) => {
    const body = resetPasswordSchema.parse(request.body)

    const account = await findAccountByContact(body.cellphone, body.email)

    if (!account.length) {
      return reply.status(400).send(fail(400, '验证信息错误'))
    }

    await db.update(accounts).set({ password: body.newPassword }).where(eq(accounts.id, account[0].id))
    return success(null, '密码重置成功')
  })
}
