# page 库表结构文档（公共部分）

本目录为 `page` 库 18 张表的结构说明，每个表一个文件。**本文件保存所有表共用的开头元信息与尾部通用说明**，各表文件不再重复。

## 开头元信息（各表通用）

> 数据源：mysql127（127.0.0.1:3306）· 库 `page`
> 生成时间：2026-09-05
> 字段顺序：`order by ordinal_position`（物理列序）

## 生成 SQL（各表通用，替换 `table_name` 即可）

```sql
select COLUMN_NAME, DATA_TYPE,
       CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT
from information_schema.columns
where table_schema = 'page'
  and table_name = '<表名>'
order by ordinal_position;
```

## 尾部通用说明（各表通用）

- **列名一律小写无下划线**，与 JPA `@Column(name = "...")` 保持一致；**表名同样全小写**，MySQL 在 Linux（`lower_case_table_names=0`）下表名大小写敏感，SQL 里写驼峰会报 `Table doesn't exist`。
- **主键统一为 `id`，类型为 int**；全库唯一例外是 `workflow.id` 为 **bigint**。
- **字段类型几乎全为 varchar**：`id` 之外的列基本都是 varchar，长度直接写在类型列中（如 `varchar(100)`）。
- **时间/数值字段也多为 varchar**，范围查询与排序只能按字符串处理，需保证写入格式统一（如 `yyyy-MM-dd HH:mm:ss`）。涉及列举例：`flowhistory.dealTime`、`taskprocess.auditTime/createTime/updateTime`、`techstep.sort/isNeed`、`blueprint.weight/isFirstCheck/busbarNum`、`transferorder.num/weight`。
- **拼写与大小写说明**（2026-09-05 规则定稿：**以代码为准 = 列名全小写**，库中驼峰列由 DBA 改名对齐，DDL 见 `backend/docs/plans/2026-09-05-lowercase-columns.sql`，共 15 表 68 列，**已于 2026-09-05 执行完毕并验证通过**——除 `flownode.X/Y/W/H` 外库中已无大写列，文档/代码/库三者完全一致）：
  - `approval.starttime` —— 原 `sartTime`（疑 startTime 笔误），**2026-09-05 已由 DBA 纠正拼写并小写化**；
  - `flowgraph.height` —— 原 `heght`（疑 height 笔误），**2026-09-05 已由 DBA 纠正拼写**（实体本就映射 height，代码零改动）；
  - `flownode.X / Y / W / H` —— **保持大写**（全库唯一例外），JPA `@Column` 必须写成大写才能映射上，且为 varchar(45)，前端需自行转数值。

## 表清单

| 表 | 注释 | 字段数 | 主键 |
| ---- | ---- | ---- | ---- |
| [approval](./approval.md) | 审批工单 | 10 | int |
| [blueprint](./blueprint.md) | 蓝本记录 | 31 | int |
| [constvalue](./constvalue.md) | 常量值表 | 6 | int |
| [flowcurrent](./flowcurrent.md) | 流程当前节点 | 6 | int |
| [flowedge](./flowedge.md) | 流程连线表 | 11 | int |
| [flowgraph](./flowgraph.md) | 流程图参数表 | 6 | int |
| [flowhistory](./flowhistory.md) | 流程日志表 | 12 | int |
| [flownode](./flownode.md) | 流程节点表 | 14 | int |
| [sysright](./sysright.md) | 权限表 | 6 | int |
| [sysrole](./sysrole.md) | 角色表 | 5 | int |
| [sysroleright](./sysroleright.md) | 角色-权限关联表 | 4 | int |
| [sysroleuser](./sysroleuser.md) | 角色-用户关联表 | 4 | int |
| [sysuser](./sysuser.md) | 用户表 | 10 | int |
| [taskprocess](./taskprocess.md) | 产品流程 | 13 | int |
| [techstep](./techstep.md) | 工艺工序 | 8 | int |
| [transferorder](./transferorder.md) | 调拨单表 | 23 | int |
| [users](./users.md) | （无表注释） | 6 | int |
| [workflow](./workflow.md) | 流程表 | 11 | bigint |

> 各表文件里的 `说明` 小节只写**本表特有**的注意点，通用规则一律见本文件。
