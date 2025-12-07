package ContractGuard.ContractGuard.services.consumer.controller;

import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.consumer.service.impl.ConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consumers")
@RequiredArgsConstructor
@Tag(name = "Consumers", description = "API Consumer management endpoints")
public class ConsumerController {

    private final ConsumerService consumerService;

    @PostMapping
    @Operation(summary = "Register a new consumer")
    public ResponseEntity<Consumer> registerConsumer(
        @RequestParam String name,
        @RequestParam UUID organizationId,
        @RequestParam(required = false) String contactEmail) {
        Consumer consumer = consumerService.registerConsumer(name, organizationId, contactEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(consumer);
    }

    @GetMapping("/{consumerId}")
    @Operation(summary = "Get consumer by ID")
    public ResponseEntity<Consumer> getConsumer(@PathVariable UUID consumerId) {
        Consumer consumer = consumerService.getConsumer(consumerId);
        return ResponseEntity.ok(consumer);
    }

    @GetMapping
    @Operation(summary = "Get all consumers for organization")
    public ResponseEntity<List<Consumer>> getConsumersByOrganization(@RequestParam UUID organizationId) {
        List<Consumer> consumers = consumerService.getConsumersByOrganization(organizationId);
        return ResponseEntity.ok(consumers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active consumers for organization")
    public ResponseEntity<List<Consumer>> getActiveConsumers(@RequestParam UUID organizationId) {
        List<Consumer> consumers = consumerService.getActiveConsumers(organizationId);
        return ResponseEntity.ok(consumers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search consumers by name")
    public ResponseEntity<List<Consumer>> searchConsumers(
        @RequestParam UUID organizationId,
        @RequestParam String searchTerm) {
        List<Consumer> consumers = consumerService.searchConsumersByName(organizationId, searchTerm);
        return ResponseEntity.ok(consumers);
    }

    @PutMapping("/{consumerId}")
    @Operation(summary = "Update consumer")
    public ResponseEntity<Consumer> updateConsumer(
        @PathVariable UUID consumerId,
        @RequestParam String name,
        @RequestParam(required = false) String contactEmail,
        @RequestParam(required = false) String contactName) {
        Consumer consumer = consumerService.updateConsumer(consumerId, name, contactEmail, contactName);
        return ResponseEntity.ok(consumer);
    }

    @PostMapping("/{consumerId}/deactivate")
    @Operation(summary = "Deactivate consumer")
    public ResponseEntity<Consumer> deactivateConsumer(@PathVariable UUID consumerId) {
        Consumer consumer = consumerService.deactivateConsumer(consumerId);
        return ResponseEntity.ok(consumer);
    }

    @DeleteMapping("/{consumerId}")
    @Operation(summary = "Delete consumer")
    public ResponseEntity<Void> deleteConsumer(@PathVariable UUID consumerId) {
        consumerService.deleteConsumer(consumerId);
        return ResponseEntity.noContent().build();
    }
}

