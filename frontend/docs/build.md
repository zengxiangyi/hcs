# 构建与部署手册（Build & Deploy）

> 适用范围：本手册覆盖**前端 + 后端**的完整打包与上线流程。
>
> | 工程 | 路径 | 技术 | 产物 |
> |------|------|------|------|
> | 前端 | `f:\hb\page\frontend` | Vue 3 + Vite 8 + TypeScript | `hcs.war`（纯静态，约 0.5 MB） |
> | 后端 | `F:\hb\api` | Spring Boot 4.0.8 + Java 25 + JPA/MyBatis（Maven，`<packaging>war</packaging>`） | `api.war`（约 66 MB，含依赖） |
>
> 最后更新：2026-09-02（对应「前端 `hcs.war` + 后端 `api.war` 同 Tomcat 部署」形态）

---

## 一、部署架构总览

两个 war 部署在**同一个 Tomcat**（实测 `E:\software\tomcat11`，HTTP 端口 `8080`）：

```
┌──────────────┐  http://<host>:8080/hcs/**          ┌───────────────────────────────┐
│              │ ─────────────────────────────────► │  webapps/hcs/   ← hcs.war     │
│   浏览器      │        静态资源（前端 SPA）          │  （WEB-INF/web.xml 做 404 回退）│
│              │                                     │                               │
│              │  http://<host>:8080/api/**          │  webapps/api/   ← api.war     │
│              │ ─────────────────────────────────► │  （Spring Boot，REST）         │
└──────────────┘        REST（同源）                  └───────────────┬───────────────┘
                                                                     │ JDBC
                                                              ┌──────▼───────┐
                                                              │ MySQL 8 库 page│
                                                              └──────────────┘
```

| 组成 | 部署位置 | 访问地址 | 说明 |
|------|----------|----------|------|
| 前端 SPA | `<TOMCAT>\webapps\hcs.war` | `http://<host>:8080/hcs/` | context-path 由 war 文件名决定 |
| 后端 REST | `<TOMCAT>\webapps\api.war` | `http://<host>:8080/api/` | context-path 由 war 文件名决定，**文件名不可改** |
| 数据库 | 独立 MySQL | `127.0.0.1:3306/page` | 连接在 `application.properties` 中硬编码 |

**同源判定**：`/hcs` 与 `/api` 只是 context-path 不同，origin（scheme + host + port）相同 → **同源**。
因此 `localStorage` 共享、axios 请求 `/api/**` **不触发 CORS 与 OPTIONS 预检**。

> 这是本项目最重要的部署前提：只要前端用**相对地址** `/` 作为接口基址，就完全绕开跨域问题（后端 `CorsConfig` 目前存在「带 `Authorization` 的跨域预检被 Security 拦成 401」的已知缺陷，同源可规避）。

---

## 二、环境准备

### 2.1 构建机

| 依赖 | 版本要求 | 校验命令 |
|------|----------|----------|
| JDK | **25**（`pom.xml` 的 `java.version=25`） | `java -version` |
| Maven | 无需全局安装，用仓库自带 Wrapper（`mvnw.cmd`） | — |
| Node.js | ≥ 20.19 或 ≥ 22.12（推荐 22 LTS） | `node -v` |
| npm | ≥ 10 | `npm -v` |
| PowerShell | 5.1+（前端 `build:war` 依赖） | `$PSVersionTable.PSVersionString` |
| bsdtar（可选） | Windows 10+ 自带，用于校验 war 条目 | `tar --version` |

### 2.2 运行机（Tomcat）

| 依赖 | 版本要求 | 说明 |
|------|----------|------|
| Tomcat | **11（基线 10.1+）** | 后端是 Spring Boot 4 / Jakarta EE 11，前端 `web.xml` 用的是 `jakarta` 的 `web-app_6_0`；**Tomcat 9 及以下直接启动失败** |
| JRE/JDK | 25 | 与编译版本一致 |
| MySQL | 8.x | 库 `page`，需**提前建好表**（后端 `ddl-auto=none`，不自动建表） |

