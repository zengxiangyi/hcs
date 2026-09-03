# 数据表结构

> 最后更新：2026-08-31
>
> **本文件由 `entity/` 下 JPA `@Table` / `@Column` 注解直接提取，是当前 schema 的唯一文档依据。**
> 取代已删除的 `tb.md`、`workflow.md`。注意：本文件描述的是**代码期望的列名**，实际库中列名若有出入，属 DDL 未同步，需由 DBA 修正（AI 不执行 DDL）。
> 所有表主键均为 `id`，`bigint` 自增（`GenerationType.IDENTITY`）；其余列一律 `varchar`，长度见表。
> `blueprint` 字段的中文含义另见 `blue.md`。
>
> **已下线（待 DBA 执行）**：`flowNodeAction` 表——代码中已无对应实体，全仓库 0 处引用，确认为孤儿表。下线 SQL 见 `docs/plans/2026-08-31-drop-flownodeaction.sql`，执行后从本文件移除该记录（本文件不再收录该表）。

## 命名约定

列名一律**小写无下划线**（`createtime`、`flowgraph`、`rolelist`）。已知的例外（历史遗留，改动需 DBA 执行）：

| 表 | 列名 | 说明 |
| --- | --- | --- |
| `flowhistory` | `fromNode`、`toNode` | 驼峰，未改小写 |
| `flownode` | `X`、`Y`、`W`、`H` | 大写单字母 |
| `flowgraph` | `heght` | 拼写错误（`height` 之误），因已建表保留 |

---

## 蓝本

### blueprint

`entity/BluePrint.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(100) | 编号 |
| name | name | varchar(50) | 名称 |
| graph | graph | varchar(100) | 图形 |
| firstlevel | firstLevel | varchar(100) | 一级工艺 |
| secondlevel | secondLevel | varchar(30) | 二级工艺 |
| materialname | materialName | varchar(100) | 物料名称 |
| weight | weight | varchar(45) | 单重 |
| materialcode | materialCode | varchar(100) | 物料编码 |
| isfirstcheck | isFirstCheck | varchar(100) | 是否首检 |
| busbarnum | busbarNum | varchar(100) | 母线数量 |
| testnum | testNum | varchar(100) | 测点数量 |
| cooltime | coolTime | varchar(100) | 冷却时间 |
| hardnessdepth | hardnessDepth | varchar(100) | 硬化层深度 |
| chamfer | chamfer | varchar(100) | 辊身倒角 |
| fallhead | fallHead | varchar(100) | 身颈落差 |
| quenching | quenching | varchar(100) | 淬火部位 |
| attention | attention | varchar(100) | 注意事项 |
| model | model | varchar(100) | 材质 |
| firsthardness | firstHardness | varchar(100) | 首检硬度要求 |
| lasthardness | lastHardness | varchar(100) | 完工硬度要求 |
| specs | specs | varchar(100) | 规格 |
| customer | customer | varchar(100) | 客户 |
| edition | edition | varchar(30) | 版本 |
| state | state | varchar(100) | 状态 |
| remark | remark | varchar(100) | 备注 |
| createtime | createTime | varchar(100) | 创建时间 |
| createuser | createUser | varchar(100) | 创建人 |
| updatetime | updateTime | varchar(100) | 最近修改时间 |
| updateuser | updateUser | varchar(100) | 最近修改人 |
| workflow | workflow | varchar(100) | 流程实例编码 |

> ⚠️ `BluePrintQuery` 含 `category` 字段，但实体与 `BluePrintMapper.xml` 均无对应列，该查询条件当前**不生效**。

---

## 工作流

图定义三张表（`flowgraph` / `flownode` / `flowedge`）与实例三张表（`workflow` / `flowcurrent` / `flowhistory`）通过 `flowgraph` 列关联；实例表再以 `workflow` 列（流程实例编码）串联。流转逻辑见 `service/FlowEngine.java`。

### workflow（流程实例）

`entity/Workflow.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(30) | 流程实例编码 |
| name | name | varchar(100) | 流程实例名称 |
| state | state | varchar(30) | 流程状态 |
| sender | sender | varchar(30) | 发起人 |
| starttime | startTime | varchar | 开始时间 |
| endtime | endTime | varchar | 结束时间 |
| remark | remark | varchar(100) | 备注 |
| flowgraph | flowGraph | varchar(30) | 流程图编号 |
| targetcode | targetCode | varchar(45) | 目标编码 |
| category | category | varchar(45) | 流程分类 |

### flowgraph（流程图定义）

`entity/FlowGraph.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| flowgraph | flowGraph | varchar(30) | 流程图编号 |
| title | title | varchar(100) | 标题 |
| width | width | varchar(10) | 宽度 |
| heght | heght | varchar(10) | 高度（拼写错误，见上文） |
| remark | remark | varchar(100) | 备注 |

### flownode（节点定义）

`entity/FlowNode.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| flowgraph | flowGraph | varchar(30) | 流程图编号 |
| code | code | varchar(30) | 节点编号 |
| name | name | varchar(50) | 节点名称 |
| category | category | varchar(20) | 分类：S 开始 / M 中间 / E 结束 |
| shape | shape | varchar(20) | 形状 |
| axis | axis | varchar(100) | 坐标轴 JSON |
| color | color | varchar(10) | 颜色 |
| operator | operator | varchar(10) | 操作人分类：R 角色 / U 用户 / M 角色和用户 / P 角色或用户 |
| rolelist | roleList | varchar(100) | 角色列表 |
| userlist | userList | varchar(100) | 用户列表 |
| X | X | varchar(45) | 横坐标 |
| Y | Y | varchar(45) | 纵坐标 |
| W | W | varchar(45) | 宽 |
| H | H | varchar(45) | 高 |

### flowedge（连线定义）

