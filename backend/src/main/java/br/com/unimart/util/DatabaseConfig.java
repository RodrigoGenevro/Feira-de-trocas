package br.com.unimart.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:database/feira_trocas.db";
    private static final String DB_FALLBACK_URL = "jdbc:sqlite:../database/feira_trocas.db";
    private static final int LATEST_SCHEMA_VERSION = 2;
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static String activeDbUrl;

    private DatabaseConfig() {
    }

    public static synchronized String getActiveDbUrl() {
        if (activeDbUrl == null) {
            resolveDbPath();
        }
        return activeDbUrl;
    }

    private static void resolveDbPath() {
        String configuredUrl = System.getenv("TROCAE_DATABASE_URL");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            activeDbUrl = configuredUrl.trim();
            return;
        }

        try {
            Path cwd = Paths.get("").toAbsolutePath();
            boolean runningFromBackend = cwd.getFileName() != null
                    && cwd.getFileName().toString().equals("backend");
            Path databaseDirectory = runningFromBackend
                    ? cwd.resolve("..").resolve("database").normalize()
                    : cwd.resolve("database");

            Files.createDirectories(databaseDirectory);
            activeDbUrl = runningFromBackend ? DB_FALLBACK_URL : DB_URL;
        } catch (IOException | SecurityException e) {
            LOGGER.log(Level.WARNING, "Não foi possível preparar a pasta do banco de dados.", e);
            activeDbUrl = DB_URL;
        }
    }

    public static Connection getConnection() throws SQLException {
        return getConnection(getActiveDbUrl());
    }

    public static Connection getConnection(String jdbcUrl) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            if (jdbcUrl.startsWith("jdbc:sqlite:")) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    public static void initDatabase() throws SQLException {
        initializeDatabase(getActiveDbUrl());
    }

    static void initializeDatabase(String jdbcUrl) throws SQLException {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            initializePostgresDatabase(jdbcUrl);
            return;
        }

        try (Connection connection = getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            try {
                criarTabelaMigracoes(connection);
                int currentVersion = buscarVersaoAtual(connection);
                if (currentVersion > LATEST_SCHEMA_VERSION) {
                    throw new SQLException("O banco foi criado por uma versão mais recente do Trocaê.");
                }

                for (int version = currentVersion + 1; version <= LATEST_SCHEMA_VERSION; version++) {
                    aplicarMigracao(connection, version);
                    registrarMigracao(connection, version);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private static void initializePostgresDatabase(String jdbcUrl) throws SQLException {
        try (Connection connection = getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            try {
                criarTabelaMigracoes(connection);
                int currentVersion = buscarVersaoAtual(connection);
                if (currentVersion > LATEST_SCHEMA_VERSION) {
                    throw new SQLException("O banco foi criado por uma versão mais recente do Trocaê.");
                }

                for (int version = currentVersion + 1; version <= LATEST_SCHEMA_VERSION; version++) {
                    if (version == 1) {
                        migrarEstruturaPostgres(connection);
                    } else if (version == 2) {
                        migrarIndicesPostgres(connection);
                    }
                    registrarMigracao(connection, version);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private static void migrarEstruturaPostgres(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ATIVO',
                        criado_em TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP::text)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS anuncios (
                        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL CHECK (modalidade IN ('TROCA', 'DOACAO')),
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER REFERENCES usuarios(id),
                        status TEXT NOT NULL DEFAULT 'DISPONIVEL'
                            CHECK (status IN ('DISPONIVEL', 'RESERVADO', 'CONCLUIDO')),
                        criado_em TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP::text)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS interesses (
                        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL REFERENCES anuncios(id),
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDENTE'
                            CHECK (status IN ('PENDENTE', 'ACEITO', 'ENCERRADO', 'CANCELADO')),
                        criado_em TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP::text)
                    )
                    """);
        }
    }

    private static void migrarIndicesPostgres(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email_lower ON usuarios (lower(email))"
            );
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usuarios_status ON usuarios(status)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_usuario_id ON anuncios(usuario_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_interesses_anuncio_id ON interesses(anuncio_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_status_id ON anuncios(status, id DESC)");
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_categoria " +
                            "ON anuncios(status, categoria, modalidade, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_status_titulo ON anuncios(status, lower(titulo), id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_modalidade ON anuncios(status, modalidade, id DESC)"
            );
        }
    }

    private static void criarTabelaMigracoes(Connection connection) throws SQLException {
        String timestampDefault = connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL")
                ? "CURRENT_TIMESTAMP::text" : "CURRENT_TIMESTAMP";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version INTEGER PRIMARY KEY,
                        applied_at TEXT NOT NULL DEFAULT %s
                    )
                    """.formatted(timestampDefault));
        }
    }

    private static int buscarVersaoAtual(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_migrations")) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static void aplicarMigracao(Connection connection, int version) throws SQLException {
        switch (version) {
            case 1 -> migrarEstruturaBase(connection);
            case 2 -> migrarIntegridadeEPaginacao(connection);
            default -> throw new SQLException("Migração de banco de dados desconhecida: " + version);
        }
    }

    private static void migrarEstruturaBase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ATIVO',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS anuncios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER,
                        status TEXT NOT NULL DEFAULT 'DISPONIVEL',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
                    )
                    """);
            garantirColunaUsuarioId(connection);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS interesses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDENTE',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(anuncio_id) REFERENCES anuncios(id)
                    )
                    """);
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usuarios_status ON usuarios(status)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_usuario_id ON anuncios(usuario_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_interesses_anuncio_id ON interesses(anuncio_id)");
        }
    }

    private static void migrarIntegridadeEPaginacao(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_status_id ON anuncios(status, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_categoria " +
                            "ON anuncios(status, categoria, modalidade, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_status_titulo " +
                            "ON anuncios(status, titulo COLLATE NOCASE, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_modalidade " +
                            "ON anuncios(status, modalidade, id DESC)"
            );
            statement.executeUpdate("""
                    CREATE TRIGGER IF NOT EXISTS trg_anuncios_validate_insert
                    BEFORE INSERT ON anuncios
                    WHEN NEW.modalidade IS NULL
                      OR NEW.modalidade NOT IN ('TROCA', 'DOACAO')
                      OR NEW.status IS NULL
                      OR NEW.status NOT IN ('DISPONIVEL', 'RESERVADO', 'CONCLUIDO')
                      OR (NEW.usuario_id IS NOT NULL AND
                          NOT EXISTS (SELECT 1 FROM usuarios WHERE id = NEW.usuario_id))
                    BEGIN
                        SELECT RAISE(ABORT, 'Dados de anúncio inválidos.');
                    END
                    """);
            statement.executeUpdate("""
                    CREATE TRIGGER IF NOT EXISTS trg_anuncios_validate_update
                    BEFORE UPDATE OF modalidade, status, usuario_id ON anuncios
                    WHEN NEW.modalidade IS NULL
                      OR NEW.modalidade NOT IN ('TROCA', 'DOACAO')
                      OR NEW.status IS NULL
                      OR NEW.status NOT IN ('DISPONIVEL', 'RESERVADO', 'CONCLUIDO')
                      OR (NEW.usuario_id IS NOT NULL AND
                          NOT EXISTS (SELECT 1 FROM usuarios WHERE id = NEW.usuario_id))
                    BEGIN
                        SELECT RAISE(ABORT, 'Dados de anúncio inválidos.');
                    END
                    """);
            statement.executeUpdate("""
                    CREATE TRIGGER IF NOT EXISTS trg_interesses_validate_insert
                    BEFORE INSERT ON interesses
                    WHEN NEW.status IS NULL
                      OR NEW.status NOT IN ('PENDENTE', 'ACEITO', 'ENCERRADO', 'CANCELADO')
                      OR NOT EXISTS (
                          SELECT 1 FROM anuncios
                          WHERE id = NEW.anuncio_id AND status = 'DISPONIVEL'
                      )
                    BEGIN
                        SELECT RAISE(ABORT, 'Dados de interesse inválidos.');
                    END
                    """);
            statement.executeUpdate("""
                    CREATE TRIGGER IF NOT EXISTS trg_interesses_validate_update
                    BEFORE UPDATE OF status, anuncio_id ON interesses
                    WHEN NEW.status IS NULL
                      OR NEW.status NOT IN ('PENDENTE', 'ACEITO', 'ENCERRADO', 'CANCELADO')
                      OR NOT EXISTS (SELECT 1 FROM anuncios WHERE id = NEW.anuncio_id)
                    BEGIN
                        SELECT RAISE(ABORT, 'Dados de interesse inválidos.');
                    END
                    """);
        }
    }

    private static void garantirColunaUsuarioId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(anuncios)")) {
            while (columns.next()) {
                if ("usuario_id".equals(columns.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE anuncios ADD COLUMN usuario_id INTEGER");
        }
    }

    private static void registrarMigracao(Connection connection, int version) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO schema_migrations(version) VALUES (?)")) {
            statement.setInt(1, version);
            statement.executeUpdate();
        }
    }
}
