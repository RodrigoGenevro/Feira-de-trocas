package br.com.unimart.controller;

import java.sql.SQLException;
import java.util.List;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Denuncia;
import br.com.unimart.model.Interesse;
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.model.Usuario;
import br.com.unimart.api.FeiraApiClient;
import br.com.unimart.service.AnuncioService;
import br.com.unimart.service.AuthService;
import br.com.unimart.service.DenunciaService;
import br.com.unimart.service.InteresseService;

/**
 * Controlador responsável por mediar as ações da interface gráfica (View)
 * com as regras de negócio (Services) e manter o estado da sessão.
 */
public class FeiraController {

    private final AnuncioService anuncioService;
    private final InteresseService interesseService;
    private final AuthService authService;
    private final DenunciaService denunciaService;
    private final FeiraApiClient apiClient;

    private Usuario usuarioLogado;

    public FeiraController() {
        this.anuncioService = new AnuncioService();
        this.interesseService = new InteresseService();
        this.authService = new AuthService();
        this.denunciaService = new DenunciaService();
        this.apiClient = null;
    }

    public FeiraController(FeiraApiClient apiClient) {
        this.anuncioService = null;
        this.interesseService = null;
        this.authService = null;
        this.denunciaService = new DenunciaService();
        this.apiClient = apiClient;
    }

    public Usuario login(String email, String senha) throws SQLException {
        this.usuarioLogado = apiClient == null
                ? authService.autenticar(email, senha)
                : apiClient.login(email, senha);
        return this.usuarioLogado;
    }

    public Usuario cadastrar(String nome, String email, String turma, String senha, String confirmarSenha) throws SQLException {
        this.usuarioLogado = apiClient == null
                ? authService.cadastrar(nome, email, turma, senha, confirmarSenha)
                : apiClient.cadastrar(nome, email, turma, senha, confirmarSenha);
        return this.usuarioLogado;
    }

    public Usuario atualizarPerfil(String nome, String email, String turma, String novaSenha, String confirmarSenha) throws SQLException {
        this.usuarioLogado = apiClient == null
                ? authService.atualizarPerfil(this.usuarioLogado, nome, email, turma, novaSenha, confirmarSenha)
                : apiClient.atualizarPerfil(
                        this.usuarioLogado, nome, email, turma, novaSenha, confirmarSenha
                );
        return this.usuarioLogado;
    }

    public void logout() {
        if (apiClient != null) {
            apiClient.logout();
        }
        this.usuarioLogado = null;
    }

    public boolean isLogado() {
        return this.usuarioLogado != null;
    }

