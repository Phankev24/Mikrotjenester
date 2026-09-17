create table if not exists vendor (
    id uuid primary key,
    user_id uuid, type varchar(255),
    company_name varchar(255),
    services_description varchar(255),
    website varchar(255),
    phone varchar(255)
    );
