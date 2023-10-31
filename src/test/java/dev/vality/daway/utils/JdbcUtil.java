package dev.vality.daway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcUtil {

    public static int countEntities(JdbcTemplate jdbcTemplate,
                                    String table) {
        String query = "SELECT count(*) FROM dw." + table;
        return Objects.requireNonNull(
                jdbcTemplate.queryForObject(query, Integer.class));
    }

    public static int countPaymentEntity(JdbcTemplate jdbcTemplate,
                                   String table,
                                   String invoiceId,
                                   String paymentId,
                                   boolean withCurrent) {
        String query = "SELECT count(*) FROM dw." + table + "  WHERE invoice_id = ? AND payment_id = ?";
        if (withCurrent) {
            query += " AND current";
        }
        return Objects.requireNonNull(
                jdbcTemplate.queryForObject(query, new Object[]{invoiceId, paymentId}, Integer.class));
    }

    public static int countInvoiceEntity(JdbcTemplate jdbcTemplate,
                                   String table,
                                   String invoiceId,
                                   boolean withCurrent) {
        String query = "SELECT count(*) FROM dw." + table + "  WHERE invoice_id = ?";
        if (withCurrent) {
            query += " AND current";
        }
        return Objects.requireNonNull(jdbcTemplate.queryForObject(query, new Object[]{invoiceId}, Integer.class));
    }
}
