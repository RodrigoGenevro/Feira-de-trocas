package br.com.unimart.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Denuncia;
import br.com.unimart.model.Interesse;
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.model.Usuario;
import br.com.unimart.service.AnuncioService;
import br.com.unimart.service.AuthService;
import br.com.unimart.service.DenunciaService;
import br.com.unimart.service.InteresseService;
import br.com.unimart.util.DatabaseConfig;

public final class ApiServer {

    private static final int MAX_REQUEST_BYTES = 1_048_576;
    private static final Logger LOGGER = Logger.getLogger(ApiServer.class.getName());
    private static final ObjectMapper JSON = new ObjectMapper();

    private final AuthTokenService tokenService;
    private final AuthService authService = new AuthService();
    private final AnuncioService anuncioService = new AnuncioService();
    private final DenunciaService denunciaService = new DenunciaService();
    private final InteresseService interesseService = new InteresseService();

    private ApiServer(String secret) {
        tokenService = new AuthTokenService(secret);
    }

    public static void main(String[] args) throws IOException, SQLException {
        String databaseUrl = DatabaseConfig.getActiveDbUrl();
        if (!databaseUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("Configure TROCAE_DATABASE_URL com a URL JDBC do PostgreSQL do Supabase.");
        }
        DatabaseConfig.initDatabase();

        String secret = System.getenv("TROCAE_AUTH_SECRET");
        ApiServer application = new ApiServer(secret);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", application::handleHealth);
        server.createContext("/api/", application::handleApi);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        LOGGER.info("API Trocaê iniciada na porta " + port);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                responder(exchange, 405, Map.of("error", "Método não permitido."));
                return;
            }
            try (Connection connection = DatabaseConfig.getConnection()) {
                boolean connected = connection.isValid(2);
                responder(exchange, connected ? 200 : 503,
                        Map.of("status", connected ? "ok" : "unavailable"));
            } catch (SQLException erro) {
                LOGGER.log(Level.WARNING, "A verificação de saúde não conseguiu acessar o banco.", erro);
                responder(exchange, 503, Map.of("status", "unavailable"));
            }
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange) throws IOException {
        try {
            processar(exchange);
        } catch (AuthTokenService.NaoAutorizadoException erro) {
            responder(exchange, 401, Map.of("error", "Sessão inválida ou expirada."));
        } catch (IllegalArgumentException erro) {
            responder(exchange, 400, Map.of("error", mensagem(erro)));
        } catch (IllegalStateException erro) {
            responder(exchange, 409, Map.of("error", mensagem(erro)));
        } catch (SQLException erro) {
            LOGGER.log(Level.SEVERE, "Falha de banco de dados na API.", erro);
            responder(exchange, 500, Map.of("error", "Não foi possível concluir a operação no banco de dados."));
        } catch (IOException erro) {
            responder(exchange, 400, Map.of("error", "Corpo da requisição inválido."));
        } catch (RuntimeException erro) {
            LOGGER.log(Level.SEVERE, "Falha inesperada na API.", erro);
            responder(exchange, 500, Map.of("error", "Ocorreu um erro interno."));
        } finally {
            exchange.close();
        }
    }

    private void processar(HttpExchange exchange) throws IOException, SQLException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, Object> body = lerCorpo(exchange);

        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            Usuario usuario = authService.autenticar(texto(body, "email"), texto(body, "password"));
            responderAutenticacao(exchange, usuario);
            return;
        }
        if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            Usuario usuario = authService.cadastrar(
                    texto(body, "name"), texto(body, "email"), texto(body, "class"),
                    texto(body, "password"), texto(body, "confirmPassword")
            );
            responderAutenticacao(exchange, usuario);
            return;
        }

        int usuarioId = autenticar(exchange);
        String nomeUsuario = authService.buscarUsuarioPorId(usuarioId).getNome();

        if ("PATCH".equals(method) && "/api/profile".equals(path)) {
            Usuario usuario = authService.buscarUsuarioPorId(usuarioId);
            Usuario atualizado = authService.atualizarPerfil(
                    usuario, texto(body, "name"), texto(body, "email"), texto(body, "class"),
                    texto(body, "newPassword"), texto(body, "confirmPassword")
            );
            responder(exchange, 200, UsuarioPublico.de(atualizado));
        } else if ("GET".equals(method) && "/api/listings".equals(path)) {
            Map<String, String> query = lerQuery(exchange);
            PaginaAnuncios pagina = anuncioService.listarPaginaAnuncios(
                    query.getOrDefault("term", ""),
                    query.getOrDefault("category", ""),
                    query.getOrDefault("mode", ""),
                    query.getOrDefault("sort", "RECENTES"),
                    inteiro(query.getOrDefault("page", "0")),
                    inteiro(query.getOrDefault("size", "12"))
            );
            responder(exchange, 200, pagina);
        } else if ("GET".equals(method) && "/api/listings/mine".equals(path)) {
            responder(exchange, 200, anuncioService.listarHistoricoUsuario(usuarioId, nomeUsuario));
        } else if ("POST".equals(method) && "/api/listings".equals(path)) {
            Anuncio anuncio = criarAnuncio(body, usuarioId, nomeUsuario);
            responder(exchange, 201, anuncioService.publicarAnuncio(anuncio));
        } else if (path.startsWith("/api/listings/")) {
            processarAnuncio(exchange, method, path, body, usuarioId, nomeUsuario);
        } else if ("POST".equals(method) && "/api/interests".equals(path)) {
            int anuncioId = inteiro(body.get("listingId"));
            interesseService.registrarInteresse(
                    new Interesse(anuncioId, nomeUsuario, texto(body, "message")), usuarioId, nomeUsuario
            );
            responder(exchange, 201, Map.of("status", "created"));
        } else if ("GET".equals(method) && "/api/interests/mine".equals(path)) {
            responder(exchange, 200, interesseService.listarInteressesDoUsuario(usuarioId));
        } else if ("POST".equals(method) && "/api/reports".equals(path)) {
            denunciaService.registrar(new Denuncia(
                    inteiro(body.get("listingId")), usuarioId, nomeUsuario,
                    texto(body, "reason"), texto(body, "details")
            ));
            responder(exchange, 201, Map.of("status", "created"));
        } else if (path.startsWith("/api/interests/")) {
            processarInteresse(exchange, method, path, usuarioId, nomeUsuario);
        } else {
            responder(exchange, 404, Map.of("error", "Endpoint não encontrado."));
        }
    }

    private void processarAnuncio(
            HttpExchange exchange, String method, String path, Map<String, Object> body,
            int usuarioId, String nomeUsuario
    ) throws SQLException, IOException {
        String[] partes = path.substring("/api/listings/".length()).split("/");
        int anuncioId = inteiro(partes[0]);
        if (partes.length == 2 && "interests".equals(partes[1]) && "GET".equals(method)) {
            responder(exchange, 200, interesseService.listarInteressesDoAnuncio(anuncioId, usuarioId, nomeUsuario));
        } else if (partes.length == 2 && "complete".equals(partes[1]) && "POST".equals(method)) {
            anuncioService.concluirAnuncio(anuncioId, usuarioId, nomeUsuario);
            responder(exchange, 204, null);
        } else if (partes.length == 2 && "reopen".equals(partes[1]) && "POST".equals(method)) {
            anuncioService.reabrirAnuncio(anuncioId, usuarioId, nomeUsuario);
            responder(exchange, 204, null);
        } else if (partes.length == 2 && "reactivate".equals(partes[1]) && "POST".equals(method)) {
            anuncioService.reativarAnuncio(anuncioId, usuarioId, nomeUsuario);
            responder(exchange, 204, null);
        } else if (partes.length == 1 && "PUT".equals(method)) {
            Anuncio anuncio = criarAnuncio(body, usuarioId, nomeUsuario);
            anuncio.setId(anuncioId);
            anuncioService.atualizarAnuncio(anuncio, usuarioId, nomeUsuario);
            responder(exchange, 204, null);
        } else {
            responder(exchange, 404, Map.of("error", "Endpoint não encontrado."));
        }
    }

    private void processarInteresse(
            HttpExchange exchange, String method, String path, int usuarioId, String nomeUsuario
    ) throws SQLException, IOException {
        String[] partes = path.substring("/api/interests/".length()).split("/");
        if (partes.length == 2 && "accept".equals(partes[1]) && "POST".equals(method)) {
            anuncioService.aceitarInteresse(inteiro(partes[0]), usuarioId, nomeUsuario);
            responder(exchange, 204, null);
        } else if (partes.length == 2 && "cancel".equals(partes[1]) && "POST".equals(method)) {
            interesseService.cancelarInteresse(inteiro(partes[0]), usuarioId);
            responder(exchange, 204, null);
        } else {
            responder(exchange, 404, Map.of("error", "Endpoint não encontrado."));
        }
    }

    private Anuncio criarAnuncio(Map<String, Object> body, int usuarioId, String nomeUsuario) throws SQLException {
        Usuario usuario = authService.buscarUsuarioPorId(usuarioId);
        Anuncio anuncio = new Anuncio();
        anuncio.setUsuarioId(usuarioId);
        anuncio.setNomeAutor(nomeUsuario);
        anuncio.setTurma(usuario.getTurma());
        anuncio.setTitulo(texto(body, "title"));
        anuncio.setDescricao(texto(body, "description"));
        anuncio.setCategoria(texto(body, "category"));
        anuncio.setModalidade(texto(body, "mode"));
        anuncio.setFotoUrl(texto(body, "photo"));
        return anuncio;
    }

    private int autenticar(HttpExchange exchange) {
        String autorizacao = exchange.getRequestHeaders().getFirst("Authorization");
        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            throw new AuthTokenService.NaoAutorizadoException();
        }
        return tokenService.validar(autorizacao.substring("Bearer ".length()));
    }

    private void responderAutenticacao(HttpExchange exchange, Usuario usuario) throws IOException {
        responder(exchange, 200, new RespostaAutenticacao(UsuarioPublico.de(usuario), tokenService.emitir(usuario.getId())));
    }

    private Map<String, Object> lerCorpo(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
        if (bytes.length > MAX_REQUEST_BYTES) {
            throw new IllegalArgumentException("A requisição excede o limite permitido.");
        }
        if (bytes.length == 0) {
            return Map.of();
        }
        return JSON.readValue(bytes, new TypeReference<>() { });
    }

    private Map<String, String> lerQuery(HttpExchange exchange) {
        Map<String, String> resultado = new LinkedHashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) {
            return resultado;
        }
        Arrays.stream(query.split("&")).forEach(par -> {
            String[] partes = par.split("=", 2);
            String chave = decodificar(partes[0]);
            String valor = partes.length == 2 ? decodificar(partes[1]) : "";
            resultado.put(chave, valor);
        });
        return resultado;
    }

    private String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private String texto(Map<String, Object> body, String chave) {
        Object valor = body.get(chave);
        return valor == null ? "" : valor.toString();
    }

    private int inteiro(Object valor) {
        try {
            return Integer.parseInt(String.valueOf(valor));
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Um identificador ou parâmetro numérico é inválido.", erro);
        }
    }

    private String mensagem(RuntimeException erro) {
        return erro.getMessage() == null || erro.getMessage().isBlank()
                ? "A requisição não pôde ser validada." : erro.getMessage();
    }

    private void responder(HttpExchange exchange, int status, Object conteudo) throws IOException {
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = JSON.writeValueAsBytes(conteudo);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public record UsuarioPublico(
            Integer id, String nome, String email, String turma, String status, String criadoEm
    ) {
        private static UsuarioPublico de(Usuario usuario) {
            return new UsuarioPublico(
                    usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTurma(),
                    usuario.getStatus(), usuario.getCriadoEm()
            );
        }
    }

    public record RespostaAutenticacao(UsuarioPublico usuario, String token) {
    }

}
