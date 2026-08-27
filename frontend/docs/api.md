# 前端接口文档（API Reference）

> 本文档描述 `src/api/` 下各接口模块的导出对象、端点、入参与返回类型。
> 项目约定：统一响应包装 `ApiResponse<T> = { code, data, msg }`（`src/api/http.ts`），响应拦截器已解包，接口方法返回 `Promise<ApiResponse<T>>`，业务数据通过 `res.data` 读取；`code !== 200` 时以 `err.message` 抛出后端 `msg`；HTTP 401 视为 token 失效。
> 所有请求自动携带 `Authorization: Bearer <token>`（读自 `localStorage`）。开发环境下 `/api` 经 Vite 代理转发至 `http://localhost:8080`。

---

## 一、请求/响应约定

| 项 | 说明 |
|----|------|
| 实例 | `src/api/http.ts` 默认导出的 `http`（`TypedHttp` 类型） |
| 方法签名 | `http.get<T>(url, config)` / `http.post<T>(url, data)` / `http.put<T>(url, data)` / `http.delete<T>(url, config)` |
| 列表分页入参 | 通过 `config.params` 传查询条件 + `page` / `pageSize` |
| 列表分页返回 | `{ list, total, page, pageSize }` |

---

## 二、`src/api/user.ts` — 用户管理

导出对象：`dataAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `dataAPI.list` | `GET /api/users/list` | `UserSearchParams`（name / role / dept / status / page / pageSize） | `UserListResult { list, total, page, pageSize }` | 用户列表（服务端分页 + 条件查询） |
| `dataAPI.add` | `POST /api/users` | `UserSaveParams`（username / name / role / dept / status / password?） | `UserRow` | 新增用户 |
| `dataAPI.update` | `PUT /api/users/:id` | `UserSaveParams` | `UserRow` | 修改用户 |
| `dataAPI.remove` | `DELETE /api/users/:id` | `id: number` | `null` | 删除用户 |
| `dataAPI.exportUrl` | — | — | `string`（导出接口地址） | 导出 Excel 的下载地址常量 |

### 类型定义
- `UserRow`：`id, username, name, role, dept, status`
- `UserSearchParams`：`name?, role?, dept?, status?, page?, pageSize?`
- `UserSaveParams`：`id?, username, name, role, dept, status, password?`
- `UserListResult`：`{ list: UserRow[], total, page, pageSize }`

---

## 三、`src/api/role.ts` — 角色管理

导出对象：`roleAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `roleAPI.list` | `GET /api/role/list` | `RoleListParams`（name / code / category / page / pageSize） | `RoleListResult { list, total, page, pageSize }` | 角色列表 |
| `roleAPI.add` | `POST /api/role` | `RoleSaveParams`（name / code / category / remark?） | `RoleRow` | 新增角色 |
| `roleAPI.update` | `PUT /api/role/:id` | `RoleSaveParams` | `RoleRow` | 修改角色 |
| `roleAPI.remove` | `DELETE /api/role/:id` | `id: number` | `null` | 删除角色 |

### 类型定义
- `RoleRow`：`id, name, code, category, remark`
- `RoleListParams`：`name?, code?, category?, page?, pageSize?`
- `RoleSaveParams`：`id?, name, code, category, remark?`
- `RoleListResult`：`{ list: RoleRow[], total, page, pageSize }`

---

## 四、`src/api/right.ts` — 权限管理

