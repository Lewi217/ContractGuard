package ContractGuard.ContractGuard.services.consumer.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing consumer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConsumerRequest {
    @Size(min = 1, max = 255, message = "Consumer name must be between 1 and 255 characters")
    private String name;
    @Email(message = "Invalid email format")
    private String contactEmail;
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    private String contactName;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    @Size(max = 50, message = "Consumer type must not exceed 50 characters")
    private String consumerType;
}

