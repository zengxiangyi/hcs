---
name: module
description: 根据 create.md 自动生成后端子模块基础代码（Entity/Controller/Service/Mapper/Repository/Query/Mapper.xml 共 7 个文件）。当用户提供一份 create.md（含模块信息与字段清单）要求生成子模块代码、或提到"用 module skill 生成代码"、"按模块模板生成后端代码"时使用。
---

# module：由 create.md 生成后端子模块代码

## 输入

用户提供一份 create.md 文件路径（或直接粘贴内容），格式见 `backend/docs/create.md`：
- 模块信息：模块名 `${Name}`、表名、可选的 URL 前缀 / 业务唯一键 / 表注释
- 字段清单：Java属性 | @Column[name] | @Column[length] | Java类型（缺省 String）
- 可选的字段校验与特殊处理表（@NotBlank / WRITE_ONLY 等）

## 规范文档（生成前必读）

1. `backend/docs/Module.md` — 路径、包名、公共类、项目约定、验收清单
2. `backend/docs/Template.md` — 7 个文件的代码模板与占位符

## 工作流

1. **解析 create.md**：提取模块名、表名、字段清单、唯一键、校验要求。
   - 模块名必须是大驼峰；若用户给的是小写/下划线，先转成大驼峰并告知。
   - 若工作区中已有同名类，先向用户确认是否覆盖，禁止静默覆盖。
2. **按 Template.md 生成 7 个文件**（缺一不可）：

   | 文件 | 路径 |
   | ---- | ---- |
   | Entity | `backend/src/main/java/com/baogang/info/entity/${Name}.java` |
   | Controller | `backend/src/main/java/com/baogang/info/controller/${Name}Controller.java` |
   | Service | `backend/src/main/java/com/baogang/info/service/${Name}Service.java` |
   | Mapper | `backend/src/main/java/com/baogang/info/mapper/${Name}Mapper.java` |
   | Repository | `backend/src/main/java/com/baogang/info/repository/${Name}Repository.java` |
   | Query | `backend/src/main/java/com/baogang/info/dto/${Name}Query.java` |
   | Mapper XML | `backend/src/main/resources/mapper/${Name}Mapper.xml` |

3. **生成时强制规则**（详见 Module.md「必须遵守的项目约定」）：
   - Mapper XML 表名硬编码 `page.` 前缀；表名/列名全小写无下划线。
   - 分页：Query 含 `page/pageSize`（Integer，默认 1/10）；Controller 用 `PageParam.of()`；
     Service 收 0 基 offset，`PageResult.of(content, total, pageOffset + 1, size)` 返回 1 基。
   - 404 只抛 `ResourceNotFoundException`，禁止手写 `ApiResponse.error(404, ...)`。
   - Entity：`@Table(name=小写表名)` + `@DynamicUpdate` + 手写 getter/setter（不用 Lombok）。
   - save/update 有唯一键时 `existsByXxx` 查重，重复抛 `IllegalArgumentException`。
   - 复用公共类 `ApiResponse` / `PageParam` / `PageResult` / `ResourceNotFoundException`，不重复定义。
4. **自检**：按 `Module.md` 的「验收清单」逐项核对，并运行 `./mvnw compile` 确认编译通过。
5. **收尾报告**：列出生成的文件清单；说明建表 DDL 需以 SQL 文本交付 DBA（`ddl-auto=none`，
   禁止自行连库执行）；改动需 `.\deploy-test.ps1 -Part Back` 重新打包部署才生效。
