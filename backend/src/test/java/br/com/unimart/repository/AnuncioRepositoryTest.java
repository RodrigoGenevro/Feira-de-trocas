package br.com.unimart.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.unimart.model.Interesse;

class AnuncioRepositoryTest {

    @TempDir
    Path tempDirectory;

    private String jdbcUrl;
    private AnuncioRepository anuncioRepository;
    private InteresseRepository interesseRepository;

    @BeforeEach
    void prepararBanco() throws SQLException {
        jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("test.db");
        anuncioRepository = new AnuncioRepository(jdbcUrl);
        interesseRepository = new InteresseRepository(jdbcUrl);

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE anuncios (
                        id INTEGER PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        usuario_id INTEGER,
                        status TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE interesses (
                        id INTEGER PRIMARY KEY,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDENTE',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    @Test
    void somenteOAnuncianteConsultaInteressesETransicionaAnuncio() throws SQLException {
        inserirAnuncio(1, "Livro", 10, "Ana", "DISPONIVEL");
        inserirInteresse(101, 1, "Bia", "Tenho interesse.", "PENDENTE");
        inserirInteresse(102, 1, "Caio", "Ainda está disponível?", "PENDENTE");

        assertEquals(2, interesseRepository.listarPorAnuncio(1, 10, "Ana").size());
        assertTrue(interesseRepository.listarPorAnuncio(1, 20, "Outra pessoa").isEmpty());

        assertThrows(
                IllegalStateException.class,
                () -> anuncioRepository.aceitarInteresse(101, 20, "Outra pessoa")
        );
        anuncioRepository.aceitarInteresse(101, 10, "Ana");

        assertEquals("RESERVADO", buscarStatusAnuncio(1));
        assertEquals("ACEITO", buscarStatusInteresse(101));
        assertEquals("ENCERRADO", buscarStatusInteresse(102));

        anuncioRepository.concluirAnuncio(1, 10, "Ana");
        assertEquals("CONCLUIDO", buscarStatusAnuncio(1));
        assertEquals("ENCERRADO", buscarStatusInteresse(102));
    }

    @Test
    void reabrirAnuncioReservadoCancelaInteresseAceito() throws SQLException {
        inserirAnuncio(1, "Calculadora", 10, "Ana", "DISPONIVEL");
        inserirInteresse(101, 1, "Bia", "Tenho interesse.", "PENDENTE");
        inserirInteresse(102, 1, "Caio", "Também tenho interesse.", "PENDENTE");

        anuncioRepository.aceitarInteresse(101, 10, "Ana");
        assertEquals("ENCERRADO", buscarStatusInteresse(102));
        anuncioRepository.reabrirAnuncio(1, 10, "Ana");

        assertEquals("DISPONIVEL", buscarStatusAnuncio(1));
        assertEquals("CANCELADO", buscarStatusInteresse(101));
        assertEquals("PENDENTE", buscarStatusInteresse(102));
    }

    @Test
    void interesseNaoPodeSerAceitoParaAnuncioQueJaFoiReservado() throws SQLException {
        inserirAnuncio(1, "Caderno", 10, "Ana", "RESERVADO");
        inserirInteresse(101, 1, "Bia", "Tenho interesse.", "PENDENTE");

        assertThrows(
                IllegalStateException.class,
                () -> anuncioRepository.aceitarInteresse(101, 10, "Ana")
        );
        assertEquals("RESERVADO", buscarStatusAnuncio(1));
        assertEquals("PENDENTE", buscarStatusInteresse(101));
    }

    @Test
    void registrarInteresseSoAceitaAnuncioDisponivel() throws SQLException {
        inserirAnuncio(1, "Livro", 10, "Ana", "DISPONIVEL");
        inserirAnuncio(2, "Caderno", 10, "Ana", "RESERVADO");

        assertThrows(
                IllegalArgumentException.class,
                () -> interesseRepository.salvar(new Interesse(1, "Ana", "Tenho interesse."), 10, "Ana")
        );
        interesseRepository.salvar(new Interesse(1, "Bia", "Tenho interesse."), 20, "Bia");
        assertEquals(1, contarInteresses(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> interesseRepository.salvar(new Interesse(2, "Caio", "Tenho interesse."), 20, "Caio")
        );
        assertEquals(0, contarInteresses(2));
    }

    @Test
    void anuncioSemUsuarioVinculadoBloqueiaInteresseDoMesmoNome() throws SQLException {
        inserirAnuncio(1, "Livro", null, "Ana", "DISPONIVEL");

        assertThrows(
                IllegalArgumentException.class,
                () -> interesseRepository.salvar(new Interesse(1, "Ana", "Tenho interesse."), 20, "Ana")
        );
        assertEquals(0, contarInteresses(1));
    }

    private void inserirAnuncio(int id, String titulo, Integer usuarioId, String nome, String status)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO anuncios(id, titulo, nome, usuario_id, status) VALUES(?, ?, ?, ?, ?)"
             )) {
            statement.setInt(1, id);
            statement.setString(2, titulo);
            statement.setString(3, nome);
            if (usuarioId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, usuarioId);
            }
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }

    private void inserirInteresse(
            int id, int anuncioId, String nome, String mensagem, String status
    ) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO interesses(id, anuncio_id, nome, mensagem, status) VALUES(?, ?, ?, ?, ?)"
             )) {
            statement.setInt(1, id);
            statement.setInt(2, anuncioId);
            statement.setString(3, nome);
            statement.setString(4, mensagem);
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }

    private String buscarStatusAnuncio(int anuncioId) throws SQLException {
        return buscarStatus("anuncios", anuncioId);
    }

    private String buscarStatusInteresse(int interesseId) throws SQLException {
        return buscarStatus("interesses", interesseId);
    }

    private int contarInteresses(int anuncioId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM interesses WHERE anuncio_id = ?"
             )) {
            statement.setInt(1, anuncioId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt(1);
            }
        }
    }

    private String buscarStatus(String tabela, int id) throws SQLException {
        assertTrue(tabela.equals("anuncios") || tabela.equals("interesses"));
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM " + tabela + " WHERE id = ?"
             )) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getString("status");
            }
        }
    }
}
