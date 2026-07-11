package com.restro.controller.common;

import com.restro.utility.AppConfig;
import com.restro.utility.AppLogger;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Streams uploaded images (logos, banners, category/food photos, generated
 * QR codes) from the configurable upload.dir, which lives outside the
 * deployed WAR and therefore isn't reachable by the container's default
 * static file serving. Falls back to a small inline placeholder SVG if the
 * file is missing, so a never-uploaded image never shows as a broken-image icon.
 */
@WebServlet(name = "ImageServingServlet", urlPatterns = {"/uploads/*"})
public class ImageServingServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(ImageServingServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || pathInfo.contains("..")) {
            servePlaceholder(response);
            return;
        }

        Path root = Paths.get(AppConfig.get("upload.dir")).normalize();
        Path file = root.resolve(pathInfo.substring(1)).normalize();
        if (!file.startsWith(root) || !Files.exists(file) || !Files.isRegularFile(file)) {
            servePlaceholder(response);
            return;
        }

        String contentType = Files.probeContentType(file);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Cache-Control", "public, max-age=3600");
        Files.copy(file, response.getOutputStream());
    }

    private void servePlaceholder(HttpServletResponse response) {
        response.setContentType("image/svg+xml");
        try (Writer out = response.getWriter()) {
            out.write("<svg xmlns='http://www.w3.org/2000/svg' width='300' height='200'>"
                    + "<rect width='100%' height='100%' fill='#eee'/>"
                    + "<text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' "
                    + "fill='#999' font-family='sans-serif' font-size='16'>No Image</text></svg>");
        } catch (IOException e) {
            LOG.warn("Failed to serve placeholder image", e);
        }
    }
}
