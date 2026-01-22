package ContractGuard.ContractGuard.services.changeDetection.repository;

import ContractGuard.ContractGuard.services.changeDetection.model.ImpactAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImpactAnalysisRepository extends JpaRepository<ImpactAnalysis, UUID> {
    List<ImpactAnalysis> findByContractId(UUID contractId);

    List<ImpactAnalysis> findByBreakingChangeId(UUID breakingChangeId);

    List<ImpactAnalysis> findByConsumerId(UUID consumerId);

    List<ImpactAnalysis> findByImpactLevel(String impactLevel);

    List<ImpactAnalysis> findByStatus(String status);

    @Query("SELECT ia FROM ImpactAnalysis ia WHERE ia.contract.id = :contractId AND ia.impactLevel IN ('HIGH', 'CRITICAL')")
    List<ImpactAnalysis> findHighImpactAnalyses(@Param("contractId") UUID contractId);

    @Query("SELECT ia FROM ImpactAnalysis ia WHERE ia.breakingChange.id = :breakingChangeId AND ia.status = 'PENDING'")
    List<ImpactAnalysis> findPendingAnalyses(@Param("breakingChangeId") UUID breakingChangeId);

    @Query("SELECT ia FROM ImpactAnalysis ia WHERE ia.consumer.id = :consumerId ORDER BY ia.createdAt DESC")
    List<ImpactAnalysis> findConsumerImpactHistory(@Param("consumerId") UUID consumerId);

    @Query("SELECT COUNT(ia) FROM ImpactAnalysis ia WHERE ia.contract.id = :contractId AND ia.impactLevel = 'CRITICAL'")
    int countCriticalImpacts(@Param("contractId") UUID contractId);
}

