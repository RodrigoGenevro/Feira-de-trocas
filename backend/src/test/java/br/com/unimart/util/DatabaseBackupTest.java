package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
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
