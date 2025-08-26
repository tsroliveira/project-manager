-- Tipos de status (fixos)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'project_status') THEN
    CREATE TYPE project_status AS ENUM (
      'em_analise',
      'analise_realizada',
      'analise_aprovada',
      'iniciado',
      'planejado',
      'em_andamento',
      'encerrado',
      'cancelado'
    );
  END IF;
END
$$;

-- Tabela principal: projects
CREATE TABLE IF NOT EXISTS projects (
  id                UUID PRIMARY KEY,
  name              VARCHAR(150)      NOT NULL,
  start_date        DATE              NOT NULL,
  expected_end_date DATE              NOT NULL,
  actual_end_date   DATE              NULL,
  total_budget      NUMERIC(19,2)     NOT NULL,
  description       TEXT              NULL,
  manager_member_id VARCHAR(64)       NOT NULL,
  status            project_status    NOT NULL DEFAULT 'em_analise',
  created_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_projects_status  ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_manager ON projects(manager_member_id);

-- Alocações de membros no projeto
CREATE TABLE IF NOT EXISTS project_member_allocations (
  id          UUID PRIMARY KEY,
  project_id  UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  member_id   VARCHAR(64)  NOT NULL,
  allocated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_project_member UNIQUE (project_id, member_id)
);

CREATE INDEX IF NOT EXISTS idx_alloc_member ON project_member_allocations(member_id);

-- Cache local de membros (para não cadastrar direto e reduzir chamadas à API externa)
CREATE TABLE IF NOT EXISTS member_cache (
  id         VARCHAR(64) PRIMARY KEY,
  name       VARCHAR(150) NOT NULL,
  role       VARCHAR(50)  NOT NULL,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
