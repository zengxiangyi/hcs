# 后端 REST 约定（按需加载：写/改 Controller、Service 接口时读取）

- 响应一律 `ApiResponse<T>`（`code`/`message`/`data`），分页 `PageResult<T>`（`content`/`total`/`page`/`size`）；成功 `ApiResponse.success(data)`，失败 `ApiResponse.error(400, "...")`。
- **分页**：客户端传 1-based `page`，Controller 转 0-based 调 service，返回的 `page` 仍 1-based。
- **复杂/可变条件查询用 `POST /xxx/search` + `XxxQuery` 请求体**；GET 查询参数只用于 ≤3 个固定条件。
- 写操作：`POST /xxx/save`、`PUT /xxx/update`、`DELETE /xxx/{id}`，写接口加 `@Valid`。
- 新增时 service 强制 `setId(null)` 忽略客户端 id；更新时按 id 查出**托管实体**后逐字段 `set`，保留 `createTime`/`createUser`（配合实体 `@DynamicUpdate` 只更新改动列）。
- 业务异常抛 `IllegalArgumentException`（→400）或 `ResourceNotFoundException`（→404），由 `GlobalExceptionHandler` 统一转 JSON，**不要在 Controller 里 try/catch 自造响应**。
- Service 查询方法普遍返回 `List` 或 `null`（非 `Optional`），调用方自行判空；`getByXxx(...).get(0)` 常见但无空集合保护，改动时留意。
