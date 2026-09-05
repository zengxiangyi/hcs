-- ============================================================
-- 下线 flowNodeAction 表
-- 申请日期：2026-08-31
-- 申请人：（开发者）
-- 执行人：DBA
--
-- 下线理由：
--   1. 代码中已无对应 JPA 实体，全仓库（src/ + docs/）0 处引用；
--   2. 流程流转能力现由 flownode（节点定义）+ flowedge（连线，含 cond 条件）+ FlowEngine 承担，
--      flowNodeAction 的「当前节点 + action -> nextNode」职责已被 flowedge 取代；
--   3. DB/table/ 下已不再收录该表。
--
-- ⚠️ 风险：DROP TABLE 不可回滚。建议按「第 1 步改名观察 -> 第 2 步备份 -> 第 3 步删除」顺序执行。
-- ============================================================

-- ---------- 第 0 步：执行前确认（只读，先跑这几条） ----------

-- 确认表存在及其体积
SELECT TABLE_NAME, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'page' AND TABLE_NAME = 'flowNodeAction';

-- 确认表结构（留档）
DESCRIBE `flowNodeAction`;

-- 确认是否有数据
SELECT COUNT(*) AS row_count FROM `flowNodeAction`;

-- 确认无其他库/视图/存储过程引用
SELECT ROUTINE_SCHEMA, ROUTINE_NAME, ROUTINE_TYPE
FROM information_schema.ROUTINES
WHERE ROUTINE_DEFINITION LIKE '%flowNodeAction%';

SELECT TABLE_SCHEMA, TABLE_NAME, VIEW_DEFINITION
FROM information_schema.VIEWS
WHERE VIEW_DEFINITION LIKE '%flowNodeAction%';


-- ---------- 第 1 步：改名下线（推荐先做，可快速回滚） ----------
-- 改名后应用侧不受影响（本就无引用），观察 1~2 个发布周期无异常再删。

RENAME TABLE `flowNodeAction` TO `_dropped_20260831_flowNodeAction`;


-- ---------- 第 2 步：备份（DROP 前必须执行） ----------
-- 方式 A：CREATE TABLE ... SELECT 复制一份（库内备份）
CREATE TABLE `_bak_20260831_flowNodeAction` AS
SELECT * FROM `_dropped_20260831_flowNodeAction`;

-- 方式 B（推荐，独立文件备份）：mysqldump 单表
-- mysqldump -h 127.0.0.1 -P 3306 -u root -p page _dropped_20260831_flowNodeAction > flowNodeAction_20260831.sql


-- ---------- 第 3 步：删除（确认备份完成后执行） ----------
DROP TABLE `_dropped_20260831_flowNodeAction`;


-- ---------- 回滚方案 ----------
-- 若第 1 步改名后发现问题，立即回滚：
--   RENAME TABLE `_dropped_20260831_flowNodeAction` TO `flowNodeAction`;
--
-- 若已进入第 3 步，从库内备份恢复：
--   CREATE TABLE `flowNodeAction` AS SELECT * FROM `_bak_20260831_flowNodeAction`;
-- 或从 mysqldump 文件恢复：
--   mysql -h 127.0.0.1 -P 3306 -u root -p page < flowNodeAction_20260831.sql
-- 注意：这两种方式均不恢复索引/自增属性，需按第 0 步 DESCRIBE 的留档重建。


-- ---------- 执行后 ----------
-- 1. 从 docs/DB/table/ 移除「已下线」提示段落；
-- 2. 确认 `_bak_20260831_flowNodeAction` 保留策略（建议保留至下一发布周期后再清理）。
