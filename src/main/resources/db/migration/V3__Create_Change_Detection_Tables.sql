CREATE TABLE IF NOT EXISTS breaking_changes_detailed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    old_version VARCHAR(50) NOT NULL,
    new_version VARCHAR(50) NOT NULL,
    change_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    description TEXT,
    affected_endpoint VARCHAR(500),
    affected_field VARCHAR(255),
    migration_guide TEXT,
    code_example TEXT,
    impact_level VARCHAR(20) DEFAULT 'MEDIUM',
    deprecation_path VARCHAR(500),
    additional_context JSONB,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bcd_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_bcd_contract ON breaking_changes_detailed(contract_id);
CREATE INDEX IF NOT EXISTS idx_bcd_severity ON breaking_changes_detailed(severity);
CREATE INDEX IF NOT EXISTS idx_bcd_detected_at ON breaking_changes_detailed(detected_at);

CREATE TABLE IF NOT EXISTS change_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    breaking_change_id UUID NOT NULL REFERENCES breaking_changes_detailed(id) ON DELETE CASCADE,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_change_detail_breaking_change FOREIGN KEY (breaking_change_id) REFERENCES breaking_changes_detailed(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_change_detail_breaking_change ON change_details(breaking_change_id);
CREATE INDEX IF NOT EXISTS idx_change_detail_type ON change_details(change_type);

CREATE TABLE IF NOT EXISTS impact_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    breaking_change_id UUID NOT NULL REFERENCES breaking_changes_detailed(id) ON DELETE CASCADE,
    consumer_id UUID NOT NULL REFERENCES consumers(id) ON DELETE CASCADE,
    impact_score INTEGER NOT NULL,
    impact_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    affected_endpoints TEXT,
    estimated_migration_effort INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ia_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_ia_breaking_change FOREIGN KEY (breaking_change_id) REFERENCES breaking_changes_detailed(id) ON DELETE CASCADE,
    CONSTRAINT fk_ia_consumer FOREIGN KEY (consumer_id) REFERENCES consumers(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ia_contract ON impact_analyses(contract_id);
CREATE INDEX IF NOT EXISTS idx_ia_breaking_change ON impact_analyses(breaking_change_id);
CREATE INDEX IF NOT EXISTS idx_ia_consumer ON impact_analyses(consumer_id);

