import 'dotenv/config'
import { drizzle } from 'drizzle-orm/mysql2'
import mysql from 'mysql2/promise'
import * as schema from './schema.js'

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'user_test',
  // connectionLimit: 10,  // mysql2 默认即为 10，按需显式调整并发上限
  charset: 'utf8mb4',
})

// 提供 schema 时必须显式指定 mode（默认 'default'，Planetscale 用 'planetscale'）
export const db = drizzle(pool, { schema, mode: 'default' })
export { pool }
