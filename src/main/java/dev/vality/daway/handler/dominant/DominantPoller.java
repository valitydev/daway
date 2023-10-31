package dev.vality.daway.handler.dominant;

import dev.vality.damsel.domain_config.Commit;
import dev.vality.damsel.domain_config.RepositorySrv;
import dev.vality.daway.service.DominantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@DependsOn("flywayInitializer")
@RequiredArgsConstructor
public class DominantPoller {

    private final RepositorySrv.Iface dominantClient;
    private final DominantService dominantService;
    private final int maxQuerySize;
    private final boolean pollingEnabled;

    @Scheduled(fixedDelayString = "${dmt.polling.delay}")
    @SchedulerLock(name = "TaskScheduler_dominant_polling_process")
    public void process() {
        if (pollingEnabled) {
            Map<Long, Commit> pullRange;
            final AtomicLong versionId = new AtomicLong();
            final AtomicLong after = new AtomicLong(dominantService.getLastVersionId().orElse(0L));
            try {
                pullRange = dominantClient.pullRange(after.get(), maxQuerySize);
                pullRange.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> handleDominantData(after, versionId, e));
            } catch (Exception e) {
                log.warn("Error to polling dominant, after={}", after, e);
            }
        }
    }

    private void handleDominantData(AtomicLong after, AtomicLong versionId, Map.Entry<Long, Commit> e) {
        try {
            versionId.set(e.getKey());
            dominantService.processCommit(versionId.get(), e);
            dominantService.updateLastVersionId(versionId.get());
            after.set(versionId.get());
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    String.format("Unexpected error when polling dominant, versionId=%d", versionId.get()),
                    ex);
        }
    }
}
