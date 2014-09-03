package org.bahmni.module.admin.encounter;

import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.observation.DiagnosisMapper;
import org.bahmni.module.admin.observation.ObservationMapper;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BahmniEncounterTransactionImportService {

    private EncounterService encounterService;
    private final ObservationMapper observationService;
    private final DiagnosisMapper diagnosisService;

    public BahmniEncounterTransactionImportService(EncounterService encounterService,
                                                   ObservationMapper observationService, DiagnosisMapper diagnosisService) {
        this.encounterService = encounterService;
        this.observationService = observationService;
        this.diagnosisService = diagnosisService;
    }

    public List<BahmniEncounterTransaction> getBahmniEncounterTransaction(MultipleEncounterRow multipleEncounterRow, Patient patient) throws ParseException {
        if (multipleEncounterRow.encounterRows == null || multipleEncounterRow.encounterRows.isEmpty())
            return new ArrayList<>();

        List<BahmniEncounterTransaction> bahmniEncounterTransactions = new ArrayList<>();

        EncounterType requestedEncounterType = encounterService.getEncounterType(multipleEncounterRow.encounterType);
        if (requestedEncounterType == null) {
            throw new RuntimeException("Encounter type:'" + multipleEncounterRow.encounterType + "' not found.");
        }
        String encounterType = multipleEncounterRow.encounterType;
        String visitType = multipleEncounterRow.visitType;

        for (EncounterRow encounterRow : multipleEncounterRow.getNonEmptyEncounterRows()) {
            List<EncounterTransaction.Observation> allObservations = observationService.getObservations(encounterRow);
            List<BahmniDiagnosisRequest> allDiagnosis = diagnosisService.getBahmniDiagnosis(encounterRow);

            BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
            bahmniEncounterTransaction.setPatientUuid(patient.getUuid());
            bahmniEncounterTransaction.setBahmniDiagnoses(allDiagnosis);
            bahmniEncounterTransaction.setObservations(allObservations);

            bahmniEncounterTransaction.setEncounterDateTime(encounterRow.getEncounterDate());
            bahmniEncounterTransaction.setEncounterType(encounterType);
            bahmniEncounterTransaction.setVisitType(visitType);

            bahmniEncounterTransactions.add(bahmniEncounterTransaction);
        }


        return bahmniEncounterTransactions;
    }

}