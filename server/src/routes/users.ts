import type { FastifyInstance } from 'fastify'
import { eq, and, like, or } from 'drizzle-orm'
import { z } from 'zod'
import { db } from '../db/client.js'
import { users } from '../db/schema.js'
import { success, fail } from '../utils/response.js'

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

export async function userRoutes(fastify: FastifyInstance) {
  // 用户列表（含关键字 / 部门 / 状态 过滤 + 分页）
  fastify.get('/api/users', async (request, reply) => {
    const query = listQuerySchema.parse(request.query)
    const { userName, roleName,department, state, page, pageSize } = query

    const conditions = []
    if (userName) {
      const kw = `%${userName}%`
      conditions.push(
        or(like(users.userName, kw))
      )
    }
     if (roleName) {
      const kw = `%${roleName}%`
      conditions.push(
         like(users.roleName, kw)
      )
    }
    if (department) conditions.push(eq(users.department, department))
    if (state) conditions.push(eq(users.state, state))

    const where = conditions.length ? and(...conditions) : undefined

    const total = await db.select({ count: users.id }).from(users).where(where)
    const count = total.length

    const list = await db
      .select()
      .from(users)
      .where(where)
      .limit(pageSize)
      .offset((page - 1) * pageSize)

    return success({ list, total: count, page, pageSize })
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
  fastify.post('/api/users', async (request, reply) => {
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
    const { id } = request.params as { id: string }
    const body = saveSchema.parse(request.body)
    const uid = Number(id)

    const existing = await db.select().from(users).where(eq(users.id, uid))
    if (!existing.length) {
      return reply.status(404).send(fail(404, '用户不存在'))
    }

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
    const { id } = request.params as { id: string }
    const uid = Number(id)

    const existing = await db.select().from(users).where(eq(users.id, uid))
    if (!existing.length) {
      return reply.status(404).send(fail(404, '用户不存在'))
    }

    await db.delete(users).where(eq(users.id, uid))
    return success(null, '删除成功')
  })
}
