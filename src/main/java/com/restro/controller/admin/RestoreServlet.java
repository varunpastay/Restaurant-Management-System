package com.restro.controller.admin;

import com.restro.filters.AdminAuthFilter;
import com.restro.utility.AppLogger;
import com.restro.utility.DatabaseBackupUtil;
import com.restro.utility.FlashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;

/**
 * Restores the database from an uploaded .sql dump - a destructive,
 * irreversible action that overwrites current data, so it requires the
 * admin to type a literal confirmation phrase (checked here again
 * server-side, not just via a JS confirm() dialog) before anything runs.
 */
@WebServlet(name = "RestoreServlet", urlPatterns = {"/admin/backup/restore"})
@MultipartConfig(maxFileSize = 200L * 1024 * 1024, maxRequestSize = 200L * 1024 * 1024)
public class RestoreServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(RestoreServlet.class);
    private static final String CONFIRMATION_PHRASE = "RESTORE";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String confirmText = request.getParameter("confirmText");
        if (!CONFIRMATION_PHRASE.equals(confirmText)) {
            FlashUtil.setError(session, "Restore cancelled - you must type " + CONFIRMATION_PHRASE + " exactly to confirm.");
            response.sendRedirect(request.getContextPath() + "/admin/backup");
            return;
        }

        Part filePart = request.getPart("backupFile");
        if (filePart == null || filePart.getSize() == 0) {
            FlashUtil.setError(session, "Please choose a .sql backup file to restore.");
            response.sendRedirect(request.getContextPath() + "/admin/backup");
            return;
        }

        try (var in = filePart.getInputStream()) {
            DatabaseBackupUtil.restore(in);
            LOG.info("Database restored by adminId=" + session.getAttribute(AdminAuthFilter.SESSION_ADMIN_ID));
            FlashUtil.setSuccess(session, "Database restored successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException("Restore was interrupted", e);
        } catch (IOException e) {
            LOG.error("Database restore failed", e);
            FlashUtil.setError(session, "Restore failed: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/backup");
    }
}
