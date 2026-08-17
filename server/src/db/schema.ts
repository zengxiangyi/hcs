import {
  mysqlTable,
  int,
  varchar,
} from 'drizzle-orm/mysql-core'

/**
 * 用户表 —— 字段对齐前端 data1.vue / data2.vue 表格列
 */
export const users = mysqlTable('users',{
    id: int('id').autoincrement().primaryKey(),
    userName: varchar('userName', { length: 64 }).notNull(),
    roleName: varchar('roleName', { length: 32 }).notNull(),
    department: varchar('department', { length: 64 }).notNull(),
    state: varchar('state', { length: 16 }).notNull().default('启用'),
    createTime: varchar('createTime', { length: 32 }).notNull(),
  }
)

/** 登录用户表 —— 存储账号密码 */
export const accounts = mysqlTable('accounts', {
  id: int('id').autoincrement().primaryKey(),
  username: varchar('username', { length: 64 }).notNull().unique(),
  password: varchar('password', { length: 128 }).notNull(),
  name: varchar('name', { length: 64 }),
  // 用于“忘记密码”身份验证，与 email 一起校验是否匹配
  cellphone: varchar('cellphone', { length: 20 }).notNull().default(''),
  email: varchar('email', { length: 128 }).notNull().default(''),
})
