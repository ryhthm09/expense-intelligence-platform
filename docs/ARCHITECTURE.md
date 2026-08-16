# Expense Intelligence Platform — Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│  Next.js 16 (App Router) + React Query + TailwindCSS + Recharts │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS / REST (JWT Bearer)
┌────────────────────────────▼────────────────────────────────────┐
│                        API Layer                                 │
│  Spring Boot 3.3 · Spring Security · OpenAPI/Swagger            │
│  Controllers → Services → Repositories                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
┌────────────────────────────▼────────────────────────────────────┐
│                      Data Layer                                  │
│  PostgreSQL 16 · Flyway Migrations · JPA/Hibernate              │
└─────────────────────────────────────────────────────────────────┘
```

## Ingestion Architecture (Target State)

All automated data sources converge on a single transaction pipeline. Gmail and CSV are transport/parsing layers — not separate transaction domains.

```
                    DATA SOURCES
                         |
         +---------------+---------------+
         |                               |
       Gmail                            CSV
         |                               |
   Gmail API + OAuth              Bank format parsers
   Email discovery                (strategy pattern)
   Email extraction               |
         |                               |
         +---------------+---------------+
                         |
              Source-specific adapters
                         |
              ParsedTransaction (DTO)
                         |
              Transaction Ingestion Layer
              (normalize → dedupe → persist)
                         |
                   Transaction
                         |
         +---------------+---------------+
         |               |               |
     Analytics        Budgets           AI
```

### Ingestion Pipeline Stages

| Stage | Responsibility | Shared across sources? |
|-------|----------------|------------------------|
| **Acquire** | Gmail API, CSV upload | Per-source |
| **Discover** | Find financial emails (Gmail only) | Gmail-specific |
| **Extract** | Parse raw input → `ParsedTransaction` | Per-source strategies |
| **Normalize** | Merchant normalization, currency, type | Shared |
| **Deduplicate** | Idempotent ingest via `(user_id, source, external_id)` | Shared |
| **Persist** | Create `Transaction` records | Shared |

**Architectural rule:** Gmail does not create a separate "Gmail transaction" entity. Both Gmail and CSV produce `Transaction` rows with a source-aware `TransactionSource` enum.

## Backend Package Structure

```
com.expenseintelligence/
├── config/          Security, JWT properties, OpenAPI, JPA auditing
├── controller/      REST endpoints (thin — delegate to services)
├── dto/
│   ├── request/     Validated input DTOs
│   └── response/    Output DTOs + error responses
├── domain/
│   ├── entity/      JPA entities (User, Transaction, Category, Merchant, Budget, …)
│   └── enums/       Domain enumerations
├── exception/       Global exception handler + custom exceptions
├── mapper/          Entity ↔ DTO conversion
├── repository/      Spring Data JPA repositories
├── security/        JWT filter, token provider, UserPrincipal
└── service/         Business logic
    ├── ingestion/   Shared pipeline (normalize, dedupe, persist)     [planned]
    ├── import/      CSV parsers, bank format strategies              [planned]
    └── gmail/       OAuth, discovery, extraction, sync              [planned]
```

## Data Model

```
User (1) ──────< Transaction (N)
  │                    │
  │                    ├──> Category (N:1)
  │                    └──> Merchant (N:1)
  │
  ├──< Category (N)     [user-specific custom categories]
  ├──< Merchant (N)     [user-specific merchants]
  ├──< Budget (N) ──> Category (N:1, optional)
  ├──< GmailConnection (N)  [planned — separate from User auth]
  └──< ImportJob (N)        [ingestion/sync job tracking — CSV + Gmail]
```

### Planned: GmailConnection

Represents a user's authorized Gmail access — **separate from EIP User authentication** (JWT login ≠ Gmail OAuth consent).

| Field (conceptual) | Purpose |
|--------------------|---------|
| `userId` | Owning EIP user |
| `emailAddress` | Connected Gmail account |
| `status` | `CONNECTED`, `DISCONNECTED`, `REAUTH_REQUIRED`, `TOKEN_EXPIRED` |
| `accessToken` / `refreshToken` | Encrypted at rest |
| `tokenExpiresAt` | Access token expiry |
| `lastSyncAt` | Last successful sync timestamp |
| `historyId` / sync cursor | Gmail incremental sync state |

### TransactionSource (source-aware transactions)

Current values: `MANUAL`, `CSV_IMPORT`, `API`

Planned addition: `GMAIL` (requires Flyway migration to extend `chk_transactions_source`)

Future sources (e.g. bank/financial-data APIs) extend this enum without redesigning the `Transaction` entity.

### Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Primary keys | UUID | Safe for distributed imports, no enumeration |
| Schema management | Flyway | Version-controlled, production-safe migrations |
| Auth | Stateless JWT | Horizontally scalable, SPA-friendly |
| Gmail auth | Separate Google OAuth 2.0 | Least-privilege Gmail scopes; distinct from EIP login |
| Password hashing | BCrypt | Industry standard, built into Spring Security |
| Amount storage | NUMERIC(19,4) | Precise decimal arithmetic for financial data |
| System categories | `is_system=true, user_id=NULL` | Shared defaults, user can add custom ones |
| Import dedup | `(user_id, external_id)` unique | Prevents duplicate imports per user |
| Gmail dedup | Gmail `messageId` as `external_id` | Idempotent re-processing of same email |
| Email storage | Extracted fields only | Minimize stored PII; do not persist full email bodies by default |

## Security Model

- All endpoints except `/api/v1/auth/**`, `/api/v1/health`, and Swagger are protected
- JWT contains `userId` as subject, validated on every request
- `UserPrincipal` loaded from DB on each authenticated request
- CORS restricted to `http://localhost:3000` (configurable per environment)
- Passwords never returned in API responses

### Gmail OAuth Security (planned)

- **Separate concern:** EIP JWT login and Gmail OAuth authorization are not the same flow
- **Least-privilege scopes:** Use Gmail API scopes appropriate for reading financial emails only (e.g. `gmail.readonly` — exact scope set to be decided)
- **Token storage:** Encrypt access/refresh tokens at rest; never log tokens
- **User control:** Disconnect flow revokes EIP-side tokens and stops sync
- **Data minimization:** Process only required transaction fields; avoid storing full email content
- **Sync isolation:** Background Gmail sync runs outside synchronous HTTP request lifecycle

## API Versioning

All endpoints are prefixed with `/api/v1/`. Future breaking changes will use `/api/v2/`.

## Frontend Architecture

```
src/
├── app/              Next.js App Router (pages)
├── components/
│   ├── ui/           Reusable UI primitives (shadcn-style)
│   ├── layout/       App shell, sidebar
│   └── auth/         Protected route wrapper
├── lib/
│   ├── api/          HTTP client + endpoint functions
│   └── auth/         Token storage + React context
└── types/            TypeScript interfaces
```

- **React Query** for server state management (ready for Phase 2 CRUD)
- **Auth context** for client-side session (JWT in localStorage)
- **Protected routes** redirect unauthenticated users to `/login`
- **Gmail connect UI** (planned): separate from login/register — triggers Google OAuth consent
