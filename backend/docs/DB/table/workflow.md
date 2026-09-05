# workflow

> `page.workflow` · 流程表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | bigint | 主键 |
| 2 | code | varchar(30) | 编号 |
| 3 | name | varchar(100) | 名称 |
| 4 | category | varchar(45) | |
| 5 | targetCode | varchar(45) | 目标编号 |
| 6 | sender | varchar(30) | |
| 7 | startTime | varchar(30) | 开始时间 |
| 8 | state | varchar(30) | 状态 |
| 9 | flowGraph | varchar(30) | 流程图编号 |
| 10 | endTime | varchar(30) | 结束时间 |
| 11 | remark | varchar(100) | 备注 |

## 说明

- 主键 `id` 为 **bigint**，是全库唯一例外，实体类用 `Long` 接收。
- `category`、`sender` 未设置列注释，表格中留空。
