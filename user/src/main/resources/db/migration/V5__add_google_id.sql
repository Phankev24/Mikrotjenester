ALTER TABLE users
    ADD COLUMN google_id VARCHAR(255),
    ADD CONSTRAINT uq_users_google_id UNIQUE (google_id);
