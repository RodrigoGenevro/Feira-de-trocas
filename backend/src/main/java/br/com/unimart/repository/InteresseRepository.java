package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.unimart.model.Interesse;
import br.com.unimart.util.DatabaseConfig;

/**
 * Camada de acesso a dados (DAO/Repository) responsável pelas operações SQL de Interesse.
 */
public class InteresseRepository {

    private final String jdbcUrl;

    public InteresseRepository() {
        this(DatabaseConfig.getActiveDbUrl());
    }

    InteresseRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void salvar(Interesse interesse) throws SQLException {
        String sql = "INSERT INTO interesses(anuncio_id, nome, mensagem) " +
                     "SELECT ?, ?, ? WHERE EXISTS (" +
                     "SELECT 1 FROM anuncios WHERE id = ? AND status = 'DISPONIVEL')";
        try (Connection c = abrirConexao();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, interesse.getAnuncioId());
            p.setString(2, interesse.getNomeInteressado());
            p.setString(3, interesse.getMensagem());
            p.setInt(4, interesse.getAnuncioId());
            if (p.executeUpdate() != 1) {
                throw new IllegalArgumentException("Este anúncio não está mais disponível para interesse.");
            }
        }
    }

    public List<Interesse> listarPorAnuncio(int anuncioId, int usuarioId, String nomeUsuario) throws SQLException {
        List<Interesse> lista = new ArrayList<>();
        String sql = "SELECT i.id, i.anuncio_id, i.nome, i.mensagem, i.status, i.criado_em, a.titulo FROM interesses i " +
                     "JOIN anuncios a ON a.id = i.anuncio_id " +
                     "WHERE a.id = ? AND (a.usuario_id = ? OR " +
                     "(a.usuario_id IS NULL AND lower(a.nome) = lower(?))) " +
                     "ORDER BY i.id DESC";

        try (Connection c = abrirConexao();
             PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setInt(1, anuncioId);
            statement.setInt(2, usuarioId);
            statement.setString(3, nomeUsuario.trim());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Interesse interesse = new Interesse();
                    interesse.setId(result.getInt("id"));
                    interesse.setAnuncioId(result.getInt("anuncio_id"));
                    interesse.setNomeInteressado(result.getString("nome"));
                    interesse.setMensagem(result.getString("mensagem"));
                    interesse.setStatus(result.getString("status"));
                    interesse.setCriadoEm(result.getString("criado_em"));
                    interesse.setTituloAnuncio(result.getString("titulo"));
                    lista.add(interesse);
                }
            }
        }
        return lista;
    }

    private Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
