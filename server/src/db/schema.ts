import {
  mysqlTable,
  int,
  varchar,
  timestamp,
  index,
} from 'drizzle-orm/mysql-core'

/**
 * 用户表 —— 字段对齐前端 data1.vue / data2.vue 表格列：
 * id / name / role / dept / status / createTime
 */
export const users = mysqlTable(
  'users',
  {
    id: int('id').autoincrement().primaryKey(),
    name: varchar('name', { length: 64 }).notNull(),
    role: varchar('role', { length: 32 }).notNull(),
    dept: varchar('dept', { length: 64 }).notNull(),
    status: varchar('status', { length: 16 }).notNull().default('启用'),
    // 时间以字符串存储，与前端展示格式 YYYY-MM-DD HH:mm 保持一致
    createTime: varchar('create_time', { length: 32 }).notNull(),
  },
  (table) => ({
    nameIdx: index('idx_name').on(table.name),
    deptIdx: index('idx_dept').on(table.dept),
    statusIdx: index('idx_status').on(table.status),
  })
)

/** 登录用户表 —— 存储账号密码 */
export const accounts = mysqlTable('accounts', {
  id: int('id').autoincrement().primaryKey(),
  username: varchar('username', { length: 64 }).notNull().unique(),
  password: varchar('password', { length: 128 }).notNull(),
  name: varchar('name', { length: 64 }),
})