`entity/FlowEdge.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(30) | 连线编号 |
| name | name | varchar(100) | 连线名称 |
| color | color | varchar(10) | 颜色 |
| fromnode | fromNode | varchar(30) | 起点节点 |
| tonode | toNode | varchar(30) | 终点节点 |
| axis | axis | varchar(100) | 坐标轴 JSON |
| flowgraph | flowGraph | varchar(30) | 流程图编号 |
| category | category | varchar(20) | 条件分类：P 排他 / M 并行 |
| cond | cond | varchar(30) | 条件 |
| remark | remark | varchar(200) | 备注 |

### flowcurrent（当前节点）

`entity/FlowCurrent.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| workflow | workflow | varchar(30) | 流程实例编码 |
| flowgraph | flowGraph | varchar(100) | 流程图编号 |
| flownode | flowNode | varchar(100) | 当前节点编号 |
| starttime | startTime | varchar(30) | 开始时间 |
| remark | remark | varchar(100) | 备注 |

### flowhistory（流转历史）

`entity/FlowHistory.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| workflow | workflow | varchar(100) | 流程实例编码 |
| flowgraph | flowGraph | varchar(100) | 流程图编号 |
| edge | edge | varchar(100) | 经过的连线编号 |
| fromNode | fromNode | varchar(100) | 起点节点（⚠️ 命名例外） |
| toNode | toNode | varchar(100) | 终点节点（⚠️ 命名例外） |
| dealtime | dealTime | varchar(30) | 处理时间 |
| dealuser | dealUser | varchar(100) | 处理人工号 |
| username | userName | varchar(100) | 处理人名称 |
| remark | remark | varchar(100) | 备注 |
| action | action | varchar(100) | 动作 |
| note | note | varchar(100) | 审批笔记 |

---

## 系统权限

### sysuser

`entity/SysUser.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(10) | 编号（登录用户名） |
| name | name | varchar(20) | 名称 |
| password | password | varchar(100) | 密码（明文存储） |
| remark | remark | varchar(100) | 备注 |
| email | email | varchar(100) | 邮箱 |
| department | department | varchar(30) | 部门 |
| position | position | varchar(30) | 岗位 |
| cellphone | cellphone | varchar(15) | 手机号 |
| state | state | varchar(10) | 状态 |

> ⚠️ 密码为明文比较（`AuthController.login`），需改造为哈希。

### sysrole

`entity/SysRole.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(20) | 编码 |
| name | name | varchar(30) | 名称 |
| category | category | varchar(20) | 分类 |
| remark | remark | varchar(100) | 备注 |

### sysright

`entity/SysRight.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(100) | 编码 |
| name | name | varchar(100) | 名称 |
| category | category | varchar(20) | 分类 |
| parent | parent | varchar(100) | 父权限编码 |
| remark | remark | varchar(100) | 备注 |

### sysroleuser（角色-用户）

`entity/SysRoleUser.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| rolecode | roleCode | varchar(30) | 角色编码 |
| usercode | userCode | varchar(30) | 用户编码 |
| remark | remark | varchar(100) | 备注 |

### sysroleright（角色-权限）

`entity/SysRoleRight.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| rolecode | roleCode | varchar(30) | 角色编码 |
| rightcode | rightCode | varchar(30) | 权限编码 |
| remark | remark | varchar(100) | 备注 |

---

## 单据与常量

### constvalue

`entity/ConstValue.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(10) | 编码 |
| name | name | varchar(30) | 名称 |
| category | category | varchar(20) | 分类 |
| mark | mark | varchar(100) | 标记 |
| remark | remark | varchar(100) | 备注 |

### taskprocess

`entity/TaskProcess.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| transfer | transfer | varchar(100) | 调拨单号 |
| blueprint | blueprint | varchar(100) | 蓝本编号 |
| audituser | auditUser | varchar(100) | 审批人 |
| audittime | auditTime | varchar | 审批时间 |
| auditmessage | auditMessage | varchar(100) | 审批意见 |
| auditstate | auditState | varchar(50) | 审批状态 |
| step | step | varchar(50) | 步骤 |
| state | state | varchar(50) | 状态 |
| createuser | createUser | varchar(100) | 创建人 |
| createtime | createTime | varchar | 创建时间 |
| updateuser | updateUser | varchar(100) | 修改人 |
| updatetime | updateTime | varchar | 修改时间 |

### transferorder

`entity/TransferOrder.java`

| column | field | type | comment |
| --- | --- | --- | --- |
| id | id | bigint | 主键 |
| code | code | varchar(100) | 调拨单号 |
| name | name | varchar(100) | 名称 |
| category | category | varchar(100) | 分类 |
| transferdate | transferDate | varchar(100) | 调拨日期 |
| materialcode | materialCode | varchar(100) | 物料编码 |
| num | num | varchar(100) | 数量 |
| weight | weight | varchar(100) | 重量 |
| material | material | varchar(100) | 材质 |
| rollnum | rollNum | varchar(100) | 辊号 |
| outprocess | outProcess | varchar(100) | 转出工序 |
| inprocess | inProcess | varchar(100) | 转入工序 |
| outroom | outRoom | varchar(100) | 转出库房 |
| inroom | inRoom | varchar(100) | 转入库房 |
| remark | remark | varchar(100) | 备注 |
| prompt | prompt | varchar(100) | 提示 |
| quenching | quenching | varchar(100) | 淬火 |
| supplier | supplier | varchar(100) | 供应商 |
| createuser | createUser | varchar(100) | 创建人 |
| createtime | createTime | varchar(100) | 创建时间 |
| receiveuser | receiveUser | varchar(100) | 接收人 |
| receivetime | receiveTime | varchar(100) | 接收时间 |
| state | state | varchar(100) | 状态 |
