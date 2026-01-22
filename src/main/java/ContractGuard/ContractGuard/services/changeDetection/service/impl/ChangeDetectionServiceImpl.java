package ContractGuard.ContractGuard.services.changeDetection.service.impl;

import ContractGuard.ContractGuard.services.changeDetection.dto.*;
import ContractGuard.ContractGuard.services.changeDetection.model.BreakingChangeDetailed;
import ContractGuard.ContractGuard.services.changeDetection.model.ChangeDetail;
import ContractGuard.ContractGuard.services.changeDetection.model.ImpactAnalysis;
import ContractGuard.ContractGuard.services.changeDetection.repository.BreakingChangeDetailedRepository;
import ContractGuard.ContractGuard.services.changeDetection.repository.ChangeDetailRepository;
import ContractGuard.ContractGuard.services.changeDetection.repository.ImpactAnalysisRepository;
import ContractGuard.ContractGuard.services.changeDetection.service.*;
import ContractGuard.ContractGuard.services.contract.model.BreakingChange;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.repository.BreakingChangeRepository;
import ContractGuard.ContractGuard.services.contract.repository.ContractRepository;
import ContractGuard.ContractGuard.services.consumer.repository.ConsumerRepository;
import ContractGuard.ContractGuard.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChangeDetectionServiceImpl implements ChangeDetectionService {

    private final BreakingChangeRepository breakingChangeRepository;
    private final BreakingChangeDetailedRepository breakingChangeDetailedRepository;
    private final ChangeDetailRepository changeDetailRepository;
    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final ContractRepository contractRepository;
    private final ConsumerRepository consumerRepository;
    private final OpenApiDiffEngine openApiDiffEngine;
    private final ImpactAnalyzer impactAnalyzer;
    private final MigrationGuideGenerator migrationGuideGenerator;

    @Override
    public ChangeDetectionReport detectChanges(DetectChangesRequest request) {
        log.info("Detecting changes for contract {} between versions {} and {}",
                request.getContractId(), request.getOldVersion(), request.getNewVersion());

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        JsonNode oldSpec = contract.getOpenapiSpec();
        JsonNode newSpec = contract.getOpenapiSpec();

        List<BreakingChange> detectedBreakingChanges = compareOpenAPISpecs(
                contract, request.getOldVersion(), request.getNewVersion(), oldSpec, newSpec
        );

        // Save all breaking changes
        breakingChangeRepository.saveAll(detectedBreakingChanges);

        // Build report
        return buildReport(contract, request.getOldVersion(), request.getNewVersion(), detectedBreakingChanges);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakingChangeResponse> getBreakingChangesByContract(UUID contractId) {
        log.info("Fetching breaking changes for contract: {}", contractId);
        return breakingChangeRepository.findByContractId(contractId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakingChangeResponse> getChangesBetweenVersions(UUID contractId, String oldVersion, String newVersion) {
        log.info("Fetching changes between versions {} and {} for contract {}", oldVersion, newVersion, contractId);
        return breakingChangeRepository.findChangesBetweenVersions(contractId, oldVersion, newVersion)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakingChangeResponse> getBreakingChangesBySeverity(UUID contractId, String severity) {
        log.info("Fetching breaking changes with severity {} for contract {}", severity, contractId);
        return breakingChangeRepository.findByContractIdAndSeverity(contractId, severity)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakingChangeResponse> getBreakingChangesByVersion(UUID contractId, String version) {
        log.info("Fetching breaking changes for contract {} version {}", contractId, version);
        return breakingChangeRepository.findByContractIdAndNewVersion(contractId, version)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBreakingChanges(UUID contractId) {
        log.info("Deleting breaking changes for contract: {}", contractId);
        List<BreakingChange> changes = breakingChangeRepository.findByContractId(contractId);
        breakingChangeRepository.deleteAll(changes);
        log.info("Deleted {} breaking changes for contract {}", changes.size(), contractId);
    }

    /**
     * Core logic: Compare two OpenAPI specifications and detect breaking changes
     */
    private List<BreakingChange> compareOpenAPISpecs(Contract contract, String oldVersion, String newVersion,
                                                     JsonNode oldSpec, JsonNode newSpec) {
        List<BreakingChange> breakingChanges = new ArrayList<>();

        // Compare paths (endpoints)
        breakingChanges.addAll(compareEndpoints(contract, oldVersion, newVersion, oldSpec, newSpec));

        // Compare schemas (data models)
        breakingChanges.addAll(compareSchemas(contract, oldVersion, newVersion, oldSpec, newSpec));

        // Compare parameters
        breakingChanges.addAll(compareParameters(contract, oldVersion, newVersion, oldSpec, newSpec));

        log.info("Detected {} breaking changes between versions {} and {}",
                breakingChanges.size(), oldVersion, newVersion);
        return breakingChanges;
    }

    /**
     * Compare endpoints and detect removed or method changes
     */
    private List<BreakingChange> compareEndpoints(Contract contract, String oldVersion, String newVersion,
                                                   JsonNode oldSpec, JsonNode newSpec) {
        List<BreakingChange> changes = new ArrayList<>();

        if (!oldSpec.has("paths") || !newSpec.has("paths")) {
            return changes;
        }

        JsonNode oldPaths = oldSpec.get("paths");
        JsonNode newPaths = newSpec.get("paths");

        // Check for removed endpoints
        oldPaths.fieldNames().forEachRemaining(endpoint -> {
            if (!newPaths.has(endpoint)) {
                BreakingChange change = BreakingChange.builder()
                        .contract(contract)
                        .oldVersion(oldVersion)
                        .newVersion(newVersion)
                        .changeType("ENDPOINT_REMOVED")
                        .severity("CRITICAL")
                        .description("Endpoint '" + endpoint + "' has been removed")
                        .affectedEndpoint(endpoint)
                        .migrationGuide("Endpoint '" + endpoint + "' is no longer available. Please update your integration.")
                        .build();
                changes.add(change);
                log.warn("Breaking change detected: Endpoint removed - {}", endpoint);
            }
        });

        // Check for method changes (GET -> POST, etc.)
        Iterator<Map.Entry<String, JsonNode>> iter = oldPaths.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            String endpoint = entry.getKey();
            if (newPaths.has(endpoint)) {
                JsonNode oldMethods = entry.getValue();
                JsonNode newMethods = newPaths.get(endpoint);

                oldMethods.fieldNames().forEachRemaining(method -> {
                    if (!newMethods.has(method)) {
                        BreakingChange change = BreakingChange.builder()
                                .contract(contract)
                                .oldVersion(oldVersion)
                                .newVersion(newVersion)
                                .changeType("METHOD_REMOVED")
                                .severity("HIGH")
                                .description("Method '" + method.toUpperCase() + "' removed from endpoint '" + endpoint + "'")
                                .affectedEndpoint(endpoint)
                                .affectedField(method)
                                .migrationGuide("The " + method.toUpperCase() + " method is no longer supported on " + endpoint)
                                .build();
                        changes.add(change);
                        log.warn("Breaking change detected: Method removed - {} {}", method.toUpperCase(), endpoint);
                    }
                });
            }
        }

        return changes;
    }

    /**
     * Compare schemas and detect field removals, type changes, and required field changes
     */
    private List<BreakingChange> compareSchemas(Contract contract, String oldVersion, String newVersion,
                                                 JsonNode oldSpec, JsonNode newSpec) {
        List<BreakingChange> changes = new ArrayList<>();

        if (!oldSpec.has("components") || !newSpec.has("components")) {
            return changes;
        }

        JsonNode oldSchemas = oldSpec.get("components").get("schemas");
        JsonNode newSchemas = newSpec.get("components").get("schemas");

        if (oldSchemas == null || newSchemas == null) {
            return changes;
        }

        // Check for removed schemas
        oldSchemas.fieldNames().forEachRemaining(schemaName -> {
            if (!newSchemas.has(schemaName)) {
                BreakingChange change = BreakingChange.builder()
                        .contract(contract)
                        .oldVersion(oldVersion)
                        .newVersion(newVersion)
                        .changeType("SCHEMA_REMOVED")
                        .severity("HIGH")
                        .description("Schema '" + schemaName + "' has been removed")
                        .affectedField(schemaName)
                        .migrationGuide("Schema '" + schemaName + "' is no longer available.")
                        .build();
                changes.add(change);
                log.warn("Breaking change detected: Schema removed - {}", schemaName);
            }
        });

        // Check for field-level changes
        Iterator<Map.Entry<String, JsonNode>> schemaIter = oldSchemas.fields();
        while (schemaIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = schemaIter.next();
            String schemaName = entry.getKey();
            if (newSchemas.has(schemaName)) {
                JsonNode oldSchema = entry.getValue();
                JsonNode newSchema = newSchemas.get(schemaName);

                if (oldSchema.has("properties") && newSchema.has("properties")) {
                    changes.addAll(compareSchemaProperties(contract, oldVersion, newVersion,
                            schemaName, oldSchema, newSchema));
                }
            }
        }

        return changes;
    }

    /**
     * Compare properties within schemas
     */
    private List<BreakingChange> compareSchemaProperties(Contract contract, String oldVersion, String newVersion,
                                                         String schemaName, JsonNode oldSchema, JsonNode newSchema) {
        List<BreakingChange> changes = new ArrayList<>();

        JsonNode oldProps = oldSchema.get("properties");
        JsonNode newProps = newSchema.get("properties");

        // Check for removed properties
        oldProps.fieldNames().forEachRemaining(fieldName -> {
            if (!newProps.has(fieldName)) {
                BreakingChange change = BreakingChange.builder()
                        .contract(contract)
                        .oldVersion(oldVersion)
                        .newVersion(newVersion)
                        .changeType("FIELD_REMOVED")
                        .severity("HIGH")
                        .description("Field '" + fieldName + "' removed from schema '" + schemaName + "'")
                        .affectedField(fieldName)
                        .migrationGuide("Field '" + fieldName + "' in schema '" + schemaName + "' has been removed.")
                        .build();
                changes.add(change);
                log.warn("Breaking change detected: Field removed - {}.{}", schemaName, fieldName);
            }
        });

        // Check for type changes
        Iterator<Map.Entry<String, JsonNode>> propsIter = oldProps.fields();
        while (propsIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = propsIter.next();
            String fieldName = entry.getKey();
            if (newProps.has(fieldName)) {
                JsonNode oldType = entry.getValue();
                JsonNode newType = newProps.get(fieldName);

                String oldTypeStr = getSchemaType(oldType);
                String newTypeStr = getSchemaType(newType);

                if (!oldTypeStr.equals(newTypeStr)) {
                    BreakingChange change = BreakingChange.builder()
                            .contract(contract)
                            .oldVersion(oldVersion)
                            .newVersion(newVersion)
                            .changeType("TYPE_CHANGED")
                            .severity("CRITICAL")
                            .description("Field '" + fieldName + "' type changed from '" + oldTypeStr + "' to '" + newTypeStr + "'")
                            .affectedField(fieldName)
                            .migrationGuide("Field '" + fieldName + "' type has changed from " + oldTypeStr + " to " + newTypeStr)
                            .build();
                    changes.add(change);
                    log.warn("Breaking change detected: Type changed - {}.{}: {} -> {}",
                            schemaName, fieldName, oldTypeStr, newTypeStr);
                }
            }
        }

        // Check for required field changes
        if (oldSchema.has("required") && newSchema.has("required")) {
            changes.addAll(compareRequiredFields(contract, oldVersion, newVersion, schemaName, oldSchema, newSchema));
        }

        return changes;
    }

    /**
     * Compare required fields - newly required fields are breaking changes
     */
    private List<BreakingChange> compareRequiredFields(Contract contract, String oldVersion, String newVersion,
                                                       String schemaName, JsonNode oldSchema, JsonNode newSchema) {
        List<BreakingChange> changes = new ArrayList<>();

        Set<String> oldRequired = getRequiredFields(oldSchema);
        Set<String> newRequired = getRequiredFields(newSchema);

        // Find fields that became required (breaking change)
        newRequired.forEach(field -> {
            if (!oldRequired.contains(field)) {
                BreakingChange change = BreakingChange.builder()
                        .contract(contract)
                        .oldVersion(oldVersion)
                        .newVersion(newVersion)
                        .changeType("FIELD_REQUIRED")
                        .severity("HIGH")
                        .description("Field '" + field + "' is now required in schema '" + schemaName + "'")
                        .affectedField(field)
                        .migrationGuide("Field '" + field + "' in schema '" + schemaName + "' is now required.")
                        .build();
                changes.add(change);
                log.warn("Breaking change detected: Field now required - {}.{}", schemaName, field);
            }
        });

        return changes;
    }

    /**
     * Compare request/response parameters
     */
    private List<BreakingChange> compareParameters(Contract contract, String oldVersion, String newVersion,
                                                   JsonNode oldSpec, JsonNode newSpec) {
        List<BreakingChange> changes = new ArrayList<>();

        if (!oldSpec.has("paths") || !newSpec.has("paths")) {
            return changes;
        }

        JsonNode oldPaths = oldSpec.get("paths");
        JsonNode newPaths = newSpec.get("paths");

        Iterator<Map.Entry<String, JsonNode>> pathIter = oldPaths.fields();
        while (pathIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = pathIter.next();
            String endpoint = entry.getKey();
            if (newPaths.has(endpoint)) {
                JsonNode oldMethods = entry.getValue();
                JsonNode newMethods = newPaths.get(endpoint);

                Iterator<Map.Entry<String, JsonNode>> methodIter = oldMethods.fields();
                while (methodIter.hasNext()) {
                    Map.Entry<String, JsonNode> methodEntry = methodIter.next();
                    String method = methodEntry.getKey();
                    if (newMethods.has(method)) {
                        JsonNode oldOperation = methodEntry.getValue();
                        JsonNode newOperation = newMethods.get(method);

                        if (oldOperation.has("parameters") && newOperation.has("parameters")) {
                            changes.addAll(compareOperationParameters(contract, oldVersion, newVersion,
                                    endpoint, method, oldOperation, newOperation));
                        }
                    }
                }
            }
        }

        return changes;
    }

    /**
     * Compare parameters for a specific operation
     */
    private List<BreakingChange> compareOperationParameters(Contract contract, String oldVersion, String newVersion,
                                                            String endpoint, String method,
                                                            JsonNode oldOperation, JsonNode newOperation) {
        List<BreakingChange> changes = new ArrayList<>();

        JsonNode oldParams = oldOperation.get("parameters");
        JsonNode newParams = newOperation.get("parameters");

        // Check for removed parameters
        oldParams.forEach(oldParam -> {
            String paramName = oldParam.get("name").asText();
            boolean found = false;

            for (JsonNode newParam : newParams) {
                if (newParam.get("name").asText().equals(paramName)) {
                    found = true;
                    break;
                }
            }

            if (!found && oldParam.has("required") && oldParam.get("required").asBoolean()) {
                BreakingChange change = BreakingChange.builder()
                        .contract(contract)
                        .oldVersion(oldVersion)
                        .newVersion(newVersion)
                        .changeType("PARAMETER_REMOVED")
                        .severity("HIGH")
                        .description("Required parameter '" + paramName + "' removed from " + method.toUpperCase() + " " + endpoint)
                        .affectedEndpoint(endpoint)
                        .affectedField(paramName)
                        .migrationGuide("Parameter '" + paramName + "' is no longer supported.")
                        .build();
                changes.add(change);
                log.warn("Breaking change detected: Parameter removed - {} {}: {}", method.toUpperCase(), endpoint, paramName);
            }
        });

        return changes;
    }

    /**
     * Extract type from schema property
     */
    private String getSchemaType(JsonNode schema) {
        if (schema.has("type")) {
            return schema.get("type").asText();
        } else if (schema.has("$ref")) {
            String ref = schema.get("$ref").asText();
            return ref.substring(ref.lastIndexOf("/") + 1);
        }
        return "unknown";
    }

    /**
     * Get required fields from a schema
     */
    private Set<String> getRequiredFields(JsonNode schema) {
        Set<String> required = new HashSet<>();
        if (schema.has("required")) {
            schema.get("required").forEach(field -> required.add(field.asText()));
        }
        return required;
    }

    /**
     * Build a comprehensive change detection report
     */
    private ChangeDetectionReport buildReport(Contract contract, String oldVersion, String newVersion,
                                              List<BreakingChange> changes) {
        long criticalCount = changes.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long highCount = changes.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        long mediumCount = changes.stream().filter(c -> "MEDIUM".equals(c.getSeverity())).count();
        long lowCount = changes.stream().filter(c -> "LOW".equals(c.getSeverity())).count();

        List<BreakingChangeResponse> responses = changes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        String summary = buildSummary(changes.size(), criticalCount, highCount, mediumCount, lowCount);

        return ChangeDetectionReport.builder()
                .contractId(contract.getId())
                .contractName(contract.getName())
                .oldVersion(oldVersion)
                .newVersion(newVersion)
                .totalChanges(changes.size())
                .criticalChanges((int) criticalCount)
                .highSeverityChanges((int) highCount)
                .mediumSeverityChanges((int) mediumCount)
                .lowSeverityChanges((int) lowCount)
                .breakingChanges(responses)
                .detectedAt(LocalDateTime.now())
                .summary(summary)
                .build();
    }

    /**
     * Build human-readable summary of changes
     */
    private String buildSummary(int total, long critical, long high, long medium, long low) {
        if (total == 0) {
            return "No breaking changes detected";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Detected ").append(total).append(" breaking change(s): ");

        if (critical > 0) summary.append(critical).append(" CRITICAL, ");
        if (high > 0) summary.append(high).append(" HIGH, ");
        if (medium > 0) summary.append(medium).append(" MEDIUM, ");
        if (low > 0) summary.append(low).append(" LOW");

        return summary.toString().replaceAll(", $", "");
    }

    /**
     * Map BreakingChange entity to DTO response
     */
    private BreakingChangeResponse mapToResponse(BreakingChange change) {
        return BreakingChangeResponse.builder()
                .id(change.getId())
                .contractId(change.getContract().getId())
                .oldVersion(change.getOldVersion())
                .newVersion(change.getNewVersion())
                .changeType(change.getChangeType())
                .severity(change.getSeverity())
                .description(change.getDescription())
                .affectedEndpoint(change.getAffectedEndpoint())
                .affectedField(change.getAffectedField())
                .migrationGuide(change.getMigrationGuide())
                .detectedAt(change.getDetectedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BreakingChangeDetailedResponse getBreakingChangeDetails(UUID breakingChangeId) {
        log.info("Fetching detailed information for breaking change: {}", breakingChangeId);
        BreakingChangeDetailed change = breakingChangeDetailedRepository.findById(breakingChangeId)
            .orElseThrow(() -> new ResourceNotFoundException("Breaking change not found"));

        return mapToDetailedResponse(change);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakingChangeDetailedResponse> getDetailedChangesBetweenVersions(UUID contractId, String oldVersion, String newVersion) {
        log.info("Fetching detailed changes between versions {} and {} for contract {}", oldVersion, newVersion, contractId);
        return breakingChangeDetailedRepository.findChangesBetweenVersions(contractId, oldVersion, newVersion)
            .stream()
            .map(this::mapToDetailedResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImpactAnalysisReportResponse analyzeImpactForChanges(UUID contractId, String oldVersion, String newVersion) {
        log.info("Analyzing impact for contract {} between versions {} and {}", contractId, oldVersion, newVersion);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        List<BreakingChangeDetailed> changes = breakingChangeDetailedRepository
            .findChangesBetweenVersions(contractId, oldVersion, newVersion);

        List<ImpactAnalysis> impactAnalyses = impactAnalysisRepository.findByContractId(contractId);

        long criticalCount = impactAnalyses.stream()
            .filter(ia -> "CRITICAL".equals(ia.getImpactLevel()))
            .count();
        long highCount = impactAnalyses.stream()
            .filter(ia -> "HIGH".equals(ia.getImpactLevel()))
            .count();
        long mediumCount = impactAnalyses.stream()
            .filter(ia -> "MEDIUM".equals(ia.getImpactLevel()))
            .count();
        long lowCount = impactAnalyses.stream()
            .filter(ia -> "LOW".equals(ia.getImpactLevel()))
            .count();

        int totalEffort = impactAnalyses.stream()
            .mapToInt(ia -> ia.getEstimatedMigrationEffort() != null ? ia.getEstimatedMigrationEffort() : 0)
            .sum();

        String deploymentApproach = impactAnalyzer.recommendDeploymentApproach(impactAnalyses);

        List<ImpactAnalysisResponse> responses = impactAnalyses.stream()
            .map(this::mapImpactAnalysisToResponse)
            .collect(Collectors.toList());

        return ImpactAnalysisReportResponse.builder()
            .contractId(contractId)
            .contractName(contract.getName())
            .oldVersion(oldVersion)
            .newVersion(newVersion)
            .totalImpactedConsumers(impactAnalyses.size())
            .criticalImpactCount((int) criticalCount)
            .highImpactCount((int) highCount)
            .mediumImpactCount((int) mediumCount)
            .lowImpactCount((int) lowCount)
            .estimatedTotalMigrationEffort(totalEffort)
            .impactAnalyses(responses)
            .recommendedDeploymentApproach(deploymentApproach)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImpactAnalysisResponse> getImpactAnalysis(UUID contractId) {
        log.info("Fetching impact analysis for contract: {}", contractId);
        return impactAnalysisRepository.findByContractId(contractId)
            .stream()
            .map(this::mapImpactAnalysisToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImpactAnalysisResponse> getImpactAnalysisByConsumer(UUID consumerId) {
        log.info("Fetching impact analysis for consumer: {}", consumerId);
        return impactAnalysisRepository.findConsumerImpactHistory(consumerId)
            .stream()
            .map(this::mapImpactAnalysisToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ImpactAnalysisResponse updateImpactAnalysisStatus(UUID impactAnalysisId, String status) {
        log.info("Updating impact analysis {} status to: {}", impactAnalysisId, status);
        ImpactAnalysis analysis = impactAnalysisRepository.findById(impactAnalysisId)
            .orElseThrow(() -> new ResourceNotFoundException("Impact analysis not found"));

        analysis.setStatus(status);
        ImpactAnalysis updated = impactAnalysisRepository.save(analysis);

        return mapImpactAnalysisToResponse(updated);
    }

    @Override
    public void generateAndSaveMigrationGuides(UUID contractId) {
        log.info("Generating migration guides for contract: {}", contractId);
        List<BreakingChangeDetailed> changes = breakingChangeDetailedRepository.findByContractId(contractId);

        for (BreakingChangeDetailed change : changes) {
            String migrationGuide = migrationGuideGenerator.generateMigrationGuide(change);
            String codeExample = migrationGuideGenerator.generateCodeExample(change);

            change.setMigrationGuide(migrationGuide);
            change.setCodeExample(codeExample);
            breakingChangeDetailedRepository.save(change);
        }

        log.info("Generated migration guides for {} changes", changes.size());
    }

    private BreakingChangeDetailedResponse mapToDetailedResponse(BreakingChangeDetailed change) {
        List<ChangeDetailResponse> changeDetails = change.getChangeDetails() != null ?
            change.getChangeDetails().stream()
                .map(cd -> ChangeDetailResponse.builder()
                    .id(cd.getId())
                    .breakingChangeId(change.getId())
                    .fieldName(cd.getFieldName())
                    .oldValue(cd.getOldValue())
                    .newValue(cd.getNewValue())
                    .changeType(cd.getChangeType())
                    .severity(cd.getSeverity())
                    .createdAt(cd.getCreatedAt())
                    .build())
                .collect(Collectors.toList())
            : new ArrayList<>();

        return BreakingChangeDetailedResponse.builder()
            .id(change.getId())
            .contractId(change.getContract().getId())
            .oldVersion(change.getOldVersion())
            .newVersion(change.getNewVersion())
            .changeType(change.getChangeType())
            .severity(change.getSeverity())
            .description(change.getDescription())
            .affectedEndpoint(change.getAffectedEndpoint())
            .affectedField(change.getAffectedField())
            .migrationGuide(change.getMigrationGuide())
            .codeExample(change.getCodeExample())
            .impactLevel(change.getImpactLevel())
            .deprecationPath(change.getDeprecationPath())
            .changeDetails(changeDetails)
            .detectedAt(change.getDetectedAt())
            .build();
    }

    private ImpactAnalysisResponse mapImpactAnalysisToResponse(ImpactAnalysis analysis) {
        String consumerName = analysis.getConsumer() != null ? analysis.getConsumer().getName() : "Unknown";

        return ImpactAnalysisResponse.builder()
            .id(analysis.getId())
            .contractId(analysis.getContract().getId())
            .breakingChangeId(analysis.getBreakingChange().getId())
            .consumerId(analysis.getConsumer().getId())
            .consumerName(consumerName)
            .impactScore(analysis.getImpactScore())
            .impactLevel(analysis.getImpactLevel())
            .status(analysis.getStatus())
            .affectedEndpoints(analysis.getAffectedEndpoints())
            .estimatedMigrationEffort(analysis.getEstimatedMigrationEffort())
            .createdAt(analysis.getCreatedAt())
            .updatedAt(analysis.getUpdatedAt())
            .build();
    }
}

