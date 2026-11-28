# Trocaê — Campus Barter Marketplace

**English** · [Português (Brasil)](README.pt-BR.md)

Trocaê is a desktop application for students to exchange or donate used books,
school supplies, and equipment within the Senac community.

## Team

- **Class:** TDS — mixed classes 6570, 6756, and 6863
- **Tech lead:** Rodrigo Genevro
- **Presentation lead:** Warley Dos Reis Neto
- **Members:** Rodrigo Genevro, Vinicius Brilhante, Warley Dos Reis Neto, and
  Gabryel Pedroso de Menezes Santos

## Problem and solution

Students often need materials that other students no longer use, but offers are
scattered across informal channels. Trocaê provides one place to publish an
item, search listings, and send a message of interest.

## Technology

- Java 21
- Java Swing desktop interface
- Local SQLite database file; no external service
- Maven multi-module build (`backend/` and `frontend/`)

## Current features

- Account registration, login, and profile editing
- Listing creation with title, description, category, exchange/donation type,
  and an optional public HTTP(S) photo link
- Search and filtering of available listings
- Sortable, paginated listing results with keyboard-accessible controls
- Listing form validation with an asynchronous image preview and clear failure
  feedback
- Responsive two-row header keeps account and publish actions visible
- Interest messages for available listings
- Owner controls to review interests, accept one to reserve the listing, reopen
  a reservation, complete or reactivate the listing
- Listing owners can edit their available listings from **Meus anúncios**
- Responsive listing grid with accessible active filters and removable filter
  chips
- Versioned SQLite migrations, referential integrity, and
  listing-search indexes
- Users can cancel interests they sent
- The backend prevents duplicate interests
- Users can report other users' listings with a reason and details; reporting
  one's own listing is unavailable
- Local description suggestions and natural-language search

The application connects directly to a local SQLite database at
`database/feira_trocas.db`. It creates and migrates this file on first launch.
Data stays on that computer; there is no API, server, or synchronization between
installations. No `.env` file is needed or used.
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
├── backend/              # models, services, controller, repositories, and SQLite
├── frontend/             # Swing screens, components, and application entry point
├── config/               # Checkstyle rules
├── database/             # Local SQLite database file (ignored by Git)
├── .github/workflows/    # Build, test, and lint workflow
└── docs/                 # Briefing, rules, and architecture notes
```

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer (or use the included Maven Wrapper)

## Run

From the repository root, build and launch the desktop application:

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd -pl frontend exec:java
```

The application creates `database/feira_trocas.db` and applies schema
migrations on first launch. Use **Banco de dados → Fazer backup** to save a
backup file. To recover data, choose **Banco de dados → Restaurar backup** and
select a valid backup; restoration replaces the current data. Database files
are excluded from Git.

## Build, tests, and lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

The command runs backend and frontend tests and Checkstyle. Tests use temporary
SQLite databases and do not access personal data. GitHub Actions runs this
command on Ubuntu with Temurin JDK 25 for every pull request and every push to
`main`. The workflow uses `actions/checkout@v7` and `actions/setup-java@v6`,
with Java compiler problem annotations enabled; see
[the CI workflow](.github/workflows/ci.yml).

## Project links

- **Repository:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **GitHub Project board:** Not configured.
