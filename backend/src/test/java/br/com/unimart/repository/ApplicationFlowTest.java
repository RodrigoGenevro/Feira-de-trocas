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
import br.com.unimart.model.Usuario;
import br.com.unimart.service.AnuncioService;
import br.com.unimart.service.AuthService;
import br.com.unimart.service.InteresseService;

class ApplicationFlowTest {

    @TempDir
    Path tempDirectory;

    private String jdbcUrl;

    @BeforeEach
    void prepararBanco() throws SQLException {
        jdbcUrl = "jdbc:sqlite:" + tempDirectory.resolve("flow.db");
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        turma TEXT,
                        senha_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ATIVO',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE anuncios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        titulo TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        modalidade TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        turma TEXT,
                        foto TEXT,
                        usuario_id INTEGER,
                        status TEXT NOT NULL DEFAULT 'DISPONIVEL',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE interesses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        anuncio_id INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        mensagem TEXT NOT NULL,
                        usuario_id INTEGER,
                        status TEXT NOT NULL DEFAULT 'PENDENTE',
                        criado_em TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    @Test
    void cadastroLoginPublicacaoInteresseReservaEConclusaoFuncionamEmConjunto() throws SQLException {
        UsuarioRepository usuarioRepository = new UsuarioRepository(jdbcUrl);
        AuthService authService = new AuthService(usuarioRepository);
        Usuario usuario = authService.cadastrar(
                "Ana Silva", "ana@example.com", "TDS", "senha-segura", "senha-segura"
        );

        assertEquals(usuario.getId(), authService.autenticar("ANA@example.com", "senha-segura").getId());
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.autenticar("ana@example.com", "senha-errada")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.cadastrar(
                        "Ana Silva", "ana@example.com", "TDS", "senha-segura", "senha-segura"
                )
        );

        AnuncioRepository anuncioRepository = new AnuncioRepository(jdbcUrl);
        AnuncioService anuncioService = new AnuncioService(anuncioRepository);
        Anuncio anuncio = new Anuncio();
        anuncio.setUsuarioId(usuario.getId());
        anuncio.setNomeAutor(usuario.getNome());
        anuncio.setTurma(usuario.getTurma());
        anuncio.setTitulo("Livro de Java");
        anuncio.setDescricao("Livro em bom estado.");
        anuncio.setCategoria("Livros");
        anuncio.setModalidade("TROCA");
        anuncioService.publicarAnuncio(anuncio);

        assertEquals(1, anuncioRepository.buscarDisponiveis("", "", "").size());

        InteresseService interesseService = new InteresseService(new InteresseRepository(jdbcUrl));
        interesseService.registrarInteresse(
                new Interesse(anuncio.getId(), "Bia Souza", "Tenho um livro para trocar."),
                22,
                "Bia Souza"
        );
        int interesseId = buscarIdDoInteresse(anuncio.getId());
        anuncioRepository.aceitarInteresse(interesseId, usuario.getId(), usuario.getNome());

        assertEquals("RESERVADO", buscarStatusDoAnuncio(anuncio.getId()));
        assertTrue(anuncioRepository.buscarDisponiveis("", "", "").isEmpty());

        anuncioRepository.concluirAnuncio(anuncio.getId(), usuario.getId(), usuario.getNome());
        assertEquals("CONCLUIDO", buscarStatusDoAnuncio(anuncio.getId()));
        assertTrue(anuncioRepository.buscarDisponiveis("", "", "").isEmpty());
    }

    private int buscarIdDoInteresse(int anuncioId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM interesses WHERE anuncio_id = ?"
             )) {
            statement.setInt(1, anuncioId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt("id");
            }
        }
    }

    private String buscarStatusDoAnuncio(int anuncioId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM anuncios WHERE id = ?"
             )) {
            statement.setInt(1, anuncioId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getString("status");
            }
        }
    }
}
