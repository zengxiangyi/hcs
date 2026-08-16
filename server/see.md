# server 开发文档

> 面向 **LLM / 继任开发者** 的快速上手手册。
> 本文档覆盖：技术栈、目录结构、代码实现与接口契约、当前开发进度与待办事项。
> 生成日期：2026-08-16

---

## 1. 项目定位

`server` 是前端 `viteDemo/page`（Vue 3 SPA）配套的 **用户测试管理后端**，提供：

- 登录鉴权（简易 token）
- 用户信息 CRUD（列表 / 详情 / 新增 / 修改 / 删除）
- 统一响应与错误处理

与前端约定一致：端口 `8080`，路由前缀 `/api`，前端 Vite 开发服务器通过 `/api` 代理到 `http://localhost:8080`（见根目录 `vite.config.ts`）。

---

## 2. 技术栈

| 领域 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 运行时 | Node.js + ESM | — | `package.json` 中 `"type": "module"` |
| HTTP 框架 | **Fastify** | ^5.2.0 | 主框架，插件化 |
| CORS | @fastify/cors | ^10.0.1 | 允许跨域 |
| 通用错误 | @fastify/sensible | ^6.0.1 | 提供标准错误封装 |
| ORM | **Drizzle ORM** | ^0.38.2 | TypeScript-first 数据库访问 |
| 数据库驱动 | mysql2 | ^3.11.5 | MySQL 连接池 |
| 数据库 | **MySQL** | — | 默认库 `user_test`（utf8mb4） |
| 参数校验 | **Zod** | ^3.24.1 | 请求体 / 查询参数校验 |
| 环境变量 | dotenv | ^16.4.5 | 读取 `.env` |
| 迁移工具 | drizzle-kit | ^0.30.1 | generate / push / studio |
| 开发运行 | tsx | ^4.19.2 | TS 直接运行（watch 热重载） |
| 语言 | TypeScript | ^5.7.2 | 严格模式 |

> **注意**：项目目前**未引入** JWT 鉴权库（jsonwebtoken）、测试框架（vitest/jest）、日志分析工具。token 为简易 base64，仅供测试。

---

## 3. 目录结构

```
server/
├── drizzle.config.ts        # drizzle-kit 配置（MySQL 方言、schema 路径、迁移输出目录）
├── package.json             # 依赖与脚本
├── README.md                # 简短启动说明（本文档为其扩展）
├── tsconfig.json            # TS 编译配置（NodeNext 模块、严格模式）
├── .env / .env.example      # 环境变量（PORT、DB_*）
└── src/
    ├── index.ts             # 【入口】Fastify 实例、插件注册、路由挂载、统一错误处理
    ├── db/
    │   ├── client.ts        # mysql2 连接池 + drizzle 实例
    │   ├── schema.ts        # 数据库表定义（users / accounts）
    │   └── seed.ts          # 种子脚本：初始账号 + 8 条示例用户
    ├── routes/
    │   ├── auth.ts          # 登录路由（POST /api/auth/login）
    │   └── users.ts         # 用户 CRUD 路由
    └── utils/
        └── response.ts      # 统一响应结构 success / fail
```

> 迁移产物目录 `drizzle/` 由 `drizzle-kit generate` 生成（当前不在仓库中，push 模式按需创建表）。

---

## 4. 数据模型（`src/db/schema.ts`）

### 4.1 `users` 用户表

| 字段 | 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|------|
| id | `id` | int | PK, autoincrement | 自增主键 |
| name | `name` | varchar(64) | not null | 姓名 |
| role | `role` | varchar(32) | not null | 角色（管理员/编辑/访客） |
| dept | `dept` | varchar(64) | not null | 部门 |
| status | `status` | varchar(16) | not null, default `启用` | 启用/禁用 |
| createTime | `create_time` | varchar(32) | not null | 创建时间，字符串存储 `YYYY-MM-DD HH:mm` |

索引：`idx_name`、`idx_dept`、`idx_status`。

