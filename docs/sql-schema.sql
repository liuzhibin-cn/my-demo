-- ====================================================================
-- mydemo-dn0
-- ====================================================================
CREATE SCHEMA `mydemo-dn0` DEFAULT CHARACTER SET utf8 ;
USE `mydemo-dn0`;

DROP TABLE IF EXISTS MYCAT_SEQUENCE;
CREATE TABLE MYCAT_SEQUENCE (name VARCHAR(64) NOT NULL,  current_value BIGINT(20) NOT NULL,  increment INT NOT NULL DEFAULT 1, PRIMARY KEY (name) ) ENGINE=InnoDB;
-- ----------------------------
-- Function structure for `mycat_seq_currval`
-- ----------------------------
DROP FUNCTION IF EXISTS `mycat_seq_currval`;
DELIMITER ;;
CREATE FUNCTION `mycat_seq_currval`(seq_name VARCHAR(64)) RETURNS varchar(64) CHARSET latin1
    DETERMINISTIC
BEGIN
    DECLARE retval VARCHAR(64);
    SET retval="-1,0";
    SELECT concat(CAST(current_value AS CHAR),",",CAST(increment AS CHAR) ) INTO retval FROM mycat_sequence  WHERE name = seq_name;
    RETURN retval ;
END
;;
DELIMITER ;
-- ----------------------------
-- Function structure for `mycat_seq_nextval`
-- ----------------------------
DROP FUNCTION IF EXISTS `mycat_seq_nextval`;
DELIMITER ;;
CREATE FUNCTION `mycat_seq_nextval`(seq_name VARCHAR(64)) RETURNS varchar(64) CHARSET latin1
    DETERMINISTIC
BEGIN
    DECLARE retval VARCHAR(64);
    DECLARE val BIGINT;
    DECLARE inc INT;
    DECLARE seq_lock INT;
    set val = -1;
    set inc = 0;
    SET seq_lock = -1;
    SELECT GET_LOCK(seq_name, 15) into seq_lock;
    if seq_lock = 1 then
      SELECT current_value + increment, increment INTO val, inc FROM mycat_sequence WHERE name = seq_name for update;
      if val != -1 then
          UPDATE mycat_sequence SET current_value = val WHERE name = seq_name;
      end if;
      SELECT RELEASE_LOCK(seq_name) into seq_lock;
    end if;
    SELECT concat(CAST((val - inc + 1) as CHAR),",",CAST(inc as CHAR)) INTO retval;
    RETURN retval;
END
;;
DELIMITER ;
-- ----------------------------
-- Function structure for `mycat_seq_setvals`
-- ----------------------------
DROP FUNCTION IF EXISTS `mycat_seq_nextvals`;
DELIMITER ;;
CREATE FUNCTION `mycat_seq_nextvals`(seq_name VARCHAR(64), count INT) RETURNS VARCHAR(64) CHARSET latin1
    DETERMINISTIC
BEGIN
    DECLARE retval VARCHAR(64);
    DECLARE val BIGINT;
    DECLARE seq_lock INT;
    SET val = -1;
    SET seq_lock = -1;
    SELECT GET_LOCK(seq_name, 15) into seq_lock;
    if seq_lock = 1 then
        SELECT current_value + count INTO val FROM mycat_sequence WHERE name = seq_name for update;
        IF val != -1 THEN
            UPDATE mycat_sequence SET current_value = val WHERE name = seq_name;
        END IF;
        SELECT RELEASE_LOCK(seq_name) into seq_lock;
    end if;
    SELECT CONCAT(CAST((val - count + 1) as CHAR), ",", CAST(val as CHAR)) INTO retval;
    RETURN retval;
END
;;
DELIMITER ;
-- ----------------------------
-- Function structure for `mycat_seq_setval`
-- ----------------------------
DROP FUNCTION IF EXISTS `mycat_seq_setval`;
DELIMITER ;;
CREATE FUNCTION `mycat_seq_setval`(seq_name VARCHAR(64), value BIGINT) RETURNS varchar(64) CHARSET latin1
    DETERMINISTIC
