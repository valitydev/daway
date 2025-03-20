# Перевод таблиц в режим партиционирвания

Перед выполнением всех скриптов необходимо сделать бэкап целевой таблицы.
В рамках перехода существующих крупных таблиц в режим работы с партициями
необходимо выполнить [скрипты](/src/main/resources/db/migration/V28__add_paуment_partition.sql).

После применения скриптов появится пустая партиционированная таблица с постфиксом _new - клон целевой таблицы.
Необходимо скопировать данные из целевой таблицы в новую партиционированную.
Для этих данных предусмотрен процесс миграции в исторические партиции для новой созданной таблицы.
Пример для таблицы _dw.payment_:

```
DO $$
DECLARE
    parent_table_name TEXT := 'dw.payments_new';
    parent_table_old_name TEXT := 'dw.payments';
    start_date DATE := '2021-12-01';
    end_date DATE := date_trunc('MONTH',now())::DATE;
    partition_name TEXT;
    partition_step_interval INTERVAL := '1 month';
BEGIN
    WHILE start_date < end_date LOOP
        partition_name := parent_table_old_name || '_' || TO_CHAR(start_date, 'YYYY_MM');
        EXECUTE format('
            CREATE TABLE %1$I PARTITION OF %2$I
            FOR VALUES FROM (%1$L) TO (%2$L);

            WITH rows AS (
            SELECT old.* FROM %3$I old
            WHERE event_created_at >= %1$L AND event_created_at < %2$L
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

После того как данные будут смигрированны, daway останавливается, таблица payment переименовывается в payment_old, а
payment_new в payment.
devops команда делает небольшой сдвиг соответствующего offset в kafka для подгрузки данных, пришедших за время
даунтайма, и вновь запускают daway.

## План отката

В случае каких либо проблем предусмотрен план отката.

1. Удаление новой таблицы

```
DROP TABLE dw.payments_new.
```

2. Переименование payment в payment_new, а payment_old в payment. То есть обратный процесс.

После успешного завершения миграции удалить старую таблицу dw.payment_old. 

