package ContractGuard.ContractGuard.services.consumer.model;

import ContractGuard.ContractGuard.services.contract.model.Contract;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumer_registrations", indexes = {
    @Index(name = "idx_registration_consumer", columnList = "consumer_id"),
    @Index(name = "idx_registration_contract", columnList = "contract_id"),
    @Index(name = "idx_consumer_reg_contract", columnList = "contract_id,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_registration_consumer"))
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_registration_contract"))
    private Contract contract;

    @Column(nullable = false, length = 50)
    private String versionSubscribedTo;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, DEPRECATED, MIGRATED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime subscribedAt;
}

