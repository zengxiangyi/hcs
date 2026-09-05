### table：SysUser

> 前端映射（`src/components/sys/user.vue` + `src/api/user.ts` 的 `sysUserAPI`）：`code`=编号、`name`=名称（原 nickname）、`department`=部门（原 dept）、`position`=岗位、`cellphone`=手机号、`email`=邮箱、`remark`=备注；`state` 取值「启用/禁用」。列表/新增/修改/删除均已对接后端（`/api/sysUser/list`、`POST /api/sysUser`、`PUT /api/sysUser/{id}`、`DELETE /api/sysUser/{id}`），服务端分页。

| column     | dataType     | comment |
| ---------- | ------------ | ------- |
| id         | int          | 主键    |
| code       | varchar(10)  | 工号    |
| name       | varchar(20)  | 姓名    |
| password   | varchar(100) | 密码    |
| remark     | varchar(100) | 备注    |
| email      | varchar(100) | 邮箱    |
| department | varchar(30)  | 部门    |
| position   | varchar(30)  | 岗位    |
| cellphone  | varchar(15)  | 手机号  |
| state      | varchar(10)  | 状态    |



### table：SysRole

> 前端映射（`src/components/sys/role.vue` + `mock.ts`）：`name`=名称、`code`=编码、`category`=分类、`remark`=备注。

| column   | dataType     | comment |
| -------- | ------------ | ------- |
| id       | int          | 主键    |
| code     | varchar(20)  | 编码    |
| name     | varchar(30)  | 名称    |
| category | varchar(20)  | 分类    |
| remark   | varchar(100) | 备注    |



### table：SysRight

> 前端映射（`src/components/sys/right.vue` + `mock.ts`）：`code`=编码、`name`=名称、`category`=分类（原 module）、`remark`=备注；页面不再区分 page/btn 的 type 字段，分类即可区隔模块。

| column   | dataType     | comment |
| -------- | ------------ | ------- |
| id       | int          | 主键    |
| code     | varchar(20)  | 编码    |
| name     | varchar(50)  | 名称    |
| category | varchar(20)  | 分类    |
| remark   | varchar(100) | 备注    |



### table：SysRoleUser

| column   | dataType     | comment  |
| -------- | ------------ | -------- |
| id       | int          | 主键     |
| roleCode | varchar(30)  | 角色编码 |
| userCode | varchar(30)  | 用户编码 |
| remark   | varchar(100) | 备注     |



### table：SysRoleRight

| column    | dataType     | comment  |
| --------- | ------------ | -------- |
| id        | int          | 主键     |
| roleCode  | varchar(30)  | 角色编码 |
| rightCode | varchar(30)  | 权限编码 |
| remark    | varchar(100) | 备注     |



### table: workflow

| column    | dataType     | comment  |
| --------- | ------------ | -------- |
| id        | int          | 主键     |
| code      | varchar(30)  | 编号     |
| name      | varchar(100) | 名称     |
| state     | varchar(30)  | 状态     |
| startTime | datetime     | 开始时间 |
| endTime   | datetime     | 结束时间 |
| remark    | varchar(100) | 备注     |
| category  | varchar(100) | 分类     |



### table：flowLog

| column   | dataType     | comment    |
| -------- | ------------ | ---------- |
| id       | int          | 主键       |
| workflow | varchar(100) | 流程标记   |
| opTime   | varchar(100) | 操作时间   |
| userCode | varchar(100) | 操作人工号 |
| userName | varchar(100) | 操作人名称 |
| flowNode | varchar(100) | 流程节点   |
| remark   | varchar(100) | 备注       |
| action   | varchar(100) | 动作       |
| note     | varchar(100) | 笔记       |



### table：flowNode

| column   | dataType     | comment |
| -------- | ------------ | ------- |
| id       | int          | 主键    |
| code     | varchar(30)  | 编号    |
| name     | varchar(50)  | 名称    |
| category | varchar(20)  | 分类    |
| shape    | varchar(20)  | 形状    |
| axis     | varchar(100) | 坐标轴  |
| color    | varchar(10)  | 颜色    |



### table：flowEdge

| column   | dataType     | comment |
| -------- | ------------ | ------- |
| id       | int          | 主键    |
| code     | varchar(30)  | 编号    |
| name     | varchar(100) | 名称    |
| color    | varchar(10)  | 颜色    |
| fromNode | varchar(30)  | 起点    |
| toNode   | varchar(30)  | 终点    |
| axis     | varchar(100) | 坐标轴  |



### table：flowMap

| column    | dataType     | comment  |
| --------- | ------------ | -------- |
| id        | int          | 主键     |
| workflow  | varchar(20)  | 流程标记 |
| width     | varchar(10)  | 宽度     |
| height     | varchar(10)  | 高度     |
| firstNode | varchar(20)  | 开始节点 |
| remark    | varchar(100) | 备注     |



### table：constValue

> 前端映射（`src/components/sys/constValue.vue` + `mock.ts`）：`code`=编码、`name`=名称、`category`=分类、`mark`=标记、`remark`=备注；路由 `web/sys/constValue`，页面权限 `page:constValue`。

| column   | dataType     | comment |
| -------- | ------------ | ------- |
| id       | int          | 主键    |
| code     | varchar(10)  | 编码    |
| name     | varchar(30)  | 名称    |
| category | varchar(20)  | 分类    |
| mark     | varchar(100) | 标记    |
| remark   | varchar(100) | 备注    |
