package br.com.unimart.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:database/feira_trocas.db";
    private static final String DB_FALLBACK_URL = "jdbc:sqlite:../database/feira_trocas.db";
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
            if (cwd.getFileName() != null && cwd.getFileName().toString().equals("backend")) {
                Files.createDirectories(Paths.get("../database"));
                activeDbUrl = DB_FALLBACK_URL;
            } else {
                Files.createDirectories(Paths.get("database"));
                activeDbUrl = DB_URL;
            }
        } catch (Exception e) {
            activeDbUrl = DB_URL;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getActiveDbUrl());
    }

    public static void initDatabase() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "turma TEXT, " +
                    "senha_hash TEXT NOT NULL, " +
                    "salt TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'ATIVO', " +
                    "criado_em TEXT DEFAULT CURRENT_TIMESTAMP)");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS anuncios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "titulo TEXT NOT NULL, " +
                    "descricao TEXT NOT NULL, " +
                    "categoria TEXT NOT NULL, " +
                    "modalidade TEXT NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "turma TEXT, " +
                    "foto TEXT, " +
                    "usuario_id INTEGER, " +
                    "status TEXT NOT NULL DEFAULT 'DISPONIVEL', " +
                    "criado_em TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(usuario_id) REFERENCES usuarios(id))");

            try {
                s.executeUpdate("ALTER TABLE anuncios ADD COLUMN usuario_id INTEGER");
            } catch (SQLException ignored) {
                // A coluna já existe em bancos criados pela versão atual.
            }

            s.executeUpdate("CREATE TABLE IF NOT EXISTS interesses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "anuncio_id INTEGER NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "mensagem TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'PENDENTE', " +
                    "criado_em TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(anuncio_id) REFERENCES anuncios(id))");
        }
    }
}