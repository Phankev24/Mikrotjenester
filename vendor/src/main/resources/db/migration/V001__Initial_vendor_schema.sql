create table if not exists vendor (
    user_id bigserial primary key,
    id uuid not null unique,
    type varchar(255),
    company_name varchar(255),
    services_description varchar(255),
    website varchar(255),
    phone varchar(255)
    );
