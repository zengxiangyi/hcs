# CODEBUDDY.md

This file provides guidance to CodeBuddy when working with code in this repository.

## Project Overview

The workspace `frontend/` is a **Vue 3 + Vite 8 + TypeScript** SPA for a user-management admin app. It consumes a backend API proxied via Vite during dev.

## Commands

```bash
npm install           # install dependencies
npm run dev           # start Vite dev server (default http://localhost:5173), HMR enabled
npm run build         # type-check via vue-tsc -b, then vite build -> dist/
npm run build:war     # build, then zip dist -> ROOT.war (deployable static war)
npm run preview       # preview the production build locally
```

### Deployment

Production is deployed as a **separate static war** (`hcs.war`, built by `npm run build:war`), not bundled into the
backend:

- `hcs.war` → Tomcat context-path **`/hcs`** (i.e. the `webapps/hcs/` directory) holds `dist/` plus
  `public/WEB-INF/web.xml`, whose only job is
  `<error-page><error-code>404</error-code><location>/index.html</location></error-page>` (context-relative, so it
  resolves to `/hcs/index.html`). This is what makes vue-router **history mode** survive a refresh/direct hit on
  `/hcs/web/xxx`. (Tomcat forwards, so the status stays 404 while the body is `index.html`; the browser renders it
  normally.)
- The backend is a **standalone Fastify** service (see `../backend`) listening on `http://127.0.0.1:8080`, with
  routes registered at `/api/**` (`/api/auth/login`, `/api/users`, ...). It is **not** a Java war — do not put it in
  Tomcat.
- Because the frontend origin (Tomcat host:port) usually differs from the backend origin, `.env.production` sets an
  **absolute** `VITE_API_BASE_URL`. CORS is enabled on the backend (`origin` reflection + credentials), so
  cross-origin calls work; the `Authorization` header is attached by the axios request interceptor.
- Tomcat must be **10.1+ (Servlet 6.1)** for the `jakarta` `web-app_6.0` descriptor.
- **Packaging**: `build:war` zips `dist/` with .NET `System.IO.Compression.ZipArchive`, writing **explicit directory
  entries** (`assets/`, `WEB-INF/`) plus file entries with **forward slashes**. Do **not** switch back to
  `Compress-Archive` / `ZipFile::CreateFromDirectory`: they emit only file entries, and Tomcat 11 then fails while
  unpacking with `ContextConfig.beforeStart ... FileNotFoundException: webapps\hcs\assets\foo.js`
  (ERROR_PATH_NOT_FOUND — the `assets` directory was never created).
  Verify an archive with `tar -tf hcs.war | Select-String '/$'` (bsdtar); expect 103 entries incl. `assets/`,
  `WEB-INF/`. Note that .NET's `ZipArchiveEntry.FullName` rewrites `/` to `\` on Windows, so always inspect entry
  names with bsdtar, not .NET.

No unit-test framework is configured in `package.json`.

## Architecture

### Request/Response contract

The frontend models the API response envelope as `ApiResponse<T>` = `{ code, data, msg }` in `src/api/http.ts`. The axios response interceptor unwraps this envelope, so API methods return `Promise<ApiResponse<T>>` and callers read business data via `res.data`, while `err.message` carries the backend's `msg` (e.g. "用户名或密码错误").

**Semantic of HTTP 401 vs business errors:** the frontend treats HTTP 401 as "token expired" — it clears `localStorage.token` and hard-redirects to `/`.

### Data flow

- `src/api/http.ts` — the single axios instance with request interceptor (injects `Authorization: Bearer <token>` from `localStorage`) and response interceptor (envelope unwrap + 401 handling). Exported as a typed `TypedHttp` (get/post/put/delete returning `Promise<ApiResponse<T>>`).
- `src/api/base.ts` — auth-related endpoints (`/api/auth/login`, `/api/auth/logout`, `/api/auth/verify-identity`, `/api/auth/reset-password`, `/api/user/info`) + their TS param/result interfaces.
- `src/api/user.ts` — user CRUD endpoints (`/api/users`) + paginated list query types.
- `src/router/index.ts` — routes + a global `beforeEach` guard. `WHITE_LIST` contains route names that skip auth (currently `Login`); all other routes require a token to exist locally AND validate against the backend `getUserInfo()`. On failure it clears the token and redirects to `/` with a `redirect` query param.
- `src/components/` — `Login.vue` (login + two-step password reset dialog), `Web.vue` (authenticated layout shell with top bar + left menu + nested `<router-view>`), `MenuBar.vue` (recursive menu driven by `src/config/menu.json`), and the feature page `src/components/data/data2.vue` (fully wired to CRUD with server-side pagination + XLSX import/export).
- `src/utils/md5.ts` — hand-rolled MD5; passwords are MD5-hashed **before** being sent.

Element Plus is registered globally in `src/main.ts` with `zh-cn` locale; `unplugin-vue-components` auto-imports `el-*` components (see `vite.config.ts` + generated `components.d.ts`), so no manual component registration is needed.

### Config / env

- Vite dev proxy forwards `/api` to the target in `vite.config.ts` (no rewrite). `baseURL` defaults to `/` unless `VITE_API_BASE_URL` is set, so dev requests go to relative `/api/...` and hit the proxy. **The dev proxy has NO effect on production builds** — for `npm run build:war`, the API address comes solely from `VITE_API_BASE_URL` in `.env.production`.
- `base` is **`/hcs/`** (`vite.config.ts`): production is deployed as Tomcat context-path `/hcs`. `src/router/index.ts` uses `createWebHistory(import.meta.env.BASE_URL)`, and the 401 redirect also uses `BASE_URL`, so both follow `base` automatically — nothing else needs changing if the context-path changes.
- `.env.production` sets `VITE_API_BASE_URL` (backend origin root, e.g. `http://10.21.46.191:8080`; use `/` when frontend and backend are same-origin). Re-run `npm run build:war` after changing it.
  **Never append `/api` here.** Every module in `src/api/*.ts` already requests full paths like `/api/auth/login`, so
  the final URL is `VITE_API_BASE_URL + '/api/...'`; adding `/api` to the base yields `/api/api/auth/login` (404).
  If the backend is ever reverse-proxied onto the same origin, set this to `/` instead.

## Gotchas

- `docs/struct.md` documents the current project structure and `.gitignore` rules — keep it in sync when structure changes.
- **Never put `/api` into `VITE_API_BASE_URL`**: `src/api/*.ts` paths already start with `/api`, so `/api` in the base produces `/api/api/...` 404s. Symptom to watch for in devtools: `http://127.0.0.1:8080/api/api/auth/login`.
- Template leftover files (`HelloWorld.vue`, `src/assets/*`, `public/icons.svg`, `data1.vue`) were removed; the only feature page under `src/components/data/` is `data2.vue`.
- Passwords are MD5-hashed client-side.
- `pageSize` for the export in `data2.vue` is hardcoded to `99999` to fetch the full list.
