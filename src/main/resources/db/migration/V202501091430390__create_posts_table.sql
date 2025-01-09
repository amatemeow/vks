create extension if not exists "uuid-ossp";

create table if not exists posts (
  id uuid primary key default uuid_generate_v4(),
  post_id integer not null,
  author_id integer not null,
  group_info jsonb default '{}',
  text varchar not null default '',
  published timestamp with time zone default null
);
