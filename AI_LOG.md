# AI usage log

**English** · [Português (Brasil)](AI_LOG.pt-BR.md)

This log records AI tools used during the project, representative prompts,
generated work, and decisions made after reviewing the output.

## Tools

- ChatGPT
- Gemini
- GitHub Copilot

## Entries

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
