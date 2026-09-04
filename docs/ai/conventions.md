# 工作区约定（按需加载：涉及文件保护、日志/周报、文档可信度时读取）

## 受保护目录（可读不可写）

- 工作区级：`.idea/`、`script/`、`config/`
- 后端另有：`mvnw` / `mvnw.cmd`、`info.iml`、`target/`、`.mvn/`

## 三处 memory（写日志/周报时三处都要读）

1. `f:\hb\page\.codebuddy\memory\`（工作区根，近期）
2. `frontend\.codebuddy\memory\`（前端主日志，含 MEMORY.md）
3. `backend\.codebuddy\memory\`（后端日志，含 MEMORY.md）

## 文档可信度

- **`docs/structure.md` 是工作区结构真源**（结构以该文件为准）。
- `backend/docs/tables.md` = 数据库 schema 真源（15 张表，由 entity `@Column` 提取）。
- 已知过时文档：`backend/docs/tb.sql`（建库脚本，以 tables.md 为准）；`frontend/docs/struct.md`（结构部分过时，描述的 `src/api/base.ts`、`data/data2.vue` 已不存在）。
