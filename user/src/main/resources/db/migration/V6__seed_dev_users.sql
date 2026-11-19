UPDATE users
SET external_user_id = 'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
    username = 'ola',
    password = '$2a$10$iDsvxk7HUk0wxUmISUwHZuhcA5Xul17OfEOBbj0Ox/JOGUZOQVmou',
    age = 30,
    gender = 'male'
WHERE email = 'ola.nordmann@example.com';

UPDATE users
SET external_user_id = '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90',
    username = 'kari',
    password = '$2a$10$iDsvxk7HUk0wxUmISUwHZuhcA5Xul17OfEOBbj0Ox/JOGUZOQVmou',
    age = 28,
    gender = 'female'
WHERE email = 'kari.nordmann@example.com';

INSERT INTO users (
    external_user_id,
    username,
    email,
    password,
    age,
    gender
) VALUES
      (
          'c3d4e5f6-1111-4222-8333-444455556666',
          'erik',
          'erik.hansen@example.com',
          '$2a$10$iDsvxk7HUk0wxUmISUwHZuhcA5Xul17OfEOBbj0Ox/JOGUZOQVmou',
          25,
          'male'
      ),
      (
          'd4e5f6a7-2222-4333-9444-555566667777',
          'sofie',
          'sofie.berg@example.com',
          '$2a$10$iDsvxk7HUk0wxUmISUwHZuhcA5Xul17OfEOBbj0Ox/JOGUZOQVmou',
          27,
          'female'
      )
    ON CONFLICT (external_user_id) DO NOTHING;