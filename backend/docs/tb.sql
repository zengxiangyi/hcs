-- =============================================================
-- 建表脚本（MySQL 8.0+）
-- 数据库默认字符集 utf8mb4，排序规则 utf8mb4_general_ci
-- 引擎 InnoDB，主键自增
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sysUser
-- ----------------------------
DROP TABLE IF EXISTS sysUser;
CREATE TABLE sysUser (
  id         int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code       varchar(10)   DEFAULT NULL COMMENT '编号',
  name       varchar(20)   DEFAULT NULL COMMENT '名称',
  password   varchar(100)  DEFAULT NULL COMMENT '密码',
  remark     varchar(100)  DEFAULT NULL COMMENT '备注',
  email      varchar(100)  DEFAULT NULL COMMENT '邮箱',
  department varchar(30)   DEFAULT NULL COMMENT '部门',
  position   varchar(30)   DEFAULT NULL COMMENT '岗位',
  cellphone  varchar(15)   DEFAULT NULL COMMENT '手机号',
  state      varchar(10)   DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for sysRole
-- ----------------------------
DROP TABLE IF EXISTS sysRole;
CREATE TABLE sysRole (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code     varchar(20)   DEFAULT NULL COMMENT '编码',
  name     varchar(30)   DEFAULT NULL COMMENT '名称',
  category varchar(20)   DEFAULT NULL COMMENT '分类',
  remark   varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ----------------------------
-- Table structure for sysRight
-- ----------------------------
DROP TABLE IF EXISTS sysRight;
CREATE TABLE sysRight (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code     varchar(20)   DEFAULT NULL COMMENT '编码',
  name     varchar(50)   DEFAULT NULL COMMENT '名称',
  category varchar(20)   DEFAULT NULL COMMENT '分类',
  remark   varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- ----------------------------
-- Table structure for sysRoleUser
-- ----------------------------
DROP TABLE IF EXISTS sysRoleUser;
CREATE TABLE sysRoleUser (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  roleCode varchar(30)   DEFAULT NULL COMMENT '角色编码',
  userCode varchar(30)   DEFAULT NULL COMMENT '用户编码',
  remark   varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-用户关联表';

-- ----------------------------
-- Table structure for sysRoleRight
-- ----------------------------
DROP TABLE IF EXISTS sysRoleRight;
CREATE TABLE sysRoleRight (
  id        int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  roleCode  varchar(30)   DEFAULT NULL COMMENT '角色编码',
  rightCode varchar(30)   DEFAULT NULL COMMENT '权限编码',
  remark    varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-权限关联表';

-- ----------------------------
-- Table structure for workflow
-- ----------------------------
DROP TABLE IF EXISTS workflow;
CREATE TABLE workflow (
  id        int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code      varchar(30)   DEFAULT NULL COMMENT '编号',
  name      varchar(100)  DEFAULT NULL COMMENT '名称',
  state     varchar(30)   DEFAULT NULL COMMENT '状态',
  startTime datetime      DEFAULT NULL COMMENT '开始时间',
  endTime   datetime      DEFAULT NULL COMMENT '结束时间',
  remark    varchar(100)  DEFAULT NULL COMMENT '备注',
  category  varchar(100)  DEFAULT NULL COMMENT '分类',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程表';

-- ----------------------------
-- Table structure for flowHistory
-- ----------------------------
DROP TABLE IF EXISTS flowHistory;
CREATE TABLE flowHistory (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  workflow varchar(100)  DEFAULT NULL COMMENT '流程标记',
  opTime   varchar(100)  DEFAULT NULL COMMENT '操作时间',
  userCode varchar(100)  DEFAULT NULL COMMENT '操作人工号',
  userName varchar(100)  DEFAULT NULL COMMENT '操作人名称',
  remark   varchar(100)  DEFAULT NULL COMMENT '备注',
  action   varchar(100)  DEFAULT NULL COMMENT '动作',
  note     varchar(100)  DEFAULT NULL COMMENT '笔记',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程日志表';

-- ----------------------------
-- Table structure for flowNode
-- ----------------------------
DROP TABLE IF EXISTS flowNode;
CREATE TABLE flowNode (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code     varchar(30)   DEFAULT NULL COMMENT '编号',
  name     varchar(50)   DEFAULT NULL COMMENT '名称',
  category varchar(20)   DEFAULT NULL COMMENT '分类',
  shape    varchar(20)   DEFAULT NULL COMMENT '形状',
  axis     varchar(100)  DEFAULT NULL COMMENT '坐标轴',
  color    varchar(10)   DEFAULT NULL COMMENT '颜色',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程节点表';

-- ----------------------------
-- Table structure for flowEdge
-- ----------------------------
DROP TABLE IF EXISTS flowEdge;
CREATE TABLE flowEdge (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code     varchar(30)   DEFAULT NULL COMMENT '编号',
  name     varchar(100)  DEFAULT NULL COMMENT '名称',
  color    varchar(10)   DEFAULT NULL COMMENT '颜色',
  fromNode varchar(30)   DEFAULT NULL COMMENT '起点',
  toNode   varchar(30)   DEFAULT NULL COMMENT '终点',
  axis     varchar(100)  DEFAULT NULL COMMENT '坐标轴',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程连线表';

-- ----------------------------
-- Table structure for flowGraph
-- ----------------------------
DROP TABLE IF EXISTS flowGraph;
CREATE TABLE flowGraph (
  id        int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  workflow  varchar(20)   DEFAULT NULL COMMENT '流程标记',
  width     varchar(10)   DEFAULT NULL COMMENT '宽度',
  heght     varchar(10)   DEFAULT NULL COMMENT '高度',
  firstNode varchar(20)   DEFAULT NULL COMMENT '开始节点',
  remark    varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程图配置表';

-- ----------------------------
-- Table structure for constValue
-- ----------------------------
DROP TABLE IF EXISTS constValue;
CREATE TABLE constValue (
  id       int           NOT NULL AUTO_INCREMENT COMMENT '主键',
  code     varchar(10)   DEFAULT NULL COMMENT '编码',
  name     varchar(30)   DEFAULT NULL COMMENT '名称',
  category varchar(20)   DEFAULT NULL COMMENT '分类',
  mark     varchar(100)  DEFAULT NULL COMMENT '标记',
  remark   varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常量值表';

SET FOREIGN_KEY_CHECKS = 1;
