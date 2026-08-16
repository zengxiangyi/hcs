# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## 常用命令（Commands）

| 命令 | 用途 |
|------|------|
| `npm install` | 安装依赖（首次或 `package.json` 变动后）。 |
| `npm run dev` | 启动 Vite 开发服务器（默认 `http://localhost:5173`），支持 HMR 热更新。 |
| `npm run build` | 先用 `vue-tsc -b` 做 TypeScript 类型检查，再 `vite build` 输出生产产物到 `dist/`。 |
| `npm run preview` | 本地预览 `dist/` 生产构建产物。 |

- 无独立 lint / test / format 脚本；格式化工具为 Prettier（配置见 `.prettierrc.json`）。类型检查已内置于 `npm run build`，单文件快速校验可执行 `npx vue-tsc --noEmit`。

## 架构概览（Architecture）

这是一个基于 **Vue 3 + TypeScript + Vite** 的单页应用（SPA）模板，工程规模小、结构清晰，采用标准的 Vite 脚手架布局。核心特点：`<script setup lang="ts">` 组合式 API、按目录职责划分的源码组织、以及静态资源的分区管理（`public/` 与 `src/assets/`）。

### 依赖与构建链

- 运行时依赖仅 `vue`（^3.5.40）。
- 开发依赖：`vite`（^8.2.0）、`@vitejs/plugin-vue`（^6.0.8，编译 `.vue` SFC）、`typescript`（~6.0.2）、`vue-tsc`（^3.3.8，类型检查）、`@vue/tsconfig`（0.9.x）、`@types/node`。
- 包管理器为 npm（存在 `package-lock.json`）。`package.json` 的 `allowScripts` 显式允许 `esbuild` 与 `vue` 的安装脚本运行。
- 构建流程 `npm run build` 分两步：`vue-tsc -b` 类型检查通过后才执行 `vite build`，因此编译错误会在 CI/本地构建时被拦截。

### 配置体系（多配置文件协同）

TypeScript 采用 **Project References** 模式，根 `tsconfig.json` 仅为容器，无独立编译项，引用两个子项目：

- `tsconfig.app.json` —— 应用源码配置。继承 `@vue/tsconfig/tsconfig.dom.json`，`types: ["vite/client"]` 提供 Vite 环境类型；启用严格校验（`noUnusedLocals`、`noUnusedParameters`、`noFallthroughCasesInSwitch`）。编译范围：`src/**/*.{ts,tsx,vue}`。
- `tsconfig.node.json` —— 构建端配置。目标 `es2023`、模块 `nodenext`、`verbatimModuleSyntax`，仅覆盖 `vite.config.ts`。

两端配置分离，是为了让浏览器侧代码与 Node 侧构建脚本各自获得正确的类型环境（DOM lib vs ES2023 / Node types），避免互相污染。

`vite.config.ts` 仅注册 `@vitejs/plugin-vue` 插件，无别名、无代理等额外配置。若后续需要路径别名（如 `@/`），需同步修改 `vite.config.ts`、`tsconfig.app.json` 的 `paths` 与 `baseUrl`。

### 应用入口与数据流

1. `index.html` 定义挂载点 `<div id="app">`，以 `<script type="module" src="/src/main.ts">` 引入模块入口。
2. `src/main.ts` 调用 `createApp(App).mount('#app')` 创建并挂载根实例，同时 `import './style.css'` 注入全局样式。
3. `src/App.vue` 为根组件，当前仅渲染 `<HelloWorld />` 一个占位组件。

整个组件树目前仅两层，业务组件均位于 `src/components/`。新增页面/路由/状态管理（Vue Router、Pinia）尚未引入，按需扩展。

### 样式与主题

全局样式集中在 `src/style.css`，通过 CSS 变量（`:root` 及 `@media (prefers-color-scheme: dark)` 暗色覆盖）管理配色、字体、阴影，并定义 `#app`、`#center`、`#next-steps`、`#spacer` 等布局规则。样式基于原生 CSS + CSS 嵌套语法（当前构建链无需预处理器）。修改主题色、间距、断点（1024px）时优先调整此处变量。

### 静态资源分区（重要约定）

- `public/`：**不经 Vite 处理、原样拷贝**到产物根目录。`favicon.svg` 被 `index.html` 以绝对路径 `/favicon.svg` 引用；`icons.svg` 是 SVG Sprite（含 `#documentation-icon`、`#github-icon`、`#discord-icon`、`#x-icon`、`#bluesky-icon` 等 `<symbol>`），组件通过 `<use href="/icons.svg#xxx">` 引用。注意：`public/` 下资源始终用绝对路径 `/...` 引用，不能走 `import`。
- `src/assets/`：**参与 Vite 打包**，通过模块 `import` 引入（如 `HelloWorld.vue` 中 `import viteLogo from '../assets/vite.svg'`），会经哈希、压缩等处理。两者用途不可混淆。

### 代码格式化约定

- Prettier 配置 `.prettierrc.json`：`printWidth: 100`、`tabWidth: 2`、单引号、`trailingComma: "es5"`、`arrowParens: "avoid"`、`endOfLine: "auto"`、`vueIndentScriptAndStyle: false`。
- `.prettierignore` 排除 `node_modules`、锁文件、构建产物（`dist`/`build`/`coverage`）、压缩文件、`.env*`、IDE 配置等。格式不受影响的文件不要强行调整。

### 建议的工具链配置

`.vscode/extensions.json` 推荐安装 `Vue.volar`（Vue 3 官方语言服务），以正确解析 `.vue` 文件的类型与模板。
