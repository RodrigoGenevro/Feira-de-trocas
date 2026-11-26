package br.com.unimart.service;

import br.com.unimart.model.Interesse;
import br.com.unimart.repository.InteresseRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Camada de regras de negócio para gerenciamento de interesses em itens.
 */
public class InteresseService {

    private final InteresseRepository repository;

    public InteresseService() {
        this.repository = new InteresseRepository();
    }

    public InteresseService(InteresseRepository repository) {
        this.repository = repository;
    }

    public void registrarInteresse(Interesse interesse) throws SQLException {
        interesse.validar();
        repository.salvar(interesse);
    }

    public List<Interesse> listarInteressesDoAnuncio(
            int anuncioId, int usuarioId, String nomeUsuario
    ) throws SQLException {
        if (anuncioId <= 0 || usuarioId <= 0 || nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("É necessário estar conectado como anunciante.");
        }
        return repository.listarPorAnuncio(anuncioId, usuarioId, nomeUsuario);
    }
}
