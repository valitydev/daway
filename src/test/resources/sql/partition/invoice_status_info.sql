ALTER TABLE dw.invoice_status_info DROP CONSTRAINT invoice_status_pkey;
ALTER TABLE dw.invoice_status_info DROP CONSTRAINT invoice_status_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.invoice_status_info
    ADD CONSTRAINT invoice_status_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.invoice_status_info
    ADD CONSTRAINT invoice_status_uniq UNIQUE (invoice_id, sequence_id, change_id, event_created_at);