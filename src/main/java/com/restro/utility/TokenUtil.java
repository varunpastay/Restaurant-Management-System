package com.restro.utility;

import java.security.SecureRandom;

/** Generates opaque, URL-safe random tokens (hex, so no encoding needed when embedded directly in a query string) - used for table QR tokens. */
public final class TokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenUtil() {
    }

    public static String generateHexToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        StringBuilder hex = new StringBuilder(byteLength * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
