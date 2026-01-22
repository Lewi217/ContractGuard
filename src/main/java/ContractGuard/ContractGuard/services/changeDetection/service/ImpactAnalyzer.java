package ContractGuard.ContractGuard.services.changeDetection.service;

import ContractGuard.ContractGuard.services.changeDetection.model.BreakingChangeDetailed;
import ContractGuard.ContractGuard.services.changeDetection.model.ImpactAnalysis;
import ContractGuard.ContractGuard.services.changeDetection.repository.ImpactAnalysisRepository;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.consumer.repository.ConsumerRepository;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImpactAnalyzer {

    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final ConsumerRepository consumerRepository;

    public List<ImpactAnalysis> analyzeImpact(Contract contract, BreakingChangeDetailed change, List<Consumer> consumers) {
        List<ImpactAnalysis> impactAnalyses = new ArrayList<>();

        int baseScore = calculateBaseScore(change);

        for (Consumer consumer : consumers) {
            int impactScore = calculateConsumerImpactScore(baseScore, consumer, change);
            String impactLevel = determineImpactLevel(impactScore);
            int estimatedEffort = estimateMigrationEffort(change, consumer);

            ImpactAnalysis analysis = ImpactAnalysis.builder()
                .contract(contract)
                .breakingChange(change)
                .consumer(consumer)
                .impactScore(impactScore)
                .impactLevel(impactLevel)
                .status("PENDING")
                .affectedEndpoints(extractAffectedEndpoints(change))
                .estimatedMigrationEffort(estimatedEffort)
                .build();

            impactAnalyses.add(analysis);
        }

        return impactAnalyses;
    }

    private int calculateBaseScore(BreakingChangeDetailed change) {
        int score = 0;

        score = switch (change.getSeverity()) {
            case "CRITICAL" -> 100;
            case "HIGH" -> 75;
            case "MEDIUM" -> 50;
            case "LOW" -> 25;
            default -> 0;
        };

        switch (change.getChangeType()) {
            case "ENDPOINT_REMOVED" -> score += 50;
            case "METHOD_REMOVED" -> score += 40;
            case "FIELD_REMOVED" -> score += 30;
            case "TYPE_CHANGED" -> score += 35;
            case "FIELD_REQUIRED" -> score += 25;
        }

        return Math.min(score, 100);
    }

    private int calculateConsumerImpactScore(int baseScore, Consumer consumer, BreakingChangeDetailed change) {
        int score = baseScore;

        if (consumer.getContractVersions() != null && !consumer.getContractVersions().isEmpty()) {
            score = Math.min(baseScore + 20, 100);
        }

        return score;
    }

    private String determineImpactLevel(int impactScore) {
        if (impactScore >= 80) {
            return "CRITICAL";
        } else if (impactScore >= 60) {
            return "HIGH";
        } else if (impactScore >= 40) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private int estimateMigrationEffort(BreakingChangeDetailed change, Consumer consumer) {
        int baseEffort = 2;

        switch (change.getChangeType()) {
            case "ENDPOINT_REMOVED" -> baseEffort = 8;
            case "METHOD_REMOVED" -> baseEffort = 6;
            case "FIELD_REMOVED" -> baseEffort = 4;
            case "TYPE_CHANGED" -> baseEffort = 5;
            case "FIELD_REQUIRED" -> baseEffort = 3;
        }

        if (consumer.getContractVersions() != null && consumer.getContractVersions().size() > 1) {
            baseEffort += 2;
        }

        return Math.min(baseEffort, 10);
    }

    private String extractAffectedEndpoints(BreakingChangeDetailed change) {
        if (change.getAffectedEndpoint() != null) {
            return change.getAffectedEndpoint();
        }
        return "Multiple endpoints";
    }

    public int calculateAggregateImpactScore(List<ImpactAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return 0;
        }

        int total = analyses.stream()
            .mapToInt(ImpactAnalysis::getImpactScore)
            .sum();

        return total / analyses.size();
    }

    public long countImpactsByLevel(List<ImpactAnalysis> analyses, String level) {
        return analyses.stream()
            .filter(ia -> level.equals(ia.getImpactLevel()))
            .count();
    }

    public String recommendDeploymentApproach(List<ImpactAnalysis> analyses) {
        long criticalCount = countImpactsByLevel(analyses, "CRITICAL");
        long highCount = countImpactsByLevel(analyses, "HIGH");

        if (criticalCount > 0) {
            return "BLOCK_DEPLOYMENT - Critical impacts detected. Coordinate with affected consumers before deployment.";
        } else if (highCount >= 3) {
            return "REQUIRE_APPROVAL - High impacts detected. Requires approval from affected teams and coordinator.";
        } else if (highCount > 0) {
            return "NOTIFY_CONSUMERS - High impacts detected. Send advance notifications to affected consumers (48 hours).";
        } else {
            return "PROCEED_WITH_CAUTION - Minor impacts detected. Proceed but monitor closely and provide support.";
        }
    }
}

