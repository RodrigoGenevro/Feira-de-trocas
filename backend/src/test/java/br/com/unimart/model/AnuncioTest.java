package br.com.unimart.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AnuncioTest {

    @Test
    void validarPermiteFotoOpcionalEExigeLinkHttpOuHttpsQuandoInformado() {
        Anuncio anuncio = criarAnuncio("");

        assertDoesNotThrow(anuncio::validar);
        assertDoesNotThrow(() -> criarAnuncio(null).validar());
        assertDoesNotThrow(() -> criarAnuncio("https://example.com/item.jpg").validar());
    }

    @Test
    void validarRejeitaEsquemasDeFotoQueNaoSaoHttp() {
        Anuncio anuncio = criarAnuncio("file:///tmp/item.jpg");

        assertThrows(IllegalArgumentException.class, anuncio::validar);
    }

    @Test
    void validarRejeitaCamposAcimaDosLimitesEUrlComCredenciais() {
        Anuncio anuncioComTituloLongo = criarAnuncio("");
        anuncioComTituloLongo.setTitulo("x".repeat(Anuncio.MAX_TITULO_LENGTH + 1));
        assertThrows(IllegalArgumentException.class, anuncioComTituloLongo::validar);

        Anuncio anuncioComDescricaoLonga = criarAnuncio("");
        anuncioComDescricaoLonga.setDescricao("x".repeat(Anuncio.MAX_DESCRICAO_LENGTH + 1));
        assertThrows(IllegalArgumentException.class, anuncioComDescricaoLonga::validar);

        assertThrows(
                IllegalArgumentException.class,
                () -> Anuncio.validarUrlFoto("https://user:pass@example.com/item.jpg")
        );
    }

    private Anuncio criarAnuncio(String fotoUrl) {
        Anuncio anuncio = new Anuncio();
        anuncio.setNomeAutor("Pessoa anunciante");
        anuncio.setTitulo("Livro de exemplo");
        anuncio.setDescricao("Livro em bom estado.");
        anuncio.setFotoUrl(fotoUrl);
        return anuncio;
    }
}
