# Expense Intelligence Platform — Daily Planning Guide

> **Your daily reference for what to work on next.**  
> Check off tickets as you complete them. Ask the AI to review your code after each ticket.

**Related docs:**
- [ROADMAP.md](./ROADMAP.md) — phase goals & high-level scope
- [ARCHITECTURE.md](./ARCHITECTURE.md) — system design & data model

---

## How to Use This File

1. **Start at the top** of the ticket list — each ticket builds on the previous one.
2. **Pick one ticket** per evening (2–4 hrs) or one per weekend (6–10 hrs).
3. **Check the box** `[x]` when done and add the completion date.
4. **Paste your diff/branch** in chat and ask: *"Review my implementation for EIP-XXX"*
5. Update the **Current Focus** section below when you start a new ticket.

---

## Current Focus

| Field | Value |
|-------|-------|
| **Active Ticket** | `EIP-101` — @CurrentUser Security Utility |
| **Milestone** | M1 — Secure Foundation & User Context |
| **Started** | _fill in when you begin_ |
| **Status** | 🔲 Not started |

---

## This Week (update manually)

| Day | Ticket | Goal | Done? |
|-----|--------|------|-------|
| Mon | EIP-101 | `@CurrentUser` + `SecurityUtils` | ☐ |
| Tue | EIP-103 | Testcontainers setup (start) | ☐ |
| Wed | EIP-103 | Testcontainers setup (finish) | ☐ |
| Thu | EIP-102 | User profile API | ☐ |
| Fri | — | Buffer / review | ☐ |
| Sat | EIP-201 | List categories API | ☐ |
| Sun | EIP-202 | Custom category CRUD | ☐ |

---

## Progress Overview

| Milestone | Tickets | Done | Progress |
|-----------|---------|------|----------|
| M1 — Secure Foundation | 101–103 | 0/3 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M2 — Categories & Transactions | 201–303 | 0/5 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M3 — Merchants & Budgets | 401–502 | 0/4 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M4 — CSV Import | 601–605 | 0/5 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M5 — Analytics | 701–705 | 0/5 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M6 — AI & Intelligence | 801–804 | 0/4 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |
| M7 — Production Readiness | 901–904 | 0/4 | ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0% |

**Total: 0 / 30 tickets complete**

---

## Suggested Schedule (8-week pace)

| Week | Focus | Tickets |
|------|-------|---------|
| 1 | Foundation | EIP-101, 103, 102 |
| 2 | Categories | EIP-201, 202 |
| 3 | Transactions | EIP-301, 302, 303 |
| 4 | Merchants & Budgets | EIP-401, 402, 501 |
| 5 | Budgets + Import start | EIP-502, 601, 602 |
| 6 | Import pipeline | EIP-603, 604, 605 |
| 7 | Analytics | EIP-701, 702, 703, 704, 705 |
| 8 | AI + Production | EIP-801, 802, 803, 901, 904 |

Weeks 9–10 (optional): EIP-804, 902, 903

---

## Full Ticket Backlog (in order)

### Milestone 1 — Secure Foundation & User Context

- [ ] **EIP-101** · `@CurrentUser` Security Utility & Request Context  
  🌙 Evening · Prereq: Phase 1 auth  
  **Goal:** Resolve authenticated user in controllers without boilerplate  
  **Key deliverable:** `HandlerMethodArgumentResolver`, `SecurityUtils.getCurrentUserId()`

- [ ] **EIP-103** · Testcontainers Integration Test Foundation  
  🏖️ Weekend · Prereq: Flyway V1  
  **Goal:** PostgreSQL Testcontainers + Flyway in test suite  
  **Key deliverable:** Reusable `@SpringBootTest` base, one repository integration test

- [ ] **EIP-102** · User Profile API  
  🌙 Evening · Prereq: EIP-101  
  **Goal:** `GET/PUT /api/v1/users/me`  
  **Key deliverable:** `UserService`, `UserController`, no password in response

---

### Milestone 2 — Category & Transaction Core

