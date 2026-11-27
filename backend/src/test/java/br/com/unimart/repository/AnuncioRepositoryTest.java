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

import br.com.unimart.model.Anuncio;
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
                        descricao TEXT NOT NULL DEFAULT '',
                        categoria TEXT NOT NULL DEFAULT '',
                        modalidade TEXT NOT NULL DEFAULT 'TROCA',
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER,
                        status TEXT NOT NULL,
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
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
    void anunciantePodeEditarOsCamposDoProprioAnuncioDisponivel() throws SQLException {
        inserirAnuncio(1, "Livro antigo", 10, "Ana", "DISPONIVEL");
        Anuncio anuncio = new Anuncio();
        anuncio.setId(1);
        anuncio.setTitulo("Livro atualizado");
        anuncio.setDescricao("Em ótimo estado.");
        anuncio.setCategoria("Livros");
        anuncio.setModalidade("DOACAO");
        anuncio.setFotoUrl("https://example.com/livro.jpg");

        anuncioRepository.atualizarDisponivelDoUsuario(anuncio, 10, "Ana");

        assertEquals("Livro atualizado", buscarTituloAnuncio(1));
        assertEquals("Em ótimo estado.", buscarCampoAnuncio(1, "descricao"));
        assertEquals("Livros", buscarCampoAnuncio(1, "categoria"));
        assertEquals("DOACAO", buscarCampoAnuncio(1, "modalidade"));
        assertEquals("https://example.com/livro.jpg", buscarCampoAnuncio(1, "foto"));
    }

    @Test
    void impedeEdicaoDeAnuncioDeOutraPessoaOuQueNaoEstejaDisponivel() throws SQLException {
        inserirAnuncio(1, "Anúncio da Ana", 10, "Ana", "DISPONIVEL");
        inserirAnuncio(2, "Anúncio reservado", 10, "Ana", "RESERVADO");
        Anuncio anuncio = new Anuncio();
        anuncio.setId(1);
        anuncio.setTitulo("Alterado sem autorização");

        assertThrows(
                IllegalStateException.class,
                () -> anuncioRepository.atualizarDisponivelDoUsuario(anuncio, 20, "Bia")
        );

        anuncio.setId(2);
        assertThrows(
                IllegalStateException.class,
                () -> anuncioRepository.atualizarDisponivelDoUsuario(anuncio, 10, "Ana")
        );
        assertEquals("Anúncio da Ana", buscarTituloAnuncio(1));
        assertEquals("Anúncio reservado", buscarTituloAnuncio(2));
    }

    @Test
    void paginaResultadosRespeitaOrdenacaoFiltrosELimites() throws SQLException {
        inserirAnuncio(1, "Caderno", 10, "Ana", "DISPONIVEL");
        inserirAnuncio(2, "Algebra", 10, "Ana", "DISPONIVEL");
        inserirAnuncio(3, "Biologia", 10, "Ana", "DISPONIVEL");
        atualizarCategoriaEModalidade(1, "Livros", "TROCA");
        atualizarCategoriaEModalidade(2, "Eletrônicos", "DOACAO");
        atualizarCategoriaEModalidade(3, "Livros", "TROCA");

        var primeiraPagina = anuncioRepository.buscarPaginaDisponiveis(
                "", "", "", "TITULO_ASC", 2, 0
        );
        var segundaPagina = anuncioRepository.buscarPaginaDisponiveis(
                "", "", "", "TITULO_ASC", 2, 2
        );

        assertEquals(3, primeiraPagina.total());
        assertEquals(2, primeiraPagina.anuncios().size());
        assertEquals("Algebra", primeiraPagina.anuncios().get(0).getTitulo());
        assertEquals("Biologia", primeiraPagina.anuncios().get(1).getTitulo());
        assertEquals(3, segundaPagina.total());
        assertEquals(1, segundaPagina.anuncios().size());
        assertEquals("Caderno", segundaPagina.anuncios().get(0).getTitulo());

        var filtrada = anuncioRepository.buscarPaginaDisponiveis(
                "bio", "Livros", "TROCA", "RECENTES", 10, 0
        );
        assertEquals(1, filtrada.total());
        assertEquals("Biologia", filtrada.anuncios().get(0).getTitulo());
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

    private void atualizarCategoriaEModalidade(int anuncioId, String categoria, String modalidade)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE anuncios SET categoria = ?, modalidade = ? WHERE id = ?"
             )) {
            statement.setString(1, categoria);
            statement.setString(2, modalidade);
            statement.setInt(3, anuncioId);
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

    private String buscarTituloAnuncio(int anuncioId) throws SQLException {
        return buscarCampoAnuncio(anuncioId, "titulo");
    }

    private String buscarCampoAnuncio(int anuncioId, String campo) throws SQLException {
        assertTrue(java.util.Set.of("titulo", "descricao", "categoria", "modalidade", "foto").contains(campo));
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + campo + " FROM anuncios WHERE id = ?"
             )) {
            statement.setInt(1, anuncioId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getString(campo);
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
