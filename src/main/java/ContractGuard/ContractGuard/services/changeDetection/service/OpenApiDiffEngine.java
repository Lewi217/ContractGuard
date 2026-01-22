package ContractGuard.ContractGuard.services.changeDetection.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class OpenApiDiffEngine {

    public List<EndpointChange> compareEndpoints(JsonNode oldSpec, JsonNode newSpec) {
        List<EndpointChange> changes = new ArrayList<>();

        if (!oldSpec.has("paths") || !newSpec.has("paths")) {
            return changes;
        }

        JsonNode oldPaths = oldSpec.get("paths");
        JsonNode newPaths = newSpec.get("paths");

        oldPaths.fieldNames().forEachRemaining(endpoint -> {
            if (!newPaths.has(endpoint)) {
                changes.add(EndpointChange.builder()
                    .endpoint(endpoint)
                    .changeType("ENDPOINT_REMOVED")
                    .severity("CRITICAL")
                    .description("Endpoint '" + endpoint + "' has been removed")
                    .build());
            }
        });

        Iterator<Map.Entry<String, JsonNode>> iter = oldPaths.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            String endpoint = entry.getKey();
            if (newPaths.has(endpoint)) {
                JsonNode oldMethods = entry.getValue();
                JsonNode newMethods = newPaths.get(endpoint);

                oldMethods.fieldNames().forEachRemaining(method -> {
                    if (!newMethods.has(method)) {
                        changes.add(EndpointChange.builder()
                            .endpoint(endpoint)
                            .method(method)
                            .changeType("METHOD_REMOVED")
                            .severity("HIGH")
                            .description("Method '" + method.toUpperCase() + "' removed from endpoint '" + endpoint + "'")
                            .build());
                    }
                });
            }
        }

        return changes;
    }

    public List<SchemaChange> compareSchemas(JsonNode oldSpec, JsonNode newSpec) {
        List<SchemaChange> changes = new ArrayList<>();

        if (!oldSpec.has("components") || !newSpec.has("components")) {
            return changes;
        }

        JsonNode oldSchemas = oldSpec.get("components").get("schemas");
        JsonNode newSchemas = newSpec.get("components").get("schemas");

        if (oldSchemas == null || newSchemas == null) {
            return changes;
        }

        oldSchemas.fieldNames().forEachRemaining(schemaName -> {
            if (!newSchemas.has(schemaName)) {
                changes.add(SchemaChange.builder()
                    .schemaName(schemaName)
                    .changeType("SCHEMA_REMOVED")
                    .severity("HIGH")
                    .description("Schema '" + schemaName + "' has been removed")
                    .build());
            }
        });

        Iterator<Map.Entry<String, JsonNode>> schemaIter = oldSchemas.fields();
        while (schemaIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = schemaIter.next();
            String schemaName = entry.getKey();
            if (newSchemas.has(schemaName)) {
                JsonNode oldSchema = entry.getValue();
                JsonNode newSchema = newSchemas.get(schemaName);

                if (oldSchema.has("properties") && newSchema.has("properties")) {
                    changes.addAll(compareProperties(schemaName, oldSchema, newSchema));
                }

                if (oldSchema.has("required") && newSchema.has("required")) {
                    changes.addAll(compareRequiredFields(schemaName, oldSchema, newSchema));
                }
            }
        }

        return changes;
    }

    private List<SchemaChange> compareProperties(String schemaName, JsonNode oldSchema, JsonNode newSchema) {
        List<SchemaChange> changes = new ArrayList<>();

        JsonNode oldProps = oldSchema.get("properties");
        JsonNode newProps = newSchema.get("properties");

        oldProps.fieldNames().forEachRemaining(fieldName -> {
            if (!newProps.has(fieldName)) {
                changes.add(SchemaChange.builder()
                    .schemaName(schemaName)
                    .fieldName(fieldName)
                    .changeType("FIELD_REMOVED")
                    .severity("HIGH")
                    .description("Field '" + fieldName + "' removed from schema '" + schemaName + "'")
                    .build());
            }
        });

        Iterator<Map.Entry<String, JsonNode>> propsIter = oldProps.fields();
        while (propsIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = propsIter.next();
            String fieldName = entry.getKey();
            if (newProps.has(fieldName)) {
                JsonNode oldType = entry.getValue();
                JsonNode newType = newProps.get(fieldName);

                String oldTypeStr = getSchemaType(oldType);
                String newTypeStr = getSchemaType(newType);

                if (!oldTypeStr.equals(newTypeStr)) {
                    changes.add(SchemaChange.builder()
                        .schemaName(schemaName)
                        .fieldName(fieldName)
                        .changeType("TYPE_CHANGED")
                        .severity("CRITICAL")
                        .oldValue(oldTypeStr)
                        .newValue(newTypeStr)
                        .description("Field '" + fieldName + "' type changed from '" + oldTypeStr + "' to '" + newTypeStr + "'")
                        .build());
                }
            }
        }

        return changes;
    }

    private List<SchemaChange> compareRequiredFields(String schemaName, JsonNode oldSchema, JsonNode newSchema) {
        List<SchemaChange> changes = new ArrayList<>();

        Set<String> oldRequired = getRequiredFields(oldSchema);
        Set<String> newRequired = getRequiredFields(newSchema);

        newRequired.forEach(field -> {
            if (!oldRequired.contains(field)) {
                changes.add(SchemaChange.builder()
                    .schemaName(schemaName)
                    .fieldName(field)
                    .changeType("FIELD_REQUIRED")
                    .severity("HIGH")
                    .description("Field '" + field + "' is now required in schema '" + schemaName + "'")
                    .build());
            }
        });

        return changes;
    }

    private String getSchemaType(JsonNode schema) {
        if (schema.has("type")) {
            return schema.get("type").asText();
        } else if (schema.has("$ref")) {
            String ref = schema.get("$ref").asText();
            return ref.substring(ref.lastIndexOf("/") + 1);
        }
        return "unknown";
    }

    private Set<String> getRequiredFields(JsonNode schema) {
        Set<String> required = new HashSet<>();
        if (schema.has("required")) {
            schema.get("required").forEach(field -> required.add(field.asText()));
        }
        return required;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class EndpointChange {
        private String endpoint;
        private String method;
        private String changeType;
        private String severity;
        private String description;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class SchemaChange {
        private String schemaName;
        private String fieldName;
        private String changeType;
        private String severity;
        private String oldValue;
        private String newValue;
        private String description;
    }
}

