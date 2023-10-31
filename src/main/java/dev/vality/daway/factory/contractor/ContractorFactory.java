package dev.vality.daway.factory.contractor;

import dev.vality.damsel.domain.InternationalLegalEntity;
import dev.vality.damsel.domain.RussianLegalEntity;
import dev.vality.damsel.domain.RussianPrivateEntity;
import dev.vality.daway.domain.enums.ContractorType;
import dev.vality.daway.domain.enums.LegalEntity;
import dev.vality.daway.domain.enums.PrivateEntity;
import dev.vality.daway.domain.tables.pojos.Contractor;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractorFactory {

    public static Contractor build(long sequenceId, String eventCreatedAt, String partyId,
                                   dev.vality.damsel.domain.Contractor contractorSource,
                                   String contractorId, Integer changeId, Integer claimEffectId) {
        Contractor contractor = new Contractor();
        contractor.setSequenceId((int) sequenceId);
        contractor.setChangeId(changeId);
        contractor.setClaimEffectId(claimEffectId);
        contractor.setEventCreatedAt(TypeUtil.stringToLocalDateTime(eventCreatedAt));
        contractor.setPartyId(partyId);
        contractor.setContractorId(contractorId);
        contractor.setType(TBaseUtil.unionFieldToEnum(contractorSource, ContractorType.class));
        if (contractorSource.isSetRegisteredUser()) {
            contractor.setRegisteredUserEmail(contractorSource.getRegisteredUser().getEmail());
        } else if (contractorSource.isSetLegalEntity()) {
            contractor.setLegalEntity(TBaseUtil.unionFieldToEnum(contractorSource.getLegalEntity(), LegalEntity.class));
            if (contractorSource.getLegalEntity().isSetRussianLegalEntity()) {
                RussianLegalEntity russianLegalEntity = contractorSource.getLegalEntity().getRussianLegalEntity();
                contractor.setRussianLegalEntityRegisteredName(russianLegalEntity.getRegisteredName());
                contractor.setRussianLegalEntityRegisteredNumber(russianLegalEntity.getRegisteredNumber());
                contractor.setRussianLegalEntityInn(russianLegalEntity.getInn());
                contractor.setRussianLegalEntityActualAddress(russianLegalEntity.getActualAddress());
                contractor.setRussianLegalEntityPostAddress(russianLegalEntity.getPostAddress());
                contractor.setRussianLegalEntityRepresentativePosition(russianLegalEntity.getRepresentativePosition());
                contractor.setRussianLegalEntityRepresentativeFullName(russianLegalEntity.getRepresentativeFullName());
                contractor.setRussianLegalEntityRepresentativeDocument(russianLegalEntity.getRepresentativeDocument());
                contractor.setRussianLegalEntityRussianBankAccount(
                        russianLegalEntity.getRussianBankAccount().getAccount());
                contractor
                        .setRussianLegalEntityRussianBankName(russianLegalEntity.getRussianBankAccount().getBankName());
                contractor.setRussianLegalEntityRussianBankPostAccount(
                        russianLegalEntity.getRussianBankAccount().getBankPostAccount());
                contractor.setRussianLegalEntityRussianBankBik(russianLegalEntity.getRussianBankAccount().getBankBik());
            } else if (contractorSource.getLegalEntity().isSetInternationalLegalEntity()) {
                InternationalLegalEntity internationalLegalEntity =
                        contractorSource.getLegalEntity().getInternationalLegalEntity();
                contractor.setInternationalLegalEntityLegalName(internationalLegalEntity.getLegalName());
                contractor.setInternationalLegalEntityTradingName(internationalLegalEntity.getTradingName());
                contractor
                        .setInternationalLegalEntityRegisteredAddress(internationalLegalEntity.getRegisteredAddress());
                contractor.setInternationalLegalEntityActualAddress(internationalLegalEntity.getActualAddress());
                contractor.setInternationalLegalEntityRegisteredNumber(internationalLegalEntity.getRegisteredNumber());
                if (internationalLegalEntity.isSetCountry()) {
                    contractor.setInternationalLegalEntityCountryCode(
                            internationalLegalEntity.getCountry().getId().name());
                }
            }
        } else if (contractorSource.isSetPrivateEntity()) {
            contractor.setPrivateEntity(
                    TBaseUtil.unionFieldToEnum(contractorSource.getPrivateEntity(), PrivateEntity.class));
            if (contractorSource.getPrivateEntity().isSetRussianPrivateEntity()) {
                RussianPrivateEntity russianPrivateEntity =
                        contractorSource.getPrivateEntity().getRussianPrivateEntity();
                contractor.setRussianPrivateEntityFirstName(russianPrivateEntity.getFirstName());
                contractor.setRussianPrivateEntitySecondName(russianPrivateEntity.getSecondName());
                contractor.setRussianPrivateEntityMiddleName(russianPrivateEntity.getMiddleName());
                contractor.setRussianPrivateEntityPhoneNumber(russianPrivateEntity.getContactInfo().getPhoneNumber());
                contractor.setRussianPrivateEntityEmail(russianPrivateEntity.getContactInfo().getEmail());
            }
        }
        return contractor;
    }

}
