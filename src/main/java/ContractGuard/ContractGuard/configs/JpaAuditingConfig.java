package ContractGuard.ContractGuard.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing configuration for @CreationTimestamp and @UpdateTimestamp
}

