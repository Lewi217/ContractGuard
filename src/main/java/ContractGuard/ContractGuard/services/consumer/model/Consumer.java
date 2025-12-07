package ContractGuard.ContractGuard.services.consumer.model;

import ContractGuard.ContractGuard.services.contract.model.Organization;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumers", indexes = {
    @Index(name = "idx_consumer_org", columnList = "organization_id"),
    @Index(name = "idx_consumer_api_key", columnList = "api_key"),
    @Index(name = "idx_consumer_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_consumer_org"))
    private Organization organization;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 500)
    private String apiKey;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 255)
    private String contactName;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String consumerType = "SERVICE"; // SERVICE, WEB_APP, MOBILE_APP, THIRD_PARTY

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

