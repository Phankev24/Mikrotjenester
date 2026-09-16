create table if not exists event_vendor_ids (
    event_id bigint not null,
    vendor_id uuid not null,
    constraint pk_event_vendor_ids primary key (event_id, vendor_id),
    constraint fk_event_vendor_ids_event
    foreign key (event_id) references event(event_id)
    on delete cascade
);

create index if not exists idx_event_vendor_ids_event_id
    on event_vendor_ids (event_id);

create index if not exists idx_event_vendor_ids_vendor_id
    on event_vendor_ids (vendor_id);