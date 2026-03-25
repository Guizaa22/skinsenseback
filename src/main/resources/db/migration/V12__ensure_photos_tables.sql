-- V12: Ensure client_photo and service_photo exist (in case V11 was applied without creating them)
CREATE TABLE IF NOT EXISTS client_photo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    uploaded_by UUID REFERENCES user_account(id) ON DELETE SET NULL,
    label VARCHAR(50) NOT NULL DEFAULT 'before',
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_client_photo_client ON client_photo(client_id);
CREATE INDEX IF NOT EXISTS idx_client_photo_label ON client_photo(label);

CREATE TABLE IF NOT EXISTS service_photo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES beauty_service(id) ON DELETE CASCADE,
    uploaded_by UUID REFERENCES user_account(id) ON DELETE SET NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_service_photo_service ON service_photo(service_id);
