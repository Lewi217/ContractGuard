package ContractGuard.ContractGuard.services.changeDetection.service;

import ContractGuard.ContractGuard.services.changeDetection.dto.*;

import java.util.List;
import java.util.UUID;

public interface ChangeDetectionService {

    ChangeDetectionReport detectChanges(DetectChangesRequest request);

    List<BreakingChangeResponse> getBreakingChangesByContract(UUID contractId);

    List<BreakingChangeResponse> getChangesBetweenVersions(UUID contractId, String oldVersion, String newVersion);

    List<BreakingChangeResponse> getBreakingChangesBySeverity(UUID contractId, String severity);

    List<BreakingChangeResponse> getBreakingChangesByVersion(UUID contractId, String version);

    void deleteBreakingChanges(UUID contractId);

    BreakingChangeDetailedResponse getBreakingChangeDetails(UUID breakingChangeId);

    List<BreakingChangeDetailedResponse> getDetailedChangesBetweenVersions(UUID contractId, String oldVersion, String newVersion);

    ImpactAnalysisReportResponse analyzeImpactForChanges(UUID contractId, String oldVersion, String newVersion);

    List<ImpactAnalysisResponse> getImpactAnalysis(UUID contractId);

    List<ImpactAnalysisResponse> getImpactAnalysisByConsumer(UUID consumerId);

    ImpactAnalysisResponse updateImpactAnalysisStatus(UUID impactAnalysisId, String status);

    void generateAndSaveMigrationGuides(UUID contractId);
}

