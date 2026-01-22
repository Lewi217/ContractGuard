package ContractGuard.ContractGuard.services.changeDetection.repository;

import ContractGuard.ContractGuard.services.changeDetection.model.ChangeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChangeDetailRepository extends JpaRepository<ChangeDetail, UUID> {
    List<ChangeDetail> findByBreakingChangeId(UUID breakingChangeId);

    List<ChangeDetail> findByBreakingChangeIdAndChangeType(UUID breakingChangeId, String changeType);

    List<ChangeDetail> findByBreakingChangeIdAndSeverity(UUID breakingChangeId, String severity);
}

