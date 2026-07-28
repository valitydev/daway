CREATE TABLE dw.withdrawal_cash_change
(
    id                bigserial                   NOT NULL,
    event_created_at  timestamp without time zone NOT NULL,
    withdrawal_id     character varying           NOT NULL,

    new_amount        bigint                      NOT NULL,
    new_currency_code character varying           NOT NULL,
    old_amount        bigint                      NOT NULL,
    old_currency_code character varying           NOT NULL,

    current           BOOLEAN                     NOT NULL DEFAULT false,
    wtime             timestamp without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc'::text),
    sequence_id       bigint,

    CONSTRAINT withdrawal_cash_change_pkey PRIMARY KEY (id),
    CONSTRAINT withdrawal_cash_change_uniq UNIQUE (withdrawal_id, sequence_id)
);
