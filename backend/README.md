# server — Fastify + Drizzle ORM + MySQL 后端

为前端 `frontend` 提供的用户测试 API 后端，端口 `8080`（与前端 `vite.config.ts` 的 `/api` 代理目标一致）。

## 技术栈

- **Fastify 5** — HTTP 框架
- **Drizzle ORM + mysql2** — 数据库访问
- **MySQL** — 数据持久化
- **Zod** — 请求参数校验

## 快速开始

1. 安装依赖

   ```bash
   npm install
   ```

2. 配置 MySQL 连接

   复制 `.env.example` 为 `.env`，修改其中的 `DB_HOST / DB_PORT / DB_USER / DB_PASSWORD / DB_NAME`。

   默认连接：`localhost:3306`、`root`、空密码、数据库 `page`。

3. 建表（需 MySQL 已启动且数据库存在）

   ```bash
   npm run db:push
   ```

   > 若 `page` 数据库不存在，请先在 MySQL 执行：`CREATE DATABASE page CHARACTER SET utf8mb4;`

4. （可选）插入初始账号与示例数据

   ```bash
   npm run db:seed
   ```

   会插入登录账号 `admin / 123456`（密码按 `md5` 存储）和 8 条示例用户。

5. 启动开发服务（支持热重载）

   ```bash
   npm run dev
   ```

   访问健康检查：`http://localhost:8080/api/health`

## 接口列表

统一响应格式 `{ code, data, msg }`，成功 `code === 200`，与前端 `src/api/http.ts` 契约一致。

鉴权：除 `/api/auth/*` 与 `/api/health` 外，其余接口需携带 JWT（登录后返回，有效期 8h）；`401` 表示 token 失效，前端应清 token 跳登录。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| POST | `/api/auth/login` | 登录（body: `{username, password}`，password 为 md5） |
| POST | `/api/auth/logout` | 登出（JWT 无状态，前端删 token 即可） |
| POST | `/api/auth/verify-identity` | 身份验证（body: `{cellphone, email}`） |
| POST | `/api/auth/reset-password` | 重置密码（body: `{cellphone, email, newPassword}`） |
| GET | `/api/users` | 用户列表（query: `userName/roleName/department/state/page/pageSize`） |
| GET | `/api/user/info` | 查询单个用户信息（返回首条） |
| POST | `/api/users` | 新增用户（body: `{userName, roleName, department, state}`） |
| PUT | `/api/users/:id` | 修改用户 |
| DELETE | `/api/users/:id` | 删除用户 |

## 数据表

- `users`：`id / userName / roleName / department / state / createTime`，`createTime` 为字符串 `YYYY-MM-DD HH:mm`，字段对齐前端 `data1.vue` / `data2.vue` 表格。
- `accounts`：`id / username / password / name / cellphone / email`，登录账号（测试账号 `admin / 123456`，密码存 `md5`）。
  > ⚠️ **MD5 为历史兼容性包袱（非规范做法）**：md5 已被攻破，仅因与既有前端约定兼容而保留，新系统禁止沿用，生产环境应升级为 `bcrypt`/`argon2`。
