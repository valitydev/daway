ALTER TABLE dw.shop DROP CONSTRAINT IF EXISTS shop_uniq;
ALTER TABLE dw.party DROP CONSTRAINT IF EXISTS party_uniq;

ALTER TABLE dw.shop DROP COLUMN IF EXISTS sequence_id;
ALTER TABLE dw.shop DROP COLUMN IF EXISTS change_id;
ALTER TABLE dw.shop DROP COLUMN IF EXISTS claim_effect_id;

ALTER TABLE dw.party DROP COLUMN IF EXISTS sequence_id;
ALTER TABLE dw.party DROP COLUMN IF EXISTS change_id;
ALTER TABLE dw.party DROP COLUMN IF EXISTS claim_effect_id;
ALTER TABLE dw.party DROP COLUMN IF EXISTS revision;

ALTER TABLE dw.shop ADD COLUMN IF NOT EXISTS dominant_version_id bigint;
ALTER TABLE dw.party ADD COLUMN IF NOT EXISTS dominant_version_id bigint;

ALTER TABLE dw.shop
    ADD CONSTRAINT shop_uniq
        UNIQUE (party_id, shop_id, dominant_version_id);

ALTER TABLE dw.party
    ADD CONSTRAINT party_uniq
        UNIQUE (party_id, dominant_version_id);

DROP TABLE IF EXISTS dw.shop_revision CASCADE;
DROP TABLE IF EXISTS dw.contractor_revision CASCADE;
DROP TABLE IF EXISTS dw.withdrawal_provider CASCADE;
DROP TABLE IF EXISTS dw.calendar CASCADE;

ALTER TABLE dw.provider DROP COLUMN IF EXISTS identity;
