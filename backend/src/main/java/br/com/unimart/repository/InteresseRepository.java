package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.unimart.model.Interesse;
import br.com.unimart.util.DatabaseConfig;

/**
 * Camada de acesso a dados (DAO/Repository) responsável pelas operações SQL de Interesse.
 */
public class InteresseRepository {

    public void salvar(Interesse interesse) throws SQLException {
        String sql = "INSERT INTO interesses(anuncio_id, nome, mensagem) VALUES(?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            p.setInt(1, interesse.getAnuncioId());
            p.setString(2, interesse.getNomeInteressado());
            p.setString(3, interesse.getMensagem());
            p.executeUpdate();

            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    interesse.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<Interesse> listarTodos() throws SQLException {
        List<Interesse> lista = new ArrayList<>();
        String sql = "SELECT i.id, i.anuncio_id, i.nome, i.mensagem, i.status, i.criado_em, a.titulo FROM interesses i " +
                     "JOIN anuncios a ON a.id = i.anuncio_id ORDER BY i.id DESC";

        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                Interesse i = new Interesse();
                i.setId(r.getInt("id"));
                i.setAnuncioId(r.getInt("anuncio_id"));
                i.setNomeInteressado(r.getString("nome"));
                i.setMensagem(r.getString("mensagem"));
                i.setStatus(r.getString("status"));
                i.setCriadoEm(r.getString("criado_em"));
                i.setTituloAnuncio(r.getString("titulo"));
                lista.add(i);
            }
        }
        return lista;
    }
}
