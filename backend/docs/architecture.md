# 后端架构详解

Fastify 插件化单进程服务，无控制器/服务分层，路由文件直接内联业务逻辑。整体请求链路：

**`src/index.ts`（入口）→ 全局 `preHandler` 鉴权钩子 → 路由插件（`routes/*.ts`）→ Drizzle 访问 MySQL → 统一响应工具 `utils/response.ts`**

## 1. 入口与请求生命周期（`src/index.ts`）

唯一入口，完成所有「横向」装配，顺序即依赖关系：

1. **注册插件**：`cors`（`origin:true` 允许任意来源）、`sensible`（标准错误）、**`jwt`**（secret 取 `process.env.JWT_SECRET || 'dev-secret-change-me'`，`sign.expiresIn:'8h'`）。
2. **注册全局 `preHandler` 鉴权钩子**（在路由之前）：对每个请求 `request.url.split('?')[0]` 取 path，白名单放行 `/api/auth/*`（登录/验证/重置密码等）与 `/api/health`；其余请求必须 `await request.jwtVerify()`，失败返回 `401 + fail(401,'登录已过期，请重新登录')`。**401 是前端契约**（前端清 token 并跳登录页）。
3. **注册路由插件**：`userRoutes`、`authRoutes`（必须传 `FastifyInstance`）。
4. **根路径健康检查** `GET /api/health`。
5. **统一错误处理 `setErrorHandler`**：`ZodError` → `400`（取各 issue.message join）；其他 `Error` 若带 `<500` 的 `statusCode` 则透传该状态码，否则兜底 `500`。

**关键约定**：任何新增受保护接口都会自动被 preHandler 拦截（除非路径以 `/api/auth/` 开头）；`request.user` 在 `jwtVerify()` 后可用（类型见下方「统一响应与类型」）。

## 2. 数据访问（`src/db/`）

- `client.ts`：导出 `db`（drizzle 实例，`mode:'default'`）与 `pool`（mysql2 `createPool`，连接上限 10、utf8mb4）。连接参数全部从 `.env` 读取，均有默认值（localhost/3306/root/空密码/user_test）。
- `schema.ts`：定义两表。**`users` 字段为 `userName/roleName/department/state/createTime`**（与旧文档不同；`state` 默认 `'启用'`，`createTime` 是 `varchar` 存 `YYYY-MM-DD HH:mm` 字符串，为对齐前端展示格式）。`accounts` 含 `username/password/name/cellphone/email`（后两者用于忘记密码身份验证）。
- `seed.ts`：幂等种子脚本。账号 `admin` 密码为 **`md5('123456')`**（与前端提交格式一致，直存直比）；用户数据先 `db.delete(users)` 再批量插入。

> **跨文件注意**：`schema.ts` 的字段名必须与 `routes/users.ts` 中的 zod schema 和查询条件一致。修改表结构后需 `npm run db:push`（或 `db:generate`+push）同步数据库。

## 3. 路由层（`src/routes/`）

两个路由文件均导出 `async function xxxRoutes(fastify: FastifyInstance)`，由入口注册。**成功显式 `return success(...)`，失败 `return reply.status(xxx).send(fail(xxx, msg))`**。

### `auth.ts`（登录/鉴权流程）

- `POST /api/auth/login`：zod 校验 `{username,password}` → 查 `accounts` 匹配 → **失败返 `400`「用户名或密码错误」（刻意不用 401，避免被前端当 token 失效整页跳转）**；成功 `reply.jwtSign({id,name,username})` 签 JWT。
- `POST /api/auth/logout`：JWT 无状态，仅返回成功，凭证清理由前端删本地 token 完成。
- `POST /api/auth/verify-identity`：校验 `{cellphone,email}` 匹配 `accounts`，不匹配返 `400`。
- `POST /api/auth/reset-password`：再次校验身份后更新 `password`。

### `users.ts`（用户 CRUD）

- `GET /api/users`：过滤 + 分页。`userName`/`roleName` 用 `like` 模糊匹配，`department`/`state` 用 `eq` 精确匹配，返回 `{list,total,page,pageSize}`。
- `GET /api/user/info`：**取表首条**（占位实现，供前端 `getUserInfo`）。
- `POST /api/users` / `PUT /api/users/:id` / `DELETE /api/users/:id`：新增自动填 `createTime`（`formatNow()` 本地函数生成字符串）；修改/删除先查存在否则 `404`。

## 4. 统一响应与类型

- `utils/response.ts`：定义 `ApiResponse<T>{code,data,msg}`，`success<T>(data,msg='success')` 返回 `code:200`，`fail(code,msg)` 返回 `code` + `data:null`。**这是与前端 `src/api/http.ts` 的硬契约（前端以 `code===200` 判成功），任何改动不能破坏此结构**。
- `utils/md5.ts`：无依赖的纯函数 MD5（UTF-8，支持中文），供 seed 加密密码。前端也各自实现了 md5 并提交 `md5(password)`。
- `types/fastify-jwt.d.ts`：通过模块声明合并 `declare module '@fastify/jwt'` 扩展 `FastifyJWT.payload/user` 为 `{id:number, name:string|null, username:string}`，使 `request.user` 获得类型。

## 5. 配置文件与环境

- `drizzle.config.ts`：MySQL 方言，`schema:'./src/db/schema.ts'`，`out:'./drizzle'`，连接参数同 client 默认值。
- `tsconfig.json`：`NodeNext` 模块解析、`ES2022`、`strict`、`rootDir:src`、`outDir:dist`。**ESM 环境：源码内相对导入需带 `.js` 后缀**（如 `from './routes/users.js'`）。
- `.env` / `.env.example`：`PORT`、`DB_HOST/DB_PORT/DB_USER/DB_PASSWORD/DB_NAME`、`JWT_SECRET`。所有源码通过 `import 'dotenv/config'` 读取。

## 接口清单

统一响应 `{ code, data, msg }`，成功 `code === 200`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| POST | `/api/auth/login` | 登录（`{username,password}`），返回 JWT + user |
| POST | `/api/auth/logout` | 登出（无状态，仅确认） |
| POST | `/api/auth/verify-identity` | 验证手机号+邮箱（忘记密码步骤一） |
| POST | `/api/auth/reset-password` | 重置密码（忘记密码步骤二） |
| GET | `/api/users` | 用户列表（keyword/dept/state + 分页） |
| GET | `/api/user/info` | 查询单条用户（取首条） |
| POST | `/api/users` | 新增用户 |
| PUT | `/api/users/:id` | 修改用户 |
| DELETE | `/api/users/:id` | 删除用户 |
