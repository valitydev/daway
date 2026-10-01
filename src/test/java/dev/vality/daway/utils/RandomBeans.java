package dev.vality.daway.utils;

import dev.vality.geck.serializer.kit.mock.MockMode;
import dev.vality.geck.serializer.kit.mock.MockTBaseProcessor;
import dev.vality.geck.serializer.kit.tbase.TBaseHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.thrift.TBase;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomBeans {

    private static final long DEFAULT_SEED = 123L;

    public static <T> T random(Class<T> type, String... excludedFields) {
        return random(DEFAULT_SEED, type, excludedFields);
    }

    public static <T> T random(Long seed, Class<T> type, String... excludedFields) {
        return new EasyRandom(parameters(seed, excludedFields)).nextObject(type);
    }

    public static <T> List<T> randomListOf(int amount, Class<T> type, String... excludedFields) {
        return randomListOf(DEFAULT_SEED, amount, type, excludedFields);
    }

    public static <T> List<T> randomListOf(Long seed, int amount, Class<T> type, String... excludedFields) {
        return new EasyRandom(parameters(seed, excludedFields)).objects(type, amount).collect(Collectors.toList());
    }

    @SneakyThrows
    public static <T extends TBase<?, ?>> T randomThriftOnlyRequiredFields(Class<T> type) {
        var processor = new MockTBaseProcessor(MockMode.REQUIRED_ONLY, 25, 1);
        processor.addFieldHandler(handler -> handler.value(Instant.now().toString()), "created_at", "at", "due");
        return processor.process(type.getConstructor().newInstance(), new TBaseHandler<>(type));
    }

    private static EasyRandomParameters parameters(Long seed, String... excludedFields) {
        var parameters = new EasyRandomParameters();
        parameters.randomize(LocalDateTime.class, () -> LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        parameters.randomize(Instant.class, () -> Instant.now().truncatedTo(ChronoUnit.MICROS));
        parameters.randomize(Date.class, () -> Date.from(Instant.now().truncatedTo(ChronoUnit.MICROS)));
        parameters.randomize(Timestamp.class, () -> Timestamp.from(Instant.now().truncatedTo(ChronoUnit.MICROS)));
        parameters.randomize(OffsetDateTime.class, () -> OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
        parameters.randomize(ZonedDateTime.class, () -> ZonedDateTime.now().truncatedTo(ChronoUnit.MICROS));
        parameters.randomize(Calendar.class, () -> {
            var calendar = Calendar.getInstance();
            calendar.setTime(Date.from(Instant.now().truncatedTo(ChronoUnit.MICROS)));
            return calendar;
        });
        parameters.randomize(LocalDate.class, LocalDate::now);
        parameters.randomize(LocalTime.class, () -> LocalTime.now().truncatedTo(ChronoUnit.MICROS));
        if (excludedFields != null) {
            for (var name : excludedFields) {
                parameters.excludeField(field -> field.getName().equals(name));
            }
        }
        return parameters.seed(seed)
                .objectPoolSize(100)
                .randomizationDepth(3)
                .charset(StandardCharsets.UTF_8)
                .stringLengthRange(5, 50)
                .collectionSizeRange(1, 10);
    }
}
