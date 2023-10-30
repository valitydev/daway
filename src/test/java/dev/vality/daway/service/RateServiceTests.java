package dev.vality.daway.service;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.domain.tables.pojos.Rate;
import dev.vality.daway.utils.RateSinkEventTestUtils;
import dev.vality.machinegun.eventsink.SinkEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@PostgresqlSpringBootITest
public class RateServiceTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RateService rateService;

    @Test
    public void rateServiceTest() {
        jdbcTemplate.execute("truncate table dw.rate cascade");
        String sourceId = "CBR";

        rateService.handleEvents(RateSinkEventTestUtils.create(sourceId));

        List<Rate> rates = jdbcTemplate.query(
                "SELECT * FROM dw.rate AS rate WHERE rate.source_id = ? AND rate.current",
                new Object[]{sourceId},
                new BeanPropertyRowMapper(Rate.class)
        );
        Assertions.assertEquals(4, rates.size());
    }

    @Test
    public void rateServiceDuplicationTest() {
        jdbcTemplate.execute("truncate table dw.rate cascade");
        String sourceId = "CBR";

        List<SinkEvent> sinkEvents = RateSinkEventTestUtils.create(sourceId);
        rateService.handleEvents(sinkEvents);
        rateService.handleEvents(sinkEvents);

        List<Rate> rates = jdbcTemplate.query(
                "SELECT * FROM dw.rate AS rate WHERE rate.source_id = ? AND rate.current",
                new Object[]{sourceId},
                new BeanPropertyRowMapper(Rate.class)
        );
        Assertions.assertEquals(4, rates.size());
    }

    @Test
    public void rateServiceDuplicationWhenPaymentSystemIsNullTest() {
        jdbcTemplate.execute("truncate table dw.rate cascade");
        String sourceId = "CBR";

        List<SinkEvent> sinkEvents = RateSinkEventTestUtils.create(sourceId, "payment_system");
        rateService.handleEvents(sinkEvents);
        rateService.handleEvents(sinkEvents);

        List<Rate> rates = jdbcTemplate.query(
                "SELECT * FROM dw.rate AS rate WHERE rate.source_id = ? AND rate.current",
                new Object[]{sourceId},
                new BeanPropertyRowMapper(Rate.class)
        );
        Assertions.assertEquals(4, rates.size());
    }

}
