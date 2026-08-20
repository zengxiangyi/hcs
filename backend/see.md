# server 开发文档

> 面向 **LLM / 继任开发者** 的快速上手手册。
> 本文档覆盖：技术栈、目录结构、代码实现与接口契约、当前开发进度与待办事项。
> 生成日期：2026-08-19（对齐 `src/` 源码）

---

## 1. 项目定位

`backend`（本仓库根目录）是前端 `viteDemo/page`（Vue 3 SPA）配套的 **用户管理后端**，提供：

- 登录鉴权（**JWT**，8h 有效期，全局 preHandler 拦截受保护路由）
- 忘记密码（手机号 + 邮箱身份验证后重置）
- 用户信息 CRUD（列表 / 详情 / 新增 / 修改 / 删除）
- 统一响应与错误处理

与前端约定一致：端口 `8080`，路由前缀 `/api`，前端 Vite 开发服务器通过 `/api` 代理到 `http://localhost:8080`（见前端根目录 `vite.config.ts`）。

---

## 2. 技术栈

| 领域 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 运行时 | Node.js + ESM | — | `package.json` 中 `"type": "module"` |
| HTTP 框架 | **Fastify** | ^5.2.0 | 主框架，插件化 |
| CORS | @fastify/cors | ^10.0.1 | 允许跨域 |
| 鉴权 | **@fastify/jwt** | ^10.2.2 | 登录签发 JWT，全局 preHandler 校验 |
| ORM | **Drizzle ORM** | ^0.38.2 | TypeScript-first 数据库访问 |
| 数据库驱动 | mysql2 | ^3.11.5 | MySQL 连接池 |
| 数据库 | **MySQL** | — | 默认库 `user_test`（utf8mb4） |
| 参数校验 | **Zod** | ^3.24.1 | 请求体 / 查询参数校验 |
| 环境变量 | dotenv | ^16.4.5 | 读取 `.env` |
| 密码哈希 | md5（`utils/md5.ts`，基于 Node 内置 `node:crypto`） | — | 密码存储与比对 |
| 迁移工具 | drizzle-kit | ^0.30.1 | generate / push / studio |
| 开发运行 | tsx | ^4.19.2 | TS 直接运行（watch 热重载） |
| 语言 | TypeScript | ^5.7.2 | 严格模式 |

> 当前**已引入** `@fastify/jwt` 与 `md5` 哈希，登录鉴权已具备真实 JWT + 中间件拦截。
> 未引入：测试框架（vitest/jest）、日志分析工具、刷新令牌 / 细粒度权限控制、Dockerfile。

---

## 3. 目录结构

```
.
├── drizzle.config.ts        # drizzle-kit 配置（MySQL 方言、schema 路径、迁移输出目录）
├── package.json             # 依赖与脚本
├── README.md                # 简短启动说明（本文档为其扩展）
├── tsconfig.json            # TS 编译配置（NodeNext 模块、严格模式）
├── .env / .env.example      # 环境变量（PORT、DB_*、JWT_SECRET）
└── src/
    ├── index.ts             # 【入口】Fastify 实例、插件注册、全局鉴权钩子、路由挂载、统一错误处理
    ├── db/
    │   ├── client.ts        # mysql2 连接池 + drizzle 实例
    │   ├── schema.ts        # 数据库表定义（users / accounts）
    │   └── seed.ts          # 种子脚本：初始账号 admin/123456 + 8 条示例用户（幂等）
    ├── routes/
    │   ├── auth.ts          # 鉴权路由（login / logout / verify-identity / reset-password）
    │   └── users.ts         # 用户 CRUD 路由
    └── utils/
        ├── response.ts      # 统一响应结构 success / fail
        └── md5.ts           # md5 哈希（密码存储与比对）
```

> 迁移产物目录 `drizzle/` 由 `drizzle-kit generate` 生成（当前不在仓库中，push 模式按需创建表）。

---

## 4. 数据模型（`src/db/schema.ts`）

### 4.1 `users` 用户表

