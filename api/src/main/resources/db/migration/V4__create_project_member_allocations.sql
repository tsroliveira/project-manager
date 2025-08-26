-- V4__create_project_member_allocations.sql (idempotente e tolerante a esquema existente)

-- 1) Garante a tabela de alocações
create table if not exists project_member_allocations (
    id                 uuid primary key,
    project_id         uuid        not null references projects(id) on delete cascade,
    member_external_id varchar(64) not null,
    allocated_at       timestamptz not null default now()
);

-- 2) Se a coluna member_external_id não existe, tenta renomear member_id -> member_external_id; senão cria
do $$
begin
    if not exists (
        select 1 from information_schema.columns
        where table_name = 'project_member_allocations'
          and column_name = 'member_external_id'
    ) then
        if exists (
            select 1 from information_schema.columns
            where table_name = 'project_member_allocations'
              and column_name = 'member_id'
        ) then
            execute 'alter table project_member_allocations rename column member_id to member_external_id';
        else
            execute 'alter table project_member_allocations add column member_external_id varchar(64)';
        end if;
    end if;
end $$;

-- 3) Garante a coluna external_id na member_cache (renomeia variações comuns; senão cria)
do $$
begin
    if not exists (
        select 1 from information_schema.columns
        where table_name = 'member_cache'
          and column_name = 'external_id'
    ) then
        if exists (select 1 from information_schema.columns where table_name='member_cache' and column_name='member_id') then
            execute 'alter table member_cache rename column member_id to external_id';
        elsif exists (select 1 from information_schema.columns where table_name='member_cache' and column_name='externalid') then
            execute 'alter table member_cache rename column externalid to external_id';
        elsif exists (select 1 from information_schema.columns where table_name='member_cache' and column_name='externalId') then
            execute 'alter table member_cache rename column externalId to external_id';
        else
            -- cria a coluna se não existir nenhuma variação; deixa NULL permitidos
            execute 'alter table member_cache add column external_id varchar(64)';
        end if;
    end if;
end $$;

-- 4) Garante UNIQUE em member_cache.external_id (precisa ser único para ser alvo de FK)
do $$
begin
    if not exists (
        select 1
          from information_schema.table_constraints
         where table_name = 'member_cache'
           and constraint_name = 'uq_member_cache_external'
    ) then
        alter table member_cache
            add constraint uq_member_cache_external unique (external_id);
    end if;
end $$;

-- 5) FK allocations.member_external_id -> member_cache.external_id
do $$
begin
    if not exists (
        select 1
          from information_schema.table_constraints
         where table_name = 'project_member_allocations'
           and constraint_name = 'fk_alloc_member_cache'
    ) then
        alter table project_member_allocations
            add constraint fk_alloc_member_cache
                foreign key (member_external_id)
                references member_cache(external_id);
    end if;
end $$;

-- 6) UNIQUE (project_id, member_external_id) para evitar duplicidade da mesma pessoa no mesmo projeto
do $$
begin
    if not exists (
        select 1
          from information_schema.table_constraints
         where table_name = 'project_member_allocations'
           and constraint_name = 'uq_alloc_project_member'
    ) then
        alter table project_member_allocations
            add constraint uq_alloc_project_member unique (project_id, member_external_id);
    end if;
end $$;

-- 7) Índices auxiliares (idempotentes)
create index if not exists idx_alloc_project on project_member_allocations(project_id);
create index if not exists idx_alloc_member  on project_member_allocations(member_external_id);
