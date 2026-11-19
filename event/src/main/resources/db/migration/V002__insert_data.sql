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
      )
    ON CONFLICT (external_event_id) DO NOTHING;