package com.restro.controller.common;

import com.restro.dao.UploadedFileDao;
import com.restro.daoimpl.UploadedFileDaoImpl;
import com.restro.dto.UploadedFileDTO;
import com.restro.utility.AppLogger;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

/**
 * Streams uploaded images (logos, banners, category/food photos, generated
 * QR codes) from the uploaded_file table. Falls back to a small inline
 * placeholder SVG if the path isn't found, so a never-uploaded image never
 * shows as a broken-image icon.
 */
@WebServlet(name = "ImageServingServlet", urlPatterns = {"/uploads/*"})
public class ImageServingServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(ImageServingServlet.class);
    private final UploadedFileDao uploadedFileDao = new UploadedFileDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || pathInfo.contains("..")) {
            servePlaceholder(response);
            return;
        }

        UploadedFileDTO file;
        try {
            file = uploadedFileDao.findByPath("/uploads" + pathInfo);
        } catch (SQLException e) {
            LOG.error("Failed to look up uploaded file " + pathInfo, e);
            servePlaceholder(response);
            return;
        }
        if (file == null) {
            servePlaceholder(response);
            return;
        }

        response.setContentType(file.getContentType());
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.getOutputStream().write(file.getData());
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
