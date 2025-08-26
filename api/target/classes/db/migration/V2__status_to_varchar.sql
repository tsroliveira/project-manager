-- converte enum para texto e mantém o mesmo default
ALTER TABLE projects 
  ALTER COLUMN status TYPE VARCHAR(30) USING status::text;

ALTER TABLE projects 
  ALTER COLUMN status SET DEFAULT 'em_analise';
