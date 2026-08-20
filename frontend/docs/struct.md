# 项目结构说明（Project Structure）

> 本文档基于 `f:\hb\page\frontend` 根目录的完整文件树整理，描述每个文件 / 文件夹的作用与用途。
> 项目类型：基于 **Vue 3 + Vite + TypeScript** 的**用户管理后台**单页应用（SPA），UI 采用 Element Plus，后端 API 由 Vite 开发代理转发。

---

## 一、`.gitignore` 解析（被忽略的文件 / 目录）

> 来自 `f:\hb\page\frontend\.gitignore`，逐行说明。

| 行号 | 规则 | 含义 / 用途 |
|------|------|--------------|
| 2 | `logs` | 整个 `logs` 目录（通常是运行时日志输出） |
| 3 | `*.log` | 任何后缀为 `.log` 的日志文件 |
| 4 | `npm-debug.log*` | npm 调试日志 |
| 5 | `yarn-debug.log*` | yarn 调试日志 |
| 6 | `yarn-error.log*` | yarn 错误日志 |
| 7 | `pnpm-debug.log*` | pnpm 调试日志 |
| 8 | `lerna-debug.log*` | lerna（Lerna monorepo 工具）调试日志 |
| 10 | `node_modules` | npm/pnpm/yarn 安装的依赖包目录，体积巨大，不纳入版本控制 |
| 11 | `dist` | Vite 默认构建产物目录（生产环境输出） |
| 12 | `dist-ssr` | SSR（服务端渲染）构建产物目录 |
| 13 | `*.local` | 本地环境配置文件（如 `.env.local`），通常含敏感信息 |
| 16 | `.vscode/*` | VS Code 编辑器配置目录下的所有内容 |
| 17 | `!.vscode/extensions.json` | **例外**：保留 `.vscode/extensions.json`（推荐扩展清单） |
| 18 | `.idea` | JetBrains 系列 IDE（WebStorm、IntelliJ IDEA）配置目录 |
| 19 | `.DS_Store` | macOS Finder 自动生成的目录元数据文件 |
| 20 | `*.suo` | Visual Studio 用户解决方案选项文件 |
| 21 | `*.ntvs*` | Node.js Tools for Visual Studio 生成的辅助文件 |
| 22 | `*.njsproj` | Node.js 项目文件（VS 旧版 Node.js 插件） |
| 23 | `*.sln` | Visual Studio 解决方案文件 |
| 24 | `*.sw?` | Vim 交换文件（`*.swp` / `*.swo`） |

---

## 二、根目录文件

| 文件 | 作用 |
|------|------|
| `index.html` | 应用入口 HTML。Vite 在构建时将 `/src/main.ts` 注入此文件，挂载点是 `<div id="app">`；`<link rel="icon" href="/favicon.svg">` 定义浏览器标签图标。 |
| `package.json` | npm 项目描述文件（name: `page`）。声明脚本（`dev` / `build` / `preview`）与依赖（Vue、Element Plus、Pinia、vue-router、axios、xlsx 等）。 |
| `package-lock.json` | npm 自动生成的**精确依赖锁文件**，锁定依赖版本与依赖树，确保团队/CI 环境下安装一致。 |
| `vite.config.ts` | Vite 构建配置：注册 `vue()` 与 `vueDevTools()` 插件；`unplugin-vue-components` + `ElementPlusResolver()` 按需自动导入 Element Plus 组件并生成 `components.d.ts`；开发代理 `/api` → `http://localhost:8080`。 |
| `tsconfig.json` | TypeScript 根配置。采用 **Project References** 模式，仅作为容器，分别引用 `tsconfig.app.json` 和 `tsconfig.node.json`。 |
| `tsconfig.app.json` | 应用源码的 TS 配置。继承 `@vue/tsconfig/tsconfig.dom.json`，启用 `vite/client` 类型、`noUnusedLocals`、`noUnusedParameters` 等严格检查；编译范围：`src/**/*.{ts,tsx,vue}`。 |
| `tsconfig.node.json` | Node 端代码（Vite 配置）的 TS 配置。目标 `es2023`，模块 `nodenext`，启用 `verbatimModuleSyntax`；编译范围：`vite.config.ts`。 |
| `CODEBUDDY.md` | **AI 助手项目指引**：记录项目架构（请求/响应契约、数据流、路由守卫、配置与遗留项等），供 CodeBuddy 在写码时参考。 |
| `components.d.ts` | `unplugin-vue-components` 自动生成的全局组件类型声明（含 Element Plus 组件与 `src/components` 下的本地组件）。由 Vite 插件在 dev/build 时自动重新生成，无需手动维护。 |
| `README.md` | 项目说明文档：技术栈、快速开始、项目结构、认证流程与请求契约。 |
| `.gitignore` | Git 版本控制忽略规则（详见上文）。 |
| `.prettierrc.json` | Prettier 代码格式化配置：`printWidth: 100`、`tabWidth: 2`、`singleQuote`（单引号）、`semi: true`、`trailingComma: "es5"`、`arrowParens: "avoid"`、`endOfLine: "auto"`、`vueIndentScriptAndStyle: false` 等。 |
| `.prettierignore` | Prettier 格式化时忽略的文件/目录清单：`node_modules`、锁文件、构建产物、日志、`.env*`、IDE 目录、`.DS_Store`、`Thumbs.db` 等。 |

