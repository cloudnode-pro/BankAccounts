CREATE TABLE IF NOT EXISTS `bank_accounts`
(
    `id`      CHAR(16) NOT NULL COLLATE BINARY,
    `owner`   CHAR(36) NOT NULL COLLATE BINARY,
    `type`    TINYINT  NOT NULL,
    `name`    TEXT              DEFAULT NULL COLLATE NOCASE,
    `balance` DECIMAL(15, 2),
    `frozen`  TINYINT  NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `bank_transactions`
(
    `id`          INT            NOT NULL,
    `from`        CHAR(16)       NOT NULL COLLATE BINARY,
    `to`          CHAR(16)       NOT NULL COLLATE BINARY,
    `amount`      DECIMAL(15, 2) NOT NULL,
    `time`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `description` TEXT                    DEFAULT NULL COLLATE NOCASE,
    `instrument`  CHAR(24)                DEFAULT NULL COLLATE BINARY,
    PRIMARY KEY (`id`)
);
