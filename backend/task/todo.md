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
