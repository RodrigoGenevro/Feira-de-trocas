package br.com.unimart.model;

/**
 * Superclasse abstrata que centraliza atributos comuns de persistência
 * e encapsulamento básico para todas as entidades do domínio.
 */
public abstract class EntidadeBase {

    private Integer id;
    private String status;
    private String criadoEm;

    protected EntidadeBase() {
    }

    protected EntidadeBase(Integer id, String status, String criadoEm) {
        this.id = id;
        this.status = status;
        this.criadoEm = criadoEm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    /**
     * Contrato polimórfico para validação de regras de domínio de cada entidade.
     */
    public abstract void validar();
}
