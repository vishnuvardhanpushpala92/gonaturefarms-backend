package com.gonaturefarms.util;

/**
 * Builds minimal {@code {"success":false,"message":"..."}} JSON bodies by hand.
 * Used only by low-level servlet filters (rate limiting, auth entry points) that
 * write a fixed-shape error response directly to the raw HttpServletResponse,
 * so they don't need to depend on a full JSON mapper.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String failureJson(String message) {
        return "{\"success\":false,\"message\":\"" + escape(message) + "\"}";
    }

    private static String escape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
