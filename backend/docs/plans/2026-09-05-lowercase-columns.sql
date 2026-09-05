-- ============================================================================
-- 2026-09-05  page 库列名统一小写化（交 DBA 执行，AI 不连库执行）
--
-- 背景：代码侧约定「注解/XML 列名全小写」（唯一例外 flownode 表 X/Y/W/H 四个
--       大写列），而库中实列以驼峰为主。MySQL 列名大小写不敏感所以当前能跑，
--       为消除「代码小写 / 库中驼峰」的隐性不一致，DB 侧统一改名对齐代码。
--
-- 说明：
--   1. 仅改大小写、不改拼写：approval.startTime → starttime（疑 startTime 笔误，
--      拼写是否纠正为 startTime 另行决策，本次不动）；flowgraph.height 本已全小写。
--   2. flownode.X / Y / W / H 为既定例外，保持大写，本脚本不涉及。
--   3. MySQL 8.0.46 支持 RENAME COLUMN（8.0+ 语法）；改列名不重建表、不丢数据。
--   4. 执行时机安全：代码里 JPA @Column 与 Mapper XML 全部用小写列名，MySQL 列名
--      大小写不敏感，因此改名前后应用均可正常运行，无需停机窗口。
--   5. 实测含大写字母的列共 72 个，其中 flownode.X/Y/W/H 4 个为例外不动，
--      故本脚本共 15 张表、68 个列改名。
-- ============================================================================

-- approval（2）
ALTER TABLE page.approval    RENAME COLUMN targetCode  TO targetcode;
ALTER TABLE page.approval    RENAME COLUMN startTime    TO starttime;

-- blueprint（16）
ALTER TABLE page.blueprint   RENAME COLUMN firstLevel    TO firstlevel;
ALTER TABLE page.blueprint   RENAME COLUMN secondLevel   TO secondlevel;
ALTER TABLE page.blueprint   RENAME COLUMN materialCode  TO materialcode;
ALTER TABLE page.blueprint   RENAME COLUMN materialName  TO materialname;
ALTER TABLE page.blueprint   RENAME COLUMN isFirstCheck  TO isfirstcheck;
ALTER TABLE page.blueprint   RENAME COLUMN firstHardness TO firsthardness;
ALTER TABLE page.blueprint   RENAME COLUMN lastHardness  TO lasthardness;
ALTER TABLE page.blueprint   RENAME COLUMN busbarNum     TO busbarnum;
ALTER TABLE page.blueprint   RENAME COLUMN testNum       TO testnum;
ALTER TABLE page.blueprint   RENAME COLUMN coolTime      TO cooltime;
ALTER TABLE page.blueprint   RENAME COLUMN hardnessDepth TO hardnessdepth;
ALTER TABLE page.blueprint   RENAME COLUMN fallHead      TO fallhead;
ALTER TABLE page.blueprint   RENAME COLUMN createUser    TO createuser;
ALTER TABLE page.blueprint   RENAME COLUMN createTime    TO createtime;
ALTER TABLE page.blueprint   RENAME COLUMN updateUser    TO updateuser;
ALTER TABLE page.blueprint   RENAME COLUMN updateTime    TO updatetime;

-- flowcurrent（3）
ALTER TABLE page.flowcurrent RENAME COLUMN flowGraph TO flowgraph;
ALTER TABLE page.flowcurrent RENAME COLUMN flowNode  TO flownode;
ALTER TABLE page.flowcurrent RENAME COLUMN startTime TO starttime;

-- flowedge（3）
ALTER TABLE page.flowedge    RENAME COLUMN flowGraph TO flowgraph;
ALTER TABLE page.flowedge    RENAME COLUMN fromNode  TO fromnode;
ALTER TABLE page.flowedge    RENAME COLUMN toNode    TO tonode;

-- flowgraph（1）
ALTER TABLE page.flowgraph   RENAME COLUMN flowGraph TO flowgraph;

-- flowhistory（6）
ALTER TABLE page.flowhistory RENAME COLUMN flowGraph TO flowgraph;
ALTER TABLE page.flowhistory RENAME COLUMN fromNode  TO fromnode;
ALTER TABLE page.flowhistory RENAME COLUMN toNode    TO tonode;
ALTER TABLE page.flowhistory RENAME COLUMN dealTime  TO dealtime;
ALTER TABLE page.flowhistory RENAME COLUMN dealUser  TO dealuser;
ALTER TABLE page.flowhistory RENAME COLUMN userName  TO username;

