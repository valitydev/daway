ALTER TABLE dw.invoice
    ADD COLUMN customer_id CHARACTER VARYING,
    ADD COLUMN user_id CHARACTER VARYING;

-- The existing customer_id contains contact_info.email, not a Customer ID.
ALTER TABLE dw.withdrawal RENAME COLUMN customer_id TO email;
ALTER TABLE dw.withdrawal
    ADD COLUMN customer_id CHARACTER VARYING,
    ADD COLUMN user_id CHARACTER VARYING;
