package com.restro.utility;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/** Shared helpers for the small hand-built JSON responses AJAX endpoints across every module send. */
public final class JsonResponseUtil {

    private JsonResponseUtil() {
    }

    public static void writeJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"error\":" + JsonUtil.quote(message) + "}");
        }
    }
}
