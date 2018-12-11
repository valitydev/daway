/*
 * This file is generated by jOOQ.
 */
package com.rbkmoney.newway.domain.tables;


import com.rbkmoney.newway.domain.Keys;
import com.rbkmoney.newway.domain.Nw;
import com.rbkmoney.newway.domain.tables.records.RateRecord;
import org.jooq.*;
import org.jooq.Identity;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@Generated(
        value = {
                "http://www.jooq.org",
                "jOOQ version:3.9.6"
        },
        comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Rate extends TableImpl<RateRecord> {

    private static final long serialVersionUID = -2029745769;

    /**
     * The reference instance of <code>nw.rate</code>
     */
    public static final Rate RATE = new Rate();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RateRecord> getRecordType() {
        return RateRecord.class;
    }

    /**
     * The column <code>nw.rate.id</code>.
     */
    public final TableField<RateRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('nw.rate_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>nw.rate.event_id</code>.
     */
    public final TableField<RateRecord, Long> EVENT_ID = createField("event_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>nw.rate.event_created_at</code>.
     */
    public final TableField<RateRecord, LocalDateTime> EVENT_CREATED_AT = createField("event_created_at", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>nw.rate.source_id</code>.
     */
    public final TableField<RateRecord, String> SOURCE_ID = createField("source_id", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>nw.rate.lower_bound_inclusive</code>.
     */
    public final TableField<RateRecord, LocalDateTime> LOWER_BOUND_INCLUSIVE = createField("lower_bound_inclusive", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>nw.rate.upper_bound_exclusive</code>.
     */
    public final TableField<RateRecord, LocalDateTime> UPPER_BOUND_EXCLUSIVE = createField("upper_bound_exclusive", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>nw.rate.source_symbolic_code</code>.
     */
    public final TableField<RateRecord, String> SOURCE_SYMBOLIC_CODE = createField("source_symbolic_code", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>nw.rate.source_exponent</code>.
     */
    public final TableField<RateRecord, Short> SOURCE_EXPONENT = createField("source_exponent", org.jooq.impl.SQLDataType.SMALLINT.nullable(false), this, "");

    /**
     * The column <code>nw.rate.destination_symbolic_code</code>.
     */
    public final TableField<RateRecord, String> DESTINATION_SYMBOLIC_CODE = createField("destination_symbolic_code", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>nw.rate.destination_exponent</code>.
     */
    public final TableField<RateRecord, Short> DESTINATION_EXPONENT = createField("destination_exponent", org.jooq.impl.SQLDataType.SMALLINT.nullable(false), this, "");

    /**
     * The column <code>nw.rate.exchange_rate_rational_p</code>.
     */
    public final TableField<RateRecord, Long> EXCHANGE_RATE_RATIONAL_P = createField("exchange_rate_rational_p", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>nw.rate.exchange_rate_rational_q</code>.
     */
    public final TableField<RateRecord, Long> EXCHANGE_RATE_RATIONAL_Q = createField("exchange_rate_rational_q", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>nw.rate.wtime</code>.
     */
    public final TableField<RateRecord, LocalDateTime> WTIME = createField("wtime", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("timezone('utc'::text, now())", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>nw.rate.current</code>.
     */
    public final TableField<RateRecord, Boolean> CURRENT = createField("current", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.field("true", org.jooq.impl.SQLDataType.BOOLEAN)), this, "");

    /**
     * Create a <code>nw.rate</code> table reference
     */
    public Rate() {
        this("rate", null);
    }

    /**
     * Create an aliased <code>nw.rate</code> table reference
     */
    public Rate(String alias) {
        this(alias, RATE);
    }

    private Rate(String alias, Table<RateRecord> aliased) {
        this(alias, aliased, null);
    }

    private Rate(String alias, Table<RateRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Nw.NW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<RateRecord, Long> getIdentity() {
        return Keys.IDENTITY_RATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RateRecord> getPrimaryKey() {
        return Keys.RATE_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RateRecord>> getKeys() {
        return Arrays.<UniqueKey<RateRecord>>asList(Keys.RATE_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rate as(String alias) {
        return new Rate(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Rate rename(String name) {
        return new Rate(name, null);
    }
}
