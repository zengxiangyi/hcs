# flownode

> `page.flownode` · 流程节点表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | flowgraph | varchar(30) | 流程图编号 |
| 3 | code | varchar(30) | 编号 |
| 4 | name | varchar(100) | 名称 |
| 5 | category | varchar(20) | 分类 |
| 6 | shape | varchar(20) | 形状 |
| 7 | color | varchar(10) | 颜色 |
| 8 | operator | varchar(10) | 操作人分类 |
| 9 | rolelist | varchar(100) | 角色组 |
| 10 | userlist | varchar(100) | 具体用户 |
| 11 | X | varchar(45) | |
| 12 | Y | varchar(45) | |
| 13 | W | varchar(45) | |
| 14 | H | varchar(45) | |

## 说明

- `X`、`Y`、`W`、`H` 为库中原始**大写**列名，`@Column` 必须按大写书写才能映射上。
- 坐标/尺寸为 varchar(45)，前端使用时需自行转数值。
- 这四个字段未设置列注释，表格中留空。
