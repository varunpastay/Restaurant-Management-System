package com.restro.utility;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Minimal, dependency-free JSON string escaping for hand-built AJAX
 * responses. No JSON library is in the approved stack, and every response
 * this application sends is a small, flat, servlet-controlled shape, so a
 * full parser/binder would be more machinery than the problem needs -
 * servlets build JSON text directly with a StringBuilder, escaping any
 * user-supplied string through this helper.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Converts a DB timestamp to an unambiguous, UTC-marked ISO string
     * (e.g. "2026-07-11T10:53:00Z") for the browser's JavaScript to parse
     * with {@code new Date(...)}. A bare {@code LocalDateTime.toString()}
     * has no timezone marker, so the browser assumes it's already in the
     * browser's own local time - harmless when the app server and browser
     * happen to share a timezone (true for local dev on one machine), but
     * wrong once they don't (a cloud-hosted server and a customer's phone),
     * which showed up as kitchen/counter order ages reading hours off.
     * {@code ZoneId.systemDefault()} is whatever timezone this JVM's clock
     * actually is (matches what produced the value via {@code now()} at
     * insert time), so this is correct regardless of which timezone the
     * server happens to run in.
     */
    public static String toIsoInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Wraps a value in double quotes with escaping, or the literal null if the value is null. */
    public static String quote(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }
}
