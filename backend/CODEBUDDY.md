# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## Overview

Spring Boot 4.0.8 / Java 25 后端（Maven，WAR 打包，`finalName=api`），根包 `com.baogang.info`，连接本地 MySQL 库 `page`，对外 REST 服务端口 8090、上下文路径 `/api`。

业务分四块：

- **蓝本（BluePrint）** — 工艺蓝本主数据，字段最多、查询条件最复杂，是「双持久化」范式的样板模块，新增模块优先照抄它。
- **工作流（FlowGraph / FlowNode / FlowEdge + Workflow / FlowCurrent / FlowHistory + `FlowEngine`）** — 图定义 + 流程实例流转引擎。
- **系统权限（SysUser / SysRole / SysRight / SysRoleRight / SysRoleUser）** — 账号、角色、权限及关联关系。
- **单据与常量（TaskProcess / TransferOrder / ConstValue）**。

## Commands

Maven Wrapper 构建（Windows 用 `./mvnw.cmd`；Git Bash / PowerShell 亦可用 `./mvnw`）。**无独立 lint 步骤**。

| 目的 | 命令 |
| --- | --- |
| 构建打包（产出 `target/api.war`） | `./mvnw clean package` |
| 跳过测试打包 | `./mvnw clean package -DskipTests` |
| 本地运行（`:8090/api`） | `./mvnw spring-boot:run` |
| 全部测试 | `./mvnw test` |
| 单个测试类 | `./mvnw test -Dtest=XxxTest` |
| 单个测试方法 | `./mvnw test -Dtest=XxxTest#method` |

`src/test` 目前为空、仓库内没有任何测试类，`./mvnw test` 会编译通过并跑 0 个用例。`@SpringBootTest` 加载完整上下文，需本地 MySQL 运行于 `127.0.0.1:3306/page`（凭据见 `application.properties`），否则上下文启动失败。

**WAR 外置部署**：`ServletInitializer` 存在即有外置容器部署诉求，Baseline 为 **Servlet 6.1 / Tomcat 10.1+**；外置 Tomcat 9.x（Servlet 4.0）部署会直接启动失败。外置时 `server.servlet.context-path` 与 `server.port` **均被忽略**：context-path 由 WAR 文件名决定，必须保持 `api.war`（见 `pom.xml` 的 `finalName` 注释）。`spring-boot-devtools` 由 `spring-boot-maven-plugin` 的 `excludeDevtools` 默认自动排除，不会打进 WAR。

**前端不再随本 WAR 分发**（2026-09-02 起）：`src/main/resources/static/` 已删除，本 WAR 只提供 REST。前端 vite 产物单独打成 `ROOT.war`（context-path `/`），与本 WAR 同 Tomcat 部署——context-path 不同不影响同源判定（origin 只看 scheme/host/port），因此 localStorage 共享、axios 调 `/api/**` 不触发 CORS。相应地 `SecurityConfig.PUBLIC_PATHS` 只保留 `/auth/**`，不再放行任何静态路径。前端打包见 `f:/hb/page/frontend` 的 `npm run build:war`。

## Architecture

### 分层与职责

`controller → service → repository(JPA) / mapper(MyBatis) → entity`

- **`controller/`** — `@RestController`，仅做参数校验与 `ApiResponse` 包装，不写业务逻辑，不直接注入 mapper。
- **`service/`** — 业务层，唯一允许同时注入 Repository 与 Mapper 的地方，写操作标 `@Transactional`。
- **`repository/`** — `JpaRepository` 派生查询，只放单表、参数 ≤3 的简单查询。
- **`mapper/`** — MyBatis `@Mapper` 接口，SQL 在 `src/main/resources/mapper/*.xml`，namespace 必须与接口全限定名一致。
- **`entity/`** — JPA `@Entity`，同时兼作 MyBatis `resultType`，两层共享同一 POJO。
- **`dto/*Query`** — 可变查询条件对象，字段默认 `null` 表示不过滤，另含 `page`(默认 1) / `pageSize`(默认 10)。
- **`common/`** — `ApiResponse`（统一响应）、`PageResult`（分页）、`JwtUtil` / `JwtAuthenticationFilter` / `LoginOut`（认证）。
- **`exception/`** — `GlobalExceptionHandler`（`@RestControllerAdvice`）+ `BusinessException` / `ResourceNotFoundException`。
- **`tool/`** — 静态工具类：`StringTool`、`CollectionTool`、`DateTimeTool`、`JsonTool`、`UserInfo`。

### 双持久化（核心架构决策）

同一应用同时使用 JPA 与 MyBatis，共享同一 DataSource 与事务：

- **单表 CRUD、参数 ≤3 的简单查询 → JPA Repository**。
- **动态 / 复合 / 多表 JOIN 查询 → MyBatis**。**XML 是查询真源**：改查询行为要改 `resources/mapper/*.xml`，不能只改接口。
- 同一方法内两者混用需加 `@Transactional`，让 JPA 事务覆盖 MyBatis 语句；注意 JPA 未 flush 的变更不保证对同事务内的 MyBatis 查询可见。
- `map-underscore-to-camel-case=true`：DB 列 `createtime` ↔ 字段 `createTime`。

### 认证与权限

Spring Security 全无状态（`SessionCreationPolicy.STATELESS`）：`JwtAuthenticationFilter` 解析 `Authorization: Bearer <token>`，把 JWT subject（用户名）写入 SecurityContext，放行 `OPTIONS` 预检与 `/auth/**`，其余请求一律需有效 token，未认证统一返回 401 JSON。

