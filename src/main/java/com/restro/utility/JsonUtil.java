package com.restro.utility;

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
