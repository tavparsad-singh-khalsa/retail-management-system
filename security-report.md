# Retail Management System — Security Audit & Hardening Report

**Date:** 2026-08-05
**Scope:** All 8 microservices (auth, product, purchase, sales, customer, billing, notification, report) + infrastructure (Docker Compose, PostgreSQL, secrets).

---

## Executive Summary

All 8 services were previously only protected by the auth-service's JWT layer; the other 7 services exposed **all endpoints without any authentication**. Following the audit, every service now validates a signed JWT on every protected endpoint, cross-service calls propagate the caller's `Authorization` header, hardcoded/default secrets were removed, exception detail leakage was eliminated, and Docker images run as a non-root user.

Verified end-to-end: unauthenticated and invalid-token requests are rejected with 403 on all services; valid-token flows (purchase receive → product inventory, sale → product deduct, billing → sales fetch, report generation) all pass.

---

## Findings & Fixes

### 1. CRITICAL — No authentication on 7 of 8 services
- **Finding:** product, purchase, sales, customer, billing, notification, report services had no Spring Security; every endpoint was anonymously callable.
- **Fix:** Added a shared security package to all 7 services (`JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`) mirroring the auth-service design:
  - product: `com.retail.product_service.security`
  - purchase: `com.retail.purchase_service.security`
  - sales: `com.retail.sales_service.security`
  - customer: `com.retail.customerservice.security`
  - billing: `com.retail.billingservice.security`
  - notification: `com.retail.notificationservice.security`
  - report: `com.retail.reportservice.security`
- `SecurityConfig`: stateless session, CSRF disabled (JWT API), `permitAll` only for `/actuator/health`, all other requests authenticated.
- Added `spring-boot-starter-security` + `jjwt-api/impl/jackson` (0.13.0) to all 7 POMs and `jwt.secret=${JWT_SECRET}` to each `application.properties`.

### 2. CRITICAL — JWT secret hardcoded with a default in docker-compose
- **Finding:** `JWT_SECRET:-replace-this-with-a-32-character-secret` — a known, forgeable secret.
- **Fix:** `.env` now contains a 64-character random `JWT_SECRET`. docker-compose requires it via `${JWT_SECRET:?}` (startup fails if unset). All 7 services receive `JWT_SECRET` from the environment.

### 3. CRITICAL — PostgreSQL password hardcoded default
- **Finding:** `DB_PASSWORD:-postgres` with user `postgres`.
- **Fix:** `.env` now has a 32-character random `DB_PASSWORD`; compose uses `${DB_PASSWORD:?}` for both `POSTGRES_PASSWORD` and each service's datasource. The live Postgres container password was rotated with `ALTER USER` to match.

### 4. CRITICAL — Hardcoded real owner identity in OwnerSeeder
- **Finding:** Seeder hardcoded a real name, real email/phone, and password `ChangeMe123` for the initial OWNER account.
- **Fix:** Credentials are now injected from config (`@Value`) with secure env-backed defaults in `auth-service/src/main/resources/application.properties` (`owner.default.*`). `.env` supplies a random 20-char `OWNER_DEFAULT_PASSWORD`; compose passes `OWNER_DEFAULT_*` to the auth-service. The seeder no longer embeds any personal identity.

### 5. HIGH — Exception detail leakage
- **Finding:** `server.error.include-message=always` and `include-binding-errors=always` on auth, product, purchase, sales, report services exposed internal exception text to clients. purchase-service additionally had **no** global exception handler, so raw stack-derived `e.getMessage()` (including internal messages like "Failed to communicate with Product Service") reached the client.
- **Fix:** Set `server.error.include-message=never` and `include-binding-errors=never` on all services. Added `GlobalExceptionHandler` + `ApiErrorResponse` to purchase-service (`com.retail.purchase_service.exception`) returning a generic message for unexpected errors.

### 6. HIGH — Cross-service calls did not propagate credentials
- **Finding:** RestClient beans in purchase, sales, billing, and report made calls to product/sales/customer services without an `Authorization` header, so once the target services were secured they would have failed.
- **Fix:** Added `AuthorizationHeaderPropagationInterceptor` (copies the current request's `Authorization` header onto outbound RestClient calls) in each service's config package and wired it into the RestClient definitions (product client in purchase/sales; sales+customer clients in billing; all 6 clients in report).

### 7. MEDIUM — Containers ran as root
- **Finding:** Dockerfiles used `FROM eclipse-temurin:21-jre` without a non-root user.
- **Fix:** All 8 Dockerfiles now create `appuser` (uid 10001), `chown` the app dir, and `USER appuser`.

### 8. Not a risk (verified)
- **SQL injection:** No string-concatenated queries exist. The only native queries are static `SELECT nextval(...)` calls (billing InvoiceRepository, report ReportLogRepository).
- **CORS:** No `@CrossOrigin` anywhere; no global CORS config → cross-origin browser access is not enabled.
- **Sensitive data in logs:** Logs include entity IDs, sale/invoice/report numbers, status codes — no passwords, tokens, or PII. (Note: auth-service `application.properties` still has `spring.jpa.show-sql=true`, verbose but not sensitive.)
- **Validation:** DTOs use `@Valid` + Jakarta validation annotations (`@NotNull`, `@Positive`, `@Email`, `@Pattern`, `@Size`, `@DecimalMin`) on all write endpoints; body validation is enforced on every service.
- **Swagger:** Not present in any service, so no swagger endpoints were exposed.

---

## Verification Results (live stack)

| Check | Result |
|---|---|
| `GET /actuator/health` (all 8) | 200 (public, intended) |
| Unauthenticated `GET` to protected endpoints (all 7) | 403 |
| Invalid/malformed Bearer token | 403 |
| `POST /auth/login` (owner) | 200, JWT issued |
| Authenticated `GET` (all 7 services) | 200 |
| Purchase create → approve → receive (calls product-service via propagated token) | stock 149 → 159 |
| Sale create (calls product-service deduct via propagated token) | stock 159 → 156; sale COMPLETED/PAID |
| Invoice create (billing → sales fetch via propagated token) | INV-2026-003001 |
| Report generation (report-service, token required) | REP-2026-000021, 3002 sales |
| Notification dashboard counts | OK |

All 8 services compile clean (`mvnw clean compile`).

---

## Residual / Recommended

1. **Single shared security module:** The security package is duplicated per service. Consider extracting a shared library/JAR.
2. **Role-based authorization:** Currently all authenticated users (any role) can call every endpoint. If fine-grained role enforcement (OWNER/MANAGER/CASHIER) is required, add `@PreAuthorize` and JWT role claim checks.
3. **`spring.jpa.show-sql=true`** remains on several services — disable for production to reduce log verbosity.
4. **Refresh tokens / token expiry tuning:** JWT expiration is set in the auth-service JwtService; review against session needs.
5. **Vault/secret manager:** `.env` is gitignored but consider a secrets manager (e.g., Docker Secrets / HashiCorp Vault) for production.
6. **Rate limiting** on `/auth/login` to slow credential-stuffing (account lockout after 5 failures exists in `UserSecurityService`).
7. **TLS:** All traffic is plain HTTP inside/outside the compose network; terminate TLS at a gateway in production.
