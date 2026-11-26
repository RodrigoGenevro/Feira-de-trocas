package br.com.unimart.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitário para geração de salt e hash seguro de senhas com PBKDF2.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String gerarSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String gerarHash(String senha, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            char[] senhaChars = senha.toCharArray();
            PBEKeySpec spec = new PBEKeySpec(senhaChars, salt, ITERATIONS, KEY_LENGTH);
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
                byte[] hash = factory.generateSecret(spec).getEncoded();
                return Base64.getEncoder().encodeToString(hash);
            } finally {
                spec.clearPassword();
                Arrays.fill(senhaChars, '\0');
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro ao criptografar senha.", e);
        }
    }

    public static boolean verificarSenha(String senhaDigitada, String saltBase64, String hashSalvo) {
        if (senhaDigitada == null || saltBase64 == null || hashSalvo == null) {
            return false;
        }
        String hashCalculado = gerarHash(senhaDigitada, saltBase64);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashSalvo.getBytes(StandardCharsets.UTF_8)
        );
    }
}