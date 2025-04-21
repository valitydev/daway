ALTER TABLE dw.payment_fee DROP CONSTRAINT payment_fee_pkey;
ALTER TABLE dw.payment_fee DROP CONSTRAINT payment_fee_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_fee
    ADD CONSTRAINT payment_fee_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_fee
    ADD CONSTRAINT payment_fee_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);