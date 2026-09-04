# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## Overview

Spring Boot 4.0.8 / Java 25 后端（Maven，WAR 打包，`finalName=api` → `target/api.war`），根包 `com.baogang.info`，连接本地 MySQL 库 `page`，内嵌端口 8090、上下文 `/api`（外置 Tomcat 时均被忽略，context-path 由 war 名决定）。本文件只做概览与按需加载入口，细节拆到 `docs/ai/` 专题文件。

业务分四块：**蓝本（BluePrint）**、**工作流（FlowGraph/FlowNode/FlowEdge + Workflow/FlowCurrent/FlowHistory + `FlowEngine`）**、**系统权限（SysUser/SysRole/SysRight 及关联）**、**单据与常量（TaskProcess/TransferOrder/ConstValue）**。新增模块照抄 BluePrint（双持久化样板）。

## Commands

Maven Wrapper 构建（Windows 用 `./mvnw.cmd`）。**无独立 lint 步骤**。

| 目的 | 命令 |
| --- | --- |
| 构建打包（产出 `target/api.war`） | `./mvnw clean package` |
| 跳过测试打包 | `./mvnw clean package -DskipTests` |
| 本地运行（`:8090/api`） | `./mvnw spring-boot:run` |
| 全部测试 | `./mvnw test` |
| 单个测试类 / 方法 | `./mvnw test -Dtest=XxxTest` / `-Dtest=XxxTest#method` |

`src/test` 为空（跑 0 个用例）。`@SpringBootTest` 需本地 MySQL `127.0.0.1:3306/page` 运行，否则上下文启动失败。

## 按需加载地图（渐进式披露）

| 何时读 | 读哪个文件 |
|--------|-----------|
| 新增模块/接口、改分层职责、双持久化取舍 | `docs/ai/architecture.md` |
| 写/改 Controller、Service 接口、分页/异常处理 | `docs/ai/rest-conventions.md` |
| 改 SecurityConfig/JWT/CORS、排查 401/403 | `docs/ai/security.md` |
| 新增实体/表、写 SQL、排查 SQL 报错 | `docs/ai/db-conventions.md` |
| 改 application.properties、排查启动/连接/日志 | `docs/ai/config.md` |
| 一键发布脚本、部署形态 | `../docs/ai/deploy.md` |
| 跨端契约 | `../docs/ai/contract.md` |

## 最小必读事实

- **双持久化**：单表简单 CRUD → JPA；动态/复合/JOIN → MyBatis，**XML 是查询真源**（`resources/mapper/*.xml`）。混用需 `@Transactional`。
- **DB 列名/表名一律小写无下划线**，JPA 必须显式 `@Column(name=...)`；SQL 写驼峰表名在 Linux 会报表不存在。**`ddl-auto=none`，所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付**。schema 真源 = `docs/tables.md`。
- 响应一律 `ApiResponse<T>`，分页 `PageResult<T>`（1-based `page`）；复杂查询 `POST /xxx/search` + `XxxQuery`。
- 认证只做身份验证（JWT STATELESS），**不做角色鉴权**；角色/权限随 `LoginOut.roles/rights` 下发前端。
- 受保护只读：`mvnw`/`mvnw.cmd`、`info.iml`、`target/`、`.mvn/`、`.idea/`。
- 日常发布：工作区根 `.\deploy-test.ps1 -Part Back`（改日志级别等配置后必须重新打包重启，不热更新）。
