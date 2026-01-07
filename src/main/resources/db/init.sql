CREATE TABLE bank_meta
(
    name VARCHAR(255) PRIMARY KEY NOT NULL,
    val VARCHAR(255)             NOT NULL
);

INSERT INTO bank_meta (name, val)
VALUES ('version', 0);

CREATE TABLE bank_accounts
(
    id             VARCHAR(32) NOT NULL,
    type           TINYINT     NOT NULL,
    allow_negative BOOLEAN     NOT NULL,
    status         TINYINT     NOT NULL,
    name           VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE bank_ledger_entries
(
    id              VARCHAR(32)    NOT NULL,
    account         VARCHAR(32)    NOT NULL,
    amount          DECIMAL(20, 2) NOT NULL,
    balance         DECIMAL(20, 2) NOT NULL,
    created         TIMESTAMP      NOT NULL,
    initiator       TINYINT        NOT NULL,
    channel         VARCHAR(255)   NOT NULL,
    description     VARCHAR(255),
    related_account VARCHAR(32),
    previous_id     VARCHAR(32),
    PRIMARY KEY (id),
    FOREIGN KEY (account) REFERENCES bank_accounts (id) ON DELETE CASCADE,
    FOREIGN KEY (related_account) REFERENCES bank_accounts (id) ON DELETE SET NULL,
    FOREIGN KEY (previous_id) REFERENCES bank_ledger_entries (id),
    UNIQUE (previous_id)
);

CREATE INDEX idx_bank_ledger_entries_account_created_desc ON bank_ledger_entries (account, created DESC);
