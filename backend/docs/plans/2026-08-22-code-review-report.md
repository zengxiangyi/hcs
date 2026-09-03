# Info 项目 Java 代码评审报告

- 日期：2026-08-22
- 范围：`com.baogang.info` 全部 16 个 Java 文件 + `InfoMapper.xml`
- 方式：6 个并行评审 agent（A 入口配置 / B 安全鉴权 / C Web层 / D 业务持久化 / E 质量总览 / F 安全合规）
- 策略：先出报告，未改代码

## 严重（必须处理）
- S1 `AuthController`：登录仅校验 username 非空即签发 JWT，无密码，任意身份伪造。演示代码，生产前必改。
- S2 `JwtAuthenticationFilter:51-55`：`path.startsWith(prefix)` 前缀匹配免认证路径，可误放行 `/info/authXxx`，权限绕过。改用 `AntPathRequestMatcher`。

## 重要（建议修）
- I1 `SecurityConfig:51-54`：放行写成 `/info/auth/**`，context-path 已是 `/info`，requestMatchers 匹配 servletPath 应为 `/auth/**`，否则登录接口被 JWT 拦截（强疑似 bug，需验证 WAR context-path）。
- I2 `JwtAuthenticationFilter:28`：`@Component` 致过滤器被容器与 Security 链双重注册，可能双重执行+异常不被 EntryPoint 处理。去掉 @Component，改构造器注入。
- I3 `InfoController:26-58`：分页 page 起点无统一约定与边界校验。统一 0 基 + `@Min/@Max`。
- I4 `InfoService:43-47`：`save` 无条件 `setId(null)` 破坏 JPA 更新语义。改为仅新增时清空。
- I5 `InfoMapper.xml`+配置：依赖 `mapUnderscoreToCamelCase` 隐式映射 `create_time`→`createTime`，需验证开关已开，否则显式 resultMap。
- I6 `JwtUtil:34-36`：密钥每次重派生、无最小长度校验。缓存 SecretKey，启动校验 ≥32 字节。
- I7 `CorsConfig:27`：允许来源 `*`，生产应白名单化。
- I8 `JwtAuthenticationFilter:78-79`：`authorities=NO_AUTHORITIES`，token 无角色，无法细粒度授权。
- I9 `GlobalExceptionHandler`：异常覆盖不全（类型不匹配/JSON 解析失败→500 误用），应补分支。
- I10 `InfoRepository:4-12`：重复 import 同一组，删除即可。

## 提示（可选）
- createAndQuery 双持久化可见性注释保守但风险可控，建议收敛为示例/测试代码。
- JwtUtil 异常 catch(Exception) 吞掉、缺 jti/nbf。
- AuthController 用 Map 无 DTO，建议补 LoginRequest。
- POST /save 未区分 201/200、未分离更新。
- entity/Info 用 java.util.Date，建议迁移 java.time。
- approval/、task/ 空目录无 package-info.java。
- ApiResponse/PageResult 无直接泄露，风险在 Entity 直出。
- XSS：content 原样返回，前端 innerHTML 渲染则有存储型 XSS 风险。

## 修改优先级（后续阶段）
- P0：S1 免密登录、S2 路径绕过、I1 context-path 放行、I2 双重注册
- P1：I3 分页校验、I4 save 更新语义、I5 驼峰映射验证、I6 密钥校验、I9 异常分支
- P2：I7 CORS 收紧、I8 角色 claims、I10 重复 import、其余提示项

## 需先读配置定论的点
- I1：WAR 部署下 context-path 由外部容器决定，需确认 Security 放行路径实际匹配行为。
- I5：需查 application.properties 确认 `mybatis.configuration.map-underscore-to-camel-case=true` 是否已开。
