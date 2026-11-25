package br.com.unimart.model;

/**
 * Representa uma manifestação de interesse em um anúncio existente.
 * Herda de EntidadeBase e encapsula dados do interessado.
 */
public class Interesse extends EntidadeBase {

    private int anuncioId;
    private String nomeInteressado;
    private String mensagem;
    private String tituloAnuncio;

    public Interesse() {
        super(null, "PENDENTE", null);
    }

    public Interesse(int anuncioId, String nomeInteressado, String mensagem) {
        super(null, "PENDENTE", null);
        setAnuncioId(anuncioId);
        setNomeInteressado(nomeInteressado);
        setMensagem(mensagem);
    }

    public int getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(int anuncioId) {
        this.anuncioId = anuncioId;
    }

    public String getNomeInteressado() {
        return nomeInteressado;
    }

    public void setNomeInteressado(String nomeInteressado) {
        this.nomeInteressado = nomeInteressado != null ? nomeInteressado.trim() : "";
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem != null ? mensagem.trim() : "";
    }

    public String getTituloAnuncio() {
        return tituloAnuncio;
    }

    public void setTituloAnuncio(String tituloAnuncio) {
        this.tituloAnuncio = tituloAnuncio;
    }

    @Override
    public void validar() {
        if (anuncioId <= 0) {
            throw new IllegalArgumentException("Anúncio inválido selecionado.");
        }
        if (nomeInteressado == null || nomeInteressado.isBlank()) {
            throw new IllegalArgumentException("Seu nome é obrigatório.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem de interesse é obrigatória.");
        }
    }
}
