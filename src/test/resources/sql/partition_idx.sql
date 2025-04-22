ALTER TABLE dw.invoice DROP CONSTRAINT invoice_pkey;
ALTER TABLE dw.invoice DROP CONSTRAINT invoice_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.invoice
    ADD CONSTRAINT invoice_uniq UNIQUE (invoice_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.invoice_status_info DROP CONSTRAINT invoice_status_pkey;
ALTER TABLE dw.invoice_status_info DROP CONSTRAINT invoice_status_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.invoice_status_info
    ADD CONSTRAINT invoice_status_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.invoice_status_info
    ADD CONSTRAINT invoice_status_uniq UNIQUE (invoice_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment DROP CONSTRAINT payment_pkey;
ALTER TABLE dw.payment DROP CONSTRAINT payment_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment
    ADD CONSTRAINT payment_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_additional_info DROP CONSTRAINT payment_additional_info_pkey;
ALTER TABLE dw.payment_additional_info DROP CONSTRAINT payment_additional_info_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_additional_info
    ADD CONSTRAINT payment_additional_info_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_additional_info
    ADD CONSTRAINT payment_additional_info_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_fee DROP CONSTRAINT payment_fee_pkey;
ALTER TABLE dw.payment_fee DROP CONSTRAINT payment_fee_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_fee
    ADD CONSTRAINT payment_fee_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_fee
    ADD CONSTRAINT payment_fee_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_payer_info DROP CONSTRAINT payment_payment_payer_info_pkey;
ALTER TABLE dw.payment_payer_info DROP CONSTRAINT payment_payment_payer_info_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_payer_info
    ADD CONSTRAINT payment_payment_payer_info_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_payer_info
    ADD CONSTRAINT payment_payment_payer_info_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_risk_data DROP CONSTRAINT payment_risk_data_pkey;
ALTER TABLE dw.payment_risk_data DROP CONSTRAINT payment_risk_data_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_risk_data
    ADD CONSTRAINT payment_risk_data_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_risk_data
    ADD CONSTRAINT payment_risk_data_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_route DROP CONSTRAINT payment_route_pkey;
ALTER TABLE dw.payment_route DROP CONSTRAINT payment_route_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_route
    ADD CONSTRAINT payment_route_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_route
    ADD CONSTRAINT payment_route_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_session_info DROP CONSTRAINT payment_session_pk;
ALTER TABLE dw.payment_session_info DROP CONSTRAINT payment_session_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_session_info
    ADD CONSTRAINT payment_session_pk PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_session_info
    ADD CONSTRAINT payment_session_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.payment_status_info DROP CONSTRAINT payment_status_pkey;
ALTER TABLE dw.payment_status_info DROP CONSTRAINT payment_status_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.payment_status_info
    ADD CONSTRAINT payment_status_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.payment_status_info
    ADD CONSTRAINT payment_status_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, event_created_at);


ALTER TABLE dw.withdrawal DROP CONSTRAINT withdrawal_pkey;
ALTER TABLE dw.withdrawal DROP CONSTRAINT withdrawal_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.withdrawal
    ADD CONSTRAINT withdrawal_uniq UNIQUE (withdrawal_id, sequence_id, event_created_at);


ALTER TABLE dw.withdrawal_session DROP CONSTRAINT withdrawal_session_pk;
ALTER TABLE dw.withdrawal_session DROP CONSTRAINT withdrawal_session_uniq;
--- Создаем новые индексы с полем партиционирования
ALTER TABLE dw.withdrawal_session
    ADD CONSTRAINT withdrawal_session_pk PRIMARY KEY (id, event_created_at);
ALTER TABLE dw.withdrawal_session
    ADD CONSTRAINT withdrawal_session_uniq UNIQUE (withdrawal_session_id, sequence_id, event_created_at);