⚠️ **端口占用**：Tomcat 默认 `8080`，后端 `application.properties` 的 `server.port` 也是 `8080`（但该值在外置 Tomcat 下**被忽略**，不会冲突）。若本机还要用 `mvnw spring-boot:run` 起后端，需先停 Tomcat 或改用 `--server.port=8090`。

---

## 三、发布前配置检查清单

### 3.1 前端（4 处决定「部署路径」+「接口地址」）

| # | 位置 | 当前值 | 要求 |
|---|------|--------|------|
| 1 | `vite.config.ts` → `base` | `'/hcs/'` | 与 war 文件名 `/hcs` 保持一致 |
| 2 | `.env.production` → `VITE_API_BASE_URL` | `http://127.0.0.1:8080` ⚠️ | **建议改为 `/`**，见下方说明 |
| 3 | `index.html` → `<link rel="icon">` | `/hcs/favicon.svg` | 手写绝对路径，不随 `base` 改写，改 context-path 时必须手动同步 |
| 4 | `public/WEB-INF/web.xml` → 404 回退 | `/index.html`（context 相对） | 使 history 路由刷新可用；context 相对，改路径无需改 |

### 3.2 ⚠️ 待修正：`VITE_API_BASE_URL`

前端 `src/api/*.ts` 里写的都是**完整路径**（`/api/auth/login`、`/api/blueprint/search` …），最终请求地址 = `VITE_API_BASE_URL` + `/api/xxx`。

| 取值 | 拼接结果 | 适用 |
|------|----------|------|
| **`/`（推荐）** | `/api/auth/login`（同源相对） | 前后端同 Tomcat（当前形态），远程客户端也能用，**不触发 CORS** |
| `http://127.0.0.1:8080` | `http://127.0.0.1:8080/api/auth/login` | 仅在**浏览器与 Tomcat 同机**时可用；其他机器访问会去连自己的 8080 而失败 |
| `http://<后端IP>:8080` | 跨源绝对地址 | 前后端不同机；会触发 CORS，需承担 3.3 的预检缺陷 |

> **发布前建议**：把 `.env.production` 改为 `VITE_API_BASE_URL=/`，重新 `npm run build:war`。
> 绝不能填 `/api`（会拼成 `/api/api/...`，全线 404）。
> 环境变量在**构建时**内联进产物，改完必须重新 build 才生效。

### 3.3 开发态代理（与生产无关，但影响联调）

`vite.config.ts` 的 `server.proxy.'/api'.target` 当前为 `http://127.0.0.1:8080`，与本机 Tomcat 端口相同。
开发时二选一：

- 停掉 Tomcat，用 `mvnw spring-boot:run` 起后端（默认 `:8080`，context `/api`），代理原样可用；
- 或后端改端口 `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8090`，并把代理 target 改为 `http://127.0.0.1:8090`。

### 3.4 后端（构建前必须确认）

| 项 | 位置 | 说明 |
|----|------|------|
| **`finalName` 必须为 `api`** | `pom.xml` → `<build><finalName>api</finalName>` | 外置 Tomcat 下 `server.servlet.context-path` 被忽略，context-path **由 war 文件名决定**。改名会让接口前缀从 `/api` 变成别的（如 `/api-1.0.0`），前端全线 404 且**编译/启动期无任何提示** |
| 数据库凭据 | `src/main/resources/application.properties` | `spring.datasource.url/username/password` **明文硬编码**；**没有** profile 机制（`application-prod.properties` 不存在），换环境必须改文件后**重新打包** |
| JWT 配置 | 同上 `jwt.secret` / `jwt.expiration-ms` | 同样硬编码，换环境需改文件重打包 |
| 表结构 | MySQL 库 `page` | `spring.jpa.hibernate.ddl-auto=none`，后端**不建表不改表**；新增实体/字段必须由 DBA 先执行 DDL，否则启动即报错 |

