-- Membros no cache (para aparecerem na listagem das alocações)
insert into member_cache (id, external_id, name, role, created_at, updated_at)
values ('mgr-001','mgr-001','Gerente Exemplo','gerente', now(), now())
on conflict (external_id) do nothing;

insert into member_cache (id, external_id, name, role, created_at, updated_at)
values ('func-001','func-001','Funcionario Exemplo','funcionário', now(), now())
on conflict (external_id) do nothing;

insert into member_cache (id, external_id, name, role, created_at, updated_at)
values ('func-002','func-002','Funcionario Exemplo','funcionário', now(), now())
on conflict (external_id) do nothing;
