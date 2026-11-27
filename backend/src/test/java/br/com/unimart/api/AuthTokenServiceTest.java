package br.com.unimart.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AuthTokenServiceTest {

    private static final String SECRET = "a-secret-with-at-least-32-bytes-long";

    @Test
    void tokenAssinadoRetornaIdDoUsuario() {
        AuthTokenService service = new AuthTokenService(SECRET);
        String token = service.emitir(42);

        assertEquals(42, service.validar(token));
    }

    @Test
    void tokenAlteradoOuSecretCurtoSaoRejeitados() {
        AuthTokenService service = new AuthTokenService(SECRET);
        String token = service.emitir(42);
        String[] parts = token.split("\\.");
        String changedPayload = (parts[0].startsWith("A") ? "B" : "A") + parts[0].substring(1);

        assertThrows(AuthTokenService.NaoAutorizadoException.class,
                () -> service.validar(changedPayload + "." + parts[1]));
        assertThrows(IllegalArgumentException.class, () -> new AuthTokenService("short"));
    }
}
