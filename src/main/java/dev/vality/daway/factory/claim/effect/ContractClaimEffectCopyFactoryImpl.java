package dev.vality.daway.factory.claim.effect;

import dev.vality.daway.domain.tables.pojos.Contract;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class ContractClaimEffectCopyFactoryImpl implements ClaimEffectCopyFactory<Contract, Integer> {

    @Override
    public Contract create(MachineEvent event, long sequenceId, Integer claimEffectId, Integer id,
                           Contract withdrawalSessionOld) {
        Contract contract = null;
        if (withdrawalSessionOld != null) {
            contract = new Contract(withdrawalSessionOld);
        } else {
            contract = new Contract();
        }
        contract.setId(null);
        contract.setWtime(null);
        contract.setSequenceId((int) sequenceId);
        contract.setChangeId(id);
        contract.setClaimEffectId(claimEffectId);
        contract.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        return contract;
    }

}
