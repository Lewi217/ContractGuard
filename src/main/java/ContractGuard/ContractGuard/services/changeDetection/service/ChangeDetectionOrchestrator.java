package ContractGuard.ContractGuard.services.changeDetection.service;

import ContractGuard.ContractGuard.services.changeDetection.model.BreakingChangeDetailed;
import ContractGuard.ContractGuard.services.changeDetection.model.ChangeDetail;
import ContractGuard.ContractGuard.services.changeDetection.model.ImpactAnalysis;
import ContractGuard.ContractGuard.services.changeDetection.repository.BreakingChangeDetailedRepository;
import ContractGuard.ContractGuard.services.changeDetection.repository.ChangeDetailRepository;
import ContractGuard.ContractGuard.services.changeDetection.repository.ImpactAnalysisRepository;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChangeDetectionOrchestrator {

    private final BreakingChangeDetailedRepository breakingChangeDetailedRepository;
    private final ChangeDetailRepository changeDetailRepository;
    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final OpenApiDiffEngine openApiDiffEngine;
    private final ImpactAnalyzer impactAnalyzer;
    private final MigrationGuideGenerator migrationGuideGenerator;
    private final ObjectMapper objectMapper;

    public List<BreakingChangeDetailed> orchestrateFullDetection(
        Contract contract,
        String oldVersion,
        String newVersion,
        List<Consumer> consumers) {

        log.info("Orchestrating full change detection for contract {} between versions {} and {}",
            contract.getId(), oldVersion, newVersion);

        List<BreakingChangeDetailed> breakingChanges = new ArrayList<>();

        List<OpenApiDiffEngine.EndpointChange> endpointChanges =
            openApiDiffEngine.compareEndpoints(contract.getOpenapiSpec(), contract.getOpenapiSpec());

        List<OpenApiDiffEngine.SchemaChange> schemaChanges =
            openApiDiffEngine.compareSchemas(contract.getOpenapiSpec(), contract.getOpenapiSpec());

        breakingChanges.addAll(convertEndpointChanges(contract, oldVersion, newVersion, endpointChanges));
        breakingChanges.addAll(convertSchemaChanges(contract, oldVersion, newVersion, schemaChanges));

        breakingChangeDetailedRepository.saveAll(breakingChanges);

        for (BreakingChangeDetailed change : breakingChanges) {
            List<ImpactAnalysis> impacts = impactAnalyzer.analyzeImpact(contract, change, consumers);
            impactAnalysisRepository.saveAll(impacts);

            String migrationGuide = migrationGuideGenerator.generateMigrationGuide(change);
            String codeExample = migrationGuideGenerator.generateCodeExample(change);
            change.setMigrationGuide(migrationGuide);
            change.setCodeExample(codeExample);
            breakingChangeDetailedRepository.save(change);
        }

        log.info("Detected and analyzed {} breaking changes", breakingChanges.size());
        return breakingChanges;
    }

    private List<BreakingChangeDetailed> convertEndpointChanges(
        Contract contract,
        String oldVersion,
        String newVersion,
        List<OpenApiDiffEngine.EndpointChange> endpointChanges) {

        List<BreakingChangeDetailed> changes = new ArrayList<>();

        for (OpenApiDiffEngine.EndpointChange ec : endpointChanges) {
            String impactLevel = "ENDPOINT_REMOVED".equals(ec.getChangeType()) ? "CRITICAL" : "HIGH";

            BreakingChangeDetailed change = BreakingChangeDetailed.builder()
                .oldVersion(oldVersion)
                .newVersion(newVersion)
                .changeType(ec.getChangeType())
                .severity(ec.getSeverity())
                .description(ec.getDescription())
                .affectedEndpoint(ec.getEndpoint())
                .affectedField(ec.getMethod())
                .impactLevel(impactLevel)
                .contract(contract)
                .build();

            changes.add(change);
        }

        return changes;
    }

    private List<BreakingChangeDetailed> convertSchemaChanges(
        Contract contract,
        String oldVersion,
        String newVersion,
        List<OpenApiDiffEngine.SchemaChange> schemaChanges) {

        List<BreakingChangeDetailed> changes = new ArrayList<>();

        for (OpenApiDiffEngine.SchemaChange sc : schemaChanges) {
            String impactLevel = "CRITICAL".equals(sc.getSeverity()) ? "CRITICAL" : "HIGH";

            BreakingChangeDetailed change = BreakingChangeDetailed.builder()
                .oldVersion(oldVersion)
                .newVersion(newVersion)
                .changeType(sc.getChangeType())
                .severity(sc.getSeverity())
                .description(sc.getDescription())
                .affectedField(sc.getFieldName())
                .impactLevel(impactLevel)
                .contract(contract)
                .additionalContext(objectMapper.createObjectNode()
                    .put("schemaName", sc.getSchemaName())
                    .put("oldValue", sc.getOldValue() != null ? sc.getOldValue() : "")
                    .put("newValue", sc.getNewValue() != null ? sc.getNewValue() : ""))
                .build();

            changes.add(change);
        }

        return changes;
    }
}

