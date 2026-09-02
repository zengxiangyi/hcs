# MEMORY.md（长期记忆）

## 项目全局结构

- 物理布局：`f:\hb\page\` 下平铺两个独立工程：
  - `frontend/` —— Vue 3 + Vite + TypeScript SPA（本工作区）
  - `backend/` —— Fastify 5 + Drizzle ORM + mysql2 + MySQL，端口 8080
- Vite dev 将 `/api` 代理到 `http://127.0.0.1:8080`（无 rewrite）。
- **部署形态（2026-09-02 起）**：前端 `npm run build:war` 产出 `hcs.war`，部署到 Tomcat 的 `webapps/hcs/`（context-path `/hcs`）；`vite.config.ts` 的 `base = '/hcs/'`，路由 history 与 401 跳转均取 `import.meta.env.BASE_URL` 自动跟随。`public/WEB-INF/web.xml` 的 404 回退 `/index.html` 为 context 相对路径，无需改。
- **打 war 铁律**：必须包含目录条目（`assets/`、`WEB-INF/`）且分隔符为正斜杠，否则 Tomcat 11 解压时报 `FileNotFoundException: webapps\hcs\assets\xxx.js`（不创建父目录）。`Compress-Archive` 与 .NET `ZipFile::CreateFromDirectory` 都不写目录条目，只能用 `ZipArchive` 手工写（见 `package.json` 的 `build:war`）。查条目名用 bsdtar `tar -tf`，别用 .NET 的 `FullName`（Windows 下会把 `/` 显示成 `\`）。
- 前后端各自维护 `CODEBUDDY.md` 与 `docs/`，两端均有 `openspec/` 目录但内容为空（截至 2026-09-01）。

## 跨端契约

- 统一响应包装：`ApiResponse<T> = { code, data, msg }`，成功 `code === 200`。
- 前端 axios 响应拦截器已解包，接口方法返回 `Promise<ApiResponse<T>>`，业务数据取 `res.data`，后端 `msg` 通过 `err.message` 抛出。
- HTTP 401 = token 失效（前端清 `localStorage.token` 并跳 `/`）；登录失败后端返回 **400** 而非 401。
- 列表分页统一入参 `page` / `pageSize`，统一返回 `{ list, total, page, pageSize }`。
- 认证：`Authorization: Bearer <token>`，JWT 有效期 8h；后端白名单 `/api/auth/*`、`/api/health`。
- 密码：前端 MD5 后发送，后端 `md5(...)` 直存直比（**非加盐**，安全性待改进）。
- **URL 拼接约定（重要）**：后端 Fastify 路由自带 `/api` 前缀，前端 `src/api/*.ts` 里写的也是完整路径 `/api/xxx`，所以 `VITE_API_BASE_URL` 只能是**后端 origin 根**（生产为 `http://127.0.0.1:8080`，dev 未设 → `/`）。**绝不能填 `/api`**，否则拼出 `/api/api/...` 404（典型症状：`http://127.0.0.1:8080/api/api/auth/login`）。若日后同域反代，改填 `/`。

## 已知结构性风险

- **前后端接口严重不对齐**：前端 `src/api/` 有 15 个模块，后端 `src/routes/` 仅 `auth.ts`、`users.ts`。其余（flow / process / transfer / blueprint / tech / instance / taskProcess / role / right / roleUser / roleRight / constValue 等）为前端按"约定路径"先行编写，后端未实现。
- `docs/api.md` 自述"接口端点为前端约定路径，若后端实际路径不同需调整" —— 属前端猜测后端，是主要返工来源。

## 约定与偏好

- 每次回复末尾需输出「任务状态摘要」（已完成 / 进行中 / 待处理 / 已阻止 / 备注），摘要前后各留一个空行。
- 禁止修改 `.idea/`、`.vscode/`、`public/`、`node_modules/`（工作区规则）。
- 前端 Element Plus 全局注册 + `unplugin-vue-components` 自动导入 `el-*`，无需手动注册（`components.d.ts` 自动生成）。
