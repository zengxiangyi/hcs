# 子模块编码规范

本文档 + `Template.md`（各类文件模板）+ `create.md`（模块描述填写模板）三者共同构成
"由一份 create.md 自动生成一个子模块基础代码" 的完整上下文。

## 默认路径

| Path                                      | 说明                    |
| ----------------------------------------- | ----------------------- |
| backend                                   | 根目录                  |
| src/main/java                             | Java代码更根目录        |
| src/main/resources                        | 资源根目录              |
| src/main/resources/mapper                 | mybatis Mapper 配置目录 |
| src/main/resources/templates              | 静态资源目录            |
| src/main/resources/application.properties | 配置文件                |

## Java代码包名

```java
com.baogang.info
```

## `${name}` 表示模块的名称

`${Name}` = 大驼峰（如 `SysUser`），`${name}` = 首字母小写（如 `sysUser`）。

新建的Java文件和XML文件（共 7 个）：

| 文件                                          | 说明                  |
| --------------------------------------------- | --------------------- |
| com.baogang.info.entity.${Name}               | Entity类              |
| com.baogang.info.controller.${Name}Controller | Controller类          |
| com.baogang.info.service.${Name}Service       | Service类             |
| com.baogang.info.mapper.${Name}Mapper         | Mybatis Mapper 类     |
| com.baogang.info.repository.${Name}Repository | JpaRepository 类      |
| com.baogang.info.dto.${Name}Query             | 查询参数类            |
| src/main/resources/mapper/${Name}Mapper.xml   | mybatis SQLMapper文件 |

> 注意：Service 类是 `${Name}Service`（带 Service 后缀），其余类直接用 `${Name}` 或 `${Name}+角色后缀`。

## 生成流程

1. 用户提供一份填好的 `create.md`（模块信息 + 字段清单）。
2. 按 `Template.md` 的模板逐个生成上表 7 个文件。
3. 生成完毕按「验收清单」自检。

## 直接复用的公共类（禁止在新模块里重复定义）

| 类 | 包 | 用途 |
| -- | -- | ---- |
| `ApiResponse<T>` | `com.baogang.info.common` | 统一响应包装，`ApiResponse.success(data)` |
| `PageParam` | `com.baogang.info.common` | 分页归一：`PageParam.of(page, pageSize)`，`p.page0()` 给 0 基页码 |
| `PageResult<T>` | `com.baogang.info.common` | 分页结果：`PageResult.of(content, total, page, size)`，page 传 1 基 |
| `ResourceNotFoundException` | `com.baogang.info.exception` | 资源不存在时抛出，全局异常处理器转 HTTP 400 |

## 必须遵守的项目约定

1. **URL 动词式风格**：查询 `POST /xxx/search`；详情 `GET /xxx/{id}`；创建 `POST /xxx/save`；
   更新 `PUT /xxx/update`（id 在 body，为 null 抛 `IllegalArgumentException` → 400）；
   删除 `DELETE /xxx/{id}`（有业务唯一键时为 `DELETE /xxx/{key}/{value}`）。
2. **分页契约 1 基**：Query DTO 的 `page/pageSize` 默认 1/10；Controller 用 `PageParam.of()` 归一并取
   `p.page0()`（0 基页码）传给 Service；JPA 侧直接 `PageRequest.of(page, size)`，MyBatis 侧由 Service
   用 `(long) pageOffset * size` 得 LIMIT 偏移；`PageResult.of(..., pageOffset + 1, size)` 还原 1 基响应。
3. **Mapper XML 表名硬编码 `page.` 前缀**（如 `FROM page.sysuser`），表名/列名一律全小写无下划线。
   唯一例外：`flownode` 表的 `X/Y/W/H` 四列必须大写。
4. **404 语义统一**：资源不存在抛 `ResourceNotFoundException`（全局处理器处理），
   Controller/Service 内**禁止手写** `ApiResponse.error(404, ...)`。
5. **Entity 规范**：`@Table(name = "小写表名")` + `@DynamicUpdate`；主键 `Long id` +
   `@GeneratedValue(strategy = GenerationType.IDENTITY)`；每个字段 `@Size(max = 库中长度)` +
   `@Column(name, length)`；手写 getter/setter（不用 Lombok）。
6. **save 前查重**：有业务唯一键（如 code）时，save/update 前用 `existsByXxx` 查重，
   重复抛 `IllegalArgumentException`。
7. **建表 DDL 不由 AI 执行**：`ddl-auto=none`，DDL/DML 只以 SQL 文本交付给 DBA。
8. 改动需 `.\deploy-test.ps1 -Part Back` 重新打包部署后才生效。

## 参考示例子模块 `SysUser`

完整的 7 个文件可直接对照阅读：

| 文件 |
| ---- |
| `src/main/java/com/baogang/info/entity/SysUser.java` |
| `src/main/java/com/baogang/info/controller/SysUserController.java` |
| `src/main/java/com/baogang/info/service/SysUserService.java` |
| `src/main/java/com/baogang/info/mapper/SysUserMapper.java` |
| `src/main/java/com/baogang/info/repository/SysUserRepository.java` |
| `src/main/java/com/baogang/info/dto/SysUserQuery.java` |
| `src/main/resources/mapper/SysUserMapper.xml` |

各文件的模板与生成规则见 `Template.md`；create.md 的填写格式见 `create.md`。

## 验收清单（生成后逐项自检）

- [ ] 7 个文件全部生成，包名/路径正确（Service 是 `${Name}Service`）
- [ ] Entity：`@Table` 小写表名、`@DynamicUpdate`、id 主键、`@Size`+`@Column` 与 create.md 字段一致
- [ ] Mapper XML：`page.` 前缀、列名全小写、`Base_Column_List` 与实体字段一致
- [ ] Query DTO：含 `page/pageSize`（Integer，默认 1/10）
- [ ] Controller：`POST /search` 用 `PageParam.of()`；`PUT /update` 校验 id 非空；无手写 404
- [ ] Service：search 返回 `PageResult.of(content, total, pageOffset + 1, size)`
- [ ] Repository：`extends JpaRepository<${Name}, Long>`；有唯一键时含 `findByXxx/existsByXxx`
- [ ] 编译通过：`./mvnw compile`
