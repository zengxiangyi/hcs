# sysright

> `page.sysright` · 权限表

## 字段清单

| 序号 | COLUMN_NAME | 类型 | COLUMN_COMMENT |
| ---- | ----------- | ---- | -------------- |
| 1 | id | int | 主键 |
| 2 | code | varchar(100) | 编码 |
| 3 | name | varchar(100) | 名称 |
| 4 | category | varchar(20) | 分类 |
| 5 | parent | varchar(100) | |
| 6 | remark | varchar(100) | 备注 |

## 说明

- `parent` 为父级权限编码（自关联字段，存 `code` 值），未设置列注释，表格中留空。
