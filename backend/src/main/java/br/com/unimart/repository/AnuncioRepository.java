package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.util.DatabaseConfig;

/**
 * Camada de acesso a dados (DAO/Repository) responsável pelas operações SQL de Anuncio.
 */
public class AnuncioRepository {

    private final String jdbcUrl;

    public AnuncioRepository() {
        this(DatabaseConfig.getActiveDbUrl());
    }

    AnuncioRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public List<Anuncio> buscarDisponiveis(String termoBusca, String categoria, String modalidade) throws SQLException {
        return buscarPaginaDisponiveis(
                termoBusca, categoria, modalidade, "RECENTES", Integer.MAX_VALUE, 0
        ).anuncios();
    }

    public PaginaAnuncios buscarPaginaDisponiveis(
            String termoBusca, String categoria, String modalidade,
            String ordenacao, int limite, int deslocamento
    ) throws SQLException {
        if (limite < 1 || deslocamento < 0) {
            throw new IllegalArgumentException("Os parâmetros de paginação são inválidos.");
        }

        String termo = termoBusca == null ? "" : termoBusca.trim().toLowerCase(Locale.ROOT);
        String cat = categoria == null ? "" : categoria.trim();
        String mod = modalidade == null ? "" : modalidade.trim();
        List<String> valoresFiltro = new ArrayList<>();
        StringBuilder where = new StringBuilder(" FROM anuncios WHERE status = 'DISPONIVEL'");
        if (!termo.isEmpty()) {
            where.append(" AND lower(titulo || ' ' || descricao) LIKE ?");
            valoresFiltro.add("%" + termo + "%");
        }
        if (!cat.isEmpty()) {
            where.append(" AND categoria = ?");
            valoresFiltro.add(cat);
        }
        if (!mod.isEmpty()) {
            where.append(" AND modalidade = ?");
            valoresFiltro.add(mod);
        }
        String orderBy = switch (ordenacao == null ? "RECENTES" : ordenacao) {
            case "TITULO_ASC" -> " ORDER BY titulo COLLATE NOCASE ASC, id DESC";
            case "TITULO_DESC" -> " ORDER BY titulo COLLATE NOCASE DESC, id DESC";
            default -> " ORDER BY id DESC";
        };
        List<Anuncio> anuncios = new ArrayList<>();
        int total;

        try (Connection connection = abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*)" + where)) {
                    preencherFiltros(count, valoresFiltro);
                    try (ResultSet result = count.executeQuery()) {
                        total = result.next() ? result.getInt(1) : 0;
                    }
                }

