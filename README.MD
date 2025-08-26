# Code Group – Portfolio de Projetos

API Spring Boot para gestão de projetos, status, alocações por membro e cache de membros.

## Stack
- Java 17, Spring Boot 3.3
- JPA/Hibernate + PostgreSQL
- Flyway (migrations)
- OpenAPI (springdoc)
- Docker Compose

## Subir com Docker
```bash
docker compose --profile api build --no-cache
docker compose --profile api up -d --force-recreate
# logs
docker-compose logs -f api
```

## Subir **DB**:
```bash
docker compose --profile db up -d
```
