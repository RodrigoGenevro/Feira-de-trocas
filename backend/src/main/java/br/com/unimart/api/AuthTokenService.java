package br.com.unimart.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class AuthTokenService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final long TOKEN_LIFETIME_SECONDS = 8 * 60 * 60;
    private final byte[] secret;

    AuthTokenService(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("TROCAE_AUTH_SECRET deve ter pelo menos 32 bytes.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    String emitir(int usuarioId) {
        long expiracao = Instant.now().getEpochSecond() + TOKEN_LIFETIME_SECONDS;
        String conteudo = usuarioId + ":" + expiracao;
        String codificado = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
        return codificado + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(assinar(codificado));
    }

    int validar(String token) {
        if (token == null) {
            throw new NaoAutorizadoException();
        }
        String[] partes = token.split("\\.", -1);
        if (partes.length != 2 || !MessageDigest.isEqual(
                decodificarAssinatura(partes[1]), assinar(partes[0]))) {
            throw new NaoAutorizadoException();
        }

        try {
            String conteudo = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
            String[] claims = conteudo.split(":", -1);
            if (claims.length != 2) {
                throw new NaoAutorizadoException();
            }
            int usuarioId = Integer.parseInt(claims[0]);
            long expiracao = Long.parseLong(claims[1]);
            if (usuarioId <= 0 || expiracao <= Instant.now().getEpochSecond()) {
                throw new NaoAutorizadoException();
            }
            return usuarioId;
        } catch (IllegalArgumentException erro) {
            throw new NaoAutorizadoException();
        }
    }

    private byte[] assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.InvalidKeyException | NoSuchAlgorithmException erro) {
            throw new IllegalStateException("Não foi possível validar a sessão.", erro);
        }
    }

    private byte[] decodificarAssinatura(String assinatura) {
        try {
            return Base64.getUrlDecoder().decode(assinatura);
        } catch (IllegalArgumentException erro) {
            return new byte[0];
        }
    }

    static final class NaoAutorizadoException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
