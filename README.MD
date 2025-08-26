# Portfolio de Projetos

API Spring Boot para gestão de **projetos**, **status**, **alocações de membros** e **cache de membros**.

## Stack
- Java 17 • Spring Boot 3.3
- JPA/Hibernate • PostgreSQL
- Flyway (migrations)
- OpenAPI (springdoc) – **Swagger UI habilitado**
- Docker & Docker Compose (com **profiles**)

---

## Pré-requisitos
- **Docker** e **Docker Compose** instalados
- (Opcional p/ rodar localmente) **Java 17** e **Maven 3.9+**

> **Portas padrão**  
> API: `8080` • DB: `5433 -> 5432` (host -> container) • WireMock: `8081`

---

## Perfis do Compose
- **db**: PostgreSQL 16 (alpine)
- **wiremock** (opcional): Mock do serviço externo de membros
- **api**: aplicação Spring Boot

> Rode os comandos a partir da **pasta raiz** do repositório (onde fica o `docker-compose.yml` principal).

---

## Como executar com Docker (recomendado)

### 1) Subir o banco
```bash
docker compose --profile db up -d
```

### 2) (Opcional) Subir o WireMock
```bash
docker compose --profile wiremock up -d
# painel/health: http://localhost:8081/__admin
```

### 3) Buildar e subir a API
```bash
docker compose --profile api build --no-cache
docker compose --profile api up -d --force-recreate
```

### 4) Ver logs
```bash
docker compose logs -f api
```

### 5) Subir tudo de uma vez
```bash
docker compose --profile db --profile wiremock --profile api up -d --build
```

### 6) Encerrar/limpar
```bash
# parar serviços
docker compose down
# parar + remover volumes (⚠️ apaga dados do Postgres)
docker compose down -v
```

---

## Configuração (variáveis de ambiente)

A API dentro do container usa o **profile `docker`**.  
Se quiser rodar **fora do Docker**, exporte:

| Variável | Padrão (dev) | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/portfolio` | JDBC do Postgres (host) |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuário |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Senha |
| `MEMBER_API_BASE_URL` | `http://localhost:8081` | Base URL do serviço de membros (WireMock) |
| `SPRING_PROFILES_ACTIVE` | `docker` (no container) | Local: pode usar `local` e setar as props acima |

> No Docker, a API usa os hosts de rede do Compose: `db` (Postgres) e `wiremock` (stub).  
> Fora do Docker, use `localhost:5433` (DB) e `localhost:8081` (WireMock).

---

## Banco de Dados & Migrations

- Migrações Flyway em `src/main/resources/db/migration`
  - **V1..V4**: esquema base
  - **V5**: **constraints/índices** (idempotentes)
  - **V6**: **demo seed** (dados de exemplo)

**Reset rápido de ambiente**:
```bash
docker compose down -v
docker compose --profile db up -d
docker compose --profile api up -d --build
```

---

## Documentação da API

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html  
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Endpoints principais (resumo)

**Projetos**
- `POST /api/projects` – cria projeto  
- `GET /api/projects` – lista paginada/filtrada  
- `GET /api/projects/{id}` – detalha projeto  
- `PUT /api/projects/{id}` / `PATCH /api/projects/{id}` – atualiza  
- `DELETE /api/projects/{id}` – remove

**Alocações de membros**
- `GET /api/projects/{id}/allocations` – lista alocações (com dados do cache)  
- `POST /api/projects/{id}/allocations` – adiciona membro ao projeto  
- `DELETE /api/projects/{id}/allocations/{memberExternalId}` – remove alocação

**Health / Actuator**
- `GET /actuator/health` – healthcheck  
- `GET /actuator` – links habilitados

### Exemplos rápidos (cURL)
```bash
# Health
curl -s http://localhost:8080/actuator/health

# Criar projeto
curl -X POST http://localhost:8080/api/projects -H "Content-Type: application/json" -d '{
  "name":"Projeto X",
  "startDate":"2025-01-01",
  "expectedEndDate":"2025-06-30",
  "totalBudget": 100000.00,
  "description":"Projeto demo",
  "managerMemberId":"USR-001"
}'

# Listar projetos
curl -s http://localhost:8080/api/projects

# Listar alocações do projeto
curl -s http://localhost:8080/api/projects/{id}/allocations
```

---

## Rodar **localmente** (sem container da API)

1) Suba DB e WireMock:
```bash
docker compose --profile db --profile wiremock up -d
```

2) Exporte as variáveis:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/portfolio
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export MEMBER_API_BASE_URL=http://localhost:8081
```

3) Rode com Maven:
```bash
mvn spring-boot:run
```

---

## Testes

Executar:
```bash
mvn -DskipTests=false test
```

## Testes com Testcontainers

Os testes usam **PostgreSQL via Testcontainers**. Pré-requisitos:
- Docker em execução
- Java 17 e Maven

Para executar:
```bash
mvn clean test
```

---

## Troubleshooting

- **“relation/constraint already exists”** após ajustes de schema:  
  Reset com volumes geralmente resolve:
  ```bash
  docker compose down -v
  docker compose --profile db up -d
  docker compose --profile api up -d --build
  ```

- **WireMock não acessível** ao rodar comandos dentro de `api/`:  
  Execute os comandos a partir da **raiz** do repositório para acessar todos os serviços do Compose.

---

## Licença
Uso acadêmico/avaliativo. Ajuste conforme necessidade.
