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
- **URL 风格定稿（2026-09-05 用户确认）**：**动词式 URL，不严格 RESTful——URL 要反馈操作方法信息**。约定形态：查询 `POST /xxx/search`（复杂条件）或 `GET /xxx/list`；创建 `POST /xxx/save`；更新 `PUT /xxx/update`（id 在 body，null 抛 IAE→400）；删除 `DELETE /xxx/{id}`；**按字段过滤/定位用路径段**（`/state/{state}`、`/category/{category}`、`/workflow/{workflow}`、`/code/{code}` 等，保留不改 query 参数）。新接口照此写，勿为对齐纯 REST 改成裸资源/`PUT /{id}`/query 过滤。错误语义：404 只走全局 `ResourceNotFoundException`，Controller 不手写 404 code；手写 `error(400,...)`（HTTP 200）仅剩 Auth/绑定类业务校验，前端拦截器双通道兼容。均已写入 `docs/ai/contract.md` 第 2 条。
- **改后端接口地址要改两个不同地方**（2026-09-04 踩坑）：
  - dev → `frontend/vite.config.ts` 的 `server.proxy.target`（重启 dev 生效）；
  - 生产/发布 → `frontend/.env.production` 的 `VITE_API_BASE_URL`（**必须重新 `npm run build:war`**）。
  `server.proxy` 不参与打包，改它对发布后的页面完全无效。值可填后端 IP（`http://10.21.46.191:8080`），同源同 Tomcat 时填 `/` 更省心（跟随浏览器 origin、免重打包、无跨域；后端 `CorsConfig` 已放开 origin `*`，填 IP 跨域也通）。
- 后端 context-path 由 war 名决定，必须保持 `api.war`，否则全线 404 且无编译期提示。
- `ddl-auto=none`：所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行。
- DB 列名一律小写无下划线；MyBatis 的 XML 是查询真源。
- **列名大小写规则（2026-09-05 用户定稿，推翻旧「以库中实际拼写为准」）**：**以代码为准——JPA `@Column` / Mapper XML 列名一律全小写**（唯一例外：`flownode` 表 `X/Y/W/H` 四个大写列，`@Column` 必须写大写，不参与小写检查）；DB 侧由 DBA 把库中驼峰列改名小写对齐代码，DDL 见 `backend/docs/plans/2026-09-05-lowercase-columns.sql`（15 表 68 列），**已于 2026-09-05 由 DBA 执行完毕，AI 实测验证通过（information_schema 0 行残留）——代码/文档/库三者列名完全一致**。拼写遗留已于 2026-09-05 由 DBA 纠正并实测验证：`approval.sartTime→starttime`、`flowgraph.heght→height`；代码侧 FlowGraph 实体本就映射 height、approval 表无任何代码映射，零改动。`workflow.id` 是 **bigint** 不是 int。
- **时间/数值字段多为 varchar**（2026-09-05）：`flowhistory.dealTime` varchar(30)、`taskprocess.auditTime/createTime/updateTime` varchar(20)、`techstep.sort/isNeed` varchar(45)、`blueprint` 的 weight/isFirstCheck/busbarNum 等 varchar(100)。做范围查询/排序会受影响，后续可优化。
- **表名也是全小写**（与 JPA `@Table` 一致）。MySQL 列名大小写不敏感，但**表名在 Linux（lower_case_table_names=0）大小写敏感**：Mapper XML / 原生 SQL 里写成 `flowNode` 这类驼峰会直接报 `Table 'page.flowNode' doesn't exist`（2026-09-04 踩坑，见当日日志）。写 SQL 前先核对 `entity/*.java` 的 `@Table(name=...)`。
- **SQL 表名 schema 前缀约定（2026-09-05 用户确认，多轮讨论定稿）**：Mapper XML / 原生 SQL 中表名一律硬编码 `page.` 前缀（如 `FROM page.sysuser`、`from page.flowhistory`、`from page.flowcurrent a, page.flownode n`）。理由：①一个 DB 实例下可有多个 schema，库名归 DBA 管理、不轻易改，但 JDBC URL 的库名参数在**多 schema 环境极易被混用/配错**；②后期还要支持**多 schema 联合查询**，表名必须显式带 schema 才能在跨 schema SQL 中定位；③把前缀写进 URL 等于把库归属交给了易被误改的部署配置，一旦漏填/错填所有不带前缀的 SQL 会全线找不到表。故**前缀绝不进 JDBC URL、硬编码在 SQL/XML**；库名若真要改或新增 schema，需同步改全部 Mapper XML（换库即断风险已知并接受）。
- **表文档列名规范（2026-09-05 用户确认）**：`backend/docs/DB/table/*.md` 的字段清单 `COLUMN_NAME` 列**统一小写、无下划线**，对齐 XML/entity `@Column` 与 README「列名一律小写」规则；唯一例外是 `flownode` 表的 `X/Y/W/H` 四个大写列（JPA `@Column` 也必须大写才能映射）。库中实列已于 2026-09-05 由 DBA 全量改名小写，文档/代码/库完全一致。改表结构文档时列名保持小写（X/Y/W/H 除外）。
- **后端日志级别**（2026-09-04 踩坑）：项目无自定义 `logback-spring.xml`，走 Spring Boot 默认，**根级别 INFO**；`application.properties` 需逐个显式开 `logging.level.<包>=DEBUG`（已开 `com.baogang.info.mapper`、`com.baogang.info.tool`）。项目**无 actuator/devtools**，`logging.level.*` 不会热更新，**改完必须重新 `mvn package` + 重启 Tomcat**（`.\deploy-test.ps1 -Part Back`）。未配 `logging.file.name`，日志只进 console（Tomcat 控制台窗口 / `logs/catalina.*.log`）。
- 受保护（只读）：`.idea/`、`script/`、`config/` 目录；后端另有 `mvnw`/`mvnw.cmd`/`info.iml`/`target/`/`.mvn/`。