-- flownode（3；X/Y/W/H 例外保持大写，不在本脚本范围）
ALTER TABLE page.flownode    RENAME COLUMN flowGraph TO flowgraph;
ALTER TABLE page.flownode    RENAME COLUMN roleList  TO rolelist;
ALTER TABLE page.flownode    RENAME COLUMN userList  TO userlist;

-- sysroleright（2）
ALTER TABLE page.sysroleright RENAME COLUMN roleCode TO rolecode;
ALTER TABLE page.sysroleright RENAME COLUMN rightCode TO rightcode;

-- sysroleuser（2）
ALTER TABLE page.sysroleuser RENAME COLUMN roleCode TO rolecode;
ALTER TABLE page.sysroleuser RENAME COLUMN userCode TO usercode;

-- taskprocess（8）
ALTER TABLE page.taskprocess RENAME COLUMN auditUser    TO audituser;
ALTER TABLE page.taskprocess RENAME COLUMN auditTime    TO audittime;
ALTER TABLE page.taskprocess RENAME COLUMN auditMessage TO auditmessage;
ALTER TABLE page.taskprocess RENAME COLUMN auditState   TO auditstate;
ALTER TABLE page.taskprocess RENAME COLUMN createUser   TO createuser;
ALTER TABLE page.taskprocess RENAME COLUMN createTime   TO createtime;
ALTER TABLE page.taskprocess RENAME COLUMN updateUser   TO updateuser;
ALTER TABLE page.taskprocess RENAME COLUMN updateTime   TO updatetime;

-- techstep（4）
ALTER TABLE page.techstep    RENAME COLUMN firstLevel  TO firstlevel;
ALTER TABLE page.techstep    RENAME COLUMN secondLevel TO secondlevel;
ALTER TABLE page.techstep    RENAME COLUMN stepName    TO stepname;
ALTER TABLE page.techstep    RENAME COLUMN isNeed      TO isneed;

-- transferorder（11）
ALTER TABLE page.transferorder RENAME COLUMN transferDate TO transferdate;
ALTER TABLE page.transferorder RENAME COLUMN materialCode TO materialcode;
ALTER TABLE page.transferorder RENAME COLUMN rollNum      TO rollnum;
ALTER TABLE page.transferorder RENAME COLUMN outProcess   TO outprocess;
ALTER TABLE page.transferorder RENAME COLUMN inProcess    TO inprocess;
ALTER TABLE page.transferorder RENAME COLUMN outRoom      TO outroom;
ALTER TABLE page.transferorder RENAME COLUMN inRoom       TO inroom;
ALTER TABLE page.transferorder RENAME COLUMN createUser   TO createuser;
ALTER TABLE page.transferorder RENAME COLUMN createTime   TO createtime;
ALTER TABLE page.transferorder RENAME COLUMN receiveUser  TO receiveuser;
ALTER TABLE page.transferorder RENAME COLUMN receiveTime  TO receivetime;

-- users（3）
ALTER TABLE page.users       RENAME COLUMN userName  TO username;
ALTER TABLE page.users       RENAME COLUMN roleName  TO rolename;
ALTER TABLE page.users       RENAME COLUMN createTime TO createtime;

-- workflow（4）
ALTER TABLE page.workflow    RENAME COLUMN targetCode TO targetcode;
ALTER TABLE page.workflow    RENAME COLUMN startTime  TO starttime;
ALTER TABLE page.workflow    RENAME COLUMN flowGraph  TO flowgraph;
ALTER TABLE page.workflow    RENAME COLUMN endTime    TO endtime;

-- ============================================================================
-- 执行后验证：应返回 0 行（ flownode 的 X/Y/W/H 四个例外除外，库中不应再有
-- 含大写字母的列名）
-- ============================================================================
SELECT TABLE_NAME, COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'page'
  AND CAST(COLUMN_NAME AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin REGEXP '[A-Z]'
  AND NOT (TABLE_NAME = 'flownode' AND COLUMN_NAME IN ('X', 'Y', 'W', 'H'));
