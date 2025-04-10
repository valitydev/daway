--payment_payer_info partition
DO
$$
    DECLARE
table_name              TEXT     := 'payment_payer_info';
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
            --- Продолжим использовать предыдущую последовательность (payment_payer_info_id_seq), поэтому используем BIGINT вместо BIGSERIAL
            EXECUTE format('CREATE TABLE IF NOT EXISTS dw.%I_new
                            (
                                id                                BIGINT                      NOT NULL DEFAULT nextval(''dw.payment_payer_info_id_seq''::regclass),
                                event_created_at                  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                invoice_id                        CHARACTER VARYING,
                                payment_id                        CHARACTER VARYING,
                                payer_type                        dw.payer_type               NOT NULL,
                                payment_tool_type                 dw.payment_tool_type        NOT NULL,
                                bank_card_token                   CHARACTER VARYING,
                                bank_card_payment_system          CHARACTER VARYING,
                                bank_card_bin                     CHARACTER VARYING,
                                bank_card_masked_pan              CHARACTER VARYING,
                                bank_card_token_provider          CHARACTER VARYING,
                                payment_terminal_type             CHARACTER VARYING,
                                digital_wallet_provider           CHARACTER VARYING,
                                digital_wallet_id                 CHARACTER VARYING,
                                payment_session_id                CHARACTER VARYING,
                                ip_address                        CHARACTER VARYING,
                                fingerprint                        CHARACTER VARYING,
                                phone_number                      CHARACTER VARYING,
                                email                             CHARACTER VARYING,
                                customer_id                       CHARACTER VARYING,
                                customer_binding_id               CHARACTER VARYING,
                                customer_rec_payment_tool_id      CHARACTER VARYING,
                                recurrent_parent_invoice_id       CHARACTER VARYING,
                                recurrent_parent_payment_id       CHARACTER VARYING,
                                crypto_currency_type              CHARACTER VARYING,
                                mobile_phone_cc                   CHARACTER VARYING,
                                mobile_phone_ctn                  CHARACTER VARYING,
                                issuer_country                    CHARACTER VARYING,
                                bank_name                         CHARACTER VARYING,
                                bank_card_cardholder_name         CHARACTER VARYING,
                                mobile_operator                   CHARACTER VARYING,
                                wtime                             TIMESTAMP WITHOUT TIME ZONe NOT NULL DEFAULT (now() AT TIME ZONE ''utc''::text),
                                sequence_id                       BIGINT,
                                change_id                         INTEGER,

                                CONSTRAINT %I_new_pkey PRIMARY KEY (id, %s),
                                CONSTRAINT %I_new_uniq UNIQUE (invoice_id, payment_id, sequence_id, change_id, %s)
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