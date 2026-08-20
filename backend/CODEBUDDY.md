# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## 项目概述

`backend`：前端 `viteDemo/page` 配套的**用户管理后端**。**Fastify 5 + Drizzle ORM + mysql2 + MySQL**，端口 8080（前端 vite proxy `/api`→8080）。提供 JWT 登录鉴权与用户 CRUD。

> `see.md` 已与 `src/` 源码对齐（字段、鉴权、技术栈均以当前实现为准）。

## 核心命令（速查）

```bash
npm install      # 安装依赖（当前 node_modules 未安装）
npm run dev      # 开发服务，tsx watch 热重载，监听 8080
npm run build    # tsc 编译到 dist/（严格模式类型检查）
npm run db:push  # 建表/更新表（需先建库 user_test）
npm run db:seed  # 种子数据：admin/123456 + 8 条示例用户（幂等）
npm run db:studio # drizzle 可视化 Studio
```

无 lint / test 脚本；类型检查内置于 `npm run build`。

## 架构（概览）

**`src/index.ts`（入口）→ 全局 `preHandler` 鉴权钩子 → 路由插件（`routes/*.ts`）→ Drizzle 访问 MySQL → 统一响应 `utils/response.ts`**。

关键要点：
- **鉴权**：JWT 8h 有效期；全局 preHandler 白名单 `/api/auth/*` 与 `/api/health`，其余 `jwtVerify()` 失败返 `401`（前端契约：清 token 跳登录）。
- **响应契约**：统一 `{code,data,msg}`，成功 `code===200`；登录失败用 400 而非 401。
- **数据模型**：`users` 字段 `userName/roleName/department/state/createTime`；`accounts` 含 `cellphone/email`（忘记密码验证）。密码存 `md5(...)` 直存直比。

## 按需加载（细节文档）

- [常用命令详解](docs/commands.md)
- [架构详解（请求生命周期 / 数据访问 / 路由 / 接口清单）](docs/architecture.md)
- [开发约定（新增/修改需遵循）](docs/conventions.md)

> 注：`docs/struct.md` 为**前端**项目结构说明，与本后端无关。