| 字段 | 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|------|
| id | `id` | int | PK, autoincrement | 自增主键 |
| userName | `userName` | varchar(64) | not null | 姓名 |
| roleName | `roleName` | varchar(32) | not null | 角色（管理员/编辑/访客） |
| department | `department` | varchar(64) | not null | 部门 |
| state | `state` | varchar(16) | not null, default `启用` | 启用/禁用 |
| createTime | `createTime` | varchar(32) | not null | 创建时间，字符串存储 `YYYY-MM-DD HH:mm` |

> 字段刻意与前端 `data2.vue` 表格列对齐；时间用字符串是为了和前端展示格式一致。
> 注意字段名为 `userName/roleName/department/state`，非旧文档的 `name/role/dept/status`。

### 4.2 `accounts` 登录账号表

| 字段 | 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|------|
| id | `id` | int | PK, autoincrement | 自增主键 |
| username | `username` | varchar(64) | not null, unique | 用户名 |
| password | `password` | varchar(128) | not null | 密码（`md5(...)` 哈希存储，直存直比） |
| name | `name` | varchar(64) | nullable | 显示名 |
| cellphone | `cellphone` | varchar(20) | not null, default `''` | 手机号（忘记密码验证） |
| email | `email` | varchar(128) | not null, default `''` | 邮箱（忘记密码验证） |

> **⚠️ 安全说明（历史兼容性包袱）**：密码经 `md5(password)` 哈希后存储（见 `utils/md5.ts`，基于 Node 内置 `node:crypto.createHash('md5')`），登录时前端同样提交 md5 后的密文进行比对；前端契约要求密码以 md5 格式提交。
> **MD5 已被攻破，不可用于安全散列**，此处纯属与既有前端约定保持兼容而保留的历史包袱，**不应视为规范做法**。任何新系统禁止沿用此方案，生产环境应升级为 `bcrypt`/`argon2` 等自适应成本哈希。相关契约测试见 `src/utils/__tests__/md5.test.ts`。
> `cellphone` + `email` 用于在忘记密码流程中做身份验证，与 `accounts` 记录匹配后才允许重置。

---

## 5. 核心实现说明

### 5.1 入口 `src/index.ts`

- 创建 Fastify 实例（开启 logger）。
- 注册 `@fastify/cors`（`origin: true`，允许任意来源 + 携带凭据）。
- 注册 `@fastify/jwt`（`secret` 取自 `JWT_SECRET`，缺省 `dev-secret-change-me`；`sign.expiresIn: '8h'`）。
- **全局鉴权钩子（preHandler）**：除 `/api/auth/*` 全部接口与 `PUBLIC_PATHS`（含 `/api/health`）外，其余请求必须 `request.jwtVerify()` 通过；失败返回 `401 {code:401,msg:'登录已过期，请重新登录'}`（前端约定 401 = token 失效，清 token 跳登录）。
- 挂载 `userRoutes`、`authRoutes` 两个路由插件。
- 健康检查 `GET /api/health`。
- **统一错误处理**：`ZodError` → 400；`error.statusCode < 500` → 原状态码；其余 → 500 兜底。所有错误用 `fail(code, msg)` 包装。
- 监听 `0.0.0.0:8080`（端口可用 `PORT` 环境变量覆盖）。

### 5.2 统一响应 `src/utils/response.ts`

- `success<T>(data, msg='success')` → `{ code: 200, data, msg }`
- `fail(code, msg)` → `{ code, data: null, msg }`

该结构与前端 `src/api/http.ts` 的 `ApiResponse<T>` 完全一致，前端响应拦截器以 `code === 200` 判定成功。

### 5.3 数据库客户端 `src/db/client.ts`

- 基于 `mysql2/promise` 的 `createPool`（utf8mb4；连接上限 `connectionLimit` 用 mysql2 默认值 10）。
- 导出 `db`（drizzle 实例，`mode` 用默认值 `'default'`）与 `pool`。
- 连接参数取自环境变量，均有默认值兜底。

### 5.4 登录鉴权 `src/routes/auth.ts`

