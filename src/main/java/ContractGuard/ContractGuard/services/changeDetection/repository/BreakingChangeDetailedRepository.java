package ContractGuard.ContractGuard.services.changeDetection.repository;

import ContractGuard.ContractGuard.services.changeDetection.model.BreakingChangeDetailed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BreakingChangeDetailedRepository extends JpaRepository<BreakingChangeDetailed, UUID> {
    List<BreakingChangeDetailed> findByContractId(UUID contractId);

    List<BreakingChangeDetailed> findByContractIdAndNewVersion(UUID contractId, String newVersion);

    List<BreakingChangeDetailed> findByContractIdAndSeverity(UUID contractId, String severity);

    List<BreakingChangeDetailed> findByContractIdAndImpactLevel(UUID contractId, String impactLevel);

    @Query("SELECT bcd FROM BreakingChangeDetailed bcd WHERE bcd.contract.id = :contractId AND bcd.oldVersion = :oldVersion AND bcd.newVersion = :newVersion")
    List<BreakingChangeDetailed> findChangesBetweenVersions(@Param("contractId") UUID contractId, @Param("oldVersion") String oldVersion, @Param("newVersion") String newVersion);

    @Query("SELECT bcd FROM BreakingChangeDetailed bcd WHERE bcd.contract.id = :contractId AND bcd.detectedAt >= :since ORDER BY bcd.detectedAt DESC")
    List<BreakingChangeDetailed> findRecentChanges(@Param("contractId") UUID contractId, @Param("since") LocalDateTime since);

    @Query("SELECT bcd FROM BreakingChangeDetailed bcd WHERE bcd.contract.id = :contractId AND bcd.severity IN ('CRITICAL', 'HIGH') ORDER BY bcd.detectedAt DESC")
    List<BreakingChangeDetailed> findCriticalChanges(@Param("contractId") UUID contractId);
}

