ALTER TABLE dw.withdrawal DROP CONSTRAINT withdrawal_pkey;
ALTER TABLE dw.withdrawal DROP CONSTRAINT withdrawal_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.withdrawal
    ADD CONSTRAINT withdrawal_uniq UNIQUE (withdrawal_id, sequence_id, event_created_at);