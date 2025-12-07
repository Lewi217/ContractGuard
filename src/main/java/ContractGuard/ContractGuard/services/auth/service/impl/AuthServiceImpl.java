package ContractGuard.ContractGuard.services.auth.service.impl;

import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.services.auth.dto.*;
import ContractGuard.ContractGuard.services.auth.model.User;
import ContractGuard.ContractGuard.services.contract.dto.OrganizationResponse;
import ContractGuard.ContractGuard.shared.exception.BadRequestException;
import ContractGuard.ContractGuard.services.contract.repository.OrganizationRepository;
import ContractGuard.ContractGuard.services.auth.repository.UserRepository;
import ContractGuard.ContractGuard.configs.security.JwtProvider;
import ContractGuard.ContractGuard.services.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Register a new user and organization
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        // Check if organization slug already exists
        Optional<Organization> existingOrg = organizationRepository.findBySlug(request.getOrganizationSlug());
        if (existingOrg.isPresent()) {
            throw new BadRequestException("Organization slug already exists");
        }

        // Create organization
        Organization organization = Organization.builder()
            .name(request.getOrganizationName())
            .slug(request.getOrganizationSlug())
            .plan("FREE")
            .build();

        Organization savedOrg = organizationRepository.save(organization);
        log.info("Organization created: {}", savedOrg.getId());

        // Create user
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .organization(savedOrg)
            .role("ADMIN") // First user is admin
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        // Generate JWT token
        String token = jwtProvider.generateToken(savedUser.getId(), savedUser.getEmail());

        return RegisterResponse.builder()
            .user(mapUserToResponse(savedUser))
            .organization(mapOrgToResponse(savedOrg))
            .token(token)
            .build();
    }

    /**
     * Login user
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("User account is inactive");
        }

        // Generate JWT token
        String token = jwtProvider.generateToken(user.getId(), user.getEmail());

        log.info("User logged in successfully: {}", user.getId());

        return LoginResponse.builder()
            .token(token)
            .user(mapUserToResponse(user))
            .organization(mapOrgToResponse(user.getOrganization()))
            .build();
    }

    /**
     * Validate JWT token
     */
    @Override
    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token);
    }
    @Override
    public UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .build();
    }

    @Override
    public OrganizationResponse mapOrgToResponse(Organization org) {
        return OrganizationResponse.builder()
            .id(org.getId())
            .name(org.getName())
            .slug(org.getSlug())
            .description(org.getDescription())
            .plan(org.getPlan())
            .build();
    }
}

