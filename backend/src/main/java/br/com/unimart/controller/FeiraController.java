package br.com.unimart.controller;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Interesse;
import br.com.unimart.service.AnuncioService;
import br.com.unimart.service.InteresseService;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador responsável por mediar as ações da interface gráfica (View)
 * com as regras de negócio (Services).
 */
public class FeiraController {

    private final AnuncioService anuncioService;
    private final InteresseService interesseService;

    public FeiraController() {
        this.anuncioService = new AnuncioService();
        this.interesseService = new InteresseService();
    }

    public List<Anuncio> buscarAnuncios(String termoBusca, String categoria, String modalidade) throws SQLException {
        return anuncioService.listarAnuncios(termoBusca, categoria, modalidade);
    }

    public Anuncio criarAnuncio(String nomeAutor, String turma, String titulo, String descricao,
                                String categoria, String modalidade, String fotoUrl) throws SQLException {
        Anuncio novo = new Anuncio();
        novo.setNomeAutor(nomeAutor);
        novo.setTurma(turma);
        novo.setTitulo(titulo);
        novo.setDescricao(descricao);
        novo.setCategoria(categoria);
        novo.setModalidade(modalidade);
        novo.setFotoUrl(fotoUrl);

        return anuncioService.publicarAnuncio(novo);
    }

    public void enviarInteresse(int anuncioId, String nomeInteressado, String mensagem) throws SQLException {
        Interesse interesse = new Interesse(anuncioId, nomeInteressado, mensagem);
        interesseService.registrarInteresse(interesse);
    }

    public List<Interesse> buscarInteresses() throws SQLException {
        return interesseService.listarInteresses();
    }
}
