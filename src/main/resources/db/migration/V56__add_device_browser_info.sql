ALTER TABLE dw.payment_payer_info
    ADD COLUMN IF NOT EXISTS browser_info_json CHARACTER VARYING;
ALTER TABLE dw.payment_payer_info
    ADD COLUMN IF NOT EXISTS device_info_json CHARACTER VARYING;
ALTER TABLE dw.payment_payer_info
    ADD COLUMN IF NOT EXISTS peer_user_agent CHARACTER VARYING;
ALTER TABLE dw.payment_payer_info
    ADD COLUMN IF NOT EXISTS peer_accept_header CHARACTER VARYING;
