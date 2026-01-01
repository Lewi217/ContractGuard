package ContractGuard.ContractGuard.services.changeDetection.service;

import ContractGuard.ContractGuard.services.changeDetection.dto.BreakingChangeResponse;
import ContractGuard.ContractGuard.services.changeDetection.dto.ChangeDetectionReport;
import ContractGuard.ContractGuard.services.changeDetection.dto.DetectChangesRequest;

import java.util.List;
import java.util.UUID;

public interface ChangeDetectionService {

    ChangeDetectionReport detectChanges(DetectChangesRequest request);

    List<BreakingChangeResponse> getBreakingChangesByContract(UUID contractId);


    List<BreakingChangeResponse> getChangesBetweenVersions(UUID contractId, String oldVersion, String newVersion);


    List<BreakingChangeResponse> getBreakingChangesBySeverity(UUID contractId, String severity);


    List<BreakingChangeResponse> getBreakingChangesByVersion(UUID contractId, String version);

    void deleteBreakingChanges(UUID contractId);
}

