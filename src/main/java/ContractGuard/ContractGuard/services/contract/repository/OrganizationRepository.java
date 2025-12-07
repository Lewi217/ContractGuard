package ContractGuard.ContractGuard.services.contract.repository;

import ContractGuard.ContractGuard.services.contract.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlug(String slug);

    @Query("SELECT o FROM Organization o WHERE o.name LIKE %:searchTerm%")
    List<Organization> searchByName(@Param("searchTerm") String searchTerm);

    Optional<Organization> findByNameAndSlug(String name, String slug);
}

