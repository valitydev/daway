ALTER TABLE dw.payment_session_info DROP CONSTRAINT payment_session_pk;
ALTER TABLE dw.payment_session_info DROP CONSTRAINT payment_session_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_session_info
    ADD CONSTRAINT payment_session_pk PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_session_info
    ADD CONSTRAINT payment_session_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);