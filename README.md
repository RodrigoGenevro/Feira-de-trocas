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
- Versioned SQLite and PostgreSQL migrations, referential integrity, and
  listing-search indexes
- Users can cancel interests they sent
- The backend prevents duplicate interests
- Users can report other users' listings with a reason and details; reporting
  one's own listing is unavailable
- Local description suggestions and natural-language search

The desktop client uses a Java API; to share data, point every installation to
the same URL with `TROCAE_API_URL`. The API uses Supabase PostgreSQL and keeps
database credentials on the server. The desktop client never connects directly
to Supabase. Without `TROCAE_API_URL`, the client tries the local API at
`http://localhost:8080/api`; that API also requires PostgreSQL configuration.
SQLite remains available for tests and as a legacy data source. Manage remote
database backups in the Supabase project.
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

For local development, configure the API with a development PostgreSQL database,
start it, and then start the desktop app in another terminal. In production, set
`TROCAE_API_URL` in each desktop installation to the public HTTPS URL of the API
(`https://YOUR-SERVER/api`):

```powershell
$env:TROCAE_API_URL = "http://localhost:8080/api"
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

To consolidate SQLite databases from multiple machines, work from copies of the
files and use an empty Supabase database. Start with a dry run:

```powershell
mvn -pl backend exec:java "-Dexec.mainClass=br.com.unimart.util.SqliteToPostgresMigration" "-Dexec.args=--dry-run C:\backups\computer1.db C:\backups\computer2.db"
```

Review the counts and manually resolve accounts that share an email
(case-insensitively) but have different profiles or credentials in the SQLite
copies. The dry run blocks import when it finds conflicts or invalid references.
Then run the same file list with:

```powershell
mvn -pl backend exec:java "-Dexec.mainClass=br.com.unimart.util.SqliteToPostgresMigration" "-Dexec.args=--apply C:\backups\computer1.db C:\backups\computer2.db"
```

The importer keeps every listing, remaps IDs and references for listings,
interests, and reports, and deduplicates equivalent accounts by normalized
email. It validates row counts and writes in one transaction; it aborts if the
destination already contains data. Back up the source files and do not rerun
after a successful import. Photos remain HTTP(S) links; files are not uploaded
to Supabase Storage.

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
