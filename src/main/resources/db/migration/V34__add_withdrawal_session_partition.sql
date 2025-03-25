--withdrawal_session partition
DO
$$
    DECLARE
table_name              TEXT     := 'withdrawal_session';
        is_partitioned
BOOLEAN;
        partition_name
TEXT;
        partition_field
TEXT     := 'event_created_at';
        partition_interval
INTERVAL := '60 months'; --- задаем нужный интервал для создания партиций
        partition_step_interval
INTERVAL := '1 month'; --- задаем нужный шаг интервала партиции
        start_date
DATE     := '2021-12-01'; --- начало времен;
        end_date
DATE     := date_trunc('MONTH', start_date)::DATE + partition_interval;
BEGIN
        -- Проверяем, является ли таблица партиционированной
SELECT COUNT(*) > 0
INTO is_partitioned
FROM pg_class c
         JOIN pg_partitioned_table p ON c.oid = p.partrelid
WHERE c.relname = table_name;

IF
is_partitioned THEN
            --- Таблица уже является партиционированной. Пропускаем партиционирование
            RETURN;
ELSE
            --- Таблица не является партиционированной.
            --- 1. Создаем партиционированную таблицу копию оригинальной с постфиксом _new.
            --- Учитываем constraint с учетом поля партиционирования.
            --- Продолжим использовать предыдущую последовательность (withdrawal_session_id_seq), поэтому используем BIGINT вместо BIGSERIAL
            EXECUTE format('CREATE TABLE IF NOT EXISTS dw.%I_new
                            (
                                id                                BIGINT                                                           NOT NULL DEFAULT nextval(''dw.withdrawal_session_id_seq''::regclass),
                                event_created_at                  TIMESTAMP WITHOUT TIME ZONE                                      NOT NULL,
                                event_occured_at                  TIMESTAMP WITHOUT TIME ZONE                                      NOT NULL,
                                sequence_id                       integer                                                          NOT NULL,
                                withdrawal_session_id             CHARACTER VARYING                                                NOT NULL,
                                withdrawal_session_status         dw.withdrawal_session_status                                     NOT NULL,
                                provider_id_legacy                CHARACTER VARYING,
                                withdrawal_id                     CHARACTER VARYING                                                NOT NULL,
                                destination_card_token            CHARACTER VARYING,
                                destination_card_payment_system   VARCHAR,
                                destination_card_bin              CHARACTER VARYING,
                                destination_card_masked_pan       CHARACTER VARYING,
                                amount                            bigint                                                           NOT NULL,
                                currency_code                     CHARACTER VARYING                                                NOT NULL,
                                sender_party_id                   CHARACTER VARYING,
                                sender_provider_id                CHARACTER VARYING,
                                sender_class_id                   CHARACTER VARYING,
                                sender_contract_id                CHARACTER VARYING,
                                receiver_party_id                 CHARACTER VARYING,
                                receiver_provider_id              CHARACTER VARYING,
                                receiver_class_id                 CHARACTER VARYING,
                                receiver_contract_id              CHARACTER VARYING,
                                adapter_state                     CHARACTER VARYING,
                                tran_info_id                      CHARACTER VARYING,
                                tran_info_timestamp               TIMESTAMP WITHOUT TIME ZONE,
                                tran_info_json                    CHARACTER VARYING,
                                wtime                             TIMESTAMP WITHOUT TIME ZONE DEFAULT timezone(''utc''::text, now()) NOT NULL,
                                current                           BOOLEAN                     DEFAULT true                         NOT NULL,
                                failure_json                      CHARACTER VARYING,
                                resource_type                     dw.destination_resource_type                                     NOT NULL,
                                resource_crypto_wallet_id         CHARACTER VARYING,
                                resource_crypto_wallet_type       CHARACTER VARYING,
                                resource_crypto_wallet_data       CHARACTER VARYING,
                                resource_bank_card_type           CHARACTER VARYING,
                                resource_bank_card_issuer_country CHARACTER VARYING,
                                resource_bank_card_bank_name      CHARACTER VARYING,
                                tran_additional_info              CHARACTER VARYING,
                                tran_additional_info_rrn          CHARACTER VARYING,
                                tran_additional_info_json         CHARACTER VARYING,
                                provider_id                       INTEGER,
                                resource_digital_wallet_id        CHARACTER VARYING,
                                resource_digital_wallet_data      CHARACTER VARYING,

                                CONSTRAINT %I_new_pkey PRIMARY KEY (id, %s),
                                CONSTRAINT %I_new_uniq UNIQUE (withdrawal_session_id, sequence_id, %s)
                            ) PARTITION BY RANGE ( %s );',
                           table_name,
                           table_name,
                           partition_field,
                           table_name,
                           partition_field,
                           partition_field);


            --- 2. Создаем партиции
            WHILE
start_date < end_date
                LOOP
                    partition_name := table_name || '_' || TO_CHAR(start_date, 'YYYY_MM');
EXECUTE format('
                CREATE TABLE dw.%I PARTITION OF dw.%I_new FOR VALUES FROM (%L) TO (%L);',
               partition_name,
               table_name,
               start_date,
               start_date + partition_step_interval
        );
start_date
:= start_date + partition_step_interval;
END LOOP;
END IF;
END
$$;