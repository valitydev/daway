package dev.vality.daway.util;

import dev.vality.damsel.domain.*;
import dev.vality.geck.common.util.TypeUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ContractUtil {
    public static List<dev.vality.daway.domain.tables.pojos.ContractAdjustment> convertContractAdjustments(Contract contract, long cntrctId) {
        return contract.getAdjustments().stream().map(ca -> convertContractAdjustment(ca, cntrctId))
                .collect(Collectors.toList());
    }

    public static dev.vality.daway.domain.tables.pojos.ContractAdjustment convertContractAdjustment(ContractAdjustment ca,
                                                                                                    long cntrctId) {
        var adjustment = new dev.vality.daway.domain.tables.pojos.ContractAdjustment();
        adjustment.setCntrctId(cntrctId);
        adjustment.setContractAdjustmentId(ca.getId());
        adjustment.setCreatedAt(TypeUtil.stringToLocalDateTime(ca.getCreatedAt()));
        if (ca.isSetValidSince()) {
            adjustment.setValidSince(TypeUtil.stringToLocalDateTime(ca.getValidSince()));
        }
        if (ca.isSetValidUntil()) {
            adjustment.setValidUntil(TypeUtil.stringToLocalDateTime(ca.getValidUntil()));
        }
        adjustment.setTermsId(ca.getTerms().getId());
        return adjustment;
    }

    public static void fillContractLegalAgreementFields(dev.vality.daway.domain.tables.pojos.Contract contract,
                                                        LegalAgreement legalAgreement) {
        contract.setLegalAgreementId(legalAgreement.getLegalAgreementId());
        contract.setLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
        if (legalAgreement.isSetValidUntil()) {
            contract.setLegalAgreementValidUntil(TypeUtil.stringToLocalDateTime(legalAgreement.getValidUntil()));
        }
    }

    public static void fillReportPreferences(dev.vality.daway.domain.tables.pojos.Contract contract,
                                             ServiceAcceptanceActPreferences serviceAcceptanceActPreferences) {
        contract.setReportActScheduleId(serviceAcceptanceActPreferences.getSchedule().getId());
        contract.setReportActSignerPosition(serviceAcceptanceActPreferences.getSigner().getPosition());
        contract.setReportActSignerFullName(serviceAcceptanceActPreferences.getSigner().getFullName());
        RepresentativeDocument representativeDocument =
                serviceAcceptanceActPreferences.getSigner().getDocument();
        var reportActSignerDocument =
                TypeUtil.toEnumField(representativeDocument.getSetField().getFieldName(), dev.vality.daway.domain.enums.RepresentativeDocument.class);
        if (reportActSignerDocument == null) {
            throw new IllegalArgumentException("Illegal representative document: " + representativeDocument);
        }
        contract.setReportActSignerDocument(reportActSignerDocument);
        if (representativeDocument.isSetPowerOfAttorney()) {
            contract.setReportActSignerDocPowerOfAttorneyLegalAgreementId(
                    representativeDocument.getPowerOfAttorney().getLegalAgreementId());
            contract.setReportActSignerDocPowerOfAttorneySignedAt(
                    TypeUtil.stringToLocalDateTime(representativeDocument.getPowerOfAttorney().getSignedAt()));
            if (representativeDocument.getPowerOfAttorney().isSetValidUntil()) {
                contract.setReportActSignerDocPowerOfAttorneyValidUntil(
                        TypeUtil.stringToLocalDateTime(representativeDocument.getPowerOfAttorney().getValidUntil()));
            }
        }
    }

    public static void setNullReportPreferences(dev.vality.daway.domain.tables.pojos.Contract contract) {
        contract.setReportActScheduleId(null);
        contract.setReportActSignerPosition(null);
        contract.setReportActSignerFullName(null);
        contract.setReportActSignerDocument(null);
        contract.setReportActSignerDocPowerOfAttorneyLegalAgreementId(null);
        contract.setReportActSignerDocPowerOfAttorneySignedAt(null);
        contract.setReportActSignerDocPowerOfAttorneyValidUntil(null);
    }

}
