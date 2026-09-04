# 后端数据库约定（按需加载：新增实体/表、写 SQL、排查 SQL 报错时读取）

- **列名一律小写无下划线**（`createuser`、`flowgraph`、`rolelist`），JPA 侧必须显式 `@Column(name = "...")`，不能依赖默认命名策略。
- **表名也是全小写**（与 JPA `@Table` 一致）。MySQL 列名大小写不敏感，但**表名在 Linux（lower_case_table_names=0）大小写敏感**——Mapper XML / 原生 SQL 里写驼峰表名（如 `flowNode`）会直接报 `Table 'page.flowNode' doesn't exist`。写 SQL 前先核对 `entity/*.java` 的 `@Table(name=...)`。
- **`spring.jpa.hibernate.ddl-auto=none`**：Hibernate 不建表不改表。**所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付 schema 变更，禁止自行连库执行**。新增实体后需先确认 DBA 已建表，否则应用启动即报错。
- MyBatis `map-underscore-to-camel-case=true`：列 `createtime` ↔ 字段 `createTime`。
- SQL 日志：`logging.level.org.hibernate.SQL=DEBUG` 与 `logging.level.com.baogang.info.mapper=DEBUG`。
- **schema 真源 = `backend/docs/tables.md`**（15 张表，由 entity `@Column` 提取，开头「命名约定」列出偏离项）；`backend/docs/tb.sql` 已过时；`backend/docs/blue.md` 是 BluePrint 字段中文对照可用。
