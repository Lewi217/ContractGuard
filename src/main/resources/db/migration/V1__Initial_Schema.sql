CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE organizations (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               name VARCHAR(255) NOT NULL,
                               slug VARCHAR(100) NOT NULL UNIQUE,
                               description TEXT,
                               plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               created_by UUID
);

CREATE INDEX idx_org_slug ON organizations(slug);
COMMENT ON TABLE organizations IS 'Organizations that own API contracts';
COMMENT ON COLUMN organizations.plan IS 'Pricing plan: FREE, PRO, ENTERPRISE';


CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(500) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP
);

CREATE INDEX idx_user_org ON users(organization_id);
CREATE INDEX idx_user_email ON users(email);
COMMENT ON TABLE users IS 'System users with role-based access control';
COMMENT ON COLUMN users.role IS 'Role: ADMIN, MEMBER';

CREATE TABLE contracts (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           version VARCHAR(50) NOT NULL,
                           status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                           base_path VARCHAR(255) NOT NULL,
                           openapi_spec JSONB NOT NULL,
                           blob_storage_url VARCHAR(500),
                           tags JSONB,  -- safely defined as JSONB
                           created_by UUID REFERENCES users(id) ON DELETE SET NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deprecated_at TIMESTAMP,
                           retired_at TIMESTAMP,
                           UNIQUE(organization_id, name, version)
);

CREATE INDEX idx_contract_org ON contracts(organization_id);
CREATE INDEX idx_contract_status ON contracts(status);
CREATE INDEX idx_contract_name ON contracts(name);
COMMENT ON TABLE contracts IS 'API Contracts (OpenAPI specifications)';
COMMENT ON COLUMN contracts.status IS 'Status: DRAFT, ACTIVE, DEPRECATED, RETIRED';
COMMENT ON COLUMN contracts.openapi_spec IS 'Full OpenAPI 3.0.0 specification as JSONB';


CREATE TABLE contract_versions (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
                                   version_number VARCHAR(50) NOT NULL,
                                   openapi_spec JSONB NOT NULL,
                                   change_summary TEXT,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   created_by UUID REFERENCES users(id) ON DELETE SET NULL,
                                   UNIQUE(contract_id, version_number)
);

CREATE INDEX idx_version_contract ON contract_versions(contract_id);
COMMENT ON TABLE contract_versions IS 'Historical versions of contracts for comparison';


CREATE TABLE consumers (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           api_key VARCHAR(500) UNIQUE,
                           contact_email VARCHAR(255),
                           contact_name VARCHAR(255),
                           consumer_type VARCHAR(50) NOT NULL DEFAULT 'SERVICE',
                           is_active BOOLEAN NOT NULL DEFAULT true,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consumer_org ON consumers(organization_id);
CREATE INDEX idx_consumer_api_key ON consumers(api_key);
CREATE INDEX idx_consumer_active ON consumers(is_active);
COMMENT ON TABLE consumers IS 'API Consumers (applications/services using contracts)';
COMMENT ON COLUMN consumers.consumer_type IS 'Type: SERVICE, WEB_APP, MOBILE_APP, THIRD_PARTY';


CREATE TABLE consumer_registrations (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        consumer_id UUID NOT NULL REFERENCES consumers(id) ON DELETE CASCADE,
                                        contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
                                        version_subscribed_to VARCHAR(50) NOT NULL,
                                        status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                        subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        UNIQUE(consumer_id, contract_id)
);

CREATE INDEX idx_registration_consumer ON consumer_registrations(consumer_id);
CREATE INDEX idx_registration_contract ON consumer_registrations(contract_id);
COMMENT ON TABLE consumer_registrations IS 'Tracks which consumers use which contracts';
COMMENT ON COLUMN consumer_registrations.status IS 'Status: ACTIVE, DEPRECATED, MIGRATED';


CREATE TABLE validation_results (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
                                    consumer_id UUID REFERENCES consumers(id) ON DELETE SET NULL,
                                    endpoint_path VARCHAR(500) NOT NULL,
                                    http_method VARCHAR(10) NOT NULL,
                                    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                    passed BOOLEAN,
                                    expected_status_code INTEGER,
                                    actual_status_code INTEGER,
                                    response_time_ms INTEGER,
                                    violations JSONB,
                                    test_data JSONB,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_validation_contract ON validation_results(contract_id);
CREATE INDEX idx_validation_consumer ON validation_results(consumer_id);
CREATE INDEX idx_validation_status ON validation_results(status);
CREATE INDEX idx_validation_created ON validation_results(created_at);
COMMENT ON TABLE validation_results IS 'Records of API validation tests against contracts';
COMMENT ON COLUMN validation_results.status IS 'Status: PENDING, PASSED, FAILED, ERROR';
COMMENT ON COLUMN validation_results.violations IS 'Array of validation violations with details';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contracts_updated_at BEFORE UPDATE ON contracts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_consumers_updated_at BEFORE UPDATE ON consumers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_validation_results_updated_at BEFORE UPDATE ON validation_results
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
