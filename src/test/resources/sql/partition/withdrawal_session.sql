ALTER TABLE dw.withdrawal_session DROP CONSTRAINT withdrawal_session_pk;
ALTER TABLE dw.withdrawal_session DROP CONSTRAINT withdrawal_session_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.withdrawal_session
    ADD CONSTRAINT withdrawal_session_pk PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.withdrawal_session
    ADD CONSTRAINT withdrawal_session_uniq UNIQUE (withdrawal_session_id, sequence_id, event_created_at);