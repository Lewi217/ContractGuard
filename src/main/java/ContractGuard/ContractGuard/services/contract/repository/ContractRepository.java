package ContractGuard.ContractGuard.services.contract.repository;

import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.model.ContractVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByOrganizationId(UUID organizationId);

    List<Contract> findByOrganizationIdAndStatus(UUID organizationId, String status);

    Page<Contract> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Contract> findByOrganizationIdAndStatus(UUID organizationId, String status, Pageable pageable);

    Optional<Contract> findByNameAndVersionAndOrganizationId(String name, String version, UUID organizationId);

    @Query("SELECT c FROM Contract c WHERE c.organization.id = :orgId AND c.name LIKE %:searchTerm%")
    List<Contract> searchByName(@Param("orgId") UUID organizationId, @Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Contract c WHERE c.organization.id = :orgId AND c.name LIKE %:searchTerm%")
    Page<Contract> searchByNamePaginated(@Param("orgId") UUID organizationId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "SELECT * FROM contracts c WHERE c.organization_id = :orgId AND LOWER(c.tags::text) LIKE LOWER(CONCAT('%', :tag, '%'))", nativeQuery = true)
    List<Contract> findByOrganizationIdAndTag(@Param("orgId") UUID organizationId, @Param("tag") String tag);

    @Repository
    interface ContractVersionRepository extends JpaRepository<ContractVersion, UUID> {
        List<ContractVersion> findByContractId(UUID contractId);

        Optional<ContractVersion> findByContractIdAndVersionNumber(UUID contractId, String versionNumber);
    }
}