/**
 * 种子数据脚本 —— 插入初始登录账号与示例用户
 * 运行：npm run db:seed
 */
import 'dotenv/config'
import { db, pool } from './client.js'
import { accounts, users } from './schema.js'

async function seed() {
  // 登录账号 admin / 123456
  await db.insert(accounts).values({
    username: 'admin',
    password: '123456',
    name: '管理员',
  })

  // 示例用户（对齐前端 data2.vue mock 数据）
  const samples = [
    ['张三', '管理员', '技术部', '启用', '2026-08-01 10:00'],
    ['李四', '编辑', '内容部', '启用', '2026-08-02 11:30'],
    ['王五', '访客', '市场部', '禁用', '2026-08-03 09:15'],
    ['赵六', '管理员', '技术部', '启用', '2026-08-05 14:20'],
    ['钱七', '编辑', '设计部', '禁用', '2026-08-07 16:45'],
    ['孙八', '访客', '市场部', '启用', '2026-08-08 09:00'],
    ['周九', '管理员', '人事部', '启用', '2026-08-10 13:40'],
    ['吴十', '编辑', '财务部', '禁用', '2026-08-12 17:25'],
  ]

  for (const [name, role, dept, status, createTime] of samples) {
    await db.insert(users).values({ name, role, dept, status, createTime })
  }

  console.log('seed 完成：已插入 admin 账号与 8 条示例用户')
  await pool.end()
}

seed().catch(err => {
  console.error('seed 失败：', err)
  process.exit(1)
})
