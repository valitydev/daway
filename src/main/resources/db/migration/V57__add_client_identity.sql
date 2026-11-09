ALTER TABLE dw.invoice
    ADD COLUMN customer_id CHARACTER VARYING,
    ADD COLUMN user_id CHARACTER VARYING;

ALTER TABLE dw.payment_payer_info
    ADD COLUMN user_id CHARACTER VARYING;

-- Preserve the existing customer_id values (email for legacy events).
ALTER TABLE dw.withdrawal
    ADD COLUMN email CHARACTER VARYING,
    ADD COLUMN user_id CHARACTER VARYING;
