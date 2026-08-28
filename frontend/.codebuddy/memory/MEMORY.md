# MEMORY.md（长期记忆）

> 最后整理：2026-08-28。只放跨会话稳定的项目约定与架构事实；日常改动细节见 `YYYY-MM-DD.md`。

## 项目
- 工作区 `f:/hb/page/frontend`：Vue 3 + Vite + TypeScript + Element Plus 的管理系统前端；后端在 `../backend`。
- 技术栈：vue 3.5.x / vite 8.x / element-plus 2.14.x / vue-router 5.x / axios 1.x / xlsx 0.18.5。无单元测试框架，**无 lint 脚本**（ESLint 于 08-18 整体移除）。
- 命令：`npm run dev`(5173)、`npm run build`(vue-tsc -b + vite build)、`npm run preview`。

## 架构约定（稳定）
- `src/api/http.ts` 是唯一 axios 实例：请求头注入 `Authorization: Bearer <token>`；响应解包 `ApiResponse<T> = {code,data,msg}`，调用方用 `res.data` 取业务数据。
- **HTTP 401 = token 失效**（清 token + 硬跳转 `/`）；业务失败一律用 400，不可返回 401（曾导致登录失败被整页跳转覆盖）。非 2xx 时解包 body 的 `{code,msg}` 以 `Error(msg)` reject。
- **项目未配置 `@` 别名**，所有 import 必须相对路径。
- Vite `/api` 代理：dev 指向 `http://127.0.0.1:8090`（CODEBUDDY.md 里写的 8080 已过时）。
- 密码前端 MD5（`src/utils/md5.ts`）后提交，后端直存直比。
- Element Plus 全局注册 + `zh-cn` locale；import 路径必须是 `element-plus/es/locale/lang/zh-cn.mjs`（`.js` 在 2.11+ 会导致全站白屏）。
- `unplugin-vue-components` 自动引入 `el-*`；图标需显式 `import { Xxx } from '@element-plus/icons-vue'`。
- 公共页面样式在全局 `src/style/common.css`（`.page-title` / `.query-form` / `.toolbar` / `.el-table` / `.pagination`），由 `main.ts` 引入；多数页面已删除 scoped 同名规则改为复用（08-27）。
- 全局色变量（`src/style.css` `:root`）：`--color-text-main`、`--color-text-aux`（均 #000）；表格文字色也在 `common.css` 统一为黑。
- 子路由 path 必须相对（不带 `/`）才能嵌进父级 `<router-view/>`。

## 权限
- `src/components/sys/permission.ts` 为权限中心：`setCurrentUser(rightCodes: string[])` 写 localStorage `rights`；`restoreCurrentUserRights()`；`hasRight(code)`。router 与 MenuBar 依赖它。
- `src/components/sys/mock.ts` **已于 08-26 彻底删除**，sys 全页面改调真实 API。
- 菜单（`src/config/menu.json`）与路由（`src/router/index.ts`）权限需成对维护；系统管理 5 个子菜单/路由的 `right` 已移除（全部可见），登录流程尚未下发 rights。
- **测试阶段（08-26 起）临时移除 user/role/right 三页的按钮权限控制**（`hasRight` 现无调用方；恢复时重新加 `canAdd/canEdit/canDelete`）。
- `constValue.vue` 按钮权限走 `baseAPI.getUserButtons(pageCode)`，`rightsReady` 前默认放开。

## 业务模块现状
- `tech/`：`board.vue` 为工艺编制页；方案组件放 `src/components/tech/plan/`，命名 `TZxx.vue`，注册键 = craftTree 二级工艺 value；现有 `TZ01.vue`（列：段号/温度/时间/备注）。方案表格数组务必 `reactive(model.value.rows)`，勿用只读 computed。
- `workflow/draw.vue`：流程图设计页（节点表 + 连线表上下排列 + canvas 绘图），读路由 query `flowGraph`；`src/api/flow.ts` 约定 `/api/flow/save|get`。
- **流程相关接口统一在 `src/api/flow.ts`**（08-28 已合并原 `src/api/graph.ts`，该文件已删除）：`flowAPI`(save/get/graph/node/edge)、`flowNodeAPI`、`flowEdgeAPI`（`/api/flowNode/*`、`/api/flowEdge/*`）、`graphAPI`（`/api/flowGraph/*`，非 `/api/graph/*`）；`GraphRow/GraphSaveDTO/GraphSearchParams/GraphSearchResult` 也在 flow.ts。字段 `heght` 按用户原始拼写保留。
- `src/api/transfer.ts` + `product/transfer.vue`：调拨单列表/新增/绑定蓝本（`/api/transfer/*`）。
- `src/api/taskProcess.ts` + `process/task.vue`：工序任务（`/api/taskProcess/*`）。
- `src/api/tech.ts`：`save` → `POST /api/tech/board`、`getByCode` → `GET /api/blueprint/code?code=`。

## 待办 / 待后端实现
- 后端需补：`/api/transfer/*`（search/create/bind-blueprint）、`/api/taskProcess/*`、`/api/flowGraph/*`（search/save/update/删除）、`/api/flow/*`、`/api/flowNode/*`、`/api/flowEdge/*`、`GET /api/blueprint/code?code=`。
- 登录接口 `/api/auth/login` 返回体需带 `rights: string[]`（用户自行处理）。
- 其余 8 种工艺方案组件（TZ02…）待建并注册进 `planComponentMap`。
- 未使用依赖（bpmn-js/echarts/@vueuse/core/dayjs）与构建 chunk > 500kB 警告为既有问题，未处理。

## 工作约定
- 只读禁改目录：`.idea`、`.vscode`、`public`、`node_modules`。
- 每次回复末尾输出任务状态摘要（已完成/进行中/待处理/已阻止/备注）。
- IDE TS/Volar 诊断有陈旧缓存（尤其 `roleRright.vue`），需用户重启 TS Server；不要据陈旧诊断改代码，先 grep 验证。
- 后端实现细节不写入前端记忆，只记前端工作 + 跨端契约。
