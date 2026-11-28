package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConfigTest {

    @TempDir
    Path tempDirectory;

    @Test
    void aplicaMigracoesSqliteDeFormaRepetivelEAtivaChavesEstrangeiras() throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("trocae.db");

        DatabaseConfig.initializeDatabase(jdbcUrl);
        DatabaseConfig.initializeDatabase(jdbcUrl);

        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl)) {
            try (Statement statement = connection.createStatement();
                 ResultSet migrations = statement.executeQuery("SELECT COUNT(*) FROM schema_migrations")) {
                assertTrue(migrations.next());
                assertEquals(3, migrations.getInt(1));
            }
            try (Statement statement = connection.createStatement();
                 ResultSet foreignKeys = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(foreignKeys.next());
                assertEquals(1, foreignKeys.getInt(1));
            }
        }

        assertThrows(SQLException.class, () -> {
            try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome)
                         VALUES ('Inválido', '', '', 'INVALIDA', 'Ana')
                         """)) {
                statement.executeUpdate();
            }
        });
    }

    @Test
    void rejeitaUrlQueNaoSejaSqlite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DatabaseConfig.getConnection("jdbc:unsupported:ignored")
        );
    }
}