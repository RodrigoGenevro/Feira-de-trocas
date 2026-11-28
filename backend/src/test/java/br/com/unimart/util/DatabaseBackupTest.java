package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
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
    void criaBackupERecuperaDadosAnteriores() throws SQLException, IOException {
        Path database = tempDirectory.resolve("trocae.db");
        Path backup = tempDirectory.resolve("backup.db");
        String jdbcUrl = "jdbc:sqlite:" + database;
        DatabaseConfig.initializeDatabase(jdbcUrl);

        inserirUsuario(jdbcUrl, "ana@example.com");
        DatabaseBackup.criarBackup(jdbcUrl, backup);
        inserirUsuario(jdbcUrl, "bruno@example.com");

        DatabaseBackup.restaurarBackup(jdbcUrl, backup);

        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM usuarios WHERE email = ?"
             )) {
            statement.setString(1, "ana@example.com");
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
        }
    }

    @Test
    void recusaRestaurarArquivoQueNaoSejaBancoSqlite() throws SQLException, IOException {
        Path database = tempDirectory.resolve("trocae.db");
        Path backupInvalido = tempDirectory.resolve("invalido.db");
        String jdbcUrl = "jdbc:sqlite:" + database;
        DatabaseConfig.initializeDatabase(jdbcUrl);
        inserirUsuario(jdbcUrl, "ana@example.com");
        Files.writeString(backupInvalido, "não é um banco SQLite");

        assertThrows(
                SQLException.class,
                () -> DatabaseBackup.restaurarBackup(jdbcUrl, backupInvalido)
        );

        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }
    }

    private void inserirUsuario(String jdbcUrl, String email) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO usuarios(nome, email, turma, senha_hash, salt)
                     VALUES ('Usuário de teste', ?, 'Turma', 'hash', 'salt')
                     """)) {
            statement.setString(1, email);
            statement.executeUpdate();
        }
    }
}
