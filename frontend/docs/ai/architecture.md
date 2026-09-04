# 前端架构（按需加载：写页面组件、改路由/权限、改接口层时读取）

## 请求契约

- `src/api/http.ts` — 唯一 axios 实例（`TypedHttp`）：请求拦截器注入 `Authorization: Bearer <token>`（取自 localStorage）；响应拦截器解包 `ApiResponse<T> = { code, data, msg }` 并处理 401。
- API 方法返回 `Promise<ApiResponse<T>>`，调用方业务数据取 `res.data`，错误取 `err.message`（如"用户名或密码错误"）。
- HTTP 401 = token 过期 → 清 `localStorage.token` → 硬跳 `/`。

## 接口层（17 个模块，均为 /api/** 完整路径）

`auth` `/api/auth/**`、`user` `/api/sysUser/**`、`role` `/api/sysRole/**`、`right` `/api/sysRight/**`、`roleUser`、`roleRight`、`constValue`、`blueprint`、`tech`（复用 blueprint）、`techStep`、`process`（复用 sysRight）、`taskProcess`、`transfer`、`approval` `/api/workflow/**`、`instance`（workflow|flowCurrent|flowHistory）、`flow`（flowGraph|flowNode|flowEdge）。

## 路由与权限

- `src/router/index.ts` — 路由表 + 全局 `beforeEach` 守卫。白名单 `WHITE_LIST`（目前 Root/Login）跳过认证；其余路由需本地有 token 且后端 `getUserInfo()` 验证通过，失败清 token 带 `redirect` 跳 `/`。
- 业务页全挂 `/web` 子路由，且带 `meta.right`（`page:xxx` 权限码），由守卫 + `sys/permission.ts` 的 `hasRight()` 做页面级拦截。
- 路由用 `createWebHistory(import.meta.env.BASE_URL)`，跟随 vite `base` 自动适配 context-path。

## 组件结构

- `Login.vue`（登录 + 两步重置密码）、`Web.vue`（认证后布局外壳：顶栏 + 左菜单 + router-view）、`MenuBar.vue`（由 `src/config/menu.json` 驱动的递归菜单）。
- 业务页目录：`bluePrint/`、`tech/`（board/step/draft）、`approval/`（send/todo/done）、`workflow/`（flow/draw/instance）、`process/`（task/process + steps/S01~S16）、`product/`、`info/`、`sys/`（user/role/right/roleUser/roleRight/constValue）。

## 其他

- Element Plus 在 `main.ts` 全局注册（zh-cn）；`unplugin-vue-components` 自动导入 `el-*`（见 `vite.config.ts` + 自动生成的 `components.d.ts`，勿手改）。
- `src/utils/md5.ts` 手写 MD5，密码发送前客户端加密。
- 已删除模板遗留文件（HelloWorld.vue 等）；`data2.vue` 的导出 pageSize 硬编码 99999 拉全量。
