CREATE TYPE dw.withdrawal_validation_type AS ENUM (
    'receiver',
    'sender'
    );

CREATE TYPE dw.withdrawal_validation_status AS ENUM (
    'valid',
    'invalid'
    );

CREATE TABLE dw.withdrawal_validation
(
    id                  bigserial                       NOT NULL,
    event_created_at    timestamp without time zone NOT NULL,
    event_occured_at    timestamp without time zone NOT NULL,
    sequence_id         bigint                          NOT NULL,
    validation_id       character varying               NOT NULL,
    type                dw.withdrawal_validation_type   NOT NULL,
    personal_data_token character varying               NOT NULL,
    status              dw.withdrawal_validation_status NOT NULL,
    withdrawal_id       character varying               NOT NULL,
    wtime               timestamp without time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
    current             boolean DEFAULT true            NOT NULL,
    CONSTRAINT withdrawal_validation_pkey PRIMARY KEY (id),
    CONSTRAINT withdrawal_validation_uniq UNIQUE (validation_id, sequence_id)
);

ALTER TABLE dw.withdrawal
    ADD COLUMN IF NOT EXISTS exchange_rate real
;