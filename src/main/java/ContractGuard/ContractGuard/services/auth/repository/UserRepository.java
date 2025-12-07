package ContractGuard.ContractGuard.services.auth.repository;

import ContractGuard.ContractGuard.services.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByOrganizationId(UUID organizationId);
    Optional<User> findByEmailAndOrganizationId(String email, UUID organizationId);
}