- `POST /api/auth/login`，body `{username, password}`，Zod 校验非空。
  - 查询 `accounts` 匹配用户名；密码直接比对（前端已提交 md5 密文，后端存密文）。
  - 失败返回 `400 {msg:'用户名或密码错误'}`（**不用 401**，避免与 token 失效逻辑冲突）。
  - 成功用 `reply.jwtSign({id,name,username})` 签发 JWT，返回：
    ```json
    { "code": 200, "data": { "token": "<jwt>", "user": {id, name, username} }, "msg": "success" }
    ```
- `POST /api/auth/logout`：无状态 token 后端无需维护黑名单，仅返回 `success(null,'退出成功')`，凭证清理由前端完成。
- `POST /api/auth/verify-identity`，body `{cellphone, email}`：校验手机号 + 邮箱是否匹配某账号，成功返回 `success(null,'验证成功')`，失败 `400`。
- `POST /api/auth/reset-password`，body `{cellphone, email, newPassword}`：验证通过后更新该账号密码（newPassword 至少 6 位，前端应按约定提交 md5 后密文）。

> 鉴权由 `index.ts` 的全局 preHandler 统一拦截，无需在每个路由内单独校验（登录相关接口在白名单内跳过）。

### 5.5 用户 CRUD `src/routes/users.ts`

- **列表** `GET /api/users`：支持 `userName`（模糊）、`roleName`（模糊）、`department`（精确）、`state`（精确）、`page`、`pageSize`（默认 1/10）过滤分页。返回 `{ list, total, page, pageSize }`。
- **详情** `GET /api/user/info`：返回表中第一条用户（对齐前端 `getUserInfo` 需求）。
- **新增** `POST /api/users`：body `{userName, roleName, department, state}`，自动填充 `createTime` 为当前时间字符串。
- **修改** `PUT /api/users/:id`：body 同上，校验用户存在，更新除 id/createTime 外的字段。
- **删除** `DELETE /api/users/:id`：校验存在后删除。
- 新增/修改成功返回最新记录并带 `msg`（如 `新增成功`/`修改成功`），删除成功返回 `success(null,'删除成功')`；目标不存在返回 `404`。

---

## 6. 接口清单

统一响应结构 `{ code, data, msg }`，成功 `code === 200`；受保护接口（非 `/api/auth/*`、非 `/api/health`）未带有效 JWT 时返回 `401`。

| 方法 | 路径 | 入参 | 说明 |
|------|------|------|------|
| GET | `/api/health` | — | 健康检查，data 为 `{status:'ok'}`（白名单，免 token） |
| POST | `/api/auth/login` | body `{username,password}` | 登录，成功返回 token + user（免 token） |
| POST | `/api/auth/logout` | — | 登出确认（免 token） |
| POST | `/api/auth/verify-identity` | body `{cellphone,email}` | 忘记密码·身份验证（免 token） |
| POST | `/api/auth/reset-password` | body `{cellphone,email,newPassword}` | 忘记密码·重置（免 token） |
| GET | `/api/users` | query `userName/roleName/department/state/page/pageSize` | 用户列表（过滤 + 分页，需 token） |
| GET | `/api/user/info` | — | 查询单条用户（取第一条，需 token） |
| POST | `/api/users` | body `{userName,roleName,department,state}` | 新增用户（需 token） |
| PUT | `/api/users/:id` | body `{userName,roleName,department,state}` | 修改用户（需 token） |
| DELETE | `/api/users/:id` | — | 删除用户（需 token） |

---

## 7. 快速开始

前置：Node.js（建议 ≥ 20）、MySQL 已启动。

```bash
cd backend         # 本仓库根目录
npm install

# 配置环境变量
cp .env.example .env      # 修改 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD/DB_NAME，可设 JWT_SECRET

# 建表（需先存在数据库 user_test；不存在则先执行：
#   CREATE DATABASE user_test CHARACTER SET utf8mb4;）
npm run db:push

# 插入初始账号 admin/123456（密码以 md5 存储）与 8 条示例用户（幂等）
npm run db:seed

# 启动开发服务（tsx watch，支持热重载）
npm run dev
```

验证：`curl http://localhost:8080/api/health` → `{"code":200,...}`

登录测试：`POST /api/auth/login` body `{"username":"admin","password":"123456"}`（注意：若前端提交的是 md5 密文，则此处应提交对应密文；种子账号密码经 `md5('123456')` 存储）。

