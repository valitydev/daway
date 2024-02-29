ALTER TABLE dw.payment_session_info
    ADD COLUMN IF NOT EXISTS user_interaction boolean;
ALTER TABLE dw.payment_session_info
    ADD COLUMN IF NOT EXISTS user_interaction_url character varying;