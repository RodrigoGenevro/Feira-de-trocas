package br.com.unimart.model;

/**
 * Representa um usuário autenticado da comunidade Trocaê.
 */
public class Usuario extends EntidadeBase {

    private String nome;
    private String email;
    private String turma;
    private String senhaHash;
    private String salt;

    public Usuario() {
        super(null, "ATIVO", null);
    }

    public Usuario(Integer id, String nome, String email, String turma,
                   String senhaHash, String salt, String status, String criadoEm) {
        super(id, status != null ? status : "ATIVO", criadoEm);
        setNome(nome);
        setEmail(email);
        setTurma(turma);
        setSenhaHash(senhaHash);
        setSalt(salt);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome != null ? nome.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : "";
    }

    public String getTurma() {
        return turma;
    }

    public void setTurma(String turma) {
        this.turma = turma != null ? turma.trim() : "";
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public void validar() {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (senhaHash == null || senhaHash.isBlank() || salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("Credenciais de senha inválidas.");
        }
    }
}