# 前端配置与环境变量（按需加载：改 vite.config / env、排查接口地址问题时读取）

## vite.config.ts

- `base: '/hcs/'` — 生产部署为 Tomcat context-path `/hcs`；路由与 401 跳转均用 `BASE_URL`，context-path 变化时无需改其他代码。
- `server.proxy`：dev 时 `/api` → target（无 rewrite）；**对生产构建完全无效**。
- `unplugin-vue-components` + `ElementPlusResolver` 自动导入。

## 环境变量

- `.env.production` / `.env.example`：`VITE_API_BASE_URL` = 后端 origin 根（如 `http://10.21.46.191:8080`；同源部署填 `/`）。
- dev 请求走相对 `/api/...` 命中代理；生产 URL = `VITE_API_BASE_URL + '/api/...'`。

## 关键陷阱

- **`VITE_API_BASE_URL` 绝不能带 `/api`**：`src/api/*.ts` 路径已以 `/api` 开头，加了会变成 `/api/api/auth/login`（404）。devtools 中看到 `http://127.0.0.1:8080/api/api/...` 即此症状。
- **改接口地址要改两处**：dev 改 `server.proxy.target`；生产改 `.env.production` 且**必须重新 `npm run build:war`**（proxy 不参与打包）。