**过滤器只做认证、不做角色鉴权**——角色 / 权限由 `AuthController` 登录时查出塞进 `LoginOut.roles/rights` 交给前端，后端接口当前不按角色拦截；需要时在 `SecurityConfig.requestMatchers(...)` 上补 `.hasRole(...)`。业务代码取当前用户用 `UserInfo.currentUsername()`。

### REST 约定

- 响应一律 `ApiResponse<T>`（`code` / `message` / `data`），分页用 `PageResult<T>`（`content` / `total` / `page` / `size`）；成功 `ApiResponse.success(data)`，失败 `ApiResponse.error(400, "...")`。
- **分页**：客户端传 1-based `page`，Controller 转 0-based 后调 service，返回的 `page` 仍为 1-based。
- **复杂 / 可变条件查询用 `POST /xxx/search` + `XxxQuery` 请求体**；GET 查询参数只用于 ≤3 个固定条件。
- 写操作：`POST /xxx/save`、`PUT /xxx/update`、`DELETE /xxx/{id}`，写接口加 `@Valid`。
- 新增时 service 强制 `setId(null)` 忽略客户端 id；更新时按 id 查出**托管实体**后逐字段 `set`，以此保留 `createTime` / `createUser` 等创建信息（配合实体上的 `@DynamicUpdate` 只更新改动列）。
- 业务异常抛 `IllegalArgumentException`（→400）或 `ResourceNotFoundException`（→404），由 `GlobalExceptionHandler` 统一转 JSON，不要在 Controller 里 try/catch 自造响应。
- Service 中的查询方法普遍返回 `List` 或 `null`（而非 `Optional`），调用方需自行判空；`getByXxx(...).get(0)` 这类写法在本仓库常见但无空集合保护，改动时留意。

### 数据库约定

- 列名一律**小写无下划线**（`createuser`、`flowgraph`、`rolelist`），JPA 侧必须显式 `@Column(name = "...")` 指定，不能依赖默认命名策略。
- **`spring.jpa.hibernate.ddl-auto=none`**：Hibernate 既不建表也不改表。**所有 DDL/DML 由 DBA 执行**，AI 只以 **SQL 文本**形式交付 schema 变更，绝不自行连库执行。
- 新增实体后需先确认 DBA 已建表，否则应用启动即报错。
- SQL 通过 `logging.level.org.hibernate.SQL=DEBUG` 与 `logging.level.com.baogang.info.mapper=DEBUG` 打印。

## Key Configuration

`src/main/resources/application.properties`：端口 `8090`、上下文 `/api`；MySQL `jdbc:mysql://127.0.0.1:3306/page`（Hikari 池 10）；`ddl-auto=none`、`open-in-view=false`、dialect `MySQLDialect`；上传上限 100MB；MyBatis `mapper-locations=classpath*:mapper/*.xml`、`type-aliases-package` 指向 entity、`map-underscore-to-camel-case=true`、日志走 Slf4j（非 stdout）；JWT 的 `secret` / `expiration-ms` / `auth-path` / `issuer` 在 `jwt.*`。

**无环境化配置**：`src/main/resources` 下**只有 `application.properties` 一份**，`application-prod.properties` 等 profile 文件**不存在**，不依赖 `SPRING_PROFILES_ACTIVE`。DB 凭据与 JWT secret 均为明文硬编码其中。

**CORS 不走配置项**：`config/CorsConfig` 的 origin / methods / headers / maxAge 全是类内静态常量（`ALLOWED_ORIGINS = ["*"]`、`setAllowCredentials(false)`），**没有 `app.cors.*` 前缀，也不读环境变量**。改跨域策略必须改代码常量。
已知缺陷（未修）：`CorsConfig` 暴露的是 `CorsFilter`（无 `@Order` → LOWEST_PRECEDENCE），而 `HttpSecurity` 未调用 `.cors(...)` → Security 链（-100）先执行，**携带自定义头（`Authorization`）的跨域 OPTIONS 预检会被拦成 401**；普通跨域请求不受影响。

## 受保护文件

见 `.codebuddy/rules/FileProtection.mdc`（alwaysApply，最高优先级）：`mvnw` / `mvnw.cmd`、`info.iml`、`target/`、`.mvn/`、`.idea/` 只读，禁止写回。

## docs/ 剩余文档（可信度提示）

- **`tables.md`** — 全表结构速查（15 张表，列/类型/实体字段/含义），**由 `entity/` 的 `@Column` 提取，是当前 schema 真源**。开头「命名约定」列出了偏离小写无下划线约定的列名，改动前先看。
- `blue.md` — BluePrint 字段中文对照表，可用。
- `tb.sql` — 建库脚本，**已过时**，建表以 `tables.md` 为准。
- `plans/2026-08-22-code-review-report.md` — 历史评审报告（S1 登录无密码校验 / S2 免认证前缀匹配 / I1 放行路径写错 **未修复**；I2 Filter 双重注册已于 2026-08-31 修复）。
- `plans/2026-08-31-drop-flownodeaction.sql` — `flowNodeAction` 表下线 SQL（改名→备份→删除三步），**待 DBA 执行**。
- 已删除：`tb.md`、`workflow.md`（过时，内容并入 `tables.md`）、`tests.md`、以及描述已废弃 Info 模块的 architecture/configuration/persistence/tree/commands 五份文档。
