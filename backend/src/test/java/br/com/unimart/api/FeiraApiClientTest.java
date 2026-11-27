package br.com.unimart.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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

    private void responder(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
