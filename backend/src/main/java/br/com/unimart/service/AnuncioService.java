package br.com.unimart.service;

import br.com.unimart.model.Anuncio;
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
        String cat = (categoriaSelecionada == null || categoriaSelecionada.equals("Todas as categorias"))
                ? "" : categoriaSelecionada;

        String mod = "";
        if ("Troca".equals(modalidadeSelecionada)) {
            mod = "TROCA";
        } else if ("Doação".equals(modalidadeSelecionada)) {
            mod = "DOACAO";
        }

        return repository.buscarDisponiveis(termoBusca, cat, mod);
    }

    public Anuncio publicarAnuncio(Anuncio anuncio) throws SQLException {
        anuncio.validar();
        repository.salvar(anuncio);
        return anuncio;
    }

    public void concluirAnuncio(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de anúncio inválido.");
        }
        repository.atualizarStatus(id, "CONCLUIDO");
    }
}
