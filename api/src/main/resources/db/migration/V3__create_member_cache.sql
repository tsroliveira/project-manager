-- cache local de membros externos (origem: Members API)
create table if not exists member_cache (
    external_id           varchar(64) primary key,            -- id vindo da API externa (ex.: "mgr-001")
    name                  varchar(150) not null,
    role                  varchar(40)  not null,              -- ex.: "funcionario", "gerente"
    created_at            timestamptz  not null default now(),
    updated_at            timestamptz  not null default now()
);

create index if not exists idx_member_cache_role on member_cache(role);
