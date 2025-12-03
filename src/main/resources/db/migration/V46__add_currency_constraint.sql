ALTER TABLE dw.currency
    ADD CONSTRAINT IF NOT EXIST currency_ref_version_uidx
        UNIQUE (currency_ref_id, version_id);