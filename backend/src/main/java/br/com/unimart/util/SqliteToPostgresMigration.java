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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SqliteToPostgresMigration {

    private static final String[] REQUIRED_TABLES = {"usuarios", "anuncios", "interesses"};

    private SqliteToPostgresMigration() {
    }

    public static void main(String[] args) throws SQLException, IOException {
        if (args.length < 2 || (!"--dry-run".equals(args[0]) && !"--apply".equals(args[0]))) {
            throw new IllegalArgumentException(
                    "Use --dry-run <banco-1.db> [banco-2.db ...] para validar ou "
                            + "--apply <banco-1.db> [banco-2.db ...] para importar."
            );
        }

        boolean aplicar = "--apply".equals(args[0]);
        List<Path> arquivos = new ArrayList<>();
        Set<Path> caminhosUnicos = new HashSet<>();
        for (int indice = 1; indice < args.length; indice++) {
            Path arquivo = Path.of(args[indice]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(arquivo)) {
                throw new IOException("O arquivo SQLite não foi encontrado: " + arquivo);
            }
            if (!caminhosUnicos.add(arquivo)) {
                throw new IllegalArgumentException("O mesmo arquivo SQLite foi informado mais de uma vez.");
            }
            arquivos.add(arquivo);
        }

        String databaseUrl = DatabaseConfig.getActiveDbUrl();
        if (!databaseUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("Configure TROCAE_DATABASE_URL com a URL JDBC do PostgreSQL do Supabase.");
        }

        List<Origem> origens = inspecionarOrigens(arquivos);
        List<String> conflitos = encontrarConflitosDeContas(origens);
        imprimirPlano(origens, conflitos);
        if (!conflitos.isEmpty()) {
            throw new IllegalStateException(
                    "A importação foi bloqueada. Revise as contas divergentes nas cópias SQLite e execute novamente."
            );
        }
        if (!aplicar) {
            System.out.println("Simulação concluída; nenhum dado foi gravado.");
            return;
        }

        DatabaseConfig.initDatabase();
        migrar(origens, databaseUrl);
        System.out.println("Importação concluída e validada.");
    }

    static List<String> detectarConflitosDeContas(List<Path> arquivos) throws SQLException {
        return encontrarConflitosDeContas(inspecionarOrigens(arquivos));
    }

    private static List<Origem> inspecionarOrigens(List<Path> arquivos) throws SQLException {
        List<Origem> origens = new ArrayList<>();
        for (Path arquivo : arquivos) {
            String jdbcUrl = "jdbc:sqlite:" + arquivo;
            try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
                for (String tabela : REQUIRED_TABLES) {
                    if (!tabelaExiste(connection, tabela)) {
                        throw new SQLException(
                                "A base " + arquivo.getFileName() + " não contém a tabela " + tabela + "."
                        );
                    }
                }
                validarOrigem(connection, arquivo);
                List<Conta> contas = buscarContas(connection, arquivo);
                origens.add(new Origem(
                        arquivo,
                        jdbcUrl,
                        contas,
                        contar(connection, "anuncios"),
                        contar(connection, "interesses"),
                        tabelaExiste(connection, "denuncias") ? contar(connection, "denuncias") : 0,
                        colunaExiste(connection, "anuncios", "usuario_id"),
                        colunaExiste(connection, "interesses", "usuario_id"),
                        tabelaExiste(connection, "denuncias")
                ));
            }
        }
        if (origens.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos uma base SQLite.");
        }
        return origens;
    }

    private static void validarOrigem(Connection connection, Path arquivo) throws SQLException {
        exigirColunas(connection, "usuarios", List.of(
                "id", "nome", "email", "senha_hash", "salt", "status"
        ));
        exigirColunas(connection, "anuncios", List.of(
                "id", "titulo", "descricao", "categoria", "modalidade", "nome", "status"
        ));
        exigirColunas(connection, "interesses", List.of(
                "id", "anuncio_id", "nome", "mensagem", "status"
        ));
        verificarNenhum(connection, """
                SELECT COUNT(*) FROM usuarios
                WHERE id <= 0 OR nome IS NULL OR email IS NULL OR trim(email) = ''
                   OR senha_hash IS NULL OR salt IS NULL OR status IS NULL
                """, arquivo, "conta sem campos obrigatórios");
        verificarNenhum(connection, """
                SELECT COUNT(*) FROM anuncios
                WHERE titulo IS NULL OR descricao IS NULL OR categoria IS NULL OR nome IS NULL
                   OR modalidade IS NULL OR modalidade NOT IN ('TROCA', 'DOACAO')
                   OR status IS NULL OR status NOT IN ('DISPONIVEL', 'RESERVADO', 'CONCLUIDO')
                """, arquivo, "anúncio com campos ou status inválidos");
        verificarNenhum(connection, """
                SELECT COUNT(*) FROM interesses
                WHERE anuncio_id IS NULL OR nome IS NULL OR mensagem IS NULL
                   OR status IS NULL OR status NOT IN ('PENDENTE', 'ACEITO', 'ENCERRADO', 'CANCELADO')
                """, arquivo, "interesse com campos ou status inválidos");
        verificarNenhum(connection, """
                SELECT COUNT(*) FROM interesses i
                LEFT JOIN anuncios a ON a.id = i.anuncio_id
                WHERE a.id IS NULL
                """, arquivo, "interesse sem anúncio correspondente");

        if (colunaExiste(connection, "anuncios", "usuario_id")) {
            verificarNenhum(connection, """
                    SELECT COUNT(*) FROM anuncios a
                    LEFT JOIN usuarios u ON u.id = a.usuario_id
                    WHERE a.usuario_id IS NOT NULL AND u.id IS NULL
                    """, arquivo, "anúncio com usuário inexistente");
        }
        if (colunaExiste(connection, "interesses", "usuario_id")) {
            verificarNenhum(connection, """
                    SELECT COUNT(*) FROM interesses i
                    LEFT JOIN usuarios u ON u.id = i.usuario_id
                    WHERE i.usuario_id IS NOT NULL AND u.id IS NULL
                    """, arquivo, "interesse com usuário inexistente");
        }
        if (tabelaExiste(connection, "denuncias")) {
            exigirColunas(connection, "denuncias", List.of("id", "anuncio_id", "usuario_id", "motivo"));
            verificarNenhum(connection, """
                    SELECT COUNT(*) FROM denuncias d
                    LEFT JOIN anuncios a ON a.id = d.anuncio_id
                    LEFT JOIN usuarios u ON u.id = d.usuario_id
                    WHERE a.id IS NULL OR u.id IS NULL OR d.motivo IS NULL OR trim(d.motivo) = ''
                    """, arquivo, "denúncia com referência ou motivo inválido");
        }
    }

    private static List<Conta> buscarContas(Connection connection, Path arquivo) throws SQLException {
        exigirColunas(connection, "usuarios", List.of(
                "id", "nome", "email", "senha_hash", "salt", "status"
        ));
        String turma = colunaExiste(connection, "usuarios", "turma") ? "COALESCE(turma, '')" : "''";
        String criadoEm = colunaExiste(connection, "usuarios", "criado_em") ? "criado_em" : "NULL";
        String sql = "SELECT id, nome, email, " + turma + " AS turma, senha_hash, salt, status, "
                + criadoEm + " AS criado_em FROM usuarios ORDER BY id";
        List<Conta> contas = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                int id = result.getInt("id");
                String email = valor(result.getString("email"));
                if (id <= 0 || normalizarEmail(email).isEmpty()) {
                    throw new SQLException(
                            "A base " + arquivo.getFileName() + " contém uma conta sem ID ou e-mail válido."
                    );
                }
                contas.add(new Conta(
                        id, valor(result.getString("nome")), email, valor(result.getString("turma")),
                        valor(result.getString("senha_hash")), valor(result.getString("salt")),
                        valor(result.getString("status")), result.getString("criado_em")
                ));
            }
        }
        return contas;
    }

    private static List<String> encontrarConflitosDeContas(List<Origem> origens) {
        Map<String, ContaOrigem> contasPorEmail = new HashMap<>();
        List<String> conflitos = new ArrayList<>();
        for (Origem origem : origens) {
            for (Conta conta : origem.contas()) {
                String email = normalizarEmail(conta.email());
                ContaOrigem anterior = contasPorEmail.putIfAbsent(email, new ContaOrigem(origem, conta));
                if (anterior != null && !anterior.conta().mesmaIdentidade(conta)) {
                    conflitos.add(
                            "Contas divergentes para e-mail normalizado nas bases "
                                    + anterior.origem().arquivo().getFileName() + " (usuário "
                                    + anterior.conta().id() + ") e " + origem.arquivo().getFileName()
                                    + " (usuário " + conta.id() + ")."
                    );
                }
            }
        }
        return conflitos;
    }

    private static void imprimirPlano(List<Origem> origens, List<String> conflitos) {
        long usuarios = origens.stream()
                .flatMap(origem -> origem.contas().stream())
                .map(conta -> normalizarEmail(conta.email()))
                .distinct()
                .count();
        long anuncios = origens.stream().mapToLong(Origem::anuncios).sum();
        long interesses = origens.stream().mapToLong(Origem::interesses).sum();
        long denuncias = origens.stream().mapToLong(Origem::denuncias).sum();

        System.out.println("Plano de importação SQLite → PostgreSQL:");
        for (Origem origem : origens) {
            System.out.printf(
                    "- %s: %d contas, %d anúncios, %d interesses, %d denúncias%n",
                    origem.arquivo().getFileName(),
                    origem.contas().size(),
                    origem.anuncios(),
                    origem.interesses(),
                    origem.denuncias()
            );
        }
        System.out.printf(
                "Totais previstos: %d contas deduplicadas, %d anúncios, %d interesses, %d denúncias.%n",
                usuarios, anuncios, interesses, denuncias
        );
        if (!conflitos.isEmpty()) {
            System.out.println("Conflitos que exigem revisão manual:");
            conflitos.forEach(conflito -> System.out.println("- " + conflito));
        }
    }

    private static void migrar(List<Origem> origens, String postgresUrl) throws SQLException {
        try (Connection destino = DatabaseConfig.getConnection(postgresUrl)) {
            destino.setAutoCommit(false);
            try {
                validarDestinoVazio(destino);
                Map<String, Integer> usuariosDestino = new HashMap<>();
                for (Origem origem : origens) {
                    importarOrigem(origem, destino, usuariosDestino);
                }
                validarContagem(destino, "usuarios", usuariosDestino.size());
                validarContagem(destino, "anuncios", origens.stream().mapToLong(Origem::anuncios).sum());
                validarContagem(destino, "interesses", origens.stream().mapToLong(Origem::interesses).sum());
                validarContagem(destino, "denuncias", origens.stream().mapToLong(Origem::denuncias).sum());
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

    private static void importarOrigem(
            Origem origem, Connection destino, Map<String, Integer> usuariosDestino
    ) throws SQLException {
        Map<Integer, Integer> usuariosId = new HashMap<>();
        Map<Integer, Integer> anunciosId = new HashMap<>();
        try (Connection fonte = DriverManager.getConnection(origem.jdbcUrl())) {
            for (Conta conta : origem.contas()) {
                String email = normalizarEmail(conta.email());
                Integer destinoId = usuariosDestino.get(email);
                if (destinoId == null) {
                    destinoId = inserirUsuario(destino, conta);
                    usuariosDestino.put(email, destinoId);
                }
                usuariosId.put(conta.id(), destinoId);
            }

            importarAnuncios(origem, fonte, destino, usuariosId, anunciosId);
            importarInteresses(origem, fonte, destino, usuariosId, anunciosId);
            if (origem.temDenuncias()) {
                importarDenuncias(fonte, destino, usuariosId, anunciosId);
            }
        }
    }

    private static int inserirUsuario(Connection destino, Conta conta) throws SQLException {
        String sql = "INSERT INTO usuarios(nome, email, turma, senha_hash, salt, status, criado_em) "
                + "VALUES (?, ?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP::text)) RETURNING id";
        try (PreparedStatement statement = destino.prepareStatement(sql)) {
            statement.setString(1, conta.nome());
            statement.setString(2, conta.email());
            statement.setString(3, conta.turma());
            statement.setString(4, conta.senhaHash());
            statement.setString(5, conta.salt());
            statement.setString(6, conta.status());
            statement.setString(7, conta.criadoEm());
            return inserirERetornarId(statement, "usuário");
        }
    }

    private static void importarAnuncios(
            Origem origem, Connection fonte, Connection destino,
            Map<Integer, Integer> usuariosId, Map<Integer, Integer> anunciosId
    ) throws SQLException {
        exigirColunas(fonte, "anuncios", List.of(
                "id", "titulo", "descricao", "categoria", "modalidade", "nome", "status"
        ));
        String usuarioId = origem.anuncioTemUsuarioId() ? "usuario_id" : "NULL AS usuario_id";
        String turma = colunaExiste(fonte, "anuncios", "turma") ? "turma" : "NULL AS turma";
        String foto = colunaExiste(fonte, "anuncios", "foto") ? "foto" : "NULL AS foto";
        String criadoEm = colunaExiste(fonte, "anuncios", "criado_em") ? "criado_em" : "NULL AS criado_em";
        String sql = "SELECT id, titulo, descricao, categoria, modalidade, nome, " + turma + ", "
                + foto + ", " + usuarioId + ", status, " + criadoEm + " FROM anuncios ORDER BY id";
        String insert = """
                INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, turma, foto, usuario_id,
                                     status, criado_em)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP::text))
                RETURNING id
                """;
        try (Statement consulta = fonte.createStatement();
             ResultSet registros = consulta.executeQuery(sql);
             PreparedStatement insercao = destino.prepareStatement(insert)) {
            while (registros.next()) {
                int sourceId = registros.getInt("id");
                insercao.setString(1, registros.getString("titulo"));
                insercao.setString(2, registros.getString("descricao"));
                insercao.setString(3, registros.getString("categoria"));
                insercao.setString(4, registros.getString("modalidade"));
                insercao.setString(5, registros.getString("nome"));
                insercao.setString(6, registros.getString("turma"));
                insercao.setString(7, registros.getString("foto"));
                setMappedId(insercao, 8, registros, "usuario_id", usuariosId, origem, "anúncio");
                insercao.setString(9, registros.getString("status"));
                insercao.setString(10, registros.getString("criado_em"));
                anunciosId.put(sourceId, inserirERetornarId(insercao, "anúncio"));
            }
        }
    }

    private static void importarInteresses(
            Origem origem, Connection fonte, Connection destino,
            Map<Integer, Integer> usuariosId, Map<Integer, Integer> anunciosId
    ) throws SQLException {
        exigirColunas(fonte, "interesses", List.of(
                "id", "anuncio_id", "nome", "mensagem", "status"
        ));
        String usuarioId = origem.interesseTemUsuarioId() ? "usuario_id" : "NULL AS usuario_id";
        String criadoEm = colunaExiste(fonte, "interesses", "criado_em") ? "criado_em" : "NULL AS criado_em";
        String sql = "SELECT id, anuncio_id, nome, mensagem, " + usuarioId + ", status, "
                + criadoEm + " FROM interesses ORDER BY id";
        String insert = """
                INSERT INTO interesses(anuncio_id, nome, mensagem, usuario_id, status, criado_em)
                VALUES (?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP::text))
                """;
        try (Statement consulta = fonte.createStatement();
             ResultSet registros = consulta.executeQuery(sql);
             PreparedStatement insercao = destino.prepareStatement(insert)) {
            while (registros.next()) {
                insercao.setInt(1, mappedId(
                        anunciosId, registros.getInt("anuncio_id"), origem, "anúncio do interesse"
                ));
                insercao.setString(2, registros.getString("nome"));
                insercao.setString(3, registros.getString("mensagem"));
                setMappedId(insercao, 4, registros, "usuario_id", usuariosId, origem, "interesse");
                insercao.setString(5, registros.getString("status"));
                insercao.setString(6, registros.getString("criado_em"));
                insercao.executeUpdate();
            }
        }
    }

    private static void importarDenuncias(
            Connection fonte, Connection destino,
            Map<Integer, Integer> usuariosId, Map<Integer, Integer> anunciosId
    ) throws SQLException {
        exigirColunas(fonte, "denuncias", List.of("anuncio_id", "usuario_id", "motivo"));
        String detalhes = colunaExiste(fonte, "denuncias", "detalhes") ? "detalhes" : "NULL AS detalhes";
        String criadoEm = colunaExiste(fonte, "denuncias", "criado_em") ? "criado_em" : "NULL AS criado_em";
        String sql = "SELECT anuncio_id, usuario_id, motivo, " + detalhes + ", "
                + criadoEm + " FROM denuncias ORDER BY id";
        String insert = """
                INSERT INTO denuncias(anuncio_id, usuario_id, motivo, detalhes, criado_em)
                VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP::text))
                """;
        try (Statement consulta = fonte.createStatement();
             ResultSet registros = consulta.executeQuery(sql);
             PreparedStatement insercao = destino.prepareStatement(insert)) {
            while (registros.next()) {
                insercao.setInt(1, mappedId(
                        anunciosId, registros.getInt("anuncio_id"), null, "anúncio da denúncia"
                ));
                insercao.setInt(2, mappedId(
                        usuariosId, registros.getInt("usuario_id"), null, "usuário da denúncia"
                ));
                insercao.setString(3, registros.getString("motivo"));
                insercao.setString(4, registros.getString("detalhes"));
                insercao.setString(5, registros.getString("criado_em"));
                insercao.executeUpdate();
            }
        }
    }

    private static void setMappedId(
            PreparedStatement statement, int parameter, ResultSet registro, String column,
            Map<Integer, Integer> ids, Origem origem, String entidade
    ) throws SQLException {
        int sourceId = registro.getInt(column);
        if (registro.wasNull()) {
            statement.setNull(parameter, java.sql.Types.INTEGER);
        } else {
            statement.setInt(parameter, mappedId(ids, sourceId, origem, entidade));
        }
    }

    private static int mappedId(
            Map<Integer, Integer> ids, int sourceId, Origem origem, String entidade
    ) throws SQLException {
        Integer mapped = ids.get(sourceId);
        if (mapped == null) {
            String arquivo = origem == null ? "origem SQLite" : origem.arquivo().getFileName().toString();
            throw new SQLException("Referência a " + entidade + " inexistente na base " + arquivo + ".");
        }
        return mapped;
    }

    private static int inserirERetornarId(PreparedStatement statement, String entidade) throws SQLException {
        try (ResultSet resultado = statement.executeQuery()) {
            if (!resultado.next()) {
                throw new SQLException("O PostgreSQL não retornou o ID do novo registro de " + entidade + ".");
            }
            return resultado.getInt(1);
        }
    }

    private static void validarDestinoVazio(Connection destino) throws SQLException {
        for (String tabela : List.of("usuarios", "anuncios", "interesses", "denuncias")) {
            try (Statement statement = destino.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
                if (!result.next() || result.getLong(1) != 0) {
                    throw new SQLException(
                            "A importação foi cancelada: a tabela " + tabela + " já contém dados."
                    );
                }
            }
        }
    }

    private static void verificarNenhum(
            Connection connection, String sql, Path arquivo, String descricao
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            if (!result.next() || result.getLong(1) != 0) {
                throw new SQLException(
                        "A base " + arquivo.getFileName() + " contém " + descricao + "."
                );
            }
        }
    }

    private static void validarContagem(Connection destino, String tabela, long esperados) throws SQLException {
        try (Statement statement = destino.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            if (!result.next() || result.getLong(1) != esperados) {
                throw new SQLException("A validação da quantidade de registros da tabela " + tabela + " falhou.");
            }
        }
    }

    private static long contar(Connection connection, String tabela) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            if (!result.next()) {
                throw new SQLException("Não foi possível contar os registros da tabela " + tabela + ".");
            }
            return result.getLong(1);
        }
    }

    private static boolean tabelaExiste(Connection connection, String tabela) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"
        )) {
            statement.setString(1, tabela);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean colunaExiste(Connection connection, String tabela, String coluna) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            while (result.next()) {
                if (coluna.equalsIgnoreCase(result.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void exigirColunas(Connection connection, String tabela, List<String> colunas)
            throws SQLException {
        for (String coluna : colunas) {
            if (!colunaExiste(connection, tabela, coluna)) {
                throw new SQLException("A tabela " + tabela + " não contém a coluna " + coluna + ".");
            }
        }
    }

    private static String valor(String value) {
        return value == null ? "" : value;
    }

    private static String normalizarEmail(String email) {
        return valor(email).trim().toLowerCase(Locale.ROOT);
    }

    private record Origem(
            Path arquivo,
            String jdbcUrl,
            List<Conta> contas,
            long anuncios,
            long interesses,
            long denuncias,
            boolean anuncioTemUsuarioId,
            boolean interesseTemUsuarioId,
            boolean temDenuncias
    ) {
    }

    private record Conta(
            int id,
            String nome,
            String email,
            String turma,
            String senhaHash,
            String salt,
            String status,
            String criadoEm
    ) {
        private boolean mesmaIdentidade(Conta outra) {
            return nome.equals(outra.nome)
                    && normalizarEmail(email).equals(normalizarEmail(outra.email))
                    && turma.equals(outra.turma)
                    && senhaHash.equals(outra.senhaHash)
                    && salt.equals(outra.salt)
                    && status.equals(outra.status);
        }
    }

    private record ContaOrigem(Origem origem, Conta conta) {
    }
}
