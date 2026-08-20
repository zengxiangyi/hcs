# 用户管理后台（page）

基于 **Vue 3 + Vite + TypeScript** 的 SPA 管理后台，用于用户管理与认证。UI 采用 **Element Plus**（中文语言包），数据页支持服务端分页、条件查询与 XLSX 导入导出。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Vue 3（`<script setup>` SFC） |
| 构建 | Vite 8 + TypeScript |
| UI | Element Plus（按需自动导入，zh-cn） |
| 路由 | vue-router 5（全局鉴权守卫） |
| 状态 | Pinia（已注册，当前业务未使用 store） |
| HTTP | axios（统一拦截：Token 注入 / 响应解包 / 401 处理） |
| 其他 | xlsx（Excel 导入导出）、Prettier、ESLint |

## 快速开始

```bash
npm install     # 安装依赖
npm run dev     # 启动开发服务器，默认 http://localhost:5173（/api 代理到 8080）
npm run build   # 类型检查（vue-tsc）+ 生产构建，输出到 dist/
npm run preview # 本地预览生产构建
```

## 项目结构

```
frontend/
├── index.html                  # HTML 入口
├── vite.config.ts              # Vite 配置（dev 代理 / 组件自动导入）
├── src/
│   ├── main.ts                 # 应用入口（Pinia + Element Plus + Router）
│   ├── App.vue                 # 根组件（仅 router-view）
│   ├── api/
│   │   ├── http.ts             # axios 单例 + 拦截器
│   │   ├── base.ts             # 认证端点（登录 / 重置密码 / 用户信息）
│   │   └── data.ts             # 用户 CRUD 端点
│   ├── config/menu.json        # 侧边菜单配置
│   ├── router/index.ts         # 路由表 + 全局鉴权守卫
│   ├── utils/md5.ts            # 手写 MD5（密码加密）
│   └── components/
│       ├── Login.vue           # 登录页 + 两步重置密码
│       ├── Web.vue             # 后台布局（顶栏 + 左侧菜单）
│       ├── MenuBar.vue         # 递归菜单组件
│       └── data/data2.vue      # 用户管理页（CRUD + 分页 + XLSX）
├── docs/struct.md              # 项目结构与 .gitignore 详细说明
└── openspec/                   # 规范驱动开发目录
```

## 请求 / 响应契约

- 后端统一响应：`ApiResponse<T> = { code, data, msg }`
- 响应拦截器已解包，业务数据通过 `res.data` 获取；业务错误通过 `err.message` 拿到后端 `msg`
- HTTP 401 视为 token 失效：清除本地凭证并跳转登录页
- 密码在发送前经 **MD5 客户端加密**

## 认证流程

1. 登录成功后将 token 存入 `localStorage`
2. 路由守卫校验：白名单（登录页）外，先校验本地 token，再调用 `getUserInfo()` 向后端校验有效性
3. token 缺失 / 失效 → 清理并重定向 `/`，携带 `redirect` 参数

## 后端

后端代码位于 `../backend`（Express + Drizzle ORM，默认端口 `8080`）。开发模式下 Vite 将 `/api` 请求代理到 `http://localhost:8080`。
