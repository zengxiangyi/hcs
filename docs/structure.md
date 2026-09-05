# 仓库结构总览（Workspace Structure）

> 工作区根目录：`f:\hb\page`
>
> **核心约定：`frontend/` = 前端页面根目录，`backend/` = 后端接口根目录。**
>
> 最后核对时间：2026-09-03（以实际文件树为准）

---

## 一、顶层布局

```
f:\hb\page\
├── frontend/          # ★ 前端页面根目录（Vue 3 + Vite 8 + TypeScript SPA）
├── backend/           # ★ 后端接口根目录（Spring Boot 4.0.8 / Java 25，Maven WAR）
├── docs/              # 工作区级文档（周报、日志、本文件）
├── CODEBUDDY.md       # 工作区级 AI 指引
└── README.md
```

| 目录 | 角色 | 技术栈 | 产出物 | 部署形态 |
|------|------|--------|--------|----------|
| `frontend/` | 前端页面根 | Vue 3 + Vite 8 + TS + Element Plus | `hcs.war` | Tomcat context-path `/hcs` |
| `backend/` | 后端接口根 | Spring Boot 4.0.8 / Java 25 / JPA + MyBatis | `target/api.war` | Tomcat context-path `/api` |

两个 war 部署在**同一 Tomcat**（`E:\software\tomcat11`，端口 8080），context-path 不同但 **origin 相同**（scheme/host/port 一致），因此 localStorage 共享、前端调 `/api/**` 不触发 CORS。

> ⚠️ **该部署形态是临时的**：「前后端 war 同 Tomcat」仅为**测试阶段的过渡方案，后期会调整**（2026-09-03 确认）。
> 测试阶段发布用一键脚本 `f:\hb\page\deploy-test.ps1`（详见 `frontend/docs/build.md` 第 5.0 节），
> 不走多步手工流程；后期形态变化时只改该脚本。

> ⚠️ 根 `.codebuddy/` 之外，**`frontend/.codebuddy/` 与 `backend/.codebuddy/` 各有一套 memory**（按模块分记，写日志/周报时三处都要读）。

---

## 二、`frontend/` — 前端页面根目录

### 2.1 根目录文件

| 文件 | 用途 |
|------|------|
| `index.html` | Vite HTML 入口，挂载点 `#app` |
| `vite.config.ts` | `base: '/hcs/'`；dev 代理 `/api` → `http://127.0.0.1:8080`；`unplugin-vue-components` + `ElementPlusResolver` 自动导入 |
| `package.json` | name `page`；脚本 `dev` / `build`(`vue-tsc -b && vite build`) / `build:war` / `preview`；**无测试框架、无 lint 脚本** |
| `tsconfig.json` / `tsconfig.app.json` / `tsconfig.node.json` | Project References 模式 |
| `components.d.ts` | 自动生成的组件类型声明，勿手改 |
| `.env.production` / `.env.example` | `VITE_API_BASE_URL=http://127.0.0.1:8080`（**绝不能带 `/api` 后缀**） |
| `hcs.war` / `ROOT.war` | 构建产物（历史遗留两个 war，当前以 `hcs.war` 为准） |
| `CODEBUDDY.md` | 前端 AI 指引 |
| `docs/` | `struct.md`（部分内容已过时）、`api.md`、`build.md`（发布必读）、`table.md` |

### 2.2 `src/` 源码结构

