package br.com.unimart.service;

import java.sql.SQLException;
import java.util.List;

import br.com.unimart.model.Usuario;
import br.com.unimart.repository.UsuarioRepository;
import br.com.unimart.util.PasswordUtil;

public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrar(String nome, String email, String turma, String senha, String confirmarSenha) throws SQLException {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        String salt = PasswordUtil.gerarSalt();
        String hash = PasswordUtil.gerarHash(senha, salt);

        Usuario novo = new Usuario();
        novo.setNome(nome);
        novo.setEmail(email);
        novo.setTurma(turma);
        novo.setSalt(salt);
        novo.setSenhaHash(hash);
        novo.validar();

        if (usuarioRepository.existePorEmail(novo.getEmail())) {
            throw new IllegalArgumentException("Já existe uma conta cadastrada com este e-mail.");
        }

        return usuarioRepository.salvar(novo);
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Preencha e-mail e senha.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario == null || !PasswordUtil.verificarSenha(senha, usuario.getSalt(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }

        return usuario;
    }

    public Usuario atualizarPerfil(Usuario usuarioAtual, String novoNome, String novoEmail,
                                   String novaTurma, String novaSenha, String confirmarNovaSenha) throws SQLException {
        if (usuarioAtual == null || usuarioAtual.getId() == null) {
            throw new IllegalStateException("Nenhum usuário autenticado.");
        }

        Usuario atualizado = new Usuario(
                usuarioAtual.getId(),
                novoNome,
                novoEmail,
                novaTurma,
                usuarioAtual.getSenhaHash(),
                usuarioAtual.getSalt(),
                usuarioAtual.getStatus(),
                usuarioAtual.getCriadoEm()
        );

        if (novaSenha != null && !novaSenha.isBlank()) {
            if (novaSenha.length() < 6) {
                throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
            }
            if (!novaSenha.equals(confirmarNovaSenha)) {
                throw new IllegalArgumentException("A confirmação da nova senha não coincide.");
            }
            String novoSalt = PasswordUtil.gerarSalt();
            String novoHash = PasswordUtil.gerarHash(novaSenha, novoSalt);
            atualizado.setSalt(novoSalt);
            atualizado.setSenhaHash(novoHash);
        }

        atualizado.validar();

        if (usuarioRepository.existePorEmailEmOutroUsuario(atualizado.getEmail(), atualizado.getId())) {
            throw new IllegalArgumentException("Este e-mail já está sendo usado por outra conta.");
        }

        return usuarioRepository.atualizar(atualizado);
    }

    public List<Usuario> listarUsuariosCadastrados() throws SQLException {
        return usuarioRepository.listarUsuariosAtivos();
    }
}