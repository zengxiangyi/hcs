# 前端 war 打包与部署（按需加载：执行 build:war、排查部署/刷新 404 问题时读取）

## 打包

- `npm run build:war`：vue-tsc 类型检查 + vite build + 打包 `hcs.war`。
- `hcs.war` → Tomcat context-path **`/hcs`**（`webapps/hcs/`），内容 = `dist/` + `public/WEB-INF/web.xml`。
- `web.xml` 唯一职责：`404 → /index.html`（context-relative，解析为 `/hcs/index.html`），保证 vue-router **history 模式**刷新/直链 `/hcs/web/xxx` 可用（Tomcat forward，状态仍 404 但 body 是 index.html）。
- Tomcat 必须 **10.1+（Servlet 6.1）** 以支持 `jakarta` `web-app_6.0` 描述符。

## 打包陷阱（勿回退）

`build:war` 用 .NET `System.IO.Compression.ZipArchive` 打包，写入**显式目录条目**（`assets/`、`WEB-INF/`）+ 正斜杠文件条目。
**不要**换回 `Compress-Archive` / `ZipFile::CreateFromDirectory`：它们只产生文件条目，Tomcat 11 解包时报
`ContextConfig.beforeStart ... FileNotFoundException: webapps\hcs\assets\foo.js`（ERROR_PATH_NOT_FOUND）。

验证 war：`tar -tf hcs.war | Select-String '/$'`（必须用 bsdtar——.NET 在 Windows 会把 `/` 重写为 `\`，不能用 .NET API 查条目名）。应看到 103 条含 `assets/`、`WEB-INF/`。
