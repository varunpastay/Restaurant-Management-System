package com.restro.utility;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Shared, dependency-free input validation used by controllers before data
 * reaches the service/DAO layer. Centralized here so every module validates
 * the same way instead of each servlet inventing its own regex.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{7,15}$");
    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isValidEmail(String value) {
        return isNotBlank(value) && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        return isNotBlank(value) && PHONE_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidGstin(String value) {
        return isNotBlank(value) && GSTIN_PATTERN.matcher(value.trim().toUpperCase()).matches();
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.signum() >= 0;
    }

    public static int parseIntOrDefault(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static BigDecimal parseDecimalOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Collapses newlines/tabs in free-text fields (e.g. special instructions)
     * before they're stored, so they render predictably wherever they're
     * later displayed. This is NOT HTML escaping - JSP output must still use
     * JSTL {@code <c:out>} / EL auto-escaping to stay XSS-safe.
     */
    public static String sanitizeSingleLine(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}
