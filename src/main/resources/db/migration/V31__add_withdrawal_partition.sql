--withdrawal partition
DO
$$
    DECLARE
table_name              TEXT     := 'withdrawal';
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
            --- Продолжим использовать предыдущую последовательность (withdrawal_id_seq), поэтому используем BIGINT вместо BIGSERIAL
            EXECUTE format('CREATE TABLE IF NOT EXISTS dw.%I_new
                            (
                                id                                    BIGINT                                                           NOT NULL DEFAULT nextval(''dw.withdrawal_id_seq''::regclass),
                                event_created_at                      TIMESTAMP WITHOUT TIME ZONE                                      NOT NULL,
                                event_occured_at                      TIMESTAMP WITHOUT TIME ZONE                                      NOT NULL,
                                sequence_id                           INTEGER                                                          NOT NULL,
                                wallet_id                             CHARACTER VARYING                                                NOT NULL,
                                destination_id                        CHARACTER VARYING                                                NOT NULL,
                                withdrawal_id                         CHARACTER VARYING                                                NOT NULL,
                                provider_id_legacy                    CHARACTER VARYING,
                                amount                                BIGINT                                                           NOT NULL,
                                currency_code                         CHARACTER VARYING                                                NOT NULL,
                                withdrawal_status                     dw.withdrawal_status                                             NOT NULL,
                                withdrawal_transfer_status            dw.withdrawal_transfer_status,
                                wtime                                 TIMESTAMP WITHOUT TIME ZONE DEFAULT timezone(''utc''::text, now()) NOT NULL,
                                current                               BOOLEAN                     DEFAULT true                         NOT NULL,
                                fee                                   BIGINT,
                                provider_fee                          BIGINT,
                                external_id                           CHARACTER VARYING,
                                context_json                          CHARACTER VARYING,
                                withdrawal_status_failed_failure_json CHARACTER VARYING,
                                provider_id                           INTEGER,
                                terminal_id                           CHARACTER VARYING,
                                exchange_rate                         DECIMAL (10,4),
                                exchange_amount_from                  BIGINT,
                                exchange_currency_from                CHARACTER VARYING,
                                exchange_amount_to                    BIGINT,
                                exchange_currency_to                  CHARACTER VARYING,

                                CONSTRAINT %I_new_pkey PRIMARY KEY (id, %s),
                                CONSTRAINT %I_new_uniq UNIQUE (withdrawal_id, sequence_id, %s)
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