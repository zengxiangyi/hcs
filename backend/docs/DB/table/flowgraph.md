# flowgraph

> `page.flowgraph` · 流程图参数表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | flowgraph | varchar(30) | 流程标记 |
| 3 | title | varchar(100) | |
| 4 | width | varchar(10) | 宽度 |
| 5 | height | varchar(10) | 高度 |
| 6 | remark | varchar(100) | 备注 |

## 说明

- `height` 原 `heght`（疑 `height` 笔误），2026-09-05 已由 DBA 纠正拼写；实体 `FlowGraph` 本就映射 `@Column(name = "height")`，改名后代码零改动、前后端 JSON 字段名不变。
- `title` 未设置列注释，表格中留空。
