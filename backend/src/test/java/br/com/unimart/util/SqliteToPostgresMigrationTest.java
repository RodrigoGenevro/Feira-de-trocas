package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteToPostgresMigrationTest {

    @TempDir
    Path tempDirectory;

    @Test
    void trataEmailComDiferencaDeMaiusculasComoUmaConta() throws Exception {
        Path first = createSource("first.db", "Ana@example.com", "same-hash");
        Path second = createSource("second.db", "ana@EXAMPLE.com", "same-hash");

        assertTrue(SqliteToPostgresMigration.detectarConflitosDeContas(List.of(first, second)).isEmpty());
    }

    @Test
    void bloqueiaContasComMesmoEmailENovasCredenciaisDivergentes() throws Exception {
        Path first = createSource("first.db", "ana@example.com", "hash-one");
        Path second = createSource("second.db", "ana@example.com", "hash-two");

        List<String> conflicts = SqliteToPostgresMigration.detectarConflitosDeContas(List.of(first, second));

        assertEquals(1, conflicts.size());
        assertTrue(conflicts.getFirst().contains("first.db"));
        assertTrue(conflicts.getFirst().contains("second.db"));
    }

    @Test
    void bloqueiaInteresseSemAnuncioDeOrigemCorrespondente() throws Exception {
        Path source = createSource("orphan.db", "ana@example.com", "same-hash");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + source);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO interesses(id, anuncio_id, nome, mensagem, status)
                    VALUES (1, 999, 'Bia', 'Tenho interesse', 'PENDENTE')
                    """);
        }

        assertThrows(
                java.sql.SQLException.class,
                () -> SqliteToPostgresMigration.detectarConflitosDeContas(List.of(source))
        );
    }

    private Path createSource(String fileName, String email, String passwordHash) throws Exception {
        Path database = tempDirectory.resolve(fileName);
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
                        status TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE interesses (
                        id INTEGER PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                    """);
            try (var insert = connection.prepareStatement("""
                    INSERT INTO usuarios(id, nome, email, turma, senha_hash, salt, status)
                    VALUES (1, 'Ana', ?, 'TDS', ?, 'salt', 'ATIVO')
                    """)) {
                insert.setString(1, email);
                insert.setString(2, passwordHash);
                insert.executeUpdate();
            }
        }
        return database;
    }
}
