CREATE TABLE IF NOT EXISTS `bank_accounts` (
     `id` CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
     `owner` CHAR(36) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
     `type` TINYINT NOT NULL COMMENT '0: personal, 1: business',
     `name` VARCHAR(24) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,
     `balance` DECIMAL(15, 2),
     `frozen` TINYINT NOT NULL DEFAULT '0' COMMENT '0 (false) or 1 (true)',
     PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `bank_transactions` (
     `id` INT NOT NULL AUTO_INCREMENT,
     `from` CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
     `to` CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
     `amount` DECIMAL(15, 2) NOT NULL,
     `time` DATETIME NOT NULL DEFAULT UTC_TIMESTAMP(),
     `description` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci DEFAULT NULL,
     `instrument` CHAR(24) CHARACTER SET latin1 COLLATE latin1_general_cs DEFAULT NULL,
     PRIMARY KEY (`id`)
);