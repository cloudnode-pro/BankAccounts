CREATE TABLE IF NOT EXISTS `bank_accounts`
(
    `id`      CHAR(16) PRIMARY KEY NOT NULL COLLATE BINARY,
    `owner`   CHAR(36)             NOT NULL COLLATE BINARY,
    `type`    TINYINT              NOT NULL,
    `name`    TEXT                          DEFAULT NULL COLLATE NOCASE,
    `balance` DECIMAL(15, 2),
    `frozen`  TINYINT              NOT NULL DEFAULT '0'
);

CREATE TABLE IF NOT EXISTS `bank_transactions`
(
    `id`          INTEGER PRIMARY KEY NOT NULL,
    `from`        CHAR(16)            NOT NULL COLLATE BINARY,
    `to`          CHAR(16)            NOT NULL COLLATE BINARY,
    `amount`      DECIMAL(15, 2)      NOT NULL,
    `time`        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `description` TEXT                         DEFAULT NULL COLLATE NOCASE,
    `instrument`  CHAR(24)                     DEFAULT NULL COLLATE BINARY
);

CREATE TABLE IF NOT EXISTS `pos`
(
    `x`           INTEGER  NOT NULL,
    `y`           INTEGER  NOT NULL,
    `z`           INTEGER  NOT NULL,
    `world`       TEXT     NOT NULL,
    `price`       NUMERIC  NOT NULL,
    `description` TEXT              DEFAULT NULL,
    `seller`      TEXT     NOT NULL,
    `created`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`x`, `y`, `z`, `world`)
);

ALTER TABLE `bank_transactions` DROP COLUMN `instrument`; -- column has never been in use before
ALTER TABLE `bank_transactions` ADD COLUMN `instrument` VARCHAR(24) DEFAULT NULL COLLATE BINARY;
