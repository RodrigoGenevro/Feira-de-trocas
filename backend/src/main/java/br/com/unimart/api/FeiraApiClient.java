package br.com.unimart.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Interesse;
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.model.Usuario;

public final class FeiraApiClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final String baseUrl;
    private final HttpClient httpClient;
    private String token;

    public FeiraApiClient(String baseUrl) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("TROCAE_API_URL deve ser uma URL HTTP ou HTTPS.");
        }
        URI uri = URI.create(baseUrl);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("TROCAE_API_URL deve ser uma URL base válida, sem credenciais.");
        }
        boolean localHttp = "http".equalsIgnoreCase(scheme)
                && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host));
        if (!"https".equalsIgnoreCase(scheme) && !localHttp) {
            throw new IllegalArgumentException(
                    "TROCAE_API_URL deve usar HTTPS, exceto para uma API local em localhost."
            );
        }
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static FeiraApiClient fromEnvironment() {
        String baseUrl = System.getenv().getOrDefault("TROCAE_API_URL", "http://localhost:8080/api");
        return new FeiraApiClient(baseUrl);
    }

    public Usuario login(String email, String senha) throws SQLException {
        return autenticar("auth/login", Map.of("email", valor(email), "password", valor(senha)));
    }

    public Usuario cadastrar(
            String nome, String email, String turma, String senha, String confirmarSenha
    ) throws SQLException {
        return autenticar("auth/register", Map.of(
                "name", valor(nome), "email", valor(email), "class", valor(turma),
                "password", valor(senha), "confirmPassword", valor(confirmarSenha)
        ));
    }

    public Usuario atualizarPerfil(
            Usuario usuario, String nome, String email, String turma, String novaSenha, String confirmarSenha
    ) throws SQLException {
        JsonNode response = requisitar("PATCH", "profile", Map.of(
                "name", valor(nome), "email", valor(email), "class", valor(turma),
                "newPassword", novaSenha == null ? "" : novaSenha,
                "confirmPassword", confirmarSenha == null ? "" : confirmarSenha
        ), JsonNode.class);
        return usuario(response);
    }

    public void logout() {
        token = null;
    }

    public PaginaAnuncios buscarPaginaAnuncios(
            String termo, String categoria, String modalidade, String ordenacao, int pagina, int tamanho
    ) throws SQLException {
        String url = "listings?term=" + codificar(termo) + "&category=" + codificar(categoria)
                + "&mode=" + codificar(modalidade) + "&sort=" + codificar(ordenacao)
                + "&page=" + pagina + "&size=" + tamanho;
        return requisitar("GET", url, null, PaginaAnuncios.class);
    }

    public Anuncio criarAnuncio(
            String titulo, String descricao, String categoria, String modalidade, String foto
    ) throws SQLException {
        return requisitar("POST", "listings", Map.of(
                "title", valor(titulo), "description", valor(descricao), "category", valor(categoria),
                "mode", valor(modalidade), "photo", valor(foto)
        ), Anuncio.class);
    }

    public void atualizarAnuncio(
            int anuncioId, String titulo, String descricao, String categoria, String modalidade, String foto
    ) throws SQLException {
        requisitar("PUT", "listings/" + anuncioId, Map.of(
                "title", valor(titulo), "description", valor(descricao), "category", valor(categoria),
                "mode", valor(modalidade), "photo", valor(foto)
        ), Void.class);
    }

    public void enviarInteresse(int anuncioId, String mensagem) throws SQLException {
        requisitar("POST", "interests", Map.of("listingId", anuncioId, "message", valor(mensagem)), Void.class);
    }

    public List<Interesse> buscarInteresses(int anuncioId) throws SQLException {
        return requisitarLista("GET", "listings/" + anuncioId + "/interests", Interesse.class);
    }

    public void aceitarInteresse(int interesseId) throws SQLException {
        requisitar("POST", "interests/" + interesseId + "/accept", Map.of(), Void.class);
    }

    public void concluirAnuncio(int anuncioId) throws SQLException {
        requisitar("POST", "listings/" + anuncioId + "/complete", Map.of(), Void.class);
    }

    public void reabrirAnuncio(int anuncioId) throws SQLException {
        requisitar("POST", "listings/" + anuncioId + "/reopen", Map.of(), Void.class);
    }

    public List<Anuncio> buscarHistoricoUsuario() throws SQLException {
        return requisitarLista("GET", "listings/mine", Anuncio.class);
    }

    private Usuario autenticar(String endpoint, Map<String, String> dados) throws SQLException {
        JsonNode response = requisitar("POST", endpoint, dados, JsonNode.class);
        JsonNode tokenNode = response.get("token");
        JsonNode userNode = response.get("usuario");
        if (tokenNode == null || !tokenNode.isTextual() || userNode == null || !userNode.isObject()) {
            throw new SQLException("A API retornou uma resposta de autenticação inválida.");
        }
        token = tokenNode.asText();
        return usuario(userNode);
    }

    private Usuario usuario(JsonNode dados) throws SQLException {
        JsonNode id = dados.get("id");
        if (id == null || !id.canConvertToInt()) {
            throw new SQLException("A API retornou dados de usuário inválidos.");
        }
        return new Usuario(
                id.intValue(), texto(dados.get("nome")), texto(dados.get("email")),
                texto(dados.get("turma")), null, null, texto(dados.get("status")), texto(dados.get("criadoEm"))
        );
    }

    private <T> List<T> requisitarLista(String method, String path, Class<T> elementType) throws SQLException {
        JsonNode resposta = requisitar(method, path, null, JsonNode.class);
        if (resposta == null || !resposta.isArray()) {
            throw new SQLException("A API retornou uma lista inválida.");
        }
        try {
            return JSON.convertValue(
                    resposta, JSON.getTypeFactory().constructCollectionType(List.class, elementType)
            );
        } catch (IllegalArgumentException erro) {
            throw new SQLException("A API retornou uma lista inválida.", erro);
        }
    }

    private <T> T requisitar(String method, String path, Object body, Class<T> tipo) throws SQLException {
        String resposta = requisitarTexto(method, path, body);
        if (tipo == Void.class || resposta.isBlank()) {
            return null;
        }
        if (tipo == JsonNode.class) {
            try {
                return tipo.cast(JSON.readTree(resposta));
            } catch (IOException erro) {
                throw new SQLException("A API retornou uma resposta inválida.", erro);
            }
        }
        try (JsonParser parser = JSON.getFactory().createParser(resposta)) {
            return JSON.readValue(parser, tipo);
        } catch (IOException erro) {
            throw new SQLException("A API retornou uma resposta inválida.", erro);
        }
    }

    private String requisitarTexto(String method, String path, Object body) throws SQLException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + "/" + path))
                .timeout(Duration.ofSeconds(20)).header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            try {
                builder.header("Content-Type", "application/json; charset=utf-8")
                        .method(method, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
            } catch (IOException erro) {
                throw new SQLException("Não foi possível preparar a requisição para a API.", erro);
            }
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw criarErro(response.statusCode(), response.body());
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new SQLException("A chamada à API foi interrompida.", erro);
        } catch (IOException erro) {
            throw new SQLException("Não foi possível conectar à API Trocaê.", erro);
        }
    }

    private SQLException criarErro(int status, String body) {
        String mensagem = "A API Trocaê respondeu com erro HTTP " + status + ".";
        try {
            JsonNode payload = JSON.readTree(body);
            JsonNode error = payload.get("error");
            if (error != null && !error.isNull()) {
                mensagem = error.asText();
            }
        } catch (IOException erro) {
            return new SQLException(mensagem, erro);
        }
        if (status == 400) {
            return new SQLException(mensagem);
        }
        if (status == 401 || status == 403 || status == 409) {
            return new SQLException(mensagem);
        }
        return new SQLException(mensagem + " Verifique a conexão e tente novamente.");
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor(valor), StandardCharsets.UTF_8);
    }

    private String texto(JsonNode valor) {
        return valor == null || valor.isNull() ? "" : valor.asText();
    }

    private String valor(String value) {
        return value == null ? "" : value;
    }
}