## 数据库（MCP）

- MCP 有 3 个 MySQL 数据源：`mysql127`、`mysql186`、`mysql197`，各带 `execute_sql` 工具。
- **`mysql127` = 本机 127.0.0.1:3306，root，默认库 `sakila`，MySQL 8.0.46**，已实测连通（2026-09-05）。
- 库清单：`erp`、`hcs`、`hcs_test`、`page`、`sakila`、`world`、`information_schema` 等。
  - **`page` = 本工作区 backend 的库，共 18 张表**：approval(10列)/blueprint(31)/constvalue(6)/flowcurrent(6)/flowedge(11)/flowgraph(6)/flowhistory(12)/flownode(14)/sysright(6)/sysrole(5)/sysroleright(4)/sysroleuser(4)/sysuser(10)/taskprocess(13)/techstep(8)/transferorder(23)/users(6)/workflow(11)。（2026-09-05 实测，此前记录的「16 表」有误）
  - 表结构文档已全量导出到 `backend/docs/DB/table/<表名>.md`（18 个文件）。**公共部分（数据源/生成时间/通用生成 SQL/通用约定/18 表清单索引）统一放在同目录 `README.md`，每个表文件只保留本表独有的信息**（2026-09-05 压缩，此前重复的开头块与「说明」尾部通用句已提取）。
  - 表文件模板：标题 + `` `> page.<表名> · 表注释` `` 一行 → 「字段清单」表（序号·COLUMN_NAME·类型·COLUMN_COMMENT，**类型与长度合并写成 `varchar(100)`**）→ 「说明」（只写本表特有注意点，无则省略）。统一 `order by ordinal_position`（物理列序）。
  - **schema 真源 = `backend/docs/DB/table/`**（2026-09-05 起）。旧真源 `backend/docs/tables.md`（15 表，由 entity `@Column` 提取）**已按用户要求删除**，其引用已全量改写；`backend/CODEBUDDY.md`、`docs/ai/contract.md`、`docs/ai/conventions.md`、`docs/structure.md`、`backend/docs/ai/db-conventions.md` 均指新路径。
  - `hcs`（35 表）、`hcs_test`、`erp`（11 表，sys_*）为其它系统/历史库。
