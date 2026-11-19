CREATE TABLE event (
    internal_event_id BIGSERIAL PRIMARY KEY,
    external_event_id UUID NOT NULL UNIQUE,
    external_user_id UUID,
    external_vendor_id UUID,
    event_organizer UUID NOT NULL,
    event_name VARCHAR(255),
    event_description VARCHAR(255),
    event_attendance INTEGER,
    event_location VARCHAR(255),
    event_date_time TIMESTAMP(6),
    event_category VARCHAR(50)
);

CREATE TABLE event_attendance (
  id BIGSERIAL PRIMARY KEY,
  external_event_id UUID NOT NULL,
  external_user_id UUID NOT NULL,
  CONSTRAINT uk_event_user_attendance UNIQUE (external_event_id, external_user_id)
);