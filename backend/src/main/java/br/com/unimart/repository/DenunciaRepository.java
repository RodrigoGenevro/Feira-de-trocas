package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.com.unimart.model.Denuncia;
import br.com.unimart.util.DatabaseConfig;

public class DenunciaRepository {

    private final String jdbcUrl;

    public DenunciaRepository() {
        this(DatabaseConfig.getActiveDbUrl());
    }

    public DenunciaRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void salvar(Denuncia denuncia) throws SQLException {
        String sql = "INSERT INTO denuncias(anuncio_id, usuario_id, motivo, detalhes) "
                + "SELECT ?, ?, ?, ? WHERE EXISTS (SELECT 1 FROM anuncios a WHERE a.id = ? "
                + "AND NOT ((a.usuario_id IS NOT NULL AND a.usuario_id = ?) "
                + "OR (a.usuario_id IS NULL AND lower(a.nome) = lower(?)))) "
                + "AND NOT EXISTS (SELECT 1 FROM denuncias WHERE anuncio_id = ? AND usuario_id = ?)";
        try (Connection connection = DatabaseConfig.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, denuncia.getAnuncioId());
            statement.setInt(2, denuncia.getUsuarioId());
            statement.setString(3, denuncia.getMotivo());
            statement.setString(4, denuncia.getDetalhes());
            statement.setInt(5, denuncia.getAnuncioId());
            statement.setInt(6, denuncia.getUsuarioId());
            statement.setString(7, denuncia.getNomeUsuario());
            statement.setInt(8, denuncia.getAnuncioId());
            statement.setInt(9, denuncia.getUsuarioId());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "O anúncio não existe, pertence a você ou já foi denunciado por você."
                );
            }
        }
    }
}
