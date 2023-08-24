CREATE TABLE transaction
(
    id             BIGSERIAL PRIMARY KEY,
    from_id        BIGSERIAL NOT NULL,
    to_id          BIGSERIAL NOT NULL,
    FOREIGN KEY (from_id) REFERENCES account (id),
    FOREIGN KEY (to_id) REFERENCES account (id),
    transaction_id VARCHAR    NOT NULL UNIQUE,
    amount         NUMERIC    NOT NULL DEFAULT 0,
    currency       varchar(3) NOT NULL

)