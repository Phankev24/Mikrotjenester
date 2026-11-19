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
      );