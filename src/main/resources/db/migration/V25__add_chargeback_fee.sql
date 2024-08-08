ALTER TABLE dw.chargeback
    ADD COLUMN IF NOT EXISTS chargeback_fee BIGINT;
ALTER TABLE dw.chargeback
    ADD COLUMN IF NOT EXISTS chargeback_provider_fee BIGINT;
ALTER TABLE dw.chargeback
    ADD COLUMN IF NOT EXISTS chargeback_external_fee BIGINT;