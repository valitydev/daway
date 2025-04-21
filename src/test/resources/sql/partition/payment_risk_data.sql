ALTER TABLE dw.payment_risk_data DROP CONSTRAINT payment_risk_data_pkey;
ALTER TABLE dw.payment_risk_data DROP CONSTRAINT payment_risk_data_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_risk_data
    ADD CONSTRAINT payment_risk_data_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_risk_data
    ADD CONSTRAINT payment_risk_data_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);