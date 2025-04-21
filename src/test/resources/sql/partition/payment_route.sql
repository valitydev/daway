ALTER TABLE dw.payment_route DROP CONSTRAINT payment_route_pkey;
ALTER TABLE dw.payment_route DROP CONSTRAINT payment_route_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_route
    ADD CONSTRAINT payment_route_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_route
    ADD CONSTRAINT payment_route_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);