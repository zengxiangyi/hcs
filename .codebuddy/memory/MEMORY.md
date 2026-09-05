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
- **改后端接口地址要改两个不同地方**（2026-09-04 踩坑）：
  - dev → `frontend/vite.config.ts` 的 `server.proxy.target`（重启 dev 生效）；
  - 生产/发布 → `frontend/.env.production` 的 `VITE_API_BASE_URL`（**必须重新 `npm run build:war`**）。
  `server.proxy` 不参与打包，改它对发布后的页面完全无效。值可填后端 IP（`http://10.21.46.191:8080`），同源同 Tomcat 时填 `/` 更省心（跟随浏览器 origin、免重打包、无跨域；后端 `CorsConfig` 已放开 origin `*`，填 IP 跨域也通）。
- 后端 context-path 由 war 名决定，必须保持 `api.war`，否则全线 404 且无编译期提示。
- `ddl-auto=none`：所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行。
- DB 列名一律小写无下划线；MyBatis 的 XML 是查询真源。
- **列名/表名大小写例外**（2026-09-05 实测）：`page` 库列名绝大多数为驼峰（`flowGraph`、`roleList`）或全小写，但 **`flownode` 表的 `X/Y/W/H` 四个列名是大写**，JPA `@Column` 必须写成大写才能映射上；`workflow.id` 是 **bigint** 不是 int；库中另有拼写遗留：`approval.sartTime`（疑 startTime 笔误）、`flowgraph.heght`（疑 height 笔误）——均以库中实际拼写为准，勿擅自纠正。
- **时间/数值字段多为 varchar**（2026-09-05）：`flowhistory.dealTime` varchar(30)、`taskprocess.auditTime/createTime/updateTime` varchar(20)、`techstep.sort/isNeed` varchar(45)、`blueprint` 的 weight/isFirstCheck/busbarNum 等 varchar(100)。做范围查询/排序会受影响，后续可优化。
- **表名也是全小写**（与 JPA `@Table` 一致）。MySQL 列名大小写不敏感，但**表名在 Linux（lower_case_table_names=0）大小写敏感**：Mapper XML / 原生 SQL 里写成 `flowNode` 这类驼峰会直接报 `Table 'page.flowNode' doesn't exist`（2026-09-04 踩坑，见当日日志）。写 SQL 前先核对 `entity/*.java` 的 `@Table(name=...)`。
- **后端日志级别**（2026-09-04 踩坑）：项目无自定义 `logback-spring.xml`，走 Spring Boot 默认，**根级别 INFO**；`application.properties` 需逐个显式开 `logging.level.<包>=DEBUG`（已开 `com.baogang.info.mapper`、`com.baogang.info.tool`）。项目**无 actuator/devtools**，`logging.level.*` 不会热更新，**改完必须重新 `mvn package` + 重启 Tomcat**（`.\deploy-test.ps1 -Part Back`）。未配 `logging.file.name`，日志只进 console（Tomcat 控制台窗口 / `logs/catalina.*.log`）。
- 受保护（只读）：`.idea/`、`script/`、`config/` 目录；后端另有 `mvnw`/`mvnw.cmd`/`info.iml`/`target/`/`.mvn/`。

## 数据库（MCP）

- MCP 有 3 个 MySQL 数据源：`mysql127`、`mysql186`、`mysql197`，各带 `execute_sql` 工具。
- **`mysql127` = 本机 127.0.0.1:3306，root，默认库 `sakila`，MySQL 8.0.46**，已实测连通（2026-09-05）。
- 库清单：`erp`、`hcs`、`hcs_test`、`page`、`sakila`、`world`、`information_schema` 等。
  - **`page` = 本工作区 backend 的库，共 18 张表**：approval(10列)/blueprint(31)/constvalue(6)/flowcurrent(6)/flowedge(11)/flowgraph(6)/flowhistory(12)/flownode(14)/sysright(6)/sysrole(5)/sysroleright(4)/sysroleuser(4)/sysuser(10)/taskprocess(13)/techstep(8)/transferorder(23)/users(6)/workflow(11)。（2026-09-05 实测，此前记录的「16 表」有误）
  - 表结构文档已全量导出到 `backend/docs/DB/table/<表名>.md`（18 个文件）。**公共部分（数据源/生成时间/通用生成 SQL/通用约定/18 表清单索引）统一放在同目录 `README.md`，每个表文件只保留本表独有的信息**（2026-09-05 压缩，此前重复的开头块与「说明」尾部通用句已提取）。
  - 表文件模板：标题 + `` `> page.<表名> · 表注释` `` 一行 → 「字段清单」表（序号·COLUMN_NAME·类型·COLUMN_COMMENT，**类型与长度合并写成 `varchar(100)`**）→ 「说明」（只写本表特有注意点，无则省略）。统一 `order by ordinal_position`（物理列序）。
  - `hcs`（35 表）、`hcs_test`、`erp`（11 表，sys_*）为其它系统/历史库。
- 注意：MCP 建连默认库是 `sakila`，查本项目数据时 SQL 要显式带库名前缀（如 `page.sysuser`）或先 `USE page`。

## 记忆文件分布（写日志/周报必读三处）

1. `f:\hb\page\.codebuddy\memory`（工作区根，仅近期）
2. `frontend\.codebuddy\memory`（前端主日志，含 MEMORY.md）
3. `backend\.codebuddy\memory`（后端日志，含 MEMORY.md）

## 用户偏好

- 每次回复末尾必须输出「任务状态摘要」（已完成/进行中/待处理/已阻止/备注），前后各空一行。
