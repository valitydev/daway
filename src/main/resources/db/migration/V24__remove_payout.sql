DROP TABLE IF EXISTS dw.payout;
DROP TABLE IF EXISTS dw.payout_method;
DROP TABLE IF EXISTS dw.payout_tool;
DROP TYPE IF EXISTS dw.payout_account_type;
DROP TYPE IF EXISTS dw.payout_paid_status_details;
DROP TYPE IF EXISTS dw.payout_status;
DROP TYPE IF EXISTS dw.payout_tool_info;
DROP TYPE IF EXISTS dw.payout_type;

ALTER TYPE dw.PAYMENT_CHANGE_TYPE RENAME TO PAYMENT_CHANGE_TYPE_OLD;
--  create the new type
CREATE TYPE dw.PAYMENT_CHANGE_TYPE AS ENUM(
    'payment',
    'refund',
    'adjustment',
    'chargeback');
--  update the columns to use the new type
ALTER TABLE dw.cash_flow ALTER COLUMN obj_type TYPE dw.PAYMENT_CHANGE_TYPE USING obj_type :: TEXT :: dw.PAYMENT_CHANGE_TYPE;

--  drop function with old type
DROP FUNCTION IF EXISTS dw.get_cashflow_sum(_cash_flow dw.cash_flow, obj_type dw.payment_change_type_old,
    source_account_type dw.cash_flow_account,
    source_account_type_values character varying [],
    destination_account_type dw.cash_flow_account,
    destination_account_type_values character varying []);

--  create function with new type
CREATE FUNCTION dw.get_cashflow_sum(_cash_flow dw.cash_flow, obj_type dw.payment_change_type,
                                    source_account_type dw.cash_flow_account,
                                    source_account_type_values character varying [],
                                    destination_account_type dw.cash_flow_account,
                                    destination_account_type_values character varying []) RETURNS bigint
    LANGUAGE plpgsql
    IMMUTABLE PARALLEL SAFE
AS
$_$
begin
return (
    coalesce(
            (select amount
             from (select ($1).*) as cash_flow
             where cash_flow.obj_type = $2
               and cash_flow.source_account_type = $3
               and cash_flow.source_account_type_value = ANY ($4)
               and cash_flow.destination_account_type = $5
               and cash_flow.destination_account_type_value = ANY ($6)
               and (
                     (cash_flow.obj_type = 'adjustment' and cash_flow.adj_flow_type = 'new_cash_flow')
                     or (cash_flow.obj_type != 'adjustment' and cash_flow.adj_flow_type is null)
                 )), 0)
    );
end;
$_$;

-- remove the old type
DROP TYPE dw.PAYMENT_CHANGE_TYPE_OLD;
