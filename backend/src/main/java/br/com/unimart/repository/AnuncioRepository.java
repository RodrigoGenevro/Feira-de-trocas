package br.com.unimart.repository;

import br.com.unimart.model.Anuncio;
import br.com.unimart.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de acesso a dados (DAO/Repository) responsável pelas operações SQL de Anuncio.
 */
public class AnuncioRepository {

    public List<Anuncio> buscarDisponiveis(String termoBusca, String categoria, String modalidade) throws SQLException {
        List<Anuncio> lista = new ArrayList<>();

        String sql = "SELECT * FROM anuncios " +
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
                    Anuncio a = new Anuncio(
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
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    public int salvar(Anuncio anuncio) throws SQLException {
        String sql = "INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, turma, foto) VALUES(?,?,?,?,?,?,?)";

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            p.setString(1, anuncio.getTitulo());
            p.setString(2, anuncio.getDescricao());
            p.setString(3, anuncio.getCategoria());
            p.setString(4, anuncio.getModalidade());
            p.setString(5, anuncio.getNomeAutor());
            p.setString(6, anuncio.getTurma());
            p.setString(7, anuncio.getFotoUrl());

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
}
