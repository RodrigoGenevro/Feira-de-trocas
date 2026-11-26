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
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static String activeDbUrl;

    private DatabaseConfig() {
        // Classe utilitária não deve ser instanciada
    }

    public static synchronized String getActiveDbUrl() {
        if (activeDbUrl == null) {
            resolveDbPath();
        }
        return activeDbUrl;
    }

    private static void resolveDbPath() {
        try {
            Path cwd = Paths.get("").toAbsolutePath();
            boolean runningFromBackend = cwd.getFileName() != null
                    && cwd.getFileName().toString().equals("backend");
            Path databaseDirectory = runningFromBackend
                    ? cwd.resolve("..").resolve("database").normalize()
                    : cwd.resolve("database");

            Files.createDirectories(databaseDirectory);
            if (runningFromBackend) {
                activeDbUrl = DB_FALLBACK_URL;
            } else {
                activeDbUrl = DB_URL;
            }
        } catch (IOException | SecurityException e) {
            LOGGER.log(Level.WARNING, "Não foi possível preparar a pasta do banco de dados.", e);
            activeDbUrl = DB_URL;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getActiveDbUrl());
    }

    public static void initDatabase() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            criarTabelaUsuarios(s);
            criarTabelaAnuncios(s);
            garantirColunaUsuarioId(c);
            criarTabelaInteresses(s);
            criarIndices(s);
        }
    }

    private static void criarTabelaUsuarios(Statement statement) throws SQLException {
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
    }

    private static void criarTabelaAnuncios(Statement statement) throws SQLException {
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

    private static void criarTabelaInteresses(Statement statement) throws SQLException {
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
    }

    private static void criarIndices(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_usuarios_status ON usuarios(status)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_status ON anuncios(status)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_usuario_id ON anuncios(usuario_id)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_categoria ON anuncios(categoria)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_anuncios_modalidade ON anuncios(modalidade)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_interesses_anuncio_id ON interesses(anuncio_id)");
    }
}