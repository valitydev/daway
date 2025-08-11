DROP TABLE IF EXISTS dw.contract CASCADE;
DROP TABLE IF EXISTS dw.contractor CASCADE;
DROP TABLE IF EXISTS dw.contract_adjustment CASCADE;
DROP TABLE IF EXISTS dw.contract_revision CASCADE;

ALTER TABLE dw.shop DROP COLUMN IF EXISTS contract_id;

ALTER TABLE dw.party DROP COLUMN IF EXISTS comment;
ALTER TABLE dw.party DROP COLUMN IF EXISTS suspension_active_since;
ALTER TABLE dw.party DROP COLUMN IF EXISTS suspension_suspended_since;

ALTER TABLE dw.party DROP COLUMN IF EXISTS suspension_active_since;
ALTER TABLE dw.party DROP COLUMN IF EXISTS suspension_suspended_since;