```
src/
├── main.ts              # 入口：Pinia + Element Plus(zh-cn) + Router
├── App.vue              # 根组件，仅 <router-view />
├── style.css
├── style/common.css
├── api/                 # 接口层（17 个模块，均为 /api/** 完整路径）
│   ├── http.ts          # axios 单例：注入 Bearer token、解包 ApiResponse、401 清 token 跳登录
│   ├── auth.ts          # /api/auth/**
│   ├── user.ts          # /api/sysUser/**
│   ├── role.ts          # /api/sysRole/**
│   ├── right.ts         # /api/sysRight/**
│   ├── roleUser.ts      # /api/sysRoleUser/**
│   ├── roleRight.ts     # /api/sysRoleRight/**
│   ├── constValue.ts    # /api/constValue/**
│   ├── blueprint.ts     # /api/blueprint/**
│   ├── tech.ts          # /api/blueprint/**（工艺编制，复用蓝本接口）
│   ├── techStep.ts      # /api/techstep/**
│   ├── process.ts       # /api/sysRight/**（流程权限复用）
│   ├── taskProcess.ts   # /api/taskprocess/**
│   ├── transfer.ts      # /api/transfer/**
│   ├── approval.ts      # /api/workflow/**
│   ├── instance.ts      # /api/workflow|flowCurrent|flowHistory/**
│   └── flow.ts          # /api/flowGraph|flowNode|flowEdge/**
├── router/index.ts      # 路由表 + 全局守卫（白名单 Root/Login；meta.right 页面级鉴权）
├── config/menu.json     # 侧边菜单配置（驱动 MenuBar 递归渲染）
├── utils/
│   ├── md5.ts           # 手写 MD5（密码发送前加密）
│   └── enum.ts
├── components/
│   ├── Login.vue        # 登录页 + 两步重置密码
│   ├── Web.vue          # 认证后布局外壳（顶栏 + 左菜单 + router-view）
│   ├── MenuBar.vue      # 递归菜单
│   ├── sys/permission.ts# 权限缓存与 hasRight() 判定
│   ├── bluePrint/       # list.vue                     → /web/bluePrint/list
│   ├── tech/            # board.vue / step.vue / draft.vue
│   ├── approval/        # send.vue / todo.vue / done.vue
│   ├── workflow/        # flow.vue / draw.vue / instance.vue
│   ├── process/         # task.vue / process.vue / processV.vue + steps/(S01~S16 子步骤组件)
│   ├── product/         # transfer.vue / state.vue
│   ├── info/            # file.vue
│   ├── sys/             # user/role/right/roleUser/roleRright/constValue .vue
│   └── step/            # （空目录，暂无文件）
└── public/
    ├── favicon.svg
    └── WEB-INF/web.xml  # 404 → /index.html，保证 history 模式刷新可用
```

### 2.3 路由与页面映射

全部业务页挂在 `/web` 子路由下，且都带 `meta.right`（`page:xxx` 权限码），由 `router.beforeEach` + `hasRight()` 做页面级拦截：

| 分组 | 路由 | 组件 | 权限码 |
|------|------|------|--------|
| 蓝本 | `/web/bluePrint/list` | `bluePrint/list.vue` | `page:blueprint:list` |
| 审批 | `/web/approval/{send,todo,done}` | `approval/*.vue` | `page:approval:*` |
| 工艺 | `/web/tech/{board,step,draft}` | `tech/*.vue` | `page:tech:*` / `page:technology` |
| 资料 | `/web/info/file` | `info/file.vue` | `page:info:file` |
| 工序 | `/web/process/{task,flow}` | `process/task.vue` / `process/process.vue` | `page:process*` |
| 产品 | `/web/product/{transfer,state}` | `product/*.vue` | `page:product` |
| 系统 | `/web/sys/{user,role,right,roleUser,roleRight,constValue}` | `sys/*.vue` | `page:system:*` / `page:info:constValue` |
| 工作流 | `/web/workflow/{flow,draw,instance}` | `workflow/*.vue` | `page:info:*`（待细分） |

### 2.4 请求契约

- 统一响应体 `ApiResponse<T> = { code, data, msg }`（`src/api/http.ts`），响应拦截器已解包，业务数据取 `res.data`，错误取 `err.message`。
- HTTP 401 = token 失效 → 清 `localStorage.token` → 跳转 `/`。
- 密码客户端 MD5 后再发送。

---

## 三、`backend/` — 后端接口根目录

### 3.1 技术栈与构建

| 项 | 值 |
|----|----|
| 框架 | Spring Boot 4.0.8（parent），Java 25 |
| 打包 | `war`，`finalName=api` → `target/api.war` |
| 根包 | `com.baogang.info` |
| 持久化 | Spring Data JPA **+** MyBatis 双持久化，共享 DataSource 与事务 |
| 数据库 | MySQL `jdbc:mysql://127.0.0.1:3306/page`（`ddl-auto=none`） |
| 安全 | Spring Security STATELESS + JWT（`JwtAuthenticationFilter`） |
| 内嵌端口 / 路径 | `8090` / `/api`（外置 Tomcat 时 port 与 context-path 均被忽略，context-path 由 war 名决定） |

常用命令（Windows 用 `./mvnw.cmd`）：

```
./mvnw clean package            # 打包出 target/api.war
./mvnw clean package -DskipTests
./mvnw spring-boot:run          # 本地 :8090/api
./mvnw test -Dtest=XxxTest      # src/test 当前为空，跑 0 个用例
```

### 3.2 `src/main/java/com/baogang/info/` 分层