> 字段刻意与前端 `data1.vue` / `data2.vue` 表格列对齐；时间用字符串是为了和前端展示格式一致。

### 4.2 `accounts` 登录账号表

| 字段 | 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|------|
| id | `id` | int | PK, autoincrement | 自增主键 |
| username | `username` | varchar(64) | not null, unique | 用户名 |
| password | `password` | varchar(128) | not null | 密码（**明文存储，仅测试用**） |
| name | `name` | varchar(64) | nullable | 显示名 |

> **安全注意**：密码为明文，无哈希/加盐。仅作演示后端，切勿直接用于生产。

---

## 5. 核心实现说明

### 5.1 入口 `src/index.ts`

- 创建 Fastify 实例（开启 logger）。
- 注册 `@fastify/cors`（`origin: true`，允许任意来源 + 携带凭据）。
- 注册 `@fastify/sensible`。
- 挂载 `userRoutes`、`authRoutes` 两个路由插件。
- 健康检查 `GET /api/health`。
- **统一错误处理**：`ZodError` → 400；`error.statusCode < 500` → 原状态码；其余 → 500 兜底。所有错误用 `fail(code, msg)` 包装。
- 监听 `0.0.0.0:8080`（端口可用 `PORT` 环境变量覆盖）。

### 5.2 统一响应 `src/utils/response.ts`

- `success<T>(data, msg='success')` → `{ code: 200, data, msg }`
- `fail(code, msg)` → `{ code, data: null, msg }`

该结构与前端 `src/api/http.ts` 的 `ApiResponse<T>` 完全一致，前端响应拦截器以 `code === 200` 判定成功。

### 5.3 数据库客户端 `src/db/client.ts`

- 基于 `mysql2/promise` 的 `createPool`（连接上限 10、utf8mb4）。
- 导出 `db`（drizzle 实例，`mode: 'default'`）与 `pool`。
- 连接参数取自环境变量，均有默认值兜底。

### 5.4 登录鉴权 `src/routes/auth.ts`

- `POST /api/auth/login`，body `{username, password}`，Zod 校验非空。
- 查询 `accounts` 匹配用户名 + 明文密码。
- 失败返回 `401 {msg: '用户名或密码错误'}`。
- 成功返回：
  ```json
  { "code": 200, "data": { "token": "<base64>", "user": {id, name, username} }, "msg": "success" }
  ```
- token = `base64(username:timestamp)`，**无过期校验、无中间件拦截**（测试用）。

### 5.5 用户 CRUD `src/routes/users.ts`

- **列表** `GET /api/users`：支持 `keyword`（模糊匹配 name/role/id）、`dept`、`status`（精确匹配）、`page`、`pageSize`（默认 1/10）分页。返回 `{ list, total, page, pageSize }`。
- **详情** `GET /api/user/info`：返回表中第一条用户（对齐前端 `getUserInfo` 需求）。
- **新增** `POST /api/users`：body `{name, role, dept, status}`，自动填充 `createTime` 为当前时间字符串。
- **修改** `PUT /api/users/:id`：body 同上，校验用户存在，更新除 id/createTime 外的字段。
- **删除** `DELETE /api/users/:id`：校验存在后删除。
- 新增/修改/删除成功均返回最新记录并带 `msg`（如 `新增成功`）。

---

## 6. 接口清单

统一响应结构 `{ code, data, msg }`，成功 `code === 200`。

| 方法 | 路径 | 入参 | 说明 |
|------|------|------|------|
| GET | `/api/health` | — | 健康检查，data 为 `{status:'ok'}` |
| POST | `/api/auth/login` | body `{username,password}` | 登录，成功返回 token + user |
| GET | `/api/users` | query `keyword/dept/status/page/pageSize` | 用户列表（过滤 + 分页） |
| GET | `/api/user/info` | — | 查询单条用户（取第一条） |
| POST | `/api/users` | body `{name,role,dept,status}` | 新增用户 |
| PUT | `/api/users/:id` | body `{name,role,dept,status}` | 修改用户 |
| DELETE | `/api/users/:id` | — | 删除用户 |

