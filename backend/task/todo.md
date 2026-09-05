# TODO（待办事项）

## [延期] JWT token 吊销机制（评审编号 P1 #4，2026-09-05 记录）

- **状态**：延期执行，待与架构师讨论后再定方案。
- **背景**：当前 JWT 无 token 吊销机制，`jwt.expiration-ms=86400000`（24h）内 token 泄漏、用户登出、账号禁用后仍然有效。
- **涉及文件**：`common/JwtUtil.java`、`common/JwtAuthenticationFilter.java`、登录相关 Controller/Service。
- **候选方案**（待架构师讨论选定）：
  1. **token 黑名单**：Redis（或内存 Caffeine + TTL）记录失效 token 至其自然过期，过滤器每次校验时查黑名单。实现简单，适合当前无 Redis 的部署需评估引入成本。
  2. **短 access token + refresh token**：access token 缩短至 15~30 分钟，refresh token 服务端持久化可吊销。改动面大，需前端配合刷新逻辑。
  3. **最低成本方案**：用户表记录 `token_valid_from`（或密码修改时间 `password_changed_at`），登录/改密/禁用时更新，过滤器比对 token 的 `iat`，早于该时间即视为失效。
- **关联约定**：开发阶段不打包验证；`jwt.secret`/DB 密码外置（P1 #1 #2）同样延后至上线前处理。

## [待架构师评审] P0 安全与正确性五项（2026-09-05 全量评审产出，先记录不修改）

> 来源：2026-09-05 第二轮 18 Controller 多 Agent 全量评审（只读），以下 5 项定级 P0，**均需架构师评审定方案后再动手**。

### P0-1 匿名重置任意账号密码（账号接管）

- **位置**：`controller/AuthController.java`（/auth/resetPassword、/auth/verify）+ `SecurityConfig`（/auth/** permitAll）。
- **问题**：resetPassword 仅凭 username + 新密码即可改**任意账号**（含管理员）密码；verify 的身份核验结果没有任何凭证传递给 resetPassword，两段式流程形同虚设；verify 还可被当作「email+cellphone 是否匹配」的探测口。
- **候选方向**（待定稿）：verify 通过后签发一次性短时效重置令牌（或邮件/短信验证码），resetPassword 强制校验；verify 加验证码与频率限制。

### P0-2 密码全链路明文

- **位置**：`AuthController`（登录 `password.equals(...)` 明文比对）、`SysUserService`（注册/重置/更新明文入库）。
- **问题**：库泄露即全体账号沦陷；叠加 `show-sql=true` + bind TRACE，密码还会打进日志。
- **候选方向**：BCrypt（注册/重置 `encode()`、登录 `matches()`），存量数据一次性迁移（如首登强制改密）；同步收敛日志级别。

### P0-3 JWT 密钥可预测 + 凭据明文残留

- **位置**：`application.properties:43`（jwt.secret 为可解码明文 Base64 串）；`:12-19`（dev 库密码明文 + 注释中的生产库地址密码）。
- **问题**：知晓该串者可离线伪造任意用户合法 token；生产凭据已随仓库泄露。
- **候选方向**：密钥/密码改环境变量或配置中心（≥256bit 随机值，分环境），**轮换已泄露的生产密码与 JWT 密钥**。与上方已延期的「JWT token 吊销机制」一并评审。

### P0-4 全局无角色鉴权（三条旁路审批路径）

- **位置**：`SecurityConfig`（`anyRequest().authenticated()`，无任何 `@PreAuthorize`/requestMatchers 角色控制）。
- **问题**：认证≠授权，任何登录用户可——
  1. `PUT /taskprocess/update` 直改 auditState 把任意任务置「审核通过」，绕过 FlowEngine 审批链；
  2. `PUT /workflow/update` 直改 state/endTime 把流程实例置 end，绕过引擎的悲观锁+待办人校验（并留 flowcurrent 孤儿行）；
  3. `POST /sysRoleRight/save`、`/sysRoleUser/save` 给自己绑任意权限/角色（自我提权），可级联删角色/权限/流程图。
- **候选方向**：SecurityConfig 建角色矩阵（至少 DELETE、update、绑定类写端点收进 ADMIN/相应角色），数据基础（sysright/sysroleright/sysroleuser）已具备；taskprocess 审核字段与 workflow 状态字段是否从通用 update 中剥离由架构师定。

### P0-5 FlowNode/FlowEdge 更新静默假成功

- **位置**：`service/FlowNodeService.update`（:51-72 附近）、`service/FlowEdgeService.update`（:49-67 附近）。
- **问题**：按 `(flowGraph, code)` 定位旧记录，查不到时**直接 return 客户端提交的游离实体**——未持久化、无异常、HTTP 200，前端以为成功。修改节点/连线 code 必然触发；PUT 传入的 id 被 Service 完全无视。
- **候选方向**：统一改为**按 id 更新**（`findById` + `orElseThrow(ResourceNotFoundException)` → 404），与 FlowGraph/SysX 系列的 update 语义拉齐；`(flowGraph, code)` 定位降级为防重复校验。注意连带：节点改 code 后关联边 fromNode/toNode 的级联同步、code 唯一索引兜底（需 DBA DDL），一并评审。

### 评审时的关联背景（P1 类，供参考）

- 业务键普遍无唯一索引（workflow.code、flownode.code、sysuser.code、blueprint(code,edition) 等）+ update 改 code 无查重；
- 待办查询三口径分裂（rolelist LIKE 子串误匹配 R1 命中 R10 / operator='R' 漏 M/P 节点 / 不过滤 state='end'）；roleList 分隔符规范需先定稿；
- `GlobalExceptionHandler` 兜底 500 不打日志的问题**已修复**（2026-09-05：兜底补 log.error 含 uri/method，并新增 MethodArgumentTypeMismatch/HttpMessageNotReadable→400、NoResourceFound→404、DataIntegrityViolation→409 区分映射）；`@Size` 与库列宽失配**已修复**（2026-09-05：techstep/taskprocess/blueprint 三实体 @Size/@Column length 全部按库列宽对齐）。
- 数据库侧：列名小写化 DDL（`backend/docs/plans/2026-09-05-lowercase-columns.sql`）**已于 2026-09-05 由 DBA 执行完毕、验证通过（除 flownode.X/Y/W/H 外库中已无大写列）**，不再是待办。
