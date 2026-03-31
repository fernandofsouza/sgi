# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build (tests skipped por padrão)
mvn clean package

# Run locally (requires application-local.yml with DB config)
mvn spring-boot:run

# Testes unitários (H2 em memória)
mvn test -DskipTests=false

# Testes de integração contra PostgreSQL real (Heroku add-on ou local)
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/sgi_test
export JDBC_DATABASE_USERNAME=postgres
export JDBC_DATABASE_PASSWORD=senha
mvn verify -P integration-test

# Um único teste de integração
mvn verify -P integration-test -Dit.test=IndicadorRepositoryIT
```

> `pom.xml` tem `<maven.test.skip>true</maven.test.skip>` por padrão — use `-DskipTests=false` para unitários ou `-P integration-test` para integração.

**Testes de integração — convenções:**
- Arquivos nomeados `*IT.java` (ex: `IndicatorRepositoryIT.java`)
- Estendem `AbstractIntegrationTest` (aplica `@SpringBootTest` + perfil `integration-test`)
- O Flyway executa `clean()` + `migrate()` ao subir o contexto Spring (schema sempre limpo)
- Métodos que alteram dados devem usar `@Transactional` para rollback automático

## Architecture

Spring Boot 3.1.12 / Java 21 backend for **SGI** (Sistema de Gestão de Indicadores — an indicators management system).

**Package root:** `br.gov.sgi`

Layered architecture: `controller → service → repository → entity`, plus `dto/`, `config/`, and `exception/` packages. Only `config/SecurityConfig.java` is currently implemented; the remaining layers are scaffolded through tests.

**Database:**
- Production (Heroku): PostgreSQL, credentials injected via `JDBC_DATABASE_*` env vars
- On-premise alternative: SQL Server (mssql-jdbc driver included)
- Tests: H2 in-memory (PostgreSQL compatibility mode), schema managed by Hibernate (`create-drop`), Flyway disabled
- Migrations live in `src/main/resources/db/migration/` (Flyway, V-prefixed SQL files)

**Profiles:**
- `heroku` — activated by `Procfile` on deploy; reads env vars, HikariCP max 5 connections, Flyway auto-runs
- `test` — loaded via `@ActiveProfiles("test")` in tests; uses H2, all endpoints public, CORS open
- `integration-test` — testes de integração contra PostgreSQL real; requer `JDBC_DATABASE_*` env vars

**Security:** Spring Security with stateless JWT sessions. Optional Entra ID (Azure AD) OAuth2 integration controlled by the `ENTRA_ID_ENABLED` env var. CORS origins configured via `CORS_ALLOWED_ORIGINS`.

**API prefix:** All REST endpoints are under `/api`.

## Heroku Deployment

```bash
git push heroku <branch>:main
heroku logs --tail
```

Required config vars: `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`, `CORS_ALLOWED_ORIGINS`. For Entra ID: `ENTRA_ID_ENABLED=true`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_APP_ID_URI`, `JWT_ISSUER_URI`.
