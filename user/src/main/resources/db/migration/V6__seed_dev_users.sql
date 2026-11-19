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
