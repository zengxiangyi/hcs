# MEMORY（长期记忆）

## 项目布局
- 本工作区 = 后端根目录 `f:\hb\page\backend`。原 `server/` 与 `../../backend/` 路径均已更名为根目录，源码在 `src/`。
- 前端项目独立位于 `f:\hb\page\frontend`（Vue3 + Vite + TS），不属于本工作区记忆范围。

## 后端技术栈与契约
- **Fastify 5 + Drizzle ORM + mysql2 + MySQL**，端口 8080（前端 vite proxy `/api`→8080）。
- 统一响应 `{code,data,msg}`，成功 `code=200`（`src/utils/response.ts`）。
- `users` 表字段：id/userName/roleName/department/**state**/**createTime**（注意：state 非 status、createTime 非 create_time；createTime 为 varchar `YYYY-MM-DD HH:mm`）。README.md/see.md 均已与 `src/db/schema.ts` 对齐。
- `accounts` 表：登录账号，含 cellphone/email 用于身份验证；密码存 `md5(...)`（直存直比）。
- 鉴权：`@fastify/jwt`（8h 有效期），全局 preHandler 白名单 `/api/auth/*` 与 `/api/health`，其余 `jwtVerify`；401 契约=前端清 token 跳登录。
- 登录失败返回 400（不用 401，避免与前端 token 失效逻辑冲突）。
- **Fastify v5 类型坑**：`setErrorHandler<TError = unknown>` 泛型默认 `unknown`，error 参数必须显式标注类型（推荐 `app.setErrorHandler<FastifyError>(...)`，从 `fastify` 导入），否则 strict 下访问 `error.statusCode`/`error.message` 报 TS18046（`npm run build` 输出错误但 exitCode 仍 0）。已两次复发（08-17、08-19），勿用收窄逻辑替代类型标注。

## 常用命令（在根目录执行）
- `npm install`（依赖未安装）
- `npm run db:push -- --force` / `npm run db:seed`（seed 幂等，含 md5 密码）
- `npm run db:studio` / `npm run dev`
- 建库：`CREATE DATABASE user_test CHARACTER SET utf8mb4`

## 开发约束
- 工作区根目录规则：AI Agent 禁止修改 `.idea/`、`script/`、`config/`（可读不可写）。
- 保护目录（`.codebuddy/rules/FileProtection.mdc`，可读不可写）：`.idea`、`public`、`node_modules`、`.vscode`、`dist`。
- 依赖未装（node_modules 缺失）时以 IDE lint 验证为主，`tsc`/`vue-tsc` 可能无法实际运行。
- 每次回复末尾需输出任务状态摘要（已完成/进行中/待处理/已阻止/备注）。

## 文档结构
- `CODEBUDDY.md`：按渐进式披露/按需加载原则压缩为「入口速览」（概述+核心命令+架构概览+链接）。
- 详情拆分在 `docs/commands.md`（命令）、`docs/architecture.md`（架构+接口清单）、`docs/conventions.md`（开发约定）。
- `docs/struct.md` 是前端项目结构说明，与后端无关。README.md/see.md 均已与 `src/` 源码对齐（字段、鉴权、技术栈）。

## 记忆维护记录
- 2026-08-19：将 08-16/17/18 三日日志压缩合并为当日文件，仅保留后端记忆、删除前端相关内容，并修正 server/→根目录路径。
- 2026-08-19：创建根目录 `CODEBUDDY.md`（agent 说明文件，含命令与架构）。修正本文件 users 字段为 state/createTime。
- 2026-08-19：按渐进式披露/按需加载重构 CODEBUDDY.md 为入口速览，详情拆分到 docs/{commands,architecture,conventions}.md。
