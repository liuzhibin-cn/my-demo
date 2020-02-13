DROP DATABASE IF EXISTS `seata`;
CREATE SCHEMA `seata` DEFAULT CHARACTER SET utf8 ;
USE `seata`;

-- -------------------------------- The script used when storeMode is 'db' --------------------------------
-- the table to store GlobalSession data
DROP TABLE IF EXISTS global_table;
CREATE TABLE IF NOT EXISTS `global_table`
(
    `xid`                       VARCHAR(128) NOT NULL,
    `transaction_id`            BIGINT,
    `status`                    TINYINT      NOT NULL,
    `application_id`            VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name`          VARCHAR(128),
    `timeout`                   INT,
    `begin_time`                BIGINT,
    `application_data`          VARCHAR(2000),
    `gmt_create`                DATETIME,
    `gmt_modified`              DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_gmt_modified_status` (`gmt_modified`, `status`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS branch_table;
-- the table to store BranchSession data
CREATE TABLE IF NOT EXISTS `branch_table`
(
    `branch_id`         BIGINT       NOT NULL,
    `xid`               VARCHAR(128) NOT NULL,
    `transaction_id`    BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id`       VARCHAR(256),
    `branch_type`       VARCHAR(8),
    `status`            TINYINT,
    `client_id`         VARCHAR(64),
    `application_data`  VARCHAR(2000),
    `gmt_create`        DATETIME(6),
    `gmt_modified`      DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- the table to store lock data
DROP TABLE IF EXISTS lock_table;
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(96),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_branch_id` (`branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;



DROP TABLE IF EXISTS branch_table_his;
CREATE TABLE `branch_table_his` (
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(128) NOT NULL,
  `transaction_id` bigint(20) DEFAULT NULL,
  `resource_group_id` varchar(32) DEFAULT NULL,
  `resource_id` varchar(256) DEFAULT NULL,
  `branch_type` varchar(8) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `application_data` varchar(2000) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS global_table_his;
CREATE TABLE `global_table_his` (
  `xid` varchar(128) NOT NULL,
  `transaction_id` bigint(20) DEFAULT NULL,
  `status` tinyint(4) NOT NULL,
  `application_id` varchar(32) DEFAULT NULL,
  `transaction_service_group` varchar(32) DEFAULT NULL,
  `transaction_name` varchar(128) DEFAULT NULL,
  `timeout` int(11) DEFAULT NULL,
  `begin_time` bigint(20) DEFAULT NULL,
  `application_data` varchar(2000) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`xid`),
  KEY `idx_gmt_modified_status` (`gmt_modified`,`status`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS lock_table_his;
CREATE TABLE `lock_table_his` (
  `row_key` varchar(128) NOT NULL,
  `xid` varchar(96) DEFAULT NULL,
  `transaction_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) NOT NULL,
  `resource_id` varchar(256) DEFAULT NULL,
  `table_name` varchar(32) DEFAULT NULL,
  `pk` varchar(36) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modified` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  KEY `idx_branch_id` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TRIGGER IF EXISTS `branch_table_AFTER_INSERT`;
DELIMITER $$
CREATE TRIGGER `branch_table_AFTER_INSERT` AFTER INSERT ON `branch_table` FOR EACH ROW
BEGIN
	insert into branch_table_his (branch_id,xid,transaction_id,resource_group_id,resource_id,branch_type,status,client_id,application_data,gmt_create,gmt_modified)
	  values (new.branch_id,new.xid,new.transaction_id,new.resource_group_id,new.resource_id,new.branch_type,new.status,new.client_id,new.application_data,new.gmt_create,new.gmt_modified);
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `branch_table_AFTER_UPDATE`;
DELIMITER $$
CREATE TRIGGER `branch_table_AFTER_UPDATE` AFTER UPDATE ON `branch_table` FOR EACH ROW
BEGIN
	update branch_table_his set status=new.status, gmt_modified=new.gmt_modified where branch_id=new.branch_id;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `branch_table_AFTER_DELETE`;
DELIMITER $$
CREATE TRIGGER `branch_table_AFTER_DELETE` AFTER DELETE ON `branch_table` FOR EACH ROW
BEGIN
  update branch_table_his set gmt_delete=now() where branch_id=old.branch_id; 
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `global_table_AFTER_INSERT`;
DELIMITER $$
CREATE TRIGGER `global_table_AFTER_INSERT` AFTER INSERT ON `global_table` FOR EACH ROW
BEGIN
  insert into global_table_his(xid,transaction_id,status,application_id,transaction_service_group,transaction_name,timeout
	,begin_time,application_data,gmt_create,gmt_modified)
  values(new.xid,new.transaction_id,new.status,new.application_id,new.transaction_service_group,new.transaction_name,new.timeout
	,new.begin_time,new.application_data,new.gmt_create,new.gmt_modified); 
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `global_table_AFTER_UPDATE`;
DELIMITER $$
CREATE TRIGGER `global_table_AFTER_UPDATE` AFTER UPDATE ON `global_table` FOR EACH ROW
BEGIN
  update global_table_his 
    set status=new.status, timeout=new.timeout, gmt_modified=new.gmt_modified
    where xid=new.xid; 
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `global_table_AFTER_DELETE`;
DELIMITER $$
CREATE TRIGGER `global_table_AFTER_DELETE` AFTER DELETE ON `global_table` FOR EACH ROW
BEGIN
  update global_table_his set gmt_delete=now() where xid=old.xid; 
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `lock_table_AFTER_INSERT`;
DELIMITER $$
CREATE TRIGGER `lock_table_AFTER_INSERT` AFTER INSERT ON `lock_table` FOR EACH ROW
BEGIN 
  insert into lock_table_his(row_key,xid,transaction_id,branch_id,resource_id,table_name,pk,gmt_create,gmt_modified)
  values(new.row_key,new.xid,new.transaction_id,new.branch_id,new.resource_id,new.table_name,new.pk,new.gmt_create,new.gmt_modified);
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS `lock_table_AFTER_DELETE`;
DELIMITER $$
CREATE TRIGGER `lock_table_AFTER_DELETE` AFTER DELETE ON `lock_table` FOR EACH ROW
BEGIN
  update lock_table_his set gmt_delete=now()  
    where xid=old.xid and branch_id=old.branch_id and table_name=old.table_name and row_key=old.row_key;
END$$
DELIMITER ;