package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseBackupTest {

    @TempDir
    Path tempDirectory;

    @Test
    void criaSnapshotQuePodeSerAbertoComoOutroBancoSQLite() throws Exception {
        Path database = tempDirectory.resolve("source.db");
        Path backup = tempDirectory.resolve("owner's-backup.db");
        String jdbcUrl = "jdbc:sqlite:" + database;

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE sample (value TEXT NOT NULL)");
            statement.executeUpdate("INSERT INTO sample(value) VALUES ('preserved')");
        }
        Files.writeString(backup, "previous backup");

        DatabaseBackup.criarBackup(jdbcUrl, backup);

        assertTrue(Files.size(backup) > 0);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + backup);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT value FROM sample")) {
            assertTrue(result.next());
            assertEquals("preserved", result.getString("value"));
        }
    }

    @Test
    void naoPermiteSobrescreverOArquivoDoBancoDeOrigem() throws SQLException {
        Path database = tempDirectory.resolve("source.db");
        String jdbcUrl = "jdbc:sqlite:" + database;

        assertThrows(
                IllegalArgumentException.class,
                () -> DatabaseBackup.criarBackup(jdbcUrl, database)
        );
    }

    @Test
    void restauraBackupValidoPreservandoEstruturaERegistros() throws Exception {
        Path currentDatabase = tempDirectory.resolve("current.db");
        Path backup = tempDirectory.resolve("trusted-backup's.db");
        criarBancoTrocae(currentDatabase, "current-user");
        criarBancoTrocae(backup, "restored-user");

        DatabaseBackup.restaurarBackup("jdbc:sqlite:" + currentDatabase, backup);

        assertEquals("restored-user", buscarPrimeiroUsuario(currentDatabase));
    }

    @Test
    void rejeitaBackupCorrompidoSemAlterarBancoAtual() throws Exception {
        Path currentDatabase = tempDirectory.resolve("current.db");
        Path invalidBackup = tempDirectory.resolve("invalid.db");
        criarBancoTrocae(currentDatabase, "current-user");
        Files.writeString(invalidBackup, "not a SQLite database");

        assertThrows(
                SQLException.class,
                () -> DatabaseBackup.restaurarBackup("jdbc:sqlite:" + currentDatabase, invalidBackup)
        );

        assertEquals("current-user", buscarPrimeiroUsuario(currentDatabase));
    }

    @Test
    void rejeitaBackupComEstruturaIncompativelSemAlterarBancoAtual() throws Exception {
        Path currentDatabase = tempDirectory.resolve("current.db");
        Path invalidBackup = tempDirectory.resolve("incompatible.db");
        criarBancoTrocae(currentDatabase, "current-user");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + invalidBackup);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE unrelated (id INTEGER PRIMARY KEY)");
        }

        assertThrows(
                SQLException.class,
                () -> DatabaseBackup.restaurarBackup("jdbc:sqlite:" + currentDatabase, invalidBackup)
        );

        assertEquals("current-user", buscarPrimeiroUsuario(currentDatabase));
    }

    @Test
    void migraBancoLegadoSemPerderDadosEAplicaIntegridadeEIndices() throws Exception {
        String jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("legacy.db");
        criarBancoLegado(jdbcUrl);

        DatabaseConfig.initializeDatabase(jdbcUrl);
        DatabaseConfig.initializeDatabase(jdbcUrl);

        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            assertEquals(1, obterInteiro(statement, "PRAGMA foreign_keys"));
            assertEquals(2, obterInteiro(statement, "SELECT COUNT(*) FROM schema_migrations"));
            assertEquals(1, obterInteiro(statement, "SELECT COUNT(*) FROM anuncios WHERE titulo = 'Livro legado'"));
            assertEquals(1, obterInteiro(statement,
                    "SELECT COUNT(*) FROM pragma_table_info('anuncios') WHERE name = 'usuario_id'"));
            assertTrue(obterInteiro(statement,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name = 'idx_anuncios_status_id'") > 0);
            assertTrue(obterInteiro(statement,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name = 'idx_anuncios_status_titulo'") > 0);
            assertTrue(obterInteiro(statement,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name = 'idx_anuncios_busca_modalidade'") > 0);
        }

        assertThrows(SQLException.class, () -> inserirAnuncioComUsuario(jdbcUrl, 999));
        assertThrows(SQLException.class, () -> inserirAnuncioComModalidadeInvalida(jdbcUrl));
        assertThrows(SQLException.class, () -> inserirInteresseSemAnuncio(jdbcUrl));
    }

    @Test
    void recusaBancoComVersaoDeEsquemaMaisRecente() throws Exception {
        String jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("future.db");
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE schema_migrations (version INTEGER PRIMARY KEY)");
            statement.executeUpdate("INSERT INTO schema_migrations(version) VALUES (99)");
        }

        assertThrows(SQLException.class, () -> DatabaseConfig.initializeDatabase(jdbcUrl));
    }

    private void criarBancoLegado(String jdbcUrl) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE usuarios (
                        id INTEGER PRIMARY KEY,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE anuncios (
                        id INTEGER PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE interesses (
                        id INTEGER PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO usuarios(id, nome, email, senha_hash, salt, status)
                    VALUES (1, 'Ana', 'ana@example.com', 'hash', 'salt', 'ATIVO')
                    """);
            statement.executeUpdate("""
                    INSERT INTO anuncios(id, titulo, descricao, categoria, modalidade, nome, status)
                    VALUES (1, 'Livro legado', 'Em bom estado', 'Livros', 'TROCA', 'Ana', 'DISPONIVEL')
                    """);
        }
    }

    private void inserirAnuncioComUsuario(String jdbcUrl, int usuarioId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, usuario_id, status)
                     VALUES ('Teste', 'Teste', 'Outros', 'TROCA', 'Ana', ?, 'DISPONIVEL')
                     """)) {
            statement.setInt(1, usuarioId);
            statement.executeUpdate();
        }
    }

    private void inserirAnuncioComModalidadeInvalida(String jdbcUrl) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, status)
                    VALUES ('Teste', 'Teste', 'Outros', 'INVALIDA', 'Ana', 'DISPONIVEL')
                    """);
        }
    }

    private void inserirInteresseSemAnuncio(String jdbcUrl) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO interesses(anuncio_id, nome, mensagem, status)
                    VALUES (999, 'Bia', 'Tenho interesse', 'PENDENTE')
                    """);
        }
    }

    private int obterInteiro(Statement statement, String sql) throws SQLException {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private void criarBancoTrocae(Path database, String email) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE usuarios (
                        id INTEGER PRIMARY KEY,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE anuncios (
                        id INTEGER PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE interesses (
                        id INTEGER PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL,
                        criado_em TEXT
                    )
                    """);
            statement.executeUpdate("INSERT INTO usuarios(id, nome, email, senha_hash, salt, status) "
                    + "VALUES (1, 'Test User', '" + email + "', 'hash', 'salt', 'ATIVO')");
        }
    }

    private String buscarPrimeiroUsuario(Path database) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT email FROM usuarios LIMIT 1")) {
            assertTrue(result.next());
            return result.getString("email");
        }
    }
}
