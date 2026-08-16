# Expense Intelligence Platform — Implementation Roadmap

> Phase 1 (complete): Project scaffolding, core entities, JWT auth, Docker infrastructure.

---

## Architecture Decisions & Trade-offs

### UUID vs BIGINT Primary Keys
**Chosen: UUID**
- Pros: Safe for distributed systems, no ID enumeration attacks, merge-friendly across imports
- Cons: Slightly larger indexes, less human-readable
- Alternative: BIGINT sequences — simpler and faster for single-DB monoliths

### Flyway vs Hibernate ddl-auto
**Chosen: Flyway with `ddl-auto: validate`**
- Pros: Version-controlled schema, reproducible deployments, safe production migrations
- Cons: Requires writing SQL migrations manually
- Alternative: Liquibase (XML/YAML) — better for large teams with complex branching

### JWT vs Session-based Auth
**Chosen: Stateless JWT**
- Pros: Horizontally scalable, works well with SPA/Next.js, no server-side session store
- Cons: Token revocation requires extra infrastructure (blocklist/short TTL)
- Alternative: OAuth2 + refresh tokens — better for third-party integrations (Phase 4)

### Monolith vs Microservices
**Chosen: Modular monolith (Phase 1–4)**
- Pros: Faster iteration, simpler deployment, sufficient for portfolio scale
- Cons: Must enforce module boundaries to avoid spaghetti
- Alternative: Extract AI/analytics service in Phase 5 if load demands it

### Gmail OAuth vs EIP JWT Login
**Chosen: Separate concerns**
- EIP login: email/password → JWT for API access
- Gmail connect: Google OAuth 2.0 → authorized Gmail API access for ingestion only
- Pros: Least-privilege scopes, clear user consent, independent token lifecycle
- Cons: Two auth flows to implement and explain in UI
- Alternative: "Sign in with Google" as primary login — conflates identity with Gmail data access

### Unified Ingestion vs Per-Source Transaction Logic
**Chosen: Shared `TransactionIngestionService` pipeline**
- CSV and Gmail produce `ParsedTransaction` → normalize → dedupe → persist
- Pros: No duplicate transaction creation logic; extensible for future sources (bank APIs)
- Cons: Requires upfront abstraction in EIP-602/604 before Gmail ships
- Alternative: Gmail-specific transaction table — rejected; violates single domain model

---

## Phase 2 — Core CRUD & Transaction Management
**Goal:** Users can manage their financial data end-to-end.

### Backend
| Task | Priority | Details |
|------|----------|---------|
| Transaction CRUD API | P0 | `POST/GET/PUT/DELETE /api/v1/transactions` with pagination, date filters |
| Category API | P0 | List system + user categories; create custom categories |
| Merchant API | P1 | Auto-normalize merchant names; deduplicate on import |
| Budget API | P1 | CRUD with period validation; compute spent vs. budget |
| User profile API | P1 | `GET/PUT /api/v1/users/me` |
| Security hardening | P0 | `@PreAuthorize` on all endpoints; user-scoped queries |
| Integration tests | P0 | Testcontainers + PostgreSQL for repository/service tests |

### Frontend
| Task | Priority | Details |
|------|----------|---------|
| Auth flow | P0 | Login/register forms, token storage, protected routes |
| Dashboard shell | P0 | Sidebar navigation, responsive layout |
| Transaction list | P0 | Paginated table with date/category filters |
| Transaction form | P0 | Create/edit modal with category picker |
| Category management | P1 | View system categories, create custom ones |
| Budget overview | P1 | Progress bars per category |

### Deliverables
- Full transaction lifecycle working in UI
- API documented in Swagger
- 80%+ test coverage on service layer

---

## Phase 3 — Data Ingestion (CSV & Gmail)
**Goal:** Import transactions from bank CSV exports and automated Gmail financial-email ingestion. Gmail is the primary automated ingestion experience; CSV remains supported as an alternative.

### Product Intent

Users should not have to manually download CSV files to get expenses into the system. The ideal flow:

```
User → Connect Gmail → Google OAuth consent → Gmail API
  → Discover financial emails → Extract transactions
  → Normalize → Deduplicate → Transaction
  → Analytics / Budgets / Insights / AI
```

