package ContractGuard.ContractGuard.services.validation.repository;

import ContractGuard.ContractGuard.services.validation.model.ValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
    List<ValidationResult> findByContractIdOrderByCreatedAtDesc(UUID contractId);

    Page<ValidationResult> findByContractIdOrderByCreatedAtDesc(UUID contractId, Pageable pageable);

    List<ValidationResult> findByStatus(String status);

    List<ValidationResult> findByContractIdAndStatus(UUID contractId, String status);

    Page<ValidationResult> findByContractIdAndStatus(UUID contractId, String status, Pageable pageable);

    List<ValidationResult> findByConsumerId(UUID consumerId);

    @Query("SELECT v FROM ValidationResult v WHERE v.contract.id = :contractId AND v.createdAt >= :since ORDER BY v.createdAt DESC")
    List<ValidationResult> findRecentValidations(@Param("contractId") UUID contractId, @Param("since") LocalDateTime since);

    @Query("SELECT v FROM ValidationResult v WHERE v.contract.id = :contractId AND v.passed = false ORDER BY v.createdAt DESC LIMIT 10")
    List<ValidationResult> findLatestFailures(@Param("contractId") UUID contractId);
}

