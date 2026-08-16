# Expense Intelligence Platform

AI-powered financial intelligence platform for importing expenses from multiple sources — including automated Gmail ingestion and CSV import — and generating intelligent spending insights.

> **ChatGPT + Personal Finance + Analytics**

## Architecture

```
┌─────────────┐     REST/JWT      ┌──────────────────────────────────┐
│  Next.js    │ ◄──────────────► │  Spring Boot 3 (Clean Architecture)│
│  Frontend   │                   │  Controllers → Services → Repos    │
└─────────────┘                   └──────────────┬───────────────────┘
                                                 │
                                                 ▼
                                        ┌─────────────────┐
                                        │   PostgreSQL    │
                                        │   (Flyway)      │
                                        └─────────────────┘
```

## Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 21, Spring Boot 3, Spring Security, JPA   |
| Database     | PostgreSQL 16, Flyway                           |
| Frontend     | Next.js, React, TypeScript, TailwindCSS        |
| Auth         | JWT                                             |
| API Docs     | OpenAPI / Swagger                               |
| Infra        | Docker Compose                                  |

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- Docker & Docker Compose

### Run with Docker (recommended)

```bash
# Start PostgreSQL + Backend + Frontend
docker compose up --build
```

| Service    | URL                                      |
|------------|------------------------------------------|
| Frontend   | http://localhost:3000                    |
| Backend    | http://localhost:8080                    |
| Swagger UI | http://localhost:8080/swagger-ui.html    |
| PostgreSQL | localhost:5432                           |

### Run locally (development)

**Database:**

```bash
docker compose up postgres -d
```

**Backend:**

```bash
cd backend
# Requires Maven 3.9+ and Java 21
mvn spring-boot:run
```

**Frontend:**

```bash
cd frontend
npm install
npm run dev
```

## Project Structure

```
├── backend/          # Spring Boot API
├── frontend/         # Next.js web application
├── docs/             # Architecture & roadmap
└── docker-compose.yml
```

## API Endpoints (Phase 1)

| Method | Endpoint              | Description        |
|--------|-----------------------|--------------------|
| POST   | `/api/v1/auth/register` | Register user    |
| POST   | `/api/v1/auth/login`    | Login & get JWT  |
| GET    | `/api/v1/health`        | Health check     |

## Phase 2 Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full implementation plan.

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for system design and data model.

## Daily Planning

See [docs/PLANNING.md](docs/PLANNING.md) for the ordered ticket backlog and weekly schedule.

## License

MIT
