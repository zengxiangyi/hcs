# CODEBUDDY.md

This file provides guidance to CodeBuddy when working with code in this repository.

## Project Overview

The workspace `frontend/` is a **Vue 3 + Vite 8 + TypeScript** SPA for a user-management admin app. It consumes a backend API proxied via Vite during dev.

## Commands

```bash
npm install           # install dependencies
npm run dev           # start Vite dev server (default http://localhost:5173), HMR enabled
npm run build         # type-check via vue-tsc -b, then vite build -> dist/
npm run preview       # preview the production build locally
```

No unit-test framework is configured in `package.json`.

## Architecture

### Request/Response contract

The frontend models the API response envelope as `ApiResponse<T>` = `{ code, data, msg }` in `src/api/http.ts`. The axios response interceptor unwraps this envelope, so API methods return `Promise<ApiResponse<T>>` and callers read business data via `res.data`, while `err.message` carries the backend's `msg` (e.g. "用户名或密码错误").

**Semantic of HTTP 401 vs business errors:** the frontend treats HTTP 401 as "token expired" — it clears `localStorage.token` and hard-redirects to `/`.

### Data flow

- `src/api/http.ts` — the single axios instance with request interceptor (injects `Authorization: Bearer <token>` from `localStorage`) and response interceptor (envelope unwrap + 401 handling). Exported as a typed `TypedHttp` (get/post/put/delete returning `Promise<ApiResponse<T>>`).
- `src/api/base.ts` — auth-related endpoints (`/api/auth/login`, `/api/auth/logout`, `/api/auth/verify-identity`, `/api/auth/reset-password`, `/api/user/info`) + their TS param/result interfaces.
- `src/api/data.ts` — user CRUD endpoints (`/api/users`) + paginated list query types.
- `src/router/index.ts` — routes + a global `beforeEach` guard. `WHITE_LIST` contains route names that skip auth (currently `Login`); all other routes require a token to exist locally AND validate against the backend `getUserInfo()`. On failure it clears the token and redirects to `/` with a `redirect` query param.
- `src/components/` — `Login.vue` (login + two-step password reset dialog), `Web.vue` (authenticated layout shell with top bar + left menu + nested `<router-view>`), `MenuBar.vue` (recursive menu driven by `src/config/menu.json`), and the feature page `src/components/data/data2.vue` (fully wired to CRUD with server-side pagination + XLSX import/export).
- `src/utils/md5.ts` — hand-rolled MD5; passwords are MD5-hashed **before** being sent.

Element Plus is registered globally in `src/main.ts` with `zh-cn` locale; `unplugin-vue-components` auto-imports `el-*` components (see `vite.config.ts` + generated `components.d.ts`), so no manual component registration is needed.

### Config / env

- Vite dev proxy forwards `/api` to `http://localhost:8080`; `baseURL` defaults to `/` unless `VITE_API_BASE_URL` is set.

## Gotchas

- `docs/struct.md` documents the current project structure and `.gitignore` rules — keep it in sync when structure changes.
- Template leftover files (`HelloWorld.vue`, `src/assets/*`, `public/icons.svg`, `data1.vue`) were removed; the only feature page under `src/components/data/` is `data2.vue`.
- Passwords are MD5-hashed client-side.
- `pageSize` for the export in `data2.vue` is hardcoded to `99999` to fetch the full list.
