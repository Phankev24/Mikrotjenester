
ALTER TABLE users
    ADD COLUMN github_id VARCHAR(255),
    ADD CONSTRAINT uq_users_github_id UNIQUE (github_id);
