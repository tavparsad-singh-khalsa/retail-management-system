# Production Readiness Checklist — Retail Management System

**Date:** 2026-08-05
**Scope:** 8 microservices + Docker Compose + PostgreSQL + build/docs.
**Legend:** ✅ PASS · ⚠️ PARTIAL · ❌ FAIL/ACTION NEEDED · ℹ️ NOTE

---

## 1. Docker

| # | Item | Status | Detail |
|---|---|---|---|
| D1 | Multi-stage builds | ✅ | All 8 services: `maven:3.9.9-eclipse-temurin-21` build → `eclipse-temurin:21-jre` runtime. |
| D2 | Non-root runtime user | ✅ | All 8: `USER appuser` (uid 10001) after useradd+chown. |
| D3 | Consistent base images | ⚠️ | Pinned minor tags (`eclipse-temurin:21-jre`, not digest/`-jammy` etc.). Acceptable; consider digest pinning. |
| D4 | `.dockerignore` | ❌ | None anywhere. Build context includes `target/`, `.git/`, `graphify-out/`, etc. Add `.dockerignore` (e.g. `target/`, `.git/`, `.idea/`) to speed builds and avoid leaking context. |
| D5 | Reproducible builds | ⚠️ | `mvn -q -DskipTests package` — no `--batch-mode`, no dependency cache layer (`COPY pom.xml` + `RUN mvn dependency:go-offline`). Cache layer would speed rebuilds. |
| D6 | Healthcheck in image | ❌ | No `HEALTHCHECK` in any Dockerfile (compose healthcheck for postgres only). |
| D7 | EXPOSE correctness | ✅ | Ports match `server.port` (9091–9098). |

## 2. Docker Compose

| # | Item | Status | Detail |
|---|---|---|---|
| C1 | Secrets via env, no defaults | ✅ | `DB_PASSWORD`, `JWT_SECRET`, `OWNER_DEFAULT_PASSWORD` use `${VAR:?}` (fail fast). Verified `.env` not tracked by git. |
| C2 | `.env.example` completeness | ❌ | `docker-compose.yml` requires `OWNER_DEFAULT_PASSWORD` (`:?`), but `.env.example` only documents `DB_USERNAME/DB_PASSWORD/JWT_SECRET` (and README shows the same 3 vars). A fresh `cp .env.example .env` → `docker compose up` **fails**. Update both. |
| C3 | Inter-service dependency health | ⚠️ | postgres uses `service_healthy`; app services use `service_started` (no app healthcheck). App-to-app depends_on won't wait for readiness → transient connection errors at boot. Add healthchecks to app services + use `service_healthy`. |
| C4 | Restart policy | ❌ | No `restart:` on any service. Container crash/reboot leaves services down. Add `restart: unless-stopped`. |
| C5 | Resource limits | ❌ | No `mem_limit`/`cpus`/`deploy.resources`. |
| C6 | Port exposure | ℹ️ | All 8 services published to host (19091–19098). For real prod, expose only a gateway/load-balancer front. |
| C7 | Networks | ℹ️ | Uses default bridge. Consider an explicit internal network (no host port mapping for internal services). |
| C8 | Volume for DB | ✅ | Named volume `postgres-data`. Init script mounted read-only into initdb.d. |
| C9 | `Customer_db` casing | ⚠️ | DB `Customer_db` (mixed case) and service URLs match, but inconsistent naming (`sales_db`, `retail_*`). Functionally fine; cosmetic. |
| C10 | TLS | ❌ | All traffic plain HTTP (internal + host ports). Terminate TLS at ingress in production. |

## 3. Health Endpoints / Actuator

| # | Item | Status | Detail |
|---|---|---|---|
| H1 | Actuator dependency | ✅ | `spring-boot-starter-actuator` present in all 8 poms. |
| H2 | Health endpoint public | ✅ | `/actuator/health` permitted in every SecurityConfig. Live: all return 200 `{"status":"UP"}`. |
| H3 | Exposure consistency | ⚠️ | Only notification + report set `management.endpoints.web.exposure.include=health,info,metrics`; other 6 rely on default (health only). Decide once globally. |
| H4 | Deep health (DB component) | ⚠️ | `health` shows only `{"status":"UP"}` — DB sub-status hidden (no `show-details`). If DB is down, health flips to DOWN (good), but details/liveness-readiness probes not configured. |
| H5 | Liveness/readiness probes | ❌ | `management.endpoint.health.probes.enabled` not set; no `/actuator/health/liveness` & `/readiness`. Needed for orchestration (K8s) or container healthchecks. |

## 4. Logging

| # | Item | Status | Detail |
|---|---|---|---|
| L1 | Logback config | ❌ | No `logback-spring.xml`/`application.yml` logging config anywhere. Defaults only (console, no rotation, no file). |
| L2 | Log rotation / file appenders | ❌ | None. Long-running containers rely on Docker log driver; no app-level rotation, no JSON/structured logging. |
| L3 | SQL logging | ⚠️ | `spring.jpa.show-sql=true` in auth, product, purchase, sales, customer, report (report + format_sql). Verbose in prod; disable or set to a profile. |
| L4 | Sensitive data in logs | ✅ | No passwords/tokens logged (verified by security review). IDs/status codes only. |
| L5 | Access/request logging | ❌ | No request logging, no metrics to Prometheus (Micrometer not configured; only `metrics` exposure on 2 services). |

