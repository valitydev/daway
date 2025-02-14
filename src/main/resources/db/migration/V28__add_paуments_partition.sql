--payments partition
DO
$$
    DECLARE
        table_name              TEXT     := 'dw.payments';
        is_partitioned          BOOLEAN;
        partition_name          TEXT;
        partition_field         TEXT     := 'event_created_at';
        partition_interval      INTERVAL := '7 months'; --- задаем нужный интервал для создания партиций
        partition_step_interval INTERVAL := '1 month'; --- задаем нужный шаг интервала партиции
        start_date              DATE     := date_trunc('MONTH', now())::DATE;
        end_date                DATE     := date_trunc('MONTH', now())::DATE + partition_interval;
    BEGIN
        -- Проверяем, является ли таблица партиционированной
        SELECT COUNT(*) > 0
        INTO is_partitioned
        FROM pg_class c
                 JOIN pg_partitioned_table p ON c.oid = p.partrelid
        WHERE c.relname = table_name;

        IF is_partitioned THEN
            --- Таблица уже является партиционированной. Пропускаем партиционирование
            EXIT;
        ELSE
            --- Таблица не является партиционированной. Создаем партиционированную таблицу и партиции для нее

            --- 1. Переименовываем текущую таблицу в ***_old
            EXECUTE format('ALTER TABLE %I RENAME TO %I_old', table_name, table_name);

            --- 2. Добавляем в индексы поле партиционирования
            --- Удаляем старые индексы
            EXECUTE format('ALTER TABLE %I_old DROP CONSTRAINT %I_old_pkey;', table_name);
            EXECUTE format('ALTER TABLE %I_old DROP CONSTRAINT %I_old_invoice_id_payment_id_sequence_id_change_id_key;',
                           table_name, table_name);
            --- Создаем новые индексы с полем партиционирования
            EXECUTE format('ALTER TABLE %I_old ADD CONSTRAINT %I_old_pkey PRIMARY KEY (id, %L);', table_name,
                           table_name, partition_field);
            EXECUTE format(
                    'ALTER TABLE %I_old ADD CONSTRAINT %I_old_invoice_id_payment_id_sequence_id_change_id_event_created_at_key UNIQUE (invoice_id, payment_id, sequence_id, change_id, %L);',
                    table_name, table_name, partition_field);

            --- 3. Создаем новую партиционированную таблицу с сохранением старого имени для обеспечения совместимости.
            --- Также сохраняется структура и индексы таблицы оригинала.
            EXECUTE format('CREATE TABLE %I (LIKE %I_old INCLUDING ALL) PARTITION BY RANGE ( %L );', table_name,
                           table_name, partition_field);

            --- 4. Создаем партиции
            WHILE start_date < end_date
                LOOP
                    partition_name := table_name || '_' || TO_CHAR(start_date, 'YYYY_MM');
                    EXECUTE format('
                CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L);',
                                   partition_name,
                                   table_name,
                                   start_date,
                                   start_date + partition_step_interval
                        );
                    start_date := start_date + partition_step_interval;
                END LOOP;
        END IF;
    END
$$;