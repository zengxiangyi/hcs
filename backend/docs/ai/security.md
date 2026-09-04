# 后端认证与安全（按需加载：改 SecurityConfig/JWT/CORS、排查 401/403 时读取）

## 认证链路

Spring Security 全无状态（`SessionCreationPolicy.STATELESS`）：

- `JwtAuthenticationFilter` 解析 `Authorization: Bearer <token>`，把 JWT subject（用户名）写入 SecurityContext；放行 `OPTIONS` 预检与 `/auth/**`；其余请求一律需有效 token，未认证统一 401 JSON。
- 401 响应带业务码（`JwtAuthenticationException`）：40101 = token 过期，40102 = 无效；未知异常回退 40102。前端约定：40101 → 跳登录，40102 → 提示无效。
- 白名单路径**不设配置项**，固定在 `SecurityConfig.PUBLIC_PATHS`（Java 常量，防运行时被改）。

## 职责边界

**过滤器只做认证、不做角色鉴权** —— 角色/权限由 `AuthController` 登录时查出塞进 `LoginOut.roles/rights` 下发前端。后端接口当前不按角色拦截；需要时在 `SecurityConfig.requestMatchers(...)` 补 `.hasRole(...)`。业务代码取当前用户用 `UserInfo.currentUsername()`。

## CORS

`config/CorsConfig`：origin 来自配置项 `cors.allowed-origins`（逗号分隔，默认 `*`，开发/测试取 *，生产改白名单）；`setAllowCredentials(false)`。

已知缺陷（未修）：`CorsFilter` 无 `@Order`（LOWEST_PRECEDENCE），`HttpSecurity` 未调 `.cors(...)` → Security 链（-100）先执行，**携带自定义头（Authorization）的跨域 OPTIONS 预检会被拦成 401**；普通跨域请求不受影响。

## 已知评审遗留（见 backend/docs/plans/ 与 task/todo.md）

- token 失效/登出无黑名单机制（延期，候选：黑名单 / refresh token / iat 比对）。
- jwt.secret 与 DB 密码仍明文硬编码于 application.properties（开发阶段暂缓外置）。
