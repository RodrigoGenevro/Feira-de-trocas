package br.com.unimart.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static String activeDbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {


        // Carrega as variáveis de ambiente (arquivo .env)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
// Vamos testar o que está a ser lido:
        String urlLida = dotenv.get("DB_URL");
        String userLido = dotenv.get("DB_USER");
        String passLida = dotenv.get("DB_PASSWORD");

        System.out.println("=== DEBUG DOTENV ===");
        System.out.println("DB_URL lida: " + urlLida);
        System.out.println("DB_USER lido: " + userLido);
        System.out.println("DB_PASSWORD lido: " + (passLida != null ? "PREENCHIDO (tamanho: " + passLida.length() + ")" : "VAZIO/NULO"));
        System.out.println("====================");
        
        // Pega do .env ou usa valores padrão/vazios
        dbUser = dotenv.get("DB_USER", "postgres");
        dbPassword = dotenv.get("DB_PASSWORD", "");
        
        // Monta a URL do PostgreSQL/Supabase
        // Exemplo no .env: DB_URL=jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
        activeDbUrl = dotenv.get("DB_URL");
        if (activeDbUrl == null || activeDbUrl.isEmpty()) {
            LOGGER.log(Level.WARNING, "DB_URL não encontrada nas variáveis de ambiente!");
        }
    }

    private DatabaseConfig() {
    }

    public static synchronized String getActiveDbUrl() {
        return activeDbUrl;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(activeDbUrl, dbUser, dbPassword);
    }

    public static Connection getConnection(String jdbcUrl) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
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
                int latestSchemaVersion = 2;

                if (currentVersion > latestSchemaVersion) {
                    throw new SQLException("O banco foi criado por uma versão mais recente do Trocaê.");
                }

                for (int version = currentVersion + 1; version <= latestSchemaVersion; version++) {
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
                if (currentVersion > LATEST_POSTGRES_SCHEMA_VERSION) {
                    throw new SQLException("O banco foi criado por uma versão mais recente do Trocaê.");
                }

                for (int version = currentVersion + 1; version <= LATEST_POSTGRES_SCHEMA_VERSION; version++) {
                    if (version == 1) {
                        migrarEstruturaPostgres(connection);
                    } else if (version == 2) {
                        migrarIndicesPostgres(connection);
                    } else if (version == 4) {
                        migrarDenunciasEInteressesPostgres(connection);
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

    private static void migrarDenunciasEInteressesPostgres(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "ALTER TABLE interesses ADD COLUMN IF NOT EXISTS usuario_id INTEGER REFERENCES usuarios(id)"
            );
            statement.executeUpdate(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_interesses_usuario_anuncio "
                            + "ON interesses(usuario_id, anuncio_id) WHERE usuario_id IS NOT NULL"
            );
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS denuncias (
                        id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL REFERENCES anuncios(id),
                        usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
                        motivo TEXT NOT NULL,
                        detalhes TEXT,
                        criado_em TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP::text)
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

    private static void criarTabelaMigracoes(Connection connection) throws SQLException {
        String timestampDefault = connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL")
                ? "CURRENT_TIMESTAMP::text" : "CURRENT_TIMESTAMP";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version INTEGER PRIMARY KEY,
                        applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
            case 3 -> migrarInteressesEDenuncias(connection);
            default -> throw new SQLException("Migração de banco de dados desconhecida: " + version);
        }
    }

    private static void migrarEstruturaBase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id SERIAL PRIMARY KEY,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ATIVO',
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS anuncios (
                        id SERIAL PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER,
                        status TEXT NOT NULL DEFAULT 'DISPONIVEL',
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS interesses (
                        id SERIAL PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDENTE',
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
                            "ON anuncios(status, LOWER(titulo), id DESC)"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_anuncios_busca_modalidade " +
                            "ON anuncios(status, modalidade, id DESC)"
            );
            // Nota: No PostgreSQL, validações complexas por 'TRIGGER BEFORE INSERT' continuam funcionando, 
            // mas em aplicações modernas costuma-se usar CHECK constraints ou validação na camada de serviço.
        }
    }

    private static void registrarMigracao(Connection connection, int version) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO schema_migrations(version) VALUES (?) ON CONFLICT (version) DO NOTHING")) {
            statement.setInt(1, version);
            statement.executeUpdate();
        }
    }
}