ALTER TABLE dw.invoice DROP CONSTRAINT invoice_pkey;
ALTER TABLE dw.invoice DROP CONSTRAINT invoice_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.invoice
    ADD CONSTRAINT invoice_uniq UNIQUE (invoice_id, sequence_id, change_id, event_created_at);