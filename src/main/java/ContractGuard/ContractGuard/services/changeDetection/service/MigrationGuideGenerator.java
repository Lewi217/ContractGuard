package ContractGuard.ContractGuard.services.changeDetection.service;

import ContractGuard.ContractGuard.services.changeDetection.model.BreakingChangeDetailed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MigrationGuideGenerator {

    public String generateMigrationGuide(BreakingChangeDetailed change) {
        StringBuilder guide = new StringBuilder();

        guide.append("Migration Guide for ").append(change.getChangeType()).append("\n\n");

        guide.append("Change Type: ").append(change.getChangeType()).append("\n");
        guide.append("Severity: ").append(change.getSeverity()).append("\n");
        guide.append("Description: ").append(change.getDescription()).append("\n\n");

        switch (change.getChangeType()) {
            case "ENDPOINT_REMOVED":
                guide.append(generateEndpointRemovedGuide(change));
                break;
            case "METHOD_REMOVED":
                guide.append(generateMethodRemovedGuide(change));
                break;
            case "FIELD_REMOVED":
                guide.append(generateFieldRemovedGuide(change));
                break;
            case "TYPE_CHANGED":
                guide.append(generateTypeChangedGuide(change));
                break;
            case "FIELD_REQUIRED":
                guide.append(generateFieldRequiredGuide(change));
                break;
            case "SCHEMA_REMOVED":
                guide.append(generateSchemaRemovedGuide(change));
                break;
            default:
                guide.append(generateDefaultGuide(change));
        }

        return guide.toString();
    }

    private String generateEndpointRemovedGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Identify all consumers using endpoint ").append(change.getAffectedEndpoint()).append("\n");
        sb.append("2. Update consumer applications to use alternative endpoints\n");
        sb.append("3. Implement request routing/fallback logic if necessary\n");
        sb.append("4. Test thoroughly in staging environment\n");
        sb.append("5. Deploy to production with monitoring enabled\n\n");

        sb.append("Deprecation Timeline:\n");
        sb.append("- Immediate: Endpoint still available but returns 410 Gone with deprecation notice\n");
        sb.append("- Week 1: Send notifications to all consumers\n");
        sb.append("- Week 2-3: Provide support and monitoring\n");
        sb.append("- Week 4: Complete removal after all consumers migrated\n");

        return sb.toString();
    }

    private String generateMethodRemovedGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Identify all consumers using ").append(change.getAffectedField()).append(" method on ").append(change.getAffectedEndpoint()).append("\n");
        sb.append("2. Suggest alternative HTTP methods or endpoints\n");
        sb.append("3. Update client libraries and documentation\n");
        sb.append("4. Verify backward compatibility with existing clients\n\n");

        sb.append("Code Changes Example:\n");
        sb.append("// Old Code:\n");
        sb.append("const response = await fetch('" + change.getAffectedEndpoint() + "', {\n");
        sb.append("  method: '" + change.getAffectedField().toUpperCase() + "'\n");
        sb.append("});\n\n");
        sb.append("// New Code:\n");
        sb.append("const response = await fetch('" + change.getAffectedEndpoint() + "', {\n");
        sb.append("  method: 'GET'  // Use GET instead\n");
        sb.append("});\n");

        return sb.toString();
    }

    private String generateFieldRemovedGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Update all code that depends on field '" + change.getAffectedField() + "'\n");
        sb.append("2. Use alternative fields if available\n");
        sb.append("3. Add fallback logic for backward compatibility\n");
        sb.append("4. Test JSON parsing and null handling\n\n");

        sb.append("Code Changes Example:\n");
        sb.append("// Old Code:\n");
        sb.append("const value = response." + change.getAffectedField() + ";\n\n");
        sb.append("// New Code:\n");
        sb.append("const value = response.newFieldName || response.fallbackField;\n");

        return sb.toString();
    }

    private String generateTypeChangedGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Update type declarations in your code\n");
        sb.append("2. Add type conversion/validation logic\n");
        sb.append("3. Handle both old and new types during transition\n");
        sb.append("4. Update API client code generation\n\n");

        sb.append("Code Changes Example:\n");
        sb.append("// Old Code (expects string):\n");
        sb.append("const userId: string = response.userId;\n\n");
        sb.append("// New Code (now number):\n");
        sb.append("const userId: number = parseInt(response.userId) || response.userId;\n");

        return sb.toString();
    }

    private String generateFieldRequiredGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Ensure all requests include field '").append(change.getAffectedField()).append("'\n");
        sb.append("2. Add validation to require this field in requests\n");
        sb.append("3. Update request builders and factories\n");
        sb.append("4. Handle missing field cases in error handling\n\n");

        sb.append("Code Changes Example:\n");
        sb.append("// Old Code (field optional):\n");
        sb.append("const request = { name: 'John' };\n\n");
        sb.append("// New Code (field required):\n");
        sb.append("const request = { name: 'John', ").append(change.getAffectedField()).append(": 'required_value' };\n");

        return sb.toString();
    }

    private String generateSchemaRemovedGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action Required:\n");
        sb.append("1. Stop using schema '").append(change.getAffectedField()).append("' in your code\n");
        sb.append("2. Migrate to alternative schema\n");
        sb.append("3. Update TypeScript/Java model classes\n");
        sb.append("4. Update all references throughout codebase\n\n");

        sb.append("Migration Steps:\n");
        sb.append("1. Search for all imports/uses of ").append(change.getAffectedField()).append("\n");
        sb.append("2. Replace with new schema name\n");
        sb.append("3. Update type definitions\n");
        sb.append("4. Run type checker to find remaining issues\n");

        return sb.toString();
    }

    private String generateDefaultGuide(BreakingChangeDetailed change) {
        StringBuilder sb = new StringBuilder();
        sb.append("General Migration Steps:\n");
        sb.append("1. Review the change: ").append(change.getDescription()).append("\n");
        sb.append("2. Identify affected code in your application\n");
        sb.append("3. Make necessary code changes\n");
        sb.append("4. Test thoroughly in staging\n");
        sb.append("5. Deploy to production with monitoring\n");

        if (change.getDeprecationPath() != null) {
            sb.append("\nDeprecation Path: ").append(change.getDeprecationPath()).append("\n");
        }

        return sb.toString();
    }

    public String generateCodeExample(BreakingChangeDetailed change) {
        StringBuilder example = new StringBuilder();

        switch (change.getChangeType()) {
            case "ENDPOINT_REMOVED":
                example.append("// Old endpoint no longer available\n");
                example.append("// fetch('").append(change.getAffectedEndpoint()).append("')  // ❌ This will fail\n\n");
                example.append("// Use new endpoint instead:\n");
                example.append("fetch('/api/v2/new-endpoint')\n");
                break;

            case "FIELD_REMOVED":
                example.append("// Before:\n");
                example.append("const data = {\n");
                example.append("  id: 1,\n");
                example.append("  ").append(change.getAffectedField()).append(": 'value'  // ❌ This field is removed\n");
                example.append("};\n\n");
                example.append("// After:\n");
                example.append("const data = {\n");
                example.append("  id: 1,\n");
                example.append("  // Use alternative field instead\n");
                example.append("};\n");
                break;

            case "TYPE_CHANGED":
                example.append("// Type conversion needed\n");
                if (change.getAdditionalContext() != null && change.getAdditionalContext().has("oldValue")) {
                    example.append("// Before: ").append(change.getAdditionalContext().get("oldValue").asText()).append("\n");
                    example.append("// After: ").append(change.getAdditionalContext().get("newValue").asText()).append("\n");
                } else {
                    example.append("// Check migration guide for type details\n");
                }
                break;

            default:
                example.append("See migration guide above for code changes\n");
        }

        return example.toString();
    }
}

