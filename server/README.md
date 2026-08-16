# server — Fastify + Drizzle ORM + MySQL 后端

为前端 `viteDemo/page` 提供的用户测试 API 后端，端口 `8080`（与前端 `vite.config.ts` 的 `/api` 代理目标一致）。

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

   默认连接：`localhost:3306`、`root`、空密码、数据库 `user_test`。

3. 建表（需 MySQL 已启动且数据库存在）

   ```bash
   npm run db:push
   ```

   > 若 `user_test` 数据库不存在，请先在 MySQL 执行：`CREATE DATABASE user_test CHARACTER SET utf8mb4;`

4. （可选）插入初始账号与示例数据

   ```bash
   npm run db:seed
   ```

   会插入登录账号 `admin / 123456` 和 8 条示例用户。

5. 启动开发服务（支持热重载）

   ```bash
   npm run dev
   ```

   访问健康检查：`http://localhost:8080/api/health`

## 接口列表

统一响应格式 `{ code, data, msg }`，成功 `code === 200`，与前端 `src/api/http.ts` 契约一致。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| POST | `/api/auth/login` | 登录（body: `{username, password}`） |
| GET | `/api/users` | 用户列表（query: `keyword/dept/status/page/pageSize`） |
| GET | `/api/user/info` | 查询单个用户信息 |
| POST | `/api/users` | 新增用户（body: `{name, role, dept, status}`） |
| PUT | `/api/users/:id` | 修改用户 |
| DELETE | `/api/users/:id` | 删除用户 |

## 数据表

- `users`：`id / name / role / dept / status / create_time`，字段对齐前端 `data1.vue` / `data2.vue` 表格。
- `accounts`：`id / username / password / name`，登录账号（测试账号：`admin / 123456`，需手动插入或用登录接口验证前插入）。
