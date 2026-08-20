import type { FastifyInstance, FastifyReply } from 'fastify'
import { count, eq, and, like } from 'drizzle-orm'
import type { SQL } from 'drizzle-orm'
import { z } from 'zod'
import { db } from '../db/client.js'
import { users } from '../db/schema.js'
import { success, fail } from '../utils/response.js'

/**
 * 解析路由 /:id 参数为整数；非法（NaN/负数）时直接返 400 并回 null。
 * 路由参数在 Zod 无法覆盖的层级，需在此显式校验，避免 NaN 进入 SQL。
 */
async function parseIdOr400(reply: FastifyReply, raw: unknown): Promise<number | null> {
  const id = Number(raw)
  if (!Number.isInteger(id) || id <= 0) {
    reply.status(400).send(fail(400, '无效的用户 ID'))
    return null
  }
  return id
}

const formatNow = () => {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const listQuerySchema = z.object({
  userName: z.string().optional().default(''),
  roleName: z.string().optional().default(''),
  department: z.string().optional().default(''),
  state: z.string().optional().default(''),
  page: z.coerce.number().int().min(1).optional().default(1),
  pageSize: z.coerce.number().int().min(1).optional().default(10),
})

const saveSchema = z.object({
  id: z.number().int().optional(),
  userName: z.string().min(1, '请输入姓名'),
  roleName: z.string().min(1, '请输入角色'),
  department: z.string().min(1, '请选择部门'),
  state: z.string().min(1, '请选择状态'),
})

/**
 * 按主键查询用户；不存在时已通过 reply 返回 404，返回 null。
 * 供 PUT/DELETE 复用「查存在 → 404」逻辑。
 */
async function findUserOr404(
  reply: FastifyReply,
  id: number,
): Promise<typeof users.$inferSelect | null> {
  const existing = await db.select().from(users).where(eq(users.id, id)).limit(1)
  if (!existing.length) {
    reply.status(404).send(fail(404, '用户不存在'))
    return null
  }
  return existing[0]
}

export async function userRoutes(fastify: FastifyInstance) {
  // 用户列表（含关键字 / 部门 / 状态 过滤 + 分页）
  fastify.get('/api/users', async (request) => {
    const query = listQuerySchema.parse(request.query)
    const { userName, roleName, department, state, page, pageSize } = query

    const conditions: SQL[] = []
    if (userName) conditions.push(like(users.userName, `%${userName}%`))
    if (roleName) conditions.push(like(users.roleName, `%${roleName}%`))
    if (department) conditions.push(eq(users.department, department))
    if (state) conditions.push(eq(users.state, state))

    const where = conditions.length ? and(...conditions) : undefined

    const [totalRow] = await db
      .select({ value: count() })
      .from(users)
      .where(where)
    const total = totalRow?.value ?? 0

    const list = await db
      .select()
      .from(users)
      .where(where)
      .limit(pageSize)
      .offset((page - 1) * pageSize)

    return success({ list, total, page, pageSize })
  })

  // 查询单个用户信息（对齐前端 baseAPI.getUserInfo）
  fastify.get('/api/user/info', async (request, reply) => {
    const first = await db.select().from(users).limit(1)
    if (!first.length) {
      return reply.status(404).send(fail(404, '暂无用户数据'))
    }
    return success(first[0])
  })

  // 新增用户
  fastify.post('/api/users', async (request) => {
    const body = saveSchema.parse(request.body)
    const [inserted] = await db
      .insert(users)
      .values({
        userName: body.userName,
        roleName: body.roleName,
        department: body.department,
        state: body.state,
        createTime: formatNow(),
      })
      .$returningId()
    const row = await db.select().from(users).where(eq(users.id, inserted.id))
    return success(row[0], '新增成功')
  })

  // 修改用户
  fastify.put('/api/users/:id', async (request, reply) => {
    const uid = await parseIdOr400(reply, (request.params as { id: string }).id)
    if (uid === null) return
    const existing = await findUserOr404(reply, uid)
    if (!existing) return

    const body = saveSchema.parse(request.body)
    await db
      .update(users)
      .set({
        userName: body.userName,
        roleName: body.roleName,
        department: body.department,
        state: body.state,
      })
      .where(eq(users.id, uid))

    const row = await db.select().from(users).where(eq(users.id, uid))
    return success(row[0], '修改成功')
  })

  // 删除用户
  fastify.delete('/api/users/:id', async (request, reply) => {
    const uid = await parseIdOr400(reply, (request.params as { id: string }).id)
    if (uid === null) return
    const existing = await findUserOr404(reply, uid)
    if (!existing) return

    await db.delete(users).where(eq(users.id, uid))
    return success(null, '删除成功')
  })
}
