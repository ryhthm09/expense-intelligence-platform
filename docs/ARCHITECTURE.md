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

## Backend Package Structure

```
com.expenseintelligence/
├── config/          Security, JWT properties, OpenAPI, JPA auditing
├── controller/      REST endpoints (thin — delegate to services)
├── dto/
│   ├── request/     Validated input DTOs
│   └── response/    Output DTOs + error responses
├── domain/
│   ├── entity/      JPA entities (User, Transaction, Category, Merchant, Budget)
│   └── enums/       Domain enumerations
├── exception/       Global exception handler + custom exceptions
├── mapper/          Entity ↔ DTO conversion
├── repository/      Spring Data JPA repositories
├── security/        JWT filter, token provider, UserPrincipal
└── service/         Business logic
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
  └──< Budget (N) ──> Category (N:1, optional)
```

### Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Primary keys | UUID | Safe for distributed imports, no enumeration |
| Schema management | Flyway | Version-controlled, production-safe migrations |
| Auth | Stateless JWT | Horizontally scalable, SPA-friendly |
| Password hashing | BCrypt | Industry standard, built into Spring Security |
| Amount storage | NUMERIC(19,4) | Precise decimal arithmetic for financial data |
| System categories | `is_system=true, user_id=NULL` | Shared defaults, user can add custom ones |
| Import dedup | `(user_id, external_id)` unique | Prevents duplicate CSV imports |

## Security Model

- All endpoints except `/api/v1/auth/**`, `/api/v1/health`, and Swagger are protected
- JWT contains `userId` as subject, validated on every request
- `UserPrincipal` loaded from DB on each authenticated request
- CORS restricted to `http://localhost:3000` (configurable per environment)
- Passwords never returned in API responses

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
