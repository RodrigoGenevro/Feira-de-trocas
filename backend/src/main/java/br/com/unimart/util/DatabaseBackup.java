package br.com.unimart.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DatabaseBackup {

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    private DatabaseBackup() {
    }

    public static void criarBackup(Path destino) throws SQLException, IOException {
        criarBackup(DatabaseConfig.getActiveDbUrl(), destino);
    }

    public static void restaurarBackup(Path origem) throws SQLException, IOException {
        restaurarBackup(DatabaseConfig.getActiveDbUrl(), origem);
    }

    static void criarBackup(String jdbcUrl, Path destino) throws SQLException, IOException {
        Path arquivoOrigem = resolverArquivoBanco(jdbcUrl);
        Path arquivoDestino = resolverDestino(destino);
        validarDestino(arquivoOrigem, arquivoDestino);

        Path arquivoTemporario = arquivoDestino.resolveSibling(
                "." + arquivoDestino.getFileName() + "." + UUID.randomUUID() + ".tmp"
        );
        String caminhoSql = escaparLiteralSqlite(arquivoTemporario);

        try {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 Statement statement = connection.createStatement()) {
                statement.execute("VACUUM INTO '" + caminhoSql + "'");
            }
            moverSubstituindo(arquivoTemporario, arquivoDestino);
        } finally {
            Files.deleteIfExists(arquivoTemporario);
        }
    }

    static void restaurarBackup(String jdbcUrl, Path origem) throws SQLException, IOException {
        Path arquivoBanco = resolverArquivoBanco(jdbcUrl);
        Path arquivoBackup = resolverDestino(origem);
        validarDestino(arquivoBanco, arquivoBackup);
        if (!Files.isRegularFile(arquivoBanco)) {
            throw new IOException("O banco de dados atual não foi encontrado.");
        }
        validarBancoSqlite(arquivoBackup);

        Path stagedDatabase = arquivoBanco.resolveSibling(
                "." + arquivoBanco.getFileName() + "." + UUID.randomUUID() + ".restore"
        );
        Path rollbackDatabase = arquivoBanco.resolveSibling(
                "." + arquivoBanco.getFileName() + "." + UUID.randomUUID() + ".rollback"
        );
        boolean bancoSubstituido = false;

        try {
            criarBackup("jdbc:sqlite:" + arquivoBackup, stagedDatabase);
            validarBancoSqlite(stagedDatabase);
            normalizarBancoParaSubstituicao(jdbcUrl, arquivoBanco);
            criarBackup(jdbcUrl, rollbackDatabase);
            moverSubstituindo(stagedDatabase, arquivoBanco);
            bancoSubstituido = true;
            removerArquivosJournal(arquivoBanco);
            validarBancoSqlite(arquivoBanco);
        } catch (SQLException | IOException | RuntimeException erro) {
            if (bancoSubstituido) {
                try {
                    normalizarBancoParaSubstituicao(jdbcUrl, arquivoBanco);
                    moverSubstituindo(rollbackDatabase, arquivoBanco);
                    removerArquivosJournal(arquivoBanco);
                } catch (IOException erroRestauracao) {
                    erro.addSuppressed(erroRestauracao);
                } catch (SQLException erroRestauracao) {
                    erro.addSuppressed(erroRestauracao);
                }
            }
            throw erro;
        } finally {
            Files.deleteIfExists(stagedDatabase);
            Files.deleteIfExists(rollbackDatabase);
        }
    }

    private static Path resolverArquivoBanco(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith(SQLITE_URL_PREFIX)) {
            throw new IllegalArgumentException("A URL do banco de dados SQLite é inválida.");
        }
        return Paths.get(jdbcUrl.substring(SQLITE_URL_PREFIX.length()))
                .toAbsolutePath()
                .normalize();
    }

    private static Path resolverDestino(Path destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Selecione um arquivo de banco de dados.");
        }
        return destino.toAbsolutePath().normalize();
    }

    private static void validarDestino(Path arquivoOrigem, Path arquivoDestino) throws IOException {
        if (arquivoOrigem.equals(arquivoDestino)) {
            throw new IllegalArgumentException("O arquivo selecionado não pode ser o banco de dados atual.");
        }

        Path pastaDestino = arquivoDestino.getParent();
        if (pastaDestino == null || !Files.isDirectory(pastaDestino)) {
            throw new IOException("A pasta de destino do backup não existe.");
        }
    }

    private static String escaparLiteralSqlite(Path path) {
        return path.toString().replace("'", "''");
    }

    private static void validarBancoSqlite(Path database) throws SQLException, IOException {
        if (!Files.isRegularFile(database) || Files.size(database) == 0) {
            throw new IOException("O arquivo selecionado não contém um banco SQLite válido.");
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA quick_check")) {
            boolean hasResult = false;
            while (result.next()) {
                hasResult = true;
                if (!"ok".equalsIgnoreCase(result.getString(1))) {
                    throw new SQLException("A verificação de integridade do banco falhou.");
                }
            }
            if (!hasResult) {
                throw new SQLException("A verificação de integridade do banco não retornou resultado.");
            }
        }

        Map<String, Set<String>> colunasObrigatorias = Map.of(
                "usuarios", Set.of("id", "nome", "email", "turma", "senha_hash", "salt", "status", "criado_em"),
                "anuncios", Set.of(
                        "id", "titulo", "descricao", "categoria", "modalidade",
                        "nome", "turma", "foto", "usuario_id", "status", "criado_em"
                ),
                "interesses", Set.of(
                        "id", "anuncio_id", "nome", "mensagem", "status", "criado_em"
                )
        );
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            for (Map.Entry<String, Set<String>> tabela : colunasObrigatorias.entrySet()) {
                Set<String> colunasEncontradas = new HashSet<>();
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery("PRAGMA table_info(" + tabela.getKey() + ")")) {
                    while (result.next()) {
                        colunasEncontradas.add(result.getString("name"));
                    }
                }
                if (!colunasEncontradas.containsAll(tabela.getValue())) {
                    throw new SQLException("O arquivo não contém uma estrutura compatível com o Trocaê.");
                }
            }
        }
    }

    private static void moverSubstituindo(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void normalizarBancoParaSubstituicao(String jdbcUrl, Path database)
            throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement checkpointStatement = connection.createStatement();
             ResultSet checkpoint = checkpointStatement.executeQuery("PRAGMA wal_checkpoint(TRUNCATE)")) {
            if (!checkpoint.next() || checkpoint.getInt(1) != 0) {
                throw new SQLException("O banco está em uso e não pode ser substituído agora.");
            }
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet journalMode = statement.executeQuery("PRAGMA journal_mode = DELETE")) {
            if (!journalMode.next() || !"delete".equalsIgnoreCase(journalMode.getString(1))) {
                throw new SQLException("Não foi possível preparar o banco para a restauração.");
            }
        }
        removerArquivosJournal(database);
    }

    private static void removerArquivosJournal(Path database) throws IOException {
        Files.deleteIfExists(Paths.get(database.toString() + "-wal"));
        Files.deleteIfExists(Paths.get(database.toString() + "-shm"));
    }
}
