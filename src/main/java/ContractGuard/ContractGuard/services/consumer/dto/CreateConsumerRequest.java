package ContractGuard.ContractGuard.services.consumer.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConsumerRequest {
    @NotBlank(message = "Consumer name is required")
    @Size(min = 1, max = 255, message = "Consumer name must be between 1 and 255 characters")
    private String name;
    @NotBlank(message = "Organization ID is required")
    private UUID organizationId;
    @Email(message = "Invalid email format")
    private String contactEmail;
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    private String contactName;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    @Builder.Default
    @Size(max = 50, message = "Consumer type must not exceed 50 characters")
    private String consumerType = "SERVICE";
}

