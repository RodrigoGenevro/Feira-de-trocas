package br.com.unimart.model;

import java.util.List;

public record PaginaAnuncios(List<Anuncio> anuncios, int total) {

    public PaginaAnuncios {
        anuncios = List.copyOf(anuncios);
    }
}
