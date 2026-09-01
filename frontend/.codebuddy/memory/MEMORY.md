# MEMORY.md（长期记忆）

## 项目全局结构

- 物理布局：`f:\hb\page\` 下平铺两个独立工程：
  - `frontend/` —— Vue 3 + Vite + TypeScript SPA（本工作区）
  - `backend/` —— Fastify 5 + Drizzle ORM + mysql2 + MySQL，端口 8080
- Vite dev 将 `/api` 代理到 `http://localhost:8080`。
- 前后端各自维护 `CODEBUDDY.md` 与 `docs/`，两端均有 `openspec/` 目录但内容为空（截至 2026-09-01）。

## 跨端契约

- 统一响应包装：`ApiResponse<T> = { code, data, msg }`，成功 `code === 200`。
- 前端 axios 响应拦截器已解包，接口方法返回 `Promise<ApiResponse<T>>`，业务数据取 `res.data`，后端 `msg` 通过 `err.message` 抛出。
- HTTP 401 = token 失效（前端清 `localStorage.token` 并跳 `/`）；登录失败后端返回 **400** 而非 401。
- 列表分页统一入参 `page` / `pageSize`，统一返回 `{ list, total, page, pageSize }`。
- 认证：`Authorization: Bearer <token>`，JWT 有效期 8h；后端白名单 `/api/auth/*`、`/api/health`。
- 密码：前端 MD5 后发送，后端 `md5(...)` 直存直比（**非加盐**，安全性待改进）。

## 已知结构性风险

- **前后端接口严重不对齐**：前端 `src/api/` 有 15 个模块，后端 `src/routes/` 仅 `auth.ts`、`users.ts`。其余（flow / process / transfer / blueprint / tech / instance / taskProcess / role / right / roleUser / roleRight / constValue 等）为前端按"约定路径"先行编写，后端未实现。
- `docs/api.md` 自述"接口端点为前端约定路径，若后端实际路径不同需调整" —— 属前端猜测后端，是主要返工来源。

## 约定与偏好

- 每次回复末尾需输出「任务状态摘要」（已完成 / 进行中 / 待处理 / 已阻止 / 备注），摘要前后各留一个空行。
- 禁止修改 `.idea/`、`.vscode/`、`public/`、`node_modules/`（工作区规则）。
- 前端 Element Plus 全局注册 + `unplugin-vue-components` 自动导入 `el-*`，无需手动注册（`components.d.ts` 自动生成）。
