package ContractGuard.ContractGuard.services.auth.service;

import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.services.auth.dto.*;
import ContractGuard.ContractGuard.services.auth.model.User;
import ContractGuard.ContractGuard.services.contract.dto.OrganizationResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    boolean validateToken(String token);
    UserResponse mapUserToResponse(User user);
    OrganizationResponse mapOrgToResponse(Organization org);

}
