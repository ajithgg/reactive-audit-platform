CREATE TABLE IF NOT EXISTS user_record (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    external_id VARCHAR(200) NOT NULL,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attributes_json TEXT NOT NULL DEFAULT '{}',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_record_tenant_external UNIQUE (tenant_id, external_id)
);

CREATE INDEX IF NOT EXISTS idx_user_record_tenant ON user_record (tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_record_tenant_email ON user_record (tenant_id, email);
