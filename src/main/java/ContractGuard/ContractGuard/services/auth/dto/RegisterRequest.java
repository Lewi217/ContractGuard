package ContractGuard.ContractGuard.services.auth.dto;

import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Organization slug is required")
    @Size(min = 3, max = 50, message = "Slug should be between 3 and 50 characters")
    private String organizationSlug;
}