> 后端代码位于 `../backend`（原 `../server`，已重命名），不属于本前端工作区。

---

## 三、`docs/` — 项目文档

| 文件 | 用途 |
|------|------|
| `docs/struct.md` | 本文件，记录项目结构与 `.gitignore` 解析。 |

---

## 四、`openspec/` — 规范驱动开发目录

| 文件 / 目录 | 用途 |
|------|------|
| `openspec/config.yaml` | OpenSpec 配置（`schema: spec-driven`），可定义项目上下文、产物规则与操作指导。 |
| `openspec/changes/` | 变更提案目录（含 `archive/` 已归档变更）。 |
| `openspec/specs/` | 主规范定义目录（当前为空）。 |

---

## 五、`public/` — 静态资源（不经 Vite 处理，原样拷贝到产物根目录）

| 文件 | 用途 |
|------|------|
| `public/favicon.svg` | 浏览器标签页图标，被 `index.html` 通过 `<link rel="icon" href="/favicon.svg">` 引用。 |

---

## 六、`src/` — 应用源码（Vite 编译入口）

### 6.1 根级文件

| 文件 | 用途 |
|------|------|
| `src/main.ts` | 应用启动入口。`createApp(App)` 后依次注册 **Pinia**（`createPinia()`）、**Element Plus**（`zh-cn` 中文语言包）、**vue-router**，再 `mount('#app')`。 |
| `src/App.vue` | 根组件。内容仅为 `<router-view />`，所有页面由路由驱动渲染。 |
| `src/style.css` | 全局基础样式（基础字体设置 + `body` 重置），由 `main.ts` 引入。模板专用样式（`.hero`、`#center`、`#next-steps` 等）已全部移除。 |

### 6.2 `src/api/` — 接口层

| 文件 | 用途 |
|------|------|
| `src/api/http.ts` | axios 单例封装，统一拦截：请求拦截注入 `Authorization: Bearer <token>`（读自 `localStorage`）；响应拦截解包统一响应体 `ApiResponse<T> = { code, data, msg }`，`code !== 200` 时以 `err.message` 抛出后端 `msg`；HTTP 401 视为 token 失效，清理本地凭证并跳转 `/`。导出类型化 `TypedHttp`（get/post/put/delete 返回 `Promise<ApiResponse<T>>`）。 |
| `src/api/base.ts` | 认证相关端点 + TS 类型：`/api/auth/login`、`/api/auth/verify-identity`、`/api/auth/reset-password`、`/api/user/info`，封装为 `baseAPI` 对象。 |
| `src/api/data.ts` | 用户 CRUD 端点 + TS 类型：`/api/users` 列表（服务端分页）/新增/修改/删除，封装为 `dataAPI` 对象。 |

