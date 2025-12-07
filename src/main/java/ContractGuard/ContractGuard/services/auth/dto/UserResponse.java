package ContractGuard.ContractGuard.services.auth.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
}

