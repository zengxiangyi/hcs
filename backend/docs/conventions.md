# 后端开发约定

新增/修改代码时需遵循以下约定。

## 1. 保持响应结构

所有接口返回必须走 `success` / `fail`，维持 `{code, data, msg}` 结构（前端 `src/api/http.ts` 以 `code===200` 判成功，破坏会误判）。

## 2. 新路由写法

仿 `src/routes/users.ts` 导出 `async function xRoutes(fastify)`，并在 `src/index.ts` 注册。若需免鉴权，路径以 `/api/auth/` 开头或加入 `PUBLIC_PATHS`。

## 3. 新表扩展

在 `src/db/schema.ts` 用 `mysqlTable` 定义 → `npm run db:push`（或 `db:generate` + push）。

## 4. 密码 / 登录契约

前端提交 `md5(password)`，后端存 `md5(...)` 并直比（见 `seed.ts` / `utils/md5.ts`）；登录失败用 400，不用 401（401 在前端被定义为 token 失效，会触发整页跳转）。

## 5. 受保护接口读用户

`await request.jwtVerify()` 后使用 `request.user`（类型由 `types/fastify-jwt.d.ts` 提供）。

## 6. 字段命名

`users` 用 `userName/roleName/department/state`，勿回退到旧名 `name/role/dept/status`。

## 7. 已知局限

- 密码为 md5（非安全哈希，仅演示用），无生产部署配置、无 Dockerfile、无单元/集成测试。
- `GET /api/user/info` 取表首条，仅占位。
