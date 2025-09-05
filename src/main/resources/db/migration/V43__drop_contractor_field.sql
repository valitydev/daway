ALTER TABLE dw.payment_institution DROP COLUMN IF EXISTS default_contract_template_json;
ALTER TABLE dw.payment_institution DROP COLUMN IF EXISTS defaultWalletContractTemplateJson;

DROP TABLE IF EXISTS dw.contractor_revision CASCADE;