package dev.vality.daway.service;

import dev.vality.damsel.domain_config.Commit;
import dev.vality.damsel.domain_config.Operation;
import dev.vality.daway.dao.dominant.iface.DominantDao;
import dev.vality.daway.handler.dominant.DominantHandler;
import dev.vality.daway.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DominantService {

    private final DominantDao dominantDao;

    private final List<DominantHandler> handlers;

    @Transactional(propagation = Propagation.REQUIRED)
    public void processCommit(long versionId, Map.Entry<Long, Commit> entry) {
        List<Operation> operations = entry.getValue().getOps();
        operations.forEach(operation -> handlers.forEach(handler -> {
            if (handler.acceptAndSet(operation)) {
                processOperation(handler, operation, versionId);
            }
        }));
    }

    private void processOperation(DominantHandler handler, Operation operation, Long versionId) {
        try {
            log.info("Start to process commit with versionId={} operation={} ",
                    versionId, JsonUtil.thriftBaseToJsonString(operation));
            handler.handle(operation, versionId);
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
