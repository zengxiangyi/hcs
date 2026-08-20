# 后端常用命令

所有命令在根目录执行。无 lint / test 脚本；类型检查内置于 `npm run build`（`tsc`）。

## 开发 / 构建

| 命令 | 作用 |
|------|------|
| `npm install` | 安装依赖（当前 `node_modules` 未安装，首次需执行） |
| `npm run dev` | 启动开发服务，`tsx watch src/index.ts` 热重载，监听 8080 |
| `npm start` | `tsx` 直接运行（不监听热重载） |
| `npm run build` | `tsc` 编译 TS 到 `dist/`（严格模式类型检查） |

## 数据库

| 命令 | 作用 |
|------|------|
| `npm run db:push` | 将 schema 直接推送到数据库建表/更新表（需先存在库 `user_test`） |
| `npm run db:generate` | 由 schema 生成迁移 SQL 到 `drizzle/` |
| `npm run db:studio` | 打开 drizzle 可视化 Studio |
| `npm run db:seed` | 插入种子数据：`admin/123456` 账号 + 8 条示例用户（幂等，可重复执行） |

## 前置条件

- 依赖：Node.js ≥ 20
- MySQL 已启动；若库不存在先执行：
  `CREATE DATABASE user_test CHARACTER SET utf8mb4;`

## 验证

- 健康检查：`curl http://localhost:8080/api/health`
- 登录：`POST /api/auth/login` body `{"username":"admin","password":"123456"}`