BEGIN
    DECLARE retval VARCHAR(64);
    DECLARE inc INT;
    SET inc = 0;
    SELECT increment INTO inc FROM mycat_sequence WHERE name = seq_name;
    UPDATE mycat_sequence SET current_value = value WHERE name = seq_name;
    SELECT concat(CAST(value as CHAR),",",CAST(inc as CHAR)) INTO retval;
    RETURN retval;
END
;;
DELIMITER ;
INSERT INTO MYCAT_SEQUENCE(name,current_value,increment) VALUES ('GLOBAL', 100000, 1);
INSERT INTO MYCAT_SEQUENCE(name,current_value,increment) VALUES ('ORD_ORDER_ITEM', 1000000, 20);
INSERT INTO MYCAT_SEQUENCE(name,current_value,increment) VALUES ('ORD_USER_ORDER', 9965738, 20);
INSERT INTO MYCAT_SEQUENCE(name,current_value,increment) VALUES ('USR_USER', 2906300, 20);

-- Seata回滚表
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    `branch_id`     BIGINT(20)   NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME     NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME     NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';

-- ====================================================================
-- mydemo-dn1: ord_order, ord_order_item, user, usr_user_account, ord_user_order
-- ====================================================================
DROP DATABASE IF EXISTS `mydemo-dn1`;
CREATE SCHEMA `mydemo-dn1` DEFAULT CHARACTER SET utf8 ;
USE `mydemo-dn1`;
DROP TABLE IF EXISTS `ord_order`;
CREATE TABLE `ord_order` (
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '订单状态',
  `total` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `payment` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '付款金额',
  `pay_time` DATETIME NOT NULL DEFAULT '1900-01-01' COMMENT '付款时间',
  `pay_status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '付款状态',
  `contact` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '电话',
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT '详细地址',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '下单时间',
  `last_update` TIMESTAMP NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单主表';
DROP TABLE IF EXISTS `ord_order_item`;
CREATE TABLE `ord_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `item_id` INT NOT NULL DEFAULT 0 COMMENT '产品ID',
  `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '产品标题',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_item_id`),
  INDEX `ix_order_id` (`order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单明细';
DROP TABLE IF EXISTS `ord_user_order`;
CREATE TABLE `ord_user_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '无意义主键，目前seata不支持组合主键',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_uid_oid` (`user_id` ASC, `order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员-订单异构索引表';
DROP TABLE IF EXISTS `usr_user`;
CREATE TABLE `usr_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `nickname` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '昵称',
  `mobile` VARCHAR(11) NOT NULL DEFAULT '' COMMENT '邮箱',
  `email` VARCHAR(40) NOT NULL DEFAULT '' COMMENT '手机号',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '注册时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员主表';
DROP TABLE IF EXISTS `usr_user_account`;
CREATE TABLE `usr_user_account` (
  `account` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '登录账号',
  `password` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '密码',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `account_hash` int not null default 0 comment 'account的hashcode，分片字段',
  PRIMARY KEY (`account`),
  INDEX ix_account_hash (account_hash asc),
  INDEX ix_user_id (user_id asc)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员登录账号';

-- ====================================================================
-- mydemo-dn2: ord_order, ord_order_item, user, usr_user_account, ord_user_order
-- ====================================================================
DROP DATABASE IF EXISTS `mydemo-dn2`;
CREATE SCHEMA `mydemo-dn2` DEFAULT CHARACTER SET utf8 ;
USE `mydemo-dn2`;
DROP TABLE IF EXISTS `ord_order`;
CREATE TABLE `ord_order` (
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '订单状态',
  `total` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `payment` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '付款金额',
  `pay_time` DATETIME NOT NULL DEFAULT '1900-01-01' COMMENT '付款时间',
  `pay_status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '付款状态',
  `contact` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '电话',
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT '详细地址',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '下单时间',
  `last_update` TIMESTAMP NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单主表';
DROP TABLE IF EXISTS `ord_order_item`;
CREATE TABLE `ord_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `item_id` INT NOT NULL DEFAULT 0 COMMENT '产品ID',
  `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '产品标题',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_item_id`),
  INDEX `ix_order_id` (`order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单明细';
DROP TABLE IF EXISTS `ord_user_order`;
CREATE TABLE `ord_user_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '无意义主键，目前seata不支持组合主键',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_uid_oid` (`user_id` ASC, `order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员-订单异构索引表';
DROP TABLE IF EXISTS `usr_user`;
CREATE TABLE `usr_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `nickname` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '昵称',
  `mobile` VARCHAR(11) NOT NULL DEFAULT '' COMMENT '邮箱',
  `email` VARCHAR(40) NOT NULL DEFAULT '' COMMENT '手机号',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '注册时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员主表';
DROP TABLE IF EXISTS `usr_user_account`;
CREATE TABLE `usr_user_account` (
  `account` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '登录账号',
  `password` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '密码',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `account_hash` int not null default 0 comment 'account的hashcode，分片字段',
  PRIMARY KEY (`account`),
  INDEX ix_account_hash (account_hash asc),
  INDEX ix_user_id (user_id asc)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员登录账号';

-- ====================================================================
-- mydemo-dn3: ord_order, ord_order_item, ord_user_order
-- ====================================================================
DROP DATABASE IF EXISTS `mydemo-dn3`;
CREATE SCHEMA `mydemo-dn3` DEFAULT CHARACTER SET utf8 ;
USE `mydemo-dn3`;
DROP TABLE IF EXISTS `ord_order`;
CREATE TABLE `ord_order` (
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '订单状态',
  `total` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `payment` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '付款金额',
  `pay_time` DATETIME NOT NULL DEFAULT '1900-01-01' COMMENT '付款时间',
  `pay_status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '付款状态',
  `contact` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '电话',
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT '详细地址',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '下单时间',
  `last_update` TIMESTAMP NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单主表';
DROP TABLE IF EXISTS `ord_order_item`;
CREATE TABLE `ord_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `item_id` INT NOT NULL DEFAULT 0 COMMENT '产品ID',
  `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '产品标题',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_item_id`),
  INDEX `ix_order_id` (`order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单明细';
DROP TABLE IF EXISTS `ord_user_order`;
CREATE TABLE `ord_user_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '无意义主键，目前seata不支持组合主键',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_uid_oid` (`user_id` ASC, `order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员-订单异构索引表';

-- ====================================================================
-- mydemo-dn4: ord_order, ord_order_item, ord_user_order
-- ====================================================================
DROP DATABASE IF EXISTS `mydemo-dn4`;
CREATE SCHEMA `mydemo-dn4` DEFAULT CHARACTER SET utf8 ;
USE `mydemo-dn4`;
DROP TABLE IF EXISTS `ord_order`;
CREATE TABLE `ord_order` (
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '订单状态',
  `total` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `payment` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '付款金额',
  `pay_time` DATETIME NOT NULL DEFAULT '1900-01-01' COMMENT '付款时间',
  `pay_status` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '付款状态',
  `contact` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '电话',
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT '详细地址',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '下单时间',
  `last_update` TIMESTAMP NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单主表';
DROP TABLE IF EXISTS `ord_order_item`;
CREATE TABLE `ord_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  `item_id` INT NOT NULL DEFAULT 0 COMMENT '产品ID',
  `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '产品标题',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `last_update` DATETIME NOT NULL DEFAULT current_timestamp on update current_timestamp COMMENT '最后更新时间',
  PRIMARY KEY (`order_item_id`),
  INDEX `ix_order_id` (`order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '订单明细';
DROP TABLE IF EXISTS `ord_user_order`;
CREATE TABLE `ord_user_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '无意义主键，目前seata不支持组合主键',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '会员ID',
  `order_id` BIGINT NOT NULL DEFAULT 0 COMMENT '订单ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_uid_oid` (`user_id` ASC, `order_id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci COMMENT = '会员-订单异构索引表';