Both Gmail and CSV feed the **same transaction ingestion pipeline** (see [ARCHITECTURE.md](./ARCHITECTURE.md#ingestion-architecture-target-state)).

### Backend — Shared Ingestion Foundation

| Task | Priority | Details |
|------|----------|---------|
| Import/sync job tracking | P0 | `ImportJob` entity (or generalized ingestion job): status, row/message counts, errors, `source` (`CSV`, `GMAIL`) |
| Shared ingestion pipeline | P0 | `ParsedTransaction` DTO + `TransactionIngestionService`: normalize → dedupe → persist (EIP-602/604) |
| `TransactionSource.GMAIL` | P0 | Extend enum + Flyway migration; source-aware transactions |
| Merchant normalization reuse | P0 | Gmail-derived transactions use same `MerchantNormalizer` / `findOrCreate` as CSV |

### Backend — CSV Import

| Task | Priority | Details |
|------|----------|---------|
| CSV upload endpoint | P0 | `POST /api/v1/imports/csv` with multipart file |
| Bank format registry | P0 | Strategy pattern: `BankFormatParser` interface |
| Built-in parsers | P0 | Chase, Bank of America, Wells Fargo, generic CSV |
| Duplicate detection | P0 | `(user_id, external_id)` via shared ingestion layer |
| Merchant extraction | P1 | Parse merchant from description; auto-create `Merchant` records |
| Async processing | P2 | Spring `@Async` or message queue for large files |

### Backend — Gmail Integration

| Task | Priority | Details |
|------|----------|---------|
| Gmail OAuth 2.0 | P0 | User connects Gmail; Google consent screen; least-privilege scopes; **not** the same as EIP login |
| Gmail connection management | P0 | `GmailConnection` entity: status, token lifecycle, sync state, disconnect/re-auth |
| Financial email discovery | P0 | Gmail API message listing/search; identify potentially financial emails only |
| Email extraction | P0 | Extract merchant, amount, currency, date, external ref; extensible per-merchant strategies |
| Normalization | P0 | Gmail path feeds shared ingestion pipeline (same as CSV) |
| Deduplication / idempotency | P0 | Gmail `messageId` (or equivalent) as `external_id`; safe re-processing |
| Background / incremental sync | P0 | Initial historical sync + incremental updates via Gmail history API; not synchronous HTTP |
| Failure / retry handling | P1 | Retries, partial processing, expired/revoked auth, recoverable failures |
| Privacy / security | P0 | Encrypted tokens, minimal email storage, user-controlled disconnect |

### Design Pattern — Unified Ingestion

```
                 DATA SOURCES
                      |
          +-----------+-----------+
          |                       |
        Gmail                    CSV
          |                       |
   GmailOAuthService          CsvImportService
   GmailDiscoveryService       BankFormatDetector
   GmailExtractionService    BankFormatParser (strategy)
          |                       |
          +-----------+-----------+
                      |
            ParsedTransaction (source-agnostic DTO)
                      |
         TransactionIngestionService
           ├── MerchantNormalizer / findOrCreate
           ├── DeduplicationService (user_id + external_id)
           └── TransactionRepository.persist
                      |
                  Transaction
```

### Frontend

| Task | Priority | Details |
|------|----------|---------|
| Gmail connect flow | P0 | Connect / disconnect / re-authorize Gmail (separate from login) |
| Gmail sync status | P0 | Connection status, last sync, sync-in-progress indicator |
| Import wizard (CSV) | P0 | Upload → preview → confirm → results |
| Import/sync history | P1 | List past CSV imports and Gmail sync jobs |
| Bank format selector | P1 | Auto-detect with manual override (CSV) |

### Deliverables
- CSV: import 1000+ rows in < 5 seconds; 3+ bank formats; duplicates skipped with summary
- Gmail: user can connect Gmail, run initial sync, receive transactions without CSV upload
- Gmail: incremental sync updates transactions without duplicates
- Both sources: transactions land in same `Transaction` table with correct `source`

### EIP Tickets (backend implementation order)

| Ticket | Scope |
|--------|-------|
| EIP-601 | ImportJob entity + migration (generalized for CSV + Gmail job tracking) |
| EIP-602 | Bank format strategy + generic CSV parser + **`ParsedTransaction` + ingestion interfaces** |
| EIP-603 | Chase, BofA, Wells Fargo parsers |
| EIP-604 | CSV upload + shared `TransactionIngestionService` (normalize → dedupe → persist) |
| EIP-605 | Merchant auto-extraction on CSV import |
| EIP-606 | Gmail OAuth 2.0 + `GmailConnection` + connect/disconnect/re-auth |
| EIP-607 | Gmail API financial email discovery (search/list, not full mailbox) |
| EIP-608 | Gmail transaction extraction (separate from transport; extensible strategies) |
| EIP-609 | Gmail sync, idempotency, incremental processing, retry/failure handling |

---

## Phase 4 — Analytics Engine
**Goal:** Rich spending analytics without AI.

### Backend
| Task | Priority | Details |
|------|----------|---------|
| Spending summary API | P0 | Total by period, category breakdown |
| Trend API | P0 | Month-over-month spending trends |
| Top merchants API | P1 | Ranked by spend amount |
| Budget vs actual API | P1 | Per-category budget utilization |
| Cash flow API | P2 | Income vs expense over time |
| Caching layer | P2 | Redis for expensive aggregation queries |

### Database
- Materialized views or summary tables for heavy aggregations
- Indexes on `(user_id, transaction_date, category_id)`

### Frontend
| Task | Priority | Details |
|------|----------|---------|
| Analytics dashboard | P0 | Summary cards, category pie chart |
| Trend charts | P0 | Recharts line/bar charts for monthly trends |
| Merchant leaderboard | P1 | Top 10 merchants table |
| Budget tracker | P1 | Visual progress rings per category |
| Date range picker | P0 | Filter all analytics by custom range |

### Deliverables
- Dashboard loads analytics in < 500ms
- Charts render correctly on mobile

---

## Phase 5 — AI-Powered Insights
**Goal:** "ChatGPT for your finances" — the differentiator.

### Backend
| Task | Priority | Details |
|------|----------|---------|
| Auto-categorization | P0 | ML/rules engine to categorize uncategorized transactions |
| Insight generation | P0 | Scheduled job: detect anomalies, trends, savings opportunities |
| Natural language query | P1 | `POST /api/v1/insights/ask` — "How much did I spend on food last month?" |
| Insight storage | P0 | `Insight` entity: type, severity, message, metadata |
| LLM integration | P1 | Azure OpenAI / OpenAI API with structured prompts |
| Prompt engineering | P0 | System prompts with user's aggregated data (not raw PII) |

### AI Architecture
```
User Question
  → Intent Classifier (spending query? comparison? advice?)
    → Data Aggregator (fetch relevant transactions/summaries)
      → LLM Prompt Builder (structured context + guardrails)
        → LLM Response → formatted insight
```

### Privacy Considerations
- Never send raw transaction descriptions to LLM without user consent
- Aggregate data before prompting (category totals, not individual rows)
- Option to disable AI features

### Frontend
| Task | Priority | Details |
|------|----------|---------|
| Insights feed | P0 | Card-based feed of AI-generated insights |
| Ask AI chat | P1 | Chat interface for natural language queries |
| Category suggestions | P1 | Accept/reject auto-categorization proposals |
| Insight notifications | P2 | Badge for unread insights |

### Deliverables
- Auto-categorize 80%+ of common merchants
- Generate 3+ actionable insights per month per active user
- NL query responds in < 3 seconds

---

## Phase 6 — Production Hardening
**Goal:** Deploy-ready, observable, secure.

| Area | Tasks |
|------|-------|
| **CI/CD** | GitHub Actions: build, test, Docker push, deploy |
| **Observability** | Structured logging (JSON), Azure App Insights / Prometheus metrics |
| **Security** | Rate limiting, CORS lockdown, JWT refresh tokens, HTTPS |
| **Performance** | Connection pooling (HikariCP tuning), query optimization |
| **Infrastructure** | Azure Container Apps / AWS ECS, managed PostgreSQL |
| **Documentation** | API versioning strategy, ADRs, runbook |

---

## Suggested Timeline

| Phase | Duration | Cumulative |
|-------|----------|------------|
| Phase 1 — Scaffold | 1 week | Week 1 |
| Phase 2 — Core CRUD | 2 weeks | Week 3 |
| Phase 3 — Data Ingestion (CSV + Gmail) | 3–4 weeks | Week 7–8 |
| Phase 4 — Analytics | 2 weeks | Week 10 |
| Phase 5 — AI Insights | 3 weeks | Week 13 |
| Phase 6 — Production | 2 weeks | Week 15 |

---

## Package Structure (Target State)

```
backend/src/main/java/com/expenseintelligence/
├── config/           # Security, OpenAPI, async, caching
├── controller/       # REST endpoints (thin)
├── dto/
│   ├── request/
│   └── response/
├── domain/
│   ├── entity/
│   └── enums/
├── exception/        # Global handler + custom exceptions
├── mapper/           # Entity ↔ DTO mappers
├── repository/       # Spring Data JPA
├── security/         # JWT filter, token provider
├── service/          # Business logic
│   ├── ingestion/    # Shared pipeline: normalize, dedupe, persist (Phase 3)
│   ├── import/       # CSV parsers, bank format strategies (Phase 3)
│   ├── gmail/        # OAuth, discovery, extraction, sync (Phase 3)
│   ├── analytics/    # Aggregation queries (Phase 4)
│   └── ai/           # LLM integration (Phase 5)
└── util/             # Shared utilities

frontend/src/
├── app/              # Next.js App Router pages
├── components/
│   ├── ui/           # shadcn/ui primitives
│   ├── layout/       # Shell, sidebar, header
│   ├── transactions/ # Feature components
│   ├── analytics/    # Charts, dashboards
│   └── insights/     # AI insight cards
├── hooks/            # Custom React hooks
├── lib/
│   ├── api/          # API client + endpoints
│   └── auth/         # Token management
└── types/            # TypeScript interfaces
```
