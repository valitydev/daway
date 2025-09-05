package dev.vality.daway.service;

import dev.vality.damsel.domain_config_v2.FinalOperation;
import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.daway.dao.dominant.iface.DominantDao;
import dev.vality.daway.handler.dominant.DominantHandler;
import dev.vality.daway.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DominantService {

    private final DominantDao dominantDao;

    private final List<DominantHandler> handlers;

    @Transactional(propagation = Propagation.REQUIRED)
    public void processCommit(List<HistoricalCommit> operations) {
        log.debug("Process commit with operations: {}", operations);
        operations.forEach(operation -> handlers.forEach(handler -> {
            operation.getOps().forEach(finalOperation -> {
                        if (handler.acceptAndSet(finalOperation)) {
                            processOperation(handler, finalOperation, operation.version, operation.created_at);
                        }
                    }
            );

        }));
    }

    private void processOperation(DominantHandler handler, FinalOperation operation, Long versionId, String createdAt) {
        try {
            log.info("Start to process commit with versionId={} operation={} ",
                    versionId, JsonUtil.thriftBaseToJsonString(operation));
            handler.handle(operation, versionId, createdAt);
            log.info("End to process commit with versionId={}", versionId);
        } catch (Exception ex) {
            log.error("The error was received when the service processed operation", ex);
            throw ex;
        }
    }

    public Optional<Long> getLastVersionId() {
        Optional<Long> lastVersionId = Optional.ofNullable(dominantDao.getLastVersionId());
        log.info("Last dominant versionId={}", lastVersionId);
        return lastVersionId;
    }

    public void updateLastVersionId(Long lastVersionId) {
        dominantDao.updateLastVersionId(lastVersionId);
        log.info("Last dominant versionId={} is updated", lastVersionId);
    }
}
