package ContractGuard.ContractGuard.services.consumer.controller;

import ContractGuard.ContractGuard.services.consumer.dto.CreateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.dto.UpdateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.dto.ConsumerResponse;
import ContractGuard.ContractGuard.services.consumer.service.impl.ConsumerServiceImpl;
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
@RequestMapping("/api/v1/consumers")
@RequiredArgsConstructor
@Tag(name = "Consumers", description = "API Consumer management endpoints")
public class ConsumerController {

    private final ConsumerServiceImpl consumerServiceImpl;

    @PostMapping
    @Operation(summary = "Register a new consumer")
    public ResponseEntity<ConsumerResponse> registerConsumer(
        @Valid @RequestBody CreateConsumerRequest request) {
        ConsumerResponse consumer = consumerServiceImpl.registerConsumer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consumer);
    }

    @GetMapping("/{consumerId}")
    @Operation(summary = "Get consumer by ID")
    public ResponseEntity<ConsumerResponse> getConsumer(@PathVariable UUID consumerId) {
        ConsumerResponse consumer = consumerServiceImpl.getConsumer(consumerId);
        return ResponseEntity.ok(consumer);
    }

    @GetMapping
    @Operation(summary = "Get all consumers for organization")
    public ResponseEntity<List<ConsumerResponse>> getConsumersByOrganization(@RequestParam UUID organizationId) {
        List<ConsumerResponse> consumers = consumerServiceImpl.getConsumersByOrganization(organizationId);
        return ResponseEntity.ok(consumers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active consumers for organization")
    public ResponseEntity<List<ConsumerResponse>> getActiveConsumers(@RequestParam UUID organizationId) {
        List<ConsumerResponse> consumers = consumerServiceImpl.getActiveConsumers(organizationId);
        return ResponseEntity.ok(consumers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search consumers by name")
    public ResponseEntity<List<ConsumerResponse>> searchConsumers(
        @RequestParam UUID organizationId,
        @RequestParam String searchTerm) {
        List<ConsumerResponse> consumers = consumerServiceImpl.searchConsumersByName(organizationId, searchTerm);
        return ResponseEntity.ok(consumers);
    }

    @PutMapping("/{consumerId}")
    @Operation(summary = "Update consumer")
    public ResponseEntity<ConsumerResponse> updateConsumer(
        @PathVariable UUID consumerId,
        @Valid @RequestBody UpdateConsumerRequest request) {
        ConsumerResponse consumer = consumerServiceImpl.updateConsumer(consumerId, request);
        return ResponseEntity.ok(consumer);
    }

    @PostMapping("/{consumerId}/deactivate")
    @Operation(summary = "Deactivate consumer")
    public ResponseEntity<ConsumerResponse> deactivateConsumer(@PathVariable UUID consumerId) {
        ConsumerResponse consumer = consumerServiceImpl.deactivateConsumer(consumerId);
        return ResponseEntity.ok(consumer);
    }

    @DeleteMapping("/{consumerId}")
    @Operation(summary = "Delete consumer")
    public ResponseEntity<Void> deleteConsumer(@PathVariable UUID consumerId) {
        consumerServiceImpl.deleteConsumer(consumerId);
        return ResponseEntity.noContent().build();
    }
}

