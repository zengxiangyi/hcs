# 前后端跨端契约（按需加载：改接口、改请求/响应格式、排查前后端联调问题时读取）

1. **接口前缀**：前端 `src/api/*.ts` 写完整路径 `/api/xxx`；后端根包 `com.baogang.info`，context-path `/api`（由 war 名决定）。
2. **响应体**：`ApiResponse<T> = { code, data, msg }`；分页 `PageResult<T>`（1-based `page`）。**URL 风格定稿（2026-09-05 用户确认）：动词式 URL，不严格 RESTful——URL 要反馈操作方法信息**（复杂/可变条件查询 `POST /xxx/search` + `XxxQuery` 请求体；写操作 `POST /xxx/save`、`PUT /xxx/update`（id 在 body、null 抛 IAE→400）、`DELETE /xxx/{id}`；业务键定位可用 `GET/DELETE /xxx/code/{code}`）。新接口照此风格写，**不要**为对齐纯 REST 而改成 `POST /xxx` 裸资源或 `PUT /xxx/{id}`。
   **过滤值放路径段（2026-09-05 用户确认）：保留不改 query 参数**——`GET/DELETE /xxx/state/{state}`、`/category/{category}`、`/workflow/{workflow}`、`/flowGraph/{flowGraph}`、`/code/{code}` 这类「按某字段值定位/过滤」的端点维持路径段形态，新接口照此写。
   **错误语义现状（2026-09-05 定稿）**：资源不存在**只走全局异常处理器**——Controller 一律抛 `ResourceNotFoundException` → HTTP 404（body code=404），**Controller 层不手写 404 code**（已全量清理）。手写 `ApiResponse.error(400, ...)` 仅剩 Auth 与角色绑定类接口的业务校验（HTTP 200 + body code 400）。前端 `http.ts` 拦截器双通道兼容：`code !== 200` 与 HTTP 非 2xx 都会 reject 并透出 `message`，调用方统一 `err.message` 提示即可。
3. **认证**：JWT Bearer token（`Authorization: Bearer <token>`）。HTTP 401 = token 失效，前端清 `localStorage.token` 跳登录页。密码客户端 MD5 后发送。
4. **改后端接口地址要改两处**（易踩坑）：
   - dev → `frontend/vite.config.ts` 的 `server.proxy.target`（重启 dev 生效）；
   - 生产 → `frontend/.env.production` 的 `VITE_API_BASE_URL`（**必须重新 `npm run build:war`**；`server.proxy` 不参与打包，改它对发布后的页面无效）。
   - `VITE_API_BASE_URL` 只填 origin 根，**绝不能带 `/api`**（`src/api/*.ts` 路径已含 `/api`，带后会变成 `/api/api/...` 404）。同源部署填 `/` 最省心。
5. **数据库**：MySQL 库 `page`；列名/表名一律**小写无下划线**（Linux 下表名大小写敏感，SQL 写驼峰表名会报 `Table 'page.flowNode' doesn't exist`）；`ddl-auto=none`，**所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行**。schema 真源 = `backend/docs/DB/table/`。
6. **字段名陷阱**：后端 `techstep` 表 `step` = 工序编号、`stepName` = 工序名称；前端表格 row 的 `step` 存名称、`stepCode` 存编号（见 `frontend/src/components/tech/board.vue`）。