- [ ] **EIP-201** · List Categories (System + User-Scoped)  
  🌙 Evening · Prereq: EIP-101, 103  
  **Goal:** `GET /api/v1/categories` with optional `?type=` filter  
  **Key deliverable:** `CategoryService`, `CategoryMapper`, `CategoryResponse`

- [ ] **EIP-202** · Custom Category CRUD  
  🌙 Evening · Prereq: EIP-201  
  **Goal:** `POST/PUT/DELETE /api/v1/categories`  
  **Key deliverable:** Cannot modify system categories; user-scoped ownership

- [ ] **EIP-301** · Create Transaction  
  🌙 Evening · Prereq: EIP-201, 101  
  **Goal:** `POST /api/v1/transactions`  
  **Key deliverable:** Validation, category ownership check, `source=MANUAL`

- [ ] **EIP-302** · List Transactions (Paginated + Filters)  
  🌙 Evening · Prereq: EIP-301  
  **Goal:** `GET /api/v1/transactions` with page, date, category, type filters  
  **Key deliverable:** Pagination metadata, user isolation

- [ ] **EIP-303** · Get, Update, Delete Single Transaction  
  🌙 Evening · Prereq: EIP-301, 302  
  **Goal:** `GET/PUT/PATCH/DELETE /api/v1/transactions/{id}`  
  **Key deliverable:** Cross-user access returns 404

---

### Milestone 3 — Merchants & Budgets

- [ ] **EIP-401** · Merchant Name Normalization Utility  
  🌙 Evening · Prereq: EIP-101  
  **Goal:** `MerchantNormalizer` pure function  
  **Key deliverable:** 15+ unit test cases

- [ ] **EIP-402** · Merchant CRUD & Find-or-Create  
  🌙 Evening · Prereq: EIP-401, 301  
  **Goal:** `GET/POST/PUT/DELETE /api/v1/merchants` + `findOrCreate()`  
  **Key deliverable:** Dedup via `normalizedName`

- [ ] **EIP-501** · Budget CRUD  
  🏖️ Weekend · Prereq: EIP-202, 301  
  **Goal:** Full budget lifecycle APIs  
  **Key deliverable:** Period validation, auto `endDate`, overlap rules

- [ ] **EIP-502** · Budget Utilization (Spent vs. Budget)  
  🌙 Evening · Prereq: EIP-501, 302  
  **Goal:** `GET /api/v1/budgets/{id}/utilization`  
  **Key deliverable:** Sum DEBIT transactions in budget period

---

### Milestone 4 — CSV Import Pipeline

- [ ] **EIP-601** · ImportJob Entity & Migration  
  🌙 Evening · Prereq: EIP-103, 301  
  **Goal:** Flyway V2, `ImportJob` entity + repository  
  **Key deliverable:** `ImportStatus` enum

- [ ] **EIP-602** · Bank Format Strategy & Generic CSV Parser  
  🏖️ Weekend · Prereq: EIP-601  
  **Goal:** `BankFormatParser` interface + `GenericCsvParser`  
  **Key deliverable:** `BankFormatDetector`, CSV test fixtures

- [ ] **EIP-603** · Bank-Specific Parsers (Chase, BofA, Wells Fargo)  
  🏖️ Weekend · Prereq: EIP-602  
  **Goal:** Three real bank parsers  
  **Key deliverable:** Anonymized CSV fixtures in `src/test/resources/csv/`

- [ ] **EIP-604** · CSV Upload Endpoint & Duplicate Detection  
  🏖️ Weekend · Prereq: EIP-602, 603, 601, 402  
  **Goal:** `POST /api/v1/imports/csv` + job status APIs  
  **Key deliverable:** Dedup via `(userId, externalId)`

- [ ] **EIP-605** · Merchant Auto-Extraction on Import  
  🌙 Evening · Prereq: EIP-604, 402  
  **Goal:** Auto-link merchants during import  
  **Key deliverable:** `merchantsCreated` count in job summary

---

### Milestone 5 — Analytics Engine

