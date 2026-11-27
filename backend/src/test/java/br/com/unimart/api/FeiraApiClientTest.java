package br.com.unimart.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import br.com.unimart.controller.FeiraController;

class FeiraApiClientTest {

    @Test
    void rejeitaHttpForaDoHostLocal() {
        assertThrows(IllegalArgumentException.class,
                () -> new FeiraApiClient("http://api.example.com/api"));
    }

    @Test
    void loginGuardaTokenEEnviaBearerNasProximasRequisicoes() throws Exception {
        AtomicReference<String> autorizacao = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/auth/login", exchange -> responder(
                exchange,
                200,
                "{\"usuario\":{\"id\":12,\"nome\":\"Ana\",\"email\":\"ana@example.com\","
                        + "\"turma\":\"TDS\",\"status\":\"ATIVO\"},\"token\":\"token-assinado\"}"
        ));
        server.createContext("/api/listings/mine", exchange -> {
            autorizacao.set(exchange.getRequestHeaders().getFirst("Authorization"));
            responder(exchange, 200, "[]");
        });
        server.start();
        try {
            FeiraApiClient client = new FeiraApiClient(
                    "http://localhost:" + server.getAddress().getPort() + "/api"
            );

            assertEquals(12, client.login("ana@example.com", "senha").getId());
            assertEquals(0, client.buscarHistoricoUsuario().size());
            assertEquals("Bearer token-assinado", autorizacao.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void controllerUsaApiCompartilhadaParaDenunciasInteressesECancelamento() throws Exception {
        List<String> requisicoes = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/api/auth/login".equals(path)) {
                responder(
                        exchange,
                        200,
                        "{\"usuario\":{\"id\":12,\"nome\":\"Ana\",\"email\":\"ana@example.com\","
                                + "\"turma\":\"TDS\",\"status\":\"ATIVO\"},\"token\":\"token-assinado\"}"
                );
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requisicoes.add(exchange.getRequestMethod() + " " + path + " " + body);
            responder(exchange, 200, "/api/interests/mine".equals(path) ? "[]" : "{}");
        });
        server.start();
        try {
            FeiraController controller = new FeiraController(new FeiraApiClient(
                    "http://localhost:" + server.getAddress().getPort() + "/api"
            ));
            assertEquals(12, controller.login("ana@example.com", "senha").getId());
            controller.cancelarInteresse(21);
            assertEquals(0, controller.buscarInteressesDoUsuario().size());
            controller.reativarAnuncio(32);
            controller.denunciarAnuncio(44, "Inadequado", "Descrição");

            assertEquals(List.of(
                    "POST /api/interests/21/cancel",
                    "GET /api/interests/mine",
                    "POST /api/listings/32/reactivate",
                    "POST /api/reports"
            ), requisicoes.stream().map(requisicao -> requisicao.split(" ", 3)[0] + " "
                    + requisicao.split(" ", 3)[1]).toList());
            assertTrue(requisicoes.get(3).contains("\"listingId\":44"));
            assertTrue(requisicoes.get(3).contains("\"reason\":\"Inadequado\""));
            assertTrue(requisicoes.get(3).contains("\"details\":\"Descrição\""));
        } finally {
            server.stop(0);
        }
    }

    private void responder(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
