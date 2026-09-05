# blueprint

> `page.blueprint` · 蓝本记录

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | code | varchar(100) | 蓝本工艺编号 |
| 3 | name | varchar(100) | 蓝本名称 |
| 4 | graph | varchar(100) | 图号 |
| 5 | firstLevel | varchar(100) | 一级工艺 |
| 6 | secondLevel | varchar(30) | 二级工艺 |
| 7 | materialCode | varchar(100) | 物料编码 |
| 8 | materialName | varchar(100) | 物料名称 |
| 9 | weight | varchar(100) | 单重 |
| 10 | isFirstCheck | varchar(100) | 是否首检 |
| 11 | firstHardness | varchar(100) | 首检硬度要求 |
| 12 | lastHardness | varchar(100) | 完工硬度要求 |
| 13 | busbarNum | varchar(100) | 母线数量 |
| 14 | testNum | varchar(100) | 测点数量 |
| 15 | coolTime | varchar(100) | 冷却时间 |
| 16 | hardnessDepth | varchar(100) | 硬化层深度 |
| 17 | chamfer | varchar(100) | 辊身倒角 |
| 18 | fallHead | varchar(100) | 身颈落差 |
| 19 | quenching | varchar(100) | 淬火部位 |
| 20 | attention | varchar(100) | 注意事项 |
| 21 | model | varchar(100) | 材质 |
| 22 | specs | varchar(100) | 规格 |
| 23 | customer | varchar(100) | 客户名称 |
| 24 | edition | varchar(30) | 版本号 |
| 25 | state | varchar(100) | 状态 |
| 26 | remark | varchar(100) | 工艺备注 |
| 27 | createUser | varchar(45) | |
| 28 | createTime | varchar(45) | 创建时间 |
| 29 | updateUser | varchar(45) | 最近修改人 |
| 30 | updateTime | varchar(45) | 最近修改时间 |
| 31 | workflow | varchar(45) | 流程编号 |

## 说明

- `createUser` 未设置列注释，表格中留空。
