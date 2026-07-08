CREATE TABLE IF NOT EXISTS dw.payment_exchange_context
(
    id                              bigserial                   NOT NULL,
    event_created_at                timestamp without time zone NOT NULL,
    invoice_id                      character varying           NOT NULL,
    payment_id                      character varying           NOT NULL,
    source_currency_code            character varying           NOT NULL,
    destination_currency_code       character varying           NOT NULL,
    exchange_rate_rational_p        bigint                      NOT NULL,
    exchange_rate_rational_q        bigint                      NOT NULL,
    current                         boolean                     NOT NULL DEFAULT false,
    wtime                           timestamp without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc'::text),
    sequence_id                     bigint,
    change_id                       integer,

    CONSTRAINT payment_exchange_context_pkey PRIMARY KEY (id),
    CONSTRAINT payment_exchange_context_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id)
);

ALTER TABLE dw.cash_flow
    ADD COLUMN IF NOT EXISTS exchange_source_currency_code character varying,
    ADD COLUMN IF NOT EXISTS exchange_destination_currency_code character varying,
    ADD COLUMN IF NOT EXISTS exchange_rate_rational_p bigint,
    ADD COLUMN IF NOT EXISTS exchange_rate_rational_q bigint;
