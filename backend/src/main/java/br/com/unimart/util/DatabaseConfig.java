package br.com.unimart.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConfig {

    private static final String DATABASE_URL_PREFIX = "jdbc:sqlite:";
    private static final int LATEST_SCHEMA_VERSION = 3;
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static String activeDbUrl;

    private DatabaseConfig() {
    }

    public static synchronized String getActiveDbUrl() {
        if (activeDbUrl == null) {
            Path workingDirectory = Path.of("").toAbsolutePath();
            Path databaseDirectory = workingDirectory.resolve("database");
            try {
                Files.createDirectories(databaseDirectory);
                activeDbUrl = DATABASE_URL_PREFIX
                        + databaseDirectory.resolve("feira_trocas.db").toString();
            } catch (IOException | SecurityException error) {
                LOGGER.log(Level.SEVERE, "Não foi possível preparar a pasta do banco SQLite.", error);
                throw new IllegalStateException("Não foi possível preparar o banco de dados local.", error);
            }
        }
        return activeDbUrl;
    }

    public static Connection getConnection() throws SQLException {
        return getConnection(getActiveDbUrl());
    }

    public static Connection getConnection(String jdbcUrl) throws SQLException {
        if (jdbcUrl == null || !jdbcUrl.startsWith(DATABASE_URL_PREFIX)) {
            throw new IllegalArgumentException("Somente conexões SQLite são suportadas.");
        }

        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (SQLException error) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    public static void initDatabase() throws SQLException {
        initializeDatabase(getActiveDbUrl());
    }

    static void initializeDatabase(String jdbcUrl) throws SQLException {
        if (jdbcUrl == null || !jdbcUrl.startsWith(DATABASE_URL_PREFIX)) {
            throw new IllegalArgumentException("Somente conexões SQLite são suportadas.");
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
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }

    private static void criarTabelaMigracoes(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version INTEGER PRIMARY KEY,
                        applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
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
            case 3 -> migrarInteressesEDenuncias(connection);
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
            garantirColunaUsuarioId(connection, "anuncios");
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
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_categoria "
                            + "ON anuncios(status, categoria, modalidade, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_status_titulo "
                            + "ON anuncios(status, titulo COLLATE NOCASE, id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_modalidade "
                            + "ON anuncios(status, modalidade, id DESC)"
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

    private static void migrarInteressesEDenuncias(Connection connection) throws SQLException {
        garantirColunaUsuarioId(connection, "interesses");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_interesses_usuario_anuncio "
                            + "ON interesses(usuario_id, anuncio_id) WHERE usuario_id IS NOT NULL"
            );
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS denuncias (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        anuncio_id INTEGER NOT NULL,
                        usuario_id INTEGER NOT NULL,
                        motivo TEXT NOT NULL,
                        detalhes TEXT,
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(anuncio_id) REFERENCES anuncios(id),
                        FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
                    )
                    """);
            statement.executeUpdate(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_denuncias_usuario_anuncio "
                            + "ON denuncias(usuario_id, anuncio_id)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_denuncias_anuncio ON denuncias(anuncio_id)"
            );
        }
    }

    private static void garantirColunaUsuarioId(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if ("usuario_id".equals(columns.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN usuario_id INTEGER");
        }
    }

    private static void registrarMigracao(Connection connection, int version) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO schema_migrations(version) VALUES (?)")) {
            statement.setInt(1, version);
            statement.executeUpdate();
        }
    }
}
