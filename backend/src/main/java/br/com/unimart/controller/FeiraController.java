package br.com.unimart.controller;

import java.sql.SQLException;
import java.util.List;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Interesse;
import br.com.unimart.model.Usuario;
import br.com.unimart.service.AnuncioService;
import br.com.unimart.service.AuthService;
import br.com.unimart.service.InteresseService;

/**
 * Controlador responsável por mediar as ações da interface gráfica (View)
 * com as regras de negócio (Services) e manter o estado da sessão.
 */
public class FeiraController {

    private final AnuncioService anuncioService;
    private final InteresseService interesseService;
    private final AuthService authService;

    private Usuario usuarioLogado;

    public FeiraController() {
        this.anuncioService = new AnuncioService();
        this.interesseService = new InteresseService();
        this.authService = new AuthService();
    }

    public Usuario login(String email, String senha) throws SQLException {
        this.usuarioLogado = authService.autenticar(email, senha);
        return this.usuarioLogado;
    }

    public Usuario cadastrar(String nome, String email, String turma, String senha, String confirmarSenha) throws SQLException {
        this.usuarioLogado = authService.cadastrar(nome, email, turma, senha, confirmarSenha);
        return this.usuarioLogado;
    }

    public Usuario atualizarPerfil(String nome, String email, String turma, String novaSenha, String confirmarSenha) throws SQLException {
        this.usuarioLogado = authService.atualizarPerfil(this.usuarioLogado, nome, email, turma, novaSenha, confirmarSenha);
        return this.usuarioLogado;
    }

    public void logout() {
        this.usuarioLogado = null;
    }

    public boolean isLogado() {
        return this.usuarioLogado != null;
    }

    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }

    public List<Anuncio> buscarAnuncios(String termoBusca, String categoria, String modalidade) throws SQLException {
        return anuncioService.listarAnuncios(termoBusca, categoria, modalidade);
    }

    public Anuncio criarAnuncio(String nomeAutor, String turma, String titulo, String descricao,
                                String categoria, String modalidade, String fotoUrl) throws SQLException {
        String autorFinal = isLogado() ? usuarioLogado.getNome() : nomeAutor;
        String turmaFinal = (isLogado() && usuarioLogado.getTurma() != null && !usuarioLogado.getTurma().isBlank())
                ? usuarioLogado.getTurma()
                : turma;

        Anuncio novo = new Anuncio();
        novo.setNomeAutor(autorFinal);
        novo.setTurma(turmaFinal);
        novo.setTitulo(titulo);
        novo.setDescricao(descricao);
        novo.setCategoria(categoria);
        novo.setModalidade(modalidade);
        novo.setFotoUrl(fotoUrl);

        return anuncioService.publicarAnuncio(novo);
    }

    public void enviarInteresse(int anuncioId, String nomeInteressado, String mensagem) throws SQLException {
        String interessadoFinal = isLogado() ? usuarioLogado.getNome() : nomeInteressado;
        Interesse interesse = new Interesse(anuncioId, interessadoFinal, mensagem);
        interesseService.registrarInteresse(interesse);
    }

    public List<Interesse> buscarInteresses() throws SQLException {
        return interesseService.listarInteresses();
    }
}