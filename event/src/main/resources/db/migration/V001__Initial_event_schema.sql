create table if not exists event(
       event_id bigserial primary key,
       external_event_id uuid unique not null,
       vendor_id uuid unique,
       event_name varchar(255),
       event_description varchar(255),
       event_attendance integer,
       event_date_time timestamp,
       event_category varchar(50)
);