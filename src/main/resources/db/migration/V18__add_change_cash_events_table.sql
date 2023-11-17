CREATE TABLE dw.payment_cash_change
(
    id                bigserial                   NOT NULL,
    event_created_at  timestamp without time zone NOT NULL,
    invoice_id        character varying           NOT NULL,
    payment_id        character varying           NOT NULL,

    new_amount        bigint                      NOT NULL,
    new_currency_code character varying           NOT NULL,
    old_amount        bigint                      NOT NULL,
    old_currency_code character varying           NOT NULL,

    current           BOOLEAN                     NOT NULL DEFAULT false,
    wtime             timestamp without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc'::text),
    sequence_id       bigint,
    change_id         integer,
    CONSTRAINT payment_cash_change_pkey PRIMARY KEY (id),
    CONSTRAINT payment_cash_change_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id)
);