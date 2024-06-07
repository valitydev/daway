ALTER TABLE dw.party
    RENAME COLUMN contact_info_email TO registration_email;

ALTER TABLE dw.party
    ADD COLUMN IF NOT EXISTS manager_contact_emails text[];

ALTER TABLE dw.party
    ADD COLUMN IF NOT EXISTS party_name text;

ALTER TABLE dw.party
    ADD COLUMN IF NOT EXISTS comment text;