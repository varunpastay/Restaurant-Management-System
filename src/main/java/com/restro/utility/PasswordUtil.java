package com.restro.utility;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * JDK-only password hashing: PBKDF2WithHmacSHA256, a random salt per
 * password, stored as separate Base64 columns (no external hashing library
 * needed - javax.crypto ships with the JDK).
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordUtil() {
    }

    /** Generates a new random salt, Base64-encoded, for a new password. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Hashes a plaintext password with the given Base64 salt, returning the Base64-encoded hash. */
    public static String hash(String plaintextPassword, String base64Salt) {
        byte[] salt = Base64.getDecoder().decode(base64Salt);
        return Base64.getEncoder().encodeToString(pbkdf2(plaintextPassword.toCharArray(), salt));
    }

    /** Constant-time comparison of a candidate password against a stored hash+salt. */
    public static boolean matches(String plaintextPassword, String storedHash, String base64Salt) {
        String candidateHash = hash(plaintextPassword, base64Salt);
        return constantTimeEquals(candidateHash, storedHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing algorithm unavailable: " + ALGORITHM, e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < aBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }
}