### 6.3 `src/config/` — 静态配置

| 文件 | 用途 |
|------|------|
| `src/config/menu.json` | 侧边菜单配置（`menu` 数组，支持 `children` 多级）：首页 `/web`、数据（数据2 `/web/data/data2`）。驱动 `MenuBar` 递归渲染。 |

### 6.4 `src/router/` — 路由

| 文件 | 用途 |
|------|------|
| `src/router/index.ts` | 路由表 + 全局前置守卫。路由：`/` 与 `/login` → `Login`；`/web` → `Web` 布局，子路由 `/web/data/data2` → `data2`。守卫逻辑：白名单（`Login`）直接放行；其余路由先校验本地 token 是否存在，再调用 `baseAPI.getUserInfo()` 向后端校验有效性，失败则清理 token 并重定向登录页（携带 `redirect` 参数）。 |

### 6.5 `src/utils/` — 工具

| 文件 | 用途 |
|------|------|
| `src/utils/md5.ts` | 手写 MD5 实现。密码在**发送前**先经 MD5 加密（登录与重置密码均使用）。 |

### 6.6 `src/components/` — 组件目录

| 文件 | 用途 |
|------|------|
| `src/components/Login.vue` | 登录页。表单提交登录（密码 MD5 加密），成功后保存 token 并跳转 `/web`；内含**两步重置密码对话框**：① 验证手机号 + 邮箱 → ② 设置新密码。 |
| `src/components/Web.vue` | 认证后的**布局外壳**：顶部标题栏（欢迎语 + 当前用户 + 退出按钮）、左侧菜单（`MenuBar`）、右侧 `<router-view />` 渲染子页面。 |
| `src/components/MenuBar.vue` | **递归菜单**组件。默认读取 `config/menu.json`，支持传入 `items` 属性；父级点击展开/收起，叶子节点 `router.push` 跳转。 |
| `src/components/data/data2.vue` | **完整 CRUD 实现**：服务端分页 + 条件查询（姓名/角色/部门/状态）、新增/编辑对话框（表单校验）、删除、XLSX **导入**（按表头"姓名/角色/部门/状态"逐行新增）与**导出**（以 `pageSize: 99999` 拉全量后生成 xlsx）。 |

---

## 七、整体结构总览

```
frontend/
├── CODEBUDDY.md                  # AI 助手项目指引（架构说明）
├── README.md                     # 项目说明（技术栈 / 快速开始 / 认证流程）
├── .gitignore                    # Git 忽略规则
├── .prettierrc.json              # Prettier 格式化配置
├── .prettierignore               # Prettier 忽略规则
├── index.html                    # HTML 入口
├── package.json                  # npm 项目配置（name: page）
├── package-lock.json             # 依赖锁文件
├── vite.config.ts                # Vite 构建配置（代理 + 自动导入）
├── tsconfig.json                 # TS 根配置（容器）
├── tsconfig.app.json             # 应用源码 TS 配置
├── tsconfig.node.json            # 构建端 TS 配置
├── components.d.ts               # 自动生成的组件类型声明
├── docs/
│   └── struct.md                 # 项目结构说明（本文件）
├── openspec/                     # 规范驱动开发目录
│   ├── config.yaml
│   ├── changes/                  # 变更提案（含 archive/）
│   └── specs/                    # 主规范
├── public/
│   └── favicon.svg               # 浏览器标签图标
└── src/                          # 源码
    ├── main.ts                   # 应用启动入口（Pinia + Element Plus + Router）
    ├── App.vue                   # 根组件（仅 router-view）
    ├── style.css                 # 全局基础样式
    ├── api/                      # 接口层
    │   ├── http.ts               # axios 单例 + 拦截器（Token/解包/401）
    │   ├── base.ts               # 认证端点（登录/重置密码/用户信息）
    │   └── data.ts               # 用户 CRUD 端点
    ├── config/
    │   └── menu.json             # 侧边菜单配置
    ├── router/
    │   └── index.ts              # 路由表 + 全局守卫
    ├── utils/
    │   └── md5.ts                # 手写 MD5（密码加密）
    └── components/
        ├── Login.vue             # 登录页 + 两步重置密码
        ├── Web.vue               # 后台布局外壳（顶栏 + 左侧菜单）
        ├── MenuBar.vue           # 递归菜单组件
        └── data/
            └── data2.vue         # 完整 CRUD + 分页 + XLSX 导入导出
```

