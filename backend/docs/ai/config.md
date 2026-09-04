# 后端配置（按需加载：改 application.properties、排查启动/连接问题时读取）

`src/main/resources/application.properties`：

- 端口 `8090`、上下文 `/api`（**外置 Tomcat 时两者均被忽略**：context-path 由 war 文件名决定，必须保持 `api.war`）。
- MySQL `jdbc:mysql://127.0.0.1:3306/page`（Hikari 池 10）；`ddl-auto=none`、`open-in-view=false`、dialect `MySQLDialect`。
- 上传上限 100MB；MyBatis `mapper-locations=classpath*:mapper/*.xml`、`type-aliases-package` 指向 entity、`map-underscore-to-camel-case=true`。
- JWT：`jwt.secret` / `jwt.expiration-ms` / `jwt.issuer`（明文硬编码，开发阶段暂缓外置）。
- CORS：`cors.allowed-origins`（默认 `*`，生产改白名单）。

## 无环境化配置

`src/main/resources` 下**只有一份 `application.properties`**，不存在 `application-prod.properties` 等 profile 文件，不依赖 `SPRING_PROFILES_ACTIVE`。

## 日志

- 项目无自定义 `logback-spring.xml`，走 Spring Boot 默认**根级别 INFO**；需逐个显式开 `logging.level.<包>=DEBUG`（已开 `com.baogang.info.mapper`、`com.baogang.info.tool`）。
- 无 actuator/devtools，`logging.level.*` 不会热更新，**改完必须重新 `mvn package` + 重启 Tomcat**（`.\deploy-test.ps1 -Part Back`）。
- 未配 `logging.file.name`，日志只进 console（Tomcat 控制台 / `logs/catalina.*.log`）。
