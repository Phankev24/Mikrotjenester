INSERT INTO event (
    external_event_id,
    external_user_id,
    external_vendor_id,
    event_organizer,
    event_name,
    event_description,
    event_attendance,
    event_location,
    event_date_time,
    event_category
) VALUES
      (
          '11111111-1111-1111-1111-111111111111',
          NULL,
          '00000000-0000-0000-0000-000000000001',
          '00000000-0000-0000-0000-000000000001',
          'Tech Conference 2023',
          'Annual Tech Conference',
          100,
          'Conference Hall A',
          NOW() + INTERVAL '30 days',
          'CONFERENCE'
      ),
      (
          '22222222-2222-2222-2222-222222222222',
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          NULL,
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          'Summer Picnic',
          'Community Picnic',
          50,
          'City Park',
          NOW() + INTERVAL '15 days',
          'SOCIAL'
      ),
      (
          '33333333-3333-3333-3333-333333333333',
          NULL,
          '00000000-0000-0000-0000-000000000001',
          '00000000-0000-0000-0000-000000000001',
          'Indoor Bouldering & Top-Rope Session',
          'Guided climbing session for beginners and experienced climbers. Shoes and chalk included.',
          25,
          'Drammen Klatresenter',
          NOW() + INTERVAL '5 days',
          'SPORTS'
      ),
      (
          '44444444-4444-4444-4444-444444444444',
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          NULL,
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          'Outdoor Crag Climbing Meetup',
          'Casual evening lead and top-rope climbing outdoors. Bring your own harness and belay device.',
          14,
          'Spiralen Klatrefelt',
          NOW() + INTERVAL '9 days',
          'SPORTS'
      ),
      (
          '55555555-5555-5555-5555-555555555555',
          NULL,
          '00000000-0000-0000-0000-000000000001',
          '00000000-0000-0000-0000-000000000001',
          'Microservices & Docker Hands-On Workshop',
          'Build, containerize, and deploy Spring Boot microservices with PostgreSQL and Flyway.',
          40,
          'Innovation Hub Lab 2',
          NOW() + INTERVAL '12 days',
          'CONFERENCE'
      ),
      (
          '66666666-6666-6666-6666-666666666666',
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          NULL,
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          'Acoustic Rooftop Jam Night',
          'Open acoustic session and live indie sets. Bring an instrument or just come hang out.',
          60,
          'Union Scene Rooftop',
          NOW() + INTERVAL '18 days',
          'MUSIC'
      ),
      (
          '77777777-7777-7777-7777-777777777777',
          NULL,
          '00000000-0000-0000-0000-000000000001',
          '00000000-0000-0000-0000-000000000001',
          'Winter Trail Half Marathon',
          '10K and Half Marathon forest trail race with timing chips and hot drinks at the finish line.',
          150,
          'Bragernesåsen Trailhead',
          NOW() + INTERVAL '24 days',
          'SPORTS'
      ),
      (
          '88888888-8888-8888-8888-888888888888',
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          NULL,
          'a7f3bf47-f373-4cdf-b81c-b0475305b7fe',
          'Board Game & Pizza Night',
          'Strategy board games, Catan, and wood-fired pizza with local tech students.',
          18,
          'Studenthuset Lounge',
          NOW() + INTERVAL '7 days',
          'SOCIAL'
      ),
      (
          '99999999-9999-9999-9999-999999999999',
          NULL,
          '00000000-0000-0000-0000-000000000002',
          '00000000-0000-0000-0000-000000000002',
          'Nordic Electronic Club Night',
          'Live DJ sets, deep house, and full club sound system.',
          200,
          'Union Scene Main Hall',
          NOW() + INTERVAL '10 days',
          'MUSIC'
      ),
      (
          'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
          NULL,
          '00000000-0000-0000-0000-000000000003',
          '00000000-0000-0000-0000-000000000003',
          'Autumn Wreath & Bouquet Workshop',
          'Hands-on floral arrangement class with seasonal flowers and tools provided.',
          20,
          'Bragernes Torv Studio',
          NOW() + INTERVAL '14 days',
          'SOCIAL'
      ),
      (
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          NULL,
          '00000000-0000-0000-0000-000000000004',
          '00000000-0000-0000-0000-000000000004',
          'Stage Lighting & Live Sound Showcase',
          'Interactive demo of DMX stage lighting rigs and live audio setups.',
          45,
          'Papirbredden Auditorium',
          NOW() + INTERVAL '20 days',
          'CONFERENCE'
      ),
      (
          'cccccccc-cccc-cccc-cccc-cccccccccccc',
          NULL,
          '11111111-1111-1111-1111-111111111111',
          '11111111-1111-1111-1111-111111111111',
          'Vendor Showcase & Networking Expo',
          'Meet local event suppliers, caterers, and tech organizers.',
          120,
          'Drammen Congress Center',
          NOW() + INTERVAL '28 days',
          'CONFERENCE'
      ),
      (
          'dddddddd-dddd-dddd-dddd-dddddddddddd',
          '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90',
          NULL,
          '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90',
          'Sunday Riverfront Trail Run',
          'Easy 8 km group run along Drammenselva followed by coffee.',
          30,
          'Ypsilon Bridge',
          NOW() + INTERVAL '6 days',
          'SPORTS'
      ),
      (
          'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
          'c3d4e5f6-1111-4222-8333-444455556666',
          NULL,
          'c3d4e5f6-1111-4222-8333-444455556666',
          'Spring Boot & Kafka Hackathon',
          'Evening coding jam building event-driven microservices.',
          35,
          'Campus Kronstad Lab 4',
          NOW() + INTERVAL '16 days',
          'CONFERENCE'
      )
    ON CONFLICT (external_event_id) DO NOTHING;

INSERT INTO event_attendance (
    external_event_id,
    external_user_id
) VALUES
      ('11111111-1111-1111-1111-111111111111', 'a7f3bf47-f373-4cdf-b81c-b0475305b7fe'),
      ('99999999-9999-9999-9999-999999999999', 'a7f3bf47-f373-4cdf-b81c-b0475305b7fe'),
      ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'a7f3bf47-f373-4cdf-b81c-b0475305b7fe'),
      ('22222222-2222-2222-2222-222222222222', '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90'),
      ('33333333-3333-3333-3333-333333333333', '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90'),
      ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90'),
      ('33333333-3333-3333-3333-333333333333', 'c3d4e5f6-1111-4222-8333-444455556666'),
      ('55555555-5555-5555-5555-555555555555', 'c3d4e5f6-1111-4222-8333-444455556666'),
      ('99999999-9999-9999-9999-999999999999', 'c3d4e5f6-1111-4222-8333-444455556666'),
      ('66666666-6666-6666-6666-666666666666', 'd4e5f6a7-2222-4333-9444-555566667777'),
      ('88888888-8888-8888-8888-888888888888', 'd4e5f6a7-2222-4333-9444-555566667777'),
      ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'd4e5f6a7-2222-4333-9444-555566667777')
    ON CONFLICT DO NOTHING;