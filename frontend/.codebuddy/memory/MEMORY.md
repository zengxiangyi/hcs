# MEMORY.md（长期记忆）

## 项目全局结构

- 物理布局（2026-09-02 更正，**后端已确认换人**）：
  - `f:\hb\page\frontend` —— Vue 3 + Vite 8 + TypeScript SPA（本工作区）
  - `F:\hb\api` —— **当前真后端**：Spring Boot 4.0.8 + Java 25 + JPA/MyBatis，Maven `<packaging>war</packaging>`，`finalName=api` → `api.war`；MySQL 库 `page`（127.0.0.1:3306）。
  - `F:\hb\page\backend` —— 早期 Fastify + Drizzle 的 Node 原型，**已废弃**，不参与构建/部署，勿再引用。
- Vite dev 将 `/api` 代理到 `http://127.0.0.1:8080`（无 rewrite，对应 `mvnw spring-boot:run` 的默认端口）。
- **部署形态（2026-09-02 起）**：
  - 前端 `npm run build:war` 产出 `hcs.war`，部署到 Tomcat 的 `webapps/hcs/`（context-path `/hcs`）；`vite.config.ts` 的 `base = '/hcs/'`，路由 history 与 401 跳转均取 `import.meta.env.BASE_URL` 自动跟随。`public/WEB-INF/web.xml` 的 404 回退 `/index.html` 为 context 相对路径，无需改。
  - 后端 `.\mvnw.cmd clean package -DskipTests` 产出 `target\api.war`（约 66MB），部署到同一个 Tomcat 的 `webapps/api.war`（context-path `/api`）。**war 文件名不可改**（外置 Tomcat 忽略 `server.servlet.context-path`，改名 → 接口前缀变化 → 前端全线 404）。
  - 实测 Tomcat：`E:\software\tomcat11`，端口 `8080`（`conf/server.xml`）。前后端同机同端口 → **同源**（context-path 不同不影响 origin），localStorage 共享、不触发 CORS。
  - 后端无 profile 机制，DB 凭据与 JWT secret 明文硬编码在 `src/main/resources/application.properties`，换环境需改文件后重打包；`ddl-auto=none`，表结构由 DBA 手工维护。
  - 已废弃：`F:\hb\build.ps1`（把 dist 拷进 `api/src/main/resources/static/` 随后端 war 分发的方式已停止，`static/` 已删除）。
- **打 war 铁律**：必须包含目录条目（`assets/`、`WEB-INF/`）且分隔符为正斜杠，否则 Tomcat 11 解压时报 `FileNotFoundException: webapps\hcs\assets\xxx.js`（不创建父目录）。`Compress-Archive` 与 .NET `ZipFile::CreateFromDirectory` 都不写目录条目，只能用 `ZipArchive` 手工写（见 `package.json` 的 `build:war`）。查条目名用 bsdtar `tar -tf`，别用 .NET 的 `FullName`（Windows 下会把 `/` 显示成 `\`）。
- 前后端各自维护 `CODEBUDDY.md` 与 `docs/`，两端均有 `openspec/` 目录但内容为空（截至 2026-09-01）。

## 跨端契约

- 统一响应包装：`ApiResponse<T> = { code, message, data }`，成功 `code === 200`；分页 `PageResult<T> = { content, total, page, size }`（1-based `page`）。
- 前端 axios 响应拦截器已解包，接口方法返回 `Promise<ApiResponse<T>>`，业务数据取 `res.data`，后端 `message` 通过 `err.message` 抛出。
- HTTP 401 = token 失效（前端清 `localStorage.token` 并跳 `/`）；业务异常后端返回 400 / 404。
- 认证：`Authorization: Bearer <token>`，Spring Security 全无状态，后端只放行 `/api/auth/**`，无 `/api/health` 端点；登录后返回 `{ token, user, rights, roles }`，前端把 `rights` 存入 `localStorage.rights` 做路由/菜单鉴权。
- 密码：前端 MD5 后发送。
- **URL 拼接约定（重要，2026-09-02 更新）**：后端 context-path `/api` + Controller 路径（`/auth/login`、`/blueprint/search` …），前端 `src/api/*.ts` 里写的也是完整路径 `/api/xxx`。最终地址 = `VITE_API_BASE_URL` + `/api/xxx`。
  - **生产推荐填 `/`**：前后端同 Tomcat 同源，远程客户端也能用，且不触发 CORS（可规避后端 `CorsConfig` 的「带 Authorization 的跨域 OPTIONS 预检被 Security 拦成 401」已知缺陷）。
  - 当前 `.env.production` 仍是 `http://127.0.0.1:8080`，**仅在浏览器与 Tomcat 同机时可用**，远程访问会失败 —— 发布前建议改为 `/`。
  - **绝不能填 `/api`**，否则拼出 `/api/api/...` 404。

## 已知结构性风险

- ~~前后端接口严重不对齐~~（2026-09-02 已缓解）：真后端 `F:\hb\api` 已覆盖 blueprint / workflow / flowNode / flowCurrent / transfer / taskProcess / constValue / auth / sys* 等模块，前端 15 个 api 模块基本有对应 Controller。少数差异仍需逐个核对（如前端调 `/api/auth/logout`，后端 AuthController 无此映射）。
- `docs/api.md` 是早期按"约定路径"写的，与真后端可能有出入，以 `F:\hb\api` 的 Controller 为准。
- 后端已知缺陷：`CorsConfig` 暴露无 `@Order` 的 `CorsFilter`（LOWEST_PRECEDENCE），而 `HttpSecurity` 未调 `.cors(...)` → 带自定义头的跨域 OPTIONS 预检被拦成 401（同源部署可规避）。
- 构建/部署的完整流程见 `docs/build.md`（2026-09-02 建立，已按 Spring Boot war 后端重写）。

## 约定与偏好

- 每次回复末尾需输出「任务状态摘要」（已完成 / 进行中 / 待处理 / 已阻止 / 备注），摘要前后各留一个空行。
- 禁止修改 `.idea/`、`.vscode/`、`public/`、`node_modules/`（工作区规则）。
- 前端 Element Plus 全局注册 + `unplugin-vue-components` 自动导入 `el-*`，无需手动注册（`components.d.ts` 自动生成）。
