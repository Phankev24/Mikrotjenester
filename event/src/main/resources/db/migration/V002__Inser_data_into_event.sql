insert into event(
                  external_event_id,
                  external_user_id,
                  external_vendor_id,
                  event_name,
                  event_description,
                  event_attendance,
                  event_location,
                  event_date_time,
                  event_category
)
values
    (
     '85c9f1ef-060b-42f7-b46d-95d7b5abb74e',
     'f80a5683-d13a-4422-ae6c-3e4b38e2b71d',
     '7fd2a272-3d9d-4330-a1b6-a633e5dc75a4',
     'Bowling med jobb',
     'Bowling med kollegaer',
     13,
     'Bowling 1 - Torggata 16, 0181 Oslo'
        ,'2026-10-01 09:00:00',
     'SOCIAL'
    ),


    ('2dd345d4-5a65-4c01-b2ed-d71d6fd71497',
     'b579eb2b-29d5-4fdd-b662-a0b6272bcaab',
     'e2cd42d0-4066-45be-b22b-836160c96c3a',
     'World Cup Finale',        'Se på finalen av worldcup',
     25,
     'Ullevål stadion - Sognsveien 75K, 0855 Oslo',
     '2026-09-20 14:30:00',
     'SPORTS'
    );