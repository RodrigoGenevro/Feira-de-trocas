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

- Java 25 (latest LTS)
- Java Swing desktop interface
- Java API and hosted PostgreSQL on Supabase
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
- SQLite database backup and restore, with integrity checks before replacement
  and versioned migrations
- Users can cancel interests they sent
- The backend prevents duplicate interests
- Users can report other users' listings with a reason and details; reporting
  one's own listing is unavailable
- Local description suggestions and natural-language search
- Versioned SQLite migrations, referential integrity checks, and listing-search
  indexes

The database is local to the machine. Use **Banco de dados** in the application
header to create a consistent SQLite snapshot or restore a validated backup.
The current database is preserved if validation or restoration fails. A shared
hosted database is not configured.
- HTTP API for authentication, profiles, listings, and interests
- Versioned PostgreSQL migrations, referential integrity, and search indexes
- Signed expiring sessions; passwords and database credentials remain on the
  server

The app is prepared to share data through Supabase PostgreSQL via a Java API.
To enable sharing, host the API and point every desktop installation to the same
URL using `TROCAE_API_URL`. The desktop client does not connect directly to the
database and contains no Supabase credentials. Manage remote backups in the
Supabase project.
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
├── backend/              # API, models, services, controller, and repositories
├── frontend/             # Swing screens, components, and application entry point
├── config/               # Checkstyle rules
├── database/             # Legacy local SQLite database for development
├── .github/workflows/    # Build, test, and lint workflow
└── docs/                 # Briefing, rules, and architecture notes
```

## Requirements

- JDK 25
- Maven 3.9 or newer

## Run locally

Create a Supabase project and configure the API with its PostgreSQL JDBC URL, a
random session secret (at least 32 bytes), and the port assigned by the hosting
provider. For Render, choose **Connect → Session pooler** in the Supabase
dashboard and copy the host, port, user, and database shown there. The API
applies migrations when it starts:

```powershell
$env:TROCAE_DATABASE_URL = "jdbc:postgresql://POOLER_HOST:5432/postgres?user=POOLER_USER&password=URL_ENCODED_PASSWORD&sslmode=require"
$env:TROCAE_AUTH_SECRET = "replace-with-a-random-secret-at-least-32-bytes-long"
$env:PORT = "8080"
mvn -pl backend exec:java -Dexec.mainClass="br.com.unimart.api.ApiServer"
```

The pooler username may include the project reference. Use the exact values
shown in the dashboard; special characters in the URL password must be
percent-encoded. Session mode supports persistent connections and prepared
statements. The database account must be allowed to create the tables and
indexes used by migrations.

For local development, start the API and then start the desktop app in another
terminal. In production, set `TROCAE_API_URL` in each desktop installation to the
public HTTPS URL of the API (`https://YOUR-SERVER/api`):

```powershell
$env:TROCAE_API_URL = "http://localhost:8080/api"
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

To import data from an existing SQLite database once, first make a copy of the
file and use an empty Supabase database:

```powershell
mvn -pl backend exec:java -Dexec.mainClass="br.com.unimart.util.SqliteToPostgresMigration" -Dexec.args="database/feira_trocas.db"
```

The importer preserves IDs, validates row counts, and aborts if destination
tables already contain records. Do not rerun it after a successful migration.

Never put the database password or `TROCAE_AUTH_SECRET` in source code, the
desktop client, or versioned files. Use the hosting provider's secret manager
and enable HTTPS before exposing the API publicly.

## Deploy the API to Render

1. Create a Supabase project and open **Connect → Session pooler**. Copy the
   host, port, user, and database shown there. Use a URL such as
   `jdbc:postgresql://POOLER_HOST:5432/postgres?user=POOLER_USER&password=URL_ENCODED_PASSWORD&sslmode=require`,
   using the exact values from the dashboard. Session mode supports IPv4 and
   persistent connections; do not use the Transaction pooler for this API.
2. From the repository root, create a working branch, review the changes, and
   push that branch (use a unique branch name):

   ```powershell
   git switch -c feature/my-change
   git status
   git add Dockerfile .dockerignore render.yaml README.md README.pt-BR.md backend frontend DECISIONS.md DECISIONS.pt-BR.md
   git commit -m "feat: prepare deployment via Render and Supabase"
   git push -u origin feature/my-change
   ```

   Open a pull request targeting `main`; wait for review and a green CI run
   before merging. Do not push directly to `main`. Do not commit `.env` files,
   passwords, or connection URLs containing secrets.
3. In Render, choose **New → Blueprint**, connect the GitHub repository, and
   select the `main` branch. Render reads `render.yaml`, prompts for
   `TROCAE_DATABASE_URL`, and generates `TROCAE_AUTH_SECRET`. Do not set `PORT`;
   Render provides it.
4. Once the deploy is healthy, open `https://<service>.onrender.com/health`;
   the expected response is `{"status":"ok"}`. The API creates its tables at
   startup.
5. On each desktop computer, set
   `TROCAE_API_URL=https://<service>.onrender.com/api` and restart the app. Every
   client must use the same URL.

Render's free plan may suspend an idle API, so the first request after it wakes
up can take longer. The desktop client currently has a 20-second request
timeout, so a cold start may time out; retry after the service has started.
This configuration starts with an empty Supabase database and does not import
local SQLite files.

## Build, tests, and lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

The command runs backend and frontend tests and Checkstyle. SQLite tests continue
to cover the legacy mode; Supabase connectivity requires external configuration
and must be validated in the deployment environment. GitHub Actions runs this
command on Ubuntu with Temurin JDK 25 for every pull request and every push to
`main`. The workflow uses `actions/checkout@v7` and `actions/setup-java@v6`,
with Java compiler problem annotations enabled; see
[the CI workflow](.github/workflows/ci.yml).

## Deployment and project links

- **Repository:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **Deployment:** `render.yaml` is ready; the service is not live until GitHub is
  connected and Supabase is configured in Render.
- **GitHub Project board:** Not configured.
