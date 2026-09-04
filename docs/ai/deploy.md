# 部署与发布（按需加载：执行部署/发布操作时读取）

## 一键发布脚本 `f:\hb\page\deploy-test.ps1`（Windows PowerShell）

```powershell
.\deploy-test.ps1                # 全量：构建前端 hcs.war + 后端 api.war → 部署到 E:\software\tomcat11 → 跟踪日志
.\deploy-test.ps1 -Part Front    # 只构建部署前端（改前端页面最快路径，不必重打 66MB 后端包）
.\deploy-test.ps1 -Part Back     # 只构建部署后端
.\deploy-test.ps1 -NoBuild       # 跳过构建，直接部署已有 war
.\deploy-test.ps1 -NoTail        # 部署完不跟踪 catalina 日志
.\deploy-test.ps1 -Force         # Tomcat 30 秒未退出时强制杀 java（会杀掉本机全部 java）
.\deploy-test.ps1 -RunTests      # 后端构建不跳过测试（默认 -DskipTests）
```

脚本流程：构建 → war 条目自检（tar 检查 `assets/`、`WEB-INF/web.xml`）→ 停 Tomcat → 清理 `webapps/{api,hcs}` 与 `work/Catalina/localhost/*` 残留 → 拷贝 war → 启动 → 跟踪日志。
日志成功特征 `Started ApiApplication`；失败特征 `HikariPool-1 ... Exception` / `FileNotFoundException: webapps\hcs\assets`。

## 部署形态（临时/过渡，后期会调整）

- `hcs.war`（前端，context-path `/hcs`）与 `api.war`（后端，context-path `/api`）同部署于 `E:\software\tomcat11`（端口 8080）。
- 同 Tomcat 同端口 → **origin 相同** → localStorage 共享、前端调 `/api/**` 无 CORS。
- 外置容器时后端 `server.port` 与 `server.servlet.context-path` 均被忽略，**context-path 由 war 文件名决定**，必须保持 `api.war`，否则前端全线 404 且无编译期提示。
- 部署形态变化时**只改 `deploy-test.ps1`**。
