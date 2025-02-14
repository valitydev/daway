# Перевод таблиц в режим партиционирвания

Перед выполнением всех скриптов необходимо сделать бэкап целевой таблицы.
В рамках перехода существующих крупных таблиц в режим работы с партициями
необходимо выполнить [скрипты](/src/main/resources/db/migration/V28__add_paуment_partition.sql).

После применения скриптов может возникнуть ситуация, когда в старой таблице (***_old) остались исторические данные.
Для этих данных предусмотрен процесс миграции в исторические партиции для созданной партиционированной таблицы.
Миграция будет проходить при даунтайме сервиса, т.е. скорее всего в ночное время или время минимальной нагрузки.

Для проведения миграции предлагается следующий скрипт (для таблицы _dw.payments_):

```
DO $$
DECLARE
    parent_table_name TEXT := 'dw.payments';
    parent_table_old_name TEXT := 'dw.payments_old';
    start_date DATE := '2021-12-01';
    end_date DATE := date_trunc('MONTH',now())::DATE;
    partition_name TEXT;
    partition_step_interval INTERVAL := '1 month';
BEGIN
    WHILE start_date < end_date LOOP
        partition_name := parent_table_name || '_' || TO_CHAR(start_date, 'YYYY_MM');
        EXECUTE format('
            CREATE TABLE %1$I PARTITION OF %2$I
            FOR VALUES FROM (%1$L) TO (%2$L);

            WITH rows AS (
            DELETE FROM %3$I old
            WHERE event_created_at >= %1$L AND event_created_at < %2$L
            RETURNING old.*
            )
            INSERT INTO %1$I
            SELECT * FROM rows;',
            partition_name,
            parent_table_name,
            parent_table_old_name,
            start_date,
            start_date + partition_step_interval
        );
        start_date := start_date + partition_step_interval;
    END LOOP;
END $$;
```

## План отката

В случае каких либо проблем предусмотрен план отката.

1. Копирование данных из созданной партиционированно таблицы в случае, если были добавлены новые записи за время
   миграции

```
INSERT INTO dw.payments_old
SELECT * FROM dw.payments;
```

2. Удаление таблицы

```
DROP TABLE dw.payments.
```

3. Переименовани старой таблицы

```
ALTER TABLE dw.payments_old RENAME TO dw.payments
```

Все это при условии, что старую таблицу не дропали. Иначе необходимо восстановить старую таблицу из бэкапа.

