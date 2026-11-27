# AI usage log

**English** · [Português (Brasil)](AI_LOG.pt-BR.md)

This log records AI tools used during the project, representative prompts,
generated work, and decisions made after reviewing the output.

## Tools

- ChatGPT
- Gemini
- GitHub Copilot

## Entries

### November 27, 2026 — Preventing reports on one's own listings

- **Main prompt:** Keep the report option for other users' listings but hide it
  from the owner's own listings.
- **What the AI generated:** The frontend omits the report action for owned
  listings, and the backend rejects self-reports, including legacy listings
  associated by owner name.
- **What the team kept:** Both interface and repository-level protections, with
  regression tests for own and other users' listings.
- **What the team corrected or rejected, and why:** No moderation workflow or
  automatic action was added; reports remain records for later review.

### November 27, 2026 — Integrity, cancellation and local assistance

- **Main prompt:** Implement the relevant backend and frontend suggestions
  without expanding scope beyond the demonstration period.
- **What the AI generated:** Duplicate-interest prevention, user cancellation,
  announcement reports, simple natural-language search and local description
  assistance.
- **What the team kept:** Generated descriptions remain local and require human
  review; no text is published automatically.
- **What the team corrected or rejected, and why:** No external provider is used
  and no automatic decision or classification is applied.

### 24/11/2026 — Initial screen design

- **Main prompt:** Use the existing UC4 project as the basis for the new home
  screen.
- **What the AI generated:** An initial screen implementation using
  object-oriented patterns.
- **What the team kept:** The proposed implementation was not used.
- **What the team corrected or rejected, and why:** The design did not meet the
  presentation goals, and GitHub issues prevented adopting that version.

### 25/11/2026 — Improving screen design

- **Main prompt:** Improve the application's screens.
- **What the AI generated:** A visual redesign with colors that reduced button
  visibility.
- **What the team kept:** The layout direction.
- **What the team corrected or rejected, and why:** The colors were changed so
  that buttons remained visible.

### 26/11/2026 — MVP compliance and owner workflows

- **Main prompt:** Compare the application with the project rules and briefing,
  then implement the identified gaps without changing the existing appearance
  unnecessarily.
- **What the AI generated:** Code and documentation updates that initially
  required photo links, added owner-managed interest and listing statuses, and
  added tests, lint, and CI.
- **What the team kept:** The owner can review incoming interests, accept one to
  reserve a listing, reopen a reservation, or complete a listing.
- **What the team corrected or rejected, and why:** The photo requirement was
  reverted because photos should be optional. AI-powered description generation
  was also deferred; no listing content is sent to an external AI provider.

### 26/11/2026 — Java LTS upgrade and listing workflow refinements

- **Main prompt:** Upgrade the Java runtime to the latest LTS and refine listing
  ownership, logout, and status-update behavior.
- **What the AI generated:** Updated the Maven compiler target, CI Java version,
  and English and Brazilian Portuguese requirements to Java 25. Changed the
  history button label to “Meus anúncios,” blocked interest in a user's own
  listings in the UI and database, hid the main window while showing the logout
  login dialog, and refreshed available listings after reservation, completion,
  or reopening. Added focused tests for the owner-interest restriction.
- **What the team kept:** Java 25 as the project target and the requested
  listing and logout behavior.
- **What the team corrected or rejected, and why:** The `permitirInteresse`
  value was made a card field after a compile error showed it was out of scope
  in the button-building method. The added tests were not run because local
  command execution was not approved.

### 26/11/2026 — Trocaê pre-login screen

- **Main prompt:** Create a pre-login screen inspired by the supplied reference,
  use the application's existing colors, and rename the visible application to
  “Trocaê.”
- **What the AI generated:** Reworked the login and registration dialog into a
  two-panel access screen with a welcome panel, green gradient, and the existing
  email/password login, quick account selection, and account-creation flows.
  Updated visible application branding in the main window and related dialogs.
- **What the team kept:** The reference's split welcome-and-access composition,
  adapted to the application's established green palette and Swing UI.
- **What the team corrected or rejected, and why:** The supplied image was used
  as visual direction only; the screen was implemented with native Swing
  components and Java2D shapes rather than embedding the image. Tests were not
  run for this change.

### 26/11/2026 — Listing editing

- **Main prompt:** Add an option for users to edit their own listings.
- **What the AI generated:** Added an edit action to **Meus anúncios**, reused
  the listing form to save changes, and added owner and availability checks to
  the database update.
- **What the team kept:** Owners can edit available listings; reserved and
  completed listings cannot be edited.
- **What the team corrected or rejected, and why:** Editing was restricted to
  available listings so active reservations and completed listing history are
  not changed. Focused repository tests were added.

### 26/11/2026 — Reliability and backup improvements

- **Main prompt:** Implement the suggested application improvements: verify
  flows, standardize the Trocaê brand, improve operation feedback, provide a
  database backup, and add flow tests.
- **What the AI generated:** Updated the bilingual README, added a SQLite
  snapshot backup action, displayed loading and error states, prevented
  repeated submissions during asynchronous operations, and added temporary
  database tests for backup integrity and registration-to-listing completion.
- **What the team kept:** A user-selected backup destination and tests isolated
  from the real application database.
- **What the team corrected or rejected, and why:** All 12 tests discovered by
  VS Code passed. The Maven `verify`/Checkstyle task was not run because the
  Java build tool could not start with the configured model.

### 26/11/2026 — Listing discovery and database integrity

- **Main prompt:** Implement the suggested frontend and backend improvements,
  including photo preview, form validation, sorting, accessibility,
  versioned SQLite migrations, pagination, integrity checks, and focused tests.
- **What the AI generated:** Added bounded asynchronous photo previews, field
  validation and character counts, sortable paginated results, stronger
  keyboard and text contrast, versioned schema migrations, foreign-key
  connection settings, integrity triggers, indexes, and a two-row header layout
  that keeps the publish action visible.
- **What the team kept:** Existing desktop and local SQLite architecture,
  backward-compatible migration of legacy databases, and test fixtures using
  temporary databases.
- **What the team corrected or rejected, and why:** Preview downloads are
  bounded by time, byte size, and image dimensions. Maven was unavailable on
  PATH; all 23 discovered VS Code tests passed, while the Maven build could not
  be run.
