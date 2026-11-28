# Project decisions

**English** · [Português (Brasil)](DECISIONS.pt-BR.md)

This file records the decisions reflected in the current implementation.

## 27/11/2026 — Local SQLite as the only database

- **Decision:** Use one SQLite database file on each computer and connect to it
  directly from the desktop application. Do not require a Java API, Supabase,
  or a hosted deployment.
- **Alternative considered:** A shared remote database accessed through an API.
- **Rationale:** Keep the application self-contained and avoid external service
  setup. Data is local to each computer and is not synchronized.

## 23/11/2026 — Java and desktop stack

- **Decision:** Use Java 21, Swing, and Maven.
- **Alternatives considered:** A web application using JavaScript or another
  backend language.
- **Rationale:** The team already works primarily with Java, and a desktop
  application fits the one-week MVP timeframe.

## 26/11/2026 — Upgrade to the latest Java LTS

- **Decision:** Upgrade the project runtime and build target to Java 25 while
  retaining Swing and Maven.
- **Rationale:** Java 25 is the latest LTS release; the Maven compiler target,
  CI runtime, and documented requirements now use this version.

## 24/11/2026 — Separate frontend and backend modules

- **Decision:** Keep the Swing UI in `frontend/` and domain, service, controller,
  and persistence code in `backend/`.
- **Alternatives considered:** Keeping all application code in one module.
- **Rationale:** Maven keeps both modules integrated while making their
  responsibilities easier to locate.

## 25/11/2026 — Initial database implementation

- **Decision reflected in code:** Use SQLite, with a local database file created
  at runtime.
- **Alternative considered:** A shared Supabase database.
- **Rationale and status:** This was the initial implementation; it was
  followed by a shared-database decision on 27/11/2026, which was later
  superseded by the current local SQLite decision above.

## 27/11/2026 — Shared Supabase database

- **Decision:** Keep the Swing desktop client, add a hosted Java API, and use
  Supabase PostgreSQL as the shared datastore.
- **Alternative considered:** Connect each desktop installation directly to
  the remote database.
- **Rationale:** A centralized API keeps database credentials out of the
  distributed application and applies authentication and authorization on the
  server.
- **Operation:** Configure the JDBC URL, session secret, and public API URL using
  environment variables; public deployment requires HTTPS.
- **Status:** Superseded by the local SQLite decision at the top of this file.

## 26/11/2026 — Photo links and briefing requirement

- **Current implementation:** A photo link is optional. When provided, it must
  be an HTTP(S) URL; image files are not uploaded.
- **Briefing requirement:** The MVP listing flow requires a photo.
- **Compliance status:** The current optional-photo behavior does not satisfy
  that requirement. The implementation and this decision must be revised
  before claiming full briefing compliance.
- **Alternative considered:** Storing uploaded image files with the listing.
- **Rationale for the current behavior:** The application displays images from
  URLs and has no image-storage service. This documents the existing behavior;
  it does not waive the briefing requirement.

## 26/11/2026 — AI-powered listing descriptions

- **Decision:** Defer an AI provider integration.
- **Alternative considered:** Sending listing text to a hosted AI API.
- **Rationale:** No provider or API key was selected. Listing data should not be
  sent to an external provider without an explicit decision.

## November 27, 2026 — Local search and description assistance

- **Decision:** Implement a local description suggestion and basic
  natural-language term recognition without sending data to external providers.
- **Rationale:** This keeps assisted behavior transparent and safe within the
  immersion deadline while allowing a demonstration without API credentials.

## November 27, 2026 — Interest integrity and reports

- **Decision:** Prevent duplicate interests, allow the interested user to cancel
  a pending interest, and allow one report per user and announcement only when
  the announcement belongs to someone else.
- **Rationale:** This closes important use cases, reduces noise in the listing and
  creates clear responsibilities for expressing interest or reporting content.
  The interface hides the report action for the owner's listings, and the
  backend also rejects self-reports.