### 脚本速查（`package.json` scripts）

| 命令 | 作用 |
|------|------|
| `npm run dev` | tsx watch 热重载开发 |
| `npm start` | tsx 直接运行（不监听） |
| `npm run build` | `tsc` 编译到 `dist/`（严格模式类型检查） |
| `npm run db:generate` | 由 schema 生成 SQL 迁移 |
| `npm run db:push` | 直接推送 schema 到数据库建表 |
| `npm run db:studio` | 打开 drizzle 可视化 Studio |
| `npm run db:seed` | 执行种子脚本（幂等） |

---

## 8. 与前端集成

- **代理**：前端根目录 `vite.config.ts` 将 `/api` 代理到 `http://localhost:8080`，开发时前端无需配 CORS。
- **契约**：前端 `src/api/http.ts` 拦截器以 `code === 200` 判定成功；`401` 触发跳登录并清理本地 token。登录失败用 `400` 而非 `401`，避免被误判为 token 失效。
- **密码约定**：前端登录 / 重置密码提交的是 `md5(password)` 后的密文，与后端 `accounts.password` 中存储的 md5 密文直接比对。
- **调用示例**：前端 `src/api/` 下的 `base.ts` / `data.ts` 封装对应接口。

---

## 9. 开发进度与现状

### 已实现（可用）
- [x] Fastify 服务骨架 + CORS + 统一错误处理
- [x] 统一响应结构（success/fail）
- [x] Drizzle + MySQL 连接池
- [x] `users` / `accounts` 表定义
- [x] JWT 登录鉴权（@fastify/jwt，8h 有效期）
- [x] 全局 preHandler 鉴权钩子（白名单 `/api/auth/*` 与 `/api/health`）
- [x] 登录 / 登出 / 忘记密码（验证身份 + 重置）接口
- [x] 用户列表（过滤 + 分页）
- [x] 用户详情 / 新增 / 修改 / 删除
- [x] 健康检查接口
- [x] 种子脚本（admin 账号 md5 密码 + 8 示例用户，幂等）
- [x] drizzle-kit 建表 / Studio 配置
- [x] 密码 md5 哈希存储

### 待办 / 已知局限（明确未做）
- [ ] **无单元 / 集成测试**。
- [ ] **`GET /api/user/info` 语义临时**：返回表首条记录，仅占位前端展示，后续应改为按登录用户或 token 解析。
- [ ] **无 `dist/` 生产构建产物**提交（`npm run build` 可用，未配置部署）。
- [ ] **无 Dockerfile / 部署配置**。
- [ ] **密码哈希强度（⚠️ 历史包袱，非规范）**：当前为 md5（已被攻破，仅因前端兼容保留），生产环境务必升级为 `bcrypt`/`argon2`。
- [ ] **无刷新令牌 / 细粒度权限控制**（JWT 仅含 id/name/username，未做角色级路由鉴权）。

---

## 10. 给继任开发者 / LLM 的建议

1. **扩展新表**：在 `src/db/schema.ts` 用 `mysqlTable` 定义，跑 `npm run db:generate` + `db:push`。
2. **新增路由**：仿照 `src/routes/users.ts` 写一个导出 `fastify: FastifyInstance` 的函数，在 `index.ts` 注册；受保护接口自动受全局 preHandler 拦截。
3. **新增免校验接口**：若新接口需跳过 JWT 鉴权，在 `index.ts` 的 `preHandler` 白名单（如 `/api/auth/`）或 `PUBLIC_PATHS` 中补充。
4. **密码处理**：新增账号 / 改密码务必走 `utils/md5.ts` 的 `md5()`（基于 Node 内置 `node:crypto`），与前端提交格式保持一致。⚠️ 注意 md5 仅为历史兼容性包袱（非规范），仅用于维护现有契约，新功能不得新引入 md5 散列。
5. **保持契约一致**：任何响应改动务必维持 `{ code, data, msg }` 结构，否则前端拦截器会误判；登录失败保持 `400`。
6. **安全升级前**：md5 仅为演示强度，生产部署前建议升级密码哈希算法并补充权限控制。
