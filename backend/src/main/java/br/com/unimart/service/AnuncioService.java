package br.com.unimart.service;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.repository.AnuncioRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Camada de regras de negócio para gerenciamento de anúncios.
 */
public class AnuncioService {

    private final AnuncioRepository repository;

    public AnuncioService() {
        this.repository = new AnuncioRepository();
    }

    public AnuncioService(AnuncioRepository repository) {
        this.repository = repository;
    }

    public List<Anuncio> listarAnuncios(String termoBusca, String categoriaSelecionada, String modalidadeSelecionada) throws SQLException {
        String[] filtros = normalizarFiltros(categoriaSelecionada, modalidadeSelecionada);
        return repository.buscarDisponiveis(termoBusca, filtros[0], filtros[1]);
    }

    public PaginaAnuncios listarPaginaAnuncios(
            String termoBusca, String categoriaSelecionada, String modalidadeSelecionada,
            String ordenacao, int pagina, int tamanhoPagina
    ) throws SQLException {
        if (pagina < 0 || tamanhoPagina < 1 || tamanhoPagina > 100) {
            throw new IllegalArgumentException("Os parâmetros de paginação são inválidos.");
        }
        String[] filtros = normalizarFiltros(categoriaSelecionada, modalidadeSelecionada);
        long deslocamento = (long) pagina * tamanhoPagina;
        if (deslocamento > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("O número da página excede o limite permitido.");
        }
        return repository.buscarPaginaDisponiveis(
                termoBusca, filtros[0], filtros[1], ordenacao, tamanhoPagina, (int) deslocamento
        );
    }

    private String[] normalizarFiltros(String categoriaSelecionada, String modalidadeSelecionada) {
        String cat = (categoriaSelecionada == null || categoriaSelecionada.equals("Todas as categorias"))
                ? "" : categoriaSelecionada;

        String mod = "";
        if ("Troca".equals(modalidadeSelecionada)) {
            mod = "TROCA";
        } else if ("Doação".equals(modalidadeSelecionada)) {
            mod = "DOACAO";
        }
        return new String[]{cat, mod};
    }

    public Anuncio publicarAnuncio(Anuncio anuncio) throws SQLException {
        anuncio.validar();
        repository.salvar(anuncio);
        return anuncio;
    }

    public void atualizarAnuncio(Anuncio anuncio, int usuarioId, String nomeUsuario) throws SQLException {
        validarAnuncioEIdentidade(anuncio.getId(), usuarioId, nomeUsuario);
        anuncio.validar();
        repository.atualizarDisponivelDoUsuario(anuncio, usuarioId, nomeUsuario);
    }

    public void concluirAnuncio(int id, int usuarioId, String nomeUsuario) throws SQLException {
        validarAnuncioEIdentidade(id, usuarioId, nomeUsuario);
        repository.concluirAnuncio(id, usuarioId, nomeUsuario);
    }

    public void reabrirAnuncio(int id, int usuarioId, String nomeUsuario) throws SQLException {
        validarAnuncioEIdentidade(id, usuarioId, nomeUsuario);
        repository.reabrirAnuncio(id, usuarioId, nomeUsuario);
    }

    public void aceitarInteresse(int interesseId, int usuarioId, String nomeUsuario) throws SQLException {
        if (interesseId <= 0) {
            throw new IllegalArgumentException("ID de interesse inválido.");
        }
        validarIdentidade(usuarioId, nomeUsuario);
        repository.aceitarInteresse(interesseId, usuarioId, nomeUsuario);
    }

    public List<Anuncio> listarHistoricoUsuario(int usuarioId, String nomeUsuario) throws SQLException {
        return repository.buscarPorUsuario(usuarioId, nomeUsuario);
    }

    private void validarAnuncioEIdentidade(int id, int usuarioId, String nomeUsuario) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de anúncio inválido.");
        }
        validarIdentidade(usuarioId, nomeUsuario);
    }

    private void validarIdentidade(int usuarioId, String nomeUsuario) {
        if (usuarioId <= 0 || nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("É necessário estar conectado como anunciante.");
        }
    }
}
