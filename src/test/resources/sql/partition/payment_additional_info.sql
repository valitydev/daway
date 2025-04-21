ALTER TABLE dw.payment_additional_info DROP CONSTRAINT payment_additional_info_pkey;
ALTER TABLE dw.payment_additional_info DROP CONSTRAINT payment_additional_info_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_additional_info
    ADD CONSTRAINT payment_additional_info_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_additional_info
    ADD CONSTRAINT payment_additional_info_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);