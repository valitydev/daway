DROP TABLE IF EXISTS dw.identity CASCADE;
DROP TABLE IF EXISTS dw.wallet CASCADE;
DROP TABLE IF EXISTS dw.challenge CASCADE;

ALTER TABLE dw.source DROP COLUMN IF EXISTS identity_id;
ALTER TABLE dw.destination DROP COLUMN IF EXISTS identity_id;
ALTER TABLE dw.source DROP COLUMN IF EXISTS accounter_account_id;
ALTER TABLE dw.destination DROP COLUMN IF EXISTS accounter_account_id;
ALTER TABLE dw.withdrawal_adjustment DROP COLUMN IF EXISTS party_revision;
