# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build (tests currently skipped in pom.xml)
mvn clean package

# Run locally (requires application-local.yml with DB config)
mvn spring-boot:run

# Run all tests (override the skip flag)
mvn test -DskipTests=false

# Run a single test class
mvn test -Dtest=IndicatorServiceTest -DskipTests=false
```

> Note: `pom.xml` has `<maven.test.skip>true</maven.test.skip>` — always pass `-DskipTests=false` to actually run tests.

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

**Security:** Spring Security with stateless JWT sessions. Optional Entra ID (Azure AD) OAuth2 integration controlled by the `ENTRA_ID_ENABLED` env var. CORS origins configured via `CORS_ALLOWED_ORIGINS`.

**API prefix:** All REST endpoints are under `/api`.

## Heroku Deployment

```bash
git push heroku <branch>:main
heroku logs --tail
```

Required config vars: `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`, `CORS_ALLOWED_ORIGINS`. For Entra ID: `ENTRA_ID_ENABLED=true`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_APP_ID_URI`, `JWT_ISSUER_URI`.
