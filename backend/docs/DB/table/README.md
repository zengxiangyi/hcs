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
- **拼写与大小写例外**（以库中实际拼写为准，勿擅自纠正，改实体类映射而非改库）：
  - `approval.sartTime` —— 疑为 `startTime` 笔误；
  - `flowgraph.heght` —— 疑为 `height` 笔误；
  - `flownode.X / Y / W / H` —— **大写列名**，JPA `@Column` 必须写成大写才能映射上，且为 varchar(45)，前端需自行转数值。

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
