package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.unimart.model.Anuncio;
import br.com.unimart.util.DatabaseConfig;

/**
 * Camada de acesso a dados (DAO/Repository) responsável pelas operações SQL de Anuncio.
 */
public class AnuncioRepository {

    public List<Anuncio> buscarDisponiveis(String termoBusca, String categoria, String modalidade) throws SQLException {
        List<Anuncio> lista = new ArrayList<>();

        String sql = "SELECT id, titulo, descricao, categoria, modalidade, nome, turma, foto, status, criado_em FROM anuncios " +
                     "WHERE (? = '' OR lower(titulo || ' ' || descricao) LIKE ?) " +
                     "AND (? = '' OR categoria = ?) " +
                     "AND (? = '' OR modalidade = ?) " +
                     "AND status != 'CONCLUIDO' " +
                     "ORDER BY id DESC";

        String q = termoBusca != null ? termoBusca.trim() : "";
        String cat = categoria != null ? categoria.trim() : "";
        String mod = modalidade != null ? modalidade.trim() : "";

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, q);
            p.setString(2, "%" + q.toLowerCase() + "%");
            p.setString(3, cat);
            p.setString(4, cat);
            p.setString(5, mod);
            p.setString(6, mod);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    lista.add(montarAnuncio(r));
                }
            }
        }
        return lista;
    }

    public int salvar(Anuncio anuncio) throws SQLException {
        String sql = "INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, turma, foto, usuario_id) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection c = DatabaseConfig.getConnection();
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

    public void atualizarStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE anuncios SET status = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, novoStatus);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    public List<Anuncio> buscarPorUsuario(int usuarioId, String nomeUsuario) throws SQLException {
        List<Anuncio> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, descricao, categoria, modalidade, nome, turma, foto, status, criado_em, usuario_id FROM anuncios " +
                     "WHERE usuario_id = ? OR (usuario_id IS NULL AND lower(nome) = lower(?)) " +
                     "ORDER BY id DESC";

        try (Connection c = DatabaseConfig.getConnection();
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

    private Anuncio montarAnuncio(ResultSet r) throws SQLException {
        return new Anuncio(
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
    }
}
