package br.com.unimart.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.unimart.model.Usuario;
import br.com.unimart.util.DatabaseConfig;

public class UsuarioRepository {

    private final String jdbcUrl;

    public UsuarioRepository() {
        this(DatabaseConfig.getActiveDbUrl());
    }

    UsuarioRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Usuario salvar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email, turma, senha_hash, salt, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTurma());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setString(5, usuario.getSalt());
            stmt.setString(6, usuario.getStatus());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getInt(1));
                }
            }
            return usuario;
        }
    }

    public Usuario atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, turma = ?, senha_hash = ?, salt = ? WHERE id = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTurma());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setString(5, usuario.getSalt());
            stmt.setInt(6, usuario.getId());

            stmt.executeUpdate();
            return usuario;
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, nome, email, turma, senha_hash, salt, status, criado_em " +
                     "FROM usuarios WHERE lower(email) = lower(?) AND status = 'ATIVO' LIMIT 1";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    public boolean existePorEmailEmOutroUsuario(String email, Integer idAtual) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT id FROM usuarios WHERE lower(email) = lower(?) AND id <> ? LIMIT 1";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            stmt.setInt(2, idAtual != null ? idAtual : -1);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existePorEmail(String email) throws SQLException {
        return buscarPorEmail(email) != null;
    }

    public List<Usuario> buscarHistoricoContas() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT id, nome, email, turma, senha_hash, salt, status, criado_em FROM usuarios ORDER BY criado_em DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    public List<Usuario> listarUsuariosAtivos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome, email, turma, senha_hash, salt, status, criado_em " +
                     "FROM usuarios WHERE status = 'ATIVO' ORDER BY criado_em DESC, id DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("turma"),
                rs.getString("senha_hash"),
                rs.getString("salt"),
                rs.getString("status"),
                rs.getString("criado_em")
        );
    }

    private Connection abrirConexao() throws SQLException {
        return DatabaseConfig.getConnection(jdbcUrl);
    }
}