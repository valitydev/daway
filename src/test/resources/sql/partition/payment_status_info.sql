ALTER TABLE dw.payment_status_info DROP CONSTRAINT payment_status_pkey;
ALTER TABLE dw.payment_status_info DROP CONSTRAINT payment_status_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_status_info
    ADD CONSTRAINT payment_status_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_status_info
    ADD CONSTRAINT payment_status_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);