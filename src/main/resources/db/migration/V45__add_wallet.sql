CREATE TABLE dw.wallet
(
    id                     bigserial            NOT NULL,
    event_created_at       timestamp without time zone                                      NOT NULL,
    dominant_version_id    bigint               NOT NULL,
    wallet_id              character varying    NOT NULL,
    wallet_name            character varying    NOT NULL,
    party_id               character varying,
    currency_code          character varying,
    wtime                  timestamp without time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
    current                boolean DEFAULT true NOT NULL,
    payment_institution_id integer,
    blocked                boolean,
    active                 boolean,
    CONSTRAINT wallet_pkey PRIMARY KEY (id),
    CONSTRAINT wallet_uniq UNIQUE (wallet_id, dominant_version_id)
);

CREATE INDEX wallet_event_created_at_idx ON dw.wallet USING btree (event_created_at);
CREATE INDEX wallet_id_idx ON dw.wallet USING btree (wallet_id);