package com.restro.controller.admin;

import com.restro.filters.AdminAuthFilter;
import com.restro.utility.AppLogger;
import com.restro.utility.DatabaseBackupUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

/** Streams a full {@code mysqldump} of the restaurant database as a downloadable .sql file - "Backup Database Option". */
@WebServlet(name = "BackupDownloadServlet", urlPatterns = {"/admin/backup/download"})
public class BackupDownloadServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(BackupDownloadServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/sql");
        response.setHeader("Content-Disposition", "attachment; filename=\"restaurant-db-backup-" + LocalDate.now() + ".sql\"");
        try {
            DatabaseBackupUtil.backup(response.getOutputStream());
            LOG.info("Database backup downloaded by adminId=" + request.getSession().getAttribute(AdminAuthFilter.SESSION_ADMIN_ID));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException("Backup was interrupted", e);
        } catch (IOException e) {
            LOG.error("Database backup failed", e);
            throw new ServletException("Backup failed - is mysqldump installed and on the PATH "
                    + "(or configured via mysql.bin.dir in app.properties)?", e);
        }
    }
}
