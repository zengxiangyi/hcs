# approval

> `page.approval` · 审批工单

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | code | varchar(20) | 任务编号 |
| 3 | name | varchar(50) | 任务名称 |
| 4 | category | varchar(20) | 分类B:蓝本,C:产品 |
| 5 | targetcode | varchar(45) | 目标编号 |
| 6 | sender | varchar(20) | 发起人 |
| 7 | sarttime | varchar(30) | 发起时间 |
| 8 | state | varchar(20) | 状态A:待处理,B:通过,C:驳回,D:终止,E:撤销 |
| 9 | remark | varchar(100) | 审批描述 |
| 10 | workflow | varchar(45) | 流程实例 |

## 说明

- `sartTime` 为库中原始拼写（疑 `startTime` 笔误），实体类映射按原样书写。
