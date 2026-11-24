# Technical decisions

[Português (Brasil)](DECISIONS.pt-BR.md)

- Vanilla HTML, CSS and JavaScript keep the frontend dependency-free and approachable.
- Java 21 built-in `HttpServer` provides the backend without a web framework.
- SQLite stores data in a local file without requiring a database server.
- Maven manages only SQLite JDBC and Gson dependencies.
- The same server hosts static frontend files and the JSON API for easier local setup.
- Initial scope prioritizes listing creation, browsing, search, filters and interest submissions.
