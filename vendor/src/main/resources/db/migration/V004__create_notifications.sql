CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    external_vendor_id UUID NOT NULL,
    external_event_id UUID NOT NULL,
    event_name VARCHAR(255),
    attendee_user_id UUID,
    message VARCHAR(1000) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_notifications_event_attendee_time UNIQUE (external_event_id, attendee_user_id, joined_at)
);

CREATE INDEX idx_notifications_vendor_joined_at ON notifications (external_vendor_id, joined_at);