---

## 四、构建步骤

### 4.1 后端（`F:\hb\api`）

```powershell
cd F:\hb\api
.\mvnw.cmd clean package -DskipTests      # 产出 target\api.war（约 66 MB）
```

| 命令 | 用途 |
|------|------|
| `.\mvnw.cmd clean package` | 打包并跑测试（`src/test` 当前为空 → 0 用例，但 `@SpringBootTest` 若存在需 MySQL 可用） |
| `.\mvnw.cmd clean package -DskipTests` | **发布推荐**：跳过测试 |
| `.\mvnw.cmd spring-boot:run` | 本地内嵌容器运行（`:8080/api`），仅联调用，**不是**生产部署方式 |
| `.\mvnw.cmd test` | 跑测试 |

首次构建需联网下载依赖；产物为 `F:\hb\api\target\api.war`（依赖在 `WEB-INF/lib`）。
`spring-boot-devtools` 已被插件自动排除；`ServletInitializer` 保证可外置部署。

校验：

```powershell
Get-Item F:\hb\api\target\api.war | Select-Object Name,Length,LastWriteTime
tar -tf F:\hb\api\target\api.war | Select-String 'WEB-INF/lib/spring-boot' -First 3   # 依赖已打进
```

### 4.2 前端（`f:\hb\page\frontend`）

```powershell
cd f:\hb\page\frontend
npm ci                # 按 package-lock 精确安装（本地开发可用 npm install）
npm run build:war     # vue-tsc 类型检查 + vite build + 打 hcs.war
```

| 命令 | 用途 |
|------|------|
| `npm run build` | `vue-tsc -b` 类型检查 + `vite build` → `dist/` |
| `npm run build:war` | 先 build，再把 `dist/` 打成 `hcs.war`（覆盖旧包） |
| `npm run preview` | 本地预览 `dist/`（**无** 404 回退，history 子路由刷新会 404，仅看首屏） |

#### `build:war` 的打包铁律（勿随意替换实现方式）

用 .NET `System.IO.Compression.ZipArchive` 手工打包，两个必须满足的条件：

1. **必须写入目录条目**：先 `CreateEntry('assets/')`、`CreateEntry('WEB-INF/')`（名称以 `/` 结尾），再写文件条目。
   —— 否则 Tomcat 11 解压时不创建父目录，报
   `ContextConfig.beforeStart 上下文[/hcs]的异常修复docBase → FileNotFoundException: webapps\hcs\assets\xxx.js`。
