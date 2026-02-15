create table if not exists requests (
  id uuid primary key,
  title varchar(200) not null,
  description text not null,
  category varchar(50) not null,
  status varchar(30) not null,
  created_by varchar(80) not null,
  assigned_to varchar(80),
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index if not exists idx_requests_created_by on requests(created_by);
create index if not exists idx_requests_status on requests(status);

create table if not exists audit_logs (
  id bigserial primary key,
  request_id uuid not null,
  actor varchar(80) not null,
  action varchar(60) not null,
  details text,
  created_at timestamptz not null
);

create index if not exists idx_audit_request_id on audit_logs(request_id);
