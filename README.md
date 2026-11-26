# UniMart — Campus Barter Marketplace

**English** · [Português (Brasil)](README.pt-BR.md)

UniMart is a desktop application for students to exchange or donate used books,
school supplies, and equipment within the Senac community.

## Team

- **Class:** TDS — mixed classes 6570, 6756, and 6863
- **Tech lead:** Rodrigo Genevro
- **Presentation lead:** Warley Dos Reis Neto
- **Members:** Rodrigo Genevro, Vinicius Brilhante, Warley Dos Reis Neto, and
  Gabryel Pedroso de Menezes Santos

## Problem and solution

Students often need materials that other students no longer use, but offers are
scattered across informal channels. UniMart provides one place to publish an
item, search listings, and send a message of interest.

## Technology

- Java 21
- Java Swing desktop interface
- SQLite local database
- Maven multi-module build (`backend/` and `frontend/`)

## Current features

- Account registration, login, and profile editing
- Listing creation with title, description, category, exchange/donation type,
  and an optional public HTTP(S) photo link
- Search and filtering of available listings
- Interest messages for available listings
- Owner controls to review interests, accept one to reserve the listing, reopen
  a reservation, or complete the listing

The database is local to the machine. A shared hosted database is not configured.
AI-powered description generation is deferred; the application does not send
listing data to an external AI provider.

## Repository layout

```text
/
├── README.md
├── README.pt-BR.md
├── DECISIONS.md
├── DECISIONS.pt-BR.md
├── AI_LOG.md
├── AI_LOG.pt-BR.md
├── backend/              # Models, controller, services, repositories
├── frontend/             # Swing screens, components, and application entry point
├── config/               # Checkstyle rules
├── database/             # SQLite database created at runtime
├── .github/workflows/    # Build, test, and lint workflow
└── docs/                 # Briefing, rules, and architecture notes
```

## Requirements

- JDK 21
- Maven 3.9 or newer

## Run locally

From the repository root, build and install the modules, then start the Swing UI:

```bash
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

The SQLite database is created automatically at `database/feira_trocas.db`.

## Build, tests, and lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

The command runs backend and frontend tests and Checkstyle. GitHub Actions runs it
for every pull request and every push to `main`.

## Deployment and project links

- **Repository:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **Deployment:** Not configured; the application is a desktop app.
- **GitHub Project board:** Not configured.
