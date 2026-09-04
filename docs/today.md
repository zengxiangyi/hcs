# 工作日志 · 2026-09-03

> 项目：hb/page（前端 `frontend/` Vue3 + Vite + TS）
> 主要改动文件：`frontend/src/components/tech/board.vue`、`frontend/src/components/tech/draft.vue`

---

## 1. 移除 tech 模块「工艺编制」动态方案功能

- `board.vue`：删除模板中的「工艺编制」section（原 459-464 行）、`planComponentMap` / `currentPlan` / `planModel`、二级工艺 watch、`onCancel` 中的 plan 重置；保留一级工艺变更清空二级工艺的逻辑。
- `draft.vue`：同步清理（同样引用 `./plan/*`，且被路由 `/web/tech/draft` 加载，不清理会导致构建失败）。
- 删除 `frontend/src/components/tech/plan/` 下全部子文件（19 个方案组件 CHTZ/TH/ZH/TP*.vue + types.ts），目录保留为空。
- 备注：`board.vue` 仍存在两处既有 TS 警告（未使用的 `craftTree1`、`onCancel`），非本次改动引入。

## 2. board.vue 新增「工艺编制」工序动态表格

- 列结构：序号 / 一级工艺 / 二级工艺 / 工序（名称）/ 工序编号（S01…）/ 排序 / 选择类型 / 备注 / 操作，行内可编辑。
- `initStepRows()`：按当前一、二级工艺调用 `techStepAPI.search({ firstLevel, secondLevel, page: 1, pageSize: 200 })` 初始化；无数据或请求失败时补一行空白；`watch(basicForm.secondLevel, initStepRows, { immediate: true })`，一级工艺变更清空二级也会自动触发重新初始化。
- 字段映射约定：后端 `techstep` 表 `step` = 工序编号、`stepName` = 工序名称；表格 row 的 `step` 存名称、`stepCode` 存编号。
- 工序下拉 `stepMap`（S01–S20）与 `step.vue` 保持一致；选择 `stepCode` 后由 `onStepCodeChange` 自动带出工序名称。
- 编码→名称映射 `stepFirstLabelMap` / `stepSecondLabelMap`：先铺 `craftTree1`（完整树），再用 `craftTree`（board 自有 CH/ZH 树）覆盖，未命中则回显编码。
- 工具栏：`增加工序`、`重新初始化`（均需先选择二级工艺）。

## 3. 工序表「排序」列改为数字输入

- `StepRow.sort` 类型 `string` → `number | null`；`createStepRow` 直接赋 `stepRows.value.length + 1`。
- 新增 `strToNum(v)` 辅助函数（与既有 `numToStr` 配套），`initStepRows` 中用 `sort: strToNum(r.sort)` 做后端字符串→数字转换。
- 表格列 `<el-input>` → `<el-input-number :min="1" :precision="0" size="small" controls-position="right" style="width:100%">`，列宽 90 → 110。
- 顺带删除重复的第二个 `prop="sort" label="排序"` 列（原 595-599 行，与 566-570 完全重复）。
- 提交入参 `TechBoardSaveDTO` 当时尚未包含 stepRows，故类型变更不影响保存逻辑。

## 4. 工序表新增行内「保存（暂存）」+ 只读锁定

- `StepRow` 增加 `saved: boolean`；新增 `stepStagedRows = ref<StepRow[]>([])` 作为暂存区，保存时 push 的是**行对象引用**（非拷贝），便于编辑/删除时按引用反查移除。
- 操作列宽 80 → 140：未暂存显示「保存」，已暂存显示「编辑」（取消暂存）+ 始终显示「删除」。
- `handleSaveStep(row)`：校验 `stepCode` 非空 → push 暂存区 → `row.saved = true`；`handleEditStep(row)` 反查移除并解锁；`handleDeleteStep(index)` 同步清理暂存区。
- 行内 `sort` / `step` / `stepCode` / `isNeed` / `remark` 五个单元格统一加 `:disabled="row.saved"` 实现只读。
- 重新初始化（`initStepRows`，含二级工艺为空分支）与 `onCancel` 均清空 `stepStagedRows`，避免暂存区残留脏数据。

## 5. 工序表按 sort 正向排序

- 新增 `sortStepRows(rows: StepRow[])`：`[...rows].sort((a, b) => (a.sort ?? MAX_SAFE_INTEGER) - (b.sort ?? MAX_SAFE_INTEGER))`，按排序号升序；`sort` 为空/null 的行排末尾（Array.prototype.sort 稳定，排序号相同保持原顺序）。
- `initStepRows()`：有数据走 `sortStepRows(list.map(...))`，无数据（或加载失败）仍补一行空白。
- `handleAddStep()` 改为 `stepRows.value = sortStepRows([...stepRows.value, 新行])`，新增行按排序号归位；行对象引用不变，暂存区不受影响。
- 排序号输入框未做输入时实时重排（避免编辑跳行）。

## 6. 工具栏「重新加载」改为纯内存「排序」

- 需求：重排只针对内存中已有的 `stepRows`，不重新请求后端、不清空 `stepStagedRows`。
- 新增 `handleSortStep() { stepRows.value = sortStepRows(stepRows.value) }`；工具栏按钮文案 `重新加载` → `排序`，`@click` 由 `initStepRows` 改为 `handleSortStep`。
- `initStepRows()` 保留：仍由 `watch(basicForm.secondLevel)` 触发，从后端加载工序模板（该场景替换整表，清空暂存区仍属正确）。

---

## 待办 / 遗留

- 暂存区 `stepStagedRows` 目前只在前端暂存，提交入参 `TechBoardSaveDTO` 尚未携带工序行数据，随「预览 / 发起审核」提交待补。
- `board.vue` 既有 TS 警告：未使用的 `craftTree1`、`onCancel`（非本次改动引入）。
- `frontend/src/components/tech/plan/` 目录已空，可考虑删除。
