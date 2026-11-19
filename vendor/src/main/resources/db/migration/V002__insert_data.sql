INSERT INTO vendor (
    external_id,
    company_name,
    password,
    email,
    phone,
    website,
    description
)
VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        'Oslo Catering',
        'password123',
        'post@oslocatering.example',
        '+47 22 00 00 01',
        'https://oslocatering.example',
        'Buffets and finger food for up to 200 guests.'
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'Festlyd DJ',
        'password123',
        'booking@festlyd.example',
        '+47 22 00 00 02',
        'https://festlyd.example',
        'DJ and sound system for parties and company events.'
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'Blomsten Blomster',
        'password123',
        'hei@blomsten.example',
        '+47 22 00 00 03',
        'https://blomsten.example',
        'Flowers and table decorations.'
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        'Lys og Tone',
        'password123',
        'info@lysogtone.example',
        '+47 22 00 00 04',
        'https://lysogtone.example',
        'Lighting and stage rental.'
    ),
    (
        '11111111-1111-1111-1111-111111111111',
        'Demo Vendor',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'vendor@test.no',
        '12345678',
        'https://vendor.no/',
        'Preloaded vendor'
    )