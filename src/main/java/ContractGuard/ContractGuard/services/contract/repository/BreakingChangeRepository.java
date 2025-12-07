package ContractGuard.ContractGuard.services.contract.repository;

import ContractGuard.ContractGuard.services.contract.model.BreakingChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BreakingChangeRepository extends JpaRepository<BreakingChange, UUID> {
    List<BreakingChange> findByContractId(UUID contractId);

    List<BreakingChange> findByContractIdAndNewVersion(UUID contractId, String newVersion);

    List<BreakingChange> findByContractIdAndSeverity(UUID contractId, String severity);

    @Query("SELECT bc FROM BreakingChange bc WHERE bc.contract.id = :contractId AND bc.oldVersion = :oldVersion AND bc.newVersion = :newVersion")
    List<BreakingChange> findChangesBetweenVersions(@Param("contractId") UUID contractId, @Param("oldVersion") String oldVersion, @Param("newVersion") String newVersion);
}

