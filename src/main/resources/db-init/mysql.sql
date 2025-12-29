CREATE TABLE IF NOT EXISTS `bank_accounts`
(
    `id`      CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    `owner`   CHAR(36) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
    `type`    TINYINT                                                 NOT NULL COMMENT '0: personal, 1: business',
    `name`    VARCHAR(24) CHARACTER SET latin1 COLLATE latin1_general_ci       DEFAULT NULL,
    `balance` DECIMAL(15, 2),
    `frozen`  TINYINT                                                 NOT NULL DEFAULT '0' COMMENT '0 (false) or 1 (true)',
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `bank_transactions`
(
    `id`          INT                                                     NOT NULL AUTO_INCREMENT,
    `from`        CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    `to`          CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    `amount`      DECIMAL(15, 2)                                          NOT NULL,
    `time`        DATETIME                                                NOT NULL DEFAULT UTC_TIMESTAMP(),
    `description` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci       DEFAULT NULL,
    `instrument`  CHAR(24) CHARACTER SET latin1 COLLATE latin1_general_cs          DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `pos`
(
    `x`           INT                                                        NOT NULL,
    `y`           INT                                                        NOT NULL,
    `z`           INT                                                        NOT NULL,
    `world`       VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    `price`       DECIMAL(15, 2)                                             NOT NULL,
    `description` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci          DEFAULT NULL,
    `seller`      CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs    NOT NULL,
    `created`     DATETIME                                                   NOT NULL DEFAULT UTC_TIMESTAMP(),
    KEY `location` (`x`, `y`, `z`, `world`) USING BTREE
);

ALTER TABLE `bank_transactions`
    CHANGE COLUMN `instrument` `instrument` VARCHAR(24) CHARACTER SET latin1 COLLATE latin1_general_ci NULL DEFAULT NULL AFTER `description`;

CREATE TABLE IF NOT EXISTS `bank_invoices`
(
    `id`          CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs PRIMARY KEY NOT NULL,
    `seller`      CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs             NOT NULL,
    `amount`      DECIMAL(15, 2)                                                      NOT NULL,
    `description` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci                   DEFAULT NULL,
    `buyer`       CHAR(36) CHARACTER SET latin1 COLLATE latin1_general_ci                      DEFAULT NULL,
    `created`     DATETIME                                                            NOT NULL DEFAULT UTC_TIMESTAMP(),
    `transaction` INT                                                                          DEFAULT NULL
);

DELETE
from `pos`
WHERE `world` NOT LIKE '%-%-%-%-%';

ALTER TABLE `pos`
    CHANGE COLUMN `world` `world` CHAR(36) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL;

ALTER TABLE `bank_accounts`
    CHANGE COLUMN `name` `name` VARCHAR(24) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL;

ALTER TABLE `bank_transactions`
    CHANGE COLUMN `description` `description` VARCHAR(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL;

ALTER TABLE `pos`
    CHANGE COLUMN `description` `description` VARCHAR(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL;

ALTER TABLE `bank_invoices`
    CHANGE COLUMN `description` `description` VARCHAR(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL;
    
CREATE TABLE IF NOT EXISTS `change_owner_requests`
(
    `account`   CHAR(16) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
    `new_owner` CHAR(36) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
    `created`   DATETIME NOT NULL DEFAULT UTC_TIMESTAMP(),
    KEY `id` (`account`, `new_owner`)
);
