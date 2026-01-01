package ContractGuard.ContractGuard.services.changeDetection.controller;

import ContractGuard.ContractGuard.services.changeDetection.dto.BreakingChangeResponse;
import ContractGuard.ContractGuard.services.changeDetection.dto.ChangeDetectionReport;
import ContractGuard.ContractGuard.services.changeDetection.dto.DetectChangesRequest;
import ContractGuard.ContractGuard.services.changeDetection.service.ChangeDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/change-detection")
@RequiredArgsConstructor
@Tag(name = "Change Detection", description = "Breaking change detection endpoints")
public class ChangeDetectionController {

    private final ChangeDetectionService changeDetectionService;

    @PostMapping("/detect")
    @Operation(summary = "Detect breaking changes between contract versions")
    public ResponseEntity<ChangeDetectionReport> detectChanges(
            @Valid @RequestBody DetectChangesRequest request) {
        ChangeDetectionReport report = changeDetectionService.detectChanges(request);
        return ResponseEntity.status(HttpStatus.OK).body(report);
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get all breaking changes for a contract")
    public ResponseEntity<List<BreakingChangeResponse>> getBreakingChanges(
            @PathVariable UUID contractId) {
        List<BreakingChangeResponse> changes = changeDetectionService.getBreakingChangesByContract(contractId);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/contract/{contractId}/versions")
    @Operation(summary = "Get breaking changes between two versions")
    public ResponseEntity<List<BreakingChangeResponse>> getChangesBetweenVersions(
            @PathVariable UUID contractId,
            @RequestParam String oldVersion,
            @RequestParam String newVersion) {
        List<BreakingChangeResponse> changes = changeDetectionService.getChangesBetweenVersions(
                contractId, oldVersion, newVersion);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/contract/{contractId}/severity/{severity}")
    @Operation(summary = "Get breaking changes by severity level")
    public ResponseEntity<List<BreakingChangeResponse>> getChangesBySeverity(
            @PathVariable UUID contractId,
            @PathVariable String severity) {
        List<BreakingChangeResponse> changes = changeDetectionService.getBreakingChangesBySeverity(
                contractId, severity);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/contract/{contractId}/version/{version}")
    @Operation(summary = "Get breaking changes for a specific contract version")
    public ResponseEntity<List<BreakingChangeResponse>> getChangesByVersion(
            @PathVariable UUID contractId,
            @PathVariable String version) {
        List<BreakingChangeResponse> changes = changeDetectionService.getBreakingChangesByVersion(
                contractId, version);
        return ResponseEntity.ok(changes);
    }

    @DeleteMapping("/contract/{contractId}")
    @Operation(summary = "Delete all breaking changes for a contract")
    public ResponseEntity<Void> deleteBreakingChanges(
            @PathVariable UUID contractId) {
        changeDetectionService.deleteBreakingChanges(contractId);
        return ResponseEntity.noContent().build();
    }
}
