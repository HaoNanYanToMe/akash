/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50717
 Source Host           : localhost:3852
 Source Schema         : akash

 Target Server Type    : MySQL
 Target Server Version : 50717
 File Encoding         : 65001

 Date: 18/11/2019 17:54:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cr_engine
-- ----------------------------
DROP TABLE IF EXISTS `cr_engine`;
CREATE TABLE `cr_engine`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据引擎仓库主键编号',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据引擎仓库Code值',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据引擎仓库名称',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据引擎仓库功能简介',
  `state` int(11) NULL DEFAULT NULL COMMENT '数据引擎启用状态（0-启用/1-禁用/2-审核中）',
  `executeVail` int(11) NULL DEFAULT NULL COMMENT '数据执行验证状态（0-未通过逻辑验证/1-已通过）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '核心：数据引擎仓库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cr_engineexecute
-- ----------------------------
DROP TABLE IF EXISTS `cr_engineexecute`;
CREATE TABLE `cr_engineexecute`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据引擎流程核心主键编号',
  `eid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据引擎编号',
  `executeTag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据引擎执行引导标识',
  `executeData` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '待执行数据参数',
  `isChild` int(1) NULL DEFAULT NULL COMMENT '当前环节是否为嵌套子查询（0-否/1-是）',
  `sorts` int(11) NULL DEFAULT NULL COMMENT '数据引擎流程环节序列号',
  `state` int(1) NULL DEFAULT NULL COMMENT '当前环节是否启用（0-未启用/1-已启用）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '核心：数据引擎执行' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cr_field
-- ----------------------------
DROP TABLE IF EXISTS `cr_field`;
CREATE TABLE `cr_field`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '核心字段表主键',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心字段Code值',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心字段名称',
  `tid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联核心表主键编号',
  `sorts` int(11) NULL DEFAULT NULL COMMENT '核心字段序列',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心字段类型',
  `size` double NULL DEFAULT NULL COMMENT '核心字段长度',
  `state` int(11) NULL DEFAULT NULL COMMENT '核心字段状态（0-禁用/1-启用）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '核心：数据字段信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cr_logger
-- ----------------------------
DROP TABLE IF EXISTS `cr_logger`;
CREATE TABLE `cr_logger`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表信息变更日志主键',
  `type` int(11) NULL DEFAULT NULL COMMENT '变更类型（0-表/1-字段）',
  `tid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更的表主键编号',
  `executorId` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行变更的用户编号',
  `reson` int(11) NULL DEFAULT NULL COMMENT '变更原因（0-新增/1-移除/2-更新）',
  `sourceDataId` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '源数据编号（数据不会被直接删除，而是会留存）',
  `updateTime` datetime(0) NULL DEFAULT NULL COMMENT '数据更新时间',
  `dataId` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行变更的数据编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '核心：系统日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cr_tables
-- ----------------------------
DROP TABLE IF EXISTS `cr_tables`;
CREATE TABLE `cr_tables`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '核心表主键',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心表Code值',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心表名称（中文）',
  `note` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核心表备注信息',
  `state` int(11) NULL DEFAULT NULL COMMENT '核心表状态（0-已失效/1-正常）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '核心：数据表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