```
com.baogang.info/
├── ApiApplication.java         # Spring Boot 启动类
├── ServletInitializer.java     # 外置 Tomcat 部署入口
├── controller/   18 个       # @RestController，仅参数校验 + ApiResponse 包装
├── service/      17 个       # 业务层（含 FlowEngine 流程引擎），写操作 @Transactional
├── repository/   16 个       # JPA JpaRepository，单表 / 参数 ≤3 的简单查询
├── mapper/        9 个       # MyBatis @Mapper，SQL 在 resources/mapper/*.xml
├── entity/       16 个       # JPA @Entity 兼 MyBatis resultType
├── dto/          10 个       # XxxQuery 可变查询条件（null 不过滤）+ page/pageSize
├── common/                   # ApiResponse、PageResult、JwtUtil、JwtAuthenticationFilter、LoginOut、FowGraph
├── config/                   # SecurityConfig、CorsConfig
├── exception/                # GlobalExceptionHandler、BusinessException、ResourceNotFoundException
└── tool/                     # StringTool、CollectionTool、DateTimeTool、JsonTool、UserInfo
```

### 3.3 业务模块

| 模块 | 实体 | 主要 Controller |
|------|------|-----------------|
| 蓝本 BluePrint | `BluePrint` | `BluePrintController`（字段最多、查询最复杂，**新增模块照抄它**） |
| 工作流 | `FlowGraph` / `FlowNode` / `FlowEdge` / `Workflow` / `FlowCurrent` / `FlowHistory` | `FlowGraph/Node/Edge/Current/History/Workflow/EngineController`，引擎在 `service/FlowEngine` |
| 系统权限 | `SysUser` / `SysRole` / `SysRight` / `SysRoleRight` / `SysRoleUser` | `AuthController` + 各 `Sys*Controller` |
| 单据与常量 | `TaskProcess` / `TransferOrder` / `ConstValue` | 对应 Controller |
| 工艺工序 | 复用 `BluePrint` + `TechStep` | `TechStepController` |

MyBatis mapper（9 个，均有对应 XML）：BluePrint、ConstValue、SysRight、SysRole、SysUser、TaskProcess、TechStep、TransferOrder、Workflow。

### 3.4 关键约定

- **双持久化**：单表简单 CRUD → JPA；动态/复合/多表 JOIN → MyBatis，**XML 才是查询真源**。同方法混用需加 `@Transactional`。
- **DB 列名一律小写无下划线**（`createuser`、`flowgraph`），JPA 侧必须显式 `@Column(name=...)`；`map-underscore-to-camel-case=true`。
- **`ddl-auto=none`**：所有 DDL/DML 由 DBA 执行，AI 只以 SQL 文本交付，禁止自行连库执行。
- **REST**：响应一律 `ApiResponse<T>`，分页 `PageResult<T>`；1-based `page`；复杂查询用 `POST /xxx/search` + `XxxQuery`；写操作 `POST /xxx/save`、`PUT /xxx/update`、`DELETE /xxx/{id}`。
- **认证不做角色鉴权**：过滤器只认证，角色/权限由 `AuthController` 登录时随 `LoginOut.roles/rights` 下发前端。
- **受保护文件**：`mvnw` / `mvnw.cmd` / `info.iml` / `target/` / `.mvn/` / `.idea/` 只读。

### 3.5 `backend/docs/`

| 文件 | 可信度 |
|------|--------|
| `DB/table/` | **schema 真源**（`page` 库 18 张表，一表一文件 + `README.md` 索引，由库中 `information_schema` 导出） |
| `plans/` | 历史评审报告与待执行 SQL |

---

## 四、跨端协作要点

1. **接口前缀对齐**：前端 `src/api/*.ts` 写的是完整路径 `/api/xxx`，后端 context-path 必须保持 `/api`（即 war 名必须是 `api.war`），否则全线 404 且无任何编译期提示。
2. **同源部署**：`hcs.war` 与 `api.war` 同 Tomcat 同端口，前端应把 `VITE_API_BASE_URL` 设为 `/` 走同源（当前 `.env.production` 仍写 `http://127.0.0.1:8080`，需按实际部署调整）。
3. **字段名不一致陷阱**：后端 `techstep` 表 `step` = 工序编号、`stepName` = 工序名称；前端表格 row 的 `step` 存名称、`stepCode` 存编号（见 `frontend/src/components/tech/board.vue`）。
4. **文档过时项**：
   - `frontend/CODEBUDDY.md` 与 `.env.production` 注释仍称后端为「独立 Fastify 服务」，实际已是 Spring Boot 战争包 —— 注释待更正。
   - `frontend/docs/struct.md` 描述的 `src/api/base.ts`、`data/data2.vue` 等已不存在，结构部分已过时，以本文档为准。