导出对象：`rightAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `rightAPI.list` | `GET /api/right/list` | `RightListParams`（code / name / category / page / pageSize） | `RightListResult { list, total, page, pageSize }` | 权限列表 |
| `rightAPI.add` | `POST /api/right` | `RightSaveParams`（code / name / category / remark?） | `RightRow` | 新增权限 |
| `rightAPI.update` | `PUT /api/right/:id` | `RightSaveParams` | `RightRow` | 修改权限 |
| `rightAPI.remove` | `DELETE /api/right/:id` | `id: number` | `null` | 删除权限 |

### 类型定义
- `RightRow`：`id, code, name, category（page|button）, remark`
- `RightListParams`：`code?, name?, category?, page?, pageSize?`
- `RightSaveParams`：`id?, code, name, category, remark?`
- `RightListResult`：`{ list: RightRow[], total, page, pageSize }`

---

## 五、`src/api/roleUser.ts` — 用户-角色关联

导出对象：`roleUserAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `roleUserAPI.listByRole` | `GET /api/role-user/list` | `roleId: number`（`params`） | `RoleUserListResult { roleId, userIds }` | 某角色下的用户 id 集合 |
| `roleUserAPI.listByUser` | `GET /api/role-user/user` | `userId: number`（`params`） | `UserRoleListResult { userId, roleIds }` | 某用户拥有的角色 id 集合 |
| `roleUserAPI.save` | `POST /api/role-user` | `RoleUserSaveParams { roleId, userIds }`（全量替换） | `null` | 设置某角色的用户集合 |
| `roleUserAPI.remove` | `DELETE /api/role-user` | `roleId, userId`（`params`） | `null` | 解除单条关联 |

---

## 六、`src/api/roleRight.ts` — 角色-权限关联

导出对象：`roleRightAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `roleRightAPI.list` | `GET /api/role-right/list` | `roleId: number`（`params`） | `RoleRightListResult { roleId, rightCodes }` | 某角色下的权限编码集合 |
| `roleRightAPI.save` | `POST /api/role-right` | `RoleRightSaveParams { roleId, rightCodes }`（全量替换） | `null` | 设置某角色的权限集合 |
| `roleRightAPI.remove` | `DELETE /api/role-right` | `roleId, rightCode`（`params`） | `null` | 解除单条关联 |

---

## 七、实体关系说明

```
User ───< UserRole >─── Role ───< RoleRight >─── Right
 (用户)      (用户角色)    (角色)      (角色权限)    (权限)
```

- `roleUser` 维护「用户 ↔ 角色」多对多关联（通过 `roleId + userId`）。
- `roleRight` 维护「角色 ↔ 权限」多对多关联（通过 `roleId + rightCode`）。
- 鉴权时由 `roleUser` 查出用户角色，再由 `roleRight` 查出角色拥有的权限编码集合，前端 `permission.ts` 的 `hasRight(code)` 据此判断按钮/菜单可见性。

---

## 八、`src/api/constValue.ts` — 常量值管理

导出对象：`constValueAPI`

| 方法 | 端点 | 入参 | 返回 `res.data` | 说明 |
|------|------|------|-----------------|------|
| `constValueAPI.list` | `GET /api/const-value/list` | `ConstValueListParams`（code / name / category / page / pageSize） | `ConstValueListResult { list, total, page, pageSize }` | 常量值列表 |
| `constValueAPI.add` | `POST /api/const-value` | `ConstValueSaveParams`（code / name / category / mark? / remark?） | `ConstValueRow` | 新增常量值 |
| `constValueAPI.update` | `PUT /api/const-value/:id` | `ConstValueSaveParams` | `ConstValueRow` | 修改常量值 |
| `constValueAPI.remove` | `DELETE /api/const-value/:id` | `id: number` | `null` | 删除常量值 |

---

## 九、迁移状态

- `src/components/sys/mock.ts` 已删除；所有 `sys/` 页面（`role.vue`、`right.vue`、`roleUser.vue`、`roleRright.vue`、`constValue.vue`）已切换为调用上述真实 API（服务端分页 + 关联保存）。
- `permission.ts` 不再依赖 mock：登录后由后端下发权限编码集合，经 `setCurrentUser(rightCodes)` 写入，菜单/按钮鉴权通过 `hasRight(code)` 判定。
- 接口端点（如 `/api/role/list`）为前端约定路径，若后端实际路径不同需调整。

| 文件 | 状态 |
|------|------|
| `src/api/user.ts` | 已存在（用户 CRUD） |
| `src/api/role.ts` | 已补充（2026-08-26） |
| `src/api/right.ts` | 已补充（2026-08-26） |
| `src/api/roleUser.ts` | 已补充（2026-08-26） |
| `src/api/roleRight.ts` | 已补充（2026-08-26） |
| `src/api/constValue.ts` | 已补充（2026-08-26） |