2. **分隔符必须是正斜杠 `/`**（Windows 路径默认 `\`，需 `.Replace('\','/')`）。

**禁止改回 `Compress-Archive` 或 `[System.IO.Compression.ZipFile]::CreateFromDirectory`**：两者只写文件条目，会触发上述故障。

#### 产物自检（部署前必做）

```powershell
# 1) 条目与目录条目（应能看到 assets/ 与 WEB-INF/）
tar -tf hcs.war

# 2) 资源前缀必须是 /hcs/
Select-String -Path dist\index.html -Pattern '/hcs/assets/' | Select-Object -First 3

# 3) 接口地址正确、无 /api/api 重复前缀
Select-String -Path dist\assets\*.js -Pattern 'api/api' | Measure-Object      # 期望 0
Select-String -Path dist\assets\*.js -Pattern '127\.0\.0\.1:8080' | Measure-Object  # 若 baseURL 已改 '/'，期望 0

# 4) war 必须比 dist 新（确认是最新的包）
(Get-Item hcs.war).LastWriteTime -gt (Get-Item dist).LastWriteTime
```

期望：约 103 个条目（含 `assets/`、`WEB-INF/` 两个目录条目）；资源引用 `/hcs/assets/...`；`api/api` 命中 0。

> 校验 war 条目**只能用 bsdtar（`tar -tf`）**。.NET 的 `ZipArchiveEntry.FullName` 在 Windows 上会把 `/` 显示成 `\`，据此判断分隔符会误判。

---

## 五、部署步骤（Tomcat 11）

> 下面以实测路径为例：`<TOMCAT> = E:\software\tomcat11`。

### 5.1 停止 Tomcat

```powershell
& E:\software\tomcat11\bin\shutdown.bat
# 确认 java 进程已退出（Tomcat 停止需要几秒，尤其是 66MB 的 api 应用）
Get-Process -Name java -ErrorAction SilentlyContinue
```

如未退出，等 10 秒后 `Stop-Process -Name java -Force`（生产环境优先排查而非强杀）。

### 5.2 清理旧部署残留（关键）

失败/半途的部署会在 `webapps` 留下半成品目录，Tomcat **不会**自动重新解压，必须手动删：

```powershell
$webapps = 'E:\software\tomcat11\webapps'
$work    = 'E:\software\tomcat11\work\Catalina\localhost'

Remove-Item -Recurse -Force "$webapps\api", "$webapps\hcs" -ErrorAction SilentlyContinue
Remove-Item -Force "$webapps\api.war", "$webapps\hcs.war" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "$work\api", "$work\hcs" -ErrorAction SilentlyContinue
```

> 实测 `webapps` 下还有一个历史遗留的 `hcs1/` 目录（某次改名部署的产物），确认无用后一并删除，避免占用与混淆。

### 5.3 拷贝新包（先后端，后前端）

```powershell
Copy-Item F:\hb\api\target\api.war          $webapps\     # 约 66 MB
Copy-Item f:\hb\page\frontend\hcs.war       $webapps\     # 约 0.5 MB
```

- **后端 war 文件名必须是 `api.war`**，前端 war 文件名决定前端 context-path（当前 `hcs.war` → `/hcs`）。
- 若 Tomcat 未开启自动部署（`autoDeploy`），需重启才会解压。

### 5.4 启动并观察日志

```powershell
& E:\software\tomcat11\bin\startup.bat

# 跟踪日志（Windows 下 catalina.out 或 catalina.<日期>.log）
Get-ChildItem E:\software\tomcat11\logs | Sort-Object LastWriteTime -Descending | Select-Object -First 5
Get-Content E:\software\tomcat11\logs\catalina.out -Wait -Tail 100
```

启动成功特征：

- 后端：`Started ApiApplication in X seconds`、`Initializing ProtocolHandler ["http-nio-8080"]`
- 无 `HikariPool-1 - Exception during pool initialization`（有则说明 MySQL 未启动/凭据错/库不存在）
- 无 `FileNotFoundException: webapps\hcs\assets\...`（有则说明前端 war 缺目录条目，见 4.2）

### 5.5 冒烟验证清单

| # | 操作 | 期望 |
|---|------|------|
| 1 | 访问 `http://localhost:8080/hcs/` | 登录页正常渲染，控制台无报错 |
| 2 | 直接访问/刷新 `http://localhost:8080/hcs/web/sys/user` | 渲染登录页或业务页（**不能**是 Tomcat 404 页） |
| 3 | 浏览器标签图标 | Network 中 `/hcs/favicon.svg` 返回 200 |
| 4 | 登录后 F12 → Network | 请求地址形如 `http://<host>:8080/api/auth/login`（同源，无 `OPTIONS` 预检，无 CORS 报错） |
| 5 | 后端连通性 | 登录能成功，返回 `token/rights/roles`；错误密码显示后端 `message` |
| 6 | 退出后访问受保护路由 | 跳登录页并带 `redirect` 参数 |
| 7 | 权限 | 菜单/路由按后端下发的 `rights` 过滤（存于 `localStorage.rights`） |

> 第 2 项说明：Tomcat 的 `<error-page>` 以 **forward** 回退，HTTP 状态码仍是 404，但响应体是 `index.html`，浏览器照常渲染。DevTools 里看到 404 属正常，只要页面能渲染即可。

---

## 六、常见问题排查

| 现象 | 原因 | 处理 |
|------|------|------|
| 接口全部 404，地址是 `/api/api/xxx` | `VITE_API_BASE_URL` 里多写了 `/api` | 改为 `/`（同源）或后端 origin 根，重新 `build:war` |
| 远程客户端能开页面但接口全失败 | `.env.production` 写死 `127.0.0.1:8080`，浏览器去连本机 | 改为 `/` 重新构建 |
| 跨域 `OPTIONS` 预检返回 401 | 后端已知缺陷：`CorsConfig` 暴露的是无 `@Order` 的 `CorsFilter`（最低优先级），Security 链先执行拦截了带 `Authorization` 的预检 | 同源部署（baseURL 用 `/`）规避；或后端在 `SecurityConfig` 补 `.cors(...)` 并给 Filter 加顺序 |
| 接口路径变成 `/api-1.0.0/...` | 后端 war 文件名不是 `api.war` | 保持 `pom.xml` 的 `finalName=api`，部署时文件名必须为 `api.war` |
| Tomcat 日志 `FileNotFoundException: webapps\hcs\assets\xxx.js` | 前端 war 缺目录条目 | 用 `build:war` 重打；删掉 `webapps\hcs` 残留后重部署 |
| 刷新子路由出现 Tomcat 404 页 | `WEB-INF/web.xml` 未打进包，或 Tomcat < 10.1 | 用 `tar -tf hcs.war` 确认 `WEB-INF/web.xml` 存在；升级 Tomcat |
| 图标 404 | `index.html` 的 favicon 路径未同步 context-path | 手动改为 `/hcs/favicon.svg` |
| 后端启动失败：`HikariPool-1 - Exception during pool initialization` | MySQL 未启动 / 库 `page` 不存在 / 凭据错误 | 启动 MySQL、建库、核对 `application.properties` 后**重新打包** |
| 后端启动失败：缺表/缺列 | `ddl-auto=none`，表结构需 DBA 手工建 | 提交 DDL 给 DBA 执行后重启 |
| 后端在 Tomcat 下端口不是 8080 / context 不是 `/api` | 外置容器下 `server.port` 与 `server.servlet.context-path` **均被忽略** | 端口看 `conf/server.xml`，context-path 看 war 文件名 |
| Tomcat 起不来，报端口占用 | 本机已有进程占用 8080（如 `spring-boot:run`） | 停掉占用进程或改 Tomcat 端口 |
| 部署后仍是旧页面 | 浏览器缓存 / 未删旧目录 | Ctrl+F5；删除 `webapps/hcs` 与 `work/Catalina/localhost/hcs` 重部署 |
| 改了配置但线上未生效 | 前端环境变量**构建时**内联；后端配置**硬编码需重打包** | 重新构建并部署 |

---

## 七、变更部署路径（context-path）

**后端：不要改。** `api.war` 的文件名即 context-path `/api`，前端所有接口路径（`src/api/*.ts` 中的 `/api/...`）都依赖它；改名的代价是前端全线 404 且无编译期提示。

**前端**要从 `/hcs` 改为其它路径（如 `/app`）时，需改 3 处并重新打包：

1. `vite.config.ts` → `base: '/app/'`
2. `index.html` → `<link rel="icon" href="/app/favicon.svg">`
3. war 文件名改为 `app.war`

无需改动：`public/WEB-INF/web.xml`（404 回退是 context 相对路径）、`src/router/index.ts` 与 `http.ts` 的 401 跳转（都取 `import.meta.env.BASE_URL` 自动跟随）。

---

## 八、回滚与版本管理

```powershell
# 发布前备份
New-Item -ItemType Directory -Force f:\hb\release-backup
Copy-Item f:\hb\page\frontend\hcs.war "f:\hb\release-backup\hcs-<yyyyMMdd-HHmm>.war"
Copy-Item F:\hb\api\target\api.war   "f:\hb\release-backup\api-<yyyyMMdd-HHmm>.war"
```

回滚流程：停 Tomcat → 按 5.2 清理 `webapps` 与 `work` 下对应目录 → 拷入备份 war（**改回原文件名** `hcs.war` / `api.war`，文件名决定 context-path）→ 启动 → 跑 5.5 冒烟清单。

注意：

- 前端是纯静态资源，回滚不涉及数据。
- **后端回滚可能涉及数据库**：后端 `ddl-auto=none`，若新版本配套执行过 DDL（加表/加列），回滚代码前需评估旧版本是否兼容新表结构，必要时先做回退 DDL（由 DBA 执行）。
- `frontend/dist/`、`frontend/hcs.war`、`api/target/` 均被 `.gitignore` 忽略，不入库；发布包请单独归档备份。

---

## 九、命令速查

```powershell
# ---------- 构建 ----------
# 后端
cd F:\hb\api
.\mvnw.cmd clean package -DskipTests        # → target\api.war

# 前端
cd f:\hb\page\frontend
npm ci
npm run build:war                            # → hcs.war

# ---------- 部署 ----------
& E:\software\tomcat11\bin\shutdown.bat
$webapps = 'E:\software\tomcat11\webapps'
$work    = 'E:\software\tomcat11\work\Catalina\localhost'
Remove-Item -Recurse -Force "$webapps\api","$webapps\hcs","$work\api","$work\hcs" -ErrorAction SilentlyContinue
Remove-Item -Force "$webapps\api.war","$webapps\hcs.war" -ErrorAction SilentlyContinue
Copy-Item F:\hb\api\target\api.war    $webapps\
Copy-Item f:\hb\page\frontend\hcs.war $webapps\
& E:\software\tomcat11\bin\startup.bat

# ---------- 验证 ----------
tar -tf f:\hb\page\frontend\hcs.war                                   # 目录条目是否存在
Start-Process 'http://localhost:8080/hcs/'                            # 前端
Start-Process 'http://localhost:8080/api/constValue/category/test'    # 后端（需 token，401 即说明已通）
```

---

## 十、已废弃 / 过时的资料（勿据此操作）

| 项 | 过时内容 | 实际情况 |
|----|----------|----------|
| `F:\hb\build.ps1` | 把前端 `dist/*` 拷进 `F:\hb\api\src\main\resources\static\` 再由后端 war 一并分发 | 该分发方式**已废弃**（2026-09-02 起 `static/` 已删除，后端 war 只提供 REST）。前端改为独立 `hcs.war` 部署。脚本本身也因 `static/` 不存在会执行失败 |
| `F:\hb\page\backend` | 早期 Fastify + Drizzle 的 Node 后端原型 | **不是**当前后端，不参与构建与部署。当前后端是 `F:\hb\api` |
| `F:\hb\api\CODEBUDDY.md` | 写「端口 8090」 | `application.properties` 实际为 `server.port=8080`（外置 Tomcat 下该值被忽略，以 `conf/server.xml` 的 8080 为准） |
| `F:\hb\api\CODEBUDDY.md` | 写「前端打成 `ROOT.war`（context-path `/`）」 | 已改为 `hcs.war`（context-path `/hcs`） |
| `frontend/public/WEB-INF/web.xml` 注释 | 描述为 `ROOT.war`、与后端 `api.war` 同 Tomcat 同源 | 结论（同源）对，但前端文件名表述过时；该文件在只读目录，未改 |
| `frontend/src/router/index.ts` 注释 | 「生产为 ROOT.war，context-path /」 | 实际为 `/hcs`；代码取 `BASE_URL` 自动跟随，无需修改 |
| `frontend/.env.example` | 示例 `VITE_API_BASE_URL=/api` | **错误示例**，会拼成 `/api/api/...`；正确写法是 `/`（同源）或后端 origin 根 |