---

## 7. 快速开始

前置：Node.js（建议 ≥ 20）、MySQL 已启动。

```bash
cd server
npm install

# 配置环境变量
cp .env.example .env      # 修改 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD/DB_NAME

# 建表（需先存在数据库 user_test；不存在则先执行：
#   CREATE DATABASE user_test CHARACTER SET utf8mb4;）
npm run db:push

# （可选）插入初始账号 admin/123456 与 8 条示例用户
npm run db:seed

# 启动开发服务（tsx watch，支持热重载）
npm run dev
```

验证：`curl http://localhost:8080/api/health` → `{"code":200,...}`

登录测试：`POST /api/auth/login` body `{"username":"admin","password":"123456"}`。

### 脚本速查（`package.json` scripts）

| 命令 | 作用 |
|------|------|
| `npm run dev` | tsx watch 热重载开发 |
| `npm start` | tsx 直接运行（不监听） |
| `npm run build` | `tsc` 编译到 `dist/` |
| `npm run db:generate` | 由 schema 生成 SQL 迁移 |
| `npm run db:push` | 直接推送 schema 到数据库建表 |
| `npm run db:studio` | 打开 drizzle 可视化 Studio |
| `npm run db:seed` | 执行种子脚本 |

---

## 8. 与前端集成

- **代理**：根目录 `vite.config.ts` 将 `/api` 代理到 `http://localhost:8080`，开发时前端无需配 CORS。
- **契约**：前端 `src/api/http.ts` 拦截器以 `code === 200` 判定成功、`401` 触发跳登录并清理本地 token。
- **调用示例**：前端 `src/api/` 下的 `base.ts` / `data.ts` 封装对应接口。

---

## 9. 开发进度与现状

### 已实现（可用）
- [x] Fastify 服务骨架 + CORS + 统一错误处理
- [x] 统一响应结构（success/fail）
- [x] Drizzle + MySQL 连接池
- [x] `users` / `accounts` 表定义
- [x] 登录接口（简易 token）
- [x] 用户列表（过滤 + 分页）
- [x] 用户详情 / 新增 / 修改 / 删除
- [x] 健康检查接口
- [x] 种子脚本（admin 账号 + 8 示例用户）
- [x] drizzle-kit 建表 / Studio 配置

### 待办 / 已知局限（明确未做）
- [ ] **无鉴权中间件**：登录返回的 token 未用于拦截受保护路由，任何接口都可匿名访问。
- [ ] **token 为明文 base64**：无过期时间、无签名，安全强度仅够演示。
- [ ] **密码明文存储**：`accounts.password` 未哈希，需引入 `bcrypt`/`argon2`。
- [ ] **无 JWT / 刷新令牌 / 权限控制**。
- [ ] **无单元 / 集成测试**。
- [ ] **`GET /api/user/info` 语义临时**：返回表首条记录，仅占位前端展示，后续应改为按登录用户或 token 解析。
- [ ] **无 `dist/` 生产构建产物**提交（`npm run build` 可用，未配置部署）。
- [ ] **无 Dockerfile / 部署配置**。

---

## 10. 给继任开发者 / LLM 的建议

1. **扩展新表**：在 `src/db/schema.ts` 用 `mysqlTable` 定义，跑 `npm run db:generate` + `db:push`。
2. **新增路由**：仿照 `src/routes/users.ts` 写一个导出 `fastify: FastifyInstance` 的函数，在 `index.ts` 注册。
3. **接入真实鉴权**：建议在 `src/routes` 下抽一个 `auth` 中间件，用 `@fastify/jwt` 替换当前简易 token，并给敏感接口加保护。
4. **保持契约一致**：任何响应改动务必维持 `{ code, data, msg }` 结构，否则前端拦截器会误判。
5. **安全升级前**：勿将当前密码明文与无鉴权实现直接部署到生产。