    public boolean anuncioPertenceAoUsuarioLogado(Anuncio anuncio) {
        if (!isLogado() || anuncio == null) {
            return false;
        }
        if (anuncio.getUsuarioId() != null) {
            return usuarioLogado.getId().equals(anuncio.getUsuarioId());
        }
        return anuncio.getNomeAutor() != null
                && usuarioLogado.getNome().equalsIgnoreCase(anuncio.getNomeAutor().trim());
    }

    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }

    public List<Anuncio> buscarAnuncios(String termoBusca, String categoria, String modalidade) throws SQLException {
        if (apiClient != null) {
            return apiClient.buscarPaginaAnuncios(
                    termoBusca, categoria, modalidade, "RECENTES", 0, 100
            ).anuncios();
        }
        return anuncioService.listarAnuncios(termoBusca, categoria, modalidade);
    }

    public PaginaAnuncios buscarPaginaAnuncios(
            String termoBusca, String categoria, String modalidade,
            String ordenacao, int pagina, int tamanhoPagina
    ) throws SQLException {
        if (apiClient != null) {
            return apiClient.buscarPaginaAnuncios(
                    termoBusca, categoria, modalidade, ordenacao, pagina, tamanhoPagina
            );
        }
        return anuncioService.listarPaginaAnuncios(
                termoBusca, categoria, modalidade, ordenacao, pagina, tamanhoPagina
        );
    }

    public Anuncio criarAnuncio(String nomeAutor, String turma, String titulo, String descricao,
                                String categoria, String modalidade, String fotoUrl) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            return apiClient.criarAnuncio(
                    titulo, descricao, categoria, modalidade, fotoUrl
            );
        }
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
        if (isLogado()) {
            novo.setUsuarioId(usuarioLogado.getId());
        }

        return anuncioService.publicarAnuncio(novo);
    }

    public void atualizarAnuncio(int anuncioId, String titulo, String descricao,
                                 String categoria, String modalidade, String fotoUrl) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            apiClient.atualizarAnuncio(anuncioId, titulo, descricao, categoria, modalidade, fotoUrl);
            return;
        }
        Usuario usuario = usuarioAutenticado();
        Anuncio anuncio = new Anuncio();
        anuncio.setId(anuncioId);
        anuncio.setNomeAutor(usuario.getNome());
        anuncio.setTurma(usuario.getTurma());
        anuncio.setTitulo(titulo);
        anuncio.setDescricao(descricao);
        anuncio.setCategoria(categoria);
        anuncio.setModalidade(modalidade);
        anuncio.setFotoUrl(fotoUrl);
        anuncio.setUsuarioId(usuario.getId());

        anuncioService.atualizarAnuncio(anuncio, usuario.getId(), usuario.getNome());
    }

    public void enviarInteresse(int anuncioId, String mensagem) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            apiClient.enviarInteresse(anuncioId, mensagem);
            return;
        }
        Usuario usuario = usuarioAutenticado();
        Interesse interesse = new Interesse(anuncioId, usuario.getNome(), mensagem);
        interesseService.registrarInteresse(interesse, usuario.getId(), usuario.getNome());
    }

    public void cancelarInteresse(int interesseId) throws SQLException {
        Usuario usuario = usuarioAutenticado();
        interesseService.cancelarInteresse(interesseId, usuario.getId());
    }

    public List<Interesse> buscarInteressesDoUsuario() throws SQLException {
        Usuario usuario = usuarioAutenticado();
        return interesseService.listarInteressesDoUsuario(usuario.getId());
    }

    public void denunciarAnuncio(int anuncioId, String motivo, String detalhes) throws SQLException {
        Usuario usuario = usuarioAutenticado();
        Denuncia denuncia = new Denuncia(
                anuncioId, usuario.getId(), usuario.getNome(), motivo, detalhes
        );
        denunciaService.registrar(denuncia);
    }

    public List<Interesse> buscarInteressesDoAnuncio(int anuncioId) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            return apiClient.buscarInteresses(anuncioId);
        }
        Usuario usuario = usuarioAutenticado();
        return interesseService.listarInteressesDoAnuncio(
                anuncioId, usuario.getId(), usuario.getNome()
        );
    }

    public void aceitarInteresse(int interesseId) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            apiClient.aceitarInteresse(interesseId);
            return;
        }
        Usuario usuario = usuarioAutenticado();
        anuncioService.aceitarInteresse(interesseId, usuario.getId(), usuario.getNome());
    }

    public void concluirAnuncio(int anuncioId) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            apiClient.concluirAnuncio(anuncioId);
            return;
        }
        Usuario usuario = usuarioAutenticado();
        anuncioService.concluirAnuncio(anuncioId, usuario.getId(), usuario.getNome());
    }

    public void reabrirAnuncio(int anuncioId) throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            apiClient.reabrirAnuncio(anuncioId);
            return;
        }
        Usuario usuario = usuarioAutenticado();
        anuncioService.reabrirAnuncio(anuncioId, usuario.getId(), usuario.getNome());
    }

    public void reativarAnuncio(int anuncioId) throws SQLException {
        Usuario usuario = usuarioAutenticado();
        anuncioService.reativarAnuncio(anuncioId, usuario.getId(), usuario.getNome());
    }

    public List<Anuncio> buscarHistoricoUsuario() throws SQLException {
        if (apiClient != null) {
            usuarioAutenticado();
            return apiClient.buscarHistoricoUsuario();
        }
        if (!isLogado()) {
            throw new IllegalStateException("É necessário estar conectado para ver o histórico.");
        }
        return anuncioService.listarHistoricoUsuario(
                usuarioLogado.getId(),
                usuarioLogado.getNome()
        );
    }

    public List<Usuario> buscarUsuariosCadastrados() throws SQLException {
        if (apiClient != null) {
            return List.of();
        }
        return authService.listarUsuariosCadastrados();
    }

    private Usuario usuarioAutenticado() {
        if (!isLogado()) {
            throw new IllegalStateException("É necessário estar conectado.");
        }
        return usuarioLogado;
    }
}