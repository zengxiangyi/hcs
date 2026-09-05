# flowgraph

> `page.flowgraph` · 流程图参数表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | flowGraph | varchar(30) | 流程标记 |
| 3 | title | varchar(100) | |
| 4 | width | varchar(10) | 宽度 |
| 5 | heght | varchar(10) | 高度 |
| 6 | remark | varchar(100) | 备注 |

## 说明

- `heght` 为库中原始拼写（疑 `height` 笔误），实体类映射按原样书写。
- `title` 未设置列注释，表格中留空。