- 注意：MCP 建连默认库是 `sakila`，查本项目数据时 SQL 要显式带库名前缀（如 `page.sysuser`）或先 `USE page`。

## 记忆文件分布（写日志/周报必读三处）

1. `f:\hb\page\.codebuddy\memory`（工作区根，仅近期）
2. `frontend\.codebuddy\memory`（前端主日志，含 MEMORY.md）
3. `backend\.codebuddy\memory`（后端日志，含 MEMORY.md）

## 分页页码约定（用户 2026-09-05 明确，同日补充验证/上限规则）

- **契约 1 基，Service 一律 0 基页码**（2026-09-05 定稿）：请求与响应（含 `PageResult.page`）均为 1 基；**Service 方法收到的页码参数是 0 基页码，不是行偏移**（旧文档写成「0 基偏移」是错的）。
- 完整链路：`PageParam.of(page, size)` 归一 → Controller 取 `p.page0()`（= 契约页码 - 1）传给 Service → JPA 侧直接 `PageRequest.of(page, size)`，MyBatis 侧由 Service 自己算 LIMIT 偏移 `(long) pageOffset * size` → 返回 `PageResult.of(..., pageOffset + 1, size)` 还原 1 基（范本：SysRightController → SysRightService.search → SysRightMapper.xml `LIMIT #{offset}, #{limit}`）。
- **分页参数验证与上限规则**（用户 2026-09-05 明确；同日抽取为公共 `PageParam`）：前端把查询条件与分页参数封装在一个 Query DTO（如 `SysUserQuery`，含 page/pageSize）POST 到 `/search`；**Controller 层负责验证与限制**——page/pageSize 为 null 时退回 DTO 默认值（1 / 10），page 最小 1，size 限 1~200（`MAX_PAGE_SIZE`），防负 offset 报错与超大结果集。**MyBatis 和 JPA 一律按服务端算好的分页参数计算**，Service 层不做二次校验。
- **统一入口 `com.baogang.info.common.PageParam`**（2026-09-05 建立，已铺满 16 个 Controller / 19 处入口）：`record PageParam(int page, int size)`；`PageParam.of(page, size)` 做归一（默认 1/10、page 下限 1、size 夹 1~`ConstValue.MAX_PAGE_SIZE`）；**`p.page0()` 给 Service 的 0 基页码**，`p.size()` 给每页条数，`p.page()` 保留契约 1 基（不要传给 Service）。注释只写在 PageParam 里，Controller 侧不重复写注释。新增分页接口照此写法，不要再手写 `Math.max/min` 或 `page - 1`。
- 命名沿革（勿回退）：该方法原名 `offset()`，返回 `page - 1`，与分页语境「offset = 行偏移」的常识冲突，是长期误导源；2026-09-05 正名为 `page0()`，16 个 Controller / 19 处入口已同步。
- **两侧 Service 写法无例外**：JPA（`listPaged`/`findBySender`）形参 `int page, int size` → `PageRequest.of(page, size)`；MyBatis（`search`/`todo`/`done`）形参 `int pageOffset, int size` → `(long) pageOffset * size` 得 LIMIT 偏移（long 防大页码溢出）。两类均 `+1` 还原 1 基响应。
- 历史修复：原 JPA `listPaged` 响应 `page` 为 0 基未 +1，2026-09-05 已把 13 个 Service（14 处）统一为 `page + 1`，全后端响应 `page` 现为 1 基。改动需 `.\deploy-test.ps1 -Part Back` 生效。

## 用户偏好

- 每次回复末尾必须输出「任务状态摘要」（已完成/进行中/待处理/已阻止/备注），前后各空一行。
- **「待处理」里不要再列「重新打包部署（`.\deploy-test.ps1 -Part Back`）」和「跑 `mvn compile` 做编译确认」**（用户 2026-09-05 明确）：这些是多次改动攒够后统一执行的收尾动作，不是每次对话的遗留项。同理，也不要主动跑部署/编译脚本。但仍需在正文里说明「改动需重新打包部署才生效」，让用户自己掌握时机。
