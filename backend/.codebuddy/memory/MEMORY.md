# MEMORY.md（长期记忆，控制 4 KB 内；细节下沉到 daily 按需读取）

## 规范批改动（2026-09-05，需重打包部署 + 前端重新 build 才生效）
- **PUT 路由全量统一为 `PUT /xxx/update`（id 由请求体携带）**：后端改 sysUser/sysRole/sysRight（原 `/{id}`）、constValue（原 `/{id}` + `POST ""`→`POST /save`）、flowGraph（删与 `/update` 重复的 `PUT /{id}`）；前端 api 同步改 user.ts(2处)/role.ts/right.ts/process.ts/taskProcess.ts(原调 `/{id}` 本就不匹配，顺手修复)/constValue.ts，改法 `http.put('/api/xxx/update', {...data, id})`。按 code 删除的 `DELETE /code/{code}` 系保留未动。
- **try/catch 与手写判空拆除**：BluePrint/TransferOrder/TaskProcess/FlowGraph update 的 try-catch、`@RequestBody` 判 null 死代码、TechStep @PathVariable blank 检查全删。TechStep.batchSave 修 `&&` 判空 bug，批量下沉 `TechStepService.saveBatch`（@Transactional + saveAll）。
- **404 语义统一**：手写 error(400,"不存在") 全改抛 `ResourceNotFoundException`（BluePrint/TransferOrder/TaskProcess/TechStep getById 与 delete、FlowGraph.findByWorkflow 原 get(0) 越界）；Service 侧 BluePrintService.update、TechStepService.update、TransferOrderService.deleteById 同步。`getById` 返回 `success(null)` 型大量接口**未改**（涉及前端判空行为，待用户决定）。RNFE → HTTP 404 + {code:404,message}，前端拦截器解包提示，兼容。

## 用户偏好
- 用最简/最小实现（"懒"编码），反对过度工程化；文档用渐进式披露+按需加载的压缩风格。
- 大型代码对齐任务偏好**多 agent 并行**。

## 环境/工具
- 构建在 **Git Bash** 执行（Windows cmd 无 `tail`）；Maven 常用 `./mvnw -q compile` / `-o compile` 离线。
- MySQL MCP：`mysql127`（localhost:3306, root, page 库）可用；`mysql186`/`mysql197` 超时（需 VPN）。`execute_sql` **不支持多语句**（`USE x; SHOW TABLES;` 报 Commands out of sync），跨库查询走 `information_schema`。
- IDE lint 有陈旧缓存误报（重命名后实体"无法解析表/列"、`AuthController` 的 `SysRoleService.getByUserId`），以磁盘代码与编译结果为准。
- ponytail 插件 v4.9.0 已启用；模式 off/lite/full/ultra/review，默认 `full`（可用 `PONYTAIL_DEFAULT_MODE` 或 `%APPDATA%\ponytail\config.json` 覆盖）。

## 架构硬规则：双持久化分工
- **MyBatis**：动态查询、复杂查询、多表 JOIN。（改 SQL 编辑 `src/main/resources/mapper/*.xml`）
- **JPA**：单表操作 + 「参数 ≤ 3 个」的简单查询。参数 >3 或条件可变 → 走 MyBatis。
- 可共存于同一事务（`@Transactional`）。新增 mapper 需 `@Mapper` 接口 + XML 置于 `src/main/resources/mapper/`。
- 已收敛：BluePrint、TransferOrder。（Info 模块已整体删除，勿再引用）

## 项目现状（2026-08-31 重写 CODEBUDDY.md 后）
- 上下文路径 **`/api`**；`ddl-auto=none`（表结构全靠 DBA）；`src/test` 为空，无测试类。
- 四大业务模块：**BluePrint**（双持久化样板）、**Flow**（工作流 + FlowEngine）、**Sys**（权限）、**TaskProcess / TransferOrder / ConstValue**。
- JWT **只认证不鉴权**；REST 分页约定：客户端 **1-based**，service 层 **0-based**。
- `docs/tables.md` 是全表结构速查（15 表，由 entity `@Column` 提取），**schema 真源**；`docs/tb.sql` 已过时。

## 数据库约定
- **命名（真实库为准，改动前必查）**：表名全小写无下划线（flowgraph / flownode / flowedge / flowcurrent / flowhistory / workflow / blueprint / transferorder / taskprocess）；列名多数全小写无下划线（createtime / createuser / materialcode / audituser / firstlevel / materialname）。`docs/tb.sql` 已过时，不可作依据。
- **列名例外（已核验，勿"纠正"）**：`flowhistory.fromNode`/`toNode` 驼峰、`flownode.X/Y/W/H` 大写单字母（Java 字段名同步大写以对齐前端 JSON key）、`flowgraph.heght` 拼写错误。改名走 DBA 流程。
- **陷阱 1**：`@Table(name = "flowXxx")` 驼峰会被 Hibernate 转成 `flow_xxx` → Table doesn't exist。`@Table` 一律全小写。
- **陷阱 2**：全局 `map-underscore-to-camel-case` 对**无下划线列无效**，须在 XML 用 `resultMap` 显式映射列→驼峰字段（如 taskprocess）。
- **DDL/DML 一律由 DBA 执行**，AI 绝不自行改库；schema 变更只以 **SQL 文本**交付。`ddl-auto=none`，不会自动同步任何结构变更。

