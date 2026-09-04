# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## 工作区形态（重要）

本工作区因人手不足把前端和后端放在**同一目录**下管理，但两者是完全独立的项目：

- `frontend/` = 前端页面根目录（Vue 3 + Vite 8 + TypeScript + Element Plus）
- `backend/` = 后端接口根目录（Spring Boot 4.0.8 / Java 25，Maven WAR，根包 `com.baogang.info`）

**后期 frontend 与 backend 可能各自分离为独立仓库/独立根目录。** 因此：

- 各自的详细指引在 `frontend/CODEBUDDY.md` 与 `backend/CODEBUDDY.md`（做前后端改动前先读对应那份）。
- 本文件只记录跨端约定与工作区级事实，细节全部拆到 `docs/ai/` 下按需加载，便于将来直接拆分。
- 前后端之间唯一的耦合点是 **REST 契约**（`docs/ai/contract.md`）。

## 常用命令

```powershell
.\deploy-test.ps1                # 一键构建+部署+日志（全量）
.\deploy-test.ps1 -Part Front    # 只构建部署前端（改页面最快路径）
.\deploy-test.ps1 -Part Back     # 只构建部署后端
.\deploy-test.ps1 -NoBuild       # 跳过构建直接部署
```

子项目内命令（将来拆分后同样适用）：

```bash
# frontend/
npm run dev          # Vite dev server（:5173），/api 代理
npm run build:war    # 类型检查 + build + 打 hcs.war

# backend/（Windows 用 mvnw.cmd）
./mvnw clean package              # 产出 target/api.war
./mvnw spring-boot:run            # 本地 :8090/api
./mvnw test -Dtest=XxxTest#method # 单测（src/test 当前为空）
```

无 lint 脚本，无前端测试框架。

## 按需加载地图（渐进式披露）

| 何时读 | 读哪个文件 |
|--------|-----------|
| 执行部署/发布、排查 Tomcat 启动问题 | `docs/ai/deploy.md` |
| 改接口、改请求/响应格式、前后端联调 | `docs/ai/contract.md` |
| 文件保护、写日志/周报、判断文档可信度 | `docs/ai/conventions.md` |
| 前端改动 | `frontend/CODEBUDDY.md`（含其加载地图） |
| 后端改动 | `backend/CODEBUDDY.md`（含其加载地图） |

## 最小必读事实

- 前后端 war（`hcs.war` + `api.war`）同 Tomcat（`E:\software\tomcat11`，:8080）同源部署，**此为临时形态**；后端 war 名必须保持 `api.war`，否则前端全线 404。
- `VITE_API_BASE_URL` 只填 origin 根，**绝不能带 `/api`**；改接口地址 dev/生产要改两处（详见 `docs/ai/contract.md`）。
- 所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行。
