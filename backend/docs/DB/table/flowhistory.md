# flowhistory

> `page.flowhistory` · 流程日志表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | workflow | varchar(30) | 实例ID |
| 3 | flowgraph | varchar(30) | 流程标记 |
| 4 | edge | varchar(100) | 边线 |
| 5 | fromnode | varchar(45) | |
| 6 | tonode | varchar(45) | |
| 7 | dealtime | varchar(30) | 操作时间 |
| 8 | dealuser | varchar(100) | 操作人工号 |
| 9 | username | varchar(100) | 操作人名称 |
| 10 | action | varchar(100) | 动作描述 |
| 11 | note | varchar(100) | 审批信息 |
| 12 | remark | varchar(100) | 备注 |

## 说明

- `fromNode`、`toNode` 未设置列注释，表格中留空。
