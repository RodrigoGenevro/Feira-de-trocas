# Project decisions

**English** · [Português (Brasil)](DECISIONS.pt-BR.md)

This file records the decisions reflected in the current implementation.

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

## 25/11/2026 — Current database implementation

- **Decision reflected in code:** Use SQLite, with a local database file created
  at runtime.
- **Alternative considered:** A shared Supabase database.
- **Rationale and status:** Supabase was recorded as a proposal in an earlier
  decision log, but no Supabase integration or credentials are present in this
  implementation. A hosted shared database remains out of scope.

## 26/11/2026 — Photo links

- **Decision:** A photo is optional. When provided, its link must be a public
  HTTP(S) URL; image files are not uploaded.
- **Alternative considered:** Storing uploaded image files with the listing.
- **Rationale:** The existing application displays images from URLs and has no
  image-storage service. Keeping the photo optional preserves the listing flow
  when a publisher does not have an image link.

## 26/11/2026 — AI-powered listing descriptions

- **Decision:** Defer an AI provider integration.
- **Alternative considered:** Sending listing text to a hosted AI API.
- **Rationale:** No provider or API key was selected. Listing data should not be
  sent to an external provider without an explicit decision.
