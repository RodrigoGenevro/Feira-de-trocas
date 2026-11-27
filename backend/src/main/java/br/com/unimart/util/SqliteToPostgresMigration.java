package br.com.unimart.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class SqliteToPostgresMigration {

    private static final List<Tabela> TABELAS = List.of(
            new Tabela("usuarios", "id, nome, email, turma, senha_hash, salt, status, criado_em"),
            new Tabela(
                    "anuncios",
                    "id, titulo, descricao, categoria, modalidade, nome, turma, foto, usuario_id, status, criado_em"
            ),
            new Tabela("interesses", "id, anuncio_id, nome, mensagem, status, criado_em")
    );

    private SqliteToPostgresMigration() {
    }

    public static void main(String[] args) throws SQLException, IOException {
        if (args.length != 1 || args[0].isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o caminho do banco SQLite de origem como argumento."
            );
        }
        Path arquivoOrigem = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(arquivoOrigem)) {
            throw new IOException("O arquivo SQLite de origem não foi encontrado.");
        }
        String databaseUrl = DatabaseConfig.getActiveDbUrl();
        if (!databaseUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("Configure TROCAE_DATABASE_URL com a URL JDBC do PostgreSQL do Supabase.");
        }
        DatabaseConfig.initDatabase();
        migrar("jdbc:sqlite:" + arquivoOrigem, databaseUrl);
    }

    static void migrar(String sqliteUrl, String postgresUrl) throws SQLException {
        try (Connection origem = DriverManager.getConnection(sqliteUrl);
             Connection destino = DatabaseConfig.getConnection(postgresUrl)) {
            destino.setAutoCommit(false);
            try {
                validarDestinoVazio(destino);
                for (Tabela tabela : TABELAS) {
                    copiarTabela(origem, destino, tabela);
                }
                ajustarSequencias(destino);
                destino.commit();
            } catch (SQLException | RuntimeException erro) {
                try {
                    destino.rollback();
                } catch (SQLException erroRollback) {
                    erro.addSuppressed(erroRollback);
                }
                throw erro;
            }
        }
    }

    private static void validarDestinoVazio(Connection destino) throws SQLException {
        for (Tabela tabela : TABELAS) {
            try (Statement statement = destino.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tabela.nome())) {
                if (!result.next() || result.getLong(1) != 0) {
                    throw new SQLException(
                            "A migração foi cancelada: a tabela " + tabela.nome() + " já contém dados."
                    );
                }
            }
        }
    }

    private static void copiarTabela(Connection origem, Connection destino, Tabela tabela) throws SQLException {
        String sqlOrigem = "SELECT " + tabela.colunas() + " FROM " + tabela.nome() + " ORDER BY id";
        String sqlDestino = "INSERT INTO " + tabela.nome() + " (" + tabela.colunas() + ") VALUES ("
                + "?,".repeat(tabela.quantidadeColunas() - 1) + "?)";
        long registrosCopiados = 0;
        try (Statement consulta = origem.createStatement();
             ResultSet registros = consulta.executeQuery(sqlOrigem);
             PreparedStatement insercao = destino.prepareStatement(sqlDestino)) {
            while (registros.next()) {
                for (int indice = 1; indice <= tabela.quantidadeColunas(); indice++) {
                    insercao.setObject(indice, registros.getObject(indice));
                }
                insercao.executeUpdate();
                registrosCopiados++;
            }
        }

        try (Statement consulta = destino.createStatement();
             ResultSet contagem = consulta.executeQuery("SELECT COUNT(*) FROM " + tabela.nome())) {
            if (!contagem.next() || contagem.getLong(1) != registrosCopiados) {
                throw new SQLException("A validação da migração da tabela " + tabela.nome() + " falhou.");
            }
        }
    }

    private static void ajustarSequencias(Connection destino) throws SQLException {
        for (Tabela tabela : TABELAS) {
            String sql = "SELECT setval(pg_get_serial_sequence('" + tabela.nome()
                    + "', 'id'), GREATEST(COALESCE((SELECT MAX(id) FROM " + tabela.nome()
                    + "), 1), 1), EXISTS (SELECT 1 FROM " + tabela.nome() + "))";
            try (Statement statement = destino.createStatement()) {
                statement.executeQuery(sql).close();
            }
        }
    }

    private record Tabela(String nome, String colunas) {
        private int quantidadeColunas() {
            return colunas.split(",").length;
        }
    }
}
