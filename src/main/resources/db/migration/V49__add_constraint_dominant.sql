DROP TABLE IF EXISTS dw.payment_method CASCADE;
DROP TABLE IF EXISTS dw.inspector CASCADE;
DROP TABLE IF EXISTS dw.trade_bloc CASCADE;
DROP TABLE IF EXISTS dw.country CASCADE;

ALTER TABLE dw.payment_institution
    ADD CONSTRAINT payment_institution_uniq
        UNIQUE (payment_institution_ref_id, version_id);

ALTER TABLE dw.category
    ADD CONSTRAINT category_uniq
        UNIQUE (category_ref_id, version_id);

ALTER TABLE dw.payment_routing_rule
    ADD CONSTRAINT payment_routing_rule_uniq
        UNIQUE (rule_ref_id, version_id);

ALTER TABLE dw.provider
    ADD CONSTRAINT provider_uniq
        UNIQUE (provider_ref_id, version_id);

ALTER TABLE dw.proxy
    ADD CONSTRAINT proxy_uniq
        UNIQUE (proxy_ref_id, version_id);

ALTER TABLE dw.terminal
    ADD CONSTRAINT terminal_uniq
        UNIQUE (terminal_ref_id, version_id);

ALTER TABLE dw.term_set_hierarchy
    ADD CONSTRAINT term_set_hierarchy_uniq
        UNIQUE (term_set_hierarchy_ref_id, version_id);