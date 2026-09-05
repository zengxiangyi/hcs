# ${name} =SysUser

> 用法：复制本文件，把标题中的 `${name}` 换成模块名，填好下面各表后交给 module skill 生成代码。
> 填写示例以 `SysUser` 为例。

## 模块信息

| 项 | 值 | 说明 |
| -- | -- | ---- |
| 模块名 `${name}` | SysUser | 大驼峰，如 `Blueprint`；决定类名与包名 |
| 表名 | sysuser | `page` 库实际表名，全小写无下划线 |
| URL 前缀（可选） | /sysUser | `@RequestMapping` 值，缺省为 `/${首字母小写的模块名}` |
| 业务唯一键（可选） | code | 有则生成 `GET /xxx/{key}/{value}`、`DELETE /xxx/{key}/{value}` 与 `existsByXxx` 查重 |
| 表注释（可选） | 系统用户表 | 写入 Entity 类首行 Javadoc |

## 字段清单

| Java属性 | @Column[name] | @Column[length] | Java类型（可选，缺省 String） |
| -------- | ------------- | --------------- | ----------------------------- |
| code     | code          | 10              |                               |
| name     | name          | 20              |                               |
| password | password      | 100             |                               |
| remark   | remark        | 100             |                               |
| email    | email         | 100             |                               |
| department | department  | 30              |                               |
| position | position      | 30              |                               |
| cellphone| cellphone     | 15              |                               |
| state    | state         | 10              |                               |

> 不需要填 `id`，模板自动生成 `Long id` 主键（`GenerationType.IDENTITY`）。
> 列名一律小写无下划线（唯一例外：`flownode` 的 `X/Y/W/H` 四列）。

## 字段校验与特殊处理（可选）

| Java属性 | 校验/处理 | 说明 |
| -------- | --------- | ---- |
| code     | `@NotBlank` | 创建时必填 |
| password | `WRITE_ONLY` | 响应体不序列化，防止明文回传 |
