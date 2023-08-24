INSERT INTO account(account_id, balance, currency)
VALUES ('uuid_1', 100, 'EUR');
INSERT INTO account(account_id, balance, currency)
VALUES ('uuid_2', 200, 'EUR');


INSERT INTO transaction(from_id, to_id, transaction_id, amount, currency)
VALUES ((SELECT id FROM account WHERE account_id = 'uuid_1'),
        (SELECT id FROM account WHERE account_id = 'uuid_2'),
        'test-transaction', 100, 'EUR');