                String sql = "SELECT id, titulo, descricao, categoria, modalidade, nome, turma, foto, " +
                             "status, criado_em, usuario_id" + where + orderBy + " LIMIT ? OFFSET ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    preencherFiltros(statement, valoresFiltro);
                    statement.setInt(valoresFiltro.size() + 1, limite);
                    statement.setInt(valoresFiltro.size() + 2, deslocamento);
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            anuncios.add(montarAnuncio(result));
                        }
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
        return new PaginaAnuncios(anuncios, total);
    }

    public int salvar(Anuncio anuncio) throws SQLException {
        String sql = "INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, turma, foto, usuario_id) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection c = abrirConexao();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            p.setString(1, anuncio.getTitulo());
            p.setString(2, anuncio.getDescricao());
            p.setString(3, anuncio.getCategoria());
            p.setString(4, anuncio.getModalidade());
            p.setString(5, anuncio.getNomeAutor());
            p.setString(6, anuncio.getTurma());
            p.setString(7, anuncio.getFotoUrl());
            if (anuncio.getUsuarioId() != null) {
                p.setInt(8, anuncio.getUsuarioId());
            } else {
                p.setNull(8, java.sql.Types.INTEGER);
            }

            int afetados = p.executeUpdate();
            if (afetados == 0) {
                throw new SQLException("Falha ao inserir anúncio no banco de dados.");
            }

            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    int idGerado = keys.getInt(1);
                    anuncio.setId(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public void atualizarDisponivelDoUsuario(
            Anuncio anuncio, int usuarioId, String nomeUsuario
    ) throws SQLException {
        String sql = "UPDATE anuncios SET titulo = ?, descricao = ?, categoria = ?, modalidade = ?, foto = ? " +
                     "WHERE id = ? AND status = ? " +
                     "AND (usuario_id = ? OR (usuario_id IS NULL AND lower(nome) = lower(?)))";
        try (Connection connection = abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, anuncio.getTitulo());
            statement.setString(2, anuncio.getDescricao());
            statement.setString(3, anuncio.getCategoria());
            statement.setString(4, anuncio.getModalidade());
            statement.setString(5, anuncio.getFotoUrl());
            statement.setInt(6, anuncio.getId());
            statement.setString(7, Anuncio.STATUS_DISPONIVEL);
            statement.setInt(8, usuarioId);
            statement.setString(9, nomeUsuario.trim());

            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "O anúncio não pertence a você ou não está disponível para edição."
                );
            }
        }
    }

    public void aceitarInteresse(int interesseId, int usuarioId, String nomeUsuario) throws SQLException {
        try (Connection connection = abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                int anuncioId = buscarAnuncioDisponivelDoInteresse(
                        connection, interesseId, usuarioId, nomeUsuario
                );
                atualizarStatusDoUsuario(
                        connection, anuncioId, usuarioId, nomeUsuario,
                        Anuncio.STATUS_DISPONIVEL, Anuncio.STATUS_RESERVADO
                );
                atualizarStatusInteresse(
                        connection, interesseId, anuncioId, "PENDENTE", "ACEITO"
                );
                encerrarOutrosInteressesPendentes(connection, anuncioId, interesseId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public void concluirAnuncio(int id, int usuarioId, String nomeUsuario) throws SQLException {
        try (Connection connection = abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                atualizarStatusDoUsuario(
                        connection, id, usuarioId, nomeUsuario,
                        "DISPONIVEL", "RESERVADO", Anuncio.STATUS_CONCLUIDO
                );
                encerrarInteressesPendentes(connection, id);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public void reabrirAnuncio(int id, int usuarioId, String nomeUsuario) throws SQLException {
        try (Connection connection = abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                atualizarStatusDoUsuario(
                        connection, id, usuarioId, nomeUsuario,
                        Anuncio.STATUS_RESERVADO, Anuncio.STATUS_DISPONIVEL
                );
                atualizarStatusInteressesAceitos(connection, id);
                reabrirInteressesEncerrados(connection, id);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public List<Anuncio> buscarPorUsuario(int usuarioId, String nomeUsuario) throws SQLException {
        List<Anuncio> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, descricao, categoria, modalidade, nome, turma, foto, status, criado_em, usuario_id FROM anuncios " +
                     "WHERE usuario_id = ? OR (usuario_id IS NULL AND lower(nome) = lower(?)) " +
                     "ORDER BY id DESC";

        try (Connection c = abrirConexao();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, usuarioId);
            p.setString(2, nomeUsuario != null ? nomeUsuario.trim() : "");

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Anuncio a = montarAnuncio(r);
                    int usuarioVinculado = r.getInt("usuario_id");
                    a.setUsuarioId(r.wasNull() ? null : usuarioVinculado);
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    private int buscarAnuncioDisponivelDoInteresse(
            Connection connection, int interesseId, int usuarioId, String nomeUsuario
    ) throws SQLException {
        String sql = "SELECT a.id FROM interesses i " +
                     "JOIN anuncios a ON a.id = i.anuncio_id " +
                     "WHERE i.id = ? AND i.status = 'PENDENTE' AND a.status = 'DISPONIVEL' " +
                     "AND (a.usuario_id = ? OR (a.usuario_id IS NULL AND lower(a.nome) = lower(?)))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interesseId);
            statement.setInt(2, usuarioId);
            statement.setString(3, nomeUsuario.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                            "O interesse não está pendente ou o anúncio não pertence a você."
                    );
                }
                return result.getInt("id");
            }
        }
    }

    private void atualizarStatusDoUsuario(
            Connection connection,
            int anuncioId,
            int usuarioId,
            String nomeUsuario,
            String statusAtual,
            String novoStatus
    ) throws SQLException {
        atualizarStatusDoUsuario(
                connection, anuncioId, usuarioId, nomeUsuario, statusAtual, null, novoStatus
        );
    }

    private void atualizarStatusDoUsuario(
            Connection connection,
            int anuncioId,
            int usuarioId,
            String nomeUsuario,
            String primeiroStatusAtual,
            String segundoStatusAtual,
            String novoStatus
    ) throws SQLException {
        String sql = "UPDATE anuncios SET status = ? WHERE id = ? " +
                     "AND (status = ? OR (? IS NOT NULL AND status = ?)) " +
                     "AND (usuario_id = ? OR (usuario_id IS NULL AND lower(nome) = lower(?)))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, novoStatus);
            statement.setInt(2, anuncioId);
            statement.setString(3, primeiroStatusAtual);
            statement.setString(4, segundoStatusAtual);
            statement.setString(5, segundoStatusAtual);
            statement.setInt(6, usuarioId);
            statement.setString(7, nomeUsuario.trim());

            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "O anúncio não pertence a você ou não permite essa alteração de status."
                );
            }
        }
    }

    private void atualizarStatusInteresse(
            Connection connection, int interesseId, int anuncioId, String statusAtual, String novoStatus
    ) throws SQLException {
        String sql = "UPDATE interesses SET status = ? " +
                     "WHERE id = ? AND anuncio_id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, novoStatus);
            statement.setInt(2, interesseId);
            statement.setInt(3, anuncioId);
            statement.setString(4, statusAtual);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Este interesse já foi respondido.");
            }
        }
    }

    private void encerrarInteressesPendentes(Connection connection, int anuncioId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE interesses SET status = 'ENCERRADO' " +
                        "WHERE anuncio_id = ? AND status = 'PENDENTE'"
        )) {
            statement.setInt(1, anuncioId);
            statement.executeUpdate();
        }
    }

    private void atualizarStatusInteressesAceitos(Connection connection, int anuncioId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE interesses SET status = 'CANCELADO' " +
                        "WHERE anuncio_id = ? AND status = 'ACEITO'"
        )) {
            statement.setInt(1, anuncioId);
            statement.executeUpdate();
        }
    }

    private void encerrarOutrosInteressesPendentes(
            Connection connection, int anuncioId, int interesseAceitoId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE interesses SET status = 'ENCERRADO' " +
                        "WHERE anuncio_id = ? AND id != ? AND status = 'PENDENTE'"
        )) {
            statement.setInt(1, anuncioId);
            statement.setInt(2, interesseAceitoId);
            statement.executeUpdate();
        }
    }

    private void reabrirInteressesEncerrados(Connection connection, int anuncioId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE interesses SET status = 'PENDENTE' " +
                        "WHERE anuncio_id = ? AND status = 'ENCERRADO'"
        )) {
            statement.setInt(1, anuncioId);
            statement.executeUpdate();
        }
    }

    private void rollback(Connection connection, Exception originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    private Connection abrirConexao() throws SQLException {
        return DatabaseConfig.getConnection(jdbcUrl);
    }

    private void preencherFiltros(PreparedStatement statement, List<String> valoresFiltro) throws SQLException {
        for (int i = 0; i < valoresFiltro.size(); i++) {
            statement.setString(i + 1, valoresFiltro.get(i));
        }
    }

    private Anuncio montarAnuncio(ResultSet r) throws SQLException {
        Anuncio anuncio = new Anuncio(
                r.getInt("id"),
                r.getString("titulo"),
                r.getString("descricao"),
                r.getString("categoria"),
                r.getString("modalidade"),
                r.getString("nome"),
                r.getString("turma"),
                r.getString("foto"),
                r.getString("status"),
                r.getString("criado_em")
        );
        int usuarioId = r.getInt("usuario_id");
        anuncio.setUsuarioId(r.wasNull() ? null : usuarioId);
        return anuncio;
    }
}