## 获取登录用户
- `SecurityContextHolder` 的 principal 是 **String 用户名**（JWT subject），**不是** `UserDetailsService` 的 `User`，强转会 ClassCastException。
- 统一用 `tool/UserInfo.currentUsername()`（String，无登录返回 null）。

## 技术栈硬约束（Spring Boot 4 / Java 25）
- `spring-boot-starter-webmvc` **不再传递** jakarta.validation → 必须显式引 `spring-boot-starter-validation`，否则 `@Valid`/`@NotBlank` 编译失败。
- **禁用 Lombok**（Java 25 下未生成访问器），全手写 getter/setter。
- dialect=`MySQL8Dialect`；MyBatis log-impl=`Slf4jImpl`。
- CORS：`config/CorsConfig` 的 origin/methods/headers/maxAge **全是类内静态常量**（`["*"]` + `allowCredentials=false`），**无 `app.cors.*` 前缀、不读环境变量**，改策略必须改代码常量。已知缺陷：`CorsConfig` 暴露 `CorsFilter` 而 `HttpSecurity` 未调 `.cors(...)` → 带 `Authorization` 头的跨域 OPTIONS 预检被拦 401。
- **无环境化配置**：`src/main/resources` 下**只有 `application.properties` 一份**，`application-prod.properties` **不存在**，不依赖 `SPRING_PROFILES_ACTIVE`；DB 凭据与 JWT secret 明文硬编码。（2026-08-31 更正：此前记录的 `app.cors.*` 前缀 + prod 示例均为文档漂移，全仓库 0 命中。）

## Filter 与 Security 链（勿踩）
- **任何注册进 Security 链（`addFilterBefore` 等）的 `Filter` 都不要标 `@Component`**：Boot 会自动把它注册到 Servlet 容器链（LOWEST_PRECEDENCE），形成双重注册；`OncePerRequestFilter` 的 alreadyFiltered 标记在内层 finally 被清除，外层会再跑一遍。
- 标准修法：在 `@Configuration` 里用 `@Bean` 定义该 Filter，**再补一个 `FilterRegistrationBean` 并 `setEnabled(false)`**（Boot 的 `ServletContextInitializerBeans` 会将其标记为已处理，不再追加默认注册）。仅由 `@Component` 改成 `@Bean` **无效**，两者都会被 Boot 自动注册。
- 配套：`@Configuration` 里若用构造器注入该 Filter，会产生循环依赖（本类 @Bean 依赖本类实例）→ 改成在 `@Bean` 方法参数上注入。
- **vue-router 路由 name 必须唯一**：重名会让先注册记录的 name 在守卫中变 `undefined`，静默破坏白名单/`resolve by name`（2026-09-02 修 `/` 与 `/login` 同叫 'Login' 导致根路径被弹 `/api/login?redirect=/`）。
- **别在 Security 链前置过滤器里 throw AuthenticationException**：它早于 `ExceptionTranslationFilter`，抛出的异常不会被翻译成 401，会冒泡成 500（前端 401 拦截器失效）。正确做法是注入 `AuthenticationEntryPoint` 并主动 `commence(...)`。2026-09-02 已修 `JwtAuthenticationFilter`。

## 部署形态（2026-09-02 定案：方案 B，两个 war 同 Tomcat）
- `ROOT.war`（前端 vite dist + `public/WEB-INF/web.xml` 的 404→/index.html，context-path `/`）与 `api.war`（context-path `/api`）同一个 Tomcat。
- 同源：context-path 不同不影响 origin（只看 scheme/host/port）→ localStorage 共享、axios 调 `/api/**` 无 CORS。
- 后端 `src/main/resources/static/` 已删；`SecurityConfig.PUBLIC_PATHS` 只留 `/auth/**`。
- 前端 `vite.config.ts` 的 `base` 恒为 `'/'`，`.env.production` 为 `VITE_API_BASE_URL=/api`；打包 `npm run build:war`。
- 坑：PowerShell `Compress-Archive` 只支持 `.zip`，打 war 必须先压 `ROOT.zip` 再 `Rename-Item` 成 `ROOT.war`。

## 待跟进
- **安全评审未修**：S1 登录无密码校验、S2 免认证前缀匹配、I1 context-path 下 requestMatchers 放行路径写错 → `docs/plans/2026-08-22-code-review-report.md`（用户当时选择只出报告）。**I2 Filter 双重注册已于 2026-08-31 修复**。
- **白名单单一真源（2026-09-02 起）**：`SecurityConfig.PUBLIC_PATHS`（`/auth/**`、`/`、`/index.html`、`/favicon.svg`、`/favicon.ico`、`/assets/**`）同时用于 `requestMatchers(...).permitAll()` 与 `JwtAuthenticationFilter` 构造参数；加放行路径只改这一处。旧的 `@Value("/auth/**")` 死配置项已删。
- 4 个 Controller 的 `searchByQuery` 分页校验逻辑重复、4 个 Query DTO 分页字段重复 → 建议抽公共方法 / `PageQuery` 基类（待用户确认）。
- **`flowNodeAction` 表已申请下线**（2026-08-31）：全仓库 0 引用，职责已被 flowedge(cond) + FlowEngine 取代。SQL 见 `docs/plans/2026-08-31-drop-flownodeaction.sql`，**待 DBA 执行**（改名→备份→删除三步，不可直接 DROP）。执行后需同步移除 `docs/tables.md` 的下线提示。
