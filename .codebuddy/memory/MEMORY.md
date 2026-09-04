# MEMORY（长期记忆）

## 工作区结构（用户明确约定）

- 工作区根：`f:\hb\page`
- **`frontend/` = 前端页面根目录**
- **`backend/` = 后端接口根目录**
- 结构详情见 `docs/structure.md`（2026-09-03 建立，以该文件为准）

## 技术栈与部署

- 前端：Vue 3 + Vite 8 + TypeScript + Element Plus；`npm run build:war` 产出 `hcs.war`，Tomcat context-path `/hcs`；`vite.config.ts` 的 `base = '/hcs/'`，dev 代理 `/api` → `http://127.0.0.1:8080`。
- 后端：Spring Boot 4.0.8 / Java 25 / Maven war（`finalName=api`），根包 `com.baogang.info`，JPA + MyBatis 双持久化，MySQL 库 `page`，context-path `/api`。
- 两个 war 同部署于 `E:\software\tomcat11`（端口 8080），**同源**（origin 只看 scheme/host/port），localStorage 共享、调 `/api/**` 无 CORS。
- **该同 Tomcat 形态是临时/过渡方案，后期会调整**（用户 2026-09-03 确认）。测试阶段不追求规范化部署，以快为优先。
- **测试阶段一键发布**：`f:\hb\page\deploy-test.ps1`（`-Part All|Front|Back`、`-NoBuild`、`-NoTail`、`-Force`、`-Tomcat`）。日常发布跑它即可，不必走 `frontend/docs/build.md` 的多步手工流程；部署形态变更时只改这个脚本。改前端页面用 `.\deploy-test.ps1 -Part Front` 最快（不必重打 66MB 后端包）。
- **`F:\hb\page\backend` 就是主本**（Spring Boot，107 个 java 文件），是唯一的后端接口根目录（用户 2026-09-03 确认）。`F:\hb\api` 已不存在，任何涉及后端的改动都在 `f:\hb\page\backend` 下进行。

## 项目约定

- 接口前缀：前端 `src/api/*.ts` 写完整路径 `/api/xxx`，故 `VITE_API_BASE_URL` 只能填 origin 根，**绝不能再带 `/api`**。
- 后端 context-path 由 war 名决定，必须保持 `api.war`，否则全线 404 且无编译期提示。
- `ddl-auto=none`：所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行。
- DB 列名一律小写无下划线；MyBatis 的 XML 是查询真源。
- 受保护（只读）：`.idea/`、`script/`、`config/` 目录；后端另有 `mvnw`/`mvnw.cmd`/`info.iml`/`target/`/`.mvn/`。

## 记忆文件分布（写日志/周报必读三处）

1. `f:\hb\page\.codebuddy\memory`（工作区根，仅近期）
2. `frontend\.codebuddy\memory`（前端主日志，含 MEMORY.md）
3. `backend\.codebuddy\memory`（后端日志，含 MEMORY.md）

## 用户偏好

- 每次回复末尾必须输出「任务状态摘要」（已完成/进行中/待处理/已阻止/备注），前后各空一行。
