ALTER TYPE dw.payment_session_status ADD VALUE 'interaction_changed_redirect';
ALTER TYPE dw.payment_session_status ADD VALUE 'interaction_changed_crypto_transfer';
ALTER TYPE dw.payment_session_status ADD VALUE 'interaction_changed_api_extension';
ALTER TYPE dw.payment_session_status ADD VALUE 'interaction_changed_qr_display';
ALTER TYPE dw.payment_session_status ADD VALUE 'interaction_changed_terminal_receipt';