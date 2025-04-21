ALTER TABLE dw.payment DROP CONSTRAINT payment_pkey;
ALTER TABLE dw.payment DROP CONSTRAINT payment_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment
    ADD CONSTRAINT payment_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);