- [ ] **EIP-701** · Spending Summary by Period  
  🌙 Evening · Prereq: EIP-302  
  **Goal:** `GET /api/v1/analytics/summary`  
  **Key deliverable:** Income, expenses, net for date range

- [ ] **EIP-702** · Category Breakdown  
  🌙 Evening · Prereq: EIP-701  
  **Goal:** `GET /api/v1/analytics/by-category`  
  **Key deliverable:** Percentages, uncategorized bucket

- [ ] **EIP-703** · Monthly Spending Trends  
  🌙 Evening · Prereq: EIP-701  
  **Goal:** `GET /api/v1/analytics/trends?months=6`  
  **Key deliverable:** Zero-filled month buckets

- [ ] **EIP-704** · Top Merchants Ranking  
  🌙 Evening · Prereq: EIP-402, 302  
  **Goal:** `GET /api/v1/analytics/top-merchants`  
  **Key deliverable:** Ranked by DEBIT total

- [ ] **EIP-705** · Cash Flow Time Series  
  🌙 Evening · Prereq: EIP-703  
  **Goal:** `GET /api/v1/analytics/cash-flow`  
  **Key deliverable:** DAILY/WEEKLY granularity

---

### Milestone 6 — AI & Intelligence

- [ ] **EIP-801** · Insight Entity & Storage  
  🌙 Evening · Prereq: EIP-701  
  **Goal:** Flyway V3, `GET /api/v1/insights`  
  **Key deliverable:** Mark-as-read endpoint

- [ ] **EIP-802** · Rule-Based Insight Generation  
  🏖️ Weekend · Prereq: EIP-801, 502, 703  
  **Goal:** `POST /api/v1/insights/generate`  
  **Key deliverable:** Budget overspend, trend spike, anomaly rules

- [ ] **EIP-803** · Auto-Categorization Service  
  🏖️ Weekend · Prereq: EIP-202, 402, 302  
  **Goal:** `POST /api/v1/transactions/auto-categorize`  
  **Key deliverable:** Merchant/keyword rule engine

- [ ] **EIP-804** · Natural Language Query Endpoint  
  🏖️ Weekend · Prereq: EIP-701, 702, 801  
  **Goal:** `POST /api/v1/insights/ask`  
  **Key deliverable:** Intent classifier → predefined query templates

---

### Milestone 7 — Production Readiness

- [ ] **EIP-901** · JWT Refresh Token Flow  
  🏖️ Weekend · Prereq: Phase 1 auth  
  **Goal:** `POST /auth/refresh`, `POST /auth/logout`  
  **Key deliverable:** Token rotation, hashed storage

- [ ] **EIP-902** · Rate Limiting & Abuse Protection  
  🌙 Evening · Prereq: Phase 1 auth  
  **Goal:** 429 on auth/import abuse  
  **Key deliverable:** Configurable limits in `application.yml`

- [ ] **EIP-903** · Structured Logging & Request Correlation  
  🌙 Evening · Prereq: EIP-101  
  **Goal:** `X-Request-Id` + JSON logs in docker profile  
  **Key deliverable:** MDC correlation ID

- [ ] **EIP-904** · CI Pipeline (Build, Test, Lint)  
  🌙 Evening · Prereq: EIP-103  
  **Goal:** GitHub Actions `mvn verify` with Testcontainers  
  **Key deliverable:** `.github/workflows/backend-ci.yml`

---

## Daily Standup Template (copy/paste)

```
Yesterday:  [ticket ID] — [what you completed]
Today:      [ticket ID] — [what you'll implement]
Blockers:   [none / describe]
```

## Review Request Template

When you finish a ticket, paste this in chat:

```
Review my implementation for EIP-XXX.
Branch: [branch name]
Files changed: [list key files]
Anything uncertain: [optional]
```

---

## Completed Tickets Log

| Ticket | Completed | Notes |
|--------|-----------|-------|
| — | — | _add rows as you finish tickets_ |

---

*Last updated: 2026-08-06 · Phase 1 scaffold complete, backend implementation not started*
