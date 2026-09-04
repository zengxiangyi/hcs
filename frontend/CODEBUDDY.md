# CODEBUDDY.md

This file provides guidance to CodeBuddy when working with code in this repository.

## Project Overview

`frontend/` 是 **Vue 3 + Vite 8 + TypeScript + Element Plus** 的管理端 SPA，消费后端 REST API（Spring Boot，见 `../backend`）。本文件只做概览与按需加载入口，细节拆到 `docs/ai/` 专题文件。

## Commands

```bash
npm install           # 安装依赖
npm run dev           # Vite dev server（默认 :5173），HMR，/api 代理
npm run build         # vue-tsc -b 类型检查 + vite build → dist/
npm run build:war     # build 后把 dist 打成 hcs.war（可部署 war）
npm run preview       # 本地预览生产构建
```

无 lint 脚本，无单元测试框架。

## 按需加载地图（渐进式披露）

| 何时读 | 读哪个文件 |
|--------|-----------|
| 执行 build:war、排查部署/子路由刷新 404 | `docs/ai/deploy-war.md` |
| 写页面组件、改路由/权限/接口层 | `docs/ai/architecture.md` |
| 改 vite.config / env、排查接口地址问题 | `docs/ai/config-env.md` |
| 跨端契约（响应体/JWT/接口地址两处改） | `../docs/ai/contract.md` |
| 一键发布脚本用法 | `../docs/ai/deploy.md` |

## 最小必读事实

- 生产部署为 **`hcs.war`，Tomcat context-path `/hcs`**；`vite.config.ts` 的 `base = '/hcs/'`，路由自动跟随。
- `VITE_API_BASE_URL`（`.env.production`）只填 origin 根，**绝不能带 `/api`**；`server.proxy` 不参与打包，改生产接口地址必须重新 `build:war`。
- 统一响应体 `ApiResponse<T> = { code, data, msg }`（`src/api/http.ts` 已解包）；HTTP 401 = token 失效 → 清 localStorage → 跳登录。
- Element Plus 自动导入（`unplugin-vue-components`），无需手动注册 `el-*`。
- 密码客户端 MD5 后发送。
