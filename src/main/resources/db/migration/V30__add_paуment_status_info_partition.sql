--payment_status_info partition
DO
$$
    DECLARE
table_name              TEXT     := 'payment_status_info';
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
            --- Продолжим использовать предыдущую последовательность (payment_status_info_id_seq), поэтому используем BIGINT вместо BIGSERIAL
            EXECUTE format('CREATE TABLE IF NOT EXISTS dw.%I_new
                            (
                                id                               BIGINT                      NOT NULL DEFAULT nextval(''dw.payment_status_info_id_seq''::regclass),
                                event_created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                invoice_id                       CHARACTER VARYING           NOT NULL,
                                payment_id                       CHARACTER VARYING           NOT NULL,
                                status                           dw.payment_status           NOT NULL,
                                reason                           CHARACTER VARYING,
                                amount                           BIGINT,
                                currency_code                    CHARACTER VARYING,
                                cart_json                        CHARACTER VARYING,
                                current                          BOOLEAN                     NOT NULL DEFAULT false,
                                wtime                            TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE ''utc''::text),
                                sequence_id                      BIGINT,
                                change_id                        INTEGER,

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