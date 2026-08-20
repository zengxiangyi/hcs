/**
 * 种子数据脚本 —— 插入初始登录账号与示例用户
 * 运行：npm run db:seed
 */
import 'dotenv/config'
import { db, pool } from './client.js'
import { accounts, users } from './schema.js'
import { md5 } from '../utils/md5.js'

async function seed() {
  try {
    // 登录账号 admin / 123456 —— 密码须与前端 md5(password) 提交格式一致
    // 用 onDuplicateKeyUpdate 保证重复执行不报错；md5 只计算一次，供 insert 与 update 复用
    const adminPassword = md5('123456')

    await db
      .insert(accounts)
      .values({
        username: 'admin',
        password: adminPassword,
        name: '管理员',
        cellphone: '13800138000',
        email: 'admin@example.com',
      })
      .onDuplicateKeyUpdate({
        set: {
          password: adminPassword,
          name: '管理员',
          cellphone: '13800138000',
          email: 'admin@example.com',
        },
      })

    // 示例用户（对齐前端 data2.vue mock 数据）—— 先清空再批量插入，保证可重复执行
    await db.delete(users)

    // 以对象数组描述，字段名自解释，避免 string[][] 宽泛推断与 map 解构
    const samples = [
      { userName: '张三', roleName: '管理员', department: '技术部', state: '启用', createTime: '2026-08-01 10:00' },
      { userName: '李四', roleName: '编辑', department: '内容部', state: '启用', createTime: '2026-08-02 11:30' },
      { userName: '王五', roleName: '访客', department: '市场部', state: '禁用', createTime: '2026-08-03 09:15' },
      { userName: '赵六', roleName: '管理员', department: '技术部', state: '启用', createTime: '2026-08-05 14:20' },
      { userName: '钱七', roleName: '编辑', department: '设计部', state: '禁用', createTime: '2026-08-07 16:45' },
      { userName: '孙八', roleName: '访客', department: '市场部', state: '启用', createTime: '2026-08-08 09:00' },
      { userName: '周九', roleName: '管理员', department: '人事部', state: '启用', createTime: '2026-08-10 13:40' },
      { userName: '吴十', roleName: '编辑', department: '财务部', state: '禁用', createTime: '2026-08-12 17:25' },
    ]

    await db.insert(users).values(samples)

    console.log('seed 完成：已插入 admin 账号与 8 条示例用户')
  } finally {
    // 无论成败都释放连接池，避免脚本退出前悬挂连接
    await pool.end()
  }
}

seed().catch(err => {
  console.error('seed 失败：', err)
  process.exit(1)
})
