package ContractGuard.ContractGuard.services.contract.controller;

import ContractGuard.ContractGuard.services.contract.dto.ContractResponse;
import ContractGuard.ContractGuard.services.contract.dto.CreateContractRequest;
import ContractGuard.ContractGuard.services.contract.service.impl.ContractServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "API Contract management endpoints")
public class ContractController {

    private final ContractServiceImpl contractServiceImpl;

    @PostMapping
    @Operation(summary = "Create a new API contract")
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody CreateContractRequest request) {
        ContractResponse response = contractServiceImpl.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{contractId}")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<ContractResponse> getContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(contractServiceImpl.getContract(contractId));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get all contracts for organization")
    public ResponseEntity<List<ContractResponse>> getContractsByOrganization(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(contractServiceImpl.getContractsByOrganization(organizationId));
    }

    @GetMapping
    @Operation(summary = "Get paginated contracts for organization")
    public ResponseEntity<Page<ContractResponse>> getContractsPaginated(
            @RequestParam UUID organizationId,
            Pageable pageable) {
        return ResponseEntity.ok(contractServiceImpl.getContractsByOrganizationPaginated(organizationId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get contracts by status")
    public ResponseEntity<List<ContractResponse>> getContractsByStatus(
            @RequestParam UUID organizationId,
            @PathVariable String status) {
        return ResponseEntity.ok(contractServiceImpl.getContractsByStatus(organizationId, status));
    }

    @GetMapping("/search")
    @Operation(summary = "Search contracts by name")
    public ResponseEntity<List<ContractResponse>> searchContracts(
            @RequestParam UUID organizationId,
            @RequestParam String searchTerm) {
        return ResponseEntity.ok(contractServiceImpl.searchContractsByName(organizationId, searchTerm));
    }

    @PutMapping("/{contractId}")
    @Operation(summary = "Update an existing contract")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody CreateContractRequest request) {
        return ResponseEntity.ok(contractServiceImpl.updateContract(contractId, request));
    }

    @DeleteMapping("/{contractId}")
    @Operation(summary = "Delete a contract")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID contractId) {
        contractServiceImpl.deleteContract(contractId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contractId}/publish")
    @Operation(summary = "Publish a contract (change status to ACTIVE)")
    public ResponseEntity<ContractResponse> publishContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(contractServiceImpl.publishContract(contractId));
    }

    @PostMapping("/{contractId}/deprecate")
    @Operation(summary = "Deprecate a contract")
    public ResponseEntity<ContractResponse> deprecateContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(contractServiceImpl.deprecateContract(contractId));
    }

    @PostMapping("/{contractId}/retire")
    @Operation(summary = "Retire a contract")
    public ResponseEntity<ContractResponse> retireContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(contractServiceImpl.retireContract(contractId));
    }
}
