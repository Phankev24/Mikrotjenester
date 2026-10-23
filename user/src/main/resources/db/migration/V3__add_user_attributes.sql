ALTER TABLE users
    ADD COLUMN external_user_id UUID,
    ADD COLUMN username VARCHAR(255),
    ADD COLUMN password VARCHAR(255),
    ADD COLUMN age INTEGER,
    ADD COLUMN gender VARCHAR(255);

UPDATE users
SET external_user_id = gen_random_uuid(),
    username = 'user' || id,
    password = 'password';

ALTER TABLE users
    ALTER COLUMN external_user_id SET NOT NULL,
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN password SET NOT NULL,
    ADD CONSTRAINT uq_users_external_user_id UNIQUE (external_user_id),
    ADD CONSTRAINT uq_users_username UNIQUE (username);
