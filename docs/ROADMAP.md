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

## Phase 3 — CSV Import & Multi-Bank Support
**Goal:** Import transactions from real bank exports.

### Backend
| Task | Priority | Details |
|------|----------|---------|
| CSV upload endpoint | P0 | `POST /api/v1/imports/csv` with multipart file |
| Bank format registry | P0 | Strategy pattern: `BankFormatParser` interface |
| Built-in parsers | P0 | Chase, Bank of America, Wells Fargo, generic CSV |
| Import job tracking | P1 | `ImportJob` entity: status, row count, errors |
| Duplicate detection | P0 | Match on `(user_id, external_id)` or fuzzy date+amount+description |
| Merchant extraction | P1 | Parse merchant from description; auto-create `Merchant` records |
| Async processing | P2 | Spring `@Async` or message queue for large files |

### Design Pattern
```
CsvImportService
  └── BankFormatDetector (sniffs headers)
        └── BankFormatParser (strategy per bank)
              └── TransactionMapper → persist batch
```

### Frontend
| Task | Priority | Details |
|------|----------|---------|
| Import wizard | P0 | Upload → preview → confirm → results |
| Import history | P1 | List past imports with status/errors |
| Bank format selector | P1 | Auto-detect with manual override |

### Deliverables
- Import 1000+ row CSV in < 5 seconds
- Support 3+ bank formats out of the box
- Duplicate rows silently skipped with summary

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
| Phase 3 — CSV Import | 2 weeks | Week 5 |
| Phase 4 — Analytics | 2 weeks | Week 7 |
| Phase 5 — AI Insights | 3 weeks | Week 10 |
| Phase 6 — Production | 2 weeks | Week 12 |

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
│   ├── import/       # CSV import (Phase 3)
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
