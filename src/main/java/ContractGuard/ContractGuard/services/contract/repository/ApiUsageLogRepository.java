package ContractGuard.ContractGuard.services.contract.repository;

import ContractGuard.ContractGuard.services.contract.model.ApiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, UUID> {
    List<ApiUsageLog> findByContractId(UUID contractId);

    List<ApiUsageLog> findByConsumerId(UUID consumerId);

    @Query("SELECT a FROM ApiUsageLog a WHERE a.contract.id = :contractId AND a.createdAt >= :since")
    List<ApiUsageLog> findByContractIdAndDateRange(@Param("contractId") UUID contractId, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM ApiUsageLog a WHERE a.contract.id = :contractId AND a.endpointPath = :endpoint")
    List<ApiUsageLog> findByContractIdAndEndpoint(@Param("contractId") UUID contractId, @Param("endpoint") String endpoint);
}