## 5. Configuration

| # | Item | Status | Detail |
|---|---|---|---|
| CF1 | `ddl-auto` strategy | ❌ | **5 services use `ddl-auto=update`** (auth, product, purchase, sales, customer); only billing/notification/report use `validate` + Flyway. `update` is unsafe for prod (uncontrolled schema drift). Move all to Flyway + `validate`. |
| CF2 | `open-in-view` | ⚠️ | Set `false` only in auth, product, purchase. Others default `true` (startup warning). Set consistently to `false`. |
| CF3 | Error message inclusion | ✅ | `server.error.include-message=never` + `include-binding-errors=never` in auth, product, purchase; others rely on GlobalExceptionHandler. Acceptable but inconsistent — standardize. |
| CF4 | Profiles | ❌ | Single `application.properties` per service; no `dev`/`prod` profiles or config server. Env-var overrides work, but no environment-specific file separation. |
| CF5 | JWT secret binding | ✅ | All services read `jwt.secret=${JWT_SECRET}`. Expiration only defined in auth (`jwt.expiration=86400000`), shared by construction. |
| CF6 | Datasource URL override | ✅ | Compose injects `SPRING_DATASOURCE_URL`; local default is `localhost:5432`. |

## 6. Environment Variables

| # | Item | Status | Detail |
|---|---|---|---|
| E1 | `.env` git-ignored | ✅ | `.gitignore` ignores `.env`, `.env.*` (except `.env.example`). Confirmed not tracked. |
| E2 | Example file accurate | ❌ | `.env.example` missing `OWNER_DEFAULT_*` (esp. required `OWNER_DEFAULT_PASSWORD`). Copying it breaks compose. |
| E3 | Required-vs-optional documented | ⚠️ | Comments explain purpose but not required vs optional; `OWNER_DEFAULT_EMAIL/PHONE` empty by default. |
| E4 | No secret in code | ✅ | Grep found no hardcoded password/secret literals. |

## 7. README / Docs

| # | Item | Status | Detail |
|---|---|---|---|
| R1 | Architecture & service tables | ✅ | Accurate mermaid diagrams, ports, DB mapping, structure. |
| R2 | Startup instructions | ⚠️ | Covers manual `mvn spring-boot:run` per service, but **does not document `docker compose up`** (still listed as a "Future Enhancement"!) even though `docker-compose.yml` exists. |
| R3 | `.env` setup instructions | ❌ | Documents only 3 vars; `OWNER_DEFAULT_PASSWORD` (required) missing. |
| R4 | Authentication in API overview | ⚠️ | API tables list endpoints but don't mention Bearer-token requirement for the 7 secured services. |
| R5 | Build command accuracy | ⚠️ | Says `mvn clean install -DskipTests` at repo root, but there is **no root `pom.xml`** — must build per service. |
| R6 | Empty/placeholder docs | ❌ | `CHANGELOG.md` and `CONTRIBUTING.md` are 0 bytes. Fill in or remove. |
| R7 | Feature completeness | ℹ️ | README claims JUnit 5 + H2 testing; only 5 context-load tests exist (see M1). |

## 8. Maven

| # | Item | Status | Detail |
|---|---|---|---|
| M1 | Tests | ❌ | Only trivial `contextLoads()` tests in auth, product, purchase, sales, customer, billing (2 each max). **notification & report have no `src/test`**. No domain/service tests despite README's "JUnit 5 + H2" claim. |
| M2 | Maven wrapper | ✅ | `mvnw` + `mvnw.cmd` + `.mvn/` in all 8 services. |
| M3 | Parent/BOM | ✅ | All use `spring-boot-starter-parent` 3.5.16, Java 21. |
| M4 | Duplicate jjwt version | ℹ️ | `jjwt` 0.13.0 pinned in all 8 poms (auth original + 7 copies). Centralize via property or parent POM. |
| M5 | No root aggregator POM | ⚠️ | No multi-module build. Each service built separately; a root POM or script would help. |
| M6 | CI | ❌ | No GitHub Actions/CI config at all. |
| M7 | Devtools in auth pom | ⚠️ | `spring-boot-devtools` (runtime) in auth-service. Fine locally; ensure it's excluded in prod image (it is `optional`, so not packaged). |

## 9. Flyway

| # | Item | Status | Detail |
|---|---|---|---|
| F1 | Migration coverage | ❌ | Flyway only in **billing, notification, report**. auth/product/purchase/sales/customer rely on `ddl-auto=update` — no versioned schema. |
| F2 | Existing migrations | ✅ | billing V1/V2 (invoices, seq), notification V1, report V1/V2. Validated + up to date on live DB. |
| F3 | `validate` on Flyway services | ✅ | billing/notification/report use `ddl-auto=validate`. |
| F4 | Baseline settings | ⚠️ | report uses `flyway.baseline-on-migrate=true`; billing/notification don't. Inconsistent; document the intended baseline policy. |
| F5 | Migration naming/history | ✅ | `V1__...` / `V2__...` style, correct `flyway_schema_history` behavior. |

