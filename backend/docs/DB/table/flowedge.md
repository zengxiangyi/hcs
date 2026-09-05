# flowedge

> `page.flowedge` · 流程连线表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | flowgraph | varchar(30) | 流程实例 |
| 3 | code | varchar(30) | 编号 |
| 4 | name | varchar(100) | 名称 |
| 5 | category | varchar(20) | 边线类型 |
| 6 | fromnode | varchar(30) | 起点 |
| 7 | tonode | varchar(30) | 终点 |
| 8 | cond | varchar(30) | 触发条件 |
| 9 | remark | varchar(200) | 触发条件描述 |
| 10 | color | varchar(10) | 颜色 |
| 11 | axis | varchar(100) | 坐标轴 |
