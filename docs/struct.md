# 项目结构说明（Project Structure）

> 本文档基于 `f:\hb\viteDemo\page` 根目录的完整文件树整理，描述每个文件 / 文件夹的作用与用途。
> 项目类型：基于 **Vue 3 + TypeScript + Vite** 的前端单页应用（SPA）模板。

---

## 一、`.gitignore` 解析（被忽略的文件 / 目录）

> 来自 `f:\hb\viteDemo\page\.gitignore`，逐行说明。

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
| `package.json` | npm 项目描述文件。声明脚本（`dev` / `build` / `preview`）、依赖（`vue`、`vite`、`@vitejs/plugin-vue`、`typescript`、`vue-tsc` 等）以及 `allowScripts`（允许运行 `esbuild`、`vue` 的安装脚本）。 |
| `package-lock.json` | npm 自动生成的**精确依赖锁文件**，锁定依赖版本与依赖树，确保团队/CI 环境下安装一致。 |
| `vite.config.ts` | Vite 构建配置。注册 `@vitejs/plugin-vue` 插件以支持 Vue SFC（`.vue` 文件）编译。 |
| `tsconfig.json` | TypeScript 根配置。采用 **Project References** 模式，仅作为容器，分别引用 `tsconfig.app.json` 和 `tsconfig.node.json`。 |
| `tsconfig.app.json` | 应用源码的 TS 配置。继承 `@vue/tsconfig/tsconfig.dom.json`，启用 `vite/client` 类型、`noUnusedLocals`、`noUnusedParameters` 等严格检查；编译范围：`src/**/*.{ts,tsx,vue}`。 |
| `tsconfig.node.json` | Node 端代码（Vite 配置）的 TS 配置。目标 `es2023`，模块 `nodenext`，启用 `verbatimModuleSyntax`；编译范围：`vite.config.ts`。 |
| `README.md` | 项目说明文档。简介 Vue 3 + TypeScript + Vite 模板用法，附 `<script setup>` 与官方 TypeScript 指南链接。 |
| `.gitignore` | Git 版本控制忽略规则（详见上文）。 |

---

## 三、`docs/` — 项目文档

| 文件 | 用途 |
|------|------|
| `docs/struct.md` | 本文件，记录项目结构与 `.gitignore` 解析。 |

> 未来可在此目录扩展架构说明、组件文档、接口契约等。

---

## 四、`public/` — 静态资源（不经 Vite 处理，原样拷贝到产物根目录）

| 文件 | 用途 |
|------|------|
| `public/favicon.svg` | 浏览器标签页图标，被 `index.html` 通过 `<link rel="icon" href="/favicon.svg">` 引用。 |
| `public/icons.svg` | SVG Sprite 图标库，含 `#documentation-icon`、`#social-icon`、`#github-icon`、`#discord-icon`、`#x-icon`、`#bluesky-icon` 等 `<symbol>`，供 `HelloWorld.vue` 通过 `<use href="/icons.svg#xxx">` 引用。 |

---

## 五、`src/` — 应用源码（Vite 编译入口）

### 5.1 根级文件

| 文件 | 用途 |
|------|------|
| `src/main.ts` | 应用启动入口。`createApp(App).mount('#app')` 创建 Vue 应用实例并挂载到 DOM；顶部 `import './style.css'` 注入全局样式。 |
| `src/App.vue` | 根组件。采用 `<script setup lang="ts">`，当前渲染 `<HelloWorld />` 作为占位主视图。 |
| `src/style.css` | 全局样式。定义 CSS 变量（颜色、字体、阴影、明暗主题）、布局（`#app`、`#center`、`#next-steps`、`#spacer`）、按钮与导航样式；支持 `prefers-color-scheme: dark` 自动切换黑夜模式。 |

### 5.2 `src/components/` — 组件目录

| 文件 | 用途 |
|------|------|
| `src/components/HelloWorld.vue` | 模板示例组件。包含：(1) Hero 区域（`hero.png` 底图 + `vue.svg` / `vite.svg` 3D 透视叠加）；(2) 计数器按钮（演示 `ref` 响应式）；(3) 文档入口卡片（链接到 vite.dev / vuejs.org）；(4) 社交链接卡片（GitHub / Discord / X.com / Bluesky，使用 `public/icons.svg` 中的 SVG Sprite）。 |

### 5.3 `src/assets/` — 模块化资源（被 Vite 处理，参与打包）

| 文件 | 用途 |
|------|------|
| `src/assets/hero.png` | Hero 区域底图（被 `HelloWorld.vue` 引用为 `heroImg`）。 |
| `src/assets/vite.svg` | Vite Logo，被 `HelloWorld.vue` 引用为 `viteLogo`。 |
| `src/assets/vue.svg` | Vue Logo，被 `HelloWorld.vue` 引用为 `vueLogo`。 |

> 这些资源以模块方式被 `import`，Vite 会进行哈希、压缩、可选 inline 处理。

---

## 六、整体结构总览

```
page/
├── .gitignore                    # Git 忽略规则
├── index.html                    # HTML 入口
├── package.json                  # npm 项目配置
├── package-lock.json             # 依赖锁文件
├── vite.config.ts                # Vite 构建配置
├── tsconfig.json                 # TS 根配置（容器）
├── tsconfig.app.json             # 应用源码 TS 配置
├── tsconfig.node.json            # 构建端 TS 配置
├── README.md                     # 项目说明
├── docs/
│   └── struct.md                 # 项目结构说明（本文件）
├── public/                       # 原样拷贝的静态资源
│   ├── favicon.svg
│   └── icons.svg
└── src/                          # 源码
    ├── main.ts                   # 应用启动入口
    ├── App.vue                   # 根组件
    ├── style.css                 # 全局样式
    ├── assets/                   # 模块化资源（参与打包）
    │   ├── hero.png
    │   ├── vite.svg
    │   └── vue.svg
    └── components/               # Vue 组件
        └── HelloWorld.vue
```

---

## 七、运行与构建

| 命令 | 作用 |
|------|------|
| `npm install` | 安装依赖（首次或依赖变动后） |
| `npm run dev` | 启动 Vite 开发服务器（默认 `http://localhost:5173`），支持 HMR 热更新 |
| `npm run build` | 先用 `vue-tsc -b` 做 TypeScript 类型检查，再用 `vite build` 输出生产产物到 `dist/` |
| `npm run preview` | 本地预览生产构建产物 |

---

## 八、技术栈版本（来自 `package.json`）

- **运行时**：Vue 3.5.x
- **构建**：Vite 8.x + `@vitejs/plugin-vue` 6.x
- **类型**：TypeScript 6.x、`vue-tsc` 3.x、`@vue/tsconfig` 0.9.x
- **Node 类型**：`@types/node` 24.x
- **包管理**：npm（package-lock.json 存在）