## 10. PostgreSQL

| # | Item | Status | Detail |
|---|---|---|---|
| P1 | DB-per-service (isolated domains) | ✅ | 6 databases; shared `retail_management` for auth/product/purchase (documented shared pattern). |
| P2 | Init script | ✅ | `docker/postgres/init-databases.sh` creates all 6 DBs idempotently-ish (will error if DB exists, but runs only on fresh volume). |
| P3 | Credentials | ✅ | Strong random `DB_PASSWORD` in `.env`, required via compose; verified live. |
| P4 | Healthcheck | ✅ | `pg_isready` with 5s interval/20 retries. |
| P5 | Backups / PITR | ❌ | No backup/restore strategy, no WAL archiving, no `pg_dump` scripts. |
| P6 | Resource limits | ❌ | No memory/cpu constraints on postgres container. |
| P7 | Connection pooling | ℹ️ | Hikari defaults; no pool size/tuning configured per service. |
| P8 | Flyway on shared DB | ⚠️ | `retail_management` hosts 3 services; if you add Flyway, coordinate migrations across services sharing that DB. |

## 11. Error Handling

| # | Item | Status | Detail |
|---|---|---|---|
| EH1 | GlobalExceptionHandler presence | ✅ | All 8 services have `@RestControllerAdvice` + `ApiErrorResponse`. |
| EH2 | Consistent error contract | ❌ | **Two different shapes**: auth-service uses `ErrorResponse` (status/message/timestamp); other 7 use `ApiErrorResponse` (timestamp/status/error/message/path/validationErrors). Clients get different bodies from auth vs others. |
| EH3 | Specific vs generic handlers | ⚠️ | product/purchase/customer have rich handlers (Validation, Conflict, NotFound, BusinessRule, MethodNotSupported, TypeMismatch). sales/billing/notification/report lean on `handleAll` + a few typed. Consistent coverage would help. |
| EH4 | Validation enforced | ✅ | `@Valid` on all write endpoints; Jakarta constraints on DTOs. |
| EH5 | Generic exception messages | ✅ | `handleAll`/`handleGlobalException` return generic messages (security work done). |

## 12. Code Duplication

| # | Item | Status | Detail |
|---|---|---|---|
| CD1 | JWT security module | ❌ | `JwtService` + `JwtAuthenticationFilter` + `SecurityConfig` copied into **7 services** (byte-identical except package name). High risk of drift. Extract a shared `security-common` library. |
| CD2 | Header propagation interceptor | ⚠️ | `AuthorizationHeaderPropagationInterceptor` duplicated in purchase, sales, billing, report (identical logic). |
| CD3 | `ApiErrorResponse` | ❌ | Duplicated in 7 services. |
| CD4 | GlobalExceptionHandler | ⚠️ | 7 near-identical handlers. |
| CD5 | RestClientConfig | ⚠️ | Duplicated per service with minor variations (some use `JdkClientHttpRequestFactory`, report uses `SimpleClientHttpRequestFactory`). Unify client/factory. |
| CD6 | DTO/mapper duplication | ⚠️ | Request/response DTOs (e.g. sale, invoice, integration payloads) re-defined per service. |

---

## Top 10 Actions Before Production

1. **Add Flyway to all services** (auth, product, purchase, sales, customer) and switch `ddl-auto` from `update` → `validate` (CF1/F1).
2. **Fix `.env.example`/README** to include required `OWNER_DEFAULT_PASSWORD` + all `OWNER_DEFAULT_*` vars (C2/R3/E2).
3. **Add app healthchecks** to Dockerfiles or compose + use `service_healthy` in `depends_on` (D6/C3); enable liveness/readiness probes (H5).
4. **Add `restart: unless-stopped`** and resource limits to compose (C4/C5).
5. **Add `.dockerignore`** to each service (D4).
6. **Standardize error contract**: one shared `ApiErrorResponse` shape across all 8 services (EH2).
7. **Extract shared libraries** for security (JWT) + error handling to eliminate 7× duplication (CD1/CD3).
8. **Add real tests** (service/controller/integration) esp. for notification & report which have none (M1).
9. **Add CI pipeline** (build + test + docker build) — none exists (M6).
10. **Add logging config** (logback JSON/rotation) + disable `show-sql`; consider Micrometer/Prometheus (L1/L2/L3/L5).

## Already Production-Ready ✅

- Secrets enforced via required env vars; no hardcoded credentials.
- JWT auth on all endpoints of all 8 services; health endpoint public only.
- Non-root containers; multi-stage builds; consistent JRE runtime.
- Error detail leakage suppressed; generic messages for unexpected errors.
- Postgres healthcheck + named persistent volume + idempotent DB init script.
- All 8 services compile clean and run; end-to-end workflow verified (purchase→inventory, sale→deduct, billing, report).
