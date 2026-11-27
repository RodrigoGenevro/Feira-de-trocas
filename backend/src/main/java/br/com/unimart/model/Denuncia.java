package br.com.unimart.model;

public class Denuncia extends EntidadeBase {

    private int anuncioId;
    private int usuarioId;
    private String nomeUsuario;
    private String motivo;
    private String detalhes;

    public Denuncia() {
        super(null, null, null);
    }

    public Denuncia(int anuncioId, int usuarioId, String nomeUsuario, String motivo, String detalhes) {
        super(null, null, null);
        this.anuncioId = anuncioId;
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario != null ? nomeUsuario.trim() : "";
        this.motivo = motivo != null ? motivo.trim() : "";
        this.detalhes = detalhes != null ? detalhes.trim() : "";
    }

    public int getAnuncioId() {
        return anuncioId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDetalhes() {
        return detalhes;
    }

    @Override
    public void validar() {
        if (anuncioId <= 0) {
            throw new IllegalArgumentException("Anúncio inválido.");
        }
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("É necessário estar conectado para denunciar.");
        }
        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("É necessário estar conectado para denunciar.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Selecione um motivo para a denúncia.");
        }
        if (detalhes != null && detalhes.length() > 500) {
            throw new IllegalArgumentException("Os detalhes devem ter no máximo 500 caracteres.");
        }
    }
}
