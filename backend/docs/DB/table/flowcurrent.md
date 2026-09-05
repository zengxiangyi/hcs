# flowcurrent

> `page.flowcurrent` · 流程当前节点

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | workflow | varchar(30) | 流程标记 |
| 3 | flowGraph | varchar(30) | |
| 4 | flowNode | varchar(100) | 操作节点 |
| 5 | startTime | varchar(30) | 开始时间 |
| 6 | remark | varchar(100) | 备注 |

## 说明

- `flowGraph` 未设置列注释，表格中留空。
