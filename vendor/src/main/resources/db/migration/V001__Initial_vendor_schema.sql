CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE vendor (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    type VARCHAR(255),
    company_name VARCHAR(255),
    services_description VARCHAR(255),
    website VARCHAR(255),
    phone VARCHAR(255)
);
