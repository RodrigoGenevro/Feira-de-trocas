package br.com.unimart.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilitário para geração de salt e hash seguro de senhas com PBKDF2.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordUtil() {
    }

    public static String gerarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String gerarHash(String senha, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro ao criptografar senha.", e);
        }
    }

    public static boolean verificarSenha(String senhaDigitada, String saltBase64, String hashSalvo) {
        if (senhaDigitada == null || saltBase64 == null || hashSalvo == null) {
            return false;
        }
        String hashCalculado = gerarHash(senhaDigitada, saltBase64);
        return MessageDigest.isEqual(hashCalculado.getBytes(), hashSalvo.getBytes());
    }
}