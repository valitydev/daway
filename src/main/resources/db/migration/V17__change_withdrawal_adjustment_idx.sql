CREATE INDEX CONCURRENTLY IF NOT EXISTS withdrawal_session_withdrawal_id_idx ON dw.withdrawal_session USING btree (withdrawal_id);
