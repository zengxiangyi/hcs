# 后端架构（按需加载：新增模块/接口、改分层职责时读取）

## 分层与职责

`controller → service → repository(JPA) / mapper(MyBatis) → entity`

- **`controller/`**（18 个）— `@RestController`，仅参数校验 + `ApiResponse` 包装，不写业务逻辑，不直接注入 mapper。
- **`service/`**（17 个）— 业务层，唯一允许同时注入 Repository 与 Mapper 的地方，写操作标 `@Transactional`；流程引擎 `FlowEngine` 在此。
- **`repository/`**（16 个）— `JpaRepository` 派生查询，只放单表、参数 ≤3 的简单查询。
- **`mapper/`**（9 个）— MyBatis `@Mapper`，SQL 在 `src/main/resources/mapper/*.xml`，namespace 必须与接口全限定名一致。
- **`entity/`**（16 个）— JPA `@Entity` 兼作 MyBatis `resultType`，两层共享同一 POJO。
- **`dto/`** — `XxxQuery` 可变查询条件（字段 null 表示不过滤）+ `page`(默认1)/`pageSize`(默认10)。
- **`common/`** — ApiResponse、PageResult、JwtUtil、JwtAuthenticationFilter、LoginOut。
- **`config/`** — SecurityConfig、CorsConfig。**`exception/`** — GlobalExceptionHandler（@RestControllerAdvice）等。**`tool/`** — StringTool/CollectionTool/DateTimeTool/JsonTool/UserInfo 静态工具。

## 双持久化（核心架构决策）

同一应用同时用 JPA 与 MyBatis，共享同一 DataSource 与事务：

- 单表 CRUD、参数 ≤3 → **JPA Repository**；动态/复合/多表 JOIN → **MyBatis**，**XML 是查询真源**（改查询行为要改 `resources/mapper/*.xml`，不能只改接口）。
- 同一方法混用两者必须加 `@Transactional`，让 JPA 事务覆盖 MyBatis 语句；注意 JPA 未 flush 的变更不保证对同事务 MyBatis 查询可见。
- `map-underscore-to-camel-case=true`：DB 列 `createtime` ↔ 字段 `createTime`。

Mapper 列表（9 个，均有 XML）：BluePrint、ConstValue、SysRight、SysRole、SysUser、TaskProcess、TechStep、TransferOrder、Workflow。

## 业务模块（新增模块照抄 BluePrint —— 字段最多、查询最复杂，是双持久化样板）

| 模块 | 实体 | Controller |
|------|------|-----------|
| 蓝本 BluePrint | `BluePrint` | `BluePrintController` |
| 工作流 | FlowGraph/FlowNode/FlowEdge/Workflow/FlowCurrent/FlowHistory | FlowGraph/Node/Edge/Current/History/Workflow/EngineController，引擎 `FlowEngine` |
| 系统权限 | SysUser/SysRole/SysRight/SysRoleRight/SysRoleUser | `AuthController` + 各 Sys*Controller |
| 单据与常量 | TaskProcess/TransferOrder/ConstValue | 对应 Controller |
| 工艺工序 | 复用 BluePrint + TechStep | `TechStepController` |