---

## 八、路由表

| 路径 | 名称 | 组件 | 说明 |
|------|------|------|------|
| `/`、`/login` | `Login` | `components/Login.vue` | 登录页（白名单，免鉴权） |
| `/web` | `Web` | `components/Web.vue` | 后台布局（需登录） |
| `/web/data/data2` | `Data2` | `components/data/data2.vue` | 用户管理 CRUD 页 |

---

## 九、请求 / 响应契约

- 后端统一响应包装：`ApiResponse<T> = { code, data, msg }`，定义于 `src/api/http.ts`。
- 响应拦截器已解包该包装，API 方法返回 `Promise<ApiResponse<T>>`，业务数据通过 `res.data` 获取；业务错误通过 `err.message` 拿到后端 `msg`（如「用户名或密码错误」）。
- **HTTP 401 语义**：视为 token 已失效 → 清除 `localStorage.token` 并硬跳转 `/`。
- 密码采用 **MD5 客户端加密**后传输（见 `src/utils/md5.ts`）。

---

## 十、运行与构建

| 命令 | 作用 |
|------|------|
| `npm install` | 安装依赖（首次或依赖变动后） |
| `npm run dev` | 启动 Vite 开发服务器（默认 `http://localhost:5173`），支持 HMR 热更新；`/api` 请求代理到 `http://localhost:8080` |
| `npm run build` | 先用 `vue-tsc -b` 做 TypeScript 类型检查，再用 `vite build` 输出生产产物到 `dist/` |
| `npm run preview` | 本地预览生产构建产物 |

---

## 十一、技术栈与依赖（来自 `package.json`）

**核心运行时：**
- **Vue** ^3.5.40（`<script setup>` SFC）
- **Element Plus** ^2.14.4 + `@element-plus/icons-vue` ^2.3.2（UI 组件库，zh-cn 语言包）
- **vue-router** ^5.2.0（路由）
- **Pinia** ^4.0.3 + `pinia-plugin-persistedstate` ^4.7.1（状态管理；已在 `main.ts` 注册，但当前页面尚未使用 store）
- **axios** ^1.19.0（HTTP 客户端）
- **xlsx** ^0.18.5（Excel 导入导出，`data2.vue` 使用）
- **bpmn-js** ^18.24.0、**echarts** ^6.1.0、**dayjs** ^1.11.21、**@vueuse/core** ^14.4.0（已声明，当前业务未使用）

**构建 / 工具链：**
- **Vite** ^8.2.0 + `@vitejs/plugin-vue` ^6.0.8
- **TypeScript** ~6.0.2、**vue-tsc** ^3.3.8、**@vue/tsconfig** ^0.9.1、**@types/node** ^24.13.3
- **unplugin-vue-components** ^32.1.0（自动导入 Element Plus 并生成 `components.d.ts`）、**unplugin-auto-import** ^21.1.0
- **vite-plugin-vue-devtools** ^8.2.1（Vue DevTools）
- **Prettier** 3.9.6、**eslint** ^10.8.1
- **包管理**：npm（`package-lock.json` 存在）

> 注意：`package.json` 中**未配置**单元测试框架与 lint 脚本；`eslint` 仅作为依赖声明存在。

---

## 十二、已知遗留 / 待清理项

| 项目 | 说明 |
|------|------|
| `Login.vue` | 含 `debugger;` 调试语句（`handleLogin` 内），建议移除。 |
| `package.json` 依赖 | `bpmn-js`、`echarts`、`@vueuse/core`、`dayjs` 已声明但当前业务未使用，可按需移除。 |
