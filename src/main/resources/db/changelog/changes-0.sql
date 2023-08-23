CREATE TABLE account
(
    id         BIGSERIAL PRIMARY KEY,
    account_id VARCHAR    NOT NULL UNIQUE,
    balance    NUMERIC    NOT NULL DEFAULT 0,
    currency   varchar(3) NOT NULL,
    version    BIGINT     NOT NULL DEFAULT 0
);