package br.com.unimart.service;

import java.sql.SQLException;

import br.com.unimart.model.Denuncia;
import br.com.unimart.repository.DenunciaRepository;

public class DenunciaService {

    private final DenunciaRepository repository;

    public DenunciaService() {
        this.repository = new DenunciaRepository();
    }

    public DenunciaService(DenunciaRepository repository) {
        this.repository = repository;
    }

    public void registrar(Denuncia denuncia) throws SQLException {
        denuncia.validar();
        repository.salvar(denuncia);
    }
}
