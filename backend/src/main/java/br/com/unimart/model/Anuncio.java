package br.com.unimart.model;

import java.net.URI;

/**
 * Representa um item anunciado para troca ou doação.
 * Herda de EntidadeBase e aplica encapsulamento com validações nos setters.
 */
public class Anuncio extends EntidadeBase {

    public static final int MAX_TITULO_LENGTH = 100;
    public static final int MAX_DESCRICAO_LENGTH = 1000;
    public static final int MAX_FOTO_URL_LENGTH = 2048;
    public static final String STATUS_DISPONIVEL = "DISPONIVEL";
    public static final String STATUS_RESERVADO = "RESERVADO";
    public static final String STATUS_CONCLUIDO = "CONCLUIDO";

    private String titulo;
    private String descricao;
    private String categoria;
    private String modalidade;
    private String nomeAutor;
    private String turma;
    private String fotoUrl;
    private Integer usuarioId;

    public Anuncio() {
        super(null, STATUS_DISPONIVEL, null);
    }

    public Anuncio(Integer id, String titulo, String descricao, String categoria,
                   String modalidade, String nomeAutor, String turma, String fotoUrl,
                   String status, String criadoEm) {
        super(id, status != null ? status : STATUS_DISPONIVEL, criadoEm);
        setTitulo(titulo);
        setDescricao(descricao);
        setCategoria(categoria);
        setModalidade(modalidade);
        setNomeAutor(nomeAutor);
        setTurma(turma);
        setFotoUrl(fotoUrl);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo != null ? titulo.trim() : "";
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao != null ? descricao.trim() : "";
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria != null ? categoria.trim() : "Outros";
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        if (modalidade == null || (!modalidade.equals("TROCA") && !modalidade.equals("DOACAO"))) {
            this.modalidade = "TROCA";
        } else {
            this.modalidade = modalidade;
        }
    }

    public String getNomeAutor() {
        return nomeAutor;
    }

    public void setNomeAutor(String nomeAutor) {
        this.nomeAutor = nomeAutor != null ? nomeAutor.trim() : "";
    }

    public String getTurma() {
        return turma;
    }

    public void setTurma(String turma) {
        this.turma = turma != null ? turma.trim() : "";
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl != null ? fotoUrl.trim() : "";
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getModalidadeFormatada() {
        return "DOACAO".equals(this.modalidade) ? "DOAÇÃO" : "TROCA";
    }

    public String getAutorCompleto() {
        if (turma != null && !turma.isBlank()) {
            return "Por " + nomeAutor + " · " + turma;
        }
        return "Por " + nomeAutor;
    }

    @Override
    public void validar() {
        if (nomeAutor == null || nomeAutor.isBlank()) {
            throw new IllegalArgumentException("O nome do anunciante é obrigatório.");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do item é obrigatório.");
        }
        if (titulo.length() > MAX_TITULO_LENGTH) {
            throw new IllegalArgumentException("O título deve ter no máximo "
                    + MAX_TITULO_LENGTH + " caracteres.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do item é obrigatória.");
        }
        if (descricao.length() > MAX_DESCRICAO_LENGTH) {
            throw new IllegalArgumentException("A descrição deve ter no máximo "
                    + MAX_DESCRICAO_LENGTH + " caracteres.");
        }
        validarUrlFoto(fotoUrl);
    }

    public static void validarUrlFoto(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return;
        }
        if (fotoUrl.length() > MAX_FOTO_URL_LENGTH) {
            throw new IllegalArgumentException("O link da foto deve ter no máximo "
                    + MAX_FOTO_URL_LENGTH + " caracteres.");
        }

        try {
            URI uri = URI.create(fotoUrl);
            String esquema = uri.getScheme();
            if (uri.getHost() == null
                    || uri.getUserInfo() != null
                    || (!"http".equalsIgnoreCase(esquema) && !"https".equalsIgnoreCase(esquema))) {
                throw new IllegalArgumentException("Informe um link de foto válido (HTTP ou HTTPS).");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Informe um link de foto válido (HTTP ou HTTPS).", e);
        }
    }
}
