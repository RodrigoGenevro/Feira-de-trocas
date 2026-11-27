package br.com.unimart.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.unimart.model.Denuncia;

class DenunciaRepositoryTest {

    @TempDir
    Path tempDirectory;

    private DenunciaRepository repository;

    @BeforeEach
    void prepararBanco() throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("reports.db");
        repository = new DenunciaRepository(jdbcUrl);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE anuncios (
                        id INTEGER PRIMARY KEY,
                        nome TEXT NOT NULL,
                        usuario_id INTEGER
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE denuncias (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        anuncio_id INTEGER NOT NULL,
                        usuario_id INTEGER NOT NULL,
                        motivo TEXT NOT NULL,
                        detalhes TEXT
                    )
                    """);
            statement.executeUpdate(
                    "INSERT INTO anuncios(id, nome, usuario_id) VALUES "
                            + "(1, 'Ana', 10), (2, 'Ana legado', NULL)"
            );
        }
    }

    @Test
    void bloqueiaDenunciaDoProprioAnuncioInclusiveAnuncioLegado() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.salvar(new Denuncia(1, 10, "Ana", "Inadequado", ""))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.salvar(new Denuncia(2, 10, "Ana legado", "Inadequado", ""))
        );
    }

    @Test
    void permiteDenunciarAnuncioDeOutraPessoa() throws SQLException {
        repository.salvar(new Denuncia(1, 20, "Bia", "Inadequado", "Detalhes"));
    }
}
