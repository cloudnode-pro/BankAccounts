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
    `instrument`  VARCHAR(24)                  DEFAULT NULL COLLATE BINARY
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

-- Modify `bank_transactions`.`instrument` to be `VARCHAR(24)`
CREATE TABLE `new_bank_transactions`
(
    `id`          INTEGER PRIMARY KEY NOT NULL,
    `from`        CHAR(16)            NOT NULL COLLATE BINARY,
    `to`          CHAR(16)            NOT NULL COLLATE BINARY,
    `amount`      DECIMAL(15, 2)      NOT NULL,
    `time`        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `description` TEXT                         DEFAULT NULL COLLATE NOCASE,
    `instrument`  VARCHAR(24)                  DEFAULT NULL COLLATE NOCASE
);

INSERT INTO `new_bank_transactions`
SELECT *
FROM `bank_transactions`;

DROP TABLE `bank_transactions`;

ALTER TABLE `new_bank_transactions`
    RENAME TO `bank_transactions`;
-- END OF `bank_transactions` MODIFICATION

CREATE TABLE IF NOT EXISTS `bank_invoices`
(
    `id`          CHAR(16) PRIMARY KEY NOT NULL COLLATE BINARY,
    `seller`      CHAR(16)             NOT NULL COLLATE BINARY,
    `amount`      DECIMAL(15, 2)       NOT NULL,
    `description` TEXT                          DEFAULT NULL COLLATE NOCASE,
    `buyer`       CHAR(36)                      DEFAULT NULL COLLATE NOCASE,
    `created`     DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `transaction` INTEGER                       DEFAULT NULL
);
