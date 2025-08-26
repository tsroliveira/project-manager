-- 1) member_cache: UNIQUE(external_id)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.conname = 'uq_member_cache_external'
          AND n.nspname = 'public'
          AND t.relname = 'member_cache'
    ) THEN
        -- Se o índice com esse nome já existe (criado por tentativa anterior), reutiliza-o
        IF EXISTS (
            SELECT 1
            FROM pg_class i
            JOIN pg_namespace n ON n.oid = i.relnamespace
            WHERE i.relname = 'uq_member_cache_external'
              AND n.nspname = 'public'
              AND i.relkind = 'i'
        ) THEN
            ALTER TABLE public.member_cache
                ADD CONSTRAINT uq_member_cache_external
                UNIQUE USING INDEX uq_member_cache_external;
        ELSE
            ALTER TABLE public.member_cache
                ADD CONSTRAINT uq_member_cache_external
                UNIQUE (external_id);
        END IF;
    END IF;
END
$$;

-- 2) project_member_allocations: UNIQUE(project_id, member_external_id)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.conname = 'uq_alloc_unique'
          AND n.nspname = 'public'
          AND t.relname = 'project_member_allocations'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM pg_class i
            JOIN pg_namespace n ON n.oid = i.relnamespace
            WHERE i.relname = 'uq_alloc_unique'
              AND n.nspname = 'public'
              AND i.relkind = 'i'
        ) THEN
            ALTER TABLE public.project_member_allocations
                ADD CONSTRAINT uq_alloc_unique
                UNIQUE USING INDEX uq_alloc_unique;
        ELSE
            ALTER TABLE public.project_member_allocations
                ADD CONSTRAINT uq_alloc_unique
                UNIQUE (project_id, member_external_id);
        END IF;
    END IF;
END
$$;

-- 3) Indexes auxiliares
-- Obs.: NÃO crie índice extra em member_cache(external_id);
-- o UNIQUE acima já cria um índice. Manteríamos 2 índices iguais.

CREATE INDEX IF NOT EXISTS idx_alloc_member
    ON public.project_member_allocations (member_external_id);

CREATE INDEX IF NOT EXISTS idx_alloc_project
    ON public.project_member_allocations (project_id);

CREATE INDEX IF NOT EXISTS idx_projects_status
    ON public.projects (status);

CREATE INDEX IF NOT EXISTS idx_projects_created_at
    ON public.projects (created_